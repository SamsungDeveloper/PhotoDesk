
package com.samsung.photodesk;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.loader.ImageConfig;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.util.HiddenFolderUtil;
import com.samsung.photodesk.util.PhotoDeskUtils;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.photodesk.util.Setting;
import com.samsung.spensdk.SCanvasView;

/**
 * <p>Photo Desk main activity</p>
 * Include Folder({@link FolderFragment}) , Content({@link ContentFragment})
 *
 */
public class PhotoDeskActivity extends BasePhotoDeskActivity  {
	
	public static final int DELETE_SIGNATURE		 = 0;
    public static final int REREG_SIGNATURE  		 = 1;
    public static final int ALL_UNPROTECT 			 = 2;
	public static final int ITEM_UNPROTECT 			 = 3;
	public static final int FOLDER_UNPROTECT 		 = 4;
	public static final int SHOW_HIDDEN_FOLDER_START = 5;
	public static final int FOLDER_HIDDEN_RESULT 	 = 6;

	public static final String FOLDER_POSITION		= "position"; 
	public static final String FOLDER_EXTEND 		= "folder_extend";
	public static final String CONTENT_EXTEND 		= "content_extend";
	public static final String SELECTED_MODE 		= "select_mode";
	public static final String CHANGE_VIDEO_SETTING = "include_videoSettin_cghange";
	public static final String SELECTED_INDEX 		= "frame_index";
	public static final String PHOTODESK_CLASS_NAME = "com.samsung.photodesk.PhotoDeskActivity";
	
    public static PhotoDeskActivity sPhotoDeskActivity;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPhotoDeskActivity = this;

        setContentView(R.layout.gallery);
        
        initView(savedInstanceState);

		ProtectUtil.getInstance().checkProtectdata();
		if (FolderFragment.getFolderItems() != null) {
			showEmptyItemView(FolderFragment.getFolderItems().size() == 0);
		}
		
