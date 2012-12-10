package com.samsung.photodesk;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;
import com.samsung.photodesk.util.PhotoDeskUtils;

/**
 * <p>ActionMode Control class</p>
 * Managing ActionBar menu in PhotoDeskFragment({@link FolderFragment}),({@link ContentFragment})
 * @param <T> {@link FolderItem}, {@link MediaItem}
 */
public class ActionModeCallback<T> implements ActionMode.Callback, ActivityStatusImpl {
	private static final int MAX_SHARE_ITEM = 1000;
	
	private static ShareActionProvider sShareActionProvider;
	
	private int mMenuRes;
	Future<Void> mFutrue;
	Activity mActivity;
    Handler mHandler = new Handler();
    PhotoDeskFragment<T> mFragment;
	MenuTask mMenuTask;

	/**
	 * ActionModeCallback Constructor
	 * @param activity ({@link Activity})
	 * @param fragment PhotoDeskFragment {@link FolderFragment}, {@link ContentFragment}
	 * @param menuRes menuID
	 */
	public ActionModeCallback(Activity activity, PhotoDeskFragment<T> fragment, int menuRes) {
		mMenuRes = menuRes;
		mActivity = activity;
		mFragment = fragment;
		mMenuTask = new MenuTask(mActivity, fragment.getMenuOperationLinstener());
	}


	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		int menuId = item.getItemId();
		switch (menuId) {
		case R.id.set_as:
			PhotoDeskUtils.startSetAsActivity(mActivity, ((MediaObject)mFragment.getFirstSelectedItem()).getPath());
			mFragment.leaveSelectionMode();
			break;
			
		case R.id.detail:
			mFragment.showDetails();
			mFragment.leaveSelectionMode();
			break;
			
		case R.id.slide:
		case R.id.folder_slide:
			mFragment.startSlideShow();
			break;
			
		case R.id.link:
			MediaObject object = (MediaObject) mFragment.getFirstSelectedItem();
			if (object.getType() != MediaObject.FOLDER) return false;
			PhotoDeskUtils.linkFolder(
					mActivity, 
					(FolderItem)object);
			mFragment.leaveSelectionMode(); 
			break;
			
		case R.id.location_edit:
			Intent intent = new Intent(mActivity, MapViewEdit.class);
			mActivity.startActivityForResult(intent, ContentFragment.REQ_UNCHECK_VIEW);	
			break;
			
		case R.id.hide:
			mFragment.hide();
			break;
			
		default:
			mMenuTask.onItemClicked(item.getItemId());
			break;
		}
		
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(mMenuRes, menu);
		sShareActionProvider = getShareActionProvider(menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		if (mFragment != null) {
			mFragment.leaveSelectionMode();
		}
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		
		ArrayList<MediaObject> items = mFragment.getSelectedItems();
		int selectionItemCount = items.size();
		if (selectionItemCount == 0) return false;
		boolean isSingleSelect = (selectionItemCount == 1);
		boolean isProtectSelected = mFragment.isIncludeProtectedItem(items);
		mMenuTask.setSelectedItems(items);
		
        for (int i = 0; i < menu.size(); ++i) {
        	
        	int menuId = menu.getItem(i).getItemId();
        	MenuItem menuItem = menu.getItem(i);
        	if (mFragment.getActionStatusMode() == 1) {
        		menuItem.setVisible(false);
        		continue;
        	}
        	
			switch (menuId) {
			case R.id.protect:
			case R.id.unprotect:
				mFragment.setPortectMenu(menuItem);
				break;

			case R.id.edit:
				for (int subIndex = 0; subIndex < menuItem.getSubMenu().size(); subIndex++) {
            		if(menuItem.getSubMenu().getItem(subIndex).getItemId() == R.id.rename) {
                		if (isProtectSelected || !isSingleSelect) {
                			menuItem.getSubMenu().getItem(subIndex).setVisible(false);
                		} else {
                			menuItem.getSubMenu().getItem(subIndex).setVisible(true);
                		}   
            		} else if(menuItem.getSubMenu().getItem(subIndex).getItemId() == R.id.location_edit) {
            				menuItem.getSubMenu().getItem(subIndex).setVisible(true);
            		}
            	}
				break;
				
			case R.id.folder_rename:
				if (isProtectSelected || !isSingleSelect) {
        			menuItem.setVisible(false);
        		} else {
        			menuItem.setVisible(true);
        		}   
				break;
				
			case R.id.hide:
				mFragment.setHideMenu(menuItem);
				break;
				
			case R.id.set_as:
				if (isSingleSelect && ((MediaObject)mFragment.getFirstSelectedItem()).getType() == MediaItem.IMAGE) {
        			menuItem.setVisible(true);
        		} else {
        			menuItem.setVisible(false);
        		}
				break;
				
			case R.id.slide:
			case R.id.new_folder:
			case R.id.merge:
				menuItem.setVisible(isSingleSelect ? false : true);
				break;
				
			case R.id.delete:
				menuItem.setVisible(isProtectSelected ? false : true);
				break;
				
			case R.id.detail:
			case R.id.link:
			case R.id.folder_slide:
				menuItem.setVisible(isSingleSelect ? true : false);
				break;
				
			case R.id.rotation:
				if (isProtectSelected || mFragment.isRotationSupported() == false) {
					menuItem.setVisible(false);
				} else {
					menuItem.setVisible(true);
				}
				break;
				
			case R.id.add_new_folder:
			case R.id.drag_drop:
			case R.id.action_location_edit:
				menuItem.setVisible(false);
				break;
				
			default:
				break;
			}
        }  
       
		return false;
	}

