package com.samsung.photodesk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.samsung.photodesk.MenuTask.DonePrepareListener;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.ImageItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.data.VideoItem;
import com.samsung.photodesk.editor.SAMMDBHelper;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.util.FileControlUtil;
import com.samsung.photodesk.util.PhotoDeskUtils;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.photodesk.view.SelectedFolderDialog;
import com.samsung.photodesk.view.SpenDialog;
import com.samsung.photodesk.view.SelectedFolderDialog.SelectedFolderCallback;
import com.samsung.photodesk.view.SelectedFolderDialog.SelectedNewFolderCallback;
import com.samsung.spensdk.SCanvasView;

/**
 * <p>Managing for file edit</p>
 * FileManager class provides function that Copy, Move, Delete, Merge, Rename, Protect, etc.
 * 
 */
public class FileManager {
	public static final int ROTATION_LEFT = 0;
	public static final int ROTATION_RIGHT = 1;
	
	public static final String TAG = "FileManager";
	
	private SAMMDBHelper mSamDB;
	private Context mContext;
	private int mMenuId;
	private FolderItem mSelectedFolder;
	private ArrayList<MediaObject> mSelectedItems;
	private boolean mCanceled = false;
	
	/**
	 * Constructor
	 * @param context - {@link Context}
	 */
	public FileManager(Context context) {
		mSamDB = new SAMMDBHelper(context);
		mContext = context;
	}
	
	/**
	 * Constructor
	 * @param menuId - menu ID
	 * @param context - {@link Context}
	 * @param selectedItems - selected items
	 * @param selectedFolder - selected folder
	 */
	public FileManager(int menuId, Context context, ArrayList<MediaObject> selectedItems, FolderItem selectedFolder) {
		mSamDB = new SAMMDBHelper(context);
		mContext = context;
		mMenuId = menuId;
		mSelectedItems = selectedItems;
		mSelectedFolder = selectedFolder;
	}
	
	/**
	 * <p>Prepare to function</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	public void prepare(final DonePrepareListener donePrepare) {
		if (mMenuId == R.id.delete) {
			showWarningDialog(getDlgTitle(), getDlgMessage(), mSelectedItems, donePrepare);
		} else if (mMenuId == R.id.copy || mMenuId == R.id.move) {
			showFolderList(donePrepare);
		} else if (mMenuId == R.id.new_folder) {
			makeNewFolder(donePrepare);
		} else if (mMenuId == R.id.add_new_folder) {
			selectAddFolder(donePrepare);
		} else if (mMenuId == R.id.drag_drop) {
			itemMoveToDrag(donePrepare);
		} else if (mMenuId == R.id.rotation_left || mMenuId == R.id.rotation_right) {
			donePrepare.onDonePrepare(mSelectedItems);
		} else if (mMenuId == R.id.protect) {
			donePrepare.onDonePrepare(mSelectedItems);
		} else if (mMenuId == R.id.unprotect) {
			unProtectItem();
		} else if (mMenuId == R.id.rename || mMenuId == R.id.folder_rename) {
			showRenameDialog(donePrepare);
		} else if (mMenuId == R.id.merge) {
			selectFolderInSelectedItem(donePrepare);
		} else if (mMenuId == R.id.action_location_edit) {
			selectItemLocationEdit(donePrepare);
		}
	}

	/**
	 * <p>Prepare to location edit</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void selectItemLocationEdit(DonePrepareListener donePrepare) {
		donePrepare.onDonePrepare(mSelectedItems);
	}

	/**
	 * <p>Unprotect item</p>
	 * Folder , Content item's unprotect
	 */
	public void unProtectItem() {
		for (MediaObject item: mSelectedItems) {
			if(item.getType() == MediaObject.FOLDER) {
				ProtectUtil.getInstance().checkSignatureUnprotect(mContext, PhotoDeskActivity.FOLDER_UNPROTECT);
				break;
			} else {
				ProtectUtil.getInstance().checkSignatureUnprotect(mContext, PhotoDeskActivity.ITEM_UNPROTECT);
				break;
			}
		}
	}
	
	/**
	 * <p>Check current thread cancel state</p>
	 * @return current thread cancel state
	 */
	public boolean isCanceled() {
		return mCanceled;
	}
	
	/**
	 * <p>Set cancel</p>
	 */
	public void cancel() {
		mCanceled = true;
	}
	
