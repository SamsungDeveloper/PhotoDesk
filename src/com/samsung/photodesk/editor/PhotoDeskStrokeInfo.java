package com.samsung.photodesk.editor;

import android.graphics.Color;

import com.samsung.samm.common.SObjectStroke;

/**
 * <p>Stroke info class</p>
 * Stroke info used in Image Editor for hand/pen function.
 */
public class PhotoDeskStrokeInfo {
	public static final int STROKE_STYLE = 1;
	public static final int STROKE_WIDTH = 2;
	public static final int STROKE_COLOR = 3;
	
	private static final int STROKE_STYLE_COUNT = 5;
	
	private int mCurSelectedType;
	private StrokeInfo mStroke[] = new StrokeInfo[STROKE_STYLE_COUNT];

	
	public PhotoDeskStrokeInfo() {
		mStroke[StrokeInfo.SAMM_STROKE_STYLE_PENCIL] = new StrokeInfo(SObjectStroke.SAMM_STROKE_STYLE_PENCIL);
		mStroke[StrokeInfo.SAMM_STROKE_STYLE_BRUSH] = new StrokeInfo(SObjectStroke.SAMM_STROKE_STYLE_BRUSH);
		mStroke[StrokeInfo.SAMM_STROKE_STYLE_CHINESE_BRUSH] = new StrokeInfo(SObjectStroke.SAMM_STROKE_STYLE_CHINESE_BRUSH);
		mStroke[StrokeInfo.SAMM_STROKE_STYLE_CRAYON] = new StrokeInfo(SObjectStroke.SAMM_STROKE_STYLE_CRAYON);
		mStroke[StrokeInfo.SAMM_STROKE_STYLE_MARKER] = new StrokeInfo(SObjectStroke.SAMM_STROKE_STYLE_MARKER);
	}
	
	/**
	 * check stroke width whether valid range.
	 * @param width		stroke width
	 */
	public void setValidWidth(int width) {
		int result = width;
		if (width > SObjectStroke.SAMM_DEFAULT_MAX_STROKESIZE) {
			result = SObjectStroke.SAMM_DEFAULT_MAX_STROKESIZE;
		} else if (width < SObjectStroke.SAMM_DEFAULT_MIN_STROKESIZE) {
			result = SObjectStroke.SAMM_DEFAULT_MIN_STROKESIZE;
		} else {
			result = width;
		}
		
		mStroke[getStrokeIndex()].mWidth = result;
	}

	public int getWidthByStrokeType() {
		return mStroke[getStrokeIndex()].mWidth;
	}
	
	public void setWidthByStrokeType(int width) {
		mStroke[getStrokeIndex()].mWidth = width;
	}
	
	/**
	 * <p>Get stroke type</p>
	 * @return	stroke type
	 */
	public int getStrokeType() {
		return mCurSelectedType;
	}
	
	/**
	 * <p>Set stroke type</p>
	 * @param strokeType	stroke type
	 */
	public void setStrokeType(int strokeType) {
		mCurSelectedType = strokeType;
	}
	
	/**
	 * <p>Get stroke width</p>
	 * @return	stroke width
	 */
	public int getWidth() {
		return mStroke[getStrokeIndex()].mWidth;
	}
	
	/**
	 * <p>Set stroke width</p>
	 * @param width		stroke width
	 */
	public void setWidth(int width) {
		mStroke[getStrokeIndex()].mWidth = width;
	}
	
	/**
	 * <p>Get stroke color</p>
	 * @return		stroke color
	 */
	public int getColor() {
		return mStroke[getStrokeIndex()].mColor;
	}
	
	/**
	 * <p>Set stroke color</p>
	 * @param color		stroke color
	 */
	public void setColor(int color) {
		mStroke[getStrokeIndex()].mColor = color;
	}
	
	public int getStrokeIndex() {       
		switch(mCurSelectedType) {
		case SObjectStroke.SAMM_STROKE_STYLE_PENCIL:
			return StrokeInfo.SAMM_STROKE_STYLE_PENCIL;
		case SObjectStroke.SAMM_STROKE_STYLE_BRUSH:
			return StrokeInfo.SAMM_STROKE_STYLE_BRUSH;
		case SObjectStroke.SAMM_STROKE_STYLE_CHINESE_BRUSH:
			return StrokeInfo.SAMM_STROKE_STYLE_CHINESE_BRUSH;
		case SObjectStroke.SAMM_STROKE_STYLE_CRAYON:
			return StrokeInfo.SAMM_STROKE_STYLE_CRAYON;
		case SObjectStroke.SAMM_STROKE_STYLE_MARKER:
			return StrokeInfo.SAMM_STROKE_STYLE_MARKER;
		default :
			return StrokeInfo.SAMM_STROKE_STYLE_PENCIL;
		}
	}
	
	class StrokeInfo {
		public static final int SAMM_STROKE_STYLE_PENCIL = 0;
		public static final int SAMM_STROKE_STYLE_BRUSH = 1;
		public static final int SAMM_STROKE_STYLE_CHINESE_BRUSH = 2;
		public static final int SAMM_STROKE_STYLE_CRAYON = 3;
		public static final int SAMM_STROKE_STYLE_MARKER = 4;
		
		@SuppressWarnings("unused")
		private int mType;
		private int mWidth;
		private int mColor;
		@SuppressWarnings("unused")
		private int mAlpha;
		
		public StrokeInfo(int type) {
			mType = type;
			mWidth = 10;
			mColor = Color.BLACK;
			mAlpha = 255;
			if (type == SObjectStroke.SAMM_STROKE_STYLE_MARKER) {
				mAlpha = 30;
			}
		}
	}
}
