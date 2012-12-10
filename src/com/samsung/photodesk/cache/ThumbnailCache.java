package com.samsung.photodesk.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.samsung.photodesk.loader.MediaLoader;

/**
 * <p>Thumbnail cache</p>
 * Managing thumbnail cache for folder and content items.
 * Remove cache when size exceeds the limited. (LIMIED_FOLDER_CACHE_SIZE , LIMIED_CONTENT_CACHE_SIZE) 
 *
 */
public enum ThumbnailCache  {
	INSTANCE;
	
	private static final String TAG = "ThumbnailBitmapCache";
	
	public static final int STORAGE_CONTENT = 0;
	public static final int STORAGE_FODLER = 1;
	
	public static final int LIMIED_CONTENT_CACHE_SIZE = 20 * 1024  * 1024; //20MB
	public static final int LIMIED_FOLDER_CACHE_SIZE = 10 * 1024  * 1024; //10MB
	
	private LruCache<Long, Bitmap> mFolderCache = new LruCache<Long, Bitmap>(LIMIED_FOLDER_CACHE_SIZE) {
		@Override
		protected int sizeOf(Long key, Bitmap bitmap) {
			return bitmap.getByteCount();
		}
	};
	
	private LruCache<Long, Bitmap> mContentCache= new LruCache<Long, Bitmap>(LIMIED_CONTENT_CACHE_SIZE) {
		@Override
		protected int sizeOf(Long key, Bitmap bitmap) {
			return bitmap.getByteCount();
		}
	};
	
	public void put(long key, Bitmap bm) {
		if (bm == null) {
			Log.w(TAG, "== NULL POINT BITMAP ==");
			return;
		}
		
		if (getFolderBitmap(key) != null) {
			return;
		}
		
		if (getBitmap(key) == null) {
			mContentCache.put(key, bm);
		}
	}

	public Bitmap getBitmap(long key) {
		Bitmap bm = getFolderBitmap(key);
		if (bm != null) return bm;
		return mContentCache.get(key);
	}
	
	public void clearAll() {
		clearContent();
		clearFolder();
	}
	
	public void clearContent() {
		mContentCache.evictAll();
	}
	
	public void clearFolder() {
		mFolderCache.evictAll();
	}
	
	public void putFolderBitmap(long key, Bitmap bm) {
		mFolderCache.put(key, bm);
	}
	
	public Bitmap getFolderBitmap(long key) {
		return mFolderCache.get(key);
	}

	public void rotateBitmap(int rotation, long key) {
		Bitmap bm = getBitmap(key);
    	if (bm != null) {
    		Bitmap newBitmap = MediaLoader.rotateBitmap(bm, rotation, false);
    		if (mFolderCache.remove(key) != null) {
    			mFolderCache.put(key, newBitmap);
    		} else {
    			if (mContentCache.remove(key) != null) {
    				mContentCache.put(key, newBitmap);
    			}
    		}
    	}
	}
}

