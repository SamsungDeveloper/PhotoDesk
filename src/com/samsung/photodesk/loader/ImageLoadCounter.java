package com.samsung.photodesk.loader;

import java.util.ArrayList;

import com.samsung.photodesk.ActivityStatusImpl;

/**
 * <p>Image Load Counter</p>
 * Count is increase when foreground thread running.
 * Run background thread when if count is zero.
 */
public enum ImageLoadCounter {
	INSTANCE;
	
	private int mCounter;
	private ArrayList<ActivityStatusImpl> mLoaderItems = new ArrayList<ActivityStatusImpl>();
	
	/**
	 * register image loader
	 * @param loader background loader
	 */
	public void registerLoader(BackgroundThumbnailLoader loader) {
		if (loader == null) return;
		mLoaderItems.add(loader);
	}
	
	/**
	 * remove image loader
	 * @param loader background loader
	 */
	public void removeLoader(BackgroundThumbnailLoader loader) {
		if (loader == null) return;
		int index =  mLoaderItems.indexOf(loader);
		if (index >= 0) {
			mLoaderItems.remove(index);
		}
	}
	
	/**
	 * Increment the counter
	 */
	public synchronized void increaseCounter() {
		mCounter++;
		
		if (mCounter > 0) {
			for (ActivityStatusImpl loader: mLoaderItems) {
				if (loader != null) {
					loader.onStop();
				}
			}
		}
	}
	
	/**
	 * Counter decreases
	 */
	public synchronized void decreaseCounter() {
		mCounter--;
		
		if (mCounter <= 0) {
			for (ActivityStatusImpl loader: mLoaderItems) {
				if (loader != null) {
					loader.onResume();
				}
			}
		}
	}
	
	/**
	 * Get the count.
	 * @return count
	 */
	public synchronized int getCount() {
		return mCounter;
	}

}
