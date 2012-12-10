package com.samsung.photodesk;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.samsung.photodesk.ImageProgressDialog.CancelListener;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.photodesk.view.CustomProgressDialog;

/**
 *  <P>Menu Task class</P>
 *  Menu task for file state change.
 *  Using thread, And to indicate progress through the dialog.
 *  onStop{@link ActivityStatusImpl.onStop}} - thread stop 
 *  onPase{@link ActivityStatusImpl.onResume}} - thread resume
 *
 */
public class MenuTask implements ActivityStatusImpl {
	private Context mContext;
	private FolderItem mSelectedFolder;
	private ArrayList<MediaObject> mSelectedItems;
	private static OperationTask sOperation;
	private static OnOperationListener sListener;
	private static int sMenuId = -1;
	private GeoPoint mGPoint;
	
	public static ImageProgressDialog sDialog;
	
	public static CustomProgressDialog sProgressDialog;
	
	public MenuTask (Context context, OnOperationListener listener) {
		mContext = context;
		sListener = listener;
	}
	
	public void setSelectedItems(ArrayList<MediaObject> selectedItems) {
		mSelectedItems = selectedItems;
	}
	
	public void setSelectedItems(MediaObject selectedItem) {
		mSelectedItems = new ArrayList<MediaObject>();
		mSelectedItems.add(selectedItem);
	}
	
	public void setSelectedFolder(FolderItem selectedFolder) {
		mSelectedFolder = selectedFolder;
	}	
	
	public void setGeoPoint(GeoPoint P) {
		mGPoint = P;
	}	
	
	public void close() {
		if (sOperation != null) {
			sOperation.cancel();
			sOperation = null;
		}
		sListener = null;
	}
	
	public void setOperationLintener(OnOperationListener listener) {
		sListener = listener;
	}

	public void onItemClicked(int menuId) {
		sMenuId = menuId;
		runMenu();
	}

	private void runMenu() {
		final FileManager fileMgr = new FileManager(sMenuId, mContext, mSelectedItems, mSelectedFolder);
		fileMgr.prepare(new DonePrepareListener() {
			
			@Override
			public boolean onDonePrepare(ArrayList<MediaObject> operationItems) {
				
				sOperation = new OperationTask(fileMgr, operationItems);
				sOperation.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
				createProgressDialog();
				return true;
			}
		});
	}

	/**
	 * <p>AsyncTask Thread</p>
	 * Operation thread run during file state change.
	 *
	 */
	public class OperationTask extends AsyncTask<Long, Integer, Boolean> {
		WaitNotify mWaitNotify = new WaitNotify();
		private ArrayList<MediaObject> mDoneItems = new ArrayList<MediaObject>();
		boolean mStop = false;
		boolean mCancel = false;
		FileManager mFileMgr;
		ArrayList<MediaObject> mOperationItems;
	    
	    public OperationTask(FileManager fileMgr, ArrayList<MediaObject> operationItems) {
	    	mFileMgr = fileMgr;
	    	mOperationItems = operationItems;
	    }
	   
		public void cancel() {
			mCancel = true;
			if (mFileMgr != null) {
				mFileMgr.cancel();
			}
		}
		
		@Override
		protected void onPreExecute() {
			if (sMenuId == R.id.protect || sMenuId == R.id.unprotect) {
				ProtectUtil.getInstance().openDB(mContext);
			}
			if (sMenuId == R.id.folder_rename) {
				Toast.makeText(mContext, R.string.processing_rename_folder, Toast.LENGTH_SHORT).show();
			}
			super.onPreExecute();
		}

