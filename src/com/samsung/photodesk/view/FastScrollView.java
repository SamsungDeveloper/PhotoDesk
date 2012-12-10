/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.samsung.photodesk.view;

import java.sql.Date;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.photodesk.R;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.util.Setting;

/**
 * <p>FastScrollView class</p>
 * Custom scroll bar used by Grid ContentFragment
 *
 */
public class FastScrollView extends FrameLayout 
        implements OnScrollListener, OnHierarchyChangeListener {

    /** Scrollbar Layout */
    private RelativeLayout mScrollbar;
    /** Item's name/date View */
    private TextView mTextSection;
    
    private GridView mGridView;
    private BaseAdapter mListAdapter;

    /** first index */
    private int mVisibleTopIndex;

    /** Section Array */
    private Object [] mSections;

    /** Scrollbar Height */
    private int mScrollHeight;
    /** Scrollbar Width */
    private int mScrollWidth;
    /** Scrollbar Y position */
    private int mScrollY;

    /** Scrolling Flag */
    private boolean mScrollFlag;
    /** Scrollbar dragging Flag */
    private boolean mDragFlag;
    /** ScrollBar's Visible/gone Flag */
    private boolean mScrollVisibleFlag;    

    /** Scrollbar's Fade out handler */
    private Handler mHandler = new Handler();
    /** Scrollbar's Fade out Runnable */
    private SectionFade mSectionFade;
        
    public FastScrollView(Context context) {
        super(context);

        init(context);
    }


    public FastScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }

    public FastScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    /**
     * Interface for SectionIndexer
     *
     */
    interface SectionIndexer {
        Object[] getSections();
        
        int getPositionForSection(int section);
        
        int getSectionForPosition(int position);
    }
    
    private void setScrollbarSize() {
    	setMeasureView(mScrollbar);
    	mScrollWidth = mScrollbar.getMeasuredWidth();
        mScrollHeight = mScrollbar.getMeasuredHeight();
    }
    
    private void setMeasureView(View view) {
        ViewGroup.LayoutParams layoutParam = view.getLayoutParams();
        if (layoutParam == null)
        	layoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, layoutParam.width);
        int lpHeight = layoutParam.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(childWidthSpec, childHeightSpec);
    }
    
    public void setScrollbarPosition(int margin) {
    	FrameLayout.LayoutParams layoutParam = (FrameLayout.LayoutParams) mScrollbar.getLayoutParams();
    	layoutParam.topMargin = margin;
    	mScrollbar.setLayoutParams(layoutParam);
    }
    
    private void init(Context context) {
    	mScrollFlag = true;
        setWillNotDraw(false);
        setOnHierarchyChangeListener(this);
        mSectionFade = new SectionFade();
    }
    
    public void onScrollStateChanged(AbsListView view, int scrollState) {}
	
    public void onScroll(AbsListView view, int visibleIndex, int visibleCount, int totalCount) {
        if (totalCount - visibleCount > 0 && !mDragFlag) {
        	mScrollY = ((getHeight() - mScrollHeight) * visibleIndex) / (totalCount - visibleCount);
            setScrollbarPosition(mScrollY);
        }
        mScrollFlag = true;
        if (visibleIndex == mVisibleTopIndex) {
            return;
        }
        
        mVisibleTopIndex = visibleIndex;        
        setSectionDisplay(visibleIndex);
        startSectionAnimation(true);        
        mHandler.removeCallbacks(mSectionFade);
        if (!mDragFlag) {
            mHandler.postDelayed(mSectionFade, 1500);
        }
    }

    public void onChildViewAdded(View parent, View child) {

    	if (child instanceof GridView) {
        	mGridView = (GridView)child;
        	mGridView.setOnScrollListener(this);
        	mGridView.setOnHierarchyChangeListener(this);
        } else if(child instanceof RelativeLayout) {
        	if(child.getId() == R.id.rLScrollbar) {
        		mScrollbar = (RelativeLayout)child;
        		setScrollbarSize();
        	}
        } else if(child instanceof TextView){
        	if(child.getId() == R.id.tVSection)
        		mTextSection = (TextView)child;
        }
    }

    public void onChildViewRemoved(View parent, View child) {
        if (child == mGridView) {
        	mGridView = null;
            mListAdapter = null;
            mSections = null;
        } else if(child == mScrollbar) {
        	mScrollbar = null;
        } else if(child == mTextSection){
        	mTextSection = null;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	event.setLocation(event.getX(), event.getY() + mScrollHeight/2); // adjust Scroll bar position => Touch Y
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getX() > getWidth() - mScrollWidth
                    && event.getY() >= mScrollY  + mScrollHeight/2
                    && event.getY() <= mScrollY + mScrollHeight + mScrollHeight/2) {
            	mDragFlag = true;
                cancelFling();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mDragFlag) {
            	mDragFlag = false;
                final Handler handler = mHandler;
                handler.removeCallbacks(mSectionFade);
                handler.postDelayed(mSectionFade, 1500);
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mDragFlag) {
                final int viewHeight = getHeight();
                mScrollY = (int) event.getY() - mScrollHeight;
                if (mScrollY < 0) {
                	mScrollY = 0;
                } else if (mScrollY + mScrollHeight > viewHeight) {
                	mScrollY = viewHeight - mScrollHeight;
                }
                setScrollbarPosition(mScrollY);
                if (mScrollFlag) {
                    scrollTo((float) mScrollY / (viewHeight - mScrollHeight));
                }
                return true;
            }
        }
        
        return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScrollVisibleFlag && ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getX() > getWidth() - mScrollWidth && ev.getY() >= mScrollY &&
                    ev.getY() <= mScrollY + mScrollHeight) {
            	mDragFlag = true;
                return true;
            }            
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void scrollTo(float position) {
        int count = mGridView.getCount();
        mScrollFlag = false;
        final Object[] sections = mSections;
        if (sections != null && sections.length > 1) {
            final int nSections = sections.length;
            int section = (int) (position * nSections);
            if (section >= nSections) {
                section = nSections - 1;
            }
            final SectionIndexer baseAdapter = (SectionIndexer) mListAdapter;
            int index = baseAdapter.getPositionForSection(section);
            int nextIndex = count;
            int prevIndex = index;
            int prevSection = section;
            int nextSection = section + 1;

            if (section < nSections - 1) {
                nextIndex = baseAdapter.getPositionForSection(section + 1);
            }
            
            if (nextIndex == index) {
                while (section > 0) {
                    section--;
                     prevIndex = baseAdapter.getPositionForSection(section);
                     if (prevIndex != index) {
                         prevSection = section;
                         break;
                     }
                }
            }
            int nextNextSection = nextSection + 1;
            while (nextNextSection < nSections &&
                    baseAdapter.getPositionForSection(nextNextSection) == nextIndex) {
                nextNextSection++;
                nextSection++;
            }
            float fPrev = (float) prevSection / nSections;
            float fNext = (float) nextSection / nSections;
            index = prevIndex + (int) ((nextIndex - prevIndex) * (position - fPrev) / (fNext - fPrev));
            if (index > count - 1) index = count - 1;
            
            mGridView.setSelection(index);
        } else {
            int index = (int) (position * count);
            mGridView.setSelection(index);
        }
    }

    private void cancelFling() {
        MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mGridView.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }
    
    /**
     * Setting Section
     * @param visibleItem Item position
     */
    private void setSectionDisplay(int visibleItem) {
    	if(mGridView == null || mGridView.getCount() <= visibleItem) return;
    	try {
    	    MediaItem item = (MediaItem)mGridView.getItemAtPosition(visibleItem);
    	    if (item == null) return;
        	String section = "";
        	if (Setting.INSTANCE.getCompareMode() == Setting.COMPARE_DATE_ASC || Setting.INSTANCE.getCompareMode() == Setting.COMPARE_DATE_DESC){
        		section = item.getDateTaken();
        		if(section == null) {
        			section = "1970.01.01";
        		}else{
    	    		Date date = new Date(Long.parseLong(section));            
    	            section = DateFormat.format("yyyy.MM.dd", date).toString();
        		}
        	}else{
        		section = (item.getDisplayName().length() >= 1) ? item.getDisplayName().substring(0, 1) : ".";
        	}
            mTextSection.setText(section);
        } catch(IndexOutOfBoundsException e) {
            mTextSection.setVisibility(GONE);
        }
    }
    
	private void startSectionAnimation(boolean fadeFlag) {
		Animation alpha;
		
		if(fadeFlag) {
			if(mScrollVisibleFlag) return;
		
			mScrollVisibleFlag = true;		
			alpha = new AlphaAnimation(0.0f, 1.0f);
			mScrollbar.setVisibility(VISIBLE);
			mTextSection.setVisibility(VISIBLE);	
		} else {
			if(!mScrollVisibleFlag) return;
			
			mScrollVisibleFlag = false;			
			alpha = new AlphaAnimation(1.0f, 0.0f);
		}
		alpha.setDuration(500);
		alpha.setFillAfter(true);

		mScrollbar.startAnimation(alpha);
		mTextSection.startAnimation(alpha);
	}
	
	public class SectionFade implements Runnable {
		@Override
		public void run() {
			startSectionAnimation(false);
		}
	}
}
