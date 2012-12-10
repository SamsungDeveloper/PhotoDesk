package com.samsung.photodesk.util;

import android.net.Uri;
import android.provider.MediaStore;

import com.samsung.photodesk.data.MediaItem;

/**
 * <p>MediaUtil</p>
 * Managing media data
 */
public class MediaUtils {
	public static final int COL_ID				= 0;
	public static final int COL_DATA 			= 1;
	public static final int COL_DISPLAY_NAME   	= 2;
	public static final int COL_DATE_TAKEN   	= 3;
	public static final int COL_LATITUDE   		= 4;
	public static final int COL_LONGITUDE   	= 5;
	public static final int COL_TITLE		   	= 6;
	public static final int COL_MIME_TYPE		= 7;
	public static final int COL_ORIENTATION   	= 8;
	public static final int COL_DURATION   		= 8;
	public static final int COL_RESOLUTION   	= 9;
	
	
	public static final String[] BUCKET_IMAGE_COLUMNS = {
		MediaStore.Images.ImageColumns.BUCKET_ID,
		MediaStore.Images.ImageColumns.DATA,
		MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME 
		
	};
	
	public static final String[] BUCKET_VIDEO_COLUMNS = {
		MediaStore.Video.VideoColumns.BUCKET_ID,
		MediaStore.Video.VideoColumns.DATA,
		MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME 
	};
	
	public static final String[] IMAGE_COLUMNS = {
		MediaStore.Images.ImageColumns._ID,
		MediaStore.Images.ImageColumns.DATA,
		MediaStore.Images.ImageColumns.DISPLAY_NAME,
		MediaStore.Images.ImageColumns.DATE_TAKEN,
		MediaStore.Images.ImageColumns.LATITUDE,
		MediaStore.Images.ImageColumns.LONGITUDE,
		MediaStore.Images.ImageColumns.TITLE,
		MediaStore.Images.ImageColumns.MIME_TYPE,
		MediaStore.Images.ImageColumns.ORIENTATION
	};
	
	public static final String[] VIDEO_COLUMNS = {
		MediaStore.Video.VideoColumns._ID,
		MediaStore.Video.VideoColumns.DATA,
		MediaStore.Video.VideoColumns.DISPLAY_NAME,
		MediaStore.Video.VideoColumns.DATE_TAKEN,
		MediaStore.Video.VideoColumns.LATITUDE,
		MediaStore.Video.VideoColumns.LONGITUDE,
		MediaStore.Video.VideoColumns.TITLE,
		MediaStore.Video.VideoColumns.MIME_TYPE,
		MediaStore.Video.VideoColumns.DURATION,
		MediaStore.Video.VideoColumns.RESOLUTION
		
	};

	/**
	 * <p>Get data columns</p>
	 * @param mediaType - Image or Video
	 * @return data columns
	 */
	public static String[] getBucketColumns(int mediaType) {
		return (mediaType == MediaItem.IMAGE) 
				? BUCKET_IMAGE_COLUMNS 
				: BUCKET_VIDEO_COLUMNS;
	}

	/**
	 * <p>Get data URI</p>
	 * @param mediaType - Image or Video
	 * @return data URI
	 */
	public static Uri getContentUri(int mediaType) {
		return (mediaType == MediaItem.IMAGE) 
				? MediaStore.Images.Media.EXTERNAL_CONTENT_URI 
				: MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	}

	/**
	 * <p>Get data bucketID</p>
	 * @param mediaType - Image or Video
	 * @return data bucketID
	 */
	public static String getBucketId(int mediaType) {
		return (mediaType == MediaItem.IMAGE) 
				? MediaStore.Images.ImageColumns.BUCKET_ID
				: MediaStore.Video.VideoColumns.BUCKET_ID;
	}
	
	/**
	 * <p>Get data display name</p>
	 * @param mediaType - Image or Video
	 * @return data display name
	 */
	public static String getBucketDisplayName(int mediaType) {
		return (mediaType == MediaItem.IMAGE) 
				? MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
				: MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME;
	}
	
	/**
	 * <p>Get bucket data</p>
	 * @param mediaType - Image or Video
	 * @return bucket data
	 */
	public static String getBucketData(int mediaType) {
		return (mediaType == MediaItem.IMAGE) 
				? MediaStore.Images.ImageColumns.DATA
				: MediaStore.Video.VideoColumns.DATA;
	}
}
