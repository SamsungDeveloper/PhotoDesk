package com.samsung.photodesk;

import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.samsung.photodesk.FolderFragment.ViewHolder;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.ContentItem;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.ImageItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.editor.ImageEditorActivity;
import com.samsung.photodesk.loader.ExifThumbnailLoader;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.FutureListener;
import com.samsung.photodesk.loader.ImageLoadTask;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.util.Setting;
import com.samsung.spen.lib.input.SPenEvent;
import com.samsung.spen.lib.input.SPenLibrary;

/**
 *  <p>Screen showing the content to the slide</p>
 *  show screen content based on slide format.
 */
public class SlideContentFragment extends ContentFragment implements
		OnItemClickListener, OnItemLongClickListener, OnLongClickListener {

	private Gallery mGallery;
	private ImageView mIvDetail;
	private int mCurrentPosition;
	private ProgressBar mLodingProgress;
	Future<Bitmap> mFuture;
	ExifThumbnailLoader mThumbnailLoader;
	
    boolean mDoubletap = false;
   
    View mDragStartView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mContainer = (ViewGroup)inflater.inflate(R.layout.content_slide_view, null);
    	mGallery = (Gallery) mContainer.findViewById(R.id.galImageThumbs);
    	mFolder = (FolderItem) getArguments().getParcelable("folder");
    	if (mContainer == null || mFolder == null) return null;
    	
    	mLodingProgress = (ProgressBar) mContainer.findViewById(R.id.progressBar);
    	mIvDetail = (ImageView) mContainer.findViewById(R.id.ivDetailImage);
    	
    	ViewTreeObserver vto = mIvDetail.getViewTreeObserver();
    	vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
    		boolean init = false;
    	    public boolean onPreDraw() {
    	    	
    	    	if (!init) {
    	    		if (getItemCount() == 0) return true;
    	    		ContentItem.getInstance().get(0).setSelected(true);
    	    		setDetailImage(0);
    	    		init = true;
    	    		notifyDataSetChanged();
    	    	}
    	        return true;
    	    }
    	});
    	
    	loadItems();
    	setAdapter();
        
    	mGallery.setOnTouchListener(this);
    	mGallery.setOnItemClickListener(this);
    	mGallery.setOnItemLongClickListener(this);
    	mIvDetail.setOnLongClickListener(this);
    	mIvDetail.setOnTouchListener(this);
    	getGestureDetector().setOnDoubleTapListener(mDoubleTapListener);
 
    	return super.onCreateView(inflater, container, savedInstanceState);
    }

	public View getChildAt(int index) {
		return mGallery.getChildAt(index);
	}
    
    OnDoubleTapListener mDoubleTapListener = new OnDoubleTapListener() {
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d("double tap", "onSingleTapConfirmedonSingleTapConfirmed");
            if (mDoubletap) {
                mDoubletap = false;
                return false;
            }

            if (mSelectionMode)
                return false;

            MediaItem item = mAdapter.getItem(mCurrentPosition);
            
            if (item.getType() == MediaItem.IMAGE) {
            	if (((ImageItem)item).updateSAM()) {
                    startAnimation(item.getPath());
                } else {
                    startImageView(mFolder.getId(), mCurrentPosition);
                }
            } else if (item.getType() == MediaItem.VIDEO) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(item.getPath()), "video/*");
                startActivity(intent);
            }

            return true;
        }
        
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d("double tap", "onDoubleTap");
            mDoubletap = true;
            SPenEvent sPenEvent = SPenLibrary.getEvent(e);

            if (sPenEvent.isPen() == false || mSelectionMode){
            	return false;
            }
                
            Log.d("double tap", "onDoubleTap");
            
            MediaItem item = mAdapter.getItem(mCurrentPosition);

            if (item.getType() == MediaItem.IMAGE) {
                mDoubletap = true;
                Intent intent = new Intent(getActivity(), ImageEditorActivity.class);
                intent.putExtra("path", item.getPath());
                intent.putExtra("empty", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, 0);

                Log.d("DoubleTap", "onToubleTap");
                return true;
            } else {
                return false;
            }
        }
    };
    
    @Override
    public void update() {
    	super.update();
    	setDetailImage(mCurrentPosition);
    }
    
    public void setAdapter() {
    	setCollectionView(mGallery);
    	
    	mAdapter = new ContentMediaItemAdapter(getActivity(),
				R.layout.content_slide_item, 
				ContentItem.getInstance().getItem());
    	mGallery.setAdapter(mAdapter);
    	
	}
    
    final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (message.what == ViewHolder.IMAGE_LOAD) {
				if (mIvDetail != null) {
					WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(mIvDetail);
					imageViewReference.get().setImageBitmap((Bitmap) message.obj);
				}
			} 
			
			mLodingProgress.setVisibility(View.GONE);
		}
	};
    
    private void setDetailImage(int position) {
    	int itemCount = getItemCount();
    	if (itemCount == 0) return;
    	if (position >= itemCount) {
    		position = itemCount-1;
    	}
    	mCurrentPosition = position;
    	final MediaItem item = mAdapter.getItem(position);
    	if (item == null) return;

        ImageView ivPlay = (ImageView) mContainer.findViewById(R.id.iVPlay);
        ImageView ivAniPlay = (ImageView) mContainer.findViewById(R.id.iVAniPlay);

		if (item.getType() == MediaItem.IMAGE) {
			mLodingProgress.setVisibility(View.VISIBLE);
			ivPlay.setVisibility(View.GONE);
			
			loadExifImage(item);
			loadImage(item);
			
            if (((ImageItem)item).updateSAM()) {
            	ivAniPlay.setVisibility(View.VISIBLE);
            	ivAniPlay.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startAnimation(item.getPath());
					}
				});
            } else {
            	ivAniPlay.setVisibility(View.GONE);
            }
		} else {
			Bitmap bm = ThumbnailCache.INSTANCE.getBitmap(item.getId());
			if (bm == null) {
				bm = MediaLoader.createVideoThumbnail(item.getPath());
			}
			mIvDetail.setImageBitmap(bm);
            ivPlay.setVisibility(View.VISIBLE);
		}
	}
    
    private void loadExifImage(final MediaItem item) {
		if (mThumbnailLoader != null) mThumbnailLoader.cancel(true);
		mThumbnailLoader = new ExifThumbnailLoader(item, mIvDetail, 
				mIvDetail.getMeasuredWidth(), mIvDetail.getMeasuredHeight());
	        
        try {
        	mThumbnailLoader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.getId());
        } catch (RejectedExecutionException e) {
        	e.printStackTrace();
        }
	}
    
	private void loadImage(MediaItem item) {
		if (mFuture != null) mFuture.cancel();
		mFuture = getThreadPool().submit(
    			new ImageLoadTask(item, mIvDetail.getMeasuredWidth(), mIvDetail.getMeasuredHeight()),
                new FutureListener<Bitmap>() {
			
            public void onFutureDone(Future<Bitmap> future) {
            	
                Bitmap bitmap = future.get();
                if (future.isCancelled()) {
                    if (bitmap != null) {
                    	bitmap.recycle();
                    	bitmap = null;
                    }
                    mHandler.sendMessage(mHandler.obtainMessage());
                    return;
                }

                mHandler.sendMessage(mHandler.obtainMessage(
                		ViewHolder.IMAGE_LOAD, bitmap));
            }
        });
	}

	@Override
	public int getViewType() {
		return VIEW_SLIDE;
	}
	
	@Override
	public void realign(int compare) {
		if (compare == Setting.INSTANCE.getCompareMode()) return;
		
		Setting.INSTANCE.setCompareMode(getActivity(), compare);
		mAdapter.clear();
		mAdapter.addAll(MediaLoader.getMediaItems(mFolder.getId(), getActivity().getContentResolver()));
		setDetailImage(mCurrentPosition);
		mAdapter.getItem(mCurrentPosition).setSelected(true);
		mGallery.setSelection(mCurrentPosition, true);
		setFocus(mCurrentPosition);
		notifyDataSetChanged();
		super.realign(compare);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
		MediaItem item = mAdapter.getItem(position);
		
		if (mSelectionMode == true) {
			if (item.isSelected()) {
                startDrag(v);
			}
		} else {
			deselectItems();
            item.setSelected(true);
            startSelectionMode();
            mDragStartView = v;
			updateSelectedCount();
			setDetailImage(position);
			mActionModeCallback.setShareItems(false);		
			notifyDataSetChanged();
		}
        return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		setDetailImage(position);

		MediaItem item = mAdapter.getItem(position);
		mAdapter.getItem(position).setSelected(!item.isSelected());
		if (mSelectionMode) {
			mActionModeCallback.setShareItems(false);		
			updateSelectedCount();
		} else {
			setFocus(position);
		}
		notifyDataSetChanged();
	}

	@Override
	public boolean onLongClick(View v) {
		if (mSelectionMode == false && isRunFling() == false) {
			deselectItems();
			MediaItem item = mAdapter.getItem(mCurrentPosition);
			item.setSelected(true);
			startSelectionMode();
			
			updateSelectedCount();
			mActionModeCallback.setShareItems(false);		
			notifyDataSetChanged();
		}
		
		return true;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (mIvDetail != null) {
			mIvDetail.setEnabled(enabled);
		}
		
		if (mGallery != null) {
			mGallery.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}
	
	@Override
	public void movePosition(int position) {
		if (position >= getItemCount()) return;
		mGallery.setSelection(position);
		setDetailImage(position);
		setFocus(position);
	}

	@Override
	public void leaveSelectionMode() {
		super.leaveSelectionMode();
		
		if(mCurrentPosition >= mAdapter.getCount())
			return;
				
		MediaItem item = mAdapter.getItem(mCurrentPosition);
		item.setSelected(true);
	}
	
	@Override
    public boolean onTouch(View v, MotionEvent event) {
    	if (isSelectedMode()) {
    		int action = event.getAction();
    		
    		switch (action) {
			case MotionEvent.ACTION_MOVE:
				if (mDragStartView != null) {
					startDrag(mDragStartView);
				}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mDragStartView = null;
				break;
			default:
				break;
			}
    	}
        if (!isSelectedMode()) {
            if (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER)
                return super.onTouchPenEraser(v, event);
        }
	   	
    	if (v.getId() == R.id.galImageThumbs) return false;
    	return super.onTouch(v, event);
    }
	
    @Override
    public int getFirstVisiblePosition() {
		return mGallery.getFirstVisiblePosition();
	}
}
