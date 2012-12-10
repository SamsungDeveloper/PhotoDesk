
package com.samsung.photodesk.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.samsung.photodesk.R;

/**
 * QuickMenu dialog, shows menu list as icon and text like the one in SGallery
 * app. Currently supports vertical and horizontal layout.
 */
public class QuickMenu extends PopupWindows implements OnDismissListener {
    private View mRootView;

    private ImageView mArrowUp;

    private ImageView mArrowDown;

    private LayoutInflater mInflater;

    private ViewGroup mTrack;

    private ScrollView mScroller;

    private OnActionItemClickListener mItemClickListener;

    private OnDismissListener mDismissListener;

    private List<QucikMenuItem> actionItems = new ArrayList<QucikMenuItem>();

    private boolean mDidAction;

    private int mChildPos;

    private int mInsertPos;

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
     * Constructor for default vertical layout
     * 
     * @param context Context
     */
    public QuickMenu(Context context) {
        this(context, VERTICAL);
    }

    /**
     * Constructor allowing orientation override
     * 
     * @param context Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public QuickMenu(Context context, int orientation) {
        super(context);

        mOrientation = orientation;

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mOrientation == HORIZONTAL) {
            setRootViewId(R.layout.quick_menu_popup_horizontal);
        } else {
            setRootViewId(R.layout.quick_menu_popup_vertical);
        }

        mAnimStyle = ANIM_AUTO;
        mChildPos = 0;
    }

    /**
     * Get action item at an index
     * 
     * @param index Index of item (position from callback)
     * @return Action Item at the position
     */
    public QucikMenuItem getActionItem(int index) {
        return actionItems.get(index);
    }

    /**
     * Set root view.
     * 
     * @param id Layout resource id
     */
    public void setRootViewId(int id) {
        mRootView = (ViewGroup)mInflater.inflate(id, null);
        mTrack = (ViewGroup)mRootView.findViewById(R.id.tracks);

        mArrowDown = (ImageView)mRootView.findViewById(R.id.arrow_down);
        mArrowUp = (ImageView)mRootView.findViewById(R.id.arrow_up);

        mScroller = (ScrollView)mRootView.findViewById(R.id.scroller);

        mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        setContentView(mRootView);
    }

    /**
     * Set animation style
     * 
     * @param mAnimStyle animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(int mAnimStyle) {
        this.mAnimStyle = mAnimStyle;
    }

    /**
     * Set listener for action item clicked.
     * 
     * @param listener Listener
     */
    public void setOnActionItemClickListener(OnActionItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Add action item
     * 
     * @param action {@link QucikMenuItem}
     */
    public void addActionItem(QucikMenuItem action) {
        actionItems.add(action);

        String title = action.getTitle();
        Drawable icon = action.getIcon();

        View container;

        if (mOrientation == HORIZONTAL) {
            container = mInflater.inflate(R.layout.quick_menu_item_horizontal, null);
        } else {
            container = mInflater.inflate(R.layout.quick_menu_item_vertical, null);
        }

        ImageView img = (ImageView)container.findViewById(R.id.iv_icon);
        TextView text = (TextView)container.findViewById(R.id.tv_title);

        if (icon != null) {
            img.setImageDrawable(icon);
        } else {
            img.setVisibility(View.GONE);
        }

        img.setPadding(2, 0, 2, 0);
        if (title != null) {
            text.setText(title);
        } else {
            text.setVisibility(View.GONE);
        }

        final int pos = mChildPos;
        final int actionId = action.getActionId();

        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(QuickMenu.this, pos, actionId);
                }

                if (!getActionItem(pos).isSticky()) {
                    mDidAction = true;

                    dismiss();
                }
            }
        });

        mTrack.addView(container, mInsertPos);

        mChildPos++;
        mInsertPos++;
    }

    @Override
    protected void preShow() {
        super.preShow();
        mWindow.setFocusable(false);
    }
    
    /**
     * Show quickmenu popup. Popup is automatically positioned, on top or bottom
     * of anchor view.
     */
    public void show(View anchor) {
        preShow();

        int xPos, yPos, arrowPos;

        mDidAction = false;

        int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(),
                location[1] + anchor.getHeight());

        mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = mRootView.getMeasuredHeight();

        if (rootWidth == 0) {
            rootWidth = mRootView.getMeasuredWidth();
        }

        Point outSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(outSize);
        int screenWidth = outSize.x;
        int screenHeight = outSize.y;

        // automatically get X coord of popup (top left)
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

        boolean onTop = (dyTop > dyBottom) ? true : false;

        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                LayoutParams l = mScroller.getLayoutParams();
                l.height = dyTop - anchor.getHeight();
            } else {
                yPos = anchorRect.top - rootHeight;
            }
        } else {
            yPos = anchorRect.bottom -10;
            if(yPos < 0 ) yPos =1;

            if (rootHeight > dyBottom) {
                LayoutParams l = mScroller.getLayoutParams();
                l.height = dyBottom;
            }
        }

        showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Set animation style
     * 
     * @param screenWidth screen width
     * @param requestedX distance from left edge
     * @param onTop flag to indicate where the popup should be displayed. Set
     *            TRUE if displayed on top of anchor view and vice versa
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;

        switch (mAnimStyle) {
            case ANIM_GROW_FROM_LEFT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownWindow_Left
                        : R.style.Animations_PopDownWindow_Left);
                break;

            case ANIM_GROW_FROM_RIGHT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownWindow_Right
                        : R.style.Animations_PopDownWindow_Right);
                break;

            case ANIM_GROW_FROM_CENTER:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownWindow_Center
                        : R.style.Animations_PopDownWindow_Right);
                break;

            case ANIM_REFLECT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownWindow_Reflect
                        : R.style.Animations_PopDownWindow_Reflect);
                break;

            case ANIM_AUTO:
                if (arrowPos <= screenWidth / 4) {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownWindow_Left
                            : R.style.Animations_PopDownWindow_Left);
                } else if (arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownWindow_Center
                            : R.style.Animations_PopDownWindow_Center);
                } else {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopDownWindow_Right
                            : R.style.Animations_PopDownWindow_Right);
                }

                break;
        }
    }

    /**
     * Show arrow
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
     * Set listener for window dismissed. This listener will only be fired if
     * the quickmenu dialog is dismissed by clicking outside the dialog or
     * clicking on sticky item.
     */
    public void setOnDismissListener(QuickMenu.OnDismissListener listener) {
        setOnDismissListener(this);

        mDismissListener = listener;
    }

    @Override
    public void onDismiss() {
        if (!mDidAction && mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    /**
     * Listener for item click
     */
    public interface OnActionItemClickListener {
        public abstract void onItemClick(QuickMenu source, int pos, int actionId);
    }

    /**
     * Listener for window dismiss
     */
    public interface OnDismissListener {
        public abstract void onDismiss();
    }
}
