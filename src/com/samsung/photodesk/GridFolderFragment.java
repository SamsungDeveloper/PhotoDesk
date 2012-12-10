
package com.samsung.photodesk;

import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

/**
 *  <p>Screen showing the folder to the grid</p>
 *  show screen folder based on {@link GridView} grid format.
 */
public class GridFolderFragment extends FolderFragment {

    GridView mGridView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getActivity().findViewById(R.id.contentView) != null && !isExtend()) {
        	mContainer = (ViewGroup) inflater.inflate(R.layout.folder_grid_view, null);
        } else {
        	mContainer = (ViewGroup) inflater.inflate(R.layout.folder_large_grid_view, null);
        }
        
        mGridView = (GridView)mContainer.findViewById(R.id.gVGrid);
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnTouchListener(this);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        mGridView.setCacheColorHint(Color.TRANSPARENT);
        
        setCollectionView(mGridView);
        setAdapter();
        moveCurrentFolder();
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void setAdapter() {
    	int resource = (getActivity().findViewById(R.id.contentView) != null && !isExtend()) 
    			? R.layout.folder_grid_item 
    			: R.layout.folder_large_grid_item;
    	
        mAdapter = new FolderViewAdapter(getActivity(), resource,
                getMediaFolders());
        mGridView.setAdapter(mAdapter);
    }

    @Override
    void showContent(int index) {
        super.showContent(index);
        mGridView.setItemChecked(index, true);
    }

    @Override
    public int getViewType() {
        return VIEW_GRID;
    }
	
	@Override
	public void startSelectionMode() {
		super.startSelectionMode();
		mGridView.setChoiceMode(ListView.CHOICE_MODE_NONE);
	}
	
	@Override
	public void leaveSelectionMode() {
		mGridView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mGridView.setItemChecked(mSelectedPostion, true);
		super.leaveSelectionMode();
	}

	@Override
	public void moveFolder(int position) {
		if (position >= mGridView.getCount()) {
			return;
		}
		mGridView.setSelection(position);
	}
	
    @Override
    public boolean onDrag(View v, DragEvent event) {
        boolean flag = super.onDrag(v, event);
        switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_EXITED:
        case DragEvent.ACTION_DROP:
        case DragEvent.ACTION_DRAG_ENDED:
            v.setBackgroundResource(R.drawable.folder_view_list_grid_selector);
        }
        return flag;
    }
    
    @Override
    public int getFirstVisiblePosition() {
		return mGridView.getFirstVisiblePosition();
	}
    
    @Override
	public View getChildAt(int index) {
		return mGridView.getChildAt(index);
	}
}
