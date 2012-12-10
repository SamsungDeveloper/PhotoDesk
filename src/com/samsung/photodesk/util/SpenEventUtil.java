package com.samsung.photodesk.util;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.samsung.photodesk.PhotoDeskFragment;
import com.samsung.photodesk.view.HoverPopupWindows;
import com.samsung.photodesk.view.QuickMenu;
import com.samsung.spen.lib.input.SPenEventLibrary;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

/**
 * <p>S pen event class</p>
 * Managing S pen event.
 * Pen hover event , Pen hover button event , Pen touch event , Pen erase event
 * @author Administrator
 *
 */
public class SpenEventUtil implements SPenTouchListener, SPenHoverListener{
	
	private ViewGroup mCollection;
	private PhotoDeskFragment<?> mPhotoDesk;
	
	public SPenEventLibrary mSPenEventLibrary = new SPenEventLibrary();

	/** Represents an invalid child index */
	public static final int INVALID_INDEX = -1;

	int mHoverState = HOVER_POPUP_STATE_OFF;

	/** Hover popup state */
	static final int HOVER_POPUP_STATE_OFF = 0;

	static final int HOVER_POPUP_STATE_ON = 1;

	/** The adaptor position of the first visible item */
	int mFirstItemPosition;

	HoverPopupWindows mHoverPopup;

	int mCurrentHoverPosition;

	View mCurrentView;

	QuickMenu mQuickMenu;

	Runnable mLongHoverRunnable;

	private Rect mRect;
	
	HoverButtonUp mHoverBtnUp;
	TouchPenEraser mTouchPenEraser;
	ShowHoverPopup mShowHoverPopup;
	
	/**
	 * <p>Constructor</p>
	 * @param photoDeskFragment {@link PhotoDeskFragment}
	 * @param collection {@link ViewGroup}
	 */
	public SpenEventUtil(PhotoDeskFragment<?> photoDeskFragment, ViewGroup collection) {
		mCollection = collection;
		mPhotoDesk  = photoDeskFragment;
		
		mSPenEventLibrary.setSPenTouchListener(mCollection, this);
        mSPenEventLibrary.setSPenHoverListener(mCollection, this);
        
		
	}

	/**
	 * <p>Hover event initialize</p>
	 * @param hoverPopup {@link ShowHoverPopup}
	 */
	public void initHoverPopup(ShowHoverPopup hoverPopup) {
		mHoverPopup = new HoverPopupWindows(mCollection.getContext(), HoverPopupWindows.VERTICAL, 1);
		mShowHoverPopup = hoverPopup;
	}
	
	/**
	 * <p>Close dialog</p>
	 */
	public void close() {
		try {
            if (mHoverPopup != null)
            	mHoverPopup.dismiss();
            
            if (mQuickMenu != null)
                mQuickMenu.dismiss();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * <p>Hover button event interface</p>
	 */
	public interface HoverButtonUp {
		public void onHoverButtonUp(View view, MotionEvent event);
	}
	
	/**
	 * <p>Pen erase event interface</p>
	 */
	public interface TouchPenEraser {
		public boolean onTouchPenEraser(View view, MotionEvent event);
	}
	
	/**
	 * <p>Hover event popup dialog interface</p>
	 */
	public interface ShowHoverPopup {
		public void showHoverPopup(final int position, final View v);
	}
	
	@Override
	public void onTouchButtonDown(View view, MotionEvent event) {}

	@Override
	public void onTouchButtonUp(View view, MotionEvent event) {}
	
	@Override
	public void onHoverButtonDown(View view, MotionEvent event) {}

	/**
	 * <p>Hover button event</p>
	 * Set hover button up event
	 */
	@Override
	public void onHoverButtonUp(View view, MotionEvent event) {
		if (mHoverBtnUp != null) {
			mHoverBtnUp.onHoverButtonUp(view, event);
		}
	}

	@Override
	public boolean onTouchFinger(View view, MotionEvent event) {
		return false;
	}

	@Override
	public boolean onTouchPen(View view, MotionEvent event) {
		return false;
	}

	/**
	 * <p>Pen erase event</p>
	 * Set pen erase event
	 */
	@Override
	public boolean onTouchPenEraser(View view, MotionEvent event) {
		if (mTouchPenEraser != null) {
			return mTouchPenEraser.onTouchPenEraser(view, event);
		}
		return true;
	}

	/**
	 * <p>Hover event</p>
	 * Handle for hover event.
	 */
	@Override
	public boolean onHover(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			startHover(event);
			return true;

		case MotionEvent.ACTION_HOVER_MOVE:
			moveHover(event);
			return true;

		case MotionEvent.ACTION_HOVER_EXIT:
			endHover();
			break;

		default:
			break;
		}

		return false;
	}
	
	/**
	 * <p>Get contain child index.</p>
	 * @param x
	 * @param y
	 * @return contain child index.
	 */
    public int getContainingChildIndex(final int x, final int y) {
        if (mRect == null) {
            mRect = new Rect();
        }

        for (int index = 0; index < mCollection.getChildCount(); index++) {
        	mCollection.getChildAt(index).getHitRect(mRect);
            if (mRect.contains(x, y)) {
                return index;
            }
        }
        return INVALID_INDEX;
    }
    
    /**
     * <p>Check long hover event</p>
     * Handle for hover popup display and short cut menu display.
     */
    private void startLongHoverCheck() {
        if (mLongHoverRunnable == null) {
            mLongHoverRunnable = new Runnable() {
                public void run() {

                    if (mHoverState == HOVER_POPUP_STATE_ON) {
                        if (mCurrentHoverPosition != INVALID_INDEX && mCurrentView != null) {

                            if (mPhotoDesk.getActivity() == null )
                                   return;
                            
                            if (mQuickMenu != null) {
                                if(mQuickMenu.isShowing())
                                    return;
                            }
                                
                            if (mShowHoverPopup != null) {
                            	mShowHoverPopup.showHoverPopup(mCurrentHoverPosition, mCurrentView);
                            }
                        }
                    }
                }
            };
        }
        mCollection.postDelayed(mLongHoverRunnable, ViewConfiguration.getLongPressTimeout());
    }
    
    /**
     * <p>start hover</p>
     * @param event {@link MotionEvent}
     */
	private void startHover(final MotionEvent event) {
		final int index = getContainingChildIndex((int) event.getX(), (int) event.getY());

		if (mPhotoDesk.isSelectedMode())
			return;

		if (index != INVALID_INDEX) {

			mFirstItemPosition = mPhotoDesk.getFirstVisiblePosition();
			final int position = mFirstItemPosition + index;

			if (mHoverState == HOVER_POPUP_STATE_OFF) {
				mHoverState = HOVER_POPUP_STATE_ON;
				mCurrentHoverPosition = position;
				mCurrentView = mCollection.getChildAt(index);

				startLongHoverCheck();

			}
		} else { 
			mCurrentHoverPosition = -1;
			mCurrentView = null;
			mHoverState = HOVER_POPUP_STATE_OFF;
		}
	}
	
    /**
     * <p>move hover</p>
     * @param event {@link MotionEvent}
     */
	private void moveHover(final MotionEvent event) {
		final int index = getContainingChildIndex((int) event.getX(), (int) event.getY());

		if (index != INVALID_INDEX) {
			mFirstItemPosition = mPhotoDesk.getFirstVisiblePosition();
			final int position = mFirstItemPosition + index;

			if (mHoverState == HOVER_POPUP_STATE_OFF) {
				if (mCurrentHoverPosition != position) {
					mHoverState = HOVER_POPUP_STATE_ON;
					mCurrentHoverPosition = position;
					mCurrentView = mCollection.getChildAt(index);
					startLongHoverCheck();
				}

			} else if (mHoverState == HOVER_POPUP_STATE_ON) {

				if (mCurrentHoverPosition != position) {

					if (mCurrentView != null) {
						if (mHoverPopup.isShowing()) {
							mHoverPopup.dismiss();

						}
						if (mLongHoverRunnable != null) {
							mCollection.removeCallbacks(mLongHoverRunnable);
						}
						mCurrentView = null;
						mHoverState = HOVER_POPUP_STATE_OFF;
					}
				}

			}

		} else { 
			mCurrentHoverPosition = INVALID_INDEX;

			if (mCurrentView != null) {
				if (mHoverPopup.isShowing()) {
					mHoverPopup.dismiss();
				}
				if (mLongHoverRunnable != null) {
					mCollection.removeCallbacks(mLongHoverRunnable);
				}
				mCurrentView = null;
				mHoverState = HOVER_POPUP_STATE_OFF;
			}

		}
	}

	/**
	 * <p>end hover</p>
	 */
	private void endHover() {
		if (mCurrentView != null) {
			if (mHoverPopup.isShowing()) {
				mHoverPopup.dismiss();
			}
			if (mLongHoverRunnable != null) {
				mCollection.removeCallbacks(mLongHoverRunnable);
			}
			mCurrentHoverPosition = -1;
			mCurrentView = null;
			mHoverState = HOVER_POPUP_STATE_OFF;
		}
	}

	/**
	 * <p>Set hover button up listener</p>
	 * @param hoverBntUpListener - listener
	 */
	public void setOnHoverUpListener(HoverButtonUp hoverBntUpListener) {
		mHoverBtnUp = hoverBntUpListener;
	}
	
	/**
	 * <p>Set pen erase listener</p>
	 * @param touchPenEraser - listener
	 */
	public void setOnPenEraser(TouchPenEraser touchPenEraser) {
		mTouchPenEraser = touchPenEraser;
	}

	/**
	 * <p>Quick menu dialog</p>
	 * Show quick menu dialog.
	 * @param view {@link View}
	 */
	public void showQuickMenu(View view) {
		close();
		if (mQuickMenu == null) return;
		mQuickMenu.show(view);

		mQuickMenu.setOnDismissListener(new QuickMenu.OnDismissListener() {
			@Override
			public void onDismiss() {
				mPhotoDesk.deselectItems();
				mPhotoDesk.notifyDataSetChanged();
				mQuickMenu = null;
			}
		});
	}

	/**
	 * <p>Content hover popup</p>
	 * Show content hover popup.
	 * @param message - information
	 * @param v - {@link View}
	 */
	public void showContentHoverPopup(String message, View v) {
		mHoverPopup.setImageDetailInfo(message);
		mHoverPopup.show(v);
	}
	
	/**
	 * <p>Folder hover popup</p>
	 * Show folder hover popup.
	 * @param Foldername
	 * @param imagecount
	 * @param videocount
	 * @param v - {@link View}
	 */
	public void showFolderHoverPopup(String Foldername, int imagecount, int videocount, View v) {
		mHoverPopup.setFolderDetailInfo(Foldername, imagecount, videocount);
		mHoverPopup.show(v);
	}

	/**
	 * <p>Get quick menu</p>
	 * @return quick menu
	 */
	public QuickMenu getQuickMenu() {
		if (mQuickMenu == null) {
			mQuickMenu = new QuickMenu(mPhotoDesk.getActivity(), QuickMenu.HORIZONTAL);
		}else {
			mQuickMenu.dismiss();
			mQuickMenu = new QuickMenu(mPhotoDesk.getActivity(), QuickMenu.HORIZONTAL);
		}
		return mQuickMenu;
	}

	

	
}