	/**
	 * <p>Delete item</p>
	 * Folder , Content item delete
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	public boolean delete(MediaObject item) {
		if (item.getType() == MediaObject.FOLDER) {
			return deleteFolderFile(item);
		} else {
			return deleteContentFile(item);
		}
	}

	/**
	 * <p>Delete folder file</p>
	 * @param item - selected folder item
	 * @return true - success , false - fail
	 */
	private boolean deleteFolderFile(MediaObject item) {
		if (item.getId() == FolderItem.NEW_FOLDER_ID) return false;
		
		ArrayList<MediaItem> mediaItems = MediaLoader.getMediaItems(item.getId(), mContext.getContentResolver());
		for (MediaItem contentItem: mediaItems) {
			if (isCanceled()) break;
			deleteContentFile(contentItem);
		}
		return true;
	}

	/**
	 * <p>Delete Content file</p>
	 * @param item - selected content item
	 * @return true - success , false - fail
	 */
	private boolean deleteContentFile(MediaObject item) {
		if (mSamDB == null || isCanceled()) return false;
		if (SCanvasView.isSAMMFile(item.getPath())) {
			String voicePath = mSamDB.getVoiceData(item.getPath());
			if (voicePath != null)	{
				deleteFile(voicePath);
			}
			mSamDB.removeSAMMInfo(item.getPath());
		}
		
		deleteFile(item.getPath());
		removeImageToDB(item.getId());
		
		return true;
	}

	/**
	 * <p>Copy file</p>
	 * @param item - selected
	 * @return true - success , false - fail
	 */
	public boolean copy(MediaObject item) {
		if (mSamDB == null || isCanceled()) return false;
		String selectedPath = geSelectedPath(item.getDisplayName());
		ArrayList<String> protectPaths = new ArrayList<String>();
				
		if (copyFile(item.getPath(), selectedPath)) {
			if (mSamDB != null && SCanvasView.isSAMMFile(item.getPath())) {
				mSamDB.copySAMMInfo(item.getPath(), selectedPath);
			}
			if (ProtectUtil.getInstance().isProtected(item.getPath())) {
				protectPaths.add(selectedPath);
			}
		
			if (protectPaths.size() > 0) ProtectUtil.getInstance().changeProtectStatus(protectPaths);
			
			addItemToDB(mContext.getContentResolver(), selectedPath, item);
			return true;
		} else {
			Log.e(TAG, "Failed to copy file " + item.getPath());
			deleteFile(selectedPath);
			return false;
		}
	}
	
	/**
	 * <p>Move file</p>
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	public boolean move(MediaObject item) {
		if (mSamDB == null || isCanceled()) return false;
		
		String selectedPath = geSelectedPath(item.getDisplayName());
		File file = new File(selectedPath);
		if (file.exists()) return false;
		
		if (copy(item)) {
			delete(item);
		} else {
			Log.e(TAG, "Failed to move filee " + item.getPath());
		}
		return true;
	}
	
	/**
	 * <p>Create new folder</p>
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	public boolean newFolder(MediaObject item) {
		return move(item);
	}
	
	/**
	 * <p>Add folder</p>
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	public boolean addFolder(MediaObject item) {
		return move(item);
	}	
	
	/**
	 * <p>Rotate image</p>
	 * @param item - selected image
	 * @param rotation - selected rotation (ROTATION_LEFT = 0, ROTATION_RIGHT = 1)
	 * @return true - success , false - fail
	 */
	public boolean rotation(MediaObject item, int rotation) {
		if (item.getType() != MediaObject.IMAGE) return false;
		
		if (rotation == ROTATION_LEFT) {
			return rotationImage((ImageItem)item, -90);
		} else if (rotation == ROTATION_RIGHT) {
			return rotationImage((ImageItem)item, 90);
		} else {
			return false;
		}
	}
	
	/**
	 * <p>Protect item</p>
	 * @param item - selcted item
	 * @return true - success , false - fail
	 */
	public boolean protect(MediaObject item) {
		return protectItem(item);
	}
	
	/**
	 * <p>Rename item</p>
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	public boolean rename(MediaObject item) {
		return renameFile(item);
	}
	
	/**
	 * <p>Merge item</p>
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	public boolean merge(MediaObject item) {
		if (item.getType() == MediaObject.FOLDER) {
			return mergeFolder((FolderItem)item);
		}
		return move(item);
	}
	
	/**
	 * <p>Merge Folder</p>
	 * @param fodlerItem - selected folder item
	 * @return true - success , false - fail
	 */
	private boolean mergeFolder(FolderItem fodlerItem) {
		ArrayList<MediaItem> mediaItems = MediaLoader.getMediaItems(fodlerItem.getId(), mContext.getContentResolver());
		for (MediaItem contentItem: mediaItems) {
			if (isCanceled()) return false;
			move(contentItem);
		}
		
		return true;
	}

