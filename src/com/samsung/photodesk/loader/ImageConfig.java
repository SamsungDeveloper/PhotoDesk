package com.samsung.photodesk.loader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;

import com.samsung.photodesk.R;


/**
 * <p>Set thumbnail image class</p>
 * Set thumbnail image size, quality and whether to use EXIF thumbnail.
 * Set image size from the values ​​defined by the resolution.
 *
 */
public final class ImageConfig {
	public static final Config THUMB_CONFIG = Bitmap.Config.RGB_565;
	
    
	private static final int THUMB_SIZE = 182;
	private static final int THUMB_CROP_SIZE_W = 182;
	private static final int THUMB_CROP_SIZE_H = 157;
	
	private static boolean SUPPORT_EXIF_THUMB = true;
	
	private static int mThumbSize = THUMB_SIZE;
	private static int mThumbCropSizeWidth = THUMB_CROP_SIZE_W;
	private static int mThumbCropSizeHight = THUMB_CROP_SIZE_H;
	private static boolean mSupportExifThumb = SUPPORT_EXIF_THUMB;
	
	
	private static final Options mOptions = new Options();
	static {
        mOptions.inPreferredConfig = THUMB_CONFIG;
	}
	
	public static Options getDefaultThumbOptions() {
        return mOptions;
	}
	
	public static void init(Context context) {
		Resources resources = context.getResources();
		mThumbSize = resources.getInteger(R.integer.thumb_size);
		mThumbCropSizeWidth = resources.getInteger(R.integer.thumb_crop_size_w);
		mThumbCropSizeHight = resources.getInteger(R.integer.thumb_crop_size_h);
		mSupportExifThumb = resources.getBoolean(R.bool.support_exif_thumb);
	}
	
	public static int getThumbSize() {
		return mThumbSize;
	}
	
	public static int getThumbCropSizeWidth() {
		return mThumbCropSizeWidth;
	}
	
	public static int getThumbCropSizeHight() {
		return mThumbCropSizeHight;
	}
	
	public static boolean isExitfThumb() {
		return mSupportExifThumb;
	}

}
