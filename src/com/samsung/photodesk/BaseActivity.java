package com.samsung.photodesk;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.WindowManager;

import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.util.Setting;

/**
 * <p>Base Activity class</p>
 * Activity base class in Photo Desk.
 *
 */
public class BaseActivity extends Activity implements ActivityInterface {
	PhotoDeskActionBar mActionBar;
	ActionMode mActionMode;
	boolean mSelectionMode = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        
        setTheme(Setting.INSTANCE.getThemeId());
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
	 * <p>Start selection mode</p>
	 * @param callback - action mode callback
	 */
	public void startSelectionMode(ActionMode.Callback callback) {
		if (mSelectionMode == true) return;
        mSelectionMode = true;
		mActionMode = startActionMode(callback);
    }
	
	/**
	 * <p>Finish selection mode</p>
	 */
	public void leaveSelectionMode() {
		if (mSelectionMode == false) return;
		mSelectionMode = false;
		
		if (mActionMode != null) {
			mActionMode.finish();
			mActionMode = null;
		}
	}
	
	/**
	 * <p>Action mode invalidate</p>
	 */
	public void invalidateActionMode() {
		if (mActionMode != null) {
			mActionMode.invalidate();
		}
	}
	
	/**
	 * <p>Check selection mode</p>
	 * @return true - selection mode , false - non selection mode
	 */
	public boolean isSelectedMode() {
		return mSelectionMode;
	}

	@Override
	public ThreadPool getThreadPool() {
		return ((PhotoDeskApplication)getApplication()).getThreadPool();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return true;
	 }
}