	/**
	 * <p>Rename file</p>
	 * Folder, Content item rename.
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	private boolean renameFile(MediaObject item) {
		if (item == null) return false;
		if (item.getType() == MediaObject.FOLDER) {
			renameFolder(item);
		} else {
			renameContent(item);
		}
		return true;
	}
	
	/**
	 * <p>Rename Folder</p>
	 * @param item - selected folder item
	 */
	private void renameFolder(MediaObject item) {
		ArrayList<String> protectPaths = new ArrayList<String>();
		ArrayList<MediaItem> items = new ArrayList<MediaItem>();
			
		ArrayList<MediaItem> imageItems = MediaLoader.getMediaItems(item.getId(), mContext.getContentResolver());
		items.addAll(items.size(), imageItems);
		
		int size = items.size();

		for (int index = 0 ; index < size ; index++) {
			if (SCanvasView.isSAMMFile(items.get(index).getPath())) {
				mSamDB.moveSAMMInfo(items.get(index).getPath(), mSelectedFolder.getPath() + "/" + items.get(index).getDisplayName());
			}
			
			if (ProtectUtil.getInstance().isProtected(items.get(index).getPath())) {
				protectPaths.add(mSelectedFolder.getPath() + "/"  + items.get(index).getDisplayName());
			}
		}
		
		if (protectPaths.size() > 0) ProtectUtil.getInstance().changeProtectStatus(protectPaths);
	
		deleteFolderToDB(item.getId(), MediaItem.IMAGE);
	}
	
