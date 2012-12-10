package com.samsung.photodesk;


import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.samsung.photodesk.MenuTask.OnOperationListener;
import com.samsung.photodesk.util.HiddenFolderUtil;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.sdraw.SDrawLibrary;
import com.samsung.spensdk.SCanvasView;

/**
 * <p>Photo Desk ActionBar class</p>
 * ActionBar for Photo Desk.
 *
 */
public class PhotoDeskActionBar {
	private ActionBar mActionBar;
	private Activity mActivity;
	@SuppressWarnings("unused")
	private OnOperationListener mCallBack;
	
	public PhotoDeskActionBar(Activity activity) {
        mActionBar = activity.getActionBar();
        mActivity = activity;
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

	public boolean createMenu(int menuRes, Menu menu) {
		MenuInflater inflater = mActivity.getMenuInflater();
		inflater.inflate(menuRes, menu);
		return true;
	}

	public boolean prepareMenu(Menu menu) {
		for (int i = 0; i < menu.size(); ++i) {
			if (menu.getItem(i).getItemId() == R.id.fullunprotect) {
				MenuItem menuItem = menu.getItem(i);

				if (ProtectUtil.getInstance().isProtectedData()) {
					menuItem.setVisible(true);
				} else {
					menuItem.setVisible(false);
				}
			}
			if (menu.getItem(i).getItemId() == R.id.signature) {
				Menu submenu = menu.getItem(i).getSubMenu();
				MenuItem menuItem = menu.getItem(i);
				if (SDrawLibrary.isSupportedModel()) {
					menuItem.setVisible(true);
				} else {
					menuItem.setVisible(false);
				}
				
				
				
				for (int j = 0; j < submenu.size(); ++j) {
					MenuItem submenuItem = submenu.getItem(j);

					if (submenuItem.getItemId() == R.id.new_signature) {
						
						if (SDrawLibrary.isSupportedModel()) {

							if (SCanvasView.isSignatureExist(mActivity)) {
								submenuItem.setVisible(false);
							} else {
								submenuItem.setVisible(true);
							}
						}else {
							submenuItem.setVisible(false);
						}

					} else if (submenuItem.getItemId() == R.id.re_signature) {
					
						if (SDrawLibrary.isSupportedModel()) {
							if (SCanvasView.isSignatureExist(mActivity)) {
								submenuItem.setVisible(true);
							} else {
								submenuItem.setVisible(false);
							}
						}else {
							submenuItem.setVisible(false);
						}
					}
				}
			}
			if (menu.getItem(i).getItemId() == R.id.show_hidden_folder) {
				MenuItem menuItem = menu.getItem(i);

				if (HiddenFolderUtil.getInstance().isHiddenFolderData() &&
						SCanvasView.isSignatureExist(mActivity)) {
					menuItem.setVisible(true);
				} else {
					menuItem.setVisible(false);
				}
			}
			
			if (menu.getItem(i).getItemId() == R.id.image_editor) {
				MenuItem menuItem = menu.getItem(i);

				if (SDrawLibrary.isSupportedModel()) {
					menuItem.setVisible(true);
				} else {
					menuItem.setVisible(false);
				}
			}

		}
		return true;
	}

	public void hide() {
		mActionBar.hide();
	}
	
	public boolean isShowing() {
		return mActionBar.isShowing();
	}


	public void setActionCallback(OnOperationListener callBack) {
		mCallBack = callBack;
	}
}
