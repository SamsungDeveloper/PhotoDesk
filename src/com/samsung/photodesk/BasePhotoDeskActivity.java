package com.samsung.photodesk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.google.android.maps.MapActivity;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.util.Setting;

/**
 * <p>Base Activity for PhotoDeskActivity</p>
 * MapActivity inherits for support the Map. 
 * Pass using ({@link getMapViewContainer}) because {@link MapContentFragment} can't be generated Map.
 * 
 */
abstract public class BasePhotoDeskActivity extends MapActivity implements ActivityInterface {
	PhotoDeskActionBar mActionBar;
	private View mMapViewContainer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(Setting.INSTANCE.getThemeId());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	@Override
    public int getThemeId() {
        return Setting.INSTANCE.getThemeId();
    }

	@Override
	public PhotoDeskActionBar getPhotoDeskActionBar() {
		if (mActionBar == null) {
			mActionBar = new PhotoDeskActionBar(this);
		}
		return mActionBar;
	}
	
	/**
	 * Get FolderFragment {@link FolderFragment}
	 * @return FolderFragment or null
	 */
	public FolderFragment getFolderFragment() {
    	android.app.FragmentManager fm = getFragmentManager();
    	if (fm != null) {
    		return (FolderFragment)fm.findFragmentById(R.id.folderView);
    	}
    	return null;
    }
	
	/**
     * Get ContentFragment {@link ContentFragment}
	 * @return ContentFragment or null
	 */
	public ContentFragment getContentFragment() {
    	android.app.FragmentManager fm = getFragmentManager();
    	if (fm != null) {
    		return (ContentFragment)fm.findFragmentById(R.id.contentView);
    	}
    	return null;
    }
	
	/**
	 * Finish selection mode
	 * FolderFragment{@link FolderFragment} , ContentFragment{@link ContentFragment} selection mode finish
	 */
	public void leaveSelectionMode() {
		ContentFragment contentFragment = getContentFragment();
      	if (contentFragment != null){
      		contentFragment.leaveSelectionMode();
      	}
      	
      	FolderFragment folderFragment = getFolderFragment();
      	if (folderFragment != null) {
      		folderFragment.leaveSelectionMode();
      	}
		
	}
	
	/**
	 * FolderFragment{@link FolderFragment}'s adapter update.
	 * ContentFragment{@link ContentFragment}'s adapter update.
	 * 
	 */
	public void notifyDataSetChanged() {
		ContentFragment contentFragment = getContentFragment();
      	if (contentFragment != null){
      		contentFragment.notifyDataSetChanged();
      	}
      	
      	FolderFragment folderFragment = getFolderFragment();
      	if (folderFragment != null) {
      		folderFragment.notifyDataSetChanged();
      	}
	}
	
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    /**
     * Pass using ({@link getMapViewContainer}) because {@link MapContentFragment} can't be generated Map.
     * @return MapView container
     */
    public View getMapViewContainer() {
        if (mMapViewContainer == null) {
            mMapViewContainer = LayoutInflater.from(this).inflate(R.layout.content_map_view, null);
        }

        return mMapViewContainer;
    }
    
    @Override
    public ThreadPool getThreadPool() {
    	return ((PhotoDeskApplication)getApplication()).getThreadPool();
    }
}
