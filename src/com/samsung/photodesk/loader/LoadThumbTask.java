package com.samsung.photodesk.loader;

import android.graphics.Bitmap;

import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;

/**
 * <p>Thumbnail load class</p>
 *
 */
public class LoadThumbTask implements Job<Bitmap> {
	public static final int FOLDER = 0;
	public static final int CONTENTS = 1;
	
    MediaItem mItem;
    int mStore;
    
    public LoadThumbTask(int store, MediaItem item) {
        mItem = item;
        mStore = store;
    }
    
    public Bitmap run(JobContext jc) {
    	if (mItem == null) return null;
    	
    	return mItem.requestImage(mStore).run(jc);
    }
}