package com.samsung.photodesk.loader;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.samsung.photodesk.data.ImageItem;
import com.samsung.photodesk.data.MediaItem;

/**
 * <p>EXIF thumbnail loader class</p>
 * Thumbnail image load from EXIF data.
 *
 */
public class ExifThumbnailLoader extends AsyncTask<Long, Void, Bitmap> {
	MediaItem mItem;
	WeakReference<ImageView> mImageViewReference;
	int mWidth;
	int mHeight;
    
    public ExifThumbnailLoader(MediaItem item, ImageView imageView, int w, int h) {
    	mItem = item;
    	mImageViewReference = new WeakReference<ImageView>(imageView);
    	mWidth = w;
    	mHeight = h;
    }

    @Override
    protected Bitmap doInBackground(Long... params) {
    	if (isCancelled()) return null;
        return setThumbImage(MediaLoader.getExifThumbData(mItem));
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
    	if (isCancelled()) {
    		if (bitmap != null) {
    			bitmap.recycle();
    		}
            bitmap = null;
        }

    	if (bitmap != null) {
    		mImageViewReference.get().setImageBitmap(bitmap);
    	}
    }
    
    /**
     * Convert binary to a bitmap.
     * @param thumbData binary bitmap
     * @return bitmap
     */
    private Bitmap setThumbImage(byte[] thumbData) {
		if (thumbData == null || isCancelled()) return null;
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, bmOptions);
		
		int heightRatio = (int) Math.ceil(bmOptions.outHeight / (double) mHeight);
		int widthRatio = (int) Math.ceil(bmOptions.outWidth / (double) mWidth);
		
		if (heightRatio > 1 && widthRatio > 1) {
			if (heightRatio > widthRatio) {
				bmOptions.inSampleSize = heightRatio;
			} else {
				bmOptions.inSampleSize = widthRatio;
			}
		}
		
		bmOptions.inJustDecodeBounds = false;
		
		if (isCancelled()) return null;
		Bitmap bm = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, bmOptions);
		if (bm == null) return null;
		
		if (mItem.getType() == MediaItem.IMAGE) {
			ImageItem image = (ImageItem) mItem;
			if (image.isRotation()) {
				if (isCancelled()) return null;
				bm = MediaLoader.rotateBitmap(bm, image.getRotation(), true);
			}
		}
	
		return bm;
	}

    
}
