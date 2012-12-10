
package com.samsung.photodesk;

import android.app.Application;
import android.content.Intent;

import com.samsung.photodesk.loader.ThreadPool;

/**
 * <p>Photo Desk application class</p>
 *
 */
public class PhotoDeskApplication extends Application {

    private ThreadPool mThreadPool;
    
    public void startSettingActivity() {
    	Intent intent = new Intent(this, SettingActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);
    }

    /**
     * get the thread pool
     *
     */
    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

	
}
