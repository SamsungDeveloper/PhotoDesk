package com.samsung.photodesk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.samsung.photodesk.data.MediaObject;

/**
 * <p>Protect/Unprotect managing class</p>
 * Management using database and hashmap.
 */
public class ProtectUtil {
	private HashMap<String, String> mProtectMap = new HashMap<String, String>();
	private ProtectDBHelper mHelper;
	private Context mContext;
	private String mSDCid;
	
	/**
	 * <p>Get SingletoneHolder Instance</p>
	 * @return SingletoneHolder instance
	 */
	public static ProtectUtil getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
	 * <p>Instance generating</p>
	 * SingletoneHolder used by getInstance.
	 */
	private static class SingletonHolder {
		public static final ProtectUtil instance = new ProtectUtil();         
	} 
	
	/**
	 * <p>Constructor</p>
	 */
	private ProtectUtil() {}
	
	/**
	 * <p>Initialize</p>
	 * DataBase and Hashmap initialize
	 * @param context {@link Context}
	 */
	public void initialize(Context context) {
		mContext = context;
		mSDCid = getSDcardCID();
		if(mSDCid == null) {
			mSDCid = "";
		}
		
		initializeProtectStatus();
	}
	
	/**
	 * Interface for ProtectCompleteListener
	 *
	 */
	public interface ProtectCompleteListener {
    	public abstract void onComplete();
    }

	/**
	 * <p>Start protect AsyncTask</p>
	 * @param items - selected items
	 * @param listener - listener
	 */
	public void changeProtect(ArrayList<MediaObject> items, ProtectCompleteListener listener) {
		new ChageProtectAsyncTask(getDataPath(items), listener).execute();
	}  	
	
	/**
	 * <p>Protect AsyncTask</p>
	 * Protect/Unprotect status update.
	 * DataBase and HashMap update.
	 */
	private class ChageProtectAsyncTask extends AsyncTask<Void, Integer, Void> {
    	private ArrayList<String> mPathList;
    	ProtectCompleteListener mProtectCompleteListener;
    	
		public ChageProtectAsyncTask(
				ArrayList<String> pathList, ProtectCompleteListener listener) {
			mPathList = pathList;
			mProtectCompleteListener = listener;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			changeProtectStatus(mPathList);
			return null;
		}	
		
