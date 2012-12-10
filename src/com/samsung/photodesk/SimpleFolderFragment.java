package com.samsung.photodesk;

import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 *  <p>Screen showing the folder to the simple</p>
 *  Default folder view mode.
 *  show screen folder based on {@link ListView} simple format.
 */
public class SimpleFolderFragment extends FolderFragment {
	
	ListView mSimpleListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		mContainer = (ViewGroup) inflater.inflate(R.layout.folder_simple_view, null);
		mSimpleListView = (ListView) mContainer.findViewById(R.id.lVSimple);
		mSimpleListView.setOnItemClickListener(this);
		mSimpleListView.setOnTouchListener(this);
		mSimpleListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mSimpleListView.setCacheColorHint(Color.TRANSPARENT);
		mSimpleListView.setOnItemLongClickListener(this);
		mSimpleListView.setItemChecked(mSelectedPostion, true);
		
		setCollectionView(mSimpleListView);
		setAdapter();
		moveCurrentFolder();
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	
	@Override
	public void startSelectionMode() {
		super.startSelectionMode();
		mSimpleListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
	}
	
	@Override
	public void leaveSelectionMode() {
		mSimpleListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mSimpleListView.setItemChecked(mSelectedPostion, true);
		super.leaveSelectionMode();
	}

	public void setAdapter() {
		mAdapter = new FolderViewAdapter(getActivity(),
				R.layout.folder_simple_item, getMediaFolders());
		
		mSimpleListView.setAdapter(mAdapter);
	}
	
	@Override
	public int getViewType() {
		return VIEW_SIMPLE;
	}
	
	@Override
	public void moveFolder(int position) {
		if (position >= mSimpleListView.getCount()) {
			return;
		}
		mSimpleListView.setSelection(position);
	}

	@Override
    public void onItemClick(AdapterView<?> av, View v, int position, long id) {
	    ContentFragment cf = getContentFragment();
	    boolean contentSelectionMode = false;
        if (cf != null) contentSelectionMode = cf.isSelectedMode();
	    if (!sFolderItems.get(position).isSelected() && !mSelectionMode && !contentSelectionMode) {
            mAnimationFlag = true;
            mPreAnimationFlag = true;
        }
        super.onItemClick(av, v, position, id);
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                endFolderAnimation();
                break;
        }
        return super.onTouch(v, event);
    }
    
    @Override
    public boolean onDrag(View v, DragEvent event) {
        boolean flag = super.onDrag(v, event);
        switch (event.getAction()) {
        case DragEvent.ACTION_DRAG_EXITED:
        case DragEvent.ACTION_DROP:
        case DragEvent.ACTION_DRAG_ENDED:
            v.setBackgroundColor(Color.TRANSPARENT);
        }
        return flag;
    }
    
    @Override
    public int getFirstVisiblePosition() {
		return mSimpleListView.getFirstVisiblePosition();
	}
    
    public View getChildAt(int index) {
        
        return mSimpleListView.getChildAt(index);
     }
    
    public void endFolderAnimation() {
        mAnimationFlag = false;
        mPreAnimationFlag = false;
    }

}
