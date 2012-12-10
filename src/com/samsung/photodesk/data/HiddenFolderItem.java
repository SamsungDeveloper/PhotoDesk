package com.samsung.photodesk.data;

import android.graphics.Bitmap;

/**
 *  <p>Information class of Hidden folder <p>
 *  DATA insert in this class After DB loaded. 
 *
 */
public class HiddenFolderItem {

	private long mId;
	private String mPath;
	private Bitmap mThumbnail;
	private String mFolderName;
	private boolean mSelected = false;

	
	
	/**
	 * <p>create<p>
	 * @param id  DB id
	 * @param path path of hidden folder
	 * @param thumbnail First image of hidden folder
	 * @param folderName Name of hidden folder
	 */
	public HiddenFolderItem(long id, String path, Bitmap thumbnail, String folderName){
		mId = id;
		mPath = path;
		mThumbnail = thumbnail;
		mFolderName = folderName;
	}
	
	public HiddenFolderItem(){
	}
	
	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
	}

	public Bitmap getThumbnail() {
		return mThumbnail;
	}

	public void setThumbnail(Bitmap thumbnail) {
		this.mThumbnail = thumbnail;
	}
	
	public boolean getSelected() {
		return mSelected;
	}

	public void setSelected(boolean selected) {
		this.mSelected = selected;
	}
	public String getmFolderName() {
		return mFolderName;
	}
	public void setmFolderName(String mFolderName) {
		this.mFolderName = mFolderName;
	}	

}