		@Override
		protected void onPreExecute() {
			if (mContext == null) return;
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mProtectCompleteListener != null) {
				mProtectCompleteListener.onComplete();
			}
			super.onPostExecute(result);
		}		
    }
	
    /**
     * <p>DataBase and HashMap initialize.</p>
     * DataBase generating / HashMap generating / update HashMap
     */
	private void initializeProtectStatus() {
		if (mContext == null) return;
        mHelper = new ProtectDBHelper(mContext);
        mHelper.open();
        mProtectMap = mHelper.getProtectDataHashMap(mSDCid);
        checkProtectdata();
        mHelper.close();
	}    
	
	/**
	 * <p>Get protected item count</p>
	 * @param selectedItem - selected items
	 * @return protected item count
	 */
	public int getProtectedItemCount(ArrayList<MediaObject> selectedItem) {
		int count = 0;
		
		for(int i = 0 ; selectedItem.size() > i ; i++) {
			String path = selectedItem.get(i).getPath();			

			if (isProtected(path)) {
				count++;
			}
		}
		
		return count;
	} 	
	
	/**
	 * <p>Unprotect item</p>
	 * @param context {@link Context}
	 * @param requestType ALL_UNPROTECT = 2, ITEM_UNPROTECT = 3, FOLDER_UNPROTECT = 4, VIEW_UNPROTECT = 5
	 * 	
	 */
    public void checkSignatureUnprotect(Context context,int requestType) {
    	if (context == null) return;
    	PhotoDeskUtils.signatureVerification(context, requestType);
    }

    /**
     * <p>Get selected item's protected status</p>
     * @param path - file path
     * @return true - protected item , false - unprotected item
     */
	public boolean isProtected(String path) {
		if (mProtectMap.size() == 0)
			return false;
			
		if (mProtectMap.containsKey(path)) {
			return true;
		} else {
			return false;
		}
	}  	

	/**
	 * <p>Get current protected status</p>
	 * @return true - protected item is exist , false - protected item isn't exist
	 */
	public boolean isProtectedData() {
		if (mProtectMap.size() == 0)
			return false;
		
		return true;
	}  	
	
	/**
	 * <p>Change protect status</p>
	 * Protected Item -> Unprotected Item , Unprotected Item -> protected Item.
	 * DataBase and HashMap update.
	 * @param pathList - selected item's file path
	 */
	public void changeProtectStatus(ArrayList<String> pathList) {
		if (mContext == null) return;
	
        mHelper = new ProtectDBHelper(mContext);
        mHelper.open();		
        
		for(String path : pathList) {
			if(isProtected(path)) {
				mHelper.deleteProtectData(path, mSDCid);
			} else {
				mHelper.insertProtectData(path, mSDCid);
			}
		}
		
		mProtectMap = mHelper.getProtectDataHashMap(mSDCid);
		mHelper.close();
	}  
	
	/**
	 * <p>Check protected data</p>
	 * Check protected data used by HashMap.
	 * DataBase and HashMap update.
	 */
	public void checkProtectdata() {
		Iterator<Entry<String,String>> iterator = mProtectMap.entrySet().iterator();
		ArrayList<String> removeKeys = new ArrayList<String>();
		
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			File file = new File(entry.getKey());
			
			if (!file.exists()) {
				removeKeys.add(entry.getKey());
			}
		}
		
		for(int index = 0 ; index < removeKeys.size(); index++) {
			mHelper.open();
			mProtectMap.remove(removeKeys.get(index));
			mHelper.deleteProtectData(removeKeys.get(index), mSDCid);
			mHelper.close();
		}
		
	}  		

	/**
	 * <p>Get selected item's file path</p>
	 * @param items - selected items
	 * @return selected item's file path
	 */
	private ArrayList<String> getDataPath(ArrayList<MediaObject> items) {
		ArrayList<String> pathList = new ArrayList<String>(); 
		
		for(int index  = 0; index < items.size(); index++) {
			items.get(index).changeProtectedStatus();
			pathList.add(items.get(index).getPath());
		}		
			
		return pathList;
	}	

	/**
	 * <p>All Unprotect</p>
	 * Reset DataBase and HashMap data.
	 */
	public void allUnprotect() {
		if (mContext == null) return;
		mHelper = new ProtectDBHelper(mContext);
        mHelper.open();				
		mHelper.deleteAllProtectData(mSDCid);
		mProtectMap.clear();
		mHelper.close();
	}	
	
	/**
	 * <p>Get external memory card CID</p>
	 * @return CID
	 */
	private String getSDcardCID() {
		String state = Environment.getExternalStorageState(); 
		boolean isSDCard = state.equals(Environment.MEDIA_MOUNTED); 
		String sd_cid;
		
		if (isSDCard) {
	        try {
	            File input = new File("/sys/class/mmc_host/mmc1");
	            String cid_directory = null;
	            int i = 0;
	            File[] sid = input.listFiles();
	
	            for (i = 0; i < sid.length; i++) {
	                if (sid[i].toString().contains("mmc1:")) {
	                    cid_directory = sid[i].toString();
	                    String SID = (String) sid[i].toString().subSequence(
	                            cid_directory.length() - 4,
	                            cid_directory.length());
	                    Log.d("ProtectUtil", " SID of MMC = " + SID);
	                    break;
	                }
	            }
	            BufferedReader CID = new BufferedReader(new FileReader(
	                    cid_directory + "/cid"));
	            sd_cid = CID.readLine();
	            Log.d("ProtectUtil", "CID of the MMC = " + sd_cid);
	        } catch (Exception e) {
	        	sd_cid = "Can not read SD-card cid";
	            Log.e("ProtectUtil", sd_cid);
	        }
	
	    } else {
	    	sd_cid = "External Storage Not available!!";
	    	Log.e("ProtectUtil", sd_cid);
	    } 	
		
		return sd_cid;
	}

	/**
	 * <p>Open DataBase</p>
	 * @param context {@link Context}
	 */
	public void openDB(Context context) {
		if (mHelper == null) {
			initialize(context);
		}
		
        mHelper.open();
	}
	
	/**
	 * <p>Close DataBase</p>
	 */
	public void closeDB() {
		if (mHelper != null) {
			mHelper.open();
		}
	}

	/**
	 * <p>Change protect status</p>
	 * MediaObject item's protect status change.
	 * @param item - MediaObject item
	 * @return true - success , false - fail
	 */
	public boolean protect(MediaObject item) {
		if (mHelper == null || mSDCid == null) return false;
		if (isProtected(item.getPath())) {
			mHelper.deleteProtectData(item.getPath(), mSDCid);
		} else {
			mHelper.insertProtectData(item.getPath(), mSDCid);
		}
		
		return true;
	}
	
	/**
	 * <p>Update HashMap</p>
	 */
	public void updateProtectMap() {
		mProtectMap = mHelper.getProtectDataHashMap(mSDCid);
	}
}
