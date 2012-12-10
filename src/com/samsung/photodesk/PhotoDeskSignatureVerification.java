
package com.samsung.photodesk;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.photodesk.PasswordDialog.PasswordConfirmListener;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.view.PasswordDialogFragment;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;


/**
 * <p>Activity for Signature verification</p>
 * 
 * 
 *
 */
public class PhotoDeskSignatureVerification extends Activity implements OnClickListener {


	public SCanvasView mSCanvas;

	private Toast m_Toast = null;

	int mDialogSelect = 1;

	int mVerificationLevel = SCanvasConstants.SIGNATURE_VERIFICATION_LEVEL_LOW;

	private PasswordDialog mPasswordDlg;
	private LinearLayout mLLSignatureVerfication;
	private LinearLayout mLLSignatureRetry;
	private LinearLayout mLLSignaturePassword;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(initThemeId());
        setContentView(R.layout.signature_verification);
        

        mSCanvas = (SCanvasView)findViewById(R.id.canvas_view);
        mLLSignatureVerfication = (LinearLayout)findViewById(R.id.btnSignatureVerification);
        mLLSignatureRetry = (LinearLayout)findViewById(R.id.btnRetry);
        mLLSignaturePassword = (LinearLayout)findViewById(R.id.btnPassword);
        
        mLLSignatureVerfication.setOnClickListener(this);
        mLLSignatureRetry.setOnClickListener(this);
        mLLSignaturePassword.setOnClickListener(this);
        
        // ====================================================================================
        //
        // Set Callback Listener(Interface)
        //
        // ====================================================================================
        SCanvasInitializeListener scanvasInitializeListener = new SCanvasInitializeListener() {
            public void onInitialized() {

                // --------------------------------------------
                // Start SCanvasView/CanvasView Task Here
                // --------------------------------------------
                // Start Signature Mode
                mSCanvas.openSignatureEngine();

                mSCanvas.setZoomEnable(false); // disable Zoom
            }
        };

        mSCanvas.setSCanvasInitializeListener(scanvasInitializeListener);
        sToastS(getResources().getString(R.string.signature_verification_draw));



    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSignatureVerification : {
                if (mSCanvas.verifySignature(mVerificationLevel)) {
                  // Signature verification success
                  setResult(RESULT_OK);
                  finish();
                  sToastS(getResources().getString(
                          R.string.signature_verification_verification_success));
              } else {
                  // Signature verification failure
                  sToastS(getResources().getString(
                          R.string.signature_verification_verification_failure));
              }
            
            }
            break;
            case R.id.btnRetry : {
              if (mSCanvas.clearSignatureScreen()) {
              sToastS(getResources().getString(R.string.signature_verification_draw));
          }
                
            }
            break;
            case R.id.btnPassword : {
                
               // displayInputPasswordDialog();
            	showPasswordDialog();
            }
            break;
        }
    }
    /**
     * Show Password Dialog
     */
    
    void showPasswordDialog() {
        DialogFragment newFragment = PasswordDialogFragment.newInstance(PasswordDialog.DIALOG_CONFIRM_PASSWORD);
        
        newFragment.show(getFragmentManager(), "dialog");
    }
    /**
     * Dialog for entering the password
     */
    public void displayInputPasswordDialog() {
   
        
        mPasswordDlg = new PasswordDialog(this, PasswordDialog.DIALOG_CONFIRM_PASSWORD);

        mPasswordDlg.setOnPasswordConfirmListener(new PasswordConfirmListener() {
            
            @Override
            public void onPasswordConfirm() {
                setResult(RESULT_OK);
                mPasswordDlg.dismiss();
                finish();
            }
        });
        mPasswordDlg.show();
        
    }
    
    @Override
    public void onBackPressed() {
        mSCanvas.closeSignatureEngine();
        super.onBackPressed();
    }
    /**
     * Custom Toast message( bigger and adjusting the position)
     * @param i_String : Toasts message 
     */
    public void sToastS(String message) {
        m_Toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
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
    
    /**
     * Initialize ThemeID
     * @return Current theme dialog id
     */
    private int initThemeId(){
    	return Setting.INSTANCE.getDialogId();
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mPasswordDlg != null)
            mPasswordDlg.dismiss();
        super.onSaveInstanceState(outState);
    }
    
    
    
}
