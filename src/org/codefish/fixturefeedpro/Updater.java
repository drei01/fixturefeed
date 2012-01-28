package org.codefish.fixturefeedpro;

import org.codefish.fixturefeedpro.tv.TVListing;
import org.codefish.fixturefeedpro.util.StringUtils;
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

/**
 * Updater for loading shows from scratch
 * @author Matthew
 */
public class Updater extends AsyncTask<String, Integer, List<TVListing>> {
    private String searchRegex;
    private String excludesRegex;
    private String category;
    private FixtureFeed parent;

    public Updater(FixtureFeed context, String searchCategory) {
        super();
        this.parent = context;
        searchRegex = ".*(" + StringUtils.implode(parent.getSearchTerms(), "|") + ").*".toLowerCase();
        excludesRegex = ".*(" + StringUtils.implode(parent.getExcludeTerms(), "|") + ").*".toLowerCase();
        this.category = searchCategory;
    }

    protected List<TVListing> doInBackground(String... urls) {
        ExecutorService executor = Executors.newFixedThreadPool(parent.getMAX_THREADS());
        List<TVListing> returnList = new ArrayList<TVListing>();
        List<FutureTask<Object>> taskList = new ArrayList<FutureTask<Object>>();

        for (int i = 0; i < urls.length; i++) {
            final String url = urls[i];
            FutureTask<Object> future = new FutureTask<Object>(
                    new Callable() {
                        public Object call() throws Exception {
                            return ListingDownloader.getShows(url, category, searchRegex, excludesRegex, parent);
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
                returnList.addAll(result);
            }

            count++;
            publishProgress((int) ((count / (float) taskList.size()) * 100));//update our progress
        }

        try {
            executor.shutdown();
            executor.awaitTermination(AppConstants.CHANNEL_WAIT_TIME, TimeUnit.MILLISECONDS);//wait for the update to finish
        } catch (InterruptedException ignored) {
            executor.shutdownNow();
            //force the executor to shutdown if we can't
        }

        return returnList;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        this.parent.incrementProgress(progress[0].intValue());
    }

    @Override
    protected void onPostExecute(List<TVListing> result) {
        //update the list of shows in the parent (check again if we are not using the xml alternative at the moment)
        this.parent.updateShows((ArrayList<TVListing>) result,true);
        this.parent.setUpdating(false);//set flag to say we are not updating anymore
    }
}
