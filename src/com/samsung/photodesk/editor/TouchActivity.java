package com.samsung.photodesk.editor;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.samsung.photodesk.BaseActivity;

/**
 * <p>SCanvasContainer zoom and move class</p>
 */
public abstract class TouchActivity extends BaseActivity{
	
    private static final int MODE_MOVE = 1;
    private static final int MODE_ZOOM = 2;
    
    private static final int MOVE_TERM = 5;
    
    private static final float ZOOM_EXECUTE_GAP = 10.0f;
    
    private static final float MAX_SCALE = 5.0f;
    private static final float MIN_SCALE = 1.0f;
    
    PhotoDeskSCanvasManager mSCanvasUtil;
    
	private PointF mPreP[] = new PointF[2];
	private PointF mCurP[] = new PointF[2];
    
    private int mMode;
    
    private float mPreGap;    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		mPreP[0] = new PointF();
		mPreP[1] = new PointF();
		mCurP[0] = new PointF();
		mCurP[1] = new PointF();
		
		mSCanvasUtil = PhotoDeskSCanvasManager.getInstence(getApplicationContext());
	}

	/**
	 * <p>Set zoom or move mode</p>
	 * @param event		{@link MotionEvent}
	 * @param view		ScanvasContainer in ImageEditor
	 */
	public void setMode(MotionEvent event, RelativeLayout view) {
		float curGap = getGap(event);
		float gap = Math.abs(mPreGap - curGap);
		
		if (mMode == MODE_ZOOM && gap > ZOOM_EXECUTE_GAP) 	return;
		else 											mMode = MODE_MOVE;
		
		if (gap > ZOOM_EXECUTE_GAP * 5)					mMode = MODE_ZOOM;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			manager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * <p>operation when touch  to double finger</p>
	 * @param view		ScanvasContainer in ImageEditor	
	 * @param event		{@link MotionEvent}
	 * @return			Whether the event is handled
	 */
	public boolean doubleTouchEvent(RelativeLayout view, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			
		case MotionEvent.ACTION_POINTER_DOWN:
			mPreP[0].set(event.getX(0), event.getY(0));
			mPreP[1].set(event.getX(1), event.getY(1));
			
			mPreGap = getGap(event);
			
			break;
			
		case MotionEvent.ACTION_MOVE:
			mCurP[0].set(event.getX(0), event.getY(0));
			mCurP[1].set(event.getX(1), event.getY(1));
			
			setMode(event, view);
			
			if (mMode == MODE_ZOOM) {				
				zoom(view, event);
			} else if (mMode == MODE_MOVE) {
				move(view);			
			} 
			break;
		}

		System.gc();
		
		return false; 
	}
	

	public void move(RelativeLayout view) {
		if (Math.abs(mCurP[0].x - mPreP[0].x) > 3 || Math.abs(mCurP[0].y - mPreP[0].y) > 3) { 
			float x = (mCurP[0].x - mPreP[0].x);
			float y = (mCurP[0].y - mPreP[0].y);
			
			RectF rect = getMoveRectF(view, x, y);
			
			if (Math.abs(rect.left) >= MOVE_TERM)	{
				view.setTranslationX(view.getTranslationX() + rect.left);	
			}
		
			if (Math.abs(rect.top) >= MOVE_TERM)	{
				view.setTranslationY(view.getTranslationY() + rect.top);
			}

			mPreP[0].set(mCurP[0].x - (float)(x/2), mCurP[0].y  - (float)(y/2));
		}
	}
	
	/**
	 * <p>Get move rect after check availability move distance</p>
	 * @param view		ScanvasContainer in ImageEditor
	 * @param x			move x
	 * @param y			move y
	 * @return			availability rect
	 */
	public RectF getMoveRectF(RelativeLayout view, float x, float y) {
		float scale = view.getScaleX();
		
		float viewWidth = view.getWidth() * scale;
		float viewHeight = view.getHeight() * scale;
		
		float remainWidth = (viewWidth - mSCanvasUtil.getSCanvasParentWidth()) / 2;
		float remainHeight = (viewHeight - mSCanvasUtil.getSCanvasParentHeight()) / 2;
		
		float viewTransX = view.getTranslationX();
		float viewTransY = view.getTranslationY();
		
		x *= scale;
		y *= scale;
		

		if (remainWidth <= 0) {
			x = 0;
		} else {
			if (x > 0) {
				if (x > (remainWidth - viewTransX))		x = remainWidth - viewTransX;
			} else {
				if (x < -(remainWidth + viewTransX))	x = -(remainWidth + viewTransX);
			}
		}
		
		if (remainHeight <= 0) {
			y = 0;
		} else {
			if (y > 0) { 
				if (y > (remainHeight - viewTransY))	y = remainHeight - viewTransY;
			} else {
				if (y < -(remainHeight + viewTransY))	y = -(remainHeight + viewTransY);
			}
		}
		
		
		return new RectF(x, y, 0, 0);
	}


	/**
	 * <p>move when zoom in event</p>
	 * @param view		ScanvasContainer in ImageEditor	
	 * @param nextScale		next SCanvasContainer scale
	 */
	public void moveToZoomIn(RelativeLayout view, float nextScale) {
		float nextViewWidth = view.getWidth() * nextScale;
		float nextViewHeight = view.getHeight() * nextScale;
		
		float parentWidth = mSCanvasUtil.getSCanvasParentWidth();
		float parentHeight = mSCanvasUtil.getSCanvasParentHeight();
		
		float remainWidth = (nextViewWidth - parentWidth) / 2;
		float remainHeight = (nextViewHeight - parentHeight) / 2;

		if (remainHeight <= 0) {
			view.setTranslationY(0.0f);
			
		} else if (nextViewHeight < parentHeight) {
			view.setTranslationY(0);

		} else {
			if (view.getTranslationY() > remainHeight/2) {
				float result = view.getTranslationY() - ((view.getHeight() * view.getScaleX() - nextViewHeight)/2);
				if (result < 0)		result = 0.0f;
				view.setTranslationY(result);
					
			} else if (view.getTranslationY() < -remainHeight/2) {				
				float result = view.getTranslationY() + ((view.getHeight() * view.getScaleX() - nextViewHeight)/2);
				if (result > 0)		result = 0.0f;
				view.setTranslationY(result);
			}
		}


		if (remainWidth <= 0) {
			view.setTranslationX(0.0f);
			
		} else if (nextViewWidth < parentWidth) {
			view.setTranslationX(0);
			
		} else {
			if (view.getTranslationX() >= remainWidth/2) {
				float result = view.getTranslationX() - ((view.getWidth() * view.getScaleX() - nextViewWidth)/2);
				if (result < 0)		result = 0.0f;
				view.setTranslationX(result);
				
			} else if (view.getTranslationX() < -remainWidth/2) {
				float result = view.getTranslationX() + ((view.getWidth() * view.getScaleX() - nextViewWidth)/2);
				if (result > 0)		result = 0.0f;
				view.setTranslationX(result);

			} 
		}

	}
	
	public void zoom(RelativeLayout view, MotionEvent event) {
		float newGap = getGap(event);
		float scale = newGap / mPreGap;
		
		float nextScale = view.getScaleX() + (scale - MIN_SCALE);
		if (nextScale < 1.01f)			nextScale = MIN_SCALE;
		if (nextScale > MAX_SCALE)		nextScale = MAX_SCALE;
		
		float scaleScope = view.getScaleX() - nextScale;

		if (Math.abs(scaleScope) > 0.05f) {
			if (nextScale < view.getScaleX())	moveToZoomIn(view, nextScale);	
			view.setScaleX(nextScale);
			view.setScaleY(nextScale);	
		}
		
		mPreP[0].set(mCurP[0]);
		mPreP[1].set(mCurP[1]);
	}
	
	public float getGap(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

}
