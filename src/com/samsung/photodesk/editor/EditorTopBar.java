package com.samsung.photodesk.editor;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.photodesk.R;

/**
 * <p>Initialization top bar</p>
 * This view is placed at top. 
 * Set title size.
 */
public class EditorTopBar extends RelativeLayout {

	public EditorTopBar(Context context, AttributeSet as) {
		super(context, as);		
		inflate(context, R.layout.editor_top_bar, this);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		initTitleWidth(findViewById(R.id.title_text));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	/**
	 * <p>Init title size</p>
	 * @param titleView		title text view
	 */
	private void initTitleWidth(View titleView) {
		int orientation = getResources().getConfiguration().orientation;
		
		TextView title = (TextView) titleView;
		
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			title.setWidth(getResources().getDimensionPixelSize(R.dimen.editor_top_bar_text_width));
		} else {
			title.setWidth(getResources().getDimensionPixelSize(R.dimen.editor_top_bar_text_width_land));
		}
	}
	

}
