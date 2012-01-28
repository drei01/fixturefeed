/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codefish.fixturefeedpro;

import org.codefish.fixturefeedpro.tv.TVListing;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codefish.fixturefeedpro.util.StringUtils;

/**
 * background updater, calls back to UI every time it finds fixtures for a channel
 * @author Matthew
 */
public class BackgroundUpdater extends AsyncTask<String, Integer, Boolean>{    
    private String searchRegex;
    private String excludesRegex;
    private String category;
    private List<TVListing> currentShows;//the shows that already exists
    private FixtureFeed parent;
    private ExecutorService executor;

    private static final int MAX_BG_THREADS = 1;

    public BackgroundUpdater(FixtureFeed context, String searchCategory, List<TVListing> currentShows) {
        super();
        this.parent = context;
        searchRegex = ".*(" + StringUtils.implode(parent.getSearchTerms(), "|") + ").*";
        excludesRegex = ".*(" + StringUtils.implode(parent.getExcludeTerms(), "|") + ").*";
        this.category = searchCategory;
        this.currentShows = currentShows;

        executor = Executors.newFixedThreadPool(MAX_BG_THREADS);//only 2 threads on the updater cpu savingness
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        List<FutureTask<Object>> taskList = new ArrayList<FutureTask<Object>>();

        for (int i = 0; i < urls.length; i++) {
            final String url = urls[i];
            FutureTask<Object> future = new FutureTask<Object>(
                    new Callable() {

                        public Object call() throws Exception {
                            return ListingDownloader.getShows(url, category, searchRegex, excludesRegex, parent, currentShows);//get the shows, ignoring ones we already have
                        }
                    });

            executor.submit(future);
            taskList.add(future);//add the task to the list

        }

        int count = 0;
        //get all the results and add them to the return list
        for (FutureTask<Object> future : taskList) {
            List<TVListing> result = null;
            try {
                result = (List<TVListing>) future.get(AppConstants.CHANNEL_WAIT_TIME, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (result != null) {
                final List<TVListing> returnList = result;
                //call back to the UI to update the shows
                parent.getmHandler().post(new Runnable() {
                    public void run() {
                        parent.updateShows(returnList,false);
                    }
                });
            }

            count++;
            
        }
        
        try {
            executor.shutdown();
            executor.awaitTermination(AppConstants.CHANNEL_WAIT_TIME, TimeUnit.MILLISECONDS); //wait for the update to finish
        } catch (InterruptedException ex) {
            executor.shutdown();
            Logger.getLogger(BackgroundUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Boolean.TRUE;
    }

    @Override
    protected void onCancelled() {
         try {
            executor.shutdown();
            executor.awaitTermination(AppConstants.CHANNEL_WAIT_TIME, TimeUnit.MILLISECONDS); //wait for the update to finish
        } catch (InterruptedException ex) {
            executor.shutdown();
            Logger.getLogger(BackgroundUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Boolean result) {        
        parent.hideReloadIcon();//hide the reloading icon from the title bar
        super.onPostExecute(result);
        this.parent.saveShows();
        this.parent.setUpdating(false);
    }
}
