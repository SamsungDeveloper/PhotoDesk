package com.samsung.photodesk.data;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.util.MediaUtils;
import com.samsung.photodesk.util.ProtectUtil;

/**
 *  That contains the information of the media class
 *  configuration ({@ link ContentFragment}). Has id, title, name, path, date, and location information.
 *
 */
abstract public class MediaItem extends MediaObject implements Parcelable {
	
	long mId;
	
	String mTitle;
	
	String mDisplayName;
	
	String mPath;
	
	String mDateTaken;
	
	String mMimeType;
	
	double mLatitude = 0f;
	
	double mLongitude = 0f;
	
	boolean mSelected = false;
	
	boolean mIsProtect = false;
	
	abstract public Job<Bitmap> requestImage(int store);
	
	/**
	 * create media item
	 * @param cursor MediaStore of the cursor
	 * @param type IMAGE, VIDEO
	 * @return
	 */
	public static MediaItem createItem(Cursor cursor, int type) {
		if (type == IMAGE) {
			return new ImageItem(cursor);
		} else if (type == VIDEO) {
			return new VideoItem(cursor);
		}
		return null;
	}
	
	/**
	 * {@link Cursor} to create a media items.
	 * @param cursor MediaStore of the cursor
	 */
	public MediaItem(Cursor cursor) {
		mId = cursor.getLong(MediaUtils.COL_ID);
		mDisplayName = cursor.getString(MediaUtils.COL_DISPLAY_NAME);
		mTitle = cursor.getString(MediaUtils.COL_TITLE);
		mMimeType = cursor.getString(MediaUtils.COL_MIME_TYPE);
		mPath = cursor.getString(MediaUtils.COL_DATA);
		mDateTaken = cursor.getString(MediaUtils.COL_DATE_TAKEN);
		
		try {
			mLatitude = cursor.getDouble(MediaUtils.COL_LATITUDE);
			mLongitude = cursor.getDouble(MediaUtils.COL_LONGITUDE);
		} catch (NumberFormatException e) {
			mLatitude = 0.0f;
			mLongitude = 0.0f;
		}
		
		mIsProtect = ProtectUtil.getInstance().isProtected(mPath);
	}

	/**
	 * only view Item from web
	 * @param path The file path
	 */
	public MediaItem(String path) {
		mId = -1;
		mDisplayName = "";
		mTitle = "";
		mMimeType = "image/*";
		mPath = path;
		mDateTaken = "";
	}
	
	/**
	 * {@link Parcel} to create a media items.
	 * @param in {@link Parcel}
	 */
	public MediaItem(Parcel in) {
		mId = in.readLong();
    	mTitle = in.readString();
    	mDisplayName = in.readString();
    	mPath = in.readString();
    	mDateTaken = in.readString();
    	mMimeType = in.readString();
    	mLatitude = in.readDouble();
    	mLongitude = in.readDouble();
    	mIsProtect = ProtectUtil.getInstance().isProtected(mPath);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mTitle);
		dest.writeString(mDisplayName);
		dest.writeString(mPath);
		dest.writeString(mDateTaken);
		dest.writeString(mMimeType);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
    }

	/**
	 * Get mine type.
	 * @return mine type
	 */
	public String getMimeType() {
        return mMimeType;
    }

	/**
	 * Set the mine type
	 */
    public void setMimeType(String mimeType) {
        this.mMimeType = mimeType;
    }

    @Override
	public long getId() {
		return mId;
	}

    /**
     * Set bucket id
     * @param id the bucket id
     */
	public void setId(long id) {
		mId = id;
	}

	/**
	 * set the location infomation
	 * @param latitude latitude value
	 * @param longitude longitude value
	 */
	public void setLocation (double latitude, double longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
	}

	/**
	 * Get the latitude values​​.
	 * @return latitude values
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * set the latitude infomation
	 * @param latitude
	 */
	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}

	/**
	 * Get the longitude values​​.
	 * @return longitude values
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * set the longitude infomation
	 * @param longitude
	 */
	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}


	@Override
	public boolean isSelected() {
		return mSelected;
	}
	
	@Override
	public void setSelected(boolean selected) {
		mSelected = selected;
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
		return mPath;
	}

	public String getFolderPath() {
		return mPath.substring(0, mPath.lastIndexOf("/")+1);
	}

	/**
	 * set the path
	 * @param path  Set the path
	 */
	public void setPath(String path) {
		this.mPath = path;
	}
	
	/**
	 * get the date taken
	 * @return
	 */
	public String getDateTaken() {
		return mDateTaken;
	}


	/**
	 * set the date taken
	 * @param dateTime taken
	 */
	public void setDateTaken(String dateTime) {
		this.mDateTaken = dateTime;
	}
	
	/**
	 * get the Uri
	 * @param mediaType
	 * @return Uri
	 */
	public Uri getUri(int mediaType) {
		return MediaUtils.getContentUri(mediaType);
	}

	/**
	 * Check whether the information is valid.
	 * @return If valid, true Otherwise false
	 */
	public boolean isInvaildLocation() {
		return (mLatitude == 0f && mLongitude == 0f); 
	}
	
	@Override
	public boolean isProtected() {
		return mIsProtect;
	}
	
	@Override
	public void setProtected(boolean protect) {
		mIsProtect = protect;
	}
	
    public MediaDetails getDetails() {
        MediaDetails details = new MediaDetails();
        details.addDetail(MediaDetails.INDEX_PATH, getPath());
        details.addDetail(MediaDetails.INDEX_TITLE, mTitle);

        DateFormat formater = DateFormat.getDateTimeInstance();
        details.addDetail(MediaDetails.INDEX_DATETIME, (mDateTaken == null) ? "null" : formater.format(new Date(Long.parseLong(mDateTaken))));
        
        if (mMimeType != null) {
            details.addDetail(MediaDetails.INDEX_MIMETYPE, mMimeType);
        }
        details.addDetail(MediaDetails.INDEX_SIZE,new File(getPath()).length());
        
        if (isInvaildLocation()==false) {
            details.addDetail(MediaDetails.INDEX_LOCATION, new double[] {mLatitude, mLongitude});
        }
        
        return details;
    }

    /**
     * get the title
     * @return title
     */
	public String getTitle() {
		return mTitle;
	}

	@Override
	public void changeProtectedStatus() {
		mIsProtect = !mIsProtect;
	}

	/**
	 * set the protect
	 * @param stauts protected is true Otherwise false
	 */
	public void setProtect(boolean stauts) {
		mIsProtect = stauts;
	}	
	
}
