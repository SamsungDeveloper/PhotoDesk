package com.samsung.photodesk;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ClipDescription;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.photodesk.MenuTask.OnOperationListener;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.loader.FolderBgThumbnailLoader;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.FutureListener;
import com.samsung.photodesk.loader.ImageLoadCounter;
import com.samsung.photodesk.loader.LoadThumbTask;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;
import com.samsung.photodesk.util.HiddenFolderUtil;
import com.samsung.photodesk.util.PhotoDeskUtils;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.util.SpenEventUtil;
import com.samsung.photodesk.view.GridContentItemView;
import com.samsung.photodesk.view.GridFolderItemView;
import com.samsung.photodesk.view.QucikMenuItem;
import com.samsung.photodesk.view.QuickMenu;
import com.samsung.photodesk.view.SelectedFolderDialog;
import com.samsung.photodesk.view.SpenDialog;
import com.samsung.photodesk.view.SelectedFolderDialog.SelectedFolderCallback;
import com.samsung.spensdk.SCanvasView;

/**
 * Folders are displayed on the left side of the gallery view screen
 * Folder screen, Three screens are shown. {@ link SimpleFolderFragment}, {@ link ListFolderFragment}, grid {@ link GridFolderFragment}.
 */
abstract public class FolderFragment extends PhotoDeskFragment<FolderItem> implements OnItemClickListener , 
							OnItemLongClickListener, OnTouchListener, OnClickListener, OnDragListener {
	
	private static FolderUpdateAsyncTask sFolderUpdateAsyncTask;
	
	public static final int VIEW_SIMPLE = 0;
	public static final int VIEW_LIST 	= 1;
    public static final int VIEW_GRID 	= 2;
    
    public static final int MODE_ADD_FOLDER = 0;
    
    public static final int REQ_SLIDE_SHOW = 0;
    
    public static final String POSITION = "position"; 
	
	
	/**  The maximum number of folders that are displayed thumbnail*/
	public static final int MAX_THUMBNAIL_CNT = 4;
	
	protected static ArrayList<FolderItem> sFolderItems;
	
	private static FolderBgThumbnailLoader sImageLoaderThread;
	
	private static MenuTask sMenuTask;
	
	ViewGroup mContainer;
	
	int mSelectedPostion = 0;
	
	private ArrayList<Long> mRefreshedIds = new ArrayList<Long>();
	
	private int mBeforeFolderType;
	
	protected boolean mAnimationFlag = false;
    protected boolean mPreAnimationFlag = false;
    
    private static final int ID_SLIDE = 1;
    private static final int ID_GO = 2;
    
    /**
	 * Get the folder view type
	 * @return VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
	 */
	abstract public int getViewType();
	
	/**
	 * Move the folder to the position value.
	 * @param position folder position value
	 */
	abstract public void moveFolder(int position);
	
	/**
	 * create the folder view
	 * @param viewType Type of view to create the appearance of VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
	 * @return fodler view
	 */
	public static FolderFragment craeteView(int viewType) {
		if (viewType == VIEW_SIMPLE) {
			return new SimpleFolderFragment();
		} else if (viewType == VIEW_LIST) {
			return new ListFolderFragment();
		} else if (viewType == VIEW_GRID) {
			return new GridFolderFragment();
		} else {
			return new SimpleFolderFragment();
		}
	}
	
	/**
	 * fragment instance is created.
	 * @param viewType Type of view to create the appearance of VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
	 * @param position folder position
	 * @return fragment instance
	 */
	public static FolderFragment newInstance(int viewType, int position) {
		FolderFragment f = craeteView(viewType);
		Bundle args = new Bundle();
        args.putInt(POSITION, position);
        f.setArguments(args);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState != null) {
        	mSelectedPostion = savedInstanceState.getInt(POSITION, 0);
        } else {
            deselectItems();
            mSelectedPostion = getArguments().getInt(POSITION, 0);
        }

        showContent(mSelectedPostion);
        initContainer();
        
		return mContainer;
	}
	
	@Override
	public void onResume() {
		if (sImageLoaderThread != null) {
			sImageLoaderThread.onResume();
		}
		if (sMenuTask != null) {
			sMenuTask.onResume();
		}		
		super.onResume();
	}
	
	@Override
	public void onStop() {
		if (sFolderUpdateAsyncTask != null) {
			sFolderUpdateAsyncTask.cancel(true);
		}
		
		if (sImageLoaderThread != null) {
			sImageLoaderThread.onStop();
		}
		
		if (sMenuTask != null) {
			sMenuTask.onStop();
		}
		
		super.onStop();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(POSITION, mSelectedPostion);
        super.onSaveInstanceState(outState);
	}
	
	/**
	 * Get information media stored in folders.
	 * @return folder item ArrayList
	 */
	public ArrayList<FolderItem> getMediaFolders() {
		if (sFolderItems == null) {
			ContentResolver cr = getActivity().getContentResolver();
			sFolderItems = MediaLoader.getFolderItems(cr);
			runImageLoaderThread(cr);
		} 
		return sFolderItems;
	}
	
	/**
	 * Updated folder<p>
	 * 
	 * Delete a folder if the folder has been deleted from the array
	 * When editing folder is set to the changed item. 
	 * @param FolderItem item - change folder item , null - new folder 
	 */
	public void update(FolderItem item) {
		if (item == null || sFolderItems == null) return;
		if (item.getId() == FolderItem.NEW_FOLDER_ID) {
			updateNewFolder(item.getPath());
			return;
		}
		
		ContentResolver cr = getActivity().getContentResolver();
		FolderItem newItem = MediaLoader.getFolder(item.getId(), cr);
		int size = sFolderItems.size();
		
		for (int index = 0; index < size; index++) {
			if (sFolderItems.get(index).getId() == item.getId()) {
				if (newItem == null) {
					sFolderItems.remove(index);
					mSelectedPostion = index;
				} else {
					newItem.setItemCount(MediaLoader.getItemCount(newItem.getId(), cr));
					sFolderItems.set(index, newItem);
				}
				break;
			}
		}
		
		setFocus(mSelectedPostion);
		showContent(mSelectedPostion);
		notifyDataSetChanged();
	}
	
	/**
	 * <p>Update the newly added folder.</p>
	 * 
	 * Items newly added folder after creating the array.
	 * When you add a new folder, sort again. Then Added to the folder in which you move.
	 * @param path Add the path to the folder
	 */
	public void updateNewFolder(String path) {
		if (path == null || sFolderItems == null) return;
		ContentResolver cr = getActivity().getContentResolver();
		FolderItem newItem = MediaLoader.getFolder(path, cr);
		if (newItem == null)	return;

		newItem.setItemCount(MediaLoader.getItemCount(newItem.getId(), cr));

		sFolderItems.add(newItem);
		sFolderItems = MediaLoader.runFolderItemCompare(sFolderItems);
		
		notifyDataSetChanged();
		moveFolder(findItemPosition(newItem.getId()));
		return;
	}
	
	/**
	 * Updated folder items.
	 * 
	 * Get the folder item again. Apply the changes
	 * Updated while maintaining Adapter
	 */
	private void updateFolderItem(ArrayList<FolderItem> items) {
		if (items == null) return;
		
		if (sFolderItems.size() > items.size()) {
			for (int index = 0; index < sFolderItems.size(); index++) {
				if (items.size() > index) {
					if (sFolderItems.get(index).getId() != items.get(index).getId()
							|| sFolderItems.get(index).getItemCount() != items.get(index).getItemCount()
							|| !sFolderItems.get(index).getPath().equals(items.get(index).getPath())) {
						sFolderItems.set(index, items.get(index));
					}
				} else {
					sFolderItems.remove(index);
					index--;
				}
			}			
		} else if (sFolderItems.size() < items.size()) {
			for (int index = 0; index < items.size(); index++) {
				if (sFolderItems.size() > index) {
					if (sFolderItems.get(index).getId() != items.get(index).getId()
							|| sFolderItems.get(index).getItemCount() != items.get(index).getItemCount()
							|| !sFolderItems.get(index).getPath().equals(items.get(index).getPath())) {
						sFolderItems.set(index, items.get(index));
					}
				} else {
					sFolderItems.add(index, items.get(index));
				}
			}			
		} else {
			for (int index = 0; index < items.size(); index++) {
				if (sFolderItems.get(index).getId() != items.get(index).getId()
						|| sFolderItems.get(index).getItemCount() != items.get(index).getItemCount()
						|| !sFolderItems.get(index).getPath().equals(items.get(index).getPath())) {
					
					sFolderItems.set(index, items.get(index));
				}
			}					
		}		
		notifyDataSetChanged();		
	}
	
	/**
	 * Updated folder.
	 * 
	 * Start FolderUpdateAsyncTask Thread
	 */
	public void update() {
		if (getActivity() == null || sFolderItems == null) return;
		
		sFolderUpdateAsyncTask = new FolderUpdateAsyncTask();
		sFolderUpdateAsyncTask.execute();
	}
	
	/**
	 * FolderUpdateAsyncTask Thread
	 *
	 */
	private class FolderUpdateAsyncTask extends AsyncTask<Void, Integer, Void> {
		ArrayList<FolderItem> mItems;
		
		public FolderUpdateAsyncTask() {}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {			
			if (isCancelled()) {
				mItems = null;
				return null;
			}
			mItems	= MediaLoader.getFolderItems(getActivity().getContentResolver());
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		protected void onPostExecute(Void result) {		
			sFolderUpdateAsyncTask = null;
			updateFolderItem(mItems);
		}
	}		
	
	/**
	 * refresh the folder items
	 * Delete all the contents of the existing. Get the folder item again.
	 */
	public void refresh() {
		clearAllItems();
		setAdapter();
	}
	
	public boolean reDrawContentView(final int position) {
		if (position >= mAdapter.getCount()){
			return false;
		}
		
		ContentFragment contentView = getContentFragment();
		contentView = ContentFragment.newInstance(contentView.getViewType(), position, sFolderItems.get(position));
      	getFragmentManager().beginTransaction().replace(R.id.contentView, contentView).
			setTransition(FragmentTransaction.TRANSIT_NONE).commit();
    	
      	sFolderItems.get(position).setSelected(true);
      	
      	setTitle(sFolderItems.get(position).getDisplayName());
      	return true;
	}	
	
	/**
	 * Delete all folder items.
	 */
	public void clearAllItems() {
		if (sFolderItems != null) {
			sFolderItems.clear();
			sFolderItems = null;
		}
	}

	/**
	 * initialize the view in container.
	 * View of the extended Hide button, option button, and change the settings.
	 */
	public void initContainer() {
		if (mContainer == null) return;
		mContainer.findViewById(R.id.btnFodlerAlign).setOnClickListener(this);
        mContainer.findViewById(R.id.btnFolderAdd).setOnClickListener(this);
        View btnClose = mContainer.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        
        if (isImageSelectMode()) {
        	mContainer.findViewById(R.id.lLOption).setVisibility(View.GONE);
        } else {
        	if (isExtend()) {
        		mContainer.findViewById(R.id.partitionView).setVisibility(View.VISIBLE);
        		mContainer.findViewById(R.id.lLOption).setVisibility(View.GONE);
        	} else {
        		mContainer.findViewById(R.id.partitionView).setVisibility(View.GONE);
        		mContainer.findViewById(R.id.lLOption).setVisibility(View.VISIBLE);
        	}
        }
	}
	
	/**
	 * Folder of items to look ContentView
	 * 
	 * If position value exceed the number of items, the last item visible.
	 * @param position fodler position value
	 */
	void showContent(int position) {
		int size = getItemCount();
		if (size == 0) return;
		if (position >= size) {
			position = sFolderItems.size()-1;
		}
		mSelectedPostion = position;
		changeContentView(position);
	}
	
	/**
	 * changes to the content to fit the folder index screen.
	 *  
	 * View new content created after update the content view.
	 * @param position folder position value
	 */
	private void changeContentView(final int position) {
		ContentFragment contentView = getContentFragment();
        if (contentView == null || contentView.getShownFolderIndex() != position) {
        	int viewType = Setting.INSTANCE.getContentViewMode();
	    
  			contentView = ContentFragment.newInstance(viewType, position, (FolderItem) mAdapter.getItem(position));
	      	getFragmentManager().beginTransaction().replace(R.id.contentView, contentView).
				setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    	
	      	mAdapter.getItem(position).setSelected(true);
        } 

        contentView.setImageSelectMode(isImageSelectMode());

		setTitle(mAdapter.getItem(position).getDisplayName());
	}
	
	/**
	 * Set the name of the folder selected actionBar
	 * @param folderName Set the name of the folder
	 */
	public void setTitle(String folderName) {
		getActivity().getActionBar().setTitle(folderName);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnFodlerAlign:
			nextView();
			break;
			
		case R.id.btnFolderAdd:
			addFolder();
			break;
			
		case R.id.btnClose:
			flingLeft();
			break;

		default:
			break;
		}
	}
	
	/**
	 * Change the folder view screen.
	 * Change the order of the VIEW_SIMPLE -> VIEW_LIST -> VIEW_GRID -> VIEW_SIMPLE in order to move.
	 */
	public void nextView() {
		nextView(getViewType());
	}
	
	/**
	 * <p>Change to the next screen.<p>
	 * 
	 * Change the order of the VIEW_SIMPLE -> VIEW_LIST -> VIEW_GRID -> VIEW_SIMPLE in order to move.
	 * Is disabled, the folder view does not change.
	 * @param viewType VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
	 */
    private void nextView(int viewType) {
        if (!mEnabled) return;

            if (viewType == VIEW_GRID) {
                viewType = VIEW_SIMPLE;
            } else {
                viewType++;
            }

            if (!isSupportViewType(viewType)) {
                nextView(viewType);
            } else {
            	Setting.INSTANCE.setFolderViewMode(getActivity(), viewType);
                changeView(viewType);
            }
        }

    /**
     * <p>Change the type specified folder view.</p>
     * 
     * Then create a new folder view type change. Fragment
     * @param viewType  VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
     */
	private void changeView(int viewType) {
		if (mSelectionMode) {
			leaveSelectionMode();
		}
		
		Fragment newFolderView = FolderFragment.newInstance(viewType, mSelectedPostion);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.folderView, newFolderView).commit();
	}

	
	/**
	 * Screen shows the selected content when you click the screen a list of the folders
	 * 
	 * Enabled if running and editing mode, if you click an item is selected.
	 * Typically, the screen changes to the selected folder.
	 * Shows the expanded folder view in content view is expanded. 
	 */
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		if (!mEnabled || sFolderItems == null  || isRunFling()) return;
		
		if (mSelectionMode == true) {
			FolderItem item = sFolderItems.get(position);
			item.setSelected(!item.isSelected());
			updateSelectedCount();
			
			if (mActionModeCallback != null) {
				mActionModeCallback.setShareItems(true);
			}
			notifyDataSetChanged();
		} else {
			setFocus(position);
			if (isExtend()) {
				showFullScreenContent(position);
			} else {
				showContent(position);
				notifyDataSetChanged();
			}
		}
	}
	
	/**
	 * Screen to show only the content.
	 * @param position position value
	 */
	private void showFullScreenContent(int position) {
		View content = getActivity().findViewById(R.id.contentView);
		View folder = getActivity().findViewById(R.id.folderView);
		if (content != null || folder != null) {
			showContent(position);
			folder.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Delete the item selected by the eraser.
	 * 
	 * @param position The position of the item to delete
	 */
	public void eraserItemClick(int position) {
		if (sFolderItems == null) return;
		
		FolderItem deleteItem = sFolderItems.get(position);
		
		if (mSelectionMode == true) {
			deleteItem.setSelected(!deleteItem.isSelected());
			updateSelectedCount();
			notifyDataSetChanged();
			if (mActionModeCallback != null) {
				mActionModeCallback.setShareItems(true);
			}
			
		} else {
			for (FolderItem item: sFolderItems) {
				item.setPreSelected(false);
				if (item.isSelected()) {
					item.setSelected(false);
					item.setPreSelected(true);
				}
			}
			deleteItem.setSelected(true);
			showContent(position);
		}
	}
	
	@Override
	public void deselectItems() {
	    if (sFolderItems == null) return;
		for (FolderItem item: sFolderItems) {
			item.setSelected(false);
		}
		
		notifyDataSetChanged();
	}

	@Override
	public int getSelectedItemCount() {
		if (sFolderItems == null) return -1;
		int selectCount = 0;
		for (FolderItem item: sFolderItems) {
			if (item.isSelected()) {
				selectCount++;
			}
		}
		return selectCount;
	}

	@Override
	protected ActionModeCallback<FolderItem> createActionMode() {
		return new ActionModeCallback<FolderItem>(getActivity(), this, R.menu.folder_menu);
	}

	@Override
	public void selectAllItem() {
		if (sFolderItems == null) return;
		for (FolderItem item: sFolderItems) {
			item.setSelected(true);
		}
		if (mActionModeCallback != null) {
			mActionModeCallback.setShareItems(true);
		}
        notifyDataSetChanged();
        updateSelectedCount();
    }
	
	/**
	 * Long click the folder will change to edit mode.
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
		if (mSelectionMode == false && isImageSelectMode() == false) {
			deselectItems();
			FolderItem item = sFolderItems.get(pos);
			item.setSelected(!item.isSelected());
			startSelectionMode();
			mActionModeCallback.setShareItems(true);		
			
			updateSelectedCount();
			notifyDataSetChanged();
		}
		return true;
	}

	@Override
    public ArrayList<MediaObject> getSelectedItems() {
        ArrayList<MediaObject> selectedItems = new ArrayList<MediaObject>();

        int itemCnt = mAdapter.getCount();
        for (int index = 0; index < itemCnt; index++) {
        	FolderItem item = mAdapter.getItem(index);
            if (item.isSelected()) {
                selectedItems.add(item);
                mRefreshedIds.add(item.getId());
            }
        }

        return selectedItems;
    }
	
	/**
	 * Get the position of the selected folder.
	 * @return Selected folder position
	 */
	public int getSelectedPostion() {
		return mSelectedPostion;
	}
	
	/**
	 * Set the position of the selected folder.
	 * @param position Change the position value
	 */
	public void setSelectedPostion(int position) {
		mSelectedPostion = position;
	}	

	/**
	 *  <p>Index of the folder again focuses<p>
	 * @param position Index of the changed folder
	 */
	private void setRePositionAfterFolderChange(int position){				
		if (position < mSelectedPostion){
			setSelectedPostion(mSelectedPostion -1);
		}	
	}
	
	/**
	 * check the folder view extend
	 * 
	 * Content view does not exist or is invisible folder, view extended state
	 * @return The extent that it is true Otherwise false
	 */
	public boolean isExtend() {
		View contentView = getActivity().findViewById(R.id.contentView);
		if (contentView == null) return true;
		return contentView.getVisibility() != View.VISIBLE;
	}


	/**
	 * Run a background thread to load the thumbnail folder
	 * 
	 * If a thread is running, stop and re-run.
	 */
	public static void runImageLoaderThread(ContentResolver cr) {
		if (sFolderItems == null) return;
		stopImageLoaderThread();
		sImageLoaderThread = new FolderBgThumbnailLoader(cr, sFolderItems);
		sImageLoaderThread.setDaemon(true);
		sImageLoaderThread.start();
		ImageLoadCounter.INSTANCE.registerLoader(sImageLoaderThread);
	}
	
	/**
	 * Cancel a background thread execution.
	 */
	public static void stopImageLoaderThread() {
		if (sImageLoaderThread != null) {
			ImageLoadCounter.INSTANCE.removeLoader(sImageLoaderThread);
			sImageLoaderThread.cancel();
			sImageLoaderThread = null;
		}
	}

	/**
	 * Get the background thread to load the thumbnails folder.
	 * @return {@link FolderBgThumbnailLoader}
	 */
	public static FolderBgThumbnailLoader getImageLoader() {
		return sImageLoaderThread;
	}
	
	@Override
	public boolean onDrag(View v, DragEvent event) {
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_ENTERED:
	        TypedArray a = getActivity().obtainStyledAttributes(R.styleable.AppTheme);
			v.setBackgroundColor(a.getColor(R.styleable.AppTheme_contentViewSelectFrame, 0xffeb48));
			a.recycle();
			break;
                                                                                         
		case DragEvent.ACTION_DRAG_STARTED:
			return processDragStarted(event);

		case DragEvent.ACTION_DROP:
			return processDrop(v, event, (Integer) v.getTag(R.id.ivFolderImage));
		
		case DragEvent.ACTION_DRAG_ENDED:
            getView().setBackgroundColor(Color.TRANSPARENT);
			if (!mEnabled) {
				getCoverView(mContainer).setVisibility(View.VISIBLE);
			}
			break;
		}
		return false;
	}
	
	/**
	 * To use the folder view ViewHolder
	 * 
	 * shorten the time delay in the getView ().
	 */
	class ViewHolder {
		static final int IMAGE_LOAD = 0;
		static final int SET_ITEM = 1;
		final ImageView ivImage[] = new ImageView[MAX_THUMBNAIL_CNT];
		final TextView tvName;
		final TextView tvCount;
        final ImageView ivProtect;
        final ImageView ivCheck;
		
		
		ArrayList<Future<Bitmap>> futureBitmap = new ArrayList<Future<Bitmap>>(MAX_THUMBNAIL_CNT);
		Future<MediaItem []> futureFolder;
		
		public ViewHolder(ImageView ivMainImage, ImageView ivSubImage1,
				ImageView ivSubImage2, ImageView ivSubImage3,
				TextView tvName, TextView tvCount, ImageView ivProtect, ImageView ivCheck) {
			this.ivImage[0] = ivMainImage;
			this.ivImage[1] = ivSubImage1;
			this.ivImage[2] = ivSubImage2;
			this.ivImage[3] = ivSubImage3;
			this.tvName = tvName;
			this.tvCount = tvCount;
            this.ivProtect = ivProtect;
            this.ivCheck = ivCheck;
			
			int posX = -5;
	        int posY = 0;
	        int POS_INCREASE = 8;
	        
	        if (getViewType() == FolderFragment.VIEW_GRID) {
	        	if (!isExtend()) {
	        		POS_INCREASE = 6;
	        	} 
	        } 
			
			for (int index = 0; index < MAX_THUMBNAIL_CNT; index++) {
				futureBitmap.add(null);
				if (getViewType() == VIEW_LIST) continue;
				if (ivImage[index] != null) {
					ivImage[index].setTranslationX(posX);
					ivImage[index].setTranslationY(posY);
				}
				
				posX += POS_INCREASE;
				posY -= POS_INCREASE;
			}
		}
		
		public void init(int itemPosition, int itemCount, boolean selected, boolean preSelected){
		    int count = Math.min(itemCount, MAX_THUMBNAIL_CNT);
		    for (int index = 0; index < MAX_THUMBNAIL_CNT; index++) {
		        if (ivImage[index] == null) continue;
                if (index >= count) ivImage[index].setVisibility(View.GONE);
                else ivImage[index].setVisibility(View.VISIBLE);
                if (getViewType() == VIEW_SIMPLE) {
                    GridFolderItemView imageView = (GridFolderItemView) ivImage[index];
                    if (!mSelectionMode) {
                        selected = mSelectedPostion == itemPosition;
                        imageView.clearAnimation();
                        if ((selected && mAnimationFlag) || (preSelected && mPreAnimationFlag)) {
                        	imageView.startAnimation(selected);
                        }
                        imageView.selectImageTranslation(selected);
                    } else {
                        boolean currentFolder = false;
                        if (mSelectedPostion == itemPosition) currentFolder = true;
                        imageView.selectImageTranslation(currentFolder);
                    }
                } else if (getViewType() == VIEW_GRID && isExtend()) {
                    ((GridContentItemView)ivImage[index]).setSelect(selected);
                }
                ivImage[index].setSelected(selected);
            }
		}
		
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				if (message.what == ViewHolder.IMAGE_LOAD) {
					int position = message.arg1;
					if (ivImage[position] != null) {
						WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(ivImage[position]);
						imageViewReference.get().setImageBitmap((Bitmap) message.obj);
					}
				} 
			}
		};
	}	
	
	/**
	 *  Folder view to display images
	 *  
	 */
	public class FolderViewAdapter extends ArrayAdapter<FolderItem> {
		private final LayoutInflater mInflater;
		private int mResoruceID;
		private ThreadPool mThreadPool;
    
	    public FolderViewAdapter(Context context, int resource, List<FolderItem> objects) {
	        super(context, resource, objects);
	        
	        mResoruceID = resource;
	        mInflater = LayoutInflater.from(context);
	        mThreadPool =  ((PhotoDeskApplication)((Activity)context).getApplication()).getThreadPool();
	    }
	
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	final FolderItem item = getItem(position);
	
	        if (convertView == null) {
	            convertView = mInflater.inflate(mResoruceID, parent, false);
	            convertView.setTag(new ViewHolder(
	            		(ImageView) convertView.findViewById(R.id.ivFolderImage),
	            		(ImageView) convertView.findViewById(R.id.iVImageSub1),
	            		(ImageView) convertView.findViewById(R.id.iVImageSub2),
	            		(ImageView) convertView.findViewById(R.id.iVImageSub3),
	            		(TextView) convertView.findViewById(R.id.tvName),
	            		(TextView) convertView.findViewById(R.id.tvCount),
	            		(ImageView) convertView.findViewById(R.id.iVProtect),
                        (ImageView) convertView.findViewById(R.id.iVCheck)));
	            
	            convertView.setOnDragListener(FolderFragment.this);
	        }
	        
	        ViewHolder holder = (ViewHolder) convertView.getTag();
            if (mSelectedPostion == position)convertView.setActivated(true);
            else convertView.setActivated(false);
    	    holder.init(position, item.getItemCount(), item.isSelected(), item.isPreSelected());
	        holder.tvName.setText(item.getDisplayName());
	        holder.tvCount.setText(String.format("(%d)", item.getItemCount()));

	        holder.ivProtect.setVisibility(item.isProtected() ? View.VISIBLE : View.GONE);
	        
			if (mSelectionMode && item.isSelected()) {
			    holder.ivCheck.setVisibility(View.VISIBLE);
			}else {
			    holder.ivCheck.setVisibility(View.GONE);
			    holder.tvName.setTypeface((item.isSelected()) ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
			    holder.tvCount.setTypeface((item.isSelected()) ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
			}
			
			Future<MediaItem []> future = holder.futureFolder;
        	if (future != null && !future.isDone()) {
        		future.cancel();
			}
        	
			int count = item.getThumbCount();
			for (int index = 0; index < count; index++) {
				if (item == null) continue;
				
				Future<Bitmap> imageFuture = holder.futureBitmap.get(index);
	        	if (imageFuture != null && !imageFuture.isDone()) {
	        		imageFuture.cancel();
				}
			}
				
			setThumbView(holder, item, position);
			
	        convertView.setTag(R.id.ivFolderImage, Integer.valueOf(position));
	        
	        return convertView;
	    }

	    /**
	     * Set thumbnails.
	     * 
	     * @param holder {@link ViewHolder}
	     * @param folderItem {@link FolderItem}
	     * @param position The position of the folder
	     */
		private void setThumbView(final ViewHolder holder, final FolderItem folderItem, final int position) {
			if (!folderItem.isInitThumb()) {
				makeThumb(holder, folderItem);
				
            	for (int index = 0; index < MAX_THUMBNAIL_CNT; index++) {
            		final ImageView ivImage = holder.ivImage[index];
					if (ivImage == null) continue;
					ivImage.setImageBitmap(null);
            	}

			} else {
				MediaItem [] thubmItems = folderItem.getImages();
				for (int index = 0; index < MAX_THUMBNAIL_CNT; index++) {
					final ImageView ivImage = holder.ivImage[index];
					if (ivImage == null) continue;
					
					MediaItem item = thubmItems[index];
					if (item == null) {
						ivImage.setVisibility(View.GONE);
					} else {
						final Bitmap bm = ThumbnailCache.INSTANCE.getFolderBitmap(item.getId());
						if (bm != null) {
							WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(ivImage);
							imageViewReference.get().setImageBitmap(bm);
						} else {
							loadThumb(holder, index, folderItem, item);
							ivImage.setImageBitmap(null);
						}
					}
				}
			}
		}

		/**
		 * Create thumbnails.
		 * 
		 * Loading thumbnail to display the information in a folder ({@ link Make Thumb Info}) after
		 * Get a thumbnail image loaded threads ({@ link Load Thumb Task}).
		 * @param holder {@link ViewHolder}
		 * @param folderItem Create thumbnails folder{@link FolderItem}
		 */
		private void makeThumb(final ViewHolder holder, final FolderItem folderItem) {
			Future<MediaItem []> future = holder.futureFolder;
        	if (future != null && !future.isDone()) {
        		future.cancel();
			}
        	
        	holder.futureFolder = mThreadPool.submit(new MakeThumbInfo(folderItem),
				new FutureListener<MediaItem []>() {

					public void onFutureDone(Future<MediaItem []> future) {
						if (future.isCancelled()) {
							folderItem.clearThumb();
							return;
						}

						MediaItem [] thubmItems = folderItem.getImages();
						int count = folderItem.getThumbCount();
						for (int index = 0; index < count; index++) {
							MediaItem item = thubmItems[index];
							if (item == null) continue;
							Bitmap bm = ThumbnailCache.INSTANCE.getFolderBitmap(thubmItems[index].getId());
							
							if (bm != null) {
								holder.handler.sendMessage(holder.handler.obtainMessage(
			                    		ViewHolder.IMAGE_LOAD, index, 0, bm));
							} else {
							loadThumb(holder, index, folderItem, thubmItems[index]);
						}
					}
					}
				});
		}
		
		/**
		 * Thumbnails load.
		 * 
		 * Run a thread to load the thumbnail images.
		 * {@ Link MediaItem} requestImage () to get the image from the thread to load an image. 
		 * The loaded image is stored in the cache{@link ThumbnailCache}.
		 * 
		 * @param holder {@link ViewHolder}
		 * @param index  To load the thumbnail index
		 * @param folderItem Folder items to be loaded {@link FolderItem}
		 * @param item Thumbnail to load information {@link MediaItem}
		 */
		private void loadThumb(final ViewHolder holder, final int index, 
				final FolderItem folderItem, final MediaItem item) {
			if (item == null) return;
			
			Future<Bitmap> future = holder.futureBitmap.get(index);
        	if (future != null && !future.isDone()) {
        		future.cancel();
			}
        	
        	future = mThreadPool.submit(
        			new LoadThumbTask(LoadThumbTask.FOLDER, item),
                    new FutureListener<Bitmap>() {
				
                public void onFutureDone(Future<Bitmap> future) {
                	ImageLoadCounter.INSTANCE.decreaseCounter();
                    Bitmap bitmap = future.get();
                    if (future.isCancelled()) {
                        return;
                    }
                    
                    holder.handler.sendMessage(holder.handler.obtainMessage(
                    		ViewHolder.IMAGE_LOAD, index, 0, bitmap));
                    
                }
            });
        	
        	ImageLoadCounter.INSTANCE.increaseCounter();
        	holder.futureBitmap.set(index, future);
		}

		/**
		 * Creates a thumbnail of information.
		 */
		public class MakeThumbInfo implements Job<MediaItem []> {
			FolderItem mItem;
		    
		    public MakeThumbInfo(FolderItem item) {
		        mItem = item;
		    }
		    public MediaItem [] run(JobContext jc) {
		    	
				ArrayList<MediaItem> items = MediaLoader.getMediaItems(
						mItem.getId(), getActivity().getContentResolver());
				
				MediaItem [] thumbItems = mItem.getImages();
				for (int index = 0; index < MAX_THUMBNAIL_CNT; index++) {
					if (jc.isCancelled()) {
						break;
					}
					
					if (index >= items.size()) {
						thumbItems[index] = null;
					} else {
						thumbItems[index] = items.get(index);
					}
				}
				
				if (items.get(0) != null) {
					items.get(0).requestImage(LoadThumbTask.FOLDER).run(jc);
				}
				
		    	return thumbItems;
		    }
		}
	}
	
	
	/**
	 * If the item was drop calls 
	 * @param view The View that received the drag event.
	 * @param event The DragEvent object for the drag event. 
	 * @param position folder position
	 * @return
	 */
	private boolean processDrop(View view, DragEvent event, int position) {
		@SuppressWarnings("unchecked")
		ArrayList<MediaObject> items = (ArrayList<MediaObject>) event.getLocalState();
		if (items == null || position >= sFolderItems.size()) return false;

		itemMoveToDragDrop(position, items);

        return true;
    }

	/**
	 * Invoked if the item is dragged
	 * @param event The DragEvent object for the drag event.
	 * @return Returns true if one of the MIME types in the clip description matches the desired MIME type, else false.
	 */
	boolean processDragStarted(DragEvent event) {
		if (!mEnabled) {
			getCoverView(mContainer).setVisibility(View.GONE);
			getView().setBackgroundColor(getResources().getColor(R.color.enable_bg));
		}
		
        ClipDescription clipDesc = event.getClipDescription();
        if (clipDesc != null) {
            return clipDesc.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        }
        return false;
    }

	/**
	 * Get folder items that are loaded.
	 * @return folder items ArrayList
	 */
	public static ArrayList<FolderItem> getFolderItems() {
		return sFolderItems;
	}

	/**
	 * Folder ID to get the value of the location of the folder items.
	 */
	@Override
	public int findItemPosition(long id) {
		if (sFolderItems == null) return -1; 
		int size = sFolderItems.size();
		int index = 0;
		for (; index < size; index++) {
			if (sFolderItems.get(index).getId() == id) {
				break;
			}
		}
		
		return index;
	}

	/**
	 * Drag-and-drop the file is moved.
	 * @param position The position of the selected folder
	 * @param items Selected a list of file information
	 */
	public void itemMoveToDragDrop(final int position, ArrayList<MediaObject> items) {
		ContentResolver cr = getActivity().getContentResolver();

		mRefreshedIds = new ArrayList<Long>();
		mRefreshedIds.add(sFolderItems.get(position).getId());
		mRefreshedIds.add(MediaLoader.getFolder(items.get(0).getPath(), cr).getId());
		Log.d("itemMoveToDragDrop", "first position = " + position);	
		
		sMenuTask = new MenuTask(getActivity(), getMenuOperationLinstener());
		sMenuTask.setSelectedItems(items);
		sMenuTask.setSelectedFolder(sFolderItems.get(position));
		sMenuTask.onItemClicked(R.id.drag_drop);				
	}
	
	/**
	 * Set Folder Focus
	 * Set focus information to selected folder item.
	 * 
	 * @param position - Folder item position.
	 */
	public void setFocus(int position) {
		if (sFolderItems == null) return;
		
		int size = sFolderItems.size();
		if (size == 0) return;
		if (position >= size) {
			position = sFolderItems.size()-1;
		}
		
		FolderItem focusItem = sFolderItems.get(position);
		if (focusItem.isSelected()) return;
		for (FolderItem item: sFolderItems){
			item.setPreSelected(false);
		    if (item.isSelected()) {
		    	item.setSelected(false);
		    	item.setPreSelected(true);
		    }
		}
		focusItem.setSelected(true);
	}
	
	
	@Override
	public void startSelectionMode() {
		ContentFragment contentView = getContentFragment();
		if (contentView != null) {
			contentView.setEnabled(false);
		}
		super.startSelectionMode();
	}
	
	@Override
	public void leaveSelectionMode() {
		super.leaveSelectionMode();
		
		ContentFragment contentView = getContentFragment();
		if (contentView != null) {
			contentView.setEnabled(true);
		}
		setFocus(mSelectedPostion);
	}

	
	/**
	 * add folder.
	 */
	private void addFolder() {
		addFolder(new OnSelectedAddFolder() {
			
			@Override
			public void onSelected(int position, final String addFolderPath, final String folderName) {
				if (Setting.INSTANCE.getContentViewMode() != ContentFragment.VIEW_GRID) {
					Setting.INSTANCE.setContentViewMode(getActivity(), ContentFragment.VIEW_GRID);
				}
				setFocus(position);
				showContent(position);
				
				new Handler().postDelayed(new Runnable() {
	                
	                public void run() {
	                	final ContentFragment contentFragment = getContentFragment();
						contentFragment.startSelectionMode(new SelectedItemCallback() {

							@Override
							public void onSelected(ArrayList<MediaObject> selectedItems) {
								if (selectedItems.size() == 0) return;
								File makeFolder = new File(addFolderPath + "/");
								if (makeFolder.mkdirs() == false) {
									Toast.makeText(getActivity(), R.string.folder_creation_failture,
											Toast.LENGTH_SHORT).show();
									return;
								} 					
								
			    				FolderItem item = new FolderItem(folderName);
			    				sFolderItems.add(item);
			    				
								sMenuTask = new MenuTask(getActivity(), getMenuOperationLinstener());
								sMenuTask.setSelectedItems(selectedItems);
								sMenuTask.onItemClicked(R.id.add_new_folder);									
							}
						});
						
						contentFragment.notifyDataSetChanged();
						notifyDataSetChanged();
	                }
	            }, 30);
			}
		});
		
	}

	/**
	 * If a folder is selected, the call
	 *
	 */
	public interface OnSelectedAddFolder {
		/**
		 * If a folder is selected, the call
		 * @param position add the position
		 * @param addFolderPath add the path
		 */
		public abstract void onSelected(int position, final String addFolderPath, final String folderName);
	}
	
	/**
	 * Add the folder.
	 * @param selectedAddFolderListener {@link OnSelectedAddFolder}
	 */
	public void addFolder(final OnSelectedAddFolder selectedAddFolderListener) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.input_text_dlg, null);
        final EditText edit = (EditText)v.findViewById(R.id.etFoldername);
        edit.setText(R.string.new_folder);
        edit.selectAll();

        final SpenDialog dialog = new SpenDialog(getActivity());
        dialog.setContentView(v);
        dialog.setTitle(R.string.folder_name);
        dialog.setmWindowType(SpenDialog.CUSTOM_INPUT_DIALOG);
        dialog.setLeftBtn(R.string.cancel, null);
        dialog.setRightBtn(R.string.ok, new OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = edit.getText().toString().trim();

				File makeFolder = new File(PhotoDeskUtils.getDefualtFodler() + name+ "/");
				if (makeFolder.mkdirs() == false) {
					Toast.makeText(getActivity(), R.string.folder_creation_failture,
							Toast.LENGTH_SHORT).show();
					return;
				}                   

                if (name.length() == 0)
                    return;

                dialog.dismiss();
                makeFolder.delete();
                showFolderListDialog(selectedAddFolderListener, PhotoDeskUtils.getDefualtFodler() + name, name);
            }
        });
        dialog.show();
	}
	
	/**
	 * Shows the list of folders.
	 * @param selectedListener {@link OnSelectedAddFolder}
	 * @param addFolderPath Add the path 
	 * @param folderName Add the name
	 */
	public void showFolderListDialog(final OnSelectedAddFolder selectedListener, final String addFolderPath, final String folderName) {

		SelectedFolderDialog dlg = new SelectedFolderDialog(getActivity());
		dlg.setOnSelectedFolder(new SelectedFolderCallback() {
			
			@Override
			public void onSelectedFolder(int position, FolderItem folderItem) {
              if (selectedListener != null) {
                  selectedListener.onSelected(position, addFolderPath, folderName);
              } 
			}
		});
		dlg.show(addFolderPath);
	}	
	
	/**
	 * Move to the currently selected folder.
	 */
	protected void moveCurrentFolder() {
		moveFolder(mSelectedPostion);
	}

	/**
	 * Thumbnails folder is updated.
	 * 
	 * To empty the thumbnail cache({@link ThumbnailCache}) reload.
	 */
	public void refleshThumbnail() {
		ThumbnailCache.INSTANCE.clearFolder();
		runImageLoaderThread(getActivity().getContentResolver());
		
		int size  = mAdapter.getCount();
		for (int index = 0; index < size; index++) {
			FolderItem folderItem = getAdatper().getItem(index);
			folderItem.clearThumb();
		}
		
		notifyDataSetChanged();
	}

	/**
	 * Support to make sure that the folder view.
	 * 
	 * @param viewType VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
	 * @return Support is true Otherwise false
	 */
	public boolean isSupportViewType(int viewType) {
    	
    	if (viewType == FolderFragment.VIEW_SIMPLE) {
    		return getResources().getBoolean(R.bool.support_simple_folder);
    	} else if (viewType == FolderFragment.VIEW_GRID) {
    		return getResources().getBoolean(R.bool.support_grid_folder);
    	} else if (viewType == FolderFragment.VIEW_LIST) {
    		return getResources().getBoolean(R.bool.support_list_folder);
    	} else {
    		return false;
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

		coverView.setVisibility(enabled ? View.GONE : View.VISIBLE);
		
		View btnAlign = mContainer.findViewById(R.id.btnFodlerAlign);
		if (btnAlign != null) {
			btnAlign.setEnabled(enabled);
		}
		
        View btnAdd = mContainer.findViewById(R.id.btnFolderAdd);
        if (btnAdd != null) {
        	btnAdd.setEnabled(enabled);
		}
        
        super.setEnabled(enabled);
	}

	 /**
     * Change the folder view type.
     * 
     * @param viewType VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
     */
	public void changeFolderView(final int viewType) {
		boolean isImageSelected = false;
		
        int position = 0;
        FolderFragment oldFodlerView = getFolderFragment();
        if (oldFodlerView != null) {
            mBeforeFolderType = oldFodlerView.getViewType();
            position = oldFodlerView.getSelectedPostion();
            oldFodlerView.getView().setVisibility(View.GONE);
            isImageSelected = oldFodlerView.isImageSelectMode();
        }

        Fragment newFolderView = FolderFragment.newInstance(viewType, position);
        ((FolderFragment) newFolderView).setImageSelectMode(isImageSelected);
        
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.folderView, newFolderView).commit();
    }

	/**
	 * Get the previously selected folder view type.
	 * @return VIEW_SIMPLE, VIEW_LIST, VIEW_GRID
	 */
	public int getBeforeViewType() {
		return mBeforeFolderType;
	}

	/**
	 * Change the folder items..
	 * @param folderItems Folder items
	 */
	public static void insertFolderItems(ArrayList<FolderItem> folderItems) {
		sFolderItems = folderItems;
	}

	/**
	 * Check whether the empty folder items.
	 * @return If empty true Otherwise false
	 */
	public static boolean isItemEmpty() {
		return sFolderItems == null;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (requestCode == REQ_SLIDE_SHOW) {
	        leaveSelectionMode();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onTouchPenEraser(View view, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!isEnabled())
				return false;

			final int index = mSpenEvent.getContainingChildIndex((int) event.getX(), (int) event.getY());
			if (index != SpenEventUtil.INVALID_INDEX) {

				final int position = getFirstVisiblePosition() + index;
				if (sFolderItems.get(position).isProtected())
					return false;

				eraserItemClick(position);
				mContainer.invalidate();
				MenuTask menuTask = new MenuTask(getActivity(), getMenuOperationLinstener());
				menuTask.setSelectedItems(getSelectedItems());
				menuTask.onItemClicked(R.id.delete);

			} else { 
				return false;
			}

			return true;
		case MotionEvent.ACTION_MOVE:
			return false;

		case MotionEvent.ACTION_UP:
			break;

		default:
			break;
		}
		return false;
	}
	
	
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isSelectedMode()) {
            if (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER)
                return onTouchPenEraser(v, event);
        }
        return super.onTouch(v, event);
    }
	
	private void makeQuickMenu(QuickMenu menu, FolderItem item) {
		QucikMenuItem deleteItem = new QucikMenuItem(ID_SLIDE, null,
				getResources().getDrawable(R.drawable.menu_slideshow_selector));
		QucikMenuItem shareItem = new QucikMenuItem(ID_GO, null,
				getResources().getDrawable(R.drawable.menu_go_selector));

		menu.addActionItem(deleteItem);
		menu.addActionItem(shareItem);
	}
	
	@Override
	public void onHoverButtonUp(View view, MotionEvent event) {
		if (mSelectionMode) return;
		final int index = mSpenEvent.getContainingChildIndex((int)event.getX(), (int)event.getY());
		if (index == -1) return;
		
		final int firstPosition = getFirstVisiblePosition();
		final FolderItem item = mAdapter.getItem(index + firstPosition);
		
		QuickMenu menu = mSpenEvent.getQuickMenu();
		makeQuickMenu(menu, item);

		mSpenEvent.showQuickMenu(getChildAt(index));
		
		if (!mSelectionMode) {
			setFocus(index + firstPosition);	

			if (isExtend()) {
				mSelectedPostion = index + firstPosition;
			} else {
				showContent(index + firstPosition);
			}
			
		} 
		
		notifyDataSetChanged();
		
		menu.setOnActionItemClickListener(new QuickMenu.OnActionItemClickListener() {

			@Override
			public void onItemClick(QuickMenu source, int pos, int actionId) {
				
				Log.d("ContentFragment", " onItemClick actionId =  " + actionId);
				switch (actionId) {
				case ID_SLIDE:
					if (mSelectionMode == false && isImageSelectMode() == false) {
						leaveSelectionMode();
						getFolderItems().get(index + firstPosition).setSelected(true);
						startSlideShow();
					}
					break;
				case ID_GO:
					PhotoDeskUtils.linkFolder(getActivity(),getFolderItems().get(index + firstPosition) );

					break;
				default:
					break;
				}
			}
		});
	}
	
	@Override
	public void showHoverPopup(final int position, final View v) {
		if (sFolderItems == null || mSpenEvent == null) return;
		FolderItem currentFolder = sFolderItems.get(position);
		long bucketId = currentFolder.getId();

		try {
			ContentResolver cr = getActivity().getContentResolver();
			Cursor imageCursor = MediaLoader.getFolderImageCursor(bucketId, cr);
			Cursor videoCursor = MediaLoader.getFolderVideoCursor(bucketId, cr);

			int imageCount = imageCursor.getCount();
			int videoCount = videoCursor.getCount();

			imageCursor.close();
			videoCursor.close();

			mSpenEvent.showFolderHoverPopup(currentFolder.getDisplayName(), imageCount, videoCount, v);
		} catch (NullPointerException e) {
			Log.d("SimpleFolder", "NullPointerException================== ");
		}
	}
	
	@Override
	public void showDetails() {}
	
	@Override
    public void startSlideShow(){
        Intent intent = new Intent(getActivity(), SlideShowActivity.class);
        intent.putExtra(SlideShowActivity.SLIDE_MODE, SlideShowActivity.SLIDE_FOLDER_VIEW);
        startActivityForResult(intent, REQ_SLIDE_SHOW);
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
    public void setHideMenu(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.hide ) {		
				if (!SCanvasView.isSignatureExist(getActivity())) {
		    		menuItem.setVisible(false);
		    	}  	
        	}
        }
    
    @Override
	public boolean isIncludeProtectedItem(ArrayList<MediaObject> items) {
    	int protectFolderItemCount = ProtectUtil.getInstance().getProtectedItemCount(items);
    	if (/* protectFolderItemCount == items.size() && */protectFolderItemCount != 0) {
    		return true;
    	}

    	return false;
	}
    
    @Override
    public FolderItem getFirstSelectedItem() {
        int itemCnt = mAdapter.getCount();
        for (int index = 0; index < itemCnt; index++) {
        	FolderItem item = mAdapter.getItem(index);
            if (item.isSelected()) {
                return item;
            }
        }
        return null;
    }

    @Override
	public void allUnprotectedItem() {
		ArrayList<FolderItem> items = FolderFragment.getFolderItems();

		for (FolderItem folderItem : items) {
			folderItem.setProtect(false);
		}
	}		
    
    /**
     * Menu run after processing the results of
     */
	OnOperationListener mOperationListener = new OnOperationListener() {

		@Override
		public void onDone(long menuId, FolderItem selectedFolder, ArrayList<MediaObject> selectedItems,
				ArrayList<MediaObject> doneItems, boolean canceled) {
			if (menuId == R.id.delete) {
				removeEmptyFolder(doneItems);
				FolderItem currentFolder = mAdapter.getItem(mSelectedPostion);
				
				for (MediaObject folder : doneItems) {
					if (folder.getId() == currentFolder.getId()) {
						removeContentItems();
					}
					
					if (folder.getType() == MediaObject.FOLDER) {
						update((FolderItem)folder);
					}
				}
                if(FolderFragment.getFolderItems().size() == 0) getActivity().findViewById(R.id.tvEmptyItem).setVisibility(View.VISIBLE);
			} else if (menuId == R.id.merge) {
				removeEmptyFolder(selectedItems);
				update();
		    	ContentFragment contentView = getContentFragment();
		    	if (contentView != null) {
		    		contentView.update();
		    		contentView.notifyDataSetChanged();
		    		contentView.leaveSelectionMode();
		    	}	
		    	showContent(findItemPosition(selectedFolder.getId()));
			} else if (menuId == R.id.folder_rename) {
				for (MediaObject forlder : doneItems) {
					update((FolderItem)forlder);
				}				
				reDrawContentView(getSelectedPostion());
			}else if (menuId == R.id.add_new_folder || menuId == R.id.drag_drop) {
				removeEmptyFolder(doneItems);
				update();
		    	ContentFragment contentView = getContentFragment();
		    	if (contentView != null) {
		    		contentView.update();
		    		contentView.notifyDataSetChanged();
		    		contentView.leaveSelectionMode();
		    	}		
		    	reDrawContentView(getSelectedPostion());
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
		 * Delete content items
		 */
		private void removeContentItems() {
			ContentFragment contentView = getContentFragment();
			if (contentView != null) {
				contentView.removeAll();
			}
		}
	};

	/**
	 * Menu after running Get the listener to handle.
	 */
	public OnOperationListener getMenuOperationLinstener() {
		return mOperationListener;
	}

	/**
	 * Hide Folder
	 */
	protected void hideFolder(){
		if (sFolderItems == null) return;
		mRefreshedIds = new ArrayList<Long>();
		ArrayList<FolderItem> selectedItems = new ArrayList<FolderItem>();

		for (FolderItem item : sFolderItems) {
			if (item.isSelected()) {
				selectedItems.add(item);
			}
		}

		if (HiddenFolderUtil.getInstance().setHideFolders(selectedItems)) {
			int index = 0;
			while (index < sFolderItems.size()){
				if (sFolderItems.get(index).isSelected()){
					sFolderItems.remove(index);
					setRePositionAfterFolderChange(index);
				}else {
					index++;
				}
					
			}	
		}
		leaveSelectionMode();
		reDrawContentView(getSelectedPostion());
	}
	
	/**
	 * Hidden Folder warning window is opened.
	 */
	protected void hideFolderWarningDialog(){

		final SpenDialog dialog = new SpenDialog(getActivity());
		dialog.setContentView(getActivity().getApplicationContext().getString(R.string.hidden_Folder_warning_message) , 0);
		dialog.setTitle(R.string.warning);
		dialog.setRightBtn(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
            	if (getFolderFragment() != null) {
                 	getFolderFragment().hideFolder();
            	}
			}
		});
		dialog.setLeftBtn(R.string.cancel, null);
		dialog.show();		
	}
	
	@Override
	public void hide() {
		hideFolderWarningDialog();
	}
	
	@Override
	public int getActionStatusMode() {
		return ACTION_MODE_NOMAL;
	}
}
