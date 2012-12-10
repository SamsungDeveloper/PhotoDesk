package com.samsung.photodesk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.samsung.photodesk.R;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.HiddenFolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.view.SpenDialog;

/**
 * <p>Managing utility for hidden folder </p>
 *
 */
public class HiddenFolderUtil {
	
	public static String TAG = "HiddenFolderUtil";
	private static Context mCtx;
	
	String hiddenListFileName = "hiddenFolderList";
	
	private HashMap<String, byte[]> mHiddenFolderMap = new HashMap<String, byte[]>();

	private HiddenFolderDBHelper mHelper;
		
    private ArrayList<FolderItem> mSelectedItems;
    
    private HiddenFolderItem mDeleteItem;
    
    HideDoneListener mLisnter;
    	
	 	
	public static HiddenFolderUtil getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
	 *  <p>Signature check routin<p>
	 */
   public void checkSignature(Activity activity , int resultCode) {
	   	if (activity == null) return;
	   	PhotoDeskUtils.signatureVerification(activity, resultCode);	       
   }
   
    /**
     * <p>Init <p>
     * DB open 
     * HashMap set
     * Map Data check
     */
	private void initializeHiddenFolderStatus() {
		if (mCtx == null) return;
        mHelper = new HiddenFolderDBHelper(mCtx);
        mHelper.open();
        mHiddenFolderMap = mHelper.getHiddenFolderDataHashMap();

        mHelper.close();
	}  	
	/**         
	  * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	  * or the first access to SingletonHolder.INSTANCE, not before.         
	  **/        
	private static class SingletonHolder {
		public static final HiddenFolderUtil instance = new HiddenFolderUtil();         
	} 
	
	/**
	 *  can not create an object from outside.
	 */
	private HiddenFolderUtil () {}

	
	public void initialize(Context context) {
		mCtx = context;		
		initializeHiddenFolderStatus();
	}	
	
	/**
	 * <p>Hides the folders of the selected item<p>
	 * @param selectedItems {@link ArrayList}
	 */	
	public boolean setHideFolders(ArrayList<FolderItem> selectedItems){
		
		boolean result = false;
		int count =0;
		mSelectedItems = selectedItems;	
		
		for (FolderItem forlder : mSelectedItems) {
			result = setHideFolder(forlder);
            if (result) {
            	count++;
            	Toast.makeText(mCtx, forlder.getDisplayName()+ " " + mCtx.getResources().getString(R.string.folder_hide_success), Toast.LENGTH_SHORT).show();             	 
            }else{
            	Toast.makeText(mCtx, forlder.getDisplayName()+" "+ mCtx.getResources().getString(R.string.folder_hide_fail), Toast.LENGTH_SHORT).show(); 
            }	   						
		}	
		getHiddenFolderDBList(); //update DB list
		if(count > 0){
			updateMediaScan();
			return true;
		}
		return false;
	}	

	/**
	 * <p>One folder hide<p>
	 * @param selectedFolder{@link FolderItem}
	 */		
	public boolean setHideFolder(FolderItem selectedFolder){
		
		boolean result = false;
		String oldName = selectedFolder.getPath();
		String newName = "." + selectedFolder.getDisplayName();
		if(oldName.equals(Environment.getExternalStorageDirectory().toString() + "/PhotoDesk/")){
			Toast.makeText(mCtx, R.string.cannot_hide_root_path, Toast.LENGTH_SHORT).show();   
			return false;
		}
        File oldFile = new File(oldName);
        File newFile = new File(oldName.replace(selectedFolder.getDisplayName(), newName));
        
        ArrayList<MediaItem> items = MediaLoader.getMediaItems(
        		selectedFolder.getId(), mCtx.getContentResolver());
        
        Bitmap bm = ThumbnailCache.INSTANCE.getFolderBitmap(items.get(0).getId());
                		
		if(mHelper == null){
			mHelper = new HiddenFolderDBHelper(mCtx);
		}
        mHelper.open();	
        byte[] thumbnailImage = changeToByteFromBitmap(bm);
        if(thumbnailImage != null){
        	long rowID = mHelper.insertHiddenFolderData(oldName,thumbnailImage );
        	if(rowID != 0){
        		result = oldFile.renameTo(newFile);
        		if(result == false){ //DB list remove if Folder Hide fail.
        			mHelper.deleteHiddenFolderData(oldName, thumbnailImage); 
        		}
        	}else{
        		Toast.makeText(mCtx, mCtx.getResources().getString(R.string.cannot_insert_to_db), Toast.LENGTH_SHORT).show();
        	}
        }else{
        	Toast.makeText(mCtx, mCtx.getResources().getString(R.string.cannot_make_to_thumbnail), Toast.LENGTH_SHORT).show();
        }	        	
		mHelper.close();
		
        return result;
	}
	
