package com.samsung.photodesk.editor;

import com.samsung.photodesk.util.Setting;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.sdraw.PenSettingInfo;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;

/**
 * <p>Pen/Hand data of Image Editor control class</p>
 * Pen/Hand data save and SCanvas set to setting of pen/hand
 */
public class EditorToolUtil {
	public static final int TYPE_PEN = 0;
	public static final int TYPE_HAND = 1;
	
	private static EditorToolUtil mEditor;

	private PenHandSettingInfo mDrawingInfo[] = new PenHandSettingInfo[2];
	
	private ChangeModeListener mChangeMode;

	private boolean mIsDrawingToolDivision;
	
	public static synchronized EditorToolUtil getInstence() {
		if (mEditor == null)	mEditor = new EditorToolUtil();
		
		return mEditor;
	}
	
	private EditorToolUtil() {
		if (Setting.PEN_MODE) {
			mIsDrawingToolDivision  = true;
			initTool();
		} else {
			mIsDrawingToolDivision = false;
			mDrawingInfo[TYPE_HAND] = new PenHandSettingInfo(TYPE_HAND);
			setMode(TYPE_HAND, SCanvasConstants.SCANVAS_MODE_INPUT_PEN);			
		}		
	}
	
	/**
	 * <p>Pen/Hand data init</p>
	 */
	public void initTool() {
		mDrawingInfo[TYPE_PEN] = new PenHandSettingInfo(TYPE_PEN);
		mDrawingInfo[TYPE_HAND] = new PenHandSettingInfo(TYPE_HAND);
	}
	
	/**
	 * <p>Get if drawing tool can division</p>
	 * @return drawing tool can division
	 */
	public boolean isDrawingToolDivision() {
		return mIsDrawingToolDivision;
	}
	
	public void setChangeModeListener(ChangeModeListener changeModeListener) {
		mChangeMode = changeModeListener;
	}

	/**
	 * <p>Set drawing stroke info </p>
	 * @param eventType		event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @param attribute		stroke value (PhotoDeskStrokeInfo.STROKE_STYLE, PhotoDeskStrokeInfo.STROKE_WIDTH
	 * , PhotoDeskStrokeInfo.STROKE_COLOR)
	 * @param value			change value
	 */
	public void setStrokeInfo(int eventType, int attribute, int value) {		
		if (attribute == PhotoDeskStrokeInfo.STROKE_STYLE && value == PenSettingInfo.PEN_TYPE_ERASER) {
			return;
		}
		
		if (mDrawingInfo[getType(eventType)].getMode() == SCanvasConstants.SCANVAS_MODE_INPUT_ERASER) {
			return;
		}

		mDrawingInfo[getType(eventType)].setStrockInfo(attribute, value);
	}
	
	public void setStrokeInfo(int eventType, SObjectStroke stroke) {		
		mDrawingInfo[getType(eventType)].setStrockInfo(PhotoDeskStrokeInfo.STROKE_WIDTH, (int)stroke.getSize());
		mDrawingInfo[getType(eventType)].setStrockInfo(PhotoDeskStrokeInfo.STROKE_COLOR, stroke.getColor());
		mDrawingInfo[getType(eventType)].setStrockInfo(PhotoDeskStrokeInfo.STROKE_STYLE, stroke.getStyle());
	}
	
	/**
	 * <p>Change SCanvas stroke</p>
	 * @param sc	SCanvas {@link SCanvasView}
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 */
	public void changeStrokeSetting(SCanvasView sc, int type) {
		if (mDrawingInfo[getType(type)].getMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
			sc.setSettingStrokeInfo(mDrawingInfo[getType(type)].getStrokeInfo());
			
			if (getCurSettingViewEventType() == -1)		return;
			sc.setSettingViewStrokeInfo(mDrawingInfo[getType(type)].getStrokeInfo());
		}	
	}
	
	public int getType(int type) {
		if (mIsDrawingToolDivision)		return type;
		else 							return TYPE_HAND;
	}
	/**
	 * <p>Change SCanvas stroke setting view</p>
	 * @param sc	SCanvas {@link SCanvasView}
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 */
	public void changeStrokeSettingView(SCanvasView sc, int type) {
		if (!mIsDrawingToolDivision)		return;
		if (mDrawingInfo[getType(type)].getMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN) {
			if (isSameStroke(type, sc.getSettingViewStrokeInfo()))	return;
			sc.setSettingViewStrokeInfo(mDrawingInfo[getType(type)].getStrokeInfo());
		}
	}
	
	/**
	 * <p>Set selected clip art position</p>
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @param idx	selected clip art position
	 */
	public void setClipArtPosition(int type, int idx) {
		mDrawingInfo[getType(type)].setClipArtIndex(idx);
	}
	
	/**
	 * <p>Get Selected clip art position</p>
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @return		selected clip art position
	 */
	public int getClipArtPosition(int type) {
		return mDrawingInfo[getType(type)].getClipArtIndex();
	}

