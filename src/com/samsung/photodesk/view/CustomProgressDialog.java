/*
 * Copyright (C) 2007 The Android Open Source Project
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
 * limitations under the License.
 */

package com.samsung.photodesk.view;

import java.text.NumberFormat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.photodesk.R;

/**
 * <p>A custom dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.</p>
 * <p>The dialog can be made cancelable on back key press.</p>
 * <p>The progress range is 0..10000.</p>
 */


public class CustomProgressDialog extends Dialog {
    /** Creates a ProgressDialog with a circular, spinning progress
     * bar. This is the default.
     */

    public static final int STYLE_SPINNER = 0;
    /** Creates a ProgressDialog with a horizontal progress bar.
     */
    public static final int STYLE_HORIZONTAL = 1;
    
    private Context mContext;
    private TextView mTitle;
    private CharSequence mTitleChar;
    private ProgressBar mProgress;
    private TextView mMessageView;
    private Button mButton;
    private RelativeLayout mRLBtnGroup;
    
    private String mButtonText;
    private android.view.View.OnClickListener mbuttonListener;
    
    private int mProgressStyle = STYLE_SPINNER;
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    
    private int mMax;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private Drawable mProgressDrawable;
    private Drawable mIndeterminateDrawable;
    private CharSequence mMessage;
    private boolean mIndeterminate;

    private boolean mHasStarted;
    private Handler mViewUpdateHandler;
    
    public CustomProgressDialog(Context context) {
        this(context, android.R.style.Theme_Translucent_NoTitleBar);
    }

    public CustomProgressDialog(Context context, int theme) {
        
        super(context, theme);
        mContext = context;
        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
        
        initFormats();
    }

    private void initFormats() {
        mProgressNumberFormat = "%1d/%2d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }
    
    public static CustomProgressDialog show(Context context, CharSequence title,
            CharSequence message) {
        return show(context, title, message, false);
    }

    public static CustomProgressDialog show(Context context, CharSequence message) {
        return show(context, message, false);
    }

    public static CustomProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static CustomProgressDialog show(Context context,
            CharSequence message, boolean indeterminate) {
        return show(context, message, indeterminate, false, null);
    }

    public static CustomProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static CustomProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {
    	
        CustomProgressDialog dialog = new CustomProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.setButton(R.string.cancel, (android.view.View.OnClickListener) cancelListener);
        dialog.show();
        return dialog;
    }

    public static CustomProgressDialog show(Context context,
            CharSequence message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {
    	
        CustomProgressDialog dialog = new CustomProgressDialog(context);
        dialog.setMessage(message);
        dialog.setTitle("");
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.setButton(R.string.cancel, (android.view.View.OnClickListener) cancelListener);
        dialog.show();
        return dialog;
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (mProgressStyle == STYLE_HORIZONTAL) {
            
            /* Use a separate handler to update the text views as they
             * must be updated on the same thread that created them.
             */
            mViewUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    
                    /* Update the number and percent */
                    int progress = mProgress.getProgress();
                    int max = mProgress.getMax();
                    if (mProgressNumberFormat != null) {
                        String format = mProgressNumberFormat;
                        mProgressNumber.setText(String.format(format, progress, max));
                    } else {
                        mProgressNumber.setText("");
                    }
                    if (mProgressPercentFormat != null) {
                        double percent = (double) progress / (double) max;
                        SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                        tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                                0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mProgressPercent.setText(tmp);
                    } else {
                        mProgressPercent.setText("");
                    }
                }
            };
            View view = inflater.inflate( R.layout.custom_alert_dialog_progress, null);
            mTitle = (TextView) view.findViewById(R.id.tvTitle);
            mMessageView = (TextView) view.findViewById(R.id.message);
            mProgress = (ProgressBar) view.findViewById(R.id.progress);
            mProgressNumber = (TextView) view.findViewById(R.id.progress_number);
            mProgressPercent = (TextView) view.findViewById(R.id.progress_percent);
            mButton = (Button) view.findViewById(R.id.cancelbutton);
            mRLBtnGroup = ( RelativeLayout)view.findViewById(R.id.RLBtnGroup);
            
            setContentView(view);
        } else {
            View view = inflater.inflate( R.layout.custom_progress_dialog, null);
            mTitle = (TextView) view.findViewById(R.id.tvTitle);
            mProgress = (ProgressBar) view.findViewById(R.id.progress);
            mMessageView = (TextView) view.findViewById(R.id.message);
            setContentView(view);
        }
        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
        if (mSecondaryProgressVal > 0) {
            setSecondaryProgress(mSecondaryProgressVal);
        }
        if (mIncrementBy > 0) {
            incrementProgressBy(mIncrementBy);
        }
        if (mIncrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(mIncrementSecondaryBy);
        }
        if (mProgressDrawable != null) {
            setProgressDrawable(mProgressDrawable);
        }
        if (mIndeterminateDrawable != null) {
            setIndeterminateDrawable(mIndeterminateDrawable);
        }
        if (mMessage != null) {
            setMessage(mMessage);
        }
        if(mTitleChar != null) {
            setTitle(mTitleChar);
        }
        if(mButtonText != null  && mProgressStyle == STYLE_HORIZONTAL ) {
        	setButton(mButtonText, mbuttonListener);
        }
        
        
        setIndeterminate(mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }

    public void setProgress(int value) {
        if (mHasStarted) {
            mProgress.setProgress(value);
            onProgressChanged();
        } else {
            mProgressVal = value;
        }
    }

    public void setSecondaryProgress(int secondaryProgress) {
        if (mProgress != null) {
            mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
        } else {
            mSecondaryProgressVal = secondaryProgress;
        }
    }

    public int getProgress() {
        if (mProgress != null) {
            return mProgress.getProgress();
        }
        return mProgressVal;
    }

    public int getSecondaryProgress() {
        if (mProgress != null) {
            return mProgress.getSecondaryProgress();
        }
        return mSecondaryProgressVal;
    }

    public int getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }

    public void setMax(int max) {
        if (mProgress != null) {
            mProgress.setMax(max);
            onProgressChanged();
        } else {
            mMax = max;
        }
    }

    public void incrementProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementBy += diff;
        }
    }

    public void incrementSecondaryProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementSecondaryBy += diff;
        }
    }

    public void setProgressDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setProgressDrawable(d);
        } else {
            mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setIndeterminateDrawable(d);
        } else {
            mIndeterminateDrawable = d;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        } else {
            mIndeterminate = indeterminate;
        }
    }

    public boolean isIndeterminate() {
        if (mProgress != null) {
            return mProgress.isIndeterminate();
        }
        return mIndeterminate;
    }

    public void setMessage(CharSequence message) {
        
        
        if (mProgress != null) {
             mMessageView.setText(message);

        } else {
            mMessage = message;
        }
    }
    
    public void setProgressStyle(int style) {
        mProgressStyle = style;
    }

    /**
     * Change the format of the small text showing current and maximum units
     * of progress.  The default is "%1d/%2d".
     * Should not be called during the number is progressing.
     * @param format A string passed to {@link String#format String.format()};
     * use "%1d" for the current number and "%2d" for the maximum.  If null,
     * nothing will be shown.
     */
    public void setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
        onProgressChanged();
    }

    /**
     * Change the format of the small text showing the percentage of progress.
     * The default is
     * {@link NumberFormat#getPercentInstance() NumberFormat.getPercentageInstnace().}
     * Should not be called during the number is progressing.
     * @param format An instance of a {@link NumberFormat} to generate the
     * percentage text.  If null, nothing will be shown.
     */
    public void setProgressPercentFormat(NumberFormat format) {
        mProgressPercentFormat = format;
        onProgressChanged();
    }
    
    private void onProgressChanged() {
        if (mProgressStyle == STYLE_HORIZONTAL) {
            if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
                mViewUpdateHandler.sendEmptyMessage(0);
            }
        }
    }
    
    @Override
    public void setTitle(CharSequence title) {
    	if (mProgress != null && (title == null || title.equals(""))) {
        	findViewById(R.id.lLTitleBar).setVisibility(View.GONE);
        	return;
        }
    	
        if (mProgress != null) {
            mTitle.setText(title);

       } else {
           mTitleChar = title;
       }
    }

    
    public void setButton(int TextId, android.view.View.OnClickListener buttonListener) {
        setButton(mContext.getResources().getString(TextId), buttonListener);
    }
    
    public void setButton(String buttonText, android.view.View.OnClickListener buttonListener) {
        if (mProgress != null) {
            mRLBtnGroup.setVisibility(View.VISIBLE);
            mButton.setVisibility(View.VISIBLE);
            mButton.setText(buttonText);
            if(buttonListener == null) buttonListener = mCloseListener; 
            mButton.setOnClickListener(buttonListener);
            
        }else {
            mButtonText = buttonText;
            mbuttonListener = buttonListener;
        }
        

    }
    
    android.view.View.OnClickListener mCloseListener = new android.view.View.OnClickListener() {

        @Override
        public void onClick(View v) {
            dismiss();
        }
    
    };

}