	/**
	 * <p>DB list remove dialog<p>
	 * @param context context of HiddenFolderActivity 
	 * @param item remove item from DB {@link HiddenFolderItem }
	 * @param doneListner callback listener(for screen update) 
	 */
	public void showDeleteDBDialog(Context context, HiddenFolderItem item, final HideDoneListener doneListner){	
		
		mDeleteItem = item;
		final SpenDialog dialog = new SpenDialog(context);
		dialog.setContentView(item.getPath() + mCtx.getString(R.string.hidden_Folder_unhide_fail) , 0);
		dialog.setTitle(R.string.delete);
		dialog.setRightBtn(R.string.delete, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if(mHelper == null){
					mHelper = new HiddenFolderDBHelper(mCtx);
				}
		        mHelper.open();
				mHelper.deleteHiddenFolderData(mDeleteItem.getPath(), changeToByteFromBitmap(mDeleteItem.getThumbnail()));
				mHelper.close();
				
				if (doneListner != null) {
					doneListner.onDone();
				}				
				Toast.makeText(mCtx, mCtx.getResources().getString(R.string.delete_complete), Toast.LENGTH_SHORT).show();
			}
		});
		dialog.setLeftBtn(R.string.cancel, null);
		dialog.show();
	}
	
	/**
	 * <p>Unhide the folders of the selected item <p>
	 * @param context  context of HiddenFolderActivity
	 * @param ArrHiddenItem Info of selected Items
	 * @param doneListner callback method{@link HideDoneListener}
	 */	
	public boolean setUnHideFolders(Context context, ArrayList<HiddenFolderItem> ArrHiddenItem, final HideDoneListener doneListner){
		
		boolean result = false;
		int count =0;
		for(HiddenFolderItem item : ArrHiddenItem){
			if(item.getSelected()){
				result = setUnHideFolder(item);
				if(!result){
					showDeleteDBDialog(context, item, doneListner);
				}else{
					count++;
				}							
			}
		}
    	getHiddenFolderDBList(); //update DB list
        if (count > 0) {
        	updateMediaScan();
        	Toast.makeText(mCtx, mCtx.getResources().getString(R.string.folders_unhide_success) + mCtx.getResources().getString(R.string.folders_unhide_success_after_warning_message)
        			, Toast.LENGTH_SHORT).show();
        	return true;
        }
		return false;
	}

	/**
	 * <p> callback method called after dialog <p>
	 */	
	public interface HideDoneListener {
		public void onDone();
	}

	/**
	 * <p>One folder unhide<p>
	 * @param item {@link HiddenFolderItem}
	 */	
	public boolean setUnHideFolder(HiddenFolderItem item){
		
		String oldName = item.getPath();
        String str = oldName.substring(0,oldName.lastIndexOf("/"));          
        String str1 = str.substring(0,str.lastIndexOf("/")+1);        
        String str2 = str1 + "." +item.getmFolderName();	
               
        File oldFile = new File(str2);
        File newFile = new File(oldName.replace(str2, item.getmFolderName()));
         
		boolean result = oldFile.renameTo(newFile);

		
		if(result){
			if(mHelper == null){
				mHelper = new HiddenFolderDBHelper(mCtx);
			}
	        mHelper.open();
			mHelper.deleteHiddenFolderData(item.getPath(), changeToByteFromBitmap(item.getThumbnail()));		
			mHelper.close();
		}	
        return result;

	}
	/**
	 * <p>Media scan<p>
	 * Rescan for update
	 */		
	public void updateMediaScan(){	
		mCtx.sendBroadcast( new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}
	
	public boolean hiddenFolderDBMake(){

		String path = Environment.getExternalStorageDirectory() + "PhotoDesk";
		
		File hidenFolder = new File(path ,hiddenListFileName);
		
		try{
			boolean created = hidenFolder.createNewFile();		
			if (created){
				if (Log.isLoggable(TAG, Log.INFO)){
					Log.i(TAG, String.format(
		           "Successfully created the %s file",
		           hidenFolder.getAbsolutePath()));
				}
			}else{
				Log.e(TAG, String.format(
			       "The directory %s is already has a %s file",
			       hidenFolder.getAbsolutePath(),
			       MediaStore.MEDIA_IGNORE_FILENAME));
			}
		}catch (java.io.IOException e){
			Log.e(TAG, String.format(
					"Caught exception while creating %s: %s",
					hidenFolder.getAbsolutePath(),
					e.getMessage()));
		}
		return true;	
		
	}
	
	public boolean hiddenFolderDBUpdate(){
		
		mHelper.open();
		mHelper.getHiddenFolderDataHashMap();
		mHelper.close();
		return true;
	}
	
	/**
	 * <p>To change the bitmap image to byte<p>
	 * @param bitmap Byte change to a bitmap image
	 * @return changed image as a byte
	 */	
	public byte[] changeToByteFromBitmap(Bitmap bitmap){
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.img_new_folder);
		}
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();  
        bitmap.compress(CompressFormat.PNG, 0, stream);  
        return stream.toByteArray();  
		
	}

	/**
	 * <p>Check if there is a hidden folder<p>
	 *  
	 * @return if exist return true
	 */	
	public boolean isHiddenFolderData() {
		if (mHiddenFolderMap.size() == 0)
			return false;
		
		return true;
	}  
	/**
	 * <p>get the item in a hidden folder from DB <p>
	 * @return hashmap return
	 */		
	public HashMap<String, byte[]> getHiddenFolderDBList(){
		if(mHelper == null){
			mHelper = new HiddenFolderDBHelper(mCtx);
		}
        mHelper.open();		
		mHiddenFolderMap = mHelper.getHiddenFolderDataHashMap();
		mHelper.close();
		return mHiddenFolderMap;
		
	}
	public Bitmap getHiddenFolderThumbnailImage(byte[] image){

		return BitmapFactory.decodeByteArray(image, 0, image.length);  
	}  

	/**
	 * <p>release all of the selected folder item<p>
	 */	
	public void allUnHiddenFolder() {
		if (mCtx == null) return;
		mHelper = new HiddenFolderDBHelper(mCtx);				
		mHelper.deleteAllHiddenFolderData();
		mHiddenFolderMap.clear();
		mHelper.close();
	}	
}