		@Override
	    protected Boolean doInBackground(Long... params) {
			boolean result = false; 
			mDoneItems.clear();
			
			int index = 0;
			for (MediaObject item: mOperationItems) {
				if (mCancel) {
					break;
				}
				
				if (mStop) {
					stop();
				}
				
				switch (sMenuId) {
				case R.id.delete:
					result = mFileMgr.delete(item);
					break;
					
				case R.id.copy:
					result = mFileMgr.copy(item);
					break;
					
				case R.id.move:
				case R.id.drag_drop:
					result = mFileMgr.move(item);
					break;
					
				case R.id.new_folder:
					result = mFileMgr.newFolder(item);
					break;

				case R.id.add_new_folder:
					result = mFileMgr.addFolder(item);
					break;
					
				case R.id.rotation_left:
					result = mFileMgr.rotation(item, FileManager.ROTATION_LEFT);
					break;
					
				case R.id.rotation_right:
					result = mFileMgr.rotation(item, FileManager.ROTATION_RIGHT);
					break;
					
				case R.id.protect:
					result = mFileMgr.protect(item);
					break;
					
				case R.id.unprotect:
					result = mFileMgr.protect(item);
					break;					
					
				case R.id.rename:
				case R.id.folder_rename:
					result = mFileMgr.rename(item);
					break;
					
				case R.id.merge:
					result = mFileMgr.merge(item);
					break;
					
				case R.id.action_location_edit:
					result = mFileMgr.locationEdit(item, mGPoint);
					break;

				default:
					break;
				}
				
				if (result) {
					mDoneItems.add(item);
				}
				
				publishProgress(index);
				index++;
			}
			
	        return result;
	    }
		
		/**
		 * Update progress
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			if (isImagePrograss()) {
				if(sDialog != null) {
					sDialog.changeContentItem(values[0]);
				}
			} else {
				if (sProgressDialog != null)	sProgressDialog.setProgress(values[0]);
			}
			
			super.onProgressUpdate(values);
		}

	    @Override
	    protected void onPostExecute(Boolean success) {
	    	if (mCancel) {
	    		Log.i("OperationTask", "::: CANCEL");
	        }
	    	
	    	if (sMenuId == R.id.protect || sMenuId == R.id.unprotect) {
	    		ProtectUtil.getInstance().updateProtectMap();
	    		ProtectUtil.getInstance().closeDB();
	    	}
	    	
			if (sMenuId == R.id.folder_rename) {
				mContext.sendBroadcast( new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
			}	    	

	    	if (sListener != null) {
	    		sListener.onDone(sMenuId, mFileMgr.getSelectedFolder(), 
	    				mSelectedItems, mDoneItems, mCancel);
	    	}
	    	
	    	if (isImagePrograss()) {
	    		if (sDialog != null)	    		sDialog.dismiss();
			} else {
				if (sProgressDialog != null) {
					sProgressDialog.dismiss();
					sProgressDialog = null;
				}
				
			}
	    }

		public void stop() {
			mWaitNotify.stop();
		}
		
		public void resume() {
			mStop = false;
			mWaitNotify.resume();
		}
		
		/**
		 * Stop/Resume for operation thread
		 *
		 */
		private class WaitNotify {
			synchronized public void stop() {
				try {
					wait();
				} catch (Exception e) {}
			}

			synchronized public void resume() {
				try {
					notifyAll();
				} catch (Exception e) {}
			}
		}

		public void setStop(boolean stop) {
			mStop = stop;
		}
		
		public boolean getStop() {
			return mStop;
		}		

		public FileManager getFileManager() {
			return mFileMgr;
		}

		public ArrayList<MediaObject> getItems() {
			return mOperationItems;
		}
	}

	/**
	 * <p>Interface for operation result</p>
	 *
	 */
	public interface OnOperationListener {
		/**
		 * <p>Processing the operation result</p>
		 * @param menuId - menu ID
		 * @param selectedFolder - selected folder (Set null if does not selected folder)
		 * @param selectedItems - selected items
		 * @param doneItems - result items
		 * @param canceled - cancel
		 */
		public abstract void onDone(long menuId, FolderItem selectedFolder, ArrayList<MediaObject> selectedItems, ArrayList<MediaObject> doneItems, boolean canceled);
    }
	
