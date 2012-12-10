
package com.samsung.photodesk.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.samsung.photodesk.R;


/**
 *     for display information popup on the content view or folder view Grid View.
 */

@SuppressLint("NewApi")
public class HoverPopupWindows extends PopupWindows implements OnDismissListener {
    private View mRootView;

    private ImageView mArrowUp;

    private ImageView mArrowDown;

    private LayoutInflater mInflater;

    private OnDismissListener mDismissListener;

    private TextView mImageText;

    private boolean mDidAction;

    private int mAnimStyle;

    private int mOrientation;

    private int rootWidth = 0;

    public static final int HORIZONTAL = 0;

    public static final int VERTICAL = 1;

    public static final int ANIM_GROW_FROM_LEFT = 1;

    public static final int ANIM_GROW_FROM_RIGHT = 2;

    public static final int ANIM_GROW_FROM_CENTER = 3;

    public static final int ANIM_REFLECT = 4;

    public static final int ANIM_AUTO = 5;

    /**
     *  HoverPopupWindows default constructor  
     * 
     * @param context Context
     */
    public HoverPopupWindows(Context context) {
        this(context, VERTICAL);

    }

    /**
     *  HoverPopupWindows constructor  
     * 
     * @param context Context
     * @param orientation layout (HORIZONTAL, VERTICAL)
     */
    public HoverPopupWindows(Context context, int orientation) {
        super(context);

        mOrientation = orientation;

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mOrientation == HORIZONTAL) {
            setRootViewId(R.layout.popup_horizontal);

        } else {
            setRootViewId(R.layout.popup_vertical);
        }

        mAnimStyle = ANIM_AUTO;
    }

    /**
     * HoverPopupWindows constructor  
     * 
     * @param context Context
     * @param orientation HORIZONTAL - 0, VERTICAL - 1 
     */
  
    public HoverPopupWindows(Context context, int orientation , int type) {
        super(context);

        mOrientation = orientation;

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (type ==0) {
            if (mOrientation == HORIZONTAL) {
                setRootViewId(R.layout.popup_horizontal);
    
            } else {
                setRootViewId(R.layout.popup_vertical);
            }
        } else if(type == 1) {
            if (mOrientation == HORIZONTAL) {
                setRootViewId(R.layout.folder_popup_horizontal);
    
            } else {
                setRootViewId(R.layout.folder_popup_vertical);
            }
            
        }
        mAnimStyle = ANIM_AUTO;
    }
    
    /**
     * set root view
     * 
     * @param id Layout resource id
     */
    public void setRootViewId(int id) {
        mRootView = (ViewGroup)mInflater.inflate(id, null);

        mArrowDown = (ImageView)mRootView.findViewById(R.id.arrow_down);
        mArrowUp = (ImageView)mRootView.findViewById(R.id.arrow_up);

        mImageText = (TextView)mRootView.findViewById(R.id.popuptext);
        

        mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        setContentView(mRootView);
    }

    /**
     * set image Exif information to textview of popup.
     * 
     * @param fileinfo
     */
    public void setImageDetailInfo(String fileinfo) {

        mImageText.setText(fileinfo);
    }

    

    /**
     * set folder information to textview of popup
     * 
     * @param fileinfo
     */
    public void setFolderDetailInfo(String Foldername, int imagecount, int videocount) {

        
        String folderInfo = " "+mContext.getResources().getString(R.string.folder_name)+ " :" + Foldername;
        folderInfo += "\n "+mContext.getResources().getString(R.string.number_of_images)+ " :" + imagecount ;
        folderInfo += "\n "+mContext.getResources().getString(R.string.number_of_video)+ " :" + videocount;
        
        mImageText.setText(folderInfo);
    }
    
    /**
     *  show popup after determine location of popup
     * 
     * @param anchor - View object hover event has occurred.
     */
    @TargetApi(13)
    public void show(View anchor) {
        preShow();

        int xPos, yPos, arrowPos;

        mDidAction = false;

        int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(),
                location[1] + anchor.getHeight());

        mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = mRootView.getMeasuredHeight();

        if (rootWidth == 0) {
            rootWidth = mRootView.getMeasuredWidth();
        }

        Point outSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(outSize);
        int screenWidth = outSize.x;
        int screenHeight = outSize.y;

        if ((anchorRect.left + rootWidth) > screenWidth) {
            xPos = anchorRect.left - (rootWidth - anchor.getWidth());
            xPos = (xPos < 0) ? 0 : xPos;
            arrowPos = anchorRect.centerX() - xPos;

        } else {
            if (anchor.getWidth() > rootWidth) {
                xPos = anchorRect.centerX() - (rootWidth / 2);
            } else {
                xPos = anchorRect.left;
            }
            arrowPos = anchorRect.centerX() - xPos;

        }

        int dyTop = anchorRect.top;
        int dyBottom = screenHeight - anchorRect.bottom;

        boolean onTop = (dyTop > rootHeight +50) ? true : false;

        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
            } else {
                yPos = anchorRect.top - rootHeight;
            }
        } else {
            yPos = anchorRect.bottom;

            if (rootHeight > dyBottom) {

            }
        }

        showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * set animation style when popup display.
     * 
     * @param screenWidth
     * @param requestedX
     * @param onTop
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;

        switch (mAnimStyle) {
            case ANIM_GROW_FROM_LEFT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpWindow_Left
                        : R.style.Animations_PopUpWindow_Left);
                break;

            case ANIM_GROW_FROM_RIGHT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpWindow_Right
                        : R.style.Animations_PopUpWindow_Right);
                break;

            case ANIM_GROW_FROM_CENTER:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpWindow_Center
                        : R.style.Animations_PopUpWindow_Center);
                break;

            case ANIM_REFLECT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpWindow_Reflect
                        : R.style.Animations_PopUpWindow_Reflect);
                break;

            case ANIM_AUTO:
                if (arrowPos <= screenWidth / 4) {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpWindow_Left
                            : R.style.Animations_PopUpWindow_Left);
                } else if (arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpWindow_Center
                            : R.style.Animations_PopUpWindow_Center);
                } else {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpWindow_Right
                            : R.style.Animations_PopUpWindow_Right);
                }

                break;
        }
    }

    /**
     *  set listener for dismiss popup
     */
    public void setOnDismissListener(HoverPopupWindows.OnDismissListener listener) {
        setOnDismissListener(this);

        mDismissListener = listener;
    }

    @Override
    public void onDismiss() {
        if (!mDidAction && mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    @Override
    protected void preShow() {
        super.preShow();

        mWindow.setTouchable(false);
        mWindow.setFocusable(false);
    }
    /**
     * 
     * Display arrow image of popup 
     * 
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow
                .getLayoutParams();

        param.leftMargin = requestedX - arrowWidth / 2;

        hideArrow.setVisibility(View.INVISIBLE);
    }

    /**
     * Listener for window dismiss
     */
    public interface OnDismissListener {
        public abstract void onDismiss();
    }
}
