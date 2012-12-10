package com.samsung.photodesk.editor;

import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;

/**
 * <p>This class have Pen or hand 's Data</p>
 * it manage Pen or hand 's stroke data, mode and so on. 
 */
public class PenHandSettingInfo {
	public static final int SCANVAS_MODE_INPUT_GALLERY_IMAGE = 14;
	
	private int mMode;
	
	private boolean mGalleySelectMode;
	
	private int mClipArtIndex;
	
	private boolean isShowSettingView;
	
	private PhotoDeskStrokeInfo mStrockInfo;


	public PenHandSettingInfo(int type) {		
		initSetting(type);
	}
	
	public void initSetting(int type) {
		if (type == EditorToolUtil.TYPE_PEN) {
			mMode = SCanvasConstants.SCANVAS_MODE_INPUT_PEN;
		} else {
			mMode = SCanvasConstants.SCANVAS_MODE_INPUT_ERASER;
		}
		
		mStrockInfo = new PhotoDeskStrokeInfo();
		isShowSettingView = false;
		mClipArtIndex = 0;
	}
	
	/**
	 * <p>Get mode</p>
	 * @return	mode
	 */
	public int getMode() {
		return mMode;
	}

	/**
	 * <p>Set mode</p>
	 * @param mode		mode
	 */
	public void setMode(int mode) {
		mMode = mode;
	}

	/**
	 * <p>Return stroke info</p>
	 * @return	stroke info
	 */
	public SettingStrokeInfo getStrokeInfo() {
		SettingStrokeInfo info = new SettingStrokeInfo();
		info.setStrokeColor(mStrockInfo.getColor());
		info.setStrokeStyle(mStrockInfo.getStrokeType());
		info.setStrokeWidth(mStrockInfo.getWidth());
		return info;
	}

	/**
	 * <p>Set stroke info</p>
	 * @param attribute		stroke attribute type (PhotoDeskStrokeInfo.STROKE_STYLE, 
	 * PhotoDeskStrokeInfo.STROKE_WIDTH, PhotoDeskStrokeInfo.STROKE_COLOR)
	 * @param value			stroke value
	 */
	public void setStrockInfo(int attribute, int value) {
		switch (attribute) {
		case PhotoDeskStrokeInfo.STROKE_STYLE:
			if (mStrockInfo != null){ 
				mStrockInfo.setStrokeType(value);
			}
			break;
			
		case PhotoDeskStrokeInfo.STROKE_WIDTH:
			if (mStrockInfo != null) {
				mStrockInfo.setWidth(value);
			}
			break;
			
		case PhotoDeskStrokeInfo.STROKE_COLOR:
			if (mStrockInfo != null)  {
				mStrockInfo.setColor(value);
			}
			break;
		
		default :
			break;
			
		}
	}

	public int getClipArtIndex() {
		return mClipArtIndex;
	}

	public void setClipArtIndex(int clipArtIndex) {
		mClipArtIndex = clipArtIndex;
	}

	public boolean isShowSettingView() {
		return isShowSettingView;
	}

	public void setShowSettingView(boolean isShow) {
		isShowSettingView = isShow;
	}
	
	public void toggleShowSettingView() {
		isShowSettingView = !isShowSettingView;
	}	
	
	public boolean isGalleySelectMode() {
		return mGalleySelectMode;
	}

	public void setGalleySelectMode(boolean galleySelectMode) {
		mGalleySelectMode = galleySelectMode;
	}

	
	
}
