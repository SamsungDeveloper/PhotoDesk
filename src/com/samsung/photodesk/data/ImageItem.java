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
import com.samsung.spensdk.SCanvasView;

/**
 * Class that contains information about the image
 *
 */
public class ImageItem extends MediaItem {
	int mOrientation;
	int mThumbnailOrientation;
	
	boolean mIsSAM;
	boolean mInitSAM;

	/**
	 * {@link Cursor} to create a image items.
	 * @param cursor MediaStore of the cursor
	 */
	public ImageItem(Cursor cursor) {
		super(cursor);
		
		mOrientation = cursor.getInt(MediaUtils.COL_ORIENTATION);
		mThumbnailOrientation = cursor.getInt(MediaUtils.COL_ORIENTATION);
	}
	
	/**
	 * only view Item from web
	 * @param path The file path
	 */
	public ImageItem(String path) {
		super(path);
	}
	
	/**
	 * {@link Parcel} to create a image items.
	 * @param in {@link Parcel}
	 */
	public ImageItem(Parcel in) {
		super(in);
		
		mOrientation = in.readInt();
		mThumbnailOrientation = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(mOrientation);
		dest.writeInt(mThumbnailOrientation);
    }
	
	/**
	 * Parcel creator
	 */
	public static final Parcelable.Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
		public MediaItem createFromParcel(Parcel source) {
        	
            return new ImageItem(source);
        }
        @Override
		public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };
	
    private int getThumbnailOrientation() {
        return mThumbnailOrientation;
    }

    private void setThumbnailOrientation(int thumbnailOrientation) {
        this.mThumbnailOrientation = thumbnailOrientation;
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(MediaDetails.INDEX_ORIENTATION, Integer.valueOf(mOrientation));
        
        String filePath = getPath();
        MediaDetails.extractExifInfo(details, filePath);
        return details;
    }
	
	@Override
	public int getType() {
		return IMAGE;
	}
	
	/**
	 * get the image orientation value
	 * @return orientation value
	 */
	public int getOrientation() {
		return mOrientation;
	}
	
	/**
	 * get the image rotation value
	 * @return orientation value
	 */
	public int getRotation() {
        return mOrientation;
	}
	
	/**
	 * Set the image rotate value
	 * @param rotation rotate value
	 */
	public void setRotation(int rotation) {
		mOrientation = rotation;
	}

	/**
	 * check the rotation image
	 * @return if rotation image is true Otherwise false
	 */
	public boolean isRotation() {
		return (mOrientation == 180 || mOrientation == 90 || mOrientation == 270);
	}

	/**
	 * Rotate the image.
	 */
	public void rotateThumb() {
		 int current_rotation = getRotation();
         int pre_rotation = getThumbnailOrientation();
         int change_rotation;
         
         if (current_rotation == 0) {
        	 current_rotation = 360;
         }
             
         change_rotation = current_rotation - pre_rotation ;
         
         setRotation(getRotation());
         setThumbnailOrientation(getRotation());

         ThumbnailCache.INSTANCE.rotateBitmap(change_rotation, getId());
	}
	
	/**
	 * check the sam file
	 * @return
	 */
	public boolean isSAM() {
		return mIsSAM;
	}
	
	/**
	 * update the sam file
	 * @return sam if true Otherwise false 
	 */
	public boolean updateSAM() {
		if (!mInitSAM) {
			mIsSAM = SCanvasView.isSAMMFile(mPath);
			mInitSAM = true;
		}
		return mIsSAM;
	}

	@Override
	public Job<Bitmap> requestImage(int store) {
		return new MakeImageThumb(store);
	}
	
	/**
	 * Creates a thumbnail of the image.
	 *
	 */
	class MakeImageThumb implements Job<Bitmap> {
		
		private final int mStore;
		
		public MakeImageThumb(int store) {
			mStore = store;
		}

		/**
		 * If there is no image in the cache, create thumbnail.
		 */
		@Override
		public Bitmap run(JobContext jc) {
			Bitmap bm = null;
			if (mStore == LoadThumbTask.FOLDER) {
				bm = ThumbnailCache.INSTANCE.getFolderBitmap(getId());
			} else {
				bm = ThumbnailCache.INSTANCE.getBitmap(getId());
			}
			
			if (bm == null) {
				bm = getMediaThumbnail(jc);
			}
			return bm;
		}
		
		/**
		 * create thumbnails.
		 * @param jc {@link JobContext}
		 * @return image thumbnail
		 */
		public Bitmap getMediaThumbnail(JobContext jc) {
			if (jc.isCancelled()) return null;
			Bitmap bm = MediaLoader.getThumbnail(ImageItem.this);
			
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
	
	/**
	 * check the sam file init 
	 * @return init if true Otherwise false 
	 */
	public boolean isInitSam() {
		return mInitSAM;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
}
