package com.samsung.photodesk.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.graphics.Bitmap;

import com.samsung.photodesk.FolderFragment;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;

/**
 * <p>Folder thumbnail loader class</p>
 * Working in the background to folder thumbnail load. ({@link com.samsung.photodesk.ContentFragment})
 * Folder thumbnail images are saved to thumbnail cache. ({@link ThumbnailCache})
 * 
 */
public class FolderBgThumbnailLoader extends BackgroundThumbnailLoader {
	private final List<FolderItem> mFolderItems = Collections.synchronizedList(new ArrayList<FolderItem>());
	private ContentResolver mContentResolver;
	
	public FolderBgThumbnailLoader(ContentResolver cr, ArrayList<FolderItem> fodlerItems) {
		mFolderItems.addAll(fodlerItems);
		mContentResolver = cr;
	}
	
	@Override
	public void run() {
		
			for (int folderIndex = 0; folderIndex < mFolderItems.size(); folderIndex++) {
				FolderItem foderItem = mFolderItems.get(folderIndex);
				if (mCancel == true)
					break;
				
				if (mStop) {
					waitNotify();
				}

				ArrayList<MediaItem> items = MediaLoader.getMediaItems(foderItem.getId(), mContentResolver);

				for (int index = 0; index < FolderFragment.MAX_THUMBNAIL_CNT; index++) {
					if (mCancel == true)
						break;
					
					if (mStop) {
						waitNotify();
					}

					if (index >= items.size()) {
						break;
					} else {
						loadThumbnail(items.get(index));
					}
				}
			}
	}
	

	@Override
	void loadThumbnail(MediaItem item) {
		if (mCancel == true || item == null) return;
		
		Bitmap bm = ThumbnailCache.INSTANCE.getFolderBitmap(item.getId());
		if (bm != null) return;
		
		Bitmap result = getMediaThumbnail(item);
		if (result == null) return;
		
		ThumbnailCache.INSTANCE.putFolderBitmap(item.getId(), result);
	}

	
}
