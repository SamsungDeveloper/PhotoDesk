package com.samsung.photodesk;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.photodesk.MenuTask.OnOperationListener;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.ContentItem;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.ImageItem;
import com.samsung.photodesk.data.MediaDetails;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.data.VideoItem;
import com.samsung.photodesk.editor.AnimationData;
import com.samsung.photodesk.editor.AnimationImagePlayerActivity;
import com.samsung.photodesk.editor.SAMMDBHelper;
import com.samsung.photodesk.loader.ContentBgThumbnailLoader;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.FutureListener;
import com.samsung.photodesk.loader.ImageLoadCounter;
import com.samsung.photodesk.loader.LoadThumbTask;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;
import com.samsung.photodesk.util.PhotoDeskUtils;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.photodesk.util.SpenEventUtil;
import com.samsung.photodesk.view.ContentDragShadowBuilder;
import com.samsung.photodesk.view.DetailsHelper;
import com.samsung.photodesk.view.GridContentItemView;
import com.samsung.photodesk.view.QucikMenuItem;
import com.samsung.photodesk.view.QuickMenu;
import com.samsung.photodesk.view.DetailsHelper.CloseListener;
import com.samsung.photodesk.view.QuickMenu.OnActionItemClickListener;
import com.samsung.spensdk.SCanvasView;


/**
 * <p>Gallery view on the right side of the screen that is displayed</p>
 * Content, the grid({@link GridContentFragment}), slide({@link SlideContentFragment}), map({@link MapContentFragment}) screen is shown.
 * Content information gets into the bucket of the MediaStore. Shows the images and video in the folder.
 */
