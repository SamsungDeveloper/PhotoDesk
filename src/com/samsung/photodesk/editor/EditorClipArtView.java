package com.samsung.photodesk.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.samsung.photodesk.R;

/**
 * <p>ClipArt Popup View</p>
 * this view has clip art gridview. Connect {@link EditorPopupWindow} and {@link EditorClipArtGridView}.
 */
public class EditorClipArtView {

	private LinearLayout mClipArtView;
	private EditorClipArtGridView mClipArtGv;
	
	Context context;
	
	public EditorClipArtView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mClipArtView = (LinearLayout) inflater.inflate(R.layout.editor_clipart_view, null);
		mClipArtGv = (EditorClipArtGridView) mClipArtView.findViewById(R.id.clipart_gv);		
	}	
	
	/**
	 * <p>Get clip art Bitmap of index</p>
	 * @param index
	 * @return
	 */
	public Bitmap getClipArtImage(int index) {
		return mClipArtGv.getClipArtImage().get(index);
	}
	
	/**
	 * <p>Get clip art view</p>	
	 * @return	clip art view
	 */
	public LinearLayout getView() {
		return mClipArtView;
	}
	
	public void setCloseOnClickListener(OnClickListener close) {
		mClipArtView.findViewById(R.id.clipart_view_close_btn).setOnClickListener(close);
	}
	public void destroy() {
		mClipArtGv.recycle();
	}
}
