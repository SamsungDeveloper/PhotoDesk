package com.samsung.photodesk;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.samsung.photodesk.MenuTask.OnOperationListener;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.util.SpenEventUtil;
import com.samsung.photodesk.util.SpenEventUtil.HoverButtonUp;
import com.samsung.photodesk.util.SpenEventUtil.ShowHoverPopup;
import com.samsung.photodesk.util.SpenEventUtil.TouchPenEraser;


/**
 * <p>The base of the photo desk</p>
 * Flag of folders({@link FolderFragment}) and content({@link ContentFragment}) based fragment
 * ({@ Link SPenEventLibrary}) using SPen Events Hover function, Support using gestures ({@ link createGestureDetector}) Fling (Fling)
 * @param <T> {@link FolderItem}, {@link MediaItem}
 */
abstract public class PhotoDeskFragment<T> extends SelectedFragment<T>
		implements OnTouchListener, HoverButtonUp, ShowHoverPopup, TouchPenEraser {
	
	public static final String IS_EDIT = "is_edit";
	
	protected static int FLING_NONE = 0;
	protected static int FLING_LEFT = 1;
	protected static int FLING_RIGHT = 2;
	
	SpenEventUtil mSpenEvent;
	
    private ViewGroup mCollection;
    
	private GestureDetector mGestureDetector = null;
	
    private boolean mIsRunFling = false;
    
    boolean mIsScrolling = false;
    
    abstract public int getFirstVisiblePosition();
    
    /**
     * Protection set up the menu.
     * @param menuItem {@link MenuItem}
     */
    abstract public void setPortectMenu(MenuItem menuItem);
    
    /**
     * Set up a hidden menu.
     * @param menuItem {@link MenuItem}
     */
    abstract public void setHideMenu(MenuItem menuItem);
    
    /**
     * Check protection items that it contains.
     * @param items Check the items
     * @return If any item contained true true Otherwise false
     */
    abstract public boolean isIncludeProtectedItem(ArrayList<MediaObject> items);
    
    /**
     * Results after the end of the menu, run to get listeners to handle.
     * @return {@link OnOperationListener}
     */
    abstract public OnOperationListener getMenuOperationLinstener();
    
    /**
     * Shows the details
     */
    abstract public void showDetails();
    
    /**
     * Slider Show
     */
    abstract public void startSlideShow();
    
    /**
     * Id to get the position of the item
     * @param id 
     * @return position
     */
    abstract public int findItemPosition(long id);
    
    /**
     * Hidden menu, run
     */
    abstract public void hide();
    
    /**
     * Protected cancel an item
     */
    abstract public void allUnprotectedItem();
    
    /**
     * Get a view of the child.
     * @param index index
     * @return child view
     */
    abstract public View getChildAt(int index);
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	
    	if (mCollection != null) {
    		mSpenEvent = new SpenEventUtil(this, mCollection);
        	mSpenEvent.initHoverPopup(this);
        	mSpenEvent.setOnPenEraser(this);
        	if (!isImageSelectMode())	mSpenEvent.setOnHoverUpListener(this);
        	
        	mCollection.setOnTouchListener(this);
    	}
    	
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    /**
     * Set up a collection view.
     * Collection view is the main view that contains items show ViewGroup
     * @param collection  main view
     */
    public void setCollectionView(ViewGroup collection) {
    	mCollection = collection;
    }

    @Override
    public void onStop() {
        if (mSpenEvent != null) {
        	mSpenEvent.close();
        }
        super.onStop();
    }
    
    /**
     * Get a folder view.
     * @return {@link FolderFragment}
     */
	public FolderFragment getFolderFragment() {
    	android.app.FragmentManager fm = getFragmentManager();
    	if (fm != null) {
    		return (FolderFragment)fm.findFragmentById(R.id.folderView);
    	}
    	return null;
    }
	
	/**
     * Get a content view.
     * @return {@link ContentFragment}
     */
	public ContentFragment getContentFragment() {
    	android.app.FragmentManager fm = getFragmentManager();
    	if (fm != null) {
    		return (ContentFragment)fm.findFragmentById(R.id.contentView);
    	}
    	return null;
    }
	
	/**
     * Get a threadPool.
     * @return {@link ThreadPool}
     */
	public ThreadPool getThreadPool() {
		return ((PhotoDeskApplication) getActivity().getApplication()).getThreadPool();
	}
	
	
	@Override
    public boolean onTouch(View v, MotionEvent event) {
        return getGestureDetector().onTouchEvent(event);
    }
    
	/**
	 * Get a gesture.
	 * 
	 * Gestures are used to for fling.
	 * @return {@link GestureDetector}
	 */
    public GestureDetector getGestureDetector() {
    	if (mGestureDetector == null) {
    		createGestureDetector();
    	}
    	
    	return mGestureDetector;
    }
	
	/**
     * create GestureDetector 
     * 
     * Fling left when hiding the folder view
     * Fling right when hiding the content view
     * One view so that you can see on the screen.
     */
    private void createGestureDetector() {
        final int X_MOVE_SENSITIVITY = 90;
        final int Y_MOVE_SENSITIVITY = 150;
        if (mGestureDetector != null)
            return;

        mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {

        	@Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

        	@Override
            public void onShowPress(MotionEvent e) {}

        	@Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

        	@Override
            public void onLongPress(MotionEvent e) {}

        	@Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
            
        	@Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            	if (e1 == null || e2 == null) return false;
            	
                if (Math.abs(e2.getY() - e1.getY()) > Y_MOVE_SENSITIVITY) {
                    return false;
                }
                if (Math.abs(e2.getX() - e1.getX()) > X_MOVE_SENSITIVITY) {
                    if (e2.getX() > e1.getX()) {
                        onFlingRight();
                    } else {
                        onFlingLeft();
                    }
                    
                    workingFling();
                    return true;
                }
                return false;
            }

            /**
             * Fling left when hiding the folder view
             */
            private void onFlingLeft() {
            	flingLeft();
            }

            /**
             * Fling right when hiding the content view
             */
            private void onFlingRight() {
            	flingRight();
            }
        });
    }
    
    private void workingFling() {
    	mIsRunFling = true;
		new Handler().postDelayed(new Runnable() {
            
            public void run() {
            	mIsRunFling = false;
            }
        }, 2000);
	}

    /**
     *  Fling right when hiding the content view
     */
	public void flingRight() {
        if (mIsScrolling) return;
        ContentFragment contentFragment = getContentFragment();
        if (contentFragment == null || contentFragment.isSelectedMode())
            return;
        
        FolderFragment folderFragment = getFolderFragment();
        if (folderFragment == null || folderFragment.isSelectedMode())
            return;
        
        Activity activity = getActivity();
        View folder = activity.findViewById(R.id.folderView);
        if (folder == null) return;
        if (folder.getVisibility() != View.VISIBLE) {
        	folderFragment.changeFolderView(folderFragment.getBeforeViewType());
            folder.setVisibility(View.VISIBLE);
            if (contentFragment.getViewType() == ContentFragment.VIEW_GRID) {
                ((GridContentFragment)contentFragment).initObserver();
            }
        } else {
        	View content = activity.findViewById(R.id.contentView);
            if (content != null) {
            	if (content.getVisibility() == View.GONE) {
            		return;
            	}
            	content.setVisibility(View.GONE);
            }
            
        	FolderFragment folderFrag = getFolderFragment();
        	if (folderFrag != null) {
        		folderFrag.changeFolderView(FolderFragment.VIEW_GRID);
        	}
        }
    }
    

	/**
     * Fling left when hiding the folder view
     */
    public void flingLeft() {
        if (mIsScrolling) return;
    	ContentFragment contentFragment = getContentFragment();
        if (contentFragment == null || contentFragment.isSelectedMode())
            return;
        
        FolderFragment folderFragment = getFolderFragment();
        if (folderFragment == null || folderFragment.isSelectedMode())
            return;
        
        Activity activity = getActivity();
        View content = activity.findViewById(R.id.contentView);
        View folder = activity.findViewById(R.id.folderView);
        if (folder == null || content == null) return;
        if (content.getVisibility() != View.VISIBLE) {
        	folderFragment.changeFolderView(folderFragment.getBeforeViewType());
            content.setVisibility(View.VISIBLE);
        } else {
            folder.setVisibility(View.GONE);
        }
        if (contentFragment.getViewType() == ContentFragment.VIEW_GRID) {
            ((GridContentFragment)contentFragment).initObserver();
        }
    }

	public boolean isRotationSupported() {
		return false;
	}
	
	public static int getCurrentFlingPosition(Activity activity) {
        View content = activity.findViewById(R.id.contentView);
        View folder = activity.findViewById(R.id.folderView);
        if (content.getVisibility() == View.VISIBLE && folder.getVisibility() == View.VISIBLE) {
        	return FLING_NONE;
        } else if (content.getVisibility() == View.VISIBLE) {
        	return FLING_LEFT; //current fling left
        } else if (folder.getVisibility() == View.VISIBLE) {
        	return FLING_RIGHT; //current fling right
        }
        return 0;
	}

	public boolean isRunFling() {
		return mIsRunFling;
	}

}
