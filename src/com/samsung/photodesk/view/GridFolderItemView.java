package com.samsung.photodesk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.samsung.photodesk.R;

/**
 * <p>Grid Folder ImageView</p>
 * Draw folder item border.
 * To animate folder item.
 *
 */
public class GridFolderItemView extends ImageView {

    private Drawable mSelectDrawable;
    private Drawable mFrameDrawable;
	private Context mContext;
	
	public GridFolderItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSelected()) {
            if (mSelectDrawable == null) setSelectedBg();
            mSelectDrawable.draw(canvas);
        }else{
            if (mFrameDrawable == null) setFrameBg();
            mFrameDrawable.draw(canvas);
        }
	}

	/**
	 * Setting the drawable for selected item background
	 */
    private void setSelectedBg(){
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.AppTheme);
        mSelectDrawable = a.getDrawable(R.styleable.AppTheme_folderViewSelectShadow);
        mSelectDrawable.setBounds(0, 0,getWidth(), getHeight());
        a.recycle();
    }
    
    /**
     * Setting the drawable for normal item background
     */
    private void setFrameBg(){
        TypedArray a = mContext.obtainStyledAttributes(R.styleable.AppTheme);
        mFrameDrawable = a.getDrawable(R.styleable.AppTheme_folderViewShadow);
        mFrameDrawable.setBounds(0, 0,getWidth(), getHeight());
        a.recycle();
    }
	
    /**
     * Start simple folder animation
     * @param selected
     */
	public void startAnimation(boolean selected){
	    if(getId() == R.id.iVImageSub2) return;
		if(getVisibility() == GONE) return;
		if(selected)initImageTranslation();
		AnimationSet set = new AnimationSet(false);
		Animation translate;
		Animation rotate;
		
		int degree = 0;
		float translation = 0;
		if (getId() == R.id.ivFolderImage) { degree = -4; translation = -30.0f;}
		else if (getId() == R.id.iVImageSub1) { degree = -2; translation = -15.0f;}
		else if (getId() == R.id.iVImageSub3) { degree = 2; translation = 15.0f;}
		else return;
		if (selected) {
			rotate = new RotateAnimation(0, degree, getWidth() / 2, getHeight() / 2);
			translate = new TranslateAnimation(0.0f, translation, 0.0f, 0.0f);
		} else {
			rotate = new RotateAnimation(degree, 0, getWidth() / 2, getHeight() / 2);
			translate = new TranslateAnimation(translation, 0.0f, 0.0f, 0.0f);
		}

		translate.setDuration(500);
		rotate.setDuration(500);
		set.addAnimation(translate);
		set.addAnimation(rotate);
		startAnimation(set);
	}
	
	@Override
	protected void onAnimationEnd() {
	    super.onAnimationEnd();
        if(getId() == R.id.iVImageSub2) return;
	    selectImageTranslation(isSelected());
	    clearAnimation();
	}
	
	@Override
	protected void onAnimationStart() {
        if(getId() == R.id.iVImageSub2) return;
	    super.onAnimationStart();
	    initImageTranslation();
	}
	
	/**
	 * Initialize image position
	 */
	private void initImageTranslation(){
        if(getId() == R.id.iVImageSub2) return;
        float translationX = 11.0f;
        float translationY = -16.0f;
        if (getId() == R.id.ivFolderImage) {translationX = -5.0f; translationY = 0.0f;}
        else if (getId() == R.id.iVImageSub1) {translationX = 3.0f; translationY = -8.0f;}
        else if (getId() == R.id.iVImageSub3) {translationX = 19.0f; translationY = -24.0f;}
        setTranslationX(translationX);
        setTranslationY(translationY);
        setRotation(0);
	}

    /**
     * Setting image position
     */
	public void selectImageTranslation(boolean selected){
        if(getId() == R.id.iVImageSub2) return;
        if(selected){
            float degree = 0.0f;
            float translation = 16.0f;
            float translationY = -16.0f;
            if (getId() == R.id.ivFolderImage) { degree = -4.0f; translation = -30.0f; translationY = 2.5f;}
            else if (getId() == R.id.iVImageSub1) { degree = -2.0f; translation = -7.0f; translationY = -7.5f;}
            else if (getId() == R.id.iVImageSub3) { degree = 2.0f; translation = 40.0f; translationY = -22.8f;}
            setTranslationX(translation - 5);
            setRotation(degree);
            setTranslationY(translationY);
        }else{
            initImageTranslation();
        }
	}
}
