package com.samsung.photodesk.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.samsung.photodesk.R;

/**
 * <p>Touch image ImageView</p>
 * To apply for the pinch function on detail image viewer.
 * 
 */
public class ViewTouchImage extends ImageView {	
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private static final int WIDTH = 0;
    private static final int HEIGHT = 1;
    private static final int VIEW_SCALE = 2;
    private static final float SMALL_IMAGE_MIM_SCALE = 1.0f/(float)VIEW_SCALE;
    private static final int MAX_SCALE = 10;

	private Matrix mMatrix = new Matrix();
	private Matrix mPreMatrix = new Matrix();
	private Matrix mCurMatrix = new Matrix();

	
	private int mMode = NONE;

    private int mImageWidth;
    private int mImageHeight;
    private int mViewWidth;
    private int mViewHeight;

	private PointF mStartPoint = new PointF();
	private PointF mCenterPoint = new PointF();
	private float mPreGap = 1.0f;
	
	private GestureDetector mGestureDetector;
    OnTouchListener mGestureListener;
    
    private boolean mVideoFlag = false;
    private float mMoveGap;

	public ViewTouchImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
		mMoveGap = context.getResources().getDimension(R.dimen.detail_view_move_gap);
	}

	public ViewTouchImage(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ViewTouchImage(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    init();
	}
	
	/**
	 * Initialize
	 */
	private void init() {
	    if (setViewSize()) {
    	    updateMatrix(mMatrix);
    		setImageMatrix(mMatrix);
    		initImageSize();
    		initGesture();
	    }
	}
	
	/**
	 * Initialize gesture
	 */
	private void initGesture(){
	    mGestureDetector = new GestureDetector(getContext(), new MyGestureDetector());
	}
	
	/**
	 * Setting view size and image size
	 */
	private boolean setViewSize(){
        Drawable drawable = getDrawable();
        if(drawable == null) return false;
        mImageWidth = drawable.getIntrinsicWidth();
        mImageHeight = drawable.getIntrinsicHeight();
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
        return true;
	}

	/**
	 * The image size to fit to zoom in or out.
	 */
	public void initImageSize() {
		float[] values = new float[9];
		this.mMatrix.getValues(values);

		int scaleImageW = (int) (mImageWidth * values[Matrix.MSCALE_X]);
		int scaleImageH = (int) (mImageHeight * values[Matrix.MSCALE_Y]);

		values[Matrix.MTRANS_X] = 0;
		values[Matrix.MTRANS_Y] = 0;

		if (mImageWidth > mViewWidth || mImageHeight > mViewHeight) {
			int target = WIDTH;
			if (mImageWidth < mImageHeight)
				target = HEIGHT;

			if (target == WIDTH)
			    values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = (float) mViewWidth / mImageWidth;
			if (target == HEIGHT)
			    values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = (float) mViewHeight / mImageHeight;

			scaleImageW = (int) (mImageWidth * values[Matrix.MSCALE_X]);
			scaleImageH = (int) (mImageHeight * values[Matrix.MSCALE_Y]);

			if (scaleImageW > mViewWidth)
			    values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = (float) mViewWidth / mImageWidth;
			if (scaleImageH > mViewHeight)
			    values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = (float) mViewHeight / mImageHeight;
		}else{
		    fixSizeFullScreen(values, 1);
		}
		transImageCenter(values);
		mMatrix.setValues(values);
		setImageMatrix(mMatrix);
	}
	
	private void fixSizeFullScreen(float values[], int scale){
        float scaleViewW = mViewWidth / scale;
        float scaleViewH = mViewHeight / scale;
        float scaleX = (float) scaleViewW / mImageWidth;
        float scaleY = (float) scaleViewH / mImageHeight;	    
        int target = WIDTH;        
        if (mImageWidth < mImageHeight) target = HEIGHT;
        if (target == WIDTH) values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scaleX;
        if (target == HEIGHT) values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scaleY;
        int scaleImageW = (int) (mImageWidth * values[Matrix.MSCALE_X]);
        int scaleImageH = (int) (mImageHeight * values[Matrix.MSCALE_Y]);
        if (scaleImageW > scaleViewW) values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scaleX;
        if (scaleImageH > scaleViewH) values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = scaleY;
        values[Matrix.MSCALE_X] = min(values[Matrix.MSCALE_X], MAX_SCALE);
        values[Matrix.MSCALE_Y] = min(values[Matrix.MSCALE_Y], MAX_SCALE);
    }
	
	/**
	 * Image moves to the center of the view.
	 * @param values image matrix
	 */
	private void transImageCenter(float values[]){        
        int scaleImageW = (int) (mImageWidth * values[Matrix.MSCALE_X]);
        int scaleImageH = (int) (mImageHeight * values[Matrix.MSCALE_Y]);
        if (scaleImageW < mViewWidth) {
            values[Matrix.MTRANS_X] = (float) mViewWidth / 2 - (float) scaleImageW / 2;
        }
        if (scaleImageH < mViewHeight) {
            values[Matrix.MTRANS_Y] = (float) mViewHeight / 2 - (float) scaleImageH / 2;
        }
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if(mVideoFlag) return super.onTouchEvent(event);
        if(mGestureDetector != null && mGestureDetector.onTouchEvent(event)) return false;
        disallowInterceptTouch();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		    mPreMatrix.set(mMatrix);
			mStartPoint.set(event.getX(), event.getY());
			mMode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
		    mPreGap = pointGapSpace(event);
			if (mPreGap > 5f) {
			    mPreMatrix.set(mMatrix);
			    centerPoint(mCenterPoint, event);
				mMode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		    if(mMode == ZOOM || Math.abs(event.getX() - mStartPoint.x) > mMoveGap || Math.abs(event.getY() - mStartPoint.y) > mMoveGap){
	            mMode = NONE;
	            return false;
		    }
            mMode = NONE;
		    break;
		case MotionEvent.ACTION_POINTER_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			if (mMode == DRAG) {
				mMatrix.set(mPreMatrix);
				mMatrix.postTranslate(event.getX() - mStartPoint.x, event.getY() - mStartPoint.y);

			} else if (mMode == ZOOM) {
				float postGap = pointGapSpace(event);
				if (postGap > 5f) {
					mMatrix.set(mPreMatrix);
					float scale = postGap / mPreGap;
					mMatrix.postScale(scale, scale, mCenterPoint.x, mCenterPoint.y);
				}
			}
			break;
		}
		updateMatrix(mMatrix);
		setImageMatrix(mMatrix);
		return super.onTouchEvent(event);
	}
	
	private void disallowInterceptTouch(){
        float[] values = new float[9];
        mCurMatrix.getValues(values);
        int scaleImageW = (int) (mImageWidth * values[Matrix.MSCALE_X]);
        int scaleImageH = (int) (mImageHeight * values[Matrix.MSCALE_Y]);
        if(scaleImageW > mViewWidth || scaleImageH > mViewHeight || mMode == ZOOM){
            getParent().requestDisallowInterceptTouchEvent(true);
        }else{
            getParent().requestDisallowInterceptTouchEvent(false);
        }
	}

	private float pointGapSpace(MotionEvent event) {
		float x = event.getX() - event.getX(event.getPointerCount()-1);
		float y = event.getY() - event.getY(event.getPointerCount()-1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void centerPoint(PointF point, MotionEvent event) {
		float x = event.getX() + event.getX(event.getPointerCount()-1);
		float y = event.getY() + event.getY(event.getPointerCount()-1);
		point.set(x / 2, y / 2);
	}

	private void updateMatrix(Matrix matrix) {
        float scaleViewW = mViewWidth / VIEW_SCALE;
        float scaleViewH = mViewHeight / VIEW_SCALE;
		float[] values = new float[9];
        float[] savedValue = new float[9];

		matrix.getValues(values);
		mCurMatrix.getValues(savedValue);
		int scaleImageW = (int) (mImageWidth * values[Matrix.MSCALE_X]);
		int scaleImageH = (int) (mImageHeight * values[Matrix.MSCALE_Y]);
		
		if (values[Matrix.MTRANS_X] < mViewWidth - scaleImageW)
			values[Matrix.MTRANS_X] = mViewWidth - scaleImageW;
		if (values[Matrix.MTRANS_Y] < mViewHeight - scaleImageH)
			values[Matrix.MTRANS_Y] = mViewHeight - scaleImageH;
		if (values[Matrix.MTRANS_X] > 0)
			values[Matrix.MTRANS_X] = 0;
		if (values[Matrix.MTRANS_Y] > 0)
			values[Matrix.MTRANS_Y] = 0;

		if (values[Matrix.MSCALE_X] > MAX_SCALE || values[Matrix.MSCALE_Y] > MAX_SCALE) {
			values[Matrix.MSCALE_X] = (Float.isNaN(savedValue[Matrix.MSCALE_X])) ? MAX_SCALE : savedValue[Matrix.MSCALE_X];
			values[Matrix.MSCALE_Y] = (Float.isNaN(savedValue[Matrix.MSCALE_Y])) ? MAX_SCALE : savedValue[Matrix.MSCALE_Y];
			values[Matrix.MTRANS_X] = savedValue[Matrix.MTRANS_X];
			values[Matrix.MTRANS_Y] = savedValue[Matrix.MTRANS_Y];
		}else{
    		if (mImageWidth > mViewWidth || mImageHeight > mViewHeight) {
    			if (scaleImageW < scaleViewW && scaleImageH < scaleViewH) {
    				fixSizeFullScreen(values, VIEW_SCALE);
    			}
    		} else {
    			if (values[Matrix.MSCALE_X] < SMALL_IMAGE_MIM_SCALE)
    				values[Matrix.MSCALE_X] = SMALL_IMAGE_MIM_SCALE;
    			if (values[Matrix.MSCALE_Y] < SMALL_IMAGE_MIM_SCALE)
    				values[Matrix.MSCALE_Y] = SMALL_IMAGE_MIM_SCALE;
    		}
		}
        transImageCenter(values);
		matrix.setValues(values);
		mCurMatrix.set(matrix);
	}
	
	public void setVideoFlag(boolean videoFlag) {
        this.mVideoFlag = videoFlag;
    }

    private float min(float num1, float num2) {
        return (num1 < num2) ? num1 : num2;
    }
	
    class MyGestureDetector extends SimpleOnGestureListener {
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float values[] = new float[9];
            mCurMatrix.getValues(values);
            if (mImageWidth * values[Matrix.MSCALE_X] == mViewWidth
                    || mImageHeight * values[Matrix.MSCALE_Y] == mViewHeight) {
                values[Matrix.MSCALE_X] *= 2.0f;
                values[Matrix.MSCALE_Y] *= 2.0f;
                values[Matrix.MTRANS_X] = 0;
                transImageCenter(values);
                mMatrix.setValues(values);
                updateMatrix(mMatrix);
                setImageMatrix(mMatrix);
            } else {
                initImageSize();
            }
            return true;
        }
    }
}