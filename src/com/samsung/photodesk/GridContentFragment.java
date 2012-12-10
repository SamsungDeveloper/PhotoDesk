
package com.samsung.photodesk;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.samsung.photodesk.data.ContentItem;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.ImageItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.editor.ImageEditorActivity;
import com.samsung.photodesk.loader.ImageConfig;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.util.SpenEventUtil;
import com.samsung.photodesk.view.FastScrollView;
import com.samsung.photodesk.view.GridContentItemView;
import com.samsung.spen.lib.input.SPenEvent;
import com.samsung.spen.lib.input.SPenLibrary;

/**
 *  <p>Screen showing the content to the grid</p>
 *  show screen content based on {@link GridView} grid format.
 */
public class GridContentFragment extends ContentFragment implements OnItemClickListener,
        OnItemLongClickListener {

    boolean mDoubletap = false;
    
    private GridView mPenGrid;

    private View mDragStartView;
    
    private boolean mAnimationFlag;
    private int mImageThumbWidth;
    private int mImageThumbSpacing;
    AlphaAnimation mAlphaAnimation = new AlphaAnimation(1.0f, 1.0f);

    private float mMoveGridX = 0;
    private float mMoveGridY = 0;
    private boolean mInitMove = true;
    
    FastScrollView mFastScrollView;
    
    AbsListView.OnScrollListener mScrollListener = new OnScrollListener() {
        
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mFastScrollView != null)
                mFastScrollView.onScrollStateChanged(view, scrollState);
            switch (scrollState) {
                case SCROLL_STATE_IDLE:
                    mIsScrolling = false;
                    break;
                case SCROLL_STATE_FLING:
                    mIsScrolling = true;
                    break;
            }
        }
        
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            if (mFastScrollView != null)
                mFastScrollView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageThumbWidth = getResources().getDimensionPixelSize(R.dimen.thumb_width);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.content_thumb_spacing);
        mAlphaAnimation.setDuration(0);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContainer = (ViewGroup)inflater.inflate(R.layout.content_grid_view, null);
        mFastScrollView = (FastScrollView)mContainer.findViewById(R.id.FSIndexer);
		mPenGrid = (GridView)mContainer.findViewById(R.id.gVImages);
        mPenGrid.getViewTreeObserver().addOnGlobalLayoutListener(mObserverLayoutLisener);
		mPenGrid.setOnItemClickListener(this);
        mPenGrid.setOnItemLongClickListener(this);
        mPenGrid.setOnTouchListener(this);
        mPenGrid.setOnScrollListener(mScrollListener);
		
		mFolder = (FolderItem) getArguments().getParcelable("folder");
		if (mFolder == null) return mContainer;
		
		loadItems();
		setAdapter();
		
		final FolderFragment folderFragment = getFolderFragment(); 
		if (folderFragment == null || folderFragment.isHidden()) {
			View partitionView = mContainer.findViewById(R.id.partitionView);
			if (partitionView != null) {
				mContainer.findViewById(R.id.partitionView).setVisibility(View.GONE);
			}
		}
		
        Handler hd = new Handler();
        hd.post(new StartLayoutAnimation());
        getGestureDetector().setOnDoubleTapListener(mDoubleTapListener);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	OnDoubleTapListener mDoubleTapListener = new OnDoubleTapListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            SPenEvent sPenEvent = SPenLibrary.getEvent(e);
            Rect mRect = null;
            if (isImageSelectMode() || sPenEvent.isPen() == false || mSelectionMode)
                return false;

            Log.d("double tap", "onDoubleTap");
            int firstPosition;
            int index = SpenEventUtil.INVALID_INDEX;
            
            if (mRect == null) {
                mRect = new Rect();
            }

            for (int i = 0; i < mPenGrid.getChildCount(); i++) {
                getChildAt(i).getHitRect(mRect);
                if (mRect.contains((int)e.getX(), (int)e.getY())) {
                	index = i;
                	break;
                }
            }
            if (index == SpenEventUtil.INVALID_INDEX)
                return false;
            firstPosition = mPenGrid.getFirstVisiblePosition();
            MediaItem item = mAdapter.getItem(index + firstPosition);

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
    public View getChildAt(int index) {
       return mPenGrid.getChildAt(index);
    }
	
    @Override
	public void setAdapter() {
        setCollectionView(mPenGrid);

        mAdapter = new ContentMediaItemAdapter(getActivity(), R.layout.content_grid_item, ContentItem.getInstance().getItem());
        mPenGrid.setAdapter(mAdapter);
    }

    @Override
	public int getFirstVisiblePosition() {
		return mPenGrid.getFirstVisiblePosition();
	}

    @Override
    public int getViewType() {
        return VIEW_GRID;
    }

    @Override
    public void leaveSelectionMode() {
        super.leaveSelectionMode();
        int max = mAdapter.getCount();
        for (int i = 0; i < max; i++) {
            MediaItem item = mAdapter.getItem(i);
            if (item == null) continue;
            item.setSelected(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int position, long id) {
        if (mAnimationFlag || position < 0 || isRunFling()) return;
        if (mDoubletap) {
            mDoubletap = false;
            return;
        }
               
        MediaItem item = mAdapter.getItem(position);
        if (item == null) return;
        if (choosePicture(item)) return;

        if (mSelectionMode) {
            boolean noSelect = true;
            if (item.isSelected()) {
                item.setSelected(false);
                int max = mAdapter.getCount();
                for (int i = 0; i < max; i++) {
                    if (mAdapter.getItem(i).isSelected()) {
                        noSelect = false;
                        break;
                    }
                }
                if (noSelect) {
                	leaveSelectionMode();
                }
            } else {
                item.setSelected(true);
            }

            mActionModeCallback.setShareItems(false);          
            updateSelectedCount();
            notifyDataSetChanged();

		} else {
			if (item.getType() == MediaItem.IMAGE) {
				if (((ImageItem)item).updateSAM()) {
					startAnimation(item.getPath());				
				}else {
					startImageView(mFolder.getId(), position);
				}
				
			} else if (item.getType() == MediaItem.VIDEO) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(item.getPath()), "video/avi");
				startActivity(intent);
			}
		}
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
    	if (isImageSelectMode() || isRunFling())	return false;
        MediaItem item = mAdapter.getItem(position);
        if (mSelectionMode == true) {
            if (item.isSelected()) {
            	startDrag(v);
            }
        } else {
            if (item.isSelected() == false) {
            	deselectItems();
                item.setSelected(true);
                startSelectionMode();
                mDragStartView = v;
            }
            
            updateSelectedCount();
            mActionModeCallback.setShareItems(false);           
        }

        notifyDataSetChanged();
        return true;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMoveGridX = event.getX();
                mMoveGridY = event.getY();
                mInitMove = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mInitMove && (Math.abs(mMoveGridX - event.getX()) > 10 || Math.abs(mMoveGridY - event.getY()) > 10)) {
                    initChildScale();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                initChildScale();
                break;
        }
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
        } else if (!isSelectedMode()) {
            if (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER)
                return super.onTouchPenEraser(v, event);
        }
        return super.onTouch(v, event);
    }
    
    private void initChildScale() {
        mInitMove = true;
        for (int i = 0; i < mPenGrid.getChildCount(); i++) {
            RelativeLayout layout = (RelativeLayout)mPenGrid.getChildAt(i);
            GridContentItemView imageView = (GridContentItemView)layout.findViewById(R.id.iVImage);
            if (imageView.isTouchDown()) {
                imageView.setViewScale(1.0f);
            }
        }
    }

	@Override
	public void realign(int compare) {
		if (compare == Setting.INSTANCE.getCompareMode()) return;
		
		Setting.INSTANCE.setCompareMode(getActivity(), compare);
		mAdapter.clear();
		mAdapter.addAll(MediaLoader.getMediaItems(mFolder.getId(), getActivity().getContentResolver()));
		notifyDataSetChanged();
		
		super.realign(compare);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (mPenGrid != null) {
			mPenGrid.setEnabled(enabled);
		}
		super.setEnabled(enabled);
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_IMAGE_VIEW || requestCode == REQ_SLIDE_SHOW) {
            initLayoutAnimation(mAlphaAnimation);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
	@Override
	public void movePosition(int position) {
		if (position >= getItemCount()) return;
		mPenGrid.setSelection(position);
	}
    
    Animation.AnimationListener mGridAnimationListener = new AnimationListener() {
        
        @Override
        public void onAnimationStart(Animation animation) {
            if (!animation.equals(mAlphaAnimation)) mAnimationFlag = true;
        }
        @Override
        public void onAnimationRepeat(Animation animation) {}
        
        @Override
        public void onAnimationEnd(Animation animation) {
            mAnimationFlag = false;
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener mObserverLayoutLisener = new ViewTreeObserver.OnGlobalLayoutListener() {
        
        @Override
        public void onGlobalLayout() {
            final int numColumns = (int) Math.floor(mPenGrid.getWidth() / (mImageThumbWidth + mImageThumbSpacing));
            if (numColumns == getNumColumns()) return;
            float widthRatio = ImageConfig.getThumbCropSizeWidth();
            float heightRatio = ImageConfig.getThumbCropSizeHight();
            float width = (mPenGrid.getWidth() / numColumns) - mImageThumbSpacing;
            int columnHeight = (int)(width * heightRatio / widthRatio);
            mPenGrid.setNumColumns(numColumns);
            mPenGrid.setColumnWidth((int)width);
            setNumColumns(numColumns);
            ((ContentMediaItemAdapter)mAdapter).setItemLayoutParams(columnHeight, (int)width);
        }
    };
       
    public void initLayoutAnimation(Animation animation){
        if (animation == null || (mPenGrid.getLayoutAnimation() != null && mPenGrid.getLayoutAnimation().getAnimation().equals(mAlphaAnimation))) return;
        mPenGrid.setLayoutAnimationListener(mGridAnimationListener);
        GridLayoutAnimationController animationController = new GridLayoutAnimationController(animation, 0.2f, 0.2f);
        mPenGrid.setLayoutAnimation(animationController);
        mPenGrid.startLayoutAnimation();
    }
    
    @Override
    public void onDestroyView() {
        removeObserver();
        super.onDestroyView();
    }
    
    public void removeObserver() {
        ViewTreeObserver observer = mPenGrid.getViewTreeObserver();
        if (observer != null && observer.isAlive()) {
            observer.removeGlobalOnLayoutListener(mObserverLayoutLisener);
        }
    }
    
    public void initObserver() {
        removeObserver();
        mPenGrid.getViewTreeObserver().addOnGlobalLayoutListener(mObserverLayoutLisener);
    }
        
    public class StartLayoutAnimation implements Runnable {
        @Override
        public void run() {
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
            animation.setDuration(500);
            initLayoutAnimation(animation);
        }
    }
    
    @Override
    public void flingLeft() {
        if (mAnimationFlag) return;
        super.flingLeft();
    }
    
    @Override
    public void flingRight() {
        if (mAnimationFlag) return;
        super.flingRight();
    }
}
