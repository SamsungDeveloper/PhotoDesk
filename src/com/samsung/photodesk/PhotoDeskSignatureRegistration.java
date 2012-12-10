
package com.samsung.photodesk;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.photodesk.PasswordDialog.PasswordBackKeyListener;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.view.PasswordDialogFragment;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;


/**
 * <p>Activity for Signature registration</p>
 * 
 * 
 *
 */
public class PhotoDeskSignatureRegistration extends Activity implements OnClickListener {


	public SCanvasView mSCanvas;

	public int mSigntureRegistrationNum = 0;

	public int mSigntureRegistrationNumMax = 3;

	public ListView mSignatureList;

	private Toast m_Toast = null;

	private int mResult = 0;

	private LinearLayout mLLSignatureSave;
	private LinearLayout mLLSignatureRetry;
	private PasswordDialog mPasswordDlg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTheme(initThemeId());
        setContentView(R.layout.signature_registration);
        
        mSCanvas = (SCanvasView)findViewById(R.id.canvas_view);
        mLLSignatureSave = (LinearLayout)findViewById(R.id.btnSignatureSave);
        mLLSignatureRetry = (LinearLayout)findViewById(R.id.btnRetry);
        
        mLLSignatureSave.setOnClickListener(this);
        mLLSignatureRetry.setOnClickListener(this);
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

        sToastS(getResources().getString(R.string.signature_password_registration_desc));
        
        // --------------------------------------------
        // Display dialog for new signature
        // --------------------------------------------
        displayNewPasswordDialog();


    }
    
    /**
     * Show Password Dialog
     */

	void showPasswordDialog() {
        DialogFragment newFragment = PasswordDialogFragment.newInstance(PasswordDialog.DIALOG_NEW_PASSWORD);
        
        newFragment.show(getFragmentManager(), "dialog");
    }
    

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSignatureSave : {
                
                mResult = mSCanvas.registerSignature();
              if (mSCanvas.isSignatureRegistrationCompleted() && mResult > 0) {
                  finish();
                  // Signature registration is completed
                  sToastS(getResources().getString(
                          R.string.signature_registration_registration_success_3));
                  
              } else {
                  if (mResult == 2) {
                      mSigntureRegistrationNum = mResult;
                      // Signature registration is not completed
                      sToastS(getResources().getString(
                              R.string.signature_registration_registration_success_2));
                  } else if (mResult == 1) {
                      mSigntureRegistrationNum = mResult;
                      // Signature registration is not completed
                      sToastS(getResources().getString(
                              R.string.signature_registration_registration_success_1));
                  } else {
                      // Signature registration error
                      sToastS(getResources().getString(
                              R.string.signature_registration_registration_failure));
                  }
              }

                
            }
                break;
            case R.id.btnRetry: {
              if (mSCanvas.clearSignatureScreen()) {
              // Canvas reset success
              sToastS(getResources().getString(R.string.signature_registration_draw));
          }

            }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mSCanvas.closeSignatureEngine();
        super.onBackPressed();
    }
/**
 * Display dialog for new signature
 */
     public void displayNewPasswordDialog() {
         
         mPasswordDlg= new PasswordDialog(this, PasswordDialog.DIALOG_NEW_PASSWORD);
    	 	
         mPasswordDlg.setOnPasswordBackKeyListener(new PasswordBackKeyListener() {
 			
 			@Override
 			public void onPasswordBackKey() {
 				mPasswordDlg.dismiss();
 				onBackPressed();
 			}
 		});
         
         mPasswordDlg.show();

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
