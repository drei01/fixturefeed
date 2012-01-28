/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codefish.fixturefeedpro;

import android.util.Log;
import org.codefish.fixturefeedpro.tv.TVListing;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.util.ByteArrayBuffer;

/**
 * Helper class to download tv listings from various sources
 * @author Matthew
 */
public class ListingDownloader {
    
    private static SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
    private static SimpleDateFormat timeFormater = new SimpleDateFormat("HH:mm");


    /**
     * Return a list of shows for a particular channel
     * @param updateURL
     * @param channelName
     * @return
     */
    public static List<TVListing> getShows(String url, String category, String searchRegex, String excludeRegex, FixtureFeed parent) {
        return getShows(url, category, searchRegex, excludeRegex, parent, null);
    }

    public static  List<TVListing> getShows(String url, String category, String searchRegex, String excludeRegex, FixtureFeed parent, List<TVListing> ignoreList){
                String[] parts = url.split(parent.getSPLIT_CHAR());
        if (parts.length >= 2) {
            URL updateURL = null;
            try {
                updateURL = new URL(parts[0]);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
            String channelName = parts[1];
            String channelLogo = "";
            if (parts.length == 3) {
                channelLogo = parts[2];
            }

            List<TVListing> returnList = new ArrayList<TVListing>();
            try {
                HttpURLConnection conn = (HttpURLConnection) updateURL.openConnection();
                //spoof the user agent so radio times don't get suspicious
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6");
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }
                /* Convert the Bytes read to a String. */
                String returnValue = new String(baf.toByteArray());
                String[] shows = returnValue.split("\\r?\\n");//split into shows (by newline)
                int count = shows.length;
                for (String show : shows) {
                    String[] fields = show.split("~");//get individual fields of the show
                    if (fields.length < 22) {
                        continue;//if there aren't enough fields then look onto the next one
                    }
                    String lowerShow = show.toLowerCase();
                    //if it is of the right category and includes any of the search terms AND doesn't include any of the banned terms then it is a show
                    if (lowerShow.matches(searchRegex) && !lowerShow.matches(excludeRegex)) {
                        //check the category if we need to
                        if(category!=null){
                            if(!fields[16].toLowerCase().contains(category)){
                                continue;
                            }
                        }
                        TVListing listing = new TVListing();

                        listing.setChannelName(channelName);
                        listing.setChannelLogo(channelLogo);
                        listing.setTitle(fields[0]);
                        listing.setDescription(fields[17]);

                        try {
                            listing.setStart(dateFormater.parse(fields[19]));
                        } catch (Exception ex) {
                            Log.e("fixturefeed", "error getting start date");
                            continue;
                        }
                        try {
                            listing.setStart_Time(timeFormater.parse(fields[20]));
                        } catch (ParseException ex) {
                            Log.e("fixturefeed", "error getting start time");
                            continue;
                        }
                        try {
                            listing.setEnd_Time(timeFormater.parse(fields[21]));
                        } catch (ParseException ex) {
                            Log.e("fixturefeed", "error getting end time");
                            continue;
                        }
                        try {
                            listing.setDuration(Integer.parseInt(fields[22]));
                        } catch (Exception ex) {
                            Log.e("fixturefeed", "error getting duration");
                            continue;
                        }

                        boolean addToList=listing.getEnd().after(new Date());//don't save if it's already finished
                        if(ignoreList != null){//check if this listing is in the ignore list
                            for(TVListing compareListing: ignoreList){
                                if(compareListing.equals(listing)){
                                    addToList=false;
                                }
                            }
                        }
                        if(addToList){
                            returnList.add(listing);
                        }
                    }
                }
                conn.disconnect();
            } catch (Exception ex) {
                 Log.e("fixturefeed", "general error downling listings:"+ex.getMessage());
            }

            return returnList;

        }
        return new ArrayList<TVListing>();
    }
}