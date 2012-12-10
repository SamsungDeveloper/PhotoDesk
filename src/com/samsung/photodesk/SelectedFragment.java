package com.samsung.photodesk;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.samsung.photodesk.CustomMenu.DropDownMenu;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;

/**
 * <p>The edit mode of fragment support</p>
 * 
 * State support edit mode (ActionMode)
 * Editing mode, start {@link startSelectionMode}, Edit mode, exit the {@link leaveSelectionMode}
 * Select all {@link selectAllItem}, deselect{@link deselectItems}, fragment enable {@link setEnabled} support
 *  
 * @param <T> {@link FolderItem}, {@link MediaItem}
 */
abstract public class SelectedFragment<T> extends Fragment {
	private static final String IS_SELECTED = "is_selected";
	private static final String IS_ENABLED = "is_enabled";
	
	public static final int ACTION_MODE_NOMAL = 0;
	public static final int ACTION_MODE_SELECTED = 1;
	
	boolean mSelectionMode = false;

	boolean mImageSelectMode = false;

    ActionMode mActionMode;
    
    ArrayAdapter<T> mAdapter;
    
    View mCustomView;
    
	boolean mEnabled = true;
	
	ActionModeCallback<T> mActionModeCallback;
	
	DropDownMenu mDropDownMenu;
	
	/**
	 * Select all items.
	 */
    abstract public void selectAllItem();
    
    /**
     * Deselect all items.
     */
    abstract public void deselectItems();
    
    /**
     * Get the number of the selected item.
     * @return number of the selected item.
     */
    abstract public int getSelectedItemCount();
    
    /**
     * Get the selected item.
     * @return selected item.
     */
    abstract public ArrayList<MediaObject> getSelectedItems();
    
    /**
     * Get the first item of the selected item.
     * @return  first item of the selected item.
     */
    abstract public T getFirstSelectedItem();
    
    /**
     * create the edit mode({@link ActionModeCallback})
     * @return  {@link ActionModeCallback}
     */
    abstract protected ActionModeCallback<T> createActionMode();
    
    /**
     * Action mode, get the value.
     * ACTION_MODE_SELECTED Can not see the menu choices.
     * @return ACTION_MODE_NOMAL, ACTION_MODE_SELECTED
     */
    abstract public int getActionStatusMode();
    
    /**
     * Adapter set.
     */
    abstract public void setAdapter();
    
    /**
     * Callback to get the selected items
     */
    public interface SelectedItemCallback {
    	/**
    	 * Get the selected item. 
    	 * @param selectedItems Selected items
    	 */
    	void onSelected(ArrayList<MediaObject> selectedItems);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	
    	if (savedInstanceState != null) {
    		if (savedInstanceState.getBoolean(IS_SELECTED, false)) {
    			startSelectionMode();
    			updateSelectedCount();
    		}
    				
			if (savedInstanceState.getBoolean(IS_ENABLED, true) == false) {
    			setEnabled(false);
    		}
    	}
    	
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	outState.putBoolean(IS_SELECTED, mSelectionMode);
    	outState.putBoolean(IS_ENABLED, mEnabled);
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onResume() {
    	if (mActionModeCallback != null) {
    		mActionModeCallback.onResume();
    	}
    	super.onResume();
    }
    
    @Override
    public void onStop() {
    	if (mActionModeCallback != null) {
    		mActionModeCallback.onStop();
    	}
    	super.onStop();
    }
    
    /**
     * Check whether it is enabled.
     * @return enable is true Otherwise false
     */
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Set the enabled
     * @param enabled enabled
     */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }
    
    /**
     * Update the adapter.
     */
    public void notifyDataSetChanged() {
    	if (mAdapter != null) {
    		mAdapter.notifyDataSetChanged();
    	}
    }
    
    /**
     * Get an adapter.
     * @return ArrayAdapter
     */
    public ArrayAdapter<T> getAdatper() {
        return mAdapter;
    }
    
    /**
     * Check whether the edit mode..
     * @return Edit mode if true, Otherwise false
     */
    public boolean isSelectedMode() {
        return mSelectionMode;
    }
    
    /**
     * Get the number of items.
     * @return Number of items
     */
	public int getItemCount() {
		if (mAdapter == null) return -1;
		return mAdapter.getCount();
	}
    
	/**
	 * Changes to edit mode
	 * 
	 * Is changed to edit mode, you can select menu items run.
	 * Edit mode ({@link mActionMode}) in the {@link ActionModeCallback} menu handle.
	 */
	public void startSelectionMode() {
		if (mSelectionMode == true) return;
        mSelectionMode = true;
        mActionMode = getActivity().startActionMode(getActionModeCallback());

        CustomMenu customMenu = new CustomMenu(getActivity());
        View customView = LayoutInflater.from(getActivity()).inflate(R.layout.action_bar_seletion, null);
        mActionMode.setCustomView(customView);
        mDropDownMenu = customMenu.addDropDownMenu((Button) customView.findViewById(R.id.selection_menu), R.menu.selection);
        customMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_select_all:
                        boolean selectFlag = getItemCount() == getSelectedItemCount();
                        if (selectFlag) {
                            deselectItems();
                            leaveSelectionMode();
                        } else {
                            selectAllItem();
                        }
                        break;
                }
                return mActionModeCallback.onActionItemClicked(mActionMode, item);
            }
        });        
    }
	
	/**
	 * Cancel the edit mode
	 * 
	 * After the edit mode, the selected item will be canceled.
	 */
	public void leaveSelectionMode() {
		if (mSelectionMode == false) return;
		mSelectionMode = false;
		mActionMode.finish();
		mActionMode = null;
		
		deselectItems();
	}
	
	/**
	 * get the {@link ActionMode.Callback}
	 * @return {@link ActionModeCallback}
	 */
	ActionMode.Callback getActionModeCallback() {
		if (mActionModeCallback == null) {
			mActionModeCallback = createActionMode();
		}
		return mActionModeCallback;
	}
	
	
	/**
	 * Update the number of selected items
	 */
	public void updateSelectedCount() {
		if (mActionMode == null) return;
		int selectCount = getSelectedItemCount();
		
		if (selectCount == 0) {
			leaveSelectionMode();
			return;
		}
		
		mActionMode.invalidate();
		if (mDropDownMenu == null) return;
		MenuItem menuSelect = mDropDownMenu.findItem(R.id.action_select_all);
		if (menuSelect != null) {
			if (getItemCount() == selectCount) {
			    menuSelect.setTitle(getResources().getString(R.string.deselect_all));
			} else {
			    menuSelect.setTitle(getResources().getString(R.string.select_all));
			}
		}
		
		mDropDownMenu.setTitle(selectCount + " / " +  getItemCount() + " ");
	}
	
    /**
     * Get a view of the cover.
     * 
     * @param container Cover view container view 
     * @return cover
     */
    protected View getCoverView(View container) {
    	FrameLayout coverView = (FrameLayout) container.findViewById(R.id.cover_view_id);
    	if (coverView == null) {
    		coverView = new FrameLayout(getActivity());
    		coverView.setId(R.id.cover_view_id);
    		coverView.setBackgroundColor(getResources().getColor(R.color.enable_bg));
    	}
		return coverView;
    }
	
    public void setImageSelectMode(boolean mode) {
    	mImageSelectMode = mode;
    }
    
    public boolean isImageSelectMode() {
    	return mImageSelectMode;
    }
	
}