	public int getClipArtSelectState() {
		int handMode = mDrawingInfo[TYPE_HAND].getMode();
		int penMode = mDrawingInfo[TYPE_PEN].getMode();
		
		if (handMode == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE 
				&&penMode  == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
			return EditorClipArtGridView.BOTH;
			
		} else if (handMode ==  SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE
				&& penMode != SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
			return EditorClipArtGridView.HAND;
			
		} else if (penMode ==  SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE
				&& handMode != SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
			return EditorClipArtGridView.PEN;
			
		} else {
			return -1;
		}
	}
	
	/**
	 * <p>Set mode by type</p>
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @param mode	scanvas mode
	 */
	public void setMode(int type, int mode) {
		mDrawingInfo[getType(type)].setMode(mode);
		
		if (mChangeMode != null)	mChangeMode.onChangeMode(type);
	}
	
	/**
	 * <p>Get mode by type</p>
	 */
	public int getMode(int type) {
		return mDrawingInfo[getType(type)].getMode();		
	}
	
	/**
	 * <p>Setting view that shown state  change  by  hide</p>
	 */
	public void setCurSettingViewStateToHide() {
		if(mDrawingInfo[TYPE_HAND].isShowSettingView()) {
			mDrawingInfo[TYPE_HAND].setShowSettingView(false);
		} else {
			if (mDrawingInfo[TYPE_PEN] != null)		mDrawingInfo[TYPE_PEN].setShowSettingView(false);
		}
	}	

	/**
	 * <p>Whether setting view of type is showing</p>
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @return	Whether setting view is showing
	 */
	public boolean isShowSettingView(int type) {
		return mDrawingInfo[getType(type)].isShowSettingView();
	}
	
	/**
	 * <p>Change setting view showing state</p> 
	 * reverse type set reverse value of state
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @param state	setting view state (true, false)
	 */
	public void setSettingViewState(int type, boolean state) {
		mDrawingInfo[getType(type)].setShowSettingView(state);
		
		if (!mIsDrawingToolDivision) return;
		
		int reverseType = getReverseType(type);	
		mDrawingInfo[reverseType].setShowSettingView(!state);
	}

	/**
	 * Whether  is same stroke item.
	 * @param type			event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @param prevInfo		previous stroke info.	
	 * @return				Whether  is same stroke item.
	 */	
	public boolean isSameStroke(int eventType, SettingStrokeInfo prevInfo) {
		SettingStrokeInfo curInfo = mDrawingInfo[getType(eventType)].getStrokeInfo();

		if(curInfo.getStrokeColor() == prevInfo.getStrokeColor()
				&& curInfo.getStrokeStyle() == prevInfo.getStrokeStyle()
				&& curInfo.getStrokeWidth() == prevInfo.getStrokeWidth()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * <p>Get reverse event type</p>
	 * @param type			event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * @return				reverse event type
	 */
	public int getReverseType(int type) {
		if (type == EditorToolUtil.TYPE_PEN) {
			return EditorToolUtil.TYPE_HAND;
			
		} else {
			return EditorToolUtil.TYPE_PEN;
			
		}
	}
	
	/**
	 * <p>Get event type of setting view that shown</p>
	 * @param type	event type {@link TYPE_PEN}, {@link TYPE_HAND}
	 * 				if setting view do not show, return -1.
	 */
	public int getCurSettingViewEventType() {
		if (mIsDrawingToolDivision) {
			if (mDrawingInfo[TYPE_PEN].isShowSettingView())	return TYPE_PEN;
			else if (mDrawingInfo[TYPE_HAND].isShowSettingView()) return TYPE_HAND;
		} else {
			if (mDrawingInfo[TYPE_HAND].isShowSettingView()) return TYPE_HAND;
		}

		return -1;
	}

	public boolean isAbleSelectClipart(int eventType) {
		return mDrawingInfo[getType(eventType)].getMode() == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE;
	}
	
	/**
	 * <p>When Image Editor is empty mode, change mode with select mode when insert picture</p>    
	 * @param isSelect	whether insert picture.
	 */
	public void setGalleryImageSelectMode(boolean isSelect) {
		mDrawingInfo[TYPE_HAND].setGalleySelectMode(isSelect);
		if (mDrawingInfo[TYPE_PEN] != null)		mDrawingInfo[TYPE_PEN].setGalleySelectMode(isSelect);
	}
	
	/**
	 * <p>Get whether gallery image select mode</p>
	 * @return	gallery iamge select mode
	 */
	public boolean isGalleryImageSelectMode() {
		return mDrawingInfo[TYPE_HAND].isGalleySelectMode();
	}
	
	public void clearData() {
		mEditor = null;	
	}
	
	/**
	 * Interface for ChangeModeListener
	 *
	 */
	public interface ChangeModeListener {
		public abstract void onChangeMode(int type);
	}
	
}
