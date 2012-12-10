package com.samsung.photodesk.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

import com.samsung.photodesk.R;

/**
 * <p>Custom dialog for detail information</p>
 *
 */
public class CustomDetailsDialog {
    private Dialog mDialog;
    private Context mContext;
    
    /**
     * constructor
     * @param context - {@link Context}
     */
    public CustomDetailsDialog(Context context) {
        mContext = context;
        initCustomDialog();
    }

    /**
     * Constructor
     * @param context - {@link Context}
     * @param layout
     */
    public CustomDetailsDialog(Context context, LinearLayout layout) {
        mContext = context;

        initSetCustomDialog(layout);
    }
    
    /**
     * <p>Initialize using layout
     * @param layout
     */
    private void initSetCustomDialog( LinearLayout layout) {
        mDialog = new Dialog(mContext,
                android.R.style.Theme_Translucent_NoTitleBar);

        // Setting dialog view
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mDialog.setContentView(layout);
    }
    
    /**
     * <p>Initialize</p>
     */
    private void initCustomDialog() {
        mDialog = new Dialog(mContext,
                android.R.style.Theme_Translucent_NoTitleBar);

        // Setting dialog view
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mDialog.setContentView(R.layout.custom_details_dlg);
    }
    
    /**
     * <p>Show dialog</p>
     */
    public void show() {
        mDialog.show();
    }

    /**
     * <p>Dismiss dialog</p>
     */
    public void dismiss() {
        mDialog.dismiss();
    }
    
    /**
     * <p>Hide dialog</p>
     */
    public void hide() {
        mDialog.hide();
    }
}
