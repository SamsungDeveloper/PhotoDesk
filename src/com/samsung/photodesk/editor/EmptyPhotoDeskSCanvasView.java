package com.samsung.photodesk.editor;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;

import com.samsung.spensdk.applistener.SCanvasInitializeListener;

/**
 * <p>Custom SCanvasView When ImageEditor empty mode</p>
 * Init SCanvas size and background load.
 */
public class EmptyPhotoDeskSCanvasView extends PhotoDeskScanvasView {

	public EmptyPhotoDeskSCanvasView(Context context, AttributeSet as) {
		super(context, as);
	}
	
	public EmptyPhotoDeskSCanvasView(Context context) {
		super(context);
	}
	
	@Override
	public void initSCanvasSize(float parentWidth, float parentHeight) {
		
		int curOrientation = (parentWidth > parentHeight) ? 
				Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
		
		if (mFirstOrientation == curOrientation) {
			mSCanvasWidth = parentWidth;
			mSCanvasHeight = parentHeight;
		} else {
			mSCanvasWidth = parentHeight;
			mSCanvasHeight = parentWidth;
		}
		
		resizingSCanvsWidth(parentWidth, parentHeight);
 		
	}

	@Override
	public void initSCanvas(float rotation, String path, final boolean isAnimation) {
		setSCanvasInitializeListener(new SCanvasInitializeListener() {

			@Override
			public void onInitialized() {
				setZoomEnable(false);	
				setBackgroundColor(Color.WHITE);
				if (mOnFinishLoad != null)	mOnFinishLoad.onFinish();
			}
		});
	}
	
	
}
