package com.samsung.photodesk.loader;

import android.graphics.Bitmap;

import com.samsung.photodesk.ActivityStatusImpl;
import com.samsung.photodesk.data.MediaItem;

/**
 * <p>Background thumbnial loader abstract class</p>
 * Working in the background to thumbnail load.
 * Used by ContentBgThumbnailLoader , FolderBgThumbnailLoader
 * 
 *
 */
public abstract class BackgroundThumbnailLoader extends Thread implements ActivityStatusImpl {
	static final String TAG = "BackgroudThumbnailLoader";
	
	WaitNotify mWaitNotify = new WaitNotify();
	boolean mCancel = false;
	boolean mStop = false;
	
	abstract void loadThumbnail(MediaItem item);
	
	
	Bitmap getMediaThumbnail(MediaItem item) {
		return MediaLoader.getThumbnail(item);
    	
    }
	
	public void cancel() {
		mCancel = true;
	}

	@Override
	public void onStop() {
		mStop = true;
	}

	@Override
	public void onResume() {
		mStop = false;
		mWaitNotify.resume();
	}
	
	public void waitNotify() {
		mWaitNotify.stop();
	}
	
	/**
	 * Stop/Resume for operation thread
	 *
	 */
	private class WaitNotify {
		synchronized public void stop() {
			try {
				wait();
			} catch (Exception e) {
			}
		}

		synchronized public void resume() {
			try {
				notifyAll();
			} catch (Exception e) {
			}
		}
	}
	
}
