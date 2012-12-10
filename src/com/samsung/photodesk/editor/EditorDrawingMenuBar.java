package com.samsung.photodesk.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.samsung.photodesk.R;

/**
 * <p>Drawing Bar</p>
 * Drawing tool initialization class that exist the left when tablet,
 * exist the bottom when phone.
 */
public class EditorDrawingMenuBar extends RelativeLayout{
	
	public EditorDrawingMenuBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}
	
	/**
	 * <p>Initialization drawing bar</p>
	 * layout placed by Image editor mode.
	 * @param context	{@link Context}
	 */
	public void init(Context context) {	
		PhotoDeskSCanvasManager util = PhotoDeskSCanvasManager.getInstence(context);	
		View.inflate(context, R.layout.editor_drawing_default, this);
		
		if (util.isEmptyMode()) {
			((ImageButton) findViewById(R.id.filter_btn)).setVisibility(View.GONE);
			((ImageButton) findViewById(R.id.image_btn)).setVisibility(View.VISIBLE);		
		}
	}
}