abstract public class ContentFragment extends PhotoDeskFragment<MediaItem>
		implements OnTouchListener, OnClickListener {
	
    public static final int VIEW_GRID		= 0;
    public static final int VIEW_SLIDE		= 1;
    public static final int VIEW_MAP 		= 2;
    public static final int DEFAULT_VIEW 	= VIEW_GRID;
    
    public static final int REQ_IMAGE_VIEW 	 = 0;
    public static final int REQ_SLIDE_SHOW   = 2;
    public static final int REQ_UNCHECK_VIEW = 3;

    public static final String MEDIA_ITEMS = "meida_items";
    public static final String ITEM_UPDATE = "item_update";
    public static final String FOLDER_INDEX = "index";
    
    static int sActionStatusMode = ACTION_MODE_NOMAL;
    public static boolean sActionModeCancel;
    
    private static ThreadPool sThreadPool;
    
    private int mNumColumns = 0;
    
	FolderItem mFolder;
    
    ViewGroup mContainer;
    
    private ContentBgThumbnailLoader mImageLoaderThread;

    private DetailsHelper mDetailsHelper;

    private ContentDetailsSource mDetailsSource;
    
    View mBtnOpen;
    
    SelectedItemCallback mSelectedItemCallback;
    
    /**
     * Get a view of the content view types.
     * 
     * @return VIEW_GRID, VIEW_SLIDE, VIEW_MAP
     */
    abstract public int getViewType();

    /**
     * Content to create a view.
     * 
     * @param viewType content view types. VIEW_GRID, VIEW_SLIDE, VIEW_MAP
     * @return ContentFragment
     */
    public static ContentFragment createView(int viewType) {
        if (viewType == VIEW_GRID) {
            return new GridContentFragment();
        } else if (viewType == VIEW_SLIDE) {
            return new SlideContentFragment();
        } else if (viewType == VIEW_MAP) {
            return new MapContentFragment();
        } else {
            return new GridContentFragment();
        }
    }
    
    /**
     * create an instance fragment.
     * 
     * @param viewType content view types. VIEW_GRID, VIEW_SLIDE, VIEW_MAP
     * @param index fodler index
     * @param folder folder item{@link FolderItem}
     * @return fragment instance.
     */
    public static ContentFragment newInstance(int viewType, int index, FolderItem folder) {
        ContentFragment f = createView(viewType);

        Bundle args = new Bundle();
        args.putParcelable("folder", folder);
        args.putInt(FOLDER_INDEX, index);
        f.setArguments(args);
        ContentItem.getInstance().clear();
        return f;
	}
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
    	
    	super.onCreateView(inflater, container, savedInstanceState);
    	initView();
    	
    	return mContainer;
    }
    
    /**
	 * Initialize the view.
	 */
    private void initView() {
    	if (mContainer == null) return;
    	
    	mBtnOpen = mContainer.findViewById(R.id.btnOpen);
    	mBtnOpen.setOnClickListener(this);
    	
    	View folderView = getActivity().findViewById(R.id.folderView);
    	if (folderView == null) {
    		mContainer.findViewById(R.id.rLPartitionOpen).setVisibility(View.GONE);
    	}

		if (mDetailsSource == null) {
            mDetailsSource = new ContentDetailsSource();
        }
	}

	@Override
	public void onResume() {
		if (mImageLoaderThread != null) {
			mImageLoaderThread.onResume();
		}
		super.onResume();
	}
    
    @Override
    public void onStop() {
    	if (mImageLoaderThread != null) {
    		mImageLoaderThread.onStop();
    	}
    	super.onStop();
    }
   
    @Override
    public void onDestroyView() {
		stopImageLoaderThread();
        super.onDestroyView();
    }
   
    /**
	 * refresh the content items
	 * Delete all the contents of the existing. Get the content item again.
	 */
    public void refresh() {
    	removeAll();
    	loadItems();
		setAdapter();
	}
    
    /**
	 * Updated content.
	 * 
	 * Get the content item again. Apply the changes
	 * Updated while maintaining Adapter
	 */
    public void update() {
    	if (getActivity() == null) return;
		ArrayList<MediaItem> items = getItem();
		
		if (ContentItem.getInstance().getCount() > items.size()) {
			for (int index = 0; index < ContentItem.getInstance().getCount(); index++) {
				if (items.size() > index) {
					if (ContentItem.getInstance().get(index).getId() != items.get(index).getId()) {
						ContentItem.getInstance().set(index, items.get(index));
					}
				} else {
					ContentItem.getInstance().remove(index);
					index--;
				}
			}			
		} else if (ContentItem.getInstance().getCount() < items.size()) {
			for (int index = 0; index < items.size(); index++) {
				if(ContentItem.getInstance().getCount() > index) {
					if(ContentItem.getInstance().get(index).getId() != items.get(index).getId()) {
						ContentItem.getInstance().set(index, items.get(index));
					}
				} else {
					ContentItem.getInstance().add(index, items.get(index));
				}
			}			
		} else {
			for (int index = 0; index < items.size(); index++) {
				if (ContentItem.getInstance().get(index).getId() != items.get(index).getId()) {
					ContentItem.getInstance().set(index, items.get(index));
				}
			}					
		}
	}
    
    /**
     * Folder screen updated from the path
     * @param path The path to the folder to be updated
     */
    public void updateFolder(String path) {
		FolderFragment folder = getFolderFragment();
		if (folder == null) return;
		folder.update(mFolder);
		folder.update(MediaLoader.getFolder(path, getActivity().getContentResolver()));
	}
	
    /**
     * Folder screen updated from the folder item
     * @param selectedFolder Folder items to be updated
     */
	public void updateFolder(FolderItem selectedFolder) {
		FolderFragment folder = getFolderFragment();
		if (folder == null) return;
		folder.update();
		if (selectedFolder != null) {
			folder.showContent(folder.findItemPosition(selectedFolder.getId()));
		}
	}	
	
	/**
	 * Update the currently selected folder.
	 */
	public void updateCurrentFolder() {
		FolderFragment folder = getFolderFragment();
		if (folder == null) return;
		folder.update(mFolder);
	}
	
	/**
	 * Get content items.
	 * 
	 * If you have updated properties to bring the items back.
	 * Otherwise, without reloading the currently loaded item is returned.
	 */
	private ArrayList<MediaItem> getItem() {
		if (getActivity().getIntent().getBooleanExtra(ITEM_UPDATE, true)) {
			return MediaLoader.getMediaItems(mFolder.getId(), getActivity().getContentResolver());
		} else {
			return ContentItem.getInstance().getItem();
		}
	}

	/**
	 * load content items
	 * 
	 * Update the properties to bask for items brings up again.
	 * Items in the map screen({@link MapContentFragment}) does not update.
	 */
    public void loadItems() {
    	if (getActivity().getIntent().getBooleanExtra(ITEM_UPDATE, true)) {
    		if (ContentItem.getInstance().getCount() == 0) {
    			ContentItem.getInstance().add(MediaLoader.getMediaItems(mFolder.getId(), getActivity().getContentResolver()));
    			runImageLoaderThread();
    		}
    	}
	}

    /**
     * Initialize the adapter and delete all content items.
     */
	public void removeAll() {
		if (ContentItem.getInstance().getCount() == 0) {
			ContentItem.getInstance().clear();
		}
		if (mAdapter != null) {
			mAdapter.clear();	
		}
	}

	/**
	 * Get folder index showing the content on the screen.
	 * @return Showing the folder index
	 */
	public int getShownFolderIndex() {
		if (getItemCount() == 0) return -1;
        return getArguments().getInt(FOLDER_INDEX, 0);
    }
	
	@Override
	public void selectAllItem() {
		if (mAdapter == null) return;
		final int len = mAdapter.getCount();
		for (int i = 0; i < len; i++) {
		    MediaItem item = mAdapter.getItem(i);
		    if (item == null) continue;
			if (item.isSelected() == false) {
			    item.setSelected(true);
			} 
		}
		if (mActionModeCallback != null) {
			mActionModeCallback.setShareItems(false);
		}
		notifyDataSetChanged();
		updateSelectedCount();
	}
	
	@Override
	public void deselectItems() {
		synchronized (ContentItem.getInstance()) {
			for (MediaItem item: ContentItem.getInstance().getItem()) {
				item.setSelected(false);
			}
		}
	}

	@Override
	public int getSelectedItemCount() {
		int selectCount = 0;
		synchronized (ContentItem.getInstance()) {
			for (MediaItem item: ContentItem.getInstance().getItem()) {
				if (item.isSelected()) {
					selectCount++;
				}
			}	
		}
		return selectCount;
	}

	@Override
    public boolean isRotationSupported() {
        boolean result = true;
        synchronized (ContentItem.getInstance()) {
        	for (MediaItem item: ContentItem.getInstance().getItem()) {
                if (item.isSelected()) {
                    result = PhotoDeskUtils.isRotationSupported(item.getMimeType());
                    if (result == false)
                        return result;
                }
            }
        }
        return result;
    }
    
	/**
	 * Get the index of the first selected item.
	 * @return The first selected index. If you do not. -1 Is returned.
	 */
    public int getFristSelectedItemIndex() {
        int i = 0;
        synchronized (ContentItem.getInstance()) {
        	for (MediaItem item: ContentItem.getInstance().getItem()) {
                if (item.isSelected()) {
                    return i;
                }
                i++;
            }	
        }
        return -1;
    }

    @Override
	public ArrayList<MediaObject> getSelectedItems() {
		ArrayList<MediaObject> items = new ArrayList<MediaObject>();
		synchronized (ContentItem.getInstance()) {
			for (MediaItem item : ContentItem.getInstance().getItem()) {
				if (item.isSelected()) {
					items.add(item);
				}
			}
		}
		return items;
	}

	@Override
	protected ActionModeCallback<MediaItem> createActionMode() {
		return new ActionModeCallback<MediaItem>(getActivity(), this, R.menu.content_menu);
	}

	
	@Override
    public void startSlideShow() {
        Intent intent = new Intent(getActivity(), SlideShowActivity.class);
        intent.putExtra(SlideShowActivity.SLIDE_MODE, SlideShowActivity.SLIDE_CONTENT_VIEW);
        startActivityForResult(intent, REQ_SLIDE_SHOW);
    }

	/**
	 * Start the animation.
	 * @param path animation path
	 */
	public void startAnimation(String path) {
    	Intent intent = new Intent(getActivity(), AnimationImagePlayerActivity.class);
		SAMMDBHelper db = new SAMMDBHelper(getActivity());
		AnimationData dump = db.getSAMMInfos(path);
		
		String midi = null, voice = null;
		if (dump != null) {
			midi = dump.getMidiPath();
			voice = dump.getVoicePath();
		}
		
		AnimationData data = new AnimationData(path, midi, voice);
		intent.putExtra(AnimationImagePlayerActivity.ANIMATION_DATA, data);
		startActivity(intent);
    }
	

	/**
	 * Run the viewer.
	 * @param bucketId fodler id
	 * @param shownIndex folder index
	 */
	public void startImageView(long bucketId, int shownIndex) {
		Intent intent = new Intent(getActivity(), ImageViewActivity.class);
		intent.putExtra(ImageViewActivity.BUCKET_ID, bucketId);
		intent.putExtra(ImageViewActivity.POSITION, shownIndex);
        startActivityForResult(intent, REQ_IMAGE_VIEW);
	}
	
	/**
	 * Run a background thread to load the content on a thumbnail.
	 * 
	 * If a thread is running, stop and re-run.
	 */
	protected void runImageLoaderThread() {
		stopImageLoaderThread();
		mImageLoaderThread = new ContentBgThumbnailLoader(ContentItem.getInstance().getItem());
		mImageLoaderThread.setDaemon(true);
		mImageLoaderThread.start();
		ImageLoadCounter.INSTANCE.registerLoader(mImageLoaderThread);
	}
	
	/**
	 * A background thread to load the content on a thumbnail to cancel the execution.
	 */
	protected void stopImageLoaderThread() {
		if (mImageLoaderThread != null) {
			ImageLoadCounter.INSTANCE.removeLoader(mImageLoaderThread);
            mImageLoaderThread.cancel();
            mImageLoaderThread = null;
        }
	}
	
	/**
	 * realign
	 * 
	 * Folder items when sorting content items are sorted.
	 * @param compare COMPARE_NAME_ASC, COMPARE_NAME_DESC, COMPARE_DATE_ASC, COMPARE_DATE_DESC
	 */
    public void realign(int compare) {
    	final FolderFragment folderFragment = getFolderFragment(); 
    	if (null != folderFragment) {
    		folderFragment.refleshThumbnail();
    	}
    }

    @Override
    public void showDetails() {
        if (mDetailsSource == null) {
            mDetailsSource = new ContentDetailsSource();
        }
        mDetailsSource.findIndex(getFristSelectedItemIndex());        
        
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(getActivity(), mDetailsSource);
            mDetailsHelper.setCloseListener(new CloseListener() {
                public void onClose() {
                }
            });
        }
        mDetailsHelper.show();
    }

    /**
     * ContentDetailSource
     * 
     * Managing for content detail information.
     *
     */
    private class ContentDetailsSource implements DetailsHelper.DetailsSource {
        private int mIndex;

        public int size() {
            return mAdapter.getCount();
        }

        public int getIndex() {
            return mIndex;
        }

        public int findIndex(int indexHint) {
            mIndex = indexHint;
            return mIndex;
        }

        public MediaDetails getDetails() {

            MediaItem currentItem = mAdapter.getItem(mIndex);
            if (currentItem == null) return null;
            MediaDetails details;
            switch (currentItem.getType()) {
                case MediaItem.IMAGE:
                    ImageItem imageItem = (ImageItem)mAdapter.getItem(mIndex);
                    details = imageItem.getDetails();
                    break;
                case MediaItem.VIDEO:
                    VideoItem videoItem = (VideoItem)mAdapter.getItem(mIndex);
                    details = videoItem.getDetails();
                    break;
                default:
                    details = null;
            }
            if (details != null) {
                return details;
            } else {
                return null;
            }
        }
    }

	@Override
	public void setEnabled(boolean enabled) {
		if (mContainer == null) return;
		View coverView = mContainer.findViewById(R.id.cover_view_id);
		if (coverView == null) {
			coverView = getCoverView(mContainer);
			mContainer.addView(coverView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}

		coverView.setVisibility(enabled? View.GONE : View.VISIBLE);

        if (mBtnOpen != null) {
            mBtnOpen.setEnabled(enabled);
        }
        
        super.setEnabled(enabled);
	}
	
	@Override
	public void startSelectionMode() {
		super.startSelectionMode();
		
		FolderFragment folderView = getFolderFragment();
		if (folderView != null) {
			folderView.setEnabled(false);
		}
	}
	
	/**
	 * Start the edit mode for the selected item.
	 * 
	 * Common editing mode is selected after selecting menu items run.
	 * But Mode selection menu without running the item selection.
	 * @param selectedItemCallback Items can be selected callback{@link SelectedItemCallback}
	 */
	public void startSelectionMode(SelectedItemCallback selectedItemCallback) {
		mSelectedItemCallback = selectedItemCallback;
		deselectItems();
		sActionStatusMode = ACTION_MODE_SELECTED;
		ContentItem.getInstance().get(0).setSelected(true);
		startSelectionMode();
		updateSelectedCount();
	}
	
	/**
	 * Get in edit mode.
	 * 
	 * Common(ACTION_MODE_NOMAL) editing mode is selected after selecting menu items run.
	 * Mode selection menu(ACTION_MODE_SELECTED) without running the item selection. 
	 */
	public int getActionStatusMode() {
		return sActionStatusMode;
	}
	
	@Override
	public void leaveSelectionMode() {
		super.leaveSelectionMode();
		
		if (mSelectedItemCallback != null && sActionStatusMode == ACTION_MODE_SELECTED && sActionModeCancel != true) {
			mSelectedItemCallback.onSelected(getSelectedItems());
		}
		
		sActionModeCancel = false;
		
		FolderFragment folderView = getFolderFragment();
		if (folderView != null) {
			folderView.setEnabled(true);
		}
		
		mSelectedItemCallback = null;
		sActionStatusMode = ACTION_MODE_NOMAL;
		notifyDataSetChanged();
	}
	
	@Override
	public void updateSelectedCount() {
		super.updateSelectedCount();
        
		if (mActionMode == null) return;
		View view = mActionMode.getCustomView();
		if (view != null) {
			TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
			if (tvTitle != null) {
				if (sActionStatusMode == ACTION_MODE_SELECTED) {
					tvTitle.setText(getString(R.string.add_selected_title));
					tvTitle.setVisibility(View.VISIBLE);
				} else {
					tvTitle.setVisibility(View.GONE);
				}
			}
		}
	}		
	/**
	 * Get the thread pool.
	 */
	public ThreadPool getThreadPool() {
		if (sThreadPool == null) {
			sThreadPool = ((PhotoDeskApplication)(getActivity()).getApplication()).getThreadPool();
		}
		return sThreadPool;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_IMAGE_VIEW) {
			if (resultCode == Activity.RESULT_OK) {
				if (data.getBooleanExtra(ImageViewActivity.IS_EDIT, false)) {
					FolderFragment folder = getFolderFragment();
					if (folder != null) {
						folder.update(mFolder);
					} 
					
					if (ContentItem.getInstance().getCount() == 0 && folder == null) {
						getActivity().finish();
					}
					
					notifyDataSetChanged();
				}
				
				int position = data.getIntExtra(ImageViewActivity.CURRENT_POSITION, -1);
				if (position != -1) {
					movePosition(position);
				}
			}
		} else if (requestCode == REQ_SLIDE_SHOW) {
            leaveSelectionMode();
        } else if (requestCode == REQ_UNCHECK_VIEW){
			leaveSelectionMode();
		}
		
		FolderFragment folderFragment = getFolderFragment();
		if (folderFragment != null && folderFragment.getViewType() == FolderFragment.VIEW_SIMPLE) {
			SimpleFolderFragment folderView = (SimpleFolderFragment)getFolderFragment();
		    folderView.endFolderAnimation();
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Move the view.
	 * @param position Move the index
	 */
	public void movePosition(int position) {}

	/**
	 * Start the drag.
	 * @param v Being dragged view
	 */
	protected void startDrag(View v) {
		if (getFolderFragment() == null || isExtend()) return;
		ClipData data = ClipData.newPlainText("", "");
		ArrayList<MediaObject> items = getSelectedItems();
		v.startDrag(data, new ContentDragShadowBuilder(v, items), items, 0);
	}

    
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnOpen:
                flingRight();
                break;
        }
    }
    
    /**
	 * Set the focus.
	 * 
	 * @param position focus index
	 */
    public void setFocus(int position) {
		int size = mAdapter.getCount();
		for (int cnt = 0; cnt < size; cnt++){
			mAdapter.getItem(cnt).setSelected((cnt == position));
		}
		notifyDataSetChanged();
	}

    @Override
    public void showHoverPopup(int position, final View v) {        
        if(position < 0)
        	return;
        
		mSpenEvent.showContentHoverPopup(reloadDetails(position), v);
    }
    
	public String reloadDetails(int indexHint) {
		int index = mDetailsSource.findIndex(indexHint);
		if (index == -1)
			return null;
		MediaDetails details = mDetailsSource.getDetails();
		return setDetails(details);
	}
	
	private String setDetails(MediaDetails details) {
        String detailInfo = getString(R.string.basic_information)+ "\n";
        if (details == null) return detailInfo;
        for (Entry<Integer, Object> detail : details) {
            String value = null;
            switch (detail.getKey()) {
                
                case MediaDetails.INDEX_SIZE: {
                    int key = detail.getKey();
                    value = Formatter.formatFileSize(this.getActivity(), (Long)detail.getValue());
                    if (details.hasUnit(key)) {
                        value = String.format("%s : %s %s", DetailsHelper.getDetailsName(this.getActivity(), key),
                                value, this.getActivity().getString(details.getUnit(key)));
                    } else {
                        value = String.format("%s : %s", DetailsHelper.getDetailsName(this.getActivity(), key),
                                value);
                    }
                    
                    if (value !=null )
                        detailInfo += "\n" +value ;
                    break;
                    
                }
                
                case MediaDetails.INDEX_TITLE:
                case MediaDetails.INDEX_MIMETYPE:
                case MediaDetails.INDEX_WIDTH:
                case MediaDetails.INDEX_HEIGHT:
                case MediaDetails.INDEX_DATETIME:    
                case MediaDetails.INDEX_PATH: 
                case MediaDetails.INDEX_DURATION: {
                    Object valueObj = detail.getValue();
                    if (valueObj == null) {

                    }
                    value = valueObj.toString();
                    
                    int key = detail.getKey();
                    if (details.hasUnit(key)) {
                        value = String.format("%s : %s %s", DetailsHelper.getDetailsName(this.getActivity(), key),
                                value, getString(details.getUnit(key)));
                    } else {
                        value = String.format("%s : %s", DetailsHelper.getDetailsName(this.getActivity(), key),
                                value);
                    }
                    if(value !=null )
                     detailInfo += "\n" +value ;
                    
                    
                    break;
                }   
                default: {
                    break;
                }
            }

        }
        return detailInfo;
    }
	
	@Override
	public boolean onTouchPenEraser(View view, MotionEvent event) {
		if (mSelectionMode) return false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			final int index = mSpenEvent.getContainingChildIndex((int)event.getX(), (int)event.getY());

			if (index == SpenEventUtil.INVALID_INDEX) {
				return false;
			}

			if (!mSelectionMode) {
				int firstPosition = getFirstVisiblePosition();
				if (mAdapter.getItem(index + firstPosition).isProtected())
					return false;

				deselectItems();
				mAdapter.getItem(index + firstPosition).setSelected(true);
				notifyDataSetChanged();
			}

			MenuTask menuTask = new MenuTask(getActivity(), getMenuOperationLinstener());
			menuTask.setSelectedItems(getSelectedItems());
			menuTask.onItemClicked(R.id.delete);
			leaveSelectionMode();
			return true;
			
		case MotionEvent.ACTION_MOVE:
			return true;

		case MotionEvent.ACTION_UP:
			break;

		default:
			break;
		}
		return true;
	}
	
	@Override
	public void onHoverButtonUp(View view, MotionEvent event) {
		if (mSelectionMode) return;
		final int index = mSpenEvent.getContainingChildIndex((int)event.getX(), (int)event.getY());
		if (index <= SpenEventUtil.INVALID_INDEX) return;
		
		int firstPosition = getFirstVisiblePosition();
		final MediaItem item = mAdapter.getItem(index + firstPosition);
		
		QuickMenu menu = mSpenEvent.getQuickMenu();
		makeQuickMenu(menu, item);

		mSpenEvent.showQuickMenu(getChildAt(index));
		
		item.setSelected(!item.isSelected());
		if (!mSelectionMode) {
			setFocus(index + firstPosition);
		} 
		
		menu.setOnActionItemClickListener(mQuickMenuActionListener);
		notifyDataSetChanged();
	}
	
	QuickMenu.OnActionItemClickListener mQuickMenuActionListener = new OnActionItemClickListener() {
		
		@Override
		public void onItemClick(QuickMenu source, int pos, int actionId) {
			
		switch (actionId) {
		
			case R.id.share:
				PhotoDeskUtils.startShare(getActivity(), getFirstSelectedItem().getPath(), getSelectedItems());
				break;
				
			case R.id.set_as:
				PhotoDeskUtils.startSetAsActivity(getActivity(), getFirstSelectedItem().getPath());
				break;
				
			default:
				MenuTask menuTask = new MenuTask(getActivity(), getMenuOperationLinstener());
				menuTask.setSelectedItems(getSelectedItems());
				menuTask.onItemClicked(actionId);
				break;
			}
			deselectItems();
			notifyDataSetChanged();
		}
			
	};

	/**
	 * Make Quick Menu
	 * 
	 * Create shortcut menu
	 * @param menu
	 * @param item
	 */
	private void makeQuickMenu(QuickMenu menu, MediaItem item) {
		QucikMenuItem deleteItem = new QucikMenuItem(R.id.delete, null, getResources().getDrawable(R.drawable.menu_delete_selector));
		QucikMenuItem leftRotateItem = new QucikMenuItem(R.id.rotation_left, null,getResources().getDrawable(R.drawable.menu_left_rotate_selector));
		QucikMenuItem rightRotateItem = new QucikMenuItem(R.id.rotation_right, null, getResources().getDrawable(R.drawable.menu_right_rotate_selector));
		QucikMenuItem shareItem = new QucikMenuItem(R.id.share, null, getResources().getDrawable(R.drawable.menu_share_selector));
		QucikMenuItem settingItem = new QucikMenuItem(R.id.set_as, null, getResources().getDrawable(R.drawable.menu_setting_selector));

		String mimeType = item.getMimeType();

		Log.d("GridContent", "MIME Type = " + mimeType);

		boolean isProtectedFile = ProtectUtil.getInstance().isProtected(item.getPath());

		switch (item.getType()) {
		case MediaItem.IMAGE:
			if (!isProtectedFile && PhotoDeskUtils.isRotationSupported(mimeType)) {
				menu.addActionItem(leftRotateItem);
				menu.addActionItem(rightRotateItem);
			}
			menu.addActionItem(settingItem);
		case MediaItem.VIDEO:
			if (!isProtectedFile) {
				menu.addActionItem(deleteItem);
			}
			menu.addActionItem(shareItem);
			break;
		}
	}
	    
	/**
	 *  Image to be displayed in the content view showing the adapter
	 *  
	 *  getView() from content on a thumbnail image load the content shown on the screen.
	 *  Brings a bitmap image that is loaded into the cache from the cache if. 
	 *  If you do not have a loaded image, Image load running thread({@link LoadThumbTask}) 
	 *  Then Thumbnails stored in the cache and loaded image is shown on the screen.  
	 *
	 */
	public class ContentMediaItemAdapter extends ArrayAdapter<MediaItem> {
    	private int mResource;
        private LayoutInflater mInflater;
        private int mItemHeight = 0;
        private RelativeLayout.LayoutParams mTopImageViewLayoutParams;
        private RelativeLayout.LayoutParams mImageViewLayoutParams;
        
    	public ContentMediaItemAdapter(Context context, int resource, List<MediaItem> objects) {
            super(context, resource, objects);
            mResource = resource;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    	
    	@Override
    	public int getCount() {
    	    return super.getCount();
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(mResource, parent, false);
                ViewHolder viewHolder = new ViewHolder((ImageView)convertView.findViewById(R.id.iVImage), (ImageView)convertView.findViewById(R.id.iVPlay), 
                		(ImageView)convertView.findViewById(R.id.iVAniPlay), (ImageView)convertView.findViewById(R.id.iVProtect), 
                		(ImageView)convertView.findViewById(R.id.iVCheck));
                convertView.setTag(viewHolder);
            }
            if (getViewType() == VIEW_GRID && mTopImageViewLayoutParams == null) return convertView;
            if (position >= getCount()) return convertView;
            
            final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.ivImage.setLayoutParams((position < mNumColumns) ? mTopImageViewLayoutParams : mImageViewLayoutParams);
            viewHolder.ivImage.setMinimumWidth(viewHolder.ivImage.getLayoutParams().width - viewHolder.ivImage.getPaddingLeft() - viewHolder.ivImage.getPaddingRight());
            viewHolder.ivImage.setMaxWidth(viewHolder.ivImage.getLayoutParams().width - viewHolder.ivImage.getPaddingLeft() - viewHolder.ivImage.getPaddingRight());
            final MediaItem item = (MediaItem)getItem(position);
            final Bitmap bm = ThumbnailCache.INSTANCE.getBitmap(item.getId());
            ((GridContentItemView)viewHolder.ivImage).setSelect(item.isSelected());
            
            if (viewHolder.future != null && !viewHolder.future.isDone()) {
            	viewHolder.future.cancel();
			}
            
            udpateChildView(viewHolder, item);
            
			if (bm == null) {
				makeImage(viewHolder, item);
			} else {
				WeakReference<ImageView> imageViewReference = 
						new WeakReference<ImageView>(viewHolder.ivImage);
				imageViewReference.get().setImageBitmap(bm);
			}			
            return convertView;
    	}
    	
    	protected void udpateChildView(ViewHolder hodler, MediaItem item) {
    		
    		hodler.ivAniPlay.setVisibility(View.GONE);
    		
			if (item.getType() == MediaItem.IMAGE) {
				hodler.ivPlay.setVisibility(View.GONE);
				ImageItem imageItem = ((ImageItem)item);
				if (imageItem.isInitSam()) {
					hodler.ivAniPlay.setVisibility(imageItem.isSAM() ? View.VISIBLE : View.GONE);
				} else {
					updateMediaInfo(hodler, item);
				}
			} else {
				hodler.ivPlay.setVisibility(View.VISIBLE);
			}
			
			if (hodler.ivCheck != null) {
				if (mSelectionMode && item.isSelected()) {
					hodler.ivCheck.setVisibility(View.VISIBLE);
				} else {
					hodler.ivCheck.setVisibility(View.GONE);
				}
			}
			
			hodler.ivProtect.setVisibility(item.isProtected() ? View.VISIBLE : View.GONE);
		}
    	
    	/**
    	 * Update Media Information
    	 * processing for display information.
    	 * @param viewHolder {@link ViewHolder}
    	 * @param item {@link MediaItem}
    	 */
    	private void updateMediaInfo(final ViewHolder viewHolder, final MediaItem item) {
    		if (item.getType() != MediaObject.IMAGE) return;
    		if (((ImageItem)item).isInitSam()) return;
    		
			getThreadPool().submit(new UpdateMediaInfo(item),
				new FutureListener<MediaItem>() {

					public void onFutureDone(Future<MediaItem> future) {
						if (future.isCancelled()) {
							return;
						}

						viewHolder.handler.sendMessage(
								viewHolder.handler.obtainMessage(ViewHolder.SET_ITEM, future.get()));
					}
				});
		}

    	/**
		 * create a thumbnail.
		 * 
		 * Make a run thumbnail image loaded thread.({@link LoadThumbTask})
		 * When the load is complete, the image shown on the screen.
		 * @param holder {@link ViewHolder}
		 * @param item to be created thumbnail item{@link MediaItem}
		 */
		private void makeImage(final ViewHolder holder, final MediaItem item) {
    		holder.ivImage.setImageBitmap(null);			
    		holder.future = getThreadPool().submit(
					new LoadThumbTask(LoadThumbTask.CONTENTS, item),
                    new FutureListener<Bitmap>() {
				
                public void onFutureDone(Future<Bitmap> future) {
                	ImageLoadCounter.INSTANCE.decreaseCounter();
                	
                    Bitmap bitmap = future.get();
                    if (future.isCancelled() || bitmap == null) {
                        return;
                    }
                    
                    holder.handler.sendMessage(holder.handler.obtainMessage(
                    		ViewHolder.SET_BITMAP, bitmap));
                }
            });
			
			ImageLoadCounter.INSTANCE.increaseCounter();
		}

		/**
		 * To use content from the view ViewHolder
		 * 
		 * ViewHolder use by shorten the time delay.
		 */
		class ViewHolder {
    		static final int SET_BITMAP = 0;
    		static final int SET_ITEM = 1;
    		
    		final ImageView ivImage;
    		final ImageView ivPlay;
    		final ImageView ivAniPlay;
            final ImageView ivProtect;
            final ImageView ivCheck;
    		Future<Bitmap> future;
    		
    		public ViewHolder(ImageView ivImage, ImageView ivPlay, ImageView ivAniPlay, ImageView ivProtect, ImageView ivCheck) {
    			this.ivImage = ivImage;
    			this.ivPlay = ivPlay;
    			this.ivAniPlay = ivAniPlay;
                this.ivProtect = ivProtect;
                this.ivCheck = ivCheck;    			
    		}
    		
    		final Handler handler = new Handler() {
    			@Override
    			public void handleMessage(Message message) {
    				switch (message.what) {
					case SET_BITMAP:
						Bitmap bm = (Bitmap) message.obj;
	    				if (bm != null) {
		    				WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(ivImage);
							imageViewReference.get().setImageBitmap(bm);
	    				}
						break;						
					case SET_ITEM:
						udpateChildView((MediaItem) message.obj);
						break;
					default:
						break;
					}
    			}
    		};
    		
    		protected void udpateChildView(MediaItem item) {
    			if (item.getType() == MediaItem.IMAGE) {
    				ivAniPlay.setVisibility(((ImageItem)item).isSAM() ? View.VISIBLE : View.GONE);
    			} 
			}
    	}
		
		
		/**
		 * Update Media Information
    	 * processing for SAMM display information.
		 * 
		 */
		public class UpdateMediaInfo implements Job<MediaItem> {
			MediaItem mItem;
		    public UpdateMediaInfo(MediaItem item) {
		        mItem = item;
		    }
		    public MediaItem run(JobContext jc) {
		    	if (mItem.getType() == MediaItem.IMAGE) {
		    		((ImageItem)mItem).updateSAM();
		    	}
		    	return mItem;
		    }
		}
		
        public void setItemLayoutParams(int height, int width) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams = new RelativeLayout.LayoutParams(width, mItemHeight);
            mTopImageViewLayoutParams = new RelativeLayout.LayoutParams(width, mItemHeight);
            mTopImageViewLayoutParams.setMargins(0, (int)getResources().getDimension(R.dimen.base_padding), 0, 0);
            mTopImageViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mImageViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            notifyDataSetChanged();
        }
    }
	
	@Override
	public void setPortectMenu(MenuItem menuItem) {
		if (menuItem.getItemId() == R.id.protect || menuItem.getItemId() == R.id.unprotect) {		
			if (!SCanvasView.isSignatureExist(getActivity())) {
	    		menuItem.setVisible(false);
	    	} else {
	    		ArrayList<MediaObject> selectedItems = getSelectedItems();
	    		int selectionItemCount = selectedItems.size();
	    		int protectedItemCount = ProtectUtil.getInstance().getProtectedItemCount(selectedItems);
	    		
	        	if (protectedItemCount < selectionItemCount && protectedItemCount != 0) {
	        		if(menuItem.getItemId() == R.id.protect) menuItem.setVisible(false);
	        		if(menuItem.getItemId() == R.id.unprotect) menuItem.setVisible(false);
	            } else if (protectedItemCount == selectionItemCount) {
	        		if(menuItem.getItemId() == R.id.protect) menuItem.setVisible(false);
	        		if(menuItem.getItemId() == R.id.unprotect) menuItem.setVisible(true);
	            } else {
	        		if(menuItem.getItemId() == R.id.protect) menuItem.setVisible(true);
	        		if(menuItem.getItemId() == R.id.unprotect) menuItem.setVisible(false);	            	
	            }
	    	}
		}
	}
	
	@Override
	public boolean isIncludeProtectedItem(ArrayList<MediaObject> items) {
		if (ProtectUtil.getInstance().isProtected(mFolder.getPath())) {
			return true;
		} else {
			if (ProtectUtil.getInstance().getProtectedItemCount(items) != 0) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public MediaItem getFirstSelectedItem() {
		int itemCnt = mAdapter.getCount();
		for (int index = 0; index < itemCnt; index++) {
			MediaItem item = mAdapter.getItem(index);
			if (item == null) continue;
			if (item.isSelected()) {
				return item;
			}
		}
		return null;
	}
	
	@Override
    public void allUnprotectedItem() {
    	ArrayList<MediaItem> items = ContentItem.getInstance().getItem();
		for (MediaItem mediaItem : items) {
			mediaItem.setProtect(false);
		}
	}
    
	/**
     * Processing results menu to run after 
     */
    OnOperationListener mOperationListener = new OnOperationListener() {
		
		@Override
		public void onDone(long menuId, FolderItem selectedFolder,  ArrayList<MediaObject> selectedItems,
				ArrayList<MediaObject> doneItems, boolean canceled) {
			
			update();
			
			if (menuId == R.id.delete) {
				removeEmptyFolder(doneItems);
				deleteToAdapter(doneItems);
				updateCurrentFolder();
				
				FolderFragment folder = getFolderFragment();
				if (folder == null && getItemCount() == 0) {
					getActivity().finish();
				}
		        if (FolderFragment.getFolderItems().size() == 0) {
		        	getActivity().findViewById(R.id.tvEmptyItem).setVisibility(View.VISIBLE);
		        }
			} else if (menuId == R.id.copy) {
				if (selectedFolder != null) {
					updateFolder(selectedFolder);
				}
			} else if (menuId == R.id.move || menuId == R.id.new_folder) {
				removeEmptyFolder(doneItems);
				deleteToAdapter(doneItems);
				if (selectedFolder != null) {
					updateFolder(selectedFolder);
				}
			} else if (menuId == R.id.rotation_left || menuId == R.id.rotation_right) {
				updateCurrentFolder();
			} else if (menuId == R.id.rename) {
				FolderFragment folder = getFolderFragment();
				if (folder != null) {
					folder.update();
				}
			}
			
			notifyDataSetChanged();
			leaveSelectionMode();
		}
		
		/**
		 * Remove empty folder
		 * @param items - selected items
		 */
		private void removeEmptyFolder(ArrayList<MediaObject> items) {
			for (MediaObject item : items) {
				if (item.getId() == FolderItem.NEW_FOLDER_ID)	return ;
				
				File file = new File(item.getPath());
				if (!file.exists()) {
					if (item.getType() == MediaObject.FOLDER) {
						file = new File(((FolderItem)item).getFolderPath());
					} else {
						file = new File(((MediaItem)item).getFolderPath());
					}
				}
				
				if (file.listFiles() != null && file.listFiles().length == 0)	file.delete();
			}
		}		
		/**
		 * Delete the item of the adapter.
		 * @param deleteItems Delete items
		 */
		protected void deleteToAdapter(ArrayList<MediaObject> deleteItems) {
			
			ArrayList<MediaItem> items = ContentItem.getInstance().getItem();
			synchronized (items) {
				for (int i = 0; i < items.size() ;i++) {
					for (int j = 0; j < deleteItems.size(); j++) {
						if (items.get(i).getId() == deleteItems.get(j).getId()) {
							items.remove(i);
							i--;
							break;
						}
					}
				}
			}
		}
	};
	
	/**
	 * Get listeners to handle menu after running.
	 */
	public OnOperationListener getMenuOperationLinstener() {
		return mOperationListener;
	}
	
	/**
	 * Content ID to get the index of the content item.
	 */
	@Override
	public int findItemPosition(long id) {
		int size = ContentItem.getInstance().getItem().size();
		int index = 0;
		for (; index < size; index++) {
			if (ContentItem.getInstance().getItem().get(index).getId() == id) {
				break;
			}
		}
		
		return index;
	}

	/**
	 * Content view is extended 
	 * 
	 * Invisible folder view does not exist or if the content view extended state
	 * @return The extent that it is true. Otherwise false
	 */
	public boolean isExtend() {
		View folderView = getActivity().findViewById(R.id.folderView);
		if (folderView != null) {
			if (folderView.getVisibility() != View.VISIBLE) {
				return true;
			}
		}
		return false;
	}
    
	@Override
	public void hide() {}
	
	@Override
	public void setHideMenu(MenuItem menuItem){}
    
    public void setNumColumns(int numColumns) {
        this.mNumColumns = numColumns;
    }
    
    public int getNumColumns() {
        return mNumColumns;
    }
    
    @Override
    public int getItemCount() {
        if (mAdapter == null) return -1;
        return mAdapter.getCount();
    }
    
	public boolean choosePicture(MediaItem item) {
	    if (isImageSelectMode()) {
            Intent intent = new Intent();
            Uri capturedImage = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.getId());

            intent.setData(capturedImage);
            intent.putExtra("index", getActivity().getIntent().getIntExtra(PhotoDeskActivity.SELECTED_INDEX, 0));        
            intent.putExtra("item", item);    

            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
            Log.d("Content", "isImageSelectMode  onItemClick");
            return true;
        }
	    return false;
	}
}
