package com.samsung.photodesk.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Bitmap;

import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.MediaItem;

/**
 * <p>Content thumbnail loader class</p>
 * Working in the background to content thumbnail load. ({@link com.samsung.photodesk.ContentFragment})
 * Content thumbnail images are saved to thumbnail cache. ({@link ThumbnailCache})
 * 
 */
public class ContentBgThumbnailLoader extends BackgroundThumbnailLoader {
	private final List<MediaItem> mItems = Collections.synchronizedList(new ArrayList<MediaItem>());
	
	
	public ContentBgThumbnailLoader(ArrayList<MediaItem> items) {
		mItems.addAll(items);
	}
	
	@Override
	public void run() {
		if (mItems == null) return;
		
		synchronized (mItems) {
			for (int index = 0; index < mItems.size(); index++) {
				if (mCancel == true) break;
				
				if (mStop) {
					waitNotify();
				}

				loadThumbnail(mItems.get(index));
			}
		}
	}

	@Override
	void loadThumbnail(MediaItem item) {
		if (mCancel == true) return;
		
		if (ThumbnailCache.INSTANCE.getBitmap(item.getId()) != null) return;
		
		Bitmap bm = getMediaThumbnail(item);
		if (bm == null) return;
		ThumbnailCache.INSTANCE.put(item.getId(), bm);
	}

}