		if (Setting.INSTANCE.isFristRun()) {
			Setting.INSTANCE.setFristRun(this, false);
			selectTheme();
		}
	}
    
    private void selectTheme() {
    	SettingActivity.changeStyle(this);
	}

	private void initView(Bundle savedInstanceState) {
    	Intent intent = getIntent();
        String action = intent.getAction();

		if (Intent.ACTION_CREATE_SHORTCUT.equals(action) && savedInstanceState == null) {
			ImageConfig.init(this);
			Setting.INSTANCE.initialize(this);
			String albumPath = intent.getData().toString();
			File file = new File(albumPath);
						
			ProtectUtil.getInstance().initialize(this);
            HiddenFolderUtil.getInstance().initialize(this);
            
            if (file.exists()) {
            	initFolderView(getDefaultFolderType(), PhotoDeskUtils.getLinkFolerFilePath(file), false);
            } else {
            	initFolderView(getDefaultFolderType(), false);
            }
		} else if (Intent.ACTION_GET_CONTENT.equals(action)) {
			if (!isSupportFolderView()) {
			    Setting.INSTANCE.setContentViewMode(this, ContentFragment.VIEW_GRID);
				initFolderView(getDefaultFolderType(), true);
			}
        	if (getIntent().getBooleanExtra(CHANGE_VIDEO_SETTING, false)) {
        		Setting.INSTANCE.setIncludeVideo(getApplicationContext(), false);
        	}
		} else {
			if (savedInstanceState == null) {
				initFolderView(getDefaultFolderType(), false);
			} else {
				if (savedInstanceState.getBoolean(FOLDER_EXTEND, false)) {
					initFolderView(FolderFragment.VIEW_GRID, savedInstanceState.getInt(FOLDER_POSITION, 0), false);
					View contentView = findViewById(R.id.contentView);
					if (contentView != null) {
						contentView.setVisibility(View.GONE);
					}
					
				} else if (savedInstanceState.getBoolean(CONTENT_EXTEND, false)) {
					View folderView = findViewById(R.id.folderView);
					if (folderView != null) {
						folderView.setVisibility(View.GONE);
					}
				} else {
					if (!isSupportFolderView()) {
						initFolderView(getDefaultFolderType(), savedInstanceState.getInt(FOLDER_POSITION, 0), false);
					}
				}
			}
		}
    }

	@Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	final FolderFragment folderFragment = getFolderFragment();
    	if (folderFragment != null) {
    		outState.putInt(FOLDER_POSITION, folderFragment.getSelectedPostion());
    		outState.putBoolean(FOLDER_EXTEND, folderFragment.isExtend());
    	}
    	
    	final ContentFragment contentFragment = getContentFragment();
    	if (contentFragment != null) {
    		outState.putBoolean(CONTENT_EXTEND, contentFragment.isExtend());
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	FolderFragment folderFragment = getFolderFragment();
		if (folderFragment != null && folderFragment.isImageSelectMode()) {
			getPhotoDeskActionBar();
			return false;
		} else {
			return getPhotoDeskActionBar().createMenu(R.menu.defautl_menu, menu);
		}

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        for (int i = 0; i < menu.size(); ++i) {
        	int menuId = menu.getItem(i).getItemId();
        	MenuItem menuItem = menu.getItem(i);

            if (menuId == R.id.align) {
            	int currentCompareMode = Setting.INSTANCE.getCompareMode();
            	menuItem.getSubMenu().getItem(currentCompareMode).setChecked(true);
            }

        }
        return getPhotoDeskActionBar().prepareMenu(menu);
    }
    
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
	    	if (ContentFragment.sActionStatusMode == ContentFragment.ACTION_MODE_SELECTED) {
	    		ContentFragment.sActionModeCancel = true;
	    	}			
		}
		return super.dispatchKeyEvent(event);
	}

	
	@Override
	public void onBackPressed() {
		int currentFlingPosition = PhotoDeskFragment.getCurrentFlingPosition(this);
		if (currentFlingPosition == PhotoDeskFragment.FLING_LEFT) {
			ContentFragment contentFragment = getContentFragment();
			contentFragment.flingRight();
			return;
		} else if (currentFlingPosition == PhotoDeskFragment.FLING_RIGHT) {
			FolderFragment folderFragment =  getFolderFragment();
			folderFragment.flingLeft();
			return;
		} else {
			super.onBackPressed();
		}
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            
	        case android.R.id.home:
	        	if (getFolderFragment().isImageSelectMode()) {
	        		setResult(RESULT_CANCELED);
	        		finish();
	        	} else {
	        		moveCurrentFolder();
	        	}
				break;
				
            case R.id.view_grid:
                changeContentView(ContentFragment.VIEW_GRID);
                break;
                
            case R.id.view_map:
            	changeContentView(ContentFragment.VIEW_MAP);
                break;
                
            case R.id.view_slide:
            	changeContentView(ContentFragment.VIEW_SLIDE);
                break;

            case R.id.camera:
            	PhotoDeskUtils.startCameraActivity(this);
                break;

            case R.id.align_name_asc:
                alignContentView(Setting.COMPARE_NAME_ASC);
                item.setChecked(true);
                break;

            case R.id.align_name_desc:
                alignContentView(Setting.COMPARE_NAME_DESC);
                item.setChecked(true);
                break;

            case R.id.align_date_asc:
                alignContentView(Setting.COMPARE_DATE_ASC);
                item.setChecked(true);
                break;

            case R.id.align_date_desc:
                alignContentView(Setting.COMPARE_DATE_DESC);
                item.setChecked(true);
                break;

            case R.id.new_signature:
            case R.id.re_signature:
            	PhotoDeskUtils.signatureRegistration(this, REREG_SIGNATURE);
                break;
            case R.id.fullunprotect:
            	ProtectUtil.getInstance().checkSignatureUnprotect(this, ALL_UNPROTECT);
                break;
            case R.id.image_editor:
            	PhotoDeskUtils.startImageEditor(this, "");
            	break;
            case R.id.setting:
                ((PhotoDeskApplication)getApplication()).startSettingActivity();
                break;
            case R.id.show_hidden_folder:
            	HiddenFolderUtil.getInstance().checkSignature(this, SHOW_HIDDEN_FOLDER_START);
                break;

            default:
                return false;
        }

        return true;
    }
    
    
    /**
     * Move to current selected folder
     */
    private void moveCurrentFolder() {
    	FolderFragment fodlerView = getFolderFragment();
        if (fodlerView != null) {
        	fodlerView.moveCurrentFolder();
        }
	}

    /**
     * Check the current folder view mode is supported.
     * Folder view mode - simple{@link SimpleFolderFragment} , list{@link ListFolderFragment} , grid{@link GridFolderFragment}.
     * @return true - supported , false - not supported
     */
	private boolean isSupportFolderView() {
    	final FolderFragment folderFragment =  getFolderFragment();
		if (folderFragment == null) {
			return false;
		} 
		return folderFragment.isSupportViewType(folderFragment.getViewType());
	}
	
	/**
	 * Check the selected folder view mode is supported.
	 * @param folderViewType - simple{@link SimpleFolderFragment} , list{@link ListFolderFragment} , grid{@link GridFolderFragment}.
	 * @return VIEW_SIMPLE = 0, VIEW_GRID = 1, VIEW_LIST = 2
	 */
	private int isSupportType(int folderViewType) {
    	boolean grid = getResources().getBoolean(R.bool.support_grid_folder);
    	boolean list = getResources().getBoolean(R.bool.support_list_folder);
    	boolean simple = getResources().getBoolean(R.bool.support_simple_folder);
    	
    	if (simple && (folderViewType == FolderFragment.VIEW_SIMPLE)) {
    		return FolderFragment.VIEW_SIMPLE;
		} 
    	if (grid && (folderViewType == FolderFragment.VIEW_GRID)) {
    		return FolderFragment.VIEW_GRID;
		}
    	if (list && (folderViewType == FolderFragment.VIEW_LIST)) {
    		return FolderFragment.VIEW_LIST;
		}
    	
    	if (simple) {
    		return FolderFragment.VIEW_SIMPLE;
		} else if (grid) {
			return FolderFragment.VIEW_GRID;
		} else if (list) {
			return FolderFragment.VIEW_LIST;
		} else {
			return FolderFragment.VIEW_SIMPLE;
		}
	}
	
	/**
	 * Get default folder type
	 * @return default folder type
	 */
	private int getDefaultFolderType() {
    	int folderViewType = Setting.INSTANCE.getFolderViewMode();
    	switch (folderViewType) {
    		case FolderFragment.VIEW_SIMPLE:
    			return isSupportType(FolderFragment.VIEW_SIMPLE);
    		case FolderFragment.VIEW_LIST:
    			return isSupportType(FolderFragment.VIEW_LIST);
    		case FolderFragment.VIEW_GRID:
    			return isSupportType(FolderFragment.VIEW_GRID);
    		default :
    			return FolderFragment.VIEW_SIMPLE;
    	}
	}	

	@Override
	protected void onResume() {
		super.onResume();
		refreshView();
	}

    @Override
	protected void onPause() {
		FolderFragment folder = getFolderFragment();
		if (folder.isImageSelectMode()) {
			finish();
		}
		super.onPause();
	}

    @Override
    protected void onDestroy() {
    	if (getIntent().getBooleanExtra(CHANGE_VIDEO_SETTING, false)) {
    		Setting.INSTANCE.setIncludeVideo(getApplicationContext(), true);
    	}
    	FolderFragment.stopImageLoaderThread();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case DELETE_SIGNATURE:
                if (SCanvasView.unregisterSignature(PhotoDeskActivity.this)) {
                    // Registered signature is existed
                	PhotoDeskUtils.sToastS(this, getResources().getString(R.string.signature_start_delete_failure));
                } else {
                    // Registered signature is not existed
                	PhotoDeskUtils.sToastS(this, getResources().getString(R.string.signature_start_delete_success));
                }
                break;

            case REREG_SIGNATURE:
            	PhotoDeskUtils.startSignatureRegistrationActivity(this);
                break;

            case ALL_UNPROTECT:
            	Toast.makeText(this, R.string.full_unprotect, Toast.LENGTH_SHORT).show();
				fullUnprotectedItem();
            	ProtectUtil.getInstance().allUnprotect();
            	notifyDataSetChanged();
            	
            	break;
            	
            case ITEM_UNPROTECT:
            	Toast.makeText(this, R.string.unprotect, Toast.LENGTH_SHORT).show();
            	final ContentFragment contentFragment = getContentFragment();
            	if (contentFragment != null) {
            		ProtectUtil.getInstance().changeProtect(contentFragment.getSelectedItems() , new ProtectUtil.ProtectCompleteListener() {
						
						@Override
						public void onComplete() {
		                  	if (contentFragment != null){
		                  		contentFragment.notifyDataSetChanged();
		                  	}
						}
					});
            	}
            	break;            

            case FOLDER_UNPROTECT:
            	Toast.makeText(this, R.string.unprotect, Toast.LENGTH_SHORT).show();
            	final FolderFragment folderFragment = getFolderFragment();
            	if (folderFragment != null) {
            		ProtectUtil.getInstance().changeProtect(folderFragment.getSelectedItems(), new ProtectUtil.ProtectCompleteListener() {
						
						@Override
						public void onComplete() {
		                  	if (folderFragment != null) {
		                  		folderFragment.notifyDataSetChanged();
		                  	}
						}
					});
				}
				break;
            	
            case SHOW_HIDDEN_FOLDER_START:
            		PhotoDeskUtils.startShowHiddenFolder(this, FOLDER_HIDDEN_RESULT);
            	break;    
            	
            case FOLDER_HIDDEN_RESULT:
            	if(HiddenFolderActivity.folderPositionChanged== true){
            		FolderFragment folderfragment = getFolderFragment();
                	if (folderfragment != null) {
                   		folderfragment.setFocus(folderfragment.getSelectedPostion());
                		folderfragment.reDrawContentView(folderfragment.getSelectedPostion());
                	}
             		HiddenFolderActivity.folderPositionChanged = false;
            	}
            	break;	               	
            }
        }
    	
    	if (getFolderFragment().getViewType() == FolderFragment.VIEW_SIMPLE) ((SimpleFolderFragment)getFolderFragment()).endFolderAnimation();
    	
    	leaveSelectionMode();
    }

	private void alignContentView(int compare) {
        ContentFragment contentView = getContentFragment();
        if (contentView == null) return;
        contentView.realign(compare);
    }

    /**
     * Change content view mode
     * @param viewType VIEW_GRID, VIEW_SLIDE, VIEW_MAP
     */
    private void changeContentView(int viewType) {
    	ContentFragment oldContentView = getContentFragment();
        if (oldContentView == null || oldContentView.getViewType() == viewType) {
            return;
        }

        Setting.INSTANCE.setContentViewMode(this, viewType);
        
        Fragment newContentView = ContentFragment.newInstance(viewType,
                oldContentView.getShownFolderIndex(), oldContentView.mFolder);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.contentView, newContentView).commit();
    }
    
    /**
     * Initialize folder view
     * @param viewType - VIEW_GRID, VIEW_SLIDE, VIEW_MAP
     * @param mode - selection mode
     */
    private void initFolderView(int viewType,  boolean mode) {
		int position = 0;
		FolderFragment oldFodlerView = getFolderFragment();
		if (oldFodlerView != null) {
			position = oldFodlerView.getSelectedPostion();
		}

		Fragment newFolderView = FolderFragment.newInstance(viewType, position);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.folderView, newFolderView).commit();
		
		((FolderFragment)newFolderView).setImageSelectMode(mode);
	}
    
    /**
     * Initialize folder view
     * @param viewType - VIEW_GRID, VIEW_SLIDE, VIEW_MAP
     * @param position - folder position
     * @param mode - selection mode
     */
    private void initFolderView(final int viewType, int position, boolean mode) {
        Fragment folderView = FolderFragment.newInstance(viewType, position);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.folderView, folderView).commit();

        ((FolderFragment)folderView).setImageSelectMode(mode);
    }
    
    /**
     * Initialize folder view
     * @param viewType - VIEW_GRID, VIEW_SLIDE, VIEW_MAP
     * @param path - folder path
     * @param mode - selection mode
     */
    private void initFolderView(final int viewType, String path, boolean mode) {
    	int position = 0;
		FolderItem newItem = MediaLoader.getFolder(path, getContentResolver());
		if (newItem != null) {
			position = PhotoDeskUtils.getLinkFolderPosition(newItem.getId());
		}
			
		initFolderView(viewType, position, mode);
    }

    /**
     * Full unprotect items
     */
    public void fullUnprotectedItem() {
    	FolderFragment folderView = (FolderFragment)getFragmentManager().findFragmentById(
                R.id.folderView);
    	if (folderView != null) {
    		folderView.allUnprotectedItem();
    	}
    	
    	ContentFragment contentView = (ContentFragment)getFragmentManager().findFragmentById(R.id.contentView);
    	if (contentView != null) {
    		if (folderView != null) {
    			contentView.allUnprotectedItem();
    		}
    	}
    }

    /**
     * Refresh view
     */
    public void refreshView() {
    	FolderFragment folderView = getFolderFragment();
    	if (folderView != null) {
    		folderView.update();		
    	}
    	
    	ContentFragment contentView = getContentFragment();
    	if (contentView != null && folderView != null) {
			contentView.update();
    	}

    	showEmptyItemView(FolderFragment.getFolderItems().size() == 0);
    }
    
    /**
     * Refresh all view
     */
    public void refreshAllView() {
    	FolderFragment folderView = getFolderFragment();
    	if (folderView != null) {
    		folderView.refresh();
    	}
    	
    	ContentFragment contentView = getContentFragment();
    	if (contentView != null) {
    		contentView.refresh();
    	}
        showEmptyItemView(FolderFragment.getFolderItems().size() == 0);
    }
    
    /**
     * Show empty item view
     * @param emptyFlag - true - empty , false - not empty
     */
    private void showEmptyItemView(boolean emptyFlag) {
        if (emptyFlag) findViewById(R.id.tvEmptyItem).setVisibility(View.VISIBLE);
        else findViewById(R.id.tvEmptyItem).setVisibility(View.GONE);
    }
    
    public String getCurrentActivity(){
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<RunningTaskInfo> taskInfos = activityManager.getRunningTasks(1);
		String className = taskInfos.get(0).topActivity.getClassName();
		
		return className;
    }
}
