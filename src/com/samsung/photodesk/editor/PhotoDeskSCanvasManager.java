package com.samsung.photodesk.editor;

import java.io.IOException;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.ExifInterface;

import com.samsung.photodesk.util.PhotoDeskUtils;


/**
 * <p>PhotoDeskSCanvas manage class</p>
 * Manage mode baseScale and so on, when loading PhotoDeskSCanvas and executing PhotoDeskSCanvas.
 */
public class PhotoDeskSCanvasManager {
	
	private static PhotoDeskSCanvasManager mSCanvasUtil;
	
	private float mMultipleNum = 1;
	
	private boolean mIsEmptyMode;

	private Point mSCanvasParentSize;	
	
	private PhotoDeskSCanvasManager(Context context) {
		mSCanvasParentSize = new Point(0, 0);
	}
	
	public static synchronized PhotoDeskSCanvasManager getInstence(Context context) {
		if (mSCanvasUtil == null)	mSCanvasUtil = new PhotoDeskSCanvasManager(context);
		return mSCanvasUtil;
	}	
	
	/**
	 * <p>Get Bitmap size</p>
	 * @param path		bitmap size
	 * @return			size {@link BitmapFactory.Options}
	 */
	public BitmapFactory.Options getBitmapSize(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		options.inJustDecodeBounds = false;

		return options;
	}
	
	/**
	 * <p>Get Bitmap size</p>
	 * @param res	{@link Resources}
	 * @param id	resources id
	 * @return		size {@link BitmapFactory.Options}
	 */
	public BitmapFactory.Options getBitmapSize(Resources res, int id) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, id, options);
		
		options.inJustDecodeBounds = false;

		return options;
	}
	
	/**
	 * <p>Get resizing bitmap option</p>
	 * @param path		bitmap path
	 * @param maxW		bitmap max width
	 * @param maxH		bitmap max height
	 * @return			size {@link BitmapFactory.Options}
	 */
	public BitmapFactory.Options getResizeImageOption(String path, int maxW, int maxH) {
		BitmapFactory.Options options = getBitmapSize(path);

		int scale = (options.outWidth > options.outHeight) ? (int) (options.outWidth / maxW)
				: (int) (options.outHeight / maxH);

		if (scale < 2) {
			scale = 1;
		} else if (scale % 2 == 1) {
			scale -= 1;
		}

		options.inSampleSize = scale;
		options.inPreferredConfig = Config.RGB_565;

		return options;
	}
	
	/**
	 * <p>Get bitmap rotation</p>
	 * @param path		bitmap path
	 * @return			rotation
	 */
	public int getRotation(String path) {
		if (path.equals(""))	return 0;
		
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int exifInfo = PhotoDeskUtils.getCurrentExifOrientation(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
		int rotation = (exifInfo < 0) ? exifInfo+360 : exifInfo;
		
		return rotation;
	}
	
	/**
	 * <p>Get clip art rect</p>
	 * @param bitmap	clipart bitmap	
	 * @param x			touch x
	 * @param y			touch y
	 * @return			rect
	 */
	public RectF getClipArtRect(Bitmap bitmap, int x, int y) {
		RectF result = null;
		
		int imageW = bitmap.getWidth();
		int imageH = bitmap.getHeight();
				
		int radius = 50;
		
		int centerX = (int) (x * mMultipleNum);
		int centerY = (int) (y * mMultipleNum);
		
		if (imageW > imageH) {
			result = new RectF(centerX - radius, centerY - (radius * imageH / imageW), 
								centerX + radius, centerY + (radius * imageH / imageW));
		} else {
			result = new RectF(centerX - (radius * imageW / imageH), centerY - radius,
								centerX + (radius * imageW / imageH),centerY + radius);
		}
		
		return result;
	}
	
	/**
	 * <p>Get edit_area size</p>
	 * @param firstOrientaion	first enter orientation
	 * @return					size {@link Point}
	 */
	public Point getParentSizeOfEmptyPage(int firstOrientaion) {	
		int curOrientation = (mSCanvasUtil.getSCanvasParentWidth() > mSCanvasUtil.getSCanvasParentHeight()) ?
				Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
		
		if (firstOrientaion == curOrientation) {
			return (new Point(mSCanvasUtil.getSCanvasParentWidth(), mSCanvasUtil.getSCanvasParentHeight()));
		} else {
			return (new Point(mSCanvasUtil.getSCanvasParentHeight(), mSCanvasUtil.getSCanvasParentWidth()));
		}

	}
	
	/**
	 * <p>Get insert max image size</p>
	 * @return		size
	 */
	public int getInsertedImageMaxSize() {
		return ((mSCanvasParentSize.x > mSCanvasParentSize.y) ?
				mSCanvasParentSize.x : mSCanvasParentSize.y) / 4;
	}
	
	/**
	 * <p>Get inserted image rect (when empty mode)</p>
	 * @param width			image width
	 * @param height		image height
	 * @param firstOrientaion		first enter orientation
	 * @return		{@link RectF}
	 */
	public RectF getInsertedImageRect(int width, int height, int firstOrientaion) {
		Point parentSize = mSCanvasUtil.getParentSizeOfEmptyPage(firstOrientaion);
		int centerX = parentSize.x/2;
		int centerY = parentSize.y/2;
		
		return new RectF(centerX - (width / 2), centerY - (height / 2), 
				centerX + (width / 2), centerY + (height / 2));
	}
	
	
	public float getMultipleNum() {
		return mMultipleNum;
	}

	public void setMultipleNum(float multipleNum) {
		mMultipleNum = multipleNum;
	}
	
	public boolean isEmptyMode() {
		return mIsEmptyMode;
	}

	public void setEmptyMode(boolean isEmptyMode) {
		this.mIsEmptyMode = isEmptyMode;
	}
	
	public void setSCanvasParentSize(int width, int height) {
		mSCanvasParentSize.x = width;
		mSCanvasParentSize.y = height;
	}
	
	public int getSCanvasParentWidth() {
		return mSCanvasParentSize.x;
	}
	
	public int getSCanvasParentHeight() {
		return mSCanvasParentSize.y;
	}
	
	public boolean isEmptySCanvasParentRect() {
		if (mSCanvasParentSize.x == 0 && mSCanvasParentSize.y == 0)		return true;
		return false;
	}
	
	public void destoryData() {
		if (mSCanvasUtil != null)	mSCanvasUtil = null;
	}
}