	/**
	 * <p>Rename content</p>
	 * @param item - selected content item
	 */
	private void renameContent(MediaObject item) {
		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + mSelectedFolder.getFilePath())));
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}			
		removeImageToDB(item.getId());
	
		if (SCanvasView.isSAMMFile(item.getPath())) {
			mSamDB.moveSAMMInfo(item.getPath(), mSelectedFolder.getFilePath());
		}
	}

	/**
	 * <p>Get path</p>
	 * @param DisplayName - item's display name
	 * @return path
	 */
	private String geSelectedPath(String DisplayName) {
		return mSelectedFolder.getPath()+DisplayName;
	}
	
	/**
	 * <p>Add item to ContentResolver</p>
	 * @param cr - {@link ContentResolver}
	 * @param path - item's path
	 * @param item - item
	 */
	private void addItemToDB(ContentResolver cr, String path, MediaObject item){	    
        ContentValues values = new ContentValues();


        if (item.getType() == MediaItem.IMAGE) {
        	values.put(Images.Media.DATA, path);
        	
        	ImageItem image = (ImageItem)item;
        	values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, image.getDisplayName());
            values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, image.getDateTaken());
            values.put(MediaStore.Images.ImageColumns.LATITUDE, image.getLatitude());
            values.put(MediaStore.Images.ImageColumns.LONGITUDE, image.getLongitude());
            values.put(MediaStore.Images.ImageColumns.TITLE, image.getTitle());
            values.put(MediaStore.Images.ImageColumns.MIME_TYPE, image.getMimeType());
            values.put(MediaStore.Images.ImageColumns.ORIENTATION, image.getOrientation());
            cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
                     
        } else if (item.getType() == MediaItem.VIDEO) {
        	values.put(Video.VideoColumns.DATA, path);
        	
        	VideoItem video = (VideoItem)item;
        	values.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, video.getDisplayName());
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, video.getDateTaken());
            values.put(MediaStore.Video.VideoColumns.LATITUDE, video.getLatitude());
            values.put(MediaStore.Video.VideoColumns.LONGITUDE, video.getLongitude());
            values.put(MediaStore.Video.VideoColumns.TITLE, video.getTitle());
            values.put(MediaStore.Video.VideoColumns.MIME_TYPE, video.getMimeType());
            values.put(MediaStore.Video.VideoColumns.DURATION, video.getDuration());
            values.put(MediaStore.Video.VideoColumns.RESOLUTION, video.getResolution());
            cr.insert(Video.Media.EXTERNAL_CONTENT_URI, values);     	
        }

    }
	
	/**
	 * <p>Delete file</p>
	 * @param String - item's path
	 */
	private boolean deleteFile(String path){
		File file  = new File(path);
        if (file.exists()) {
        	if (file.delete()) {
        		return true;
        	} else {
        		Log.e(TAG, "Fail delete file");
        		return false;
        	}
        } else {
        	Log.e(TAG, "Delete file that does not exist : " + path);
        	return false;
        }
	}
	
	/**
	 * <p>Copy file</p>
	 * @param in - origin path
	 * @param out - destination path
	 * @return true - success , false - fail
	 */
	private boolean copyFile(String in, String out) {
		boolean result;
		File inFile = new File(in);
		File saveFile = new File(out);
    	if (saveFile.exists()) {
    		Log.e(TAG, "Duplicate file copy : " + out);
    		return false;
    	}
    	
        if (inFile.exists()) {
        	FileInputStream fis = null;
            FileOutputStream fos = null;
            
            try {
                fis = new FileInputStream(inFile);
                fos = new FileOutputStream(saveFile);
                
                int readcount = 0;
                byte[] buffer = new byte[1024];
                while ((readcount = fis.read(buffer, 0, 1024)) !=  -1) {
                	fos.write(buffer, 0, readcount);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception Copy file " + in + ", " + out);
                result = false;
            } finally {
            	try {
            		if (fos != null) {
            			fos.close();
            		}
            		if (fis != null) {
            			fis.close();
            		}
            	} catch (Exception e) {}
            }
            result = true;
        } else {
        	Log.e(TAG, "Copy file that does not exist : " + in);
            result = false;
        }
        
        return result;
	}

	/**
	 * <p>Delete item in ContentResolver</p>
	 * @param id - deleted item's id
	 */
	private void removeImageToDB(long id){
	    if (id == -1) return;
	    Uri uri;
	    uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
	    mContext.getContentResolver().delete(uri, null, null);
    }
	
	/**
	 * <p>Delete folder item in ContentResolver</p>
	 * @param id - deleted folder item's id
	 */
	protected void deleteFolderToDB(long id, int type){	    
		ContentResolver cr = mContext.getContentResolver();
		String whereValue[] = {String.valueOf(id)};

        if(type == MediaItem.IMAGE) {
        	String where = MediaStore.Images.ImageColumns.BUCKET_ID + "=?";
        	cr.delete(Images.Media.EXTERNAL_CONTENT_URI, where, whereValue);
            deleteFolderToDB(id, MediaItem.VIDEO);
            
        } else {
        	String where = MediaStore.Video.VideoColumns.BUCKET_ID + "=?";
        	cr.delete(Video.Media.EXTERNAL_CONTENT_URI, where, whereValue);
        }
    }		
	
	/**
	 * <p>Show warning dialog</p>
	 * @param title - dialog title
	 * @param message - dialog message
	 * @param operatioinItems - operation items
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void showWarningDialog(String title, String message,  
			final ArrayList<MediaObject> operatioinItems, final DonePrepareListener donePrepare) {
		
        final SpenDialog dialog = getWarningDialog(title, message);
        dialog.setRightBtn(R.string.yes, new OnClickListener() {
            
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				donePrepare.onDonePrepare(operatioinItems);
			}
        });
        dialog.show();
	}
	
	/**
	 * <p>Show warning dialog</p>
	 * @param title - dialog title
	 * @param message - dialog message
	 * @param overlapCount - overlap item count
	 * @param operatioinItems - operation items
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void showWarningDialog(String title, String message, final int overlapCount,  
			final ArrayList<MediaObject> operatioinItems, final DonePrepareListener donePrepare) {
		
        final SpenDialog dialog = getWarningDialog(title, message);
        dialog.setRightBtn(R.string.yes, new OnClickListener() {
            
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				donePrepare.onDonePrepare(operatioinItems);
				toastOverlap(overlapCount);
			}
        });
        dialog.show();
	}
	
	/**
	 * <p>Get warning dialog</p>
	 * @param title - dialog title
	 * @param message - dialog message
	 * @return warning dialog
	 */
	public SpenDialog getWarningDialog(String title, String message) {
		final SpenDialog dialog = new SpenDialog(mContext);
        dialog.setTitle(title);
        dialog.setLeftBtn(R.string.no, null);
        dialog.setContentView(message, mContext.getResources().getDimension(R.dimen.base_text_size));
        
        return dialog;
	}
	
	/**
	 * <p>Remove duplicate item</p>
	 * @param selectedFolder - selected folder
	 * @param selectedItems - selected items
	 * @return overlab item count
	 */
	private int removeDuplicate(FolderItem selectedFolder, ArrayList<MediaObject> selectedItems) {
		int overLapCount = 0;

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		ArrayList<MediaItem> items = MediaLoader.getMediaItems(selectedFolder.getId(), mContext.getContentResolver());
		
		for (MediaItem item : items) {
			map.put(item.getDisplayName(), 0);
		}
		
		for (int i = 0; i < selectedItems.size(); i++) {
			if (map.containsKey(selectedItems.get(i).getDisplayName())) {
				overLapCount++;
				selectedItems.remove(i--);
			}
		}
	
		return overLapCount;
	}

	/**
	 * <p>Get dialog message</p>
	 * @return dialog message
	 */
	private String getDlgMessage() {
		
		Resources resource = mContext.getResources();
		switch (mMenuId) {
		case R.id.delete:
			if (mSelectedItems.get(0).getType() == MediaObject.FOLDER) {
	            if (mSelectedItems.size() != 1) {
	                return resource.getQuantityString(R.plurals.delete_selected_folders,mSelectedItems.size());
	                
	            } else {
	            	return resource.getString(R.string.folder_name)+ " : "+ mSelectedItems.get(0).getDisplayName()+" \n" +resource.getQuantityString(R.plurals.delete_selected_folders,mSelectedItems.size());
	            }
	        } else if (mSelectedItems.size() != 1) {
	        	return resource.getQuantityString(R.plurals.delete_selection,
	        			mSelectedItems.size())
	                    + "("
	                    + mSelectedItems.size()
	                    + resource.getQuantityString(R.plurals.delete_selection_items, mSelectedItems.size()) + ")";

	        } else{
	            String fileName = mSelectedItems.get(0).getDisplayName();
	            if (!fileName.equals("")) fileName = "'" + fileName + "' \n";
	        	return fileName + resource.getQuantityString(R.plurals.delete_selection, mSelectedItems.size());
	        }
		case R.id.copy:
			return resource.getString(R.string.copy_qustion);
			
		case R.id.move:
		case R.id.new_folder:
		case R.id.add_new_folder:
		case R.id.drag_drop:
			return resource.getString(R.string.move_qustion);
			
		case R.id.rename:
		case R.id.folder_rename:
			return resource.getString(R.string.please_enter_a_name);
			
		case R.id.merge:
			return resource.getString(R.string.warring_merge_msg);
			
		default:
			break;
		}
		return null;
	}
	
	/**
	 * <p>Get dialog title</p>
	 * @return dialog title
	 */
	private String getDlgTitle() {
		Resources resource = mContext.getResources();
		switch (mMenuId) {
		case R.id.delete:
			if (mSelectedItems.get(0).getType() == MediaObject.FOLDER) {
				if (mSelectedItems.size() != 1) {
					return resource.getString(R.string.folder_delete);
				} else {
					return resource.getString(R.string.folder_delete);
				}
			} else if (mSelectedItems.size() != 1) {
				return resource.getString(R.string.delete);
			} else {
				return resource.getString(R.string.delete);
			}
		case R.id.copy:
			return resource.getString(R.string.copy)+" : " +  mSelectedFolder.getDisplayName();
			
		case R.id.move:
		case R.id.new_folder:
		case R.id.add_new_folder:
		case R.id.drag_drop:
			return resource.getString(R.string.move)+" : " +  mSelectedFolder.getDisplayName();
			
		case R.id.merge:
			return resource.getString(R.string.warring_merge_title);

		default:
			break;
		}
		return null;
	}

	/**
	 * <p>Show folder list</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	public void showFolderList(final DonePrepareListener donePrepare) {
		SelectedFolderDialog dlg = new SelectedFolderDialog(mContext);
		dlg.setOnSelectedFolder(new SelectedFolderCallback() {
			
			@Override
			public void onSelectedFolder(int position, FolderItem folderItem) {
              mSelectedFolder = folderItem;
				
				@SuppressWarnings("unchecked")
				ArrayList<MediaObject> itemClone = (ArrayList<MediaObject>)mSelectedItems.clone();
				
				int overlapCount = removeDuplicate(mSelectedFolder, mSelectedItems);
				if (mSelectedItems.size() == 0) {
					ImageProgressDialog dialog = new ImageProgressDialog(mContext, itemClone);
					dialog.showDuration(2000);
					return;
				} 
				showWarningDialog(getDlgTitle(), getDlgMessage(), overlapCount, mSelectedItems, donePrepare); 
			}
		});
		
		dlg.setOnSelectedNewFolder(new SelectedNewFolderCallback() {

			@Override
			public void onSelectedNewFolder() {
				makeNewFolder(donePrepare);
			}
			
		});
		
		
		dlg.show();
	}
	
	/**
	 * <p>Move item using Drag&Drop</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void itemMoveToDrag(final DonePrepareListener donePrepare) {
		@SuppressWarnings("unchecked")
		ArrayList<MediaObject> itemClone = (ArrayList<MediaObject>)mSelectedItems.clone();
		int overlapCount = removeDuplicate(mSelectedFolder, mSelectedItems);
		
		if (mSelectedItems.size() == 0) {			
			ImageProgressDialog dialog = new ImageProgressDialog(mContext, itemClone);
			dialog.showDuration(2000);
			return;
		}
		donePrepare.onDonePrepare(mSelectedItems);
		toastOverlap(overlapCount);
	}	
	
	/**
	 * <p>Add folder</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void selectAddFolder(final DonePrepareListener donePrepare) {
		@SuppressWarnings("unchecked")
		ArrayList<MediaObject> itemClone = (ArrayList<MediaObject>)mSelectedItems.clone();
		mSelectedFolder = getFolderItem(FolderFragment.getFolderItems().size() - 1);
		int overlapCount = removeDuplicate(mSelectedFolder, mSelectedItems);
		if (mSelectedItems.size() == 0) {			
			ImageProgressDialog dialog = new ImageProgressDialog(mContext, itemClone);
			dialog.showDuration(2000);
			return;
		}
		donePrepare.onDonePrepare(mSelectedItems);
		toastOverlap(overlapCount);
	}	
	
	/**
	 * <p>Show folder list for merge</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void selectFolderInSelectedItem(final DonePrepareListener donePrepare) {
		ArrayList<FolderItem> selectedFolderitems = new ArrayList<FolderItem>();
		for (MediaObject item: mSelectedItems) {
			selectedFolderitems.add((FolderItem)item);
		}		
		SelectedFolderDialog dlg = new SelectedFolderDialog(mContext);
		dlg.setOnSelectedFolder(new SelectedFolderCallback() {
			
			@Override
			public void onSelectedFolder(int position, FolderItem folderItem) {
				mSelectedFolder = folderItem;
				
				ArrayList<MediaObject> operatioinItems = new ArrayList<MediaObject>();
				for (MediaObject folder: mSelectedItems) {
					if (folder.getType() != MediaObject.FOLDER) continue;
					ArrayList<MediaItem> mediaItems = MediaLoader.getMediaItems(folder.getId(), mContext.getContentResolver());
					for (MediaItem contentItem: mediaItems) {
						operatioinItems.add(contentItem);
					}
				}
				
				removeDuplicate(mSelectedFolder, operatioinItems);
				
				if (operatioinItems.size() == 0) return;
				showWarningDialog(getDlgTitle(), getDlgMessage(), operatioinItems, donePrepare);
			}
		});
		
		dlg.setOnSelectedNewFolder(new SelectedNewFolderCallback() {

			@Override
			public void onSelectedNewFolder() {
				makeNewFolder(donePrepare);
			}
			
		});
		dlg.show(selectedFolderitems);
	}

	/**
	 * <p>Show toast for overlap image count</p>
	 * @param count - overlap image count
	 */
	protected void toastOverlap(int count) {
		if (count < 1)	return ;
		
		StringBuilder sb = new StringBuilder();
		
		if (count == 1) {
			sb.append(mContext.getString(R.string.warning_one_overlap))
			.append(String.format(mContext.getString(R.string.warning_overlap_image_count), count));
		} else {
			sb.append(mContext.getString(R.string.warning_many_overlap))
			.append(String.format(mContext.getString(R.string.warning_overlap_image_count), count));
		}
		
		Toast toast = Toast.makeText(mContext, null, Toast.LENGTH_LONG);
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout l = (LinearLayout)inflater.inflate(R.layout.large_toast, null);
		l.findViewById(R.id.warrning_tv);

		TextView tv = (TextView)l.findViewById(R.id.warrning_tv);
		tv.setText(sb.toString());
		
		toast.setView(l);
		toast.show();

	}
	
	/**
	 * <p>Show dialog for create new folder</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void makeNewFolder(final DonePrepareListener donePrepare) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.input_text_dlg, null);
		final EditText edit = (EditText) v.findViewById(R.id.etFoldername);
		final SpenDialog dialog = new SpenDialog(mContext);
		
        edit.setText(R.string.new_folder);
        edit.selectAll();
        
		dialog.setContentView(v);
		dialog.setTitle(R.string.folder_name);
		dialog.setmWindowType(SpenDialog.CUSTOM_INPUT_DIALOG);
		dialog.setLeftBtn(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setRightBtn(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = edit.getText().toString().trim();
				if (name.length() == 0)
					return;

				if (FileControlUtil.isContainSpecialStr(name)) {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.warring_special_str),
							Toast.LENGTH_LONG).show();
					return;
				}

				File makeFolder = new File(PhotoDeskUtils.getDefualtFodler() + name+ "/");
				if (makeFolder.mkdirs() == false) {
					Toast.makeText(mContext, R.string.folder_creation_failture,
							Toast.LENGTH_SHORT).show();
					return;
				}
				dialog.dismiss();
				
				mSelectedFolder = new FolderItem(name);

				ArrayList<MediaObject> operatioinItems = new ArrayList<MediaObject>();
				for (MediaObject folder: mSelectedItems) {
					if (folder.getType() != MediaObject.FOLDER) continue;
					ArrayList<MediaItem> mediaItems = MediaLoader.getMediaItems(folder.getId(), mContext.getContentResolver());
					for (MediaItem contentItem: mediaItems) {
						operatioinItems.add(contentItem);
					}
				}
				
				if (operatioinItems.size() == 0) { 		
					showWarningDialog(getDlgTitle(), getDlgMessage(), mSelectedItems, donePrepare);
				} else {
					showWarningDialog(getDlgTitle(), getDlgMessage(), operatioinItems, donePrepare);
				}
			}
		});
		dialog.show();
	}
	
	/**
	 * <p>Get selected folder</p>
	 * @param position - selected folder position
	 * @return
	 */
	private FolderItem getFolderItem(int position) {
		return FolderFragment.getFolderItems().get(position);
	}

	/**
	 * <p>Rotate image</p>
	 * @param item - selected item
	 * @param degrees - selected rotate degrees
	 * @return true - success , false - fail
	 */
	private boolean rotationImage(ImageItem item, int degrees) {

		Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
		ContentValues values = new ContentValues();

		try {
			ExifInterface exif = new ExifInterface(item.getPath());
			Log.d("SPenGallery", "TAG_ORIENTATION: "+ (exif.getAttribute(ExifInterface.TAG_ORIENTATION)));
			Log.d("SPenGallery", "TAG_ORIENTATION: " + Integer.valueOf(exif.getAttribute(ExifInterface.TAG_ORIENTATION)));
			Log.d("SPenGallery", "TAG_ORIENTATION degrees : " + degrees);
			int rotation = (getCurrentExifOrientation(exif.getAttribute(ExifInterface.TAG_ORIENTATION)) + degrees) % 360;
			if (rotation < 0) {
				rotation += 360;
			}
				
			exif.setAttribute(ExifInterface.TAG_ORIENTATION, getExifOrientation(rotation));
			exif.saveAttributes();

			long fileSize = new File(item.getPath()).length();
			values.put(Images.Media.SIZE, fileSize);

			values.put(Images.Media.ORIENTATION, rotation);
			if (0 != mContext.getContentResolver().update(
					baseUri, values, "_id=?", new String[] { String.valueOf(item.getId()) })) {
				item.setRotation(rotation);
			}

		} catch (IOException e) {
			Log.d("SPenGallery", "cannot set exif data: " + item.getPath());
			return false;
		}
		
		item.rotateThumb();
		
		return true;
	}
	
	/**
	 * <p>Get current orientation</p>
	 * @param orientation
	 * @return current orientation
	 */
	public static int getCurrentExifOrientation(String orientation) {

        Log.d("SPenGallery", "getExifOrientation: orientation " + orientation);
        switch (Integer.valueOf(orientation)) {
            case 0: // ORIENTATION_UNDEFINED
                return 0;
            case 1: // ORIENTATION_NORMAL
                return 0;
            case 2: // ORIENTATION_FLIP_HORIZONTAL
                return 0;
            case 3: // ORIENTATION_ROTATE_180
                return 180;
            case 4: // ORIENTATION_FLIP_VERTICAL
                return 0;
            case 5: // ORIENTATION_TRANSPOSE
                return 0;
            case 6: // ORIENTATION_ROTATE_90
                return 90;
            case 7: // ORIENTATION_TRANSVERSE
                return 0;
            case 8: // ORIENTATION_ROTATE_270
                return 270;
            default:
                throw new AssertionError("invalid: " + orientation);
        }
    }
	
	/**
	 * <p>Get Exif orientation
	 * @param orientation
	 * @return Exif orientation string
	 */
	public static String getExifOrientation(int orientation) {

        Log.d("SPenGallery", "getExifOrientation: orientation " + orientation);
        switch (orientation) {
            case 0:
                return String.valueOf(ExifInterface.ORIENTATION_NORMAL);
            case 90:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_90);
            case 180:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_180);
            case 270:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_270);
            default:
                return String.valueOf(ExifInterface.ORIENTATION_ROTATE_270);
        }
    }

	/**
	 * <p>Get selected folder</p>
	 * @return selected folder
	 */
	public FolderItem getSelectedFolder() {
		return mSelectedFolder;
	}

	/**
	 * <p>Protect item</p>
	 * @param item - selected item
	 * @return true - success , false - fail
	 */
	private boolean protectItem(MediaObject item) {
		boolean ret = ProtectUtil.getInstance().protect(item);
		if (ret) {
			item.setProtected(!item.isProtected());
		}
		return ret;
	}

	/**
	 * <p>Show dialog for rename</p>
	 * @param donePrepare - {@link DonePrepareListener}
	 */
	private void showRenameDialog(final DonePrepareListener donePrepare) {
		final MediaObject firstItem = mSelectedItems.get(0);
		
		View v = LayoutInflater.from(mContext).inflate(R.layout.input_text_dlg, null);
		final EditText edit = (EditText) v.findViewById(R.id.etFoldername);
		
		final String fileName = (firstItem.getType() != MediaObject.FOLDER) 
				? getFileName(firstItem.getDisplayName(), getExtension(firstItem.getPath())) 
				: firstItem.getDisplayName(); 
		
		edit.setText(fileName);
		edit.selectAll();
		
		final SpenDialog dialog = new SpenDialog(mContext);
		dialog.setContentView(v);
		dialog.setTitle(R.string.please_enter_a_name);
		dialog.setmWindowType(SpenDialog.CUSTOM_INPUT_DIALOG);
		dialog.setLeftBtn(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.setRightBtn(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				String newName = edit.getText().toString().trim();
				String oldName = firstItem.getPath();
				if (newName.length() == 0)
					return;

				if (FileControlUtil.isContainSpecialStr(newName)) {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(R.string.warring_special_str), 
							Toast.LENGTH_LONG).show();
					return;
				}

				if (firstItem.getType() != MediaObject.FOLDER) {
					newName += getExtension(firstItem.getPath());
				}
				
				File oldFile = new File(oldName);
				File newFile = new File(oldName.replace(firstItem.getDisplayName(), newName));

				if (oldFile != null && oldFile.exists()) {
					if (!newFile.exists()) {
						oldFile.renameTo(newFile);

						mSelectedFolder = new FolderItem(fileName);
						mSelectedFolder.setPath(newFile.getPath());

						if (firstItem.getType() != MediaObject.FOLDER) {
							donePrepare.onDonePrepare(mSelectedItems);
						} else {
							donePrepare.onDonePrepare(mSelectedItems);
						}
					} else {
						Toast.makeText(mContext, R.string.the_same_folder_already_exist, Toast.LENGTH_SHORT).show();
						return;
					}
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	/**
     * Photos and movies on the latitude, longitude registered.
     * 
     * @param object (Photos and movies)
     * @param pnt (latitude, longitude infomation)
     * 
     * @return true - success , false - fail
     */
	public boolean locationEdit(MediaObject object, GeoPoint pnt) {
		MediaItem item = (MediaItem)object;
		double latitudeE6,longitudeE6;
    	long fileSize;
		final Uri baseUriImage = Images.Media.EXTERNAL_CONTENT_URI;
		final Uri baseUriVideo = Video.Media.EXTERNAL_CONTENT_URI;
		final GeoPoint mp = pnt;
		final ContentValues values = new ContentValues();
    	
		  try {
			    ExifInterface exif = new ExifInterface(item.getPath());			            		
	      		latitudeE6	 = mp.getLatitudeE6()/1E6;           		
	      		longitudeE6  = mp.getLongitudeE6()/1E6;            		
      		
              exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, Double.toString(latitudeE6));                
              exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, Double.toString(longitudeE6)); 
              exif.saveAttributes();
              
              fileSize = new File(item.getPath()).length(); 
              if (item.getType() == MediaItem.IMAGE) {
                  values.put(Images.ImageColumns.SIZE, fileSize);
                  item.setLocation(latitudeE6, longitudeE6);				                     
                  values.put(Images.ImageColumns.LATITUDE , Double.toString(latitudeE6));
                  values.put(Images.ImageColumns.LONGITUDE , Double.toString(longitudeE6));              	
			 }else{
					
				  values.put(Video.VideoColumns.SIZE, fileSize);
                  item.setLocation(latitudeE6, longitudeE6);				                     
                  values.put(Video.VideoColumns.LATITUDE , Double.toString(latitudeE6));
                  values.put(Video.VideoColumns.LONGITUDE , Double.toString(longitudeE6));					
				}              
              if (0 != mContext.getContentResolver().update(baseUriImage, values, "_id=?",
                      new String[]{String.valueOf(item.getId())})) {			                    	
              	((ImageItem)item).setLatitude(latitudeE6);
              	((ImageItem)item).setLongitude(longitudeE6); 	                    	
              }	
              else if(0 != mContext.getContentResolver().update(baseUriVideo, values, "_id=?",
                      new String[]{String.valueOf(item.getId())})){			                    	
              	((VideoItem)item).setLatitude(latitudeE6);
              	((VideoItem)item).setLongitude(longitudeE6); 
              }
		  } catch (IOException e) {
              Log.d("PhotoDesk", "cannot set exif data: " + item.getPath());
              return false;
          }     	
		return true;
	}	
	
	/**
	 * <p>Get file extension</p>
	 * 
	 */
	 public static String getExtension(String fileStr){
		  return fileStr.substring(fileStr.lastIndexOf("."),fileStr.length());
	 }
	 
	/**
	 * <p>Get file name</p>
	 * 
	 */
	public static String getFileName(String fileStr, String extention) {
		return fileStr.replace(extention, "");
	}
}
