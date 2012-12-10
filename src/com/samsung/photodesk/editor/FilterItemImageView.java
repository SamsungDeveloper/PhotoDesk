package com.samsung.photodesk.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * <p>Filter item drawing view</p>
 * it is drawing filter item bimap and item outline.
 */
public class FilterItemImageView extends ImageView{

	private Bitmap image;
	private boolean mSelected;
	
	
	public FilterItemImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		if (drawable == null)	return;
		
		image = ((BitmapDrawable) drawable).getBitmap();
		super.setImageDrawable(drawable);
	}
	
	/**
	 * <p>Set whether selected</p>
	 */
	public void setSelected(boolean seleted) {
		mSelected = seleted;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(5.0f);
		
		if (image != null) {
			canvas.drawBitmap(image, 0, 0, null);
		}		
		
		if (mSelected) {
			int width = getMeasuredWidth();
			int height = getMeasuredHeight();
			
			RectF rec = new RectF(0, 0, width, height);		
			canvas.drawRoundRect(rec, 9, 9, paint);
		}
		
	}

}
