package com.samsung.photodesk.editor;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.samsung.photodesk.R;
import com.samsung.photodesk.editor.RecordView.RecordingListener;

/**
 * <p>Popup window used in ImageEditor</p>
 * EditorPopupWindow manage {@link EditorFilterView}, {@link EditorClipArtView}, {@link RecordView}
 */
public class EditorPopupWindow {
	public static final int POPUP_WINDOW_TYPE_CLIPART = 0;
	public static final int POPUP_WINDOW_TYPE_FILTER = 1;
	public static final int POPUP_WINDOW_TYPE_VOICE = 2;
	
	private Context mContext;
	
	private View mParent;
	
	private RecordView mRecordView;	
	EditorFilterView mFilterView;
	EditorClipArtView mClipArtView;
	
	private PopupWindow mPop;
	
	private int mCurType;
	
	private PopupWindowShowListener mShowListener;
	
	public EditorPopupWindow(View parent, Context context) {
		mParent = parent;
		mContext = context;
	}
	
	public void setPopupWindowShowListener(PopupWindowShowListener showListener) {
		mShowListener = showListener;
	}
	
	/**
	 * <p>Initialization RecordView</p>
	 * @param context			{@link Context}
	 * @param recordListener	record operation listener
	 */
	public void initRecordView(Context context, RecordingListener recordListener) {
		mRecordView = new RecordView(context);
		mRecordView.setRecordingListener(recordListener);
	}
	
	/**
	 * <p>Initialization FilterView</p>
	 * @param context			{@link Context}
	 * @param sc				{@link PhotoDeskScanvasView}
	 * @param rotation			Image ratation
	 */
	public void initFilterView(Context context, final PhotoDeskScanvasView sc, int rotation) {
		mFilterView = new EditorFilterView(context, sc, rotation);
		mFilterView.setCloseOnClickListener(mClosePopup);
	}
	
	/**
	 * <p>Initialization ClipArtView</p>
	 * @param context			{@link Context}
	 */
	public void initClipArtView(Context context) {
		mClipArtView = new EditorClipArtView(context);
		mClipArtView.setCloseOnClickListener(mClosePopup);
	}
	
	/**
	 * <p>Destroy resourse of views</p>
	 */
	public void destroyView() {
		if (mFilterView != null)	mFilterView.destroy();
		if (mClipArtView != null)	mClipArtView.destroy();
	}
	
	/**
	 * <p>Get selected clip art image bitmap</p>
	 * @param idx		seleted clip art index
	 * @return			seleted clip art bitmap
	 */
	public Bitmap getSelctedClipArtImage(int idx){
		return mClipArtView.getClipArtImage(idx);
	}
	
	/**
	 * <p>Show popup window by view type</p>
	 * @param popupType		view type (POPUP_WINDOW_TYPE_CLIPART, POPUP_WINDOW_TYPE_FILTER, 
	 * POPUP_WINDOW_TYPE_VOICE)
	 */
	public void showPopupWindow(int popupType) {
		if(mPop != null && mPop.isShowing()) {			
			mPop.dismiss();
			if (mCurType == popupType)	return;
		} 
		
		int top = ((View)mParent.getParent()).getTop();
		int marginTop = (int) mContext.getResources().getDimension(R.dimen.popup_margin_top);
		int marginLeft = (int) mContext.getResources().getDimension(R.dimen.popup_margin_left);
		
		switch (popupType) {
		case POPUP_WINDOW_TYPE_CLIPART:
			
			boolean isXLargeScreen = (mContext.getResources().getConfiguration().screenLayout 
					& Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE;
			
			int gravity = 0;
			if (isXLargeScreen) {
				gravity = Gravity.RIGHT | Gravity.TOP;
			} else {
				gravity = Gravity.LEFT | Gravity.TOP;
			}
			
			createShowPopup(mClipArtView.getView(), 
					(int) mContext.getResources().getDimension(R.dimen.clipart_view_width), 
					(int) mContext.getResources().getDimension(R.dimen.clipart_view_height), 
					false, gravity,
					marginLeft, top + marginTop);
			break;
			
		case POPUP_WINDOW_TYPE_FILTER:
			
			createShowPopup(mFilterView.getView(), 
					(int) mContext.getResources().getDimension(R.dimen.filter_view_width), 
					(int) mContext.getResources().getDimension(R.dimen.filter_view_height), 
					true, Gravity.LEFT | Gravity.TOP,
					(int)mParent.getX() + marginLeft, top + marginTop);
			break;
			
		case POPUP_WINDOW_TYPE_VOICE:
			createShowPopup(mRecordView, 
					(int) mContext.getResources().getDimension(R.dimen.record_view_width), 
					(int) mContext.getResources().getDimension(R.dimen.record_view_height), true, Gravity.CENTER, 0, 0);
			break;			
		}
		
		mCurType = popupType;
	}
	
	/**
	 * <p>Show popup window after create</p>
	 * @param v				view in popup window
	 * @param width			popup window width
	 * @param height		popup window hedith
	 * @param outSideTouch	whether dismiss popup window when touch outSide
	 * @param Gravity		popup window gravity
	 * @param marginX		popup window margin x
	 * @param marginY		popup window margin y
	 */
	private void createShowPopup(View v, int width, int height, boolean outSideTouch, 
			int Gravity, int marginX, int marginY) {
		if (v == null)	return;
		
		mPop = new PopupWindow(v, width, height);
		mPop.setOutsideTouchable(outSideTouch);
		mPop.showAtLocation(mParent, Gravity, marginX, marginY);
		
		if (mShowListener != null)	mShowListener.show();
	}
	
	/**
	 * <p>Dismiss All of the type</p>
	 */
	public void dismissAllPopup() {
		if (mPop != null && mPop.isShowing()) {
			mPop.dismiss();
		}
	}
	
	/**
	 * <p>Dismiss non drawing(POPUP_WINDOW_TYPE_FILTER, POPUP_WINDOW_TYPE_VOICE) popup</p>
	 */
	public void dismissNoDrawPopup() {
		if (mPop != null && mPop.isShowing() && mCurType != POPUP_WINDOW_TYPE_CLIPART) {
			mPop.dismiss();
		}
	}
	
	/**
	 * <p>Whether showing popup window</p>
	 * @param popupType		popup window type (POPUP_WINDOW_TYPE_CLIPART, POPUP_WINDOW_TYPE_FILTER, 
	 * POPUP_WINDOW_TYPE_VOICE)
	 * @return		Whether showing popup window (true : showing, false : hide)
	 */
	public boolean isShowing(int popupType) {
		if (popupType != mCurType) {
			return false; 
		} else {
			if (mPop != null && mPop.isShowing()) 	return true;
			else 									return false;
		}
	}
	
	/**
	 * <p>Get RecordView</p>
	 * @return	{@link RecordView}
	 */
	public RecordView getRecoredView() {
		return mRecordView;
	}
	
	/**
	 * <p>Get EditorFilterView</p>
	 * @return	{@link EditorFilterView}
	 */
	public EditorFilterView getFilterView() {
		return mFilterView;
	}
	
	/**
	 * <p>Get EditorClipArtView</p>
	 * @return	{@link EditorClipArtView}
	 */
	public EditorClipArtView getClipArtView() {
		return mClipArtView;
	}
	
	/**
	 * Interface for PopupWindowShowListener
	 *
	 */
	public interface PopupWindowShowListener {
		abstract void show();
	}

	private OnClickListener mClosePopup = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			dismissAllPopup();
			
		}
	};

}
