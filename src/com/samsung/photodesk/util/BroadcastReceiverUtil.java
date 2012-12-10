package com.samsung.photodesk.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.samsung.photodesk.PhotoDeskActivity;

/**
 * <p>BroadcastReceiver</p>
 * Listening for Broadcasts with Broadcast Receivers
 */
public class BroadcastReceiverUtil extends BroadcastReceiver {
	public static final int EXTERNAL_CARD_IN = 1;
	public static final int EXTERNAL_CARD_OUT = 2;

	public int mExternalCardInOut;
	 
	@SuppressWarnings("static-access")
	public void onReceive(Context context, Intent intent) {
		if (PhotoDeskActivity.sPhotoDeskActivity == null) {
			return;
		}
		
		if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
			mExternalCardInOut = EXTERNAL_CARD_IN; 
//			Toast.makeText(context, "MOUNTED", Toast.LENGTH_SHORT).show();
		}
		if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {	
			mExternalCardInOut = EXTERNAL_CARD_OUT; 
//			Toast.makeText(context, "UNMOUNTED", Toast.LENGTH_SHORT).show();
		}
		if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
//			Toast.makeText(context, "REMOVED", Toast.LENGTH_SHORT).show();
		}
		if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
//			Toast.makeText(context, "EJECT", Toast.LENGTH_SHORT).show();
		}		
		if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
//			Toast.makeText(context, "SCAN STATRED", Toast.LENGTH_SHORT).show();
		}		
		if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {			
//			Toast.makeText(context, "SCAN FINISHED", Toast.LENGTH_SHORT).show();
			if(PhotoDeskActivity.sPhotoDeskActivity.getCurrentActivity().equals(PhotoDeskActivity.sPhotoDeskActivity.PHOTODESK_CLASS_NAME)){
				if (mExternalCardInOut == EXTERNAL_CARD_OUT){
					PhotoDeskActivity.sPhotoDeskActivity.refreshAllView();
				}else {
					PhotoDeskActivity.sPhotoDeskActivity.refreshView();
				}
			}
		}
		if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)) {
//			Toast.makeText(context, "SCAN FINISHED", Toast.LENGTH_SHORT).show();
		}
		if (intent.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {
//			Toast.makeText(context, "SCAN FINISHED", Toast.LENGTH_SHORT).show();
		}

	}
}
