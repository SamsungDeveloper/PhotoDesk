package com.samsung.photodesk;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.photodesk.util.Setting;


/**
 *  <P>Password dialog using in the signature activities </P>
 *  
 *  This dialogs use custom soft keyboard for entering password. 
 *
 */
public class PasswordDialog {
    public static final int DIALOG_NEW_PASSWORD = 0;
    public static final int DIALOG_CONFIRM_PASSWORD = 1;
    
	public Dialog mDialog;
	private int mType;

	private Context mContext;

	private Toast m_Toast = null;
	private String mFirstPassword = null;

	private String mSecondPassword = null;

	TextView mTv;
	EditText mEt1;
	EditText mEt2;
	EditText mEt3;
	EditText mEt4;

	PasswordConfirmListener mPasswordConfirmListner;
	PasswordBackKeyListener mPasswordBackKeyListner ;
	
	/**
	 * Interface for PasswordBackKeyListener
	 *
	 */
    public interface PasswordBackKeyListener {
        void onPasswordBackKey();
    }

	/**
	 * Interface for PasswordConfirmListener
	 *
	 */
	public interface PasswordConfirmListener {
	    void onPasswordConfirm();
	}
	
	public void setOnPasswordConfirmListener(PasswordConfirmListener listener) {
	    mPasswordConfirmListner = listener;
	}
	
	public void setOnPasswordBackKeyListener(PasswordBackKeyListener listener) {
		mPasswordBackKeyListner = listener;
	}
	
