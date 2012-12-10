package com.samsung.photodesk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.samsung.photodesk.CustomMenu.DropDownMenu;
import com.samsung.photodesk.data.HiddenFolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.util.HiddenFolderUtil;
import com.samsung.photodesk.view.GridFolderItemView;

/**
 * <p>Processing the contents of hidden folders </p> 
 * Hidden Folder Item processing after selecting Hidden Folder show menu
 * @param <T> {@link HiddenFolderItem}
 *
 */
public class HiddenFolderActivity extends BaseActivity implements OnItemLongClickListener, OnItemClickListener{

	public static boolean folderPositionChanged = false;
	private static final String IS_SELECTED = "is_selected";
	
	private ArrayList<HiddenFolderItem> mArrHiddenItem;
	private ArrayList<String> selectedPath;
	private GridView mGrid;
	private HiddenFolderAdapter mhiddenFolderAdapter;
	private boolean mSelectionMode = false;
	private ActionMode mActionMode;
	private View mCustomView;
	private DropDownMenu mDropDownMenu;
	private PhotoDeskActionBar mActionBar;
	private final String ACTIVITY_NAME = "Hidden Folder";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hidden_folder_view);
		this.setResult(RESULT_OK);
		Bundle bundle;

		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean(IS_SELECTED, false)) {
				startSelectionMode();
				bundle = savedInstanceState.getBundle("HiddenItemsBundle"); 
				selectedPath = bundle.getStringArrayList("seletctedItems");
				getHiddenFolderItem();
				setSelectedItems();
			}else
				getHiddenFolderItem();
		}else{
			getHiddenFolderItem();
		}

		init();	
		mGrid.setOnItemLongClickListener(this); 
		mGrid.setOnItemClickListener(this);

		if(mSelectionMode == true)
			updateSelectedCountforActionMode();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);	
		mActionBar = new PhotoDeskActionBar(this);
		return mActionBar.createMenu(R.menu.hidden_default_menu, menu);
		
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        return super.onPrepareOptionsMenu(menu);
    }
   
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    	case android.R.id.home: 
    		finish();
    		break;
    	default :
    		break;
    	}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(IS_SELECTED, mSelectionMode);
			
		if(mSelectionMode ==true){
			Bundle bundle = new Bundle();
			setSelectedIndex();
			bundle.putStringArrayList("seletctedItems", selectedPath);	
			outState.putBundle("HiddenItemsBundle", bundle);
		}
	}

	private void init() {
		setTitle(ACTIVITY_NAME);
		mGrid = (GridView) findViewById(R.id.gvHiddenFolder);
		mhiddenFolderAdapter = new HiddenFolderAdapter(this,
				R.layout.hidden_folder_item, mArrHiddenItem);
		mGrid.setAdapter(mhiddenFolderAdapter);
	}

	/**
	 * <P>Title setting of Action Bar<P>
	 * @param folderName       
	 */
	public void setTitle(String folderName) {
		this.getActionBar().setTitle(folderName);
	}
	
	
	/**
	 * <P> Deselect all items<P>   
	 */
	public void setDeSelectAll() {
		for (HiddenFolderItem item : mArrHiddenItem) {
			if (item.getSelected()) {
				item.setSelected(false);
			}
		}
	}
	
	/**
	 * <P>Select all items<P>   
	 */
	private void setSelectAll() {
		for (HiddenFolderItem item : mArrHiddenItem) {
			if (!item.getSelected()) {
				item.setSelected(true);
			}
		}
		updateSelectedCountforActionMode();
	}

	/**
	 * <P>Set to the selected folder<P>
	 * When change the screen position, reset the selected folder. 
	 */
	
	private void setSelectedItems() {
		for(int i=0;i<selectedPath.size();i++){
			for(HiddenFolderItem item : mArrHiddenItem){
				if(selectedPath.get(i).equals(item.getPath()))
					item.setSelected(true);
			}
		}
	}
	
	/**
	 * <P>select mode finish<P>
	 * 
	 */	
	public void leaveSelectionMode() {
		if (mSelectionMode == false)
			return;
		mSelectionMode = false;
		mActionMode.finish();
		mActionMode = null;

		setDeSelectAll();
	}

	/**
	 * <P>Viewer update<P>
	 */
	private void gridViewUpdate() {
		getHiddenFolderItem();
		mhiddenFolderAdapter = new HiddenFolderAdapter(this,
				R.layout.hidden_folder_item, mArrHiddenItem);
		mGrid.setAdapter(mhiddenFolderAdapter);		
	}
	
	private void notifyDataSetChanged(){
		mhiddenFolderAdapter.notifyDataSetChanged();
	}

	/**
	 * <p>To bring up the information of hidden folder<p>
	 */
	private void getHiddenFolderItem() {

		mArrHiddenItem = new ArrayList<HiddenFolderItem>();
		HashMap<String, byte[]> mHidenFolderMap = HiddenFolderUtil.getInstance()
				.getHiddenFolderDBList();
		Set<String> setBuy = mHidenFolderMap.keySet();
		Object[] hmKeys = setBuy.toArray();
		for (int i = 0; i < hmKeys.length; i++) {
			mArrHiddenItem.add(new HiddenFolderItem(i, (String) hmKeys[i],
					HiddenFolderUtil.getInstance().getHiddenFolderThumbnailImage(
							mHidenFolderMap.get((String) hmKeys[i])),
					getFolderName((String) hmKeys[i])));
		}
	}
	
	
	private String getFolderName(String path) {
		String str = path.substring(0, path.lastIndexOf("/"));
		return str.substring(str.lastIndexOf("/") + 1);
	}
	
	/**
	 * <p>Viewer update<p>
	 *  Viewer update after calling dialog.
	 */
	
	HiddenFolderUtil.HideDoneListener mHideDone = new HiddenFolderUtil.HideDoneListener() {	
		@Override
		public void onDone() {
			gridViewUpdate();
		}
	};

	/**
	 * <p>Unhide hidden folder <p>
	 */	
	private void hiddenFolderUnHide() {

		boolean result;
		result = HiddenFolderUtil.getInstance().setUnHideFolders(this,mArrHiddenItem, mHideDone);
		leaveSelectionMode();
		folderPositionChanged = result;

		gridViewUpdate();


	}

	/**
	 * <P>get Selected items count<P>
	 */
	private int getSelectedCount() {
		int count = 0;

		for (HiddenFolderItem item : mArrHiddenItem) {
			if (item.getSelected()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * <P>saves the path of the selected folder item<P>
	 */
	private int setSelectedIndex() {
		int count = 0;
		selectedPath = new ArrayList<String>();

		for (HiddenFolderItem item : mArrHiddenItem) {
			if (item.getSelected()) {
				selectedPath.add(item.getPath());  
			}
		}
		return count;
	}

	/**
	 * <P>selected count update of action bar<P>
	 */
	
	public void updateSelectedCountforActionMode() {
		if (mActionMode == null)
			return;

		int selectCount = getSelectedCount();

		if (selectCount == 0) {
			leaveSelectionMode();
		}
		if (mActionMode != null) {
			mActionMode.invalidate();
		}
		MenuItem menuSelect = mDropDownMenu.findItem(R.id.action_select_all);
		if (menuSelect != null) {
			if (mhiddenFolderAdapter.getCount() == selectCount) {
			    menuSelect.setTitle(getResources().getString(R.string.deselect_all));
			} else {
			    menuSelect.setTitle(getResources().getString(R.string.select_all));
			}
		}				
		mDropDownMenu.setTitle(selectCount + " / " +  mArrHiddenItem.size() + " ");
	}

	/**
	 * <P>Select mode start<P>
	 * Entering selection mode, when you select an item in a folder
	 */
	public void startSelectionMode() {
		if (mSelectionMode == true)
			return;

		mSelectionMode = true;
		mActionMode = startActionMode(new HiddenActionMode());
		CustomMenu customMenu = new CustomMenu(this);
		

		mCustomView = LayoutInflater.from(this).inflate(R.layout.action_bar_seletion, null);
        mDropDownMenu = customMenu.addDropDownMenu((Button) mCustomView.findViewById(R.id.selection_menu), R.menu.selection);
        customMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_select_all:
                        boolean selectFlag = mhiddenFolderAdapter.getCount() == getSelectedCount();
                        if (selectFlag) {
                            setDeSelectAll();
                            leaveSelectionMode();
                        } else {
                            setSelectAll();
                        }
                        break;
                }            
                mhiddenFolderAdapter.notifyDataSetChanged();
				return mSelectionMode;

            }
        }); 			

		mActionMode.setCustomView(mCustomView);
	}

	/**
	 * 
	 * <p>Adapter with a hidden folder information<p>
	 * @param <T> {@link HiddenFolderItem}
	 */
	public class HiddenFolderAdapter extends ArrayAdapter<HiddenFolderItem> {

		private int mResource;
		private LayoutInflater mInflater;
	
		public HiddenFolderAdapter(Context context, int resource,
				List<HiddenFolderItem> objects) {
			super(context, resource, objects);
			mResource = resource;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(mResource, parent, false);
				HiddenViewHolder viewHolder = new HiddenViewHolder(
						(ImageView) convertView.findViewById(R.id.ivFolderImage),
						(ImageView) convertView.findViewById(R.id.iVImageSub1),
						(ImageView) convertView.findViewById(R.id.iVImageSub2),
						(ImageView) convertView.findViewById(R.id.iVImageSub3),
						(TextView) convertView.findViewById(R.id.tVName),
						(ImageView) convertView.findViewById(R.id.iVCheck));
				convertView.setTag(viewHolder);
			}
			HiddenFolderItem hiddenItem = mArrHiddenItem.get(position);
			final HiddenViewHolder viewHolder = (HiddenViewHolder) convertView.getTag();
			viewHolder.ivImage[0].setImageBitmap(hiddenItem.getThumbnail());
			viewHolder.mNameView.setText(hiddenItem.getmFolderName());

			if (hiddenItem.getSelected()) {
				viewHolder.ivCheck.setVisibility(View.VISIBLE);
				for (int index = 0; index < FolderFragment.MAX_THUMBNAIL_CNT; index++) {
				 ((GridFolderItemView)viewHolder.ivImage[index]).setSelected(hiddenItem.getSelected());		
				}
			} else {
				convertView.setActivated(false);
				viewHolder.ivCheck.setVisibility(View.GONE);
				for (int index = 0; index < FolderFragment.MAX_THUMBNAIL_CNT; index++) {
				 ((GridFolderItemView)viewHolder.ivImage[index]).setSelected(hiddenItem.getSelected());		
				}
			}
			viewHolder.mNameView.setTypeface((hiddenItem.getSelected()) ? Typeface.DEFAULT_BOLD: Typeface.DEFAULT);
			
			convertView.setTag(R.id.ivFolderImage, Integer.valueOf(position));
			return convertView;
		}
		
		/**
		 * 
		 * <p>Class for display item of hidden folders <p>
		 *
		 */
		class HiddenViewHolder {

			TextView mNameView;
			ImageView ivCheck;
			ImageView ivImage[] = new ImageView[FolderFragment.MAX_THUMBNAIL_CNT];

			ArrayList<Future<Bitmap>> futureBitmap = new ArrayList<Future<Bitmap>>(
					FolderFragment.MAX_THUMBNAIL_CNT);
			Future<MediaItem[]> futureFolder;

			public HiddenViewHolder(ImageView ivMainImage, ImageView ivSubImage1,
					ImageView ivSubImage2, ImageView ivSubImage3,
					TextView nameView, ImageView vCheck) {

				mNameView = nameView;
				this.ivImage[0] = ivMainImage;
				this.ivImage[1] = ivSubImage1;
				this.ivImage[2] = ivSubImage2;
				this.ivImage[3] = ivSubImage3;
				ivCheck = vCheck;

				int posX = -5;
				int posY = 0;
				int POS_INCREASE = 8;

				for (int index = 0; index < FolderFragment.MAX_THUMBNAIL_CNT; index++) {
					futureBitmap.add(null);
					if (ivImage[index] != null) {
						ivImage[index].setTranslationX(posX);
						ivImage[index].setTranslationY(posY);
					}

					posX += POS_INCREASE;
					posY -= POS_INCREASE;
				}

			}
		}
	}
 
	/**
	 * 
	 *  <p>processing when Edit menu state</p>
	 *	ActionMode(Edit state) of hidden folder activity ({@link HiddenFolderActivity})
	 */
	private final class HiddenActionMode implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = new MenuInflater(HiddenFolderActivity.this);
			inflater.inflate(R.menu.hidden_folder_menu, menu);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.hidden_folder_unhide:
				hiddenFolderUnHide();
				break;

			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			setDeSelectAll();
			notifyDataSetChanged();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		if (mSelectionMode == false)
			return;
		
		HiddenFolderItem item = mArrHiddenItem.get(pos);
		if (mSelectionMode == true) {
			if (item.getSelected()) {
				item.setSelected(false);
				if (getSelectedCount() < 1) {
					leaveSelectionMode();
				}
			} else {
				item.setSelected(true);
			}
			updateSelectedCountforActionMode();
		} else {
			
			if (item.getSelected()) {
				item.setSelected(false);
				if (getSelectedCount() < 1) {
					leaveSelectionMode();
				}
			}
		}
		notifyDataSetChanged();		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos,
			long arg3) {
		HiddenFolderItem item = mArrHiddenItem.get(pos);			
		if (mSelectionMode == false) {	
			startSelectionMode();
			item.setSelected(!item.getSelected());		
			updateSelectedCountforActionMode();
		} else {
			if(item.getSelected()){
				item.setSelected(false);
				if (getSelectedCount() < 1) {
					leaveSelectionMode();
				}	
			}else {
				item.setSelected(true);
			}
			updateSelectedCountforActionMode();
		}
		notifyDataSetChanged();		
		return true;
	}

}
