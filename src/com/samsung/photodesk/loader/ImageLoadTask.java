package com.samsung.photodesk.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.samsung.photodesk.data.ImageItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;

/**
 * <p>Image load class</p>
 *
 */
public class ImageLoadTask implements Job<Bitmap> {
	MediaItem mItem;
	BitmapFactory.Options options = new BitmapFactory.Options();
	int mWidth;
	int mHeigth;
	String mPath;

    public ImageLoadTask(MediaItem item, int dw, int dh) {
        mItem = item;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mWidth = dw;
        mHeigth = dh;
    }
    
    public ImageLoadTask(String path, int dw, int dh) {
        mPath = path;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mWidth = dw;
        mHeigth = dh;
    }
      
	public Bitmap run(JobContext jc) {
		
		if (jc.isCancelled()) return null;
		String path = mPath;
		if (mItem != null) path = mItem.getPath(); 
		
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bm = BitmapFactory.decodeFile(path, bmOptions);
        bmOptions.inJustDecodeBounds = true;
		
		if (jc.isCancelled()) return null;
		int heightRatio = (int) Math.ceil(bmOptions.outHeight / (double) mHeigth);
		int widthRatio = (int) Math.ceil(bmOptions.outWidth / (double) mWidth);
		
		if ((heightRatio > 1 && widthRatio > 1) || heightRatio > 5 || widthRatio > 5) {
			if (heightRatio > widthRatio) {
				bmOptions.inSampleSize = heightRatio;
			} else {
				bmOptions.inSampleSize = widthRatio;
			}
		}
		
		bmOptions.inJustDecodeBounds = false;
		if (jc.isCancelled()) return null;
	    bm = BitmapFactory.decodeFile(path, bmOptions);
		if (bm == null) return null;
		
		if (mItem != null && mItem.getType() == MediaItem.IMAGE) {
			ImageItem image = (ImageItem) mItem;
			if (image.isRotation()) {
				if (jc.isCancelled()) return null;
				bm = MediaLoader.rotateBitmap(bm, image.getRotation(), true);
			}
		}
		
		return bm;
    }

	
}
