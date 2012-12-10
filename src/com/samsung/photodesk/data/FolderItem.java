package com.samsung.photodesk.data;

import java.io.File;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.samsung.photodesk.FolderFragment;
import com.samsung.photodesk.util.PhotoDeskUtils;
import com.samsung.photodesk.util.ProtectUtil;

/**
 * The folder that contains the information class
 */
public class FolderItem extends MediaObject implements Parcelable {
	public static final int NEW_FOLDER_ID = 0;
	
    private long mBucketId;

    private String mDisplayName;
	
    private boolean mSelected = false;

	private String mPath;
	
	private int mItemCount;
	
	private boolean mPreSelected = false;
	
	private boolean mIsProtect = false;
	
	private MediaItem []mThumbImages = new MediaItem[FolderFragment.MAX_THUMBNAIL_CNT];
	
	/**
	 * {@link Cursor} to create a folder items.
	 * @param cursor MediaStore of the cursor
	 */
	public FolderItem(Cursor cursor) {
		mBucketId =  cursor.getLong(0);
		mPath = cursor.getString(1);
		mDisplayName = cursor.getString(2);
		mIsProtect = ProtectUtil.getInstance().isProtected(getPath());
	}
	
	/**
	 * Creates a folder with the name of an item.
	 * @param name Create a folder name
	 */
	public FolderItem(String name) {
		mBucketId =  NEW_FOLDER_ID;
		mDisplayName = name;
		mPath = PhotoDeskUtils.getDefualtFodler() + name + "/";
		mIsProtect = ProtectUtil.getInstance().isProtected(getPath());
	}
	
	/**
	 * {@link Parcel} to create a folder items.
	 * @param in {@link Parcel}
	 */
	public FolderItem(Parcel in) {
		mBucketId = in.readLong();
	    mDisplayName = in.readString();
		mPath = in.readString();
		mItemCount = in.readInt();
		mIsProtect = ProtectUtil.getInstance().isProtected(getPath());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mBucketId);
		dest.writeString(mDisplayName);
		dest.writeString(mPath);
		dest.writeInt(mItemCount);
    }
	
	/**
	 * Parcel creator
	 */
	public static final Parcelable.Creator<FolderItem> CREATOR = new Creator<FolderItem>() {
        @Override
		public FolderItem createFromParcel(Parcel source) {
        	
            return new FolderItem(source);
        }
        @Override
		public FolderItem[] newArray(int size) {
            return new FolderItem[size];
        }
    };

    @Override
    public long getId() {
        return mBucketId;
    }

    /**
     * Set bucket id
     * @param id the bucket id
     */
    public void setId(long id) {
        this.mBucketId = id;
    }

   
    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Set the display name.
     * @param name display name
     */
    public void setDisplayName(String name) {
        this.mDisplayName = name;
    }

   
	@Override
	public String getPath() {
		File file = new File(mPath);
		if (file.isFile()) {
			return mPath.substring(0, mPath.lastIndexOf("/")+1);
		} else {
			return mPath;
		}
	}
	
	public String getFolderPath() {
		File file = new File(mPath);
		String name = file.getName();
		String path = mPath.replace(name, "");
		return path;
	}
	
	public String getFilePath() {
		return mPath;
	}	

	/**
	 * Get thumbnail images.
	 * @return thumbnail images
	 */
	public MediaItem [] getImages() {
		return mThumbImages;
	}
	
	/**
	 * clear thumbnail images.
	 */
	synchronized public void clearThumb() {
		for (int index = 0; index < mThumbImages.length; index++) {
			mThumbImages[index] = null;
		}
	}
	
	/**
	 * get the number of a thumbnail
	 * @return number of a thumbnail
	 */
	public int getThumbCount() {
		int index = 0;
		while (index < mThumbImages.length) {
			if (mThumbImages[index] == null)
				break;
			
			index++;
		}
		return index;
	}

	@Override
	public boolean isSelected() {
		return mSelected;
	}

	@Override
	public void setSelected(boolean selected) {
		this.mSelected = selected;
	}

	/**
	 * Media to get the count that is included in the folder.
	 * @return Media count
	 */
	public int getItemCount() {
		return mItemCount;
	}

	/**
	 * Set the media, the number of which is included in the folder.
	 * @param count Media count
	 */
	public void setItemCount(int count) {
		mItemCount = count;
	}
	
	public boolean isPreSelected() {
        return mPreSelected;
    }
	
	public void setPreSelected(boolean preSelected) {
        this.mPreSelected = preSelected;
    }

	/**
	 * check the thumbnail has been initialized.
	 * @return Has been initialized true Otherwise false
	 */
	public boolean isInitThumb() {
		return (mThumbImages[0] != null);
	}

	/**
	 * Replaces the thumbnail at the specified location in this array with the specified thumbnail.
	 * @param index index the index at which to put the specified thumbnail.
	 * @param mediaItem thumbnail
	 */
	public void setThumb(int index, MediaItem mediaItem) {
		if (mThumbImages.length >= index) return;
		mThumbImages[index] = mediaItem;
	}
	
	@Override
	public void setProtected(boolean protect) {
		mIsProtect = protect;
	}

	
	@Override
	public boolean isProtected() {
		return mIsProtect;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void changeProtectedStatus() {
		mIsProtect = !mIsProtect;
	}
	
	/**
	 * set the protect
	 * @param stauts  protect stauts
	 */
	public void setProtect(boolean stauts) {
		mIsProtect = stauts;
	}
	
	@Override
	public int getType() {
		return FOLDER;
	}

	/**
	 * set the path
	 * @param path  Set the path
	 */
	public void setPath(String path) {
		mPath = path;
	}
	
}
