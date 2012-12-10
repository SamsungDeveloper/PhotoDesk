package com.samsung.photodesk.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.samsung.photodesk.FolderFragment;
import com.samsung.photodesk.PhotoDeskApplication;
import com.samsung.photodesk.R;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.FutureListener;
import com.samsung.photodesk.loader.LoadThumbTask;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.loader.ThreadPool.Job;
import com.samsung.photodesk.loader.ThreadPool.JobContext;

/**
 * <p>Folder list custom dialog class</p>
 *
 */
public class SelectedFolderDialog {
	private Context mContext;
	
	private SelectedFolderCallback mSelectedFolder;
	private SelectedNewFolderCallback mSelectedNewFolder;
	
	/**
	 * <p>Interface for selected folder callback</p>
	 *
	 */
	public interface SelectedFolderCallback {
		void onSelectedFolder(int position, FolderItem folderItem);
	}
	
	/**
	 * <p>Interface for selected new folder callback</p>
	 *
	 */
	public interface SelectedNewFolderCallback {
		void onSelectedNewFolder();
	}
	
	/**
	 * Constructor
	 * @param context {@link Context}
	 */
	public SelectedFolderDialog(Context context) {
		mContext = context;
	}
	
	/**
	 * <p>Set selected folder callback</p>
	 * @param callback - SelectedFolderCallback
	 */
	public void setOnSelectedFolder(SelectedFolderCallback callback) {
		mSelectedFolder = callback;
	}
	
	/**
	 * <p>Set selected new folder callback</p>
	 * @param callback - SelectedNewFolderCallback
	 */
	public void setOnSelectedNewFolder(SelectedNewFolderCallback callback) {
		mSelectedNewFolder = callback;
	}	
	
