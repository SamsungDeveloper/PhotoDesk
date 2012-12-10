package com.samsung.photodesk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.samsung.photodesk.R;

/**
 * <p>Grid content ImageView</p>
 * Draw content grid item border. 
 *
 */
public class GridContentItemView extends ImageView {
    final static public int CONTENT_TYPE = 1;
    final static public int FOLDER_TYPE = 2;
	
    private Paint mPaint;
	private Rect mRect;
	private Context mContext;
	private int mSelectColor;
    private int mFrameColor;
    private int mItemType;
    private boolean mSelected = false;
    private boolean mTouchDown = false;
	
	public GridContentItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomAttribute);
		mItemType = ta.getInt(R.styleable.CustomAttribute_type, CONTENT_TYPE);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
        if(mPaint == null) setSelectedPaint();
        setSelectedRect();
		if(isSelected()){
	        mPaint.setColor(mSelectColor);
			canvas.drawRect(mRect, mPaint);
		}else{
	        mPaint.setColor(mFrameColor);
            canvas.drawRect(mRect, mPaint);
		}
	}
	
	/**
	 * Setting rect for selected range
	 */
	private void setSelectedRect(){
		mRect = new Rect(getPaddingLeft(), getPaddingTop(),getWidth()-getPaddingRight(), getHeight()-getPaddingBottom());
	}
	
	/**
	 * Setting the paint to the selected object
	 */
	private void setSelectedPaint(){
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.AppTheme);
        mSelectColor = a.getColor(R.styleable.AppTheme_contentViewSelectFrame, 0xffeb48);
        mFrameColor = a.getColor(R.styleable.AppTheme_contentViewFrame, 0xffffff);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		int stroke = 4;
		mPaint.setStrokeWidth(stroke);
		mPaint.setStyle(Paint.Style.STROKE);
		a.recycle();
	}
	
	public void setSelect(boolean selected) {
	    mSelected = selected;            
	}
	
	@Override
	public boolean isSelected() {
	    return mSelected;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (mItemType == CONTENT_TYPE) {
    	    switch (event.getAction()) {
    	        case MotionEvent.ACTION_DOWN:
    	            setViewScale(1.05f);
    	            break;
    	    }
	    }
	    return super.onTouchEvent(event);
	}
	
	public void setViewScale(float scale) {
        if (scale == 1.0f) mTouchDown = false;
        else mTouchDown = true;
        setScaleX(scale);
        setScaleY(scale);
	}
	
	public boolean isTouchDown() {
        return mTouchDown;
    }
}
