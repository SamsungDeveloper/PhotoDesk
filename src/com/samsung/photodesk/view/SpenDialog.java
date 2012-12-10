package com.samsung.photodesk.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsung.photodesk.R;

/**
 * <p>Custom dialog class</p>
 * Custom dialog used by Photo Desk
 *
 */
public class SpenDialog extends Dialog {

    public static final  int CUSTOM_COMMON_DIALOG =0;
    public static final int CUSTOM_FOLDER_DIALOG =1;
    public static final int CUSTOM_INPUT_DIALOG =2;    
    
	private Context mContext;
	private TextView mTitle;
	private Button mBtnLeft;
	private Button mBtnRight;
	private LinearLayout mSummaryView;
	@SuppressWarnings("unused")
	private LinearLayout mLLDialogView;
	
	private int mWindowType;

	protected SpenDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}
	
    public SpenDialog(Context context, int theme) {
        super(context, theme);
		super.setContentView(R.layout.spen_dialog);
		mContext = context;
		mTitle = (TextView) findViewById(R.id.tvTitle);
		mBtnLeft = (Button) findViewById(R.id.btnLeft);
		mBtnRight = (Button) findViewById(R.id.btnRight);
		mSummaryView = (LinearLayout) findViewById(R.id.lLSummary);		
		mLLDialogView = (LinearLayout) findViewById(R.id.lLSdialog);		
    }

    public SpenDialog(Context context) {
        this(context, R.style.SpenDialog);
    }
	
	@Override
	public void setContentView(int layoutResId) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View child = inflater.inflate(layoutResId, mSummaryView);
        setContentView(child);
	}
	
	@Override
	public void setContentView(View view) {
        mSummaryView.addView(view);
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
        mSummaryView.addView(view, params);
	}
	
	public void setContentView(String summary, float textSize) {
	    setSummaryGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View child = inflater.inflate(R.layout.dialog_base_textview, mSummaryView);
        TextView textView = (TextView) child.findViewById(R.id.tvSummary);
        textView.setText(summary);
	}
	
	/**
	 * Setting title icon
	 * @param drawableResId Icon resource ID
	 * @param title Title
	 */
	public void setIcon(int drawableResId, CharSequence title){
	    ImageView icon = (ImageView) findViewById(R.id.ivIcon);
	    icon.setImageResource(drawableResId);
	    icon.setVisibility(View.VISIBLE);
	    setTitle(title);
	}
	
	/**
	 * Setting content view gravity
	 * @param gravity
	 */
	public void setSummaryGravity(int gravity){
	    mSummaryView.setGravity(gravity);
	}
	
	@Override
	public void setTitle(CharSequence title) {
	    findViewById(R.id.lLTitleBar).setVisibility(View.VISIBLE);
	    mTitle.setVisibility(View.VISIBLE);
		mTitle.setText(title);
	}

	/**
	 * Setting left button
	 * @param leftTextId Button title
	 * @param leftListener {@link android.view.View.OnClickListener}
	 */
	public void setLeftBtn(int leftTextId, android.view.View.OnClickListener leftListener) {
		setLeftBtn(mContext.getResources().getString(leftTextId), leftListener);
	}
	
	/**
	 * Setting right button
	 * @param rightTextId Button title
	 * @param rightListener {@link android.view.View.OnClickListener}
	 */
	public void setRightBtn(int rightTextId, android.view.View.OnClickListener rightListener) {
		setRightBtn(mContext.getResources().getString(rightTextId), rightListener);
	}

    /**
     * Setting right button
     * @param leftText Button title
     * @param rightListener {@link android.view.View.OnClickListener}
     */
	public void setLeftBtn(String leftText, android.view.View.OnClickListener leftListener) {
	    findViewById(R.id.lLBtnGroup).setVisibility(View.VISIBLE);
	    mBtnLeft.setVisibility(View.VISIBLE);
		mBtnLeft.setText(leftText);
		if(leftListener == null) leftListener = mCloseListener; 
		mBtnLeft.setOnClickListener(leftListener);
	}

    /**
     * Setting right button
     * @param rightText Button title
     * @param rightListener {@link android.view.View.OnClickListener}
     */
	public void setRightBtn(String rightText, android.view.View.OnClickListener rightListener) {
        findViewById(R.id.lLBtnGroup).setVisibility(View.VISIBLE);
        mBtnRight.setVisibility(View.VISIBLE);
		mBtnRight.setText(rightText);
		if(rightListener == null) rightListener = mCloseListener;
		mBtnRight.setOnClickListener(rightListener);
	}
	
	/**
	 * Dialog type
	 * @return Dialog type
	 */
	public int getmWindowType() {
        return mWindowType;
    }
	
	public void initContentView() {
		if (mSummaryView.getChildCount() > 0)	mSummaryView.removeAllViewsInLayout();
	}

    public void setmWindowType(int mWindowType) {
        this.mWindowType = mWindowType;
        
        switch(mWindowType) {
            case CUSTOM_INPUT_DIALOG: {
                Window window = this.getWindow();
                window.setGravity(Gravity.TOP);
                window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
                
                WindowManager.LayoutParams params = this.getWindow().getAttributes();

                params.y = (int)mContext.getResources().getDimension(R.dimen.input_dialog_height);
                params.gravity = Gravity.TOP ;       
                this.getWindow().setAttributes(params); 

                
                break;
            }     
        }
        
    }

    android.view.View.OnClickListener mCloseListener = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			dismiss();
		}
    
	};
}