	/**
	 * Set share menu intent
	 * @param isFolder true - folder item, false - content item
	 */
	public void setShareItems(final boolean isFolder) {
		if (mFutrue != null && mFutrue.isDone()) {
			mFutrue.cancel();
		}

		mFutrue = ((PhotoDeskApplication)mActivity.getApplication()).getThreadPool().submit(new Job<Void>() {
            public Void run(final JobContext jc) {
            	final Intent intent;
            	if (!jc.isCancelled()) {
            		if (isFolder) {
            			intent = createFolderShareIntent(jc);
            		} else {
            			intent = createContentShareIntent(jc);
            		}
            		
            		mHandler.post(new Runnable() {
                        public void run() {
                            if (!jc.isCancelled()) {
                                if (intent != null && sShareActionProvider != null) {
                                	sShareActionProvider.setShareIntent(intent);
                                }
                            }
                        }
                    });
            	}
                return null;
            }
        });			
	}
	
	/**
	 * Create share intent for folder item
	 * @param jc ({@link JobContext})
	 * @return share intent
	 */
	public Intent createFolderShareIntent(JobContext jc) {
		final ArrayList<Uri> uris = new ArrayList<Uri>();
		ArrayList<MediaObject> items = mFragment.getSelectedItems();
		final Intent intent = new Intent();
		int mediaType = 0;
		String mimeType = "image/*";
		
		for (int i = 0 ; items.size() > i ; i++) {
			if (jc.isCancelled()) {
				return null;
			}
			
			FolderItem item = (FolderItem) items.get(i);
			ArrayList<MediaItem> selectedItem = MediaLoader.getMediaItems(item.getId(), mActivity.getContentResolver());
			for (int j = 0; selectedItem.size() > j ; j++) {
				if (jc.isCancelled()) {
					return null;
				}
				if (uris.size() >= MAX_SHARE_ITEM) break;
				mediaType = selectedItem.get(j).getType();
				mimeType = (mediaType == MediaItem.IMAGE) ? "image/*" : "video/*";
				long id = selectedItem.get(j).getId();
				Uri uri = ContentUris.withAppendedId(selectedItem.get(j).getUri(mediaType), id);
				uris.add(uri);					
			}
		}
		
        final int size = uris.size();
        if (size > 0) {
            if (size > 1) {
                intent.setAction(Intent.ACTION_SEND_MULTIPLE).setType(mimeType);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                intent.setAction(Intent.ACTION_SEND).setType(mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            }
            intent.setType(mimeType);
        }
        
        return intent;
	}	
	
	/**
	 * Create share intent for content
	 * @param jc ({@link JobContext})
	 * @return share intent
	 */
	public Intent createContentShareIntent(JobContext jc) {
		final ArrayList<Uri> uris = new ArrayList<Uri>();
		ArrayList<MediaObject> selectedItem = mFragment.getSelectedItems();
		final Intent intent = new Intent();
		int mediaType = 0;
		String mimeType = "image/*";

		for (int i = 0; selectedItem.size() > i ; i++) {
			if (jc.isCancelled()) {
				return null;
			}
			if (uris.size() >= MAX_SHARE_ITEM) break;
			
			MediaItem item = (MediaItem) selectedItem.get(i);
			mediaType = item.getType();
			mimeType = (mediaType == MediaItem.IMAGE) ? "image/*" : "video/*";
			long id = item.getId();
			Uri uri = ContentUris.withAppendedId(item.getUri(mediaType), id);

			uris.add(uri);			
		}
	
        final int size = uris.size();
        if (size > 0) {
            
            if (size > 1) {
                intent.setAction(Intent.ACTION_SEND_MULTIPLE).setType(mimeType);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                intent.setAction(Intent.ACTION_SEND).setType(mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            }
            intent.setType(mimeType);
        }
        
        return intent;
	}
	
	/**
	 * Get share action provider
	 * @param menu ({@link Menu})
	 * @return share action provider
	 */
	public static ShareActionProvider getShareActionProvider(Menu menu) {
        MenuItem item = menu.findItem(R.id.share);
        ShareActionProvider shareActionProvider = null;
        if (item != null) {
            shareActionProvider = (ShareActionProvider) item.getActionProvider();
        }
        return shareActionProvider;
    }

	/**
	 * Menu stop running
	 */
	@Override
	public void onStop() {
		mMenuTask.onStop();
	}

	/**
	 * Menu resume
	 */
	@Override
	public void onResume() {
		mMenuTask.onResume();
	}
}
