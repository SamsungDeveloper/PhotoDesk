
package com.samsung.photodesk;

import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 *  <p>Screen showing the folder to the list</p>
 *  show screen folder based on {@link ListView} list format.
 */
public class ListFolderFragment extends FolderFragment {

    ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContainer = (ViewGroup) inflater.inflate(R.layout.folder_list_view, null);
        mListView = (ListView)mContainer.findViewById(R.id.lVList);
        mListView.setOnItemClickListener(this);
        mListView.setOnTouchListener(this);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setOnItemLongClickListener(this);
        
        setCollectionView(mListView);
        setAdapter();
        moveCurrentFolder();
        
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setAdapter() {
        mAdapter = new FolderViewAdapter(getActivity(), R.layout.folder_list_item,
                getMediaFolders());
        mListView.setAdapter(mAdapter);
    }

    @Override
    void showContent(int index) {
        super.showContent(index);
        mListView.setItemChecked(index, true);
    }

    @Override
    public int getViewType() {
        return VIEW_LIST;
    }

	@Override
	public void startSelectionMode() {
		super.startSelectionMode();
		mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
	}
	
	@Override
	public void leaveSelectionMode() {
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setItemChecked(mSelectedPostion, true);
		super.leaveSelectionMode();
	}

	@Override
	public void moveFolder(int positon) {
		if (positon >= mListView.getCount()) {
			return;
		}
		mListView.setSelection(positon);
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
		return mListView.getFirstVisiblePosition();
	}
    
    public View getChildAt(int index) {
        return mListView.getChildAt(index);
     }

}
