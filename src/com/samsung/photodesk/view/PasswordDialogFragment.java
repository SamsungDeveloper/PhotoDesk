package com.samsung.photodesk.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.samsung.photodesk.PasswordDialog;
import com.samsung.photodesk.PasswordDialog.PasswordBackKeyListener;
import com.samsung.photodesk.PasswordDialog.PasswordConfirmListener;

/**
 * <p>Password dialog class</p>
 * Create password dialog.
 * Used by signature registration or verification.
 *
 */
public class PasswordDialogFragment extends DialogFragment {
	PasswordDialog mPassDialog;
    /**
     * Create a new instance of PasswordDialogFragment, providing "type"
     * as an argument.
     */
       public static PasswordDialogFragment newInstance(int type) {
    	   PasswordDialogFragment frag = new PasswordDialogFragment();
	            Bundle args = new Bundle();
	            args.putInt("type", type);
	            frag.setArguments(args);
	            return frag;
	        }
	        
	        @Override
	        public Dialog onCreateDialog(Bundle savedInstanceState) {
	            int type = getArguments().getInt("type");
	            
	            
	            mPassDialog = new PasswordDialog(getActivity(), type);

	            mPassDialog.setOnPasswordConfirmListener(new PasswordConfirmListener() {
	                
	                @Override
	                public void onPasswordConfirm() {
	                	Activity activity = getActivity();
	                	activity.setResult(Activity.RESULT_OK);
	                    mPassDialog.dismiss();
	                    activity.finish();
	                }
	            });
	            
	            mPassDialog.setOnPasswordBackKeyListener(new PasswordBackKeyListener() {
	     			
	     			@Override
	     			public void onPasswordBackKey() {
	     				mPassDialog.dismiss();
	     				getActivity().onBackPressed();
	     			}
	     		});
	            

	            return mPassDialog.mDialog;

	        }
	    }
	
	
