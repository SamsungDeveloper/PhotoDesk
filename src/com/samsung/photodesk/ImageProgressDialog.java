package com.samsung.photodesk;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;

/**
 * <p>Image progress dialog class</p>
 * Custom progress Dialog box that contains the image.
 *
 */
public class ImageProgressDialog {

	public static final int DIALOG_TYPE_COPPING = 0;
	public static final int DIALOG_TYPE_WARNNING = 1;
	
	private Context mContext;
	
	private FolderItem mFolderItem;
	private ArrayList<MediaObject> mContentItems;
	
	private Dialog mDialog;
	private ImageView mContentIv;
	private TextView mContentTv;
	
	private CancelListener mCancelListener;
	
	/**
	 * Interface for cancel listener
	 *
	 */
	public interface CancelListener {
		public abstract void cancel();
	}
	
	public ImageProgressDialog(Context context/*, FolderItem folderItem*/, ArrayList<MediaObject> contentItems) {
		mContext = context;
		mContentItems = contentItems;
	
		initWarnningDialog();	
	}
	
	public ImageProgressDialog(Context context, FolderItem folderItem, ArrayList<MediaObject> contentItems,
			CancelListener cancel, String title) {
		mContext = context;
		mFolderItem = folderItem;
		mContentItems = contentItems;

		initCoppingDialog(cancel, title);
	}
	

	
	private void initWarnningDialog() {
		mDialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
		
		Window window = mDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mDialog.setContentView(R.layout.overlab_warnning_dialog);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.setCancelable(false);
		
		TextView tv = (TextView)mDialog.findViewById(R.id.overlapWarnningTv);
		StringBuilder sb = new StringBuilder();
		
		if (mContentItems.size() == 1) {
			sb.append(mContext.getString(R.string.warning_one_overlap))
			.append(String.format(mContext.getString(R.string.warning_overlap_image_name), mContentItems.get(0).getDisplayName()));
		} else {
			sb.append(mContext.getString(R.string.warning_many_overlap))
			.append(String.format(mContext.getString(R.string.warning_overlap_image_count), mContentItems.size()));
		}
		
		tv.setText(sb.toString());
	}
	
	private void initCoppingDialog(CancelListener cancel, String title) {
		mCancelListener = cancel;
		
		mDialog = new Dialog(mContext, R.style.SpenDialog);
		Window window = mDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mDialog.setContentView(R.layout.loading_dialog_view);		
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(false);
		
		mContentIv = (ImageView)mDialog.findViewById(R.id.iVContentImage);
		setThumbnailImage(mContentItems.get(0), mContentIv);

        mContentTv = (TextView)mDialog.findViewById(R.id.contentTv);
        mContentTv.setText(mContentItems.get(0).getDisplayName());
		
		MediaItem [] bgItems = mFolderItem.getImages();
		
		ImageView[] folderImg = {(ImageView)mDialog.findViewById(R.id.ivFolderImage)
				,(ImageView)mDialog.findViewById(R.id.ivFolderImage1)
				,(ImageView)mDialog.findViewById(R.id.ivFolderImage2)
				,(ImageView)mDialog.findViewById(R.id.ivFolderImage3)};
		
        if (bgItems == null || bgItems.length == 0 || bgItems[0] == null) {
        	folderImg[0].setVisibility(View.VISIBLE);
        	folderImg[0].setImageDrawable(mContext.getResources().getDrawable(R.drawable.img_new_folder));
        } else {
	        for (int index = 0; index < bgItems.length; index++) {
	        	if (index >= 4 || folderImg[index] == null) break;
	 	        
	        	if (bgItems[index] == null) {
	        		folderImg[index].setImageDrawable(mContext.getResources().getDrawable(R.drawable.shadow_folder));
	        	} else {
	        		folderImg[index].setVisibility(View.VISIBLE);
	        		
	        		final Bitmap bm = ThumbnailCache.INSTANCE.getBitmap(bgItems[index].getId());
    				folderImg[index].setImageBitmap(bm);
	        	}
	        }
        }
		
		TextView folderTv = (TextView)mDialog.findViewById(R.id.folderNameTv);
		folderTv.setText(mFolderItem.getDisplayName());		
		
		Button close = (Button)mDialog.findViewById(R.id.copy_cancel);
		close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCancelListener != null)	mCancelListener.cancel();
				dismiss();
			}
		});
		
        TextView titleView = (TextView)mDialog.findViewById(R.id.tvTitle);
        titleView.setText(title);
	}
	
	public void show() {
		mDialog.show();
	}
	
	public void showDuration(int time) {
		show();
		
		(new SleepThread()).start();
	}
	
	public void dismiss() {
		mDialog.dismiss();
	}
	
	public void changeContentItem(int position) {
		setThumbnailImage(mContentItems.get(position), mContentIv);
		mContentTv.setText(mContentItems.get(position).getDisplayName());
	}
	
	private void setThumbnailImage(MediaObject bgItem, ImageView ivThumbnail) {
		if (ivThumbnail == null) return;
	
		final Bitmap bm = ThumbnailCache.INSTANCE.getBitmap(bgItem.getId());
		ivThumbnail.setImageBitmap(bm);
	}
	
	class SleepThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Log.e("InterruptedException","Thread Sleep Error");
			}
			dismiss();
		}
	}

}