	public PasswordDialog(Context context, int type) {
		mContext = context;
		mType = type;
		initPasswordDialog();
	}
/**
 * Initialize password dialog
 */
	private void initPasswordDialog() {
		mDialog = new Dialog(mContext,
				android.R.style.Theme_Translucent_NoTitleBar);

		Window window = mDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		mDialog.setContentView(R.layout.password_dlg);

		mTv = (TextView) mDialog.findViewById(R.id.password_dlg_message);
		mEt1 = (EditText) mDialog.findViewById(R.id.etSecKey1);
		mEt2 = (EditText) mDialog.findViewById(R.id.etSecKey2);
		mEt3 = (EditText) mDialog.findViewById(R.id.etSecKey3);
		mEt4 = (EditText) mDialog.findViewById(R.id.etSecKey4);

		if(mType == DIALOG_NEW_PASSWORD)
		    mTv.setText(R.string.signature_input_password);
		else {
		    mTv.setText(R.string.please_input_password);
		}
		
		mDialog.setOnKeyListener( new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK) {
			           if(mPasswordBackKeyListner != null) {
			        	   mPasswordBackKeyListner.onPasswordBackKey();
			           }
				}
				return false;
			}
		});
		
		mDialog.findViewById(R.id.btnOne).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnTwo).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnThree).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnFour).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnFive).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnSix).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnSeven).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnEight).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnNine).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnZero).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnDel).setOnClickListener(mPasswordBtn);
		mDialog.findViewById(R.id.btnOk).setOnClickListener(mPasswordBtn);

	}
	
	private OnClickListener mPasswordBtn = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int code = 0;
			switch (v.getId()) {
			case R.id.btnOne:
				code = KeyEvent.KEYCODE_1;
				break;
			case R.id.btnTwo:
				code = KeyEvent.KEYCODE_2;
				break;
			case R.id.btnThree:
				code = KeyEvent.KEYCODE_3;
				break;
			case R.id.btnFour:
				code = KeyEvent.KEYCODE_4;
				break;
			case R.id.btnFive:
				code = KeyEvent.KEYCODE_5;
				break;
			case R.id.btnSix:
				code = KeyEvent.KEYCODE_6;
				break;
			case R.id.btnSeven:
				code = KeyEvent.KEYCODE_7;
				break;
			case R.id.btnEight:
				code = KeyEvent.KEYCODE_8;
				break;
			case R.id.btnNine:
				code = KeyEvent.KEYCODE_9;
				break;
			case R.id.btnZero:
				code = KeyEvent.KEYCODE_0;
				break;
			case R.id.btnDel:
			{
				EditText et1 = (EditText) mDialog
						.findViewById(R.id.etSecKey1);
				EditText et2 = (EditText) mDialog
						.findViewById(R.id.etSecKey2);
				EditText et3 = (EditText) mDialog
						.findViewById(R.id.etSecKey3);
				EditText et4 = (EditText) mDialog
						.findViewById(R.id.etSecKey4);
				
				String key1 = et1.getText().toString();
				String key2 = et2.getText().toString();
				String key3 = et3.getText().toString();
				String key4 = et4.getText().toString();					
				
				if(key4.length() > 0) {
					et4.dispatchKeyEvent(new KeyEvent(
					KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));

				}else if(key3.length() > 0) {
					et3.dispatchKeyEvent(new KeyEvent(
							KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
		
				}else if(key2.length() > 0) {
					et2.dispatchKeyEvent(new KeyEvent(
					KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));

				}else if(key1.length() > 0) {
					et1.dispatchKeyEvent(new KeyEvent(
					KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
				}

			}		
				return;
			case R.id.btnOk:
			{
			    if(mType == DIALOG_NEW_PASSWORD) {
			        CheckPassword(v, mDialog);
			    }else {
			        ConfirmPassword(v, mDialog);
			    }

			}
				return;
			}
			InputNumber(v, code, mDialog);

		}
	};
	
	public void ConfirmPassword(View v, Dialog dlg) {
        String password ;
        String key1 = mEt1.getText().toString();
        String key2 = mEt2.getText().toString();
        String key3 = mEt3.getText().toString();
        String key4 = mEt4.getText().toString();

        password = String.format(key1 + key2 + key3 + key4);

       if (Setting.INSTANCE.getPassword() == null || Setting.INSTANCE.getPassword().length() == 0) {
           sToastS(mContext.getResources().getString(R.string.password_is_not_exist));
           return;
       }
          
        
       if (Setting.INSTANCE.getPassword().equals(password)) {
           sToastS(mContext.getResources().getString(R.string.password_verification_sucess));
           if(mPasswordConfirmListner != null) {
               mPasswordConfirmListner.onPasswordConfirm();
           }
       }else {
           sToastS(mContext.getResources().getString(R.string.password_verification_failture));
           mEt1.clearFocus();
           mEt2.clearFocus();
           mEt3.clearFocus();
           mEt4.clearFocus();

           mEt1.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                   KeyEvent.KEYCODE_DEL));
           mEt2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                   KeyEvent.KEYCODE_DEL));
           mEt3.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                   KeyEvent.KEYCODE_DEL));
           mEt4.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                   KeyEvent.KEYCODE_DEL));

           mEt1.setFocusable(true);
           
       }
	}


	
	public void CheckPassword(View v, Dialog dlg) {

		String key1 = mEt1.getText().toString();
		String key2 = mEt2.getText().toString();
		String key3 = mEt3.getText().toString();
		String key4 = mEt4.getText().toString();

		if (mFirstPassword == null) {
			mFirstPassword = String.format(key1 + key2 + key3 + key4);


			if (mFirstPassword.length() != 4) {
				sToastS(mContext.getResources().getString(R.string.password_too_short));
				mFirstPassword = null;
				return;
			}
			mEt1.clearFocus();
			mEt2.clearFocus();
			mEt3.clearFocus();
			mEt4.clearFocus();
			mTv.setText(R.string.signature_input_password_again);

			mEt1.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_DEL));
			mEt2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_DEL));
			mEt3.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_DEL));
			mEt4.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_DEL));

			mEt1.setFocusable(true);

		} else {
			mSecondPassword = String.format(key1 + key2 + key3 + key4);

			if (mFirstPassword != null && mSecondPassword != null) {
				if (mFirstPassword.equals(mSecondPassword)) {
					if (mFirstPassword.length() < 4) {
						sToastS(mContext.getResources().getString(
								R.string.password_too_short));
					} else {
						Setting.INSTANCE.setPassword(mContext, mFirstPassword);

						SharedPreferences pref = mContext.getSharedPreferences(
								"password", 0);
						SharedPreferences.Editor editor = pref.edit();
						editor.putString("password", mFirstPassword);
						editor.commit();

						sToastS(mContext.getResources().getString(R.string.password_registration_sucess));

						sToastS(mContext.getResources().getString(
								R.string.signature_registration_draw));

						dlg.dismiss();
					}
				} else {
					sToastS(mContext.getResources().getString(
							R.string.passwords_dont_match));
					mSecondPassword = null;
					mTv.setText("");
					mTv.setText(R.string.signature_input_password_again);
					mEt1.clearFocus();
					mEt2.clearFocus();
					mEt3.clearFocus();
					mEt4.clearFocus();

					mEt1.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DEL));
					mEt2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DEL));
					mEt3.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DEL));
					mEt4.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_DEL));

					mEt1.setFocusable(true);

				}

			}

		}

	}

	public void InputNumber(View v, int code, Dialog dlg) {

		String key1 = mEt1.getText().toString();
		String key2 = mEt2.getText().toString();
		String key3 = mEt3.getText().toString();
		String key4 = mEt4.getText().toString();
		
		if(key1.length() == 0){
			mEt1.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
			mEt1.setFocusable(false);
			mEt2.setFocusable(true);
			mEt2.requestFocus();
			return;

		} else if (key2.length() == 0) {	
			mEt2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
			mEt2.setFocusable(false);
			mEt3.setFocusable(true);
			mEt3.requestFocus();
			return;
		} else if (key3.length() == 0) {	
			mEt3.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
			mEt3.setFocusable(false);
			mEt4.setFocusable(true);
			mEt4.requestFocus();
			return;
		} else if (key4.length() == 0) {	
			mEt4.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, code));
			mEt4.setFocusable(false);

			return;
		}

	}

	public void show() {
		mDialog.show();
	}

	public void dismiss() {
		mDialog.dismiss();
	}

	public void sToastS(String i_String) {
		m_Toast = Toast.makeText(mContext, i_String, Toast.LENGTH_SHORT);
		View rootView = m_Toast.getView();
		LinearLayout linearLayout = null;
		TextView messageTextView = null;

		// check (expected) toast layout
		if (rootView instanceof LinearLayout) {
			linearLayout = (LinearLayout) rootView;

			if (linearLayout.getChildCount() == 1) {
				View child = linearLayout.getChildAt(0);

				if (child instanceof TextView) {
					messageTextView = (TextView) child;
				}
			}
		}

		// cancel modification because toast layout is not what we expected
		if (linearLayout == null || messageTextView == null) {
			// failed to create image toast layout, using usual toast
			return;
		}

		messageTextView.setTextSize(20);

		ViewGroup.LayoutParams textParams = messageTextView.getLayoutParams();
		((LinearLayout.LayoutParams) textParams).gravity = Gravity.CENTER_VERTICAL;

		// modify root layout
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);

		m_Toast.setGravity(Gravity.TOP, 0, 0);

		m_Toast.show();


	}

}
