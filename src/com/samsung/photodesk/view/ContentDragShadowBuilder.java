package com.samsung.photodesk.view;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.MediaObject;

/**
 * <p>Draw item image on Drag&Drop</p>
 *
 */
public class ContentDragShadowBuilder extends View.DragShadowBuilder {
	private static final int MAX_IMAGE_CNT = 4;
	private static final int IMAGE_WIDTH = 120;
	private static final int IMAGE_HEIGHT = 120;
	private static final int IMAGE_INTERVAL = 10;
	
	private ArrayList<MediaObject> mSelectedItems;
    private Bitmap mSelectedImage[] = new Bitmap [MAX_IMAGE_CNT];
    
    private int mHeigth;
    private int mWidth;
    
    public ContentDragShadowBuilder(View v, ArrayList<MediaObject> selectedItems) {
        super(v);

        mSelectedItems = selectedItems;
        mHeigth = v.getHeight();
        mWidth = v.getWidth();
        
        for (int index = 0; index < selectedItems.size(); index++) {
        	if (index >= MAX_IMAGE_CNT) break;
        	
        	mSelectedImage[index] = ThumbnailCache.INSTANCE.getBitmap(selectedItems.get(index).getId());
        }
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
    	
    	Paint paint = new Paint();
    	paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        
    	int _x = mWidth;
    	int _y = mHeigth;
    	RectF rectF = new RectF();
    	
    	for (int index = 0; index < MAX_IMAGE_CNT; index++) {
    		Bitmap bm = mSelectedImage[MAX_IMAGE_CNT-index-1];
    		if (bm != null) {
    			rectF.set(_x-IMAGE_WIDTH, _y-IMAGE_HEIGHT, _x, _y);
    			canvas.drawBitmap(bm, null, rectF, null);
    			canvas.drawRect(rectF, paint);
    		} 
    		
    		_y -= IMAGE_INTERVAL;
    		_x -= IMAGE_INTERVAL;
    	}
        paint.setStyle(Paint.Style.FILL);
    	String size = String.valueOf(mSelectedItems.size());
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        paint.setColor(0xAA000000);
        rectF.set(rectF.left+3f, rectF.top+3f, rectF.left + paint.measureText(size) + 9f, rectF.top - paint.ascent() + paint.descent() + 7f);
        canvas.drawRect(rectF, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText(size, rectF.left + 3f, rectF.bottom - paint.descent() - 2f, paint);
    }
}
