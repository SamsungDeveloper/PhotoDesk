package com.samsung.photodesk.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.samsung.photodesk.R;

/**
 * <p>Filter Popup View</p>
 * this view has filter gridview. Connect {@link EditorPopupWindow} and {@link EditorFilterGridView}.
 */
public class EditorFilterView {

	private LinearLayout mFilterView;
	private EditorFilterGridView mFilterGv;

	Context context;
	
	public EditorFilterView(Context context, PhotoDeskScanvasView sc, int rotation) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFilterView = (LinearLayout) inflater.inflate(R.layout.editor_filter_view, null);
		
		mFilterGv = (EditorFilterGridView)mFilterView.findViewById(R.id.filter_gv);
		mFilterGv.setSCanvasData(sc, rotation);
	}	
	
	/**
	 * <p>Destroy resource of filter grideview</p>
	 */
	public void destroy() {
		mFilterGv.destroy();
	}
	
	public LinearLayout getView() {
		return mFilterView;
	}
	
	public void setCloseOnClickListener(OnClickListener close) {
		mFilterView.findViewById(R.id.filter_view_close_btn).setOnClickListener(close);
	}
	
}
