package org.codefish.fixturefeedpro;

import org.codefish.fixturefeedpro.view.FeedbackView;
import org.codefish.fixturefeedpro.view.PreferenceScreen;
import org.codefish.fixturefeedpro.tv.TVListingComparator;
import org.codefish.fixturefeedpro.tv.TVListingHelper;
import org.codefish.fixturefeedpro.tv.TVListing;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codefish.fixturefeedpro.util.AndroidUtils;
import org.codefish.fixturefeedpro.util.AppRater;

/**
 * Main fixture feed class
 * @author Matthew
 */
public class FixtureFeed extends Activity {

    private ListView listView;
    private ListingAdapter listAdapter;
    private ProgressDialog dialog;
    private ArrayList<TVListing> listings = new ArrayList<TVListing>();
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy - HH:mm");
    private static final TVListingComparator comparator = new TVListingComparator();
    private Uri eventsUri = Uri.parse("content://calendar/events");
    private List<String> excludeTerms;
    private int MAX_THREADS;
    private int MAX_DATA_AGE;
    private int refreshInterval;
    private final String SPLIT_CHAR = "-";
    private String SAVE_FILE;
    private Timer refreshTimer;
    private boolean updating=false;
    // Handler for callbacks to the UI thread
    final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_RIGHT_ICON);

        Resources res = getResources();
        this.MAX_DATA_AGE = res.getInteger(R.integer.days_to_keep_data);//get the maxiumum time we keep the data in days
        this.SAVE_FILE = res.getString(R.string.save_file);//the file to store serialized objects to

        listings.clear();//clear the listings list
        //try and get stored data
        try {
            ArrayList<TVListing> tmpList = TVListingHelper.read(this.SAVE_FILE, this.MAX_DATA_AGE, this);
            Date today = new Date();
            //filter out all the listings that have already been shown
            for (TVListing listing : tmpList) {
                if (!listing.getEnd().before(today)) {
                    listings.add(listing);
                }
            }
        } catch (Exception e) {
            Log.w("DEBUG", "Error getting saved data");
        }

        listings = (ArrayList<TVListing>) removeDuplicates(listings);

        Collections.sort(listings, comparator);//sort the listings into date order

        listAdapter = new ListingAdapter(this, android.R.layout.simple_list_item_1, listings);//set the list adapter
        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.ListView01);

        // By using setAdpater method in listview we an add string array in list.
        listView.setAdapter(listAdapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                AlertDialog.Builder adb = new AlertDialog.Builder(FixtureFeed.this);
                TVListing l = (TVListing) listView.getItemAtPosition(position);
                adb.setTitle(l.getChannelName() + "-" + dateFormatter.format(l.getStart()).toString());
                adb.setMessage(l.getDescription());
                adb.setPositiveButton("Ok", null);
                adb.show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
                TVListing l = (TVListing) listView.getItemAtPosition(pos);
                addCalEvent(l);
                return true; //tell android we've handled the event and thats it, no propgation
            }
        });


        this.MAX_THREADS = res.getInteger(R.integer.max_threads);//set the maximum number of threads for the updater
        this.refreshInterval = res.getInteger(R.integer.refresh_interval_mins)*60000;//refresh interval in milliseconds        
        loadExcludeList(res);

        if (listings == null || listings.isEmpty()) {
            refreshShows(false);
        }

        AndroidUtils.showWelcomeMessage(this);

        //ask the user to rate the app after 3 days (or 7 launches) of use
        AppRater.app_launched(this);

        if(KeywordPrefHelper.getAutoUpdate(this)){
            initTimer();//start the background updater timer
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!KeywordPrefHelper.getAutoUpdate(this)){
            if(refreshTimer != null){
                refreshTimer.cancel();//cancel the timer if we've turned auto updating off
                refreshTimer = null;
                hideReloadIcon();
            }
        }else{
            if(refreshTimer == null){
                initTimer();//start the timer if it isn't already started
            }
        }
    }


    /**
     * create the options menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * when an options button is pressed
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshButton:
                refreshShows(false);
                break;
            case R.id.settingButton:
                startActivity(new Intent(this, PreferenceScreen.class));
                break;
            case R.id.feedbackButton:
                startActivity(new Intent(this, FeedbackView.class));//start the feeback view
                //create form
                break;
        }
        return true;
    }

    /**
     * delete the save file and clear the list of shows
     */
    private void clearShows() {
        //delete the save file
        deleteFile(this.SAVE_FILE);
        try {
            //update the listview with an empty list
            updateShows(new ArrayList<TVListing>(),true);
        } catch (Exception e) {
            Log.w("DEBUG", e.toString());
        }
    }

    /**
     * refresh the list of shows asynchronously
     */
    private void refreshShows(Boolean useXml) {
         if(isUpdating()){//if we're not already updating then perform a new update
             return;
         }         
        //check for internet connectivity before trying to update the list
        if (AndroidUtils.haveInternet(getApplicationContext())) {
            setUpdating(true);
            if (dialog == null) {
                dialog = new ProgressDialog(this);

                // make the progress bar cancelable
                dialog.setCancelable(true);

                dialog.setMax(100);//max 100%
            }

            // set a message text
            dialog.setMessage("Loading...");

            // show it
            try{
                dialog.show();
            }catch(Exception e){
                Log.w("DEBUG", "error showing dialog");
            }

           List<String> channels = getChannels();

            String[] myStringArray = channels.toArray(new String[channels.size()]);//convert the arraylist to an array to pass to the async task
          
             new Updater(this, KeywordPrefHelper.getCategory(this)).execute(myStringArray);//update the ui
        } else {
            Toast.makeText(this, "No internet connectivity found", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * get a list of channels to search
     * @return
     */
    private List<String> getChannels() {
        Resources res = getResources();

        ArrayList<String> searchChannels = loadChannels(res, R.array.channels);

        if (KeywordPrefHelper.searchSkySports(this)) {
            searchChannels.addAll(loadChannels(res, R.array.sky_channels));
        }

        if (KeywordPrefHelper.searchEspn(this)) {
            searchChannels.addAll(loadChannels(res, R.array.espn_channels));
        }
        return searchChannels;
    }

    /**
     * load from strings.xml
     */
    private List<String> loadSearchTerms() {
        //get all the search terms from resources        
        String[] searchWords = KeywordPrefHelper.getKeywords(this,AppConstants.KEYWORD_LIST_PREF, AppConstants.KEYWORD_SELECTED_PREF, R.array.search_words);
        String[] searchSelected = KeywordPrefHelper.getKeywordValues(this,AppConstants.KEYWORD_LIST_PREF, AppConstants.KEYWORD_SELECTED_PREF, R.array.search_words);
        //only add those that are selected from the preferences screen
        ArrayList<String> tmpList = new ArrayList<String>();
        for (int i = 0; i < searchWords.length; i++) {
            //break out of the loop if we don't have the same number of keywords as selected values
            if (i >= searchSelected.length) {
                break;
            }
            if (searchSelected[i].equals(AppConstants.KEYWORD_SELECTED)) {
                tmpList.add(searchWords[i]);
            }
        }
        return tmpList;
    }

    /**
     * load the terms to exclude
     * @param res
     */
    private void loadExcludeList(Resources res) {
        if (excludeTerms == null) {
            excludeTerms = new ArrayList<String>();
        }
        TypedArray searchWords = res.obtainTypedArray(R.array.search_exclude);
        for (int i = 0; i < searchWords.length(); i++) {
            excludeTerms.add(searchWords.getString(i));
        }
    }

    /**
     * load from strings.xml
     */
    private ArrayList<String> loadChannels(Resources res, int channelResource) {
        ArrayList<String> returnList = new ArrayList<String>();
        String requestUrl = getString(R.string.request_url);
        String requestExtension = getString(R.string.request_extension);

        TypedArray channelUrls = res.obtainTypedArray(channelResource);
        for (int i = 0; i < channelUrls.length(); i++) {
            //split the string up and put it back in the right order
            String[] parts = channelUrls.getString(i).split(SPLIT_CHAR);
            if (parts.length >= 2) {
                String channel = requestUrl + parts[0] + requestExtension + SPLIT_CHAR + parts[1];
                if (parts.length == 3) {
                    channel += SPLIT_CHAR + parts[2];
                }
                returnList.add(channel);
            }
        }

        return returnList;
    }

    /**
     *
     * @return
     */
    public List<String> getSearchTerms() {
        return loadSearchTerms();
    }

    /**
     * 
     * @return
     */
    public List<String> getExcludeTerms() {
        return excludeTerms;
    }

    /**
     * Increment the progress dialog if it exists
     * @param value
     */
    public void incrementProgress(int value) {
        if (this.dialog != null && this.dialog.isShowing()) {
            this.dialog.incrementProgressBy(value);
        }
    }

    /**
     * Method to update the list of shows from a list of TVListing objects
     * @param show
     */
    public void updateShows(List<TVListing> shows, boolean showMessage) {
        if (dialog != null) {
            dialog.dismiss();//dismiss the progress dialog
            dialog.setProgress(0);//set the progress value to 0
        }
        if (listAdapter == null) {
            return;
        }
        
        shows.addAll(listAdapter.getItems());

        //clear all the existing shows
        listAdapter.clear();

        //remove duplicates
        shows = removeDuplicates(shows);
        //sort in date order
        Collections.sort(shows, comparator);

        //add all the listings to the arrayadapter
        for (TVListing show : shows) {
            listAdapter.add(show);
        }

        //persist the shows to file so we have an offline copy
        saveShows();
        
        if(showMessage){
            Toast.makeText(this, "Update Completed", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * use the helper to save the list of shows
     */
    public void saveShows(){
        try {
            TVListingHelper.write(listings, this.SAVE_FILE, this);
        } catch (Exception e) {
            Log.w("DEBUG", e.toString());
        }
    }

    /**
     * remove duplicate listings from a list
     * @param shows
     * @return
     */
    private List<TVListing> removeDuplicates(List<TVListing>shows){
        List<TVListing> returnList = new ArrayList<TVListing>();
        for(TVListing listing:shows){
            boolean isDuplicate = false;
            for(TVListing returnListing:returnList){
                if(returnListing.equals(listing)){
                    isDuplicate = true;
                    break;
                }
            }
            if(!isDuplicate){
                returnList.add(listing);
            }
        }
        return returnList;
    }

    /**
     * schedule background refreshing of the shows
     */
    private void initTimer(){
        refreshTimer = new Timer();
        //schedule refresh every so many mins, starting now
        refreshTimer.scheduleAtFixedRate(new UpdaterTimerTask(this, mHandler),10000,refreshInterval);
    }

    /**
     * refresh the shows in the background
     */
    public void backgroundRefresh(){
        if(!isUpdating() && AndroidUtils.haveInternet(this)){
            showReloadIcon();
            setUpdating(true);
            String[] myStringArray = new String[]{};
            List<String> channels = getChannels();
            new BackgroundUpdater(this, KeywordPrefHelper.getCategory(this), listings).execute(channels.toArray(myStringArray));
        }
    }


    /**
     * Add this show to the users calendar
     * @param listing
     */
    private void addCalEvent(final TVListing listing) {
        //get all active calendars
        String[] projection = new String[]{"_id", "name"};
        String path = "calendars";

        Cursor managedCursor = getCalendarManagedCursor(projection, path);

        if (managedCursor != null) {
            List<String> calNames = new ArrayList<String>();
            List<String> calIDs = new ArrayList<String>();

            // For a full list of available columns see http://tinyurl.com/yfbg76w
            int i = 0;
            while (managedCursor.moveToNext()) {
                try {
                    String displayName = managedCursor.getString(1);
                    String calID = managedCursor.getString(0);
                    //check that we have all the data we need
                    if (displayName != null && !"".equals(displayName.trim()) && calID != null && !"".equals(calID)) {
                        calNames.add(displayName);
                        calIDs.add(calID);
                        i++;
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }

            managedCursor.close();//close the cursor because we've finished with it now

            if (calNames.isEmpty()) {
                Toast.makeText(this, "Failed to add event. No calendars available", Toast.LENGTH_LONG).show();
                return;
            }
            //create final arrays to refer to inside the dialog
            final String[] finalCalNames = calNames.toArray(new String[calNames.size()]);
            final String[] finalCalIDs = calIDs.toArray(new String[calIDs.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add to calendar");
            builder.setItems(finalCalNames, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int item) {
                    String calName = null;
                    String calId = null;

                    try {
                        calName = finalCalNames[item];
                        calId = finalCalIDs[item];
                    } catch (Exception e) {
                        Log.w("DEBUG", "error getting calendar id and name to add to");
                    }

                    if (calName == null || calId == null) {
                        Toast.makeText(getApplicationContext(), "Failed to add event", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }

                    ContentValues event = new ContentValues();
                    event.put("calendar_id", calId);
                    event.put("title", listing.getTitle());
                    event.put("description", listing.getDescription());                    
                    event.put("dtstart", listing.getStart().getTime());
                    event.put("dtend", listing.getEnd().getTime());
                    event.put("hasAlarm", 1); // 0 for false, 1 for true

                    try {
                        Uri url = getContentResolver().insert(eventsUri, event);
                    } catch (Exception e) {
                        Logger.getLogger(FixtureFeed.class.getName()).log(Level.WARNING, null, e);
                    }

                    //find the event in the listings
                    int i = listings.indexOf(listing);
                    if(i>-1){
                        try {
                            listing.setAddedToCal(true);
                            listings.set(i, listing);
                            TVListingHelper.write(listings, SAVE_FILE, getApplicationContext());
                        } catch (Exception ex) {
                            Toast.makeText(getBaseContext(), "error saving show as added to calendar", Toast.LENGTH_LONG).show();
                        }
                        saveShows();// save the shows
                    }

                    Toast.makeText(getBaseContext(), "Show added to your calendar", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();


            return;
        }

        Toast.makeText(this, "Failed to add event", Toast.LENGTH_LONG).show();
    }

    /**
     * from: http://code.google.com/p/android-calendar-provider-tests/source/browse/trunk/src/com/androidbook/androidcalendar/CalendarActivity.java
     * @param projection
     * @param selection
     * @param path
     * @return
     */
    private Cursor getCalendarManagedCursor(String[] projection, String path) {

        ContentResolver contentResolver = this.getContentResolver();

        Uri calendars = Uri.parse("content://calendar/" + path);

        Cursor managedCursor = null;
        try {
            managedCursor = contentResolver.query(calendars, projection, null,
                    null, null);
        } catch (IllegalArgumentException e) {
            Log.w("DEBUG", "Failed to get provider at ["
                    + calendars.toString() + "]");
        }

        if (managedCursor == null) {
            // try again
            calendars = Uri.parse("content://com.android.calendar/" + path);
            this.setEventsUri(Uri.parse("content://com.android.calendar/events"));//set the uri for setting the event to be the same as the one we are using to get it
            try {
                managedCursor = contentResolver.query(calendars, projection, null,
                        null, null);
            } catch (IllegalArgumentException e) {
                Log.w("DEBUG", "Failed to get provider at ["
                        + calendars.toString() + "]");
            }
        }
        return managedCursor;
    }

    /**
     * show the reload icon in the title bar
     */
    public void showReloadIcon() {
        setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, R.drawable.refresh_icon);//set the refresh icon in the title bar
    }

    /**
     * remove the reload icon from the title bar
     */
    public void hideReloadIcon() {
        setFeatureDrawable(Window.FEATURE_RIGHT_ICON, null);
    }

    public String getSPLIT_CHAR() {
        return this.SPLIT_CHAR;
    }

    public int getMAX_THREADS() {
        return this.MAX_THREADS;
    }

    public void setEventsUri(Uri eventsUri) {
        this.eventsUri = eventsUri;
    }

    public boolean isUpdating() {
        return updating;
    }

    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public ArrayList<TVListing> getListings() {
        return listings;
    }

    public void setListings(ArrayList<TVListing> listings) {
        this.listings = listings;
    }

    public Handler getmHandler() {
        return mHandler;
    }
    
}
