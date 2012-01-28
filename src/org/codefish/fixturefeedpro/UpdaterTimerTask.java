/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codefish.fixturefeedpro;

import android.os.Handler;
import java.util.TimerTask;

/**
 *timer task for background updating of channels
 * @author Matthew
 */
public class UpdaterTimerTask extends TimerTask {

    private FixtureFeed context;
    private Handler uiHandler;

    public UpdaterTimerTask(FixtureFeed context, Handler uiHandler) {
        super();
        this.context = context;
        this.uiHandler = uiHandler;
    }

    /**
     * perform the asynchronous update
     */
    @Override
    public void run() {        
            //use the ui handler to call the background updater from the main thread
            uiHandler.post(new Runnable() {
                    public void run() {
                       context.backgroundRefresh();
                    }
            });            
    }
}
