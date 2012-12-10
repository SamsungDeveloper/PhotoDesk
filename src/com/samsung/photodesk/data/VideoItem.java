package com.samsung.photodesk.data;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.loader.LoadThumbTask;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;
import com.samsung.photodesk.util.MediaUtils;


/**
 * <p>Video item class</p>
 * Managing for video item.
 *
 */
public class VideoItem extends MediaItem {

    String mResolution;

	int mDuration;
	
	/**
	 * {@link Cursor} to create a video items.
	 * @param cursor MediaStore of the cursor
	 */
	public VideoItem(Cursor cursor) {
		super(cursor);
		
		mResolution = cursor.getString(MediaUtils.COL_RESOLUTION);
		mDuration = cursor.getInt(MediaUtils.COL_DURATION) /1000;
		
	}
	
	/**
	 * {@link Parcel} to create a video items.
	 * @param in {@link Parcel}
	 */
	public VideoItem(Parcel in) {
		super(in);
		
		mResolution = in.readString();
		mDuration = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mResolution);
		dest.writeInt(mDuration);
    }
	
	/**
	 * Parcel creator
	 */
	public static final Parcelable.Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
		public MediaItem createFromParcel(Parcel source) {
            return new VideoItem(source);
        }
        @Override
		public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };
	
    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(MediaDetails.INDEX_DURATION, formatDuration(mDuration));
        return details;
    }
	
	
	@Override
	public int getType() {
		return VIDEO;
	}
	
	
    public static String formatDuration(int duration) {
        int h = duration / 3600;
        int m = (duration - h * 3600) / 60;
        int s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format("%1$02d:%2$02d", m, s);
        } else {
            durationValue = String.format("%1$d:%2$02d:%3$02d", h, m, s);
        }
        return durationValue;
    }
    
    @Override
	public Job<Bitmap> requestImage(int store) {
		return new MakeVideoThumb(store);
	}
    
    /**
	 * Creates a thumbnail of the video.
	 *
	 */
    class MakeVideoThumb implements Job<Bitmap> {
		
		final int mStore ;
		
		public MakeVideoThumb(int store) {
			mStore = store;
		}

		/**
		 * If there is no video in the cache, create thumbnail.
		 */
		@Override
		public Bitmap run(JobContext jc) {
			return getMediaThumbnail(jc);
		}
		
		/**
		 * create thumbnails.
		 * @param jc {@link JobContext}
		 * @return video thumbnail
		 */
		public Bitmap getMediaThumbnail(JobContext jc) {
			if (jc.isCancelled()) return null;
			Bitmap bm = MediaLoader.getThumbnail(VideoItem.this);
			
			if (bm != null) {
				if (mStore == LoadThumbTask.FOLDER) {
					ThumbnailCache.INSTANCE.putFolderBitmap(getId(), bm);
				} else {
					ThumbnailCache.INSTANCE.put(getId(), bm);
				}
			}
			return bm;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	/**
	 * get the resolution
	 * @return
	 */
	public String getResolution() {
		return mResolution;
	}

	/**
	 * get the duration
	 * @return
	 */
	public int getDuration() {
		return mDuration;
	}
	
}
