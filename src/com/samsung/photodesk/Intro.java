package com.samsung.photodesk;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.FutureListener;
import com.samsung.photodesk.loader.ImageConfig;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;
import com.samsung.photodesk.util.HiddenFolderUtil;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.photodesk.util.Setting;

/**
 * <p>Start intro display</p>
 * Set data for photo desk gallery during intro display.
 *
 */
public class Intro extends Activity {	
	private static final int ITEM_LOADING_TIME = 800;
	
	Future<ArrayList<FolderItem>> mFuture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        
        ImageConfig.init(this);
        Setting.INSTANCE.initialize(this);
        
        if (FolderFragment.getFolderItems() != null) {
        	strartPhotoDesk();
        	return;
        }
        if (savedInstanceState == null) {
        	loading();
    	}
	}
	
	/**
	 * <p>Load data for photo desk gallery.</p>
	 * Load data during intro
	 */
	private void loading() {
		if (FolderFragment.isItemEmpty()) {
    		ProtectUtil.getInstance().initialize(this);
    		HiddenFolderUtil.getInstance().initialize(this);
    		loadFolderItems();
    	}
    	
		new Handler().postDelayed(new Runnable() {
            
            public void run() {
            	if (mFuture != null) {
            		mFuture.waitDone();
            	}
            	
                strartPhotoDesk();
            }
        }, ITEM_LOADING_TIME);
	}

	/**
	 * Set data for FolderFragment ({@link FolderFragment})
	 */
	private void loadFolderItems() {
		mFuture = getThreadPool().submit(new LoadFolderItems(), new FutureListener<ArrayList<FolderItem>>() {

			@Override
			public void onFutureDone(Future<ArrayList<FolderItem>> future) {
				FolderFragment.insertFolderItems(future.get());
				FolderFragment.runImageLoaderThread(getContentResolver());
			}
		});
	}

	/**
	 * <p>Start activity for photo desk</p>
	 */
	private void strartPhotoDesk(){
        Intent intent = new Intent(getApplicationContext(), PhotoDeskActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.intro_fade_in, R.anim.intro_fade_out);
        finish();
	}
	
	/**
	 * <p>Get ThreadPool</p>
	 * @return ThreadPool ({@link ThreadPool})
	 */
	public ThreadPool getThreadPool() {
		ThumbnailCache.INSTANCE.clearAll();
		return  ((PhotoDeskApplication)getApplication()).getThreadPool();
	}
	
	/**
	 * <p>Load Folder items</p>
	 * 
	 */
	public class LoadFolderItems implements Job<ArrayList<FolderItem>> {	
	    public ArrayList<FolderItem> run(JobContext jc) {
	    	return MediaLoader.getFolderItems(getContentResolver());
	    }
	}
}