	/**
	 * <p>Show dialog for folder list</p>
	 */
	public void show() {
		final SpenDialog dialog = new SpenDialog(mContext);
		dialog.setContentView(makeFolderList());
		dialog.setTitle(R.string.folder_list);
		dialog.setLeftBtn(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		
		ListView list = (ListView) dialog.findViewById(R.id.folder_list);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dialog.dismiss();
             
				ArrayList<FolderItem> items = FolderFragment.getFolderItems();
				if (items != null) {
					if (mSelectedFolder != null) {
						mSelectedFolder.onSelectedFolder(position, items.get(position));
					}
				}
			}
		});
		
		dialog.setRightBtn(R.string.new_folder, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (mSelectedNewFolder != null) {
					mSelectedNewFolder.onSelectedNewFolder();
				}
			}
		});		
	}
	
	/**
	 * <p>Show dialog for folder list</p>
	 */
	public void show(final ArrayList<FolderItem> folderItems) {
		final SpenDialog dialog = new SpenDialog(mContext);
		dialog.setContentView(makeFolderList(folderItems));
		dialog.setTitle(R.string.folder_list);
		dialog.setLeftBtn(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		
		ListView list = (ListView) dialog.findViewById(R.id.folder_list);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dialog.dismiss();
             
				if (folderItems != null) {
					if (mSelectedFolder != null) {
						mSelectedFolder.onSelectedFolder(position, folderItems.get(position));
					}
				}
			}
		});
		
		dialog.setRightBtn(R.string.new_folder, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (mSelectedNewFolder != null) {
					mSelectedNewFolder.onSelectedNewFolder();
				}
			}
		});		
	}
	
	/**
	 * <p>Show dialog for folder list</p>
	 */
	public void show(String addFolderPath) {
		if (addFolderPath == null) return; 
		final SpenDialog dialog = new SpenDialog(mContext);
		dialog.setContentView(makeFolderList());
		dialog.setTitle(R.string.folder_list);
		dialog.setLeftBtn(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		
		ListView list = (ListView) dialog.findViewById(R.id.folder_list);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dialog.dismiss();
             
				ArrayList<FolderItem> items = FolderFragment.getFolderItems();
				if (items != null) {
					if (mSelectedFolder != null) {
						mSelectedFolder.onSelectedFolder(position, items.get(position));
					}
				}
			}
		});
	}	
	
	/**
	 * <p>Set dialog list</p>
	 * @return View {@link View}
	 */
	private View makeFolderList() {
		View v = LayoutInflater.from(mContext).inflate(R.layout.folder_list_dlg, null);
        ListView list = (ListView)v.findViewById(R.id.folder_list );
		list.setAdapter(new SelectionAdapter(
					mContext, 
					R.layout.selected_folder_item, 
					FolderFragment.getFolderItems())
				);
		
		
        return v;
	}
	
	/**
	 * <p>Set dialog list</p>
	 * @param fodlerNames - selected folder name
	 * @return View {@link View}
	 */
	private View makeFolderList(ArrayList<FolderItem> fodlerItems) {
	View v = LayoutInflater.from(mContext).inflate(R.layout.folder_list_dlg , null );
    ListView list = (ListView)v.findViewById(R.id.folder_list );
	list.setAdapter(new SelectionAdapter(
				mContext,
				R.layout.selected_folder_item, 
				fodlerItems));
	
    return v;
	}	
	
	class ViewHolder {
		static final int IMAGE_LOAD = 0;
		
		final TextView tvName;
        final ImageView ivThumb;
		Future<MediaItem[]> futureFolder;
        
        public ViewHolder(ImageView ivThumb, TextView tvName) {
        	this.tvName = tvName;
        	this.ivThumb = ivThumb;
		}
        
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				if (message.what == ViewHolder.IMAGE_LOAD) {
					if (ivThumb != null) {
						WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(ivThumb);
						imageViewReference.get().setImageBitmap((Bitmap) message.obj);
					}
				} 
			}
		};
	}
	
	public class SelectionAdapter extends ArrayAdapter<FolderItem> {
		private final LayoutInflater mInflater;
		private int mResoruceID;
    
	    public SelectionAdapter(Context context, int resource, List<FolderItem> objects) {
	        super(context, resource, objects);
	        
	        mResoruceID = resource;
	        mInflater = LayoutInflater.from(context);
	    }
	
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	final FolderItem item = getItem(position);
	
	        if (convertView == null) {
	            convertView = mInflater.inflate(mResoruceID, parent, false);
	            convertView.setTag(new ViewHolder(
	            		(ImageView) convertView.findViewById(R.id.iVThum),
	            		(TextView) convertView.findViewById(R.id.tVName)));	            
	        }
	        
	        ViewHolder holder = (ViewHolder) convertView.getTag();
	        holder.tvName.setText(item.getDisplayName());
	        MediaItem mediaItem = item.getImages()[0];
	        if (mediaItem != null) {
	        	holder.ivThumb.setImageBitmap(ThumbnailCache.INSTANCE.getBitmap(item.getImages()[0].getId()));
	        } else {
	        	holder.ivThumb.setImageBitmap(null);
	        	makeThumb(holder, item);
	        }
	        
	        return convertView;
	    }
	    
	    private void makeThumb(final ViewHolder holder, final FolderItem folderItem) {
			Future<MediaItem []> future = holder.futureFolder;
        	if (future != null && !future.isDone()) {
        		future.cancel();
			}
        	
        	holder.futureFolder = getThreadPool().submit(new MakeThumbInfo(folderItem),
				new FutureListener<MediaItem []>() {

					public void onFutureDone(Future<MediaItem []> future) {
						if (future.isCancelled()) {
							folderItem.clearThumb();
							return;
						}
						
						MediaItem [] thubmItems = folderItem.getImages();
						if (thubmItems != null && thubmItems[0] != null) {
							Bitmap bm = ThumbnailCache.INSTANCE.getFolderBitmap(thubmItems[0].getId());
							
							if (bm != null) {
								holder.handler.sendMessage(holder.handler.obtainMessage(
			                    		ViewHolder.IMAGE_LOAD, 0, 0, bm));
							}
						}
						 
					}
				});
		}
	    
	    public class MakeThumbInfo implements Job<MediaItem []> {
			FolderItem mItem;
		    
		    public MakeThumbInfo(FolderItem item) {
		        mItem = item;
		    }
		    public MediaItem [] run(JobContext jc) {
		    	
				ArrayList<MediaItem> items = MediaLoader.getMediaItems(
						mItem.getId(), mContext.getContentResolver());
				
				MediaItem [] thumbItems = mItem.getImages();
				thumbItems[0] = items.get(0);
				
				if (items.get(0) != null) {
					items.get(0).requestImage(LoadThumbTask.FOLDER).run(jc);
				}
				
		    	return thumbItems;
		    }
		}
	}

	/**
	 * <p>Get ThreadPool</p>
	 * @return ThreadPool
	 */
	public ThreadPool getThreadPool() {
		return ((PhotoDeskApplication)((Activity)mContext).getApplication()).getThreadPool();
	}
}