	@Override
	public void onResume() {
		if (sOperation != null && sOperation.getStatus() != AsyncTask.Status.FINISHED && sOperation.getStop()) {
			sOperation.resume();
			createProgressDialog();
		}
	}
	
	private void createProgressDialog() {
		if (mContext == null || sOperation == null) return;
		
		if (isImagePrograss()) {
			sDialog = new ImageProgressDialog(
					mContext, 
					sOperation.getFileManager().getSelectedFolder(), 
					sOperation.getItems(),
					new CancelListener() {
						
						@Override
						public void cancel() {
							sOperation.cancel();
						}
					},
					getDlgTitle());
			sDialog.show();
		} else {			
			sProgressDialog = new CustomProgressDialog(mContext);
			sProgressDialog.setProgressStyle(CustomProgressDialog.STYLE_HORIZONTAL);
			sProgressDialog.setTitle(getDlgTitle());
			sProgressDialog.setMessage(getDlgMessage());
			sProgressDialog.setMax(sOperation.getItems().size());
			sProgressDialog.setCancelable(false);
			sProgressDialog.setButton(R.string.cancel, new OnClickListener() {
	            
	            @Override
	            public void onClick(View v) {
	            Log.i("",">>>> Cancel btn");
	              sProgressDialog.cancel();
	            }
	        });	
			sProgressDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					Log.i("",">>>> Cancel");
					sOperation.cancel();
					sProgressDialog = null;
				}
			});
		
			sProgressDialog.show();
		}
	}


	private boolean isImagePrograss() {
		return (sMenuId == R.id.copy ||
				sMenuId == R.id.move ||
				sMenuId == R.id.merge ||
				sMenuId == R.id.new_folder ||
				sMenuId == R.id.add_new_folder ||
				sMenuId == R.id.drag_drop);
	}


	private String getDlgMessage() {
		switch (sMenuId) {
		case R.id.delete:
			return mContext.getString(R.string.removing);
			
		case R.id.protect:
			return mContext.getString(R.string.protection_status_change);
			
		case R.id.unprotect:
			return mContext.getString(R.string.protection_status_change);			
			
		case R.id.merge:
			return mContext.getString(R.string.doing);

		default:
			break;
		}
		return null;
	}


	private String getDlgTitle() {
		switch (sMenuId) {
		case R.id.delete:
			return mContext.getString(R.string.delete_contents);
			
		case R.id.copy:
			return mContext.getString(R.string.copying);
			
		case R.id.move:
		case R.id.drag_drop:
			return mContext.getString(R.string.moving);
			
		case R.id.new_folder:
		case R.id.add_new_folder:
			return mContext.getString(R.string.adding);
			
		case R.id.rotation_left:
			return mContext.getString(R.string.rotate_left);
			
		case R.id.rotation_right:
			return mContext.getString(R.string.rotate_right);
			
		case R.id.protect:
			return mContext.getString(R.string.protect);

		case R.id.unprotect:
			return mContext.getString(R.string.unprotect);			
			
		case R.id.merge:
			return mContext.getString(R.string.merge_folders);
			
		case R.id.action_location_edit:
			return mContext.getString(R.string.location_change);			
		default:
			break;
		}
		return null;
	}

	@Override
	public void onStop() {
		if (sMenuId != -1 && (isImagePrograss())) {
			if (sDialog != null) {
				sDialog.dismiss();
				sDialog = null;
			}
		} else {
			if (sProgressDialog != null) {
				sProgressDialog.dismiss();
				sProgressDialog = null;
			}
		}
		
		if (sOperation != null) {
			sOperation.setStop(true);
		}
	}
	
	/**
	 * <p>Interface for prepare listener</p>
	 *
	 */
	interface DonePrepareListener {
		boolean onDonePrepare(ArrayList<MediaObject> operationItems);
	}

}
