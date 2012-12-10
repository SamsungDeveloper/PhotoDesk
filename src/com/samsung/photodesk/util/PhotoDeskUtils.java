package com.samsung.photodesk.util;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.samsung.photodesk.FolderFragment;
import com.samsung.photodesk.HiddenFolderActivity;
import com.samsung.photodesk.MapViewEdit;
import com.samsung.photodesk.PhotoDeskSignatureRegistration;
import com.samsung.photodesk.PhotoDeskSignatureVerification;
import com.samsung.photodesk.R;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.editor.AnimationData;
import com.samsung.photodesk.editor.AnimationImagePlayerActivity;
import com.samsung.photodesk.editor.ImageEditorActivity;
import com.samsung.photodesk.editor.SAMMDBHelper;
import com.samsung.sdraw.SDrawLibrary;
import com.samsung.spensdk.SCanvasView;

/**
 * <p>Global utility class for Photo Desk</p>
 *
 */
public class PhotoDeskUtils {
    private static Toast m_Toast = null;
    
    private static final String DEFAULT_FOLDER = Environment.getExternalStorageDirectory() + "/PhotoDesk/";
  
    /**
     * <p>Start 'Set as' function</p>
     * Set intent for start 'Set as' function.
     * @param context - {@link Context}
     * @param path - selected item path
     */
    public static void startSetAsActivity(Context context, String path) {
		File file = new File(path);
		if (file.exists()) {
			Intent intent = new Intent(Intent.ACTION_ATTACH_DATA)
					.setDataAndType(Uri.fromFile(file), "image/jpg");
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.putExtra("mimeType", intent.getType());
			context.startActivity(
					Intent.createChooser(intent, context.getString(R.string.set_as)));
		}
    }
    
    /**
     * <p>Start 'Camera' function</p>
     * Set intent for start 'Camera' function.
     * @param context - {@link Context}
     */
	public static void startCameraActivity(Context context) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
	
	/**
	 * <p>Signature registration</p>
	 * @param context - {@link Context}
	 * @param resultCode - result code
	 */
	public static void signatureRegistration(Context context, int resultCode) {

        if (!SDrawLibrary.isSupportedModel()) {
            sToastS(context, context.getResources().getString(R.string.not_support_this_feature));
            return;
        }
        if (SCanvasView.isSignatureExist(context)) {
            Intent intent = new Intent(context, PhotoDeskSignatureVerification.class);
            ((Activity) context).startActivityForResult(intent, resultCode);
        } else { 
        	startSignatureRegistrationActivity(context);
        }
    }
	
	/**
	 * <p>Signature verification</p>
	 * @param context - {@link Context}
	 * @param resultCode - result code
	 */
	public static void signatureVerification(Context context, int resultCode) {

        if (!SDrawLibrary.isSupportedModel()) {
            sToastS(context, context.getResources().getString(R.string.not_support_this_feature));
            return;
        }
        if (SCanvasView.isSignatureExist(context)) {
            Intent intent = new Intent(context, PhotoDeskSignatureVerification.class);
            ((Activity) context).startActivityForResult(intent, resultCode);
        } else { 
        	sToastS(context, context.getResources().getString(R.string.signature_start_check_failure));
        }
    }
	
    
	/**
	 * <p>Show toast message</p>
	 * @param context - {@link Context}
	 * @param i_String - string
	 */
    public static void sToastS(Context context, String i_String) {
        if (m_Toast == null) {
            m_Toast = Toast.makeText(context, i_String, Toast.LENGTH_SHORT);
        } else {
            m_Toast.setText(i_String);
        }
        m_Toast.show();
    }

    /**
     * <p>Start activity for signature registration</p>
     * @param context - {@link Context}
     */
	public static void startSignatureRegistrationActivity(Context context) {
		Intent intent = new Intent(context, PhotoDeskSignatureRegistration.class);
		context.startActivity(intent); // create RegistrationActivity
	}
	
	/**
	 * Start activity for hidden folder</p>
	 * @param context - {@link Context}
	 * @param resultCode - result code
	 */
	public static void startShowHiddenFolder(Context context, int resultCode) {

        if (SCanvasView.isSignatureExist(context)) {		
	        Intent intent = new Intent(context, HiddenFolderActivity.class);
	        ((Activity) context).startActivityForResult(intent, resultCode);
        } else { 
        	startSignatureRegistrationActivity(context);
        }
	}
	

	
	/**
	 * <p>Get linked folder's file path</p>
	 * @param file - linked folder
	 * @return file path
	 */
	public static String getLinkFolerFilePath(File file) {
		File[] files = file.listFiles();
		if (files == null)
			return null;
		   
		for(int i = 0;i < files.length;i++) {   
			if(files[i].isDirectory()) {
				Log.v("Directory : ", files[i].getName());    
				
			} else {
				if(files[i].getName().endsWith("jpg") || files[i].getName().endsWith("png") ||
						files[i].getName().endsWith("bmp") || files[i].getName().endsWith("mp4") ||
						files[i].getName().endsWith("avi") || files[i].getName().endsWith("jpeg")) {
					return files[i].getPath();
				}
			}
		} 
		return null;
	}
	
	/**
	 * <p>Get linked folder position</p>
	 * @param id - linked folder ID
	 * @return linked folder position
	 */
	public static int getLinkFolderPosition(long id) {
		if (FolderFragment.getFolderItems() == null) return 0; 
		int size = FolderFragment.getFolderItems().size();
		int index = 0;
		for (; index < size; index++) {
			if (FolderFragment.getFolderItems().get(index).getId() == id) {
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * <p>Link folder</p>
	 * Create shortcut icon in HomeScreen
	 * @param context {@link Context}
	 * @param selectedItem - selected folder
	 */
	public static void linkFolder(Context context, FolderItem selectedItem) {
		MediaItem [] thubmItems = selectedItem.getImages();
		MediaItem item = thubmItems[0];
        String albumName = selectedItem.getDisplayName();
        Bitmap albumImage = ThumbnailCache.INSTANCE.getFolderBitmap(item.getId());
        Bitmap scaledAlbumImage = Bitmap.createScaledBitmap(albumImage, 128, 128, true);
        Uri path = Uri.parse(String.valueOf(selectedItem.getPath()));
        String className = context.getClass().getName();
        String packageName = context.getPackageName();
        
        Intent shortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		shortcutIntent.setClassName(packageName, className);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.setData(path);
		 
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, albumName);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, drawShortcutIcon(scaledAlbumImage));
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT"); 
		context.sendBroadcast(intent);
	}
	
	/**
	 * <p>Draw shortcut icon</p>
	 * @param bitmap - folder image
	 * @return shortcut bitmap
	 */
	private static Bitmap drawShortcutIcon(Bitmap bitmap) {
		Bitmap output = null;
        if(bitmap != null){
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawBitmap(bitmap, null, rect, null);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            canvas.drawRect(rect, paint);
        }
		return output;
	}

	/**
	 * <p>Start share</p>
	 * Set share for quick menu.
	 * @param context - {@link Context}
	 * @param path - selected item path
	 * @param selectedItem - selected item ArrayList 
	 */
	public static void startShare(Context context, String path, ArrayList<MediaObject> selectedItem) {
		File file = new File(path);
		if (file.exists()) {
			Intent intent = createShareIntent(selectedItem);
			context.startActivity(Intent.createChooser(intent, context.getString(R.string.do_share)));
		}
	}
	
	/**
	 * <p>Set share intent</p>
	 * @param selectedItems
	 * @return shared intent
	 */
	public static Intent createShareIntent(ArrayList<MediaObject> selectedItems) {
		final ArrayList<Uri> uris = new ArrayList<Uri>();
		final Intent intent = new Intent();
		int mediaType = 0;
		String mimeType = "image/*";

		for (int i = 0; selectedItems.size() > i ; i++) {
			if (uris.size() >= 1000) break; 
			mediaType = selectedItems.get(i).getType();
			mimeType = (mediaType == MediaItem.IMAGE) ? "image/*" : "video/*";
			
			MediaItem item = (MediaItem) selectedItems.get(i);
			Uri uri = ContentUris.withAppendedId(item.getUri(mediaType), item.getId());
			uris.add(uri);			
		}
	
        final int size = uris.size();
        if (size > 0) {
            
            if (size > 1) {
                intent.setAction(Intent.ACTION_SEND_MULTIPLE).setType(mimeType);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                intent.setAction(Intent.ACTION_SEND).setType(mimeType);
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            }
            intent.setType(mimeType);
        }
        
        return intent;
	}
	
	/**
	 * <p>Set share intent</p>
	 * @param mediaItems - selectedItems
	 * @param position - item position
	 * @return shared intent
	 */
	public static Intent createShareIntent(ArrayList<MediaItem> mediaItems, int position) {
		final Intent intent = new Intent();
		String mimeType = "image/*";

		if (position >= mediaItems.size()) {
			position = mediaItems.size()-1;
		}
		
		MediaItem item = mediaItems.get(position);
		mimeType = (item.getType() == MediaItem.IMAGE) ? "image/*" : "video/*";
		
		long id = item.getId();
		Uri uri = ContentUris.withAppendedId(item.getUri(item.getType()), id);

        intent.setAction(Intent.ACTION_SEND).setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType(mimeType);
        
        return intent;
	} 
	
	/**
	 * <p>Get default folder</p>
	 * Get default folder path
	 * @return default folder path
	 */
	public static String getDefualtFodler() {
		return DEFAULT_FOLDER;
	}
	
	/**
	 * <p>Check rotation supported</p>
	 * Check item for rotation supported.
	 * @param mimeType - item's mimeType
	 * @return true - supported , false - not supported
	 */
    public static boolean isRotationSupported(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.equals("image/jpeg");
    }
    
    /**
     * <p>Get current exif orientation</p>
     * @param orientation - item's orientation
     * @return current orientation
     */
	public static int getCurrentExifOrientation(String orientation) {
        Log.d("SPenGallery", "getExifOrientation: orientation " + orientation);
        switch (Integer.valueOf(orientation)) {
            case 0: // ORIENTATION_UNDEFINED
                return 0;
            case 1: // ORIENTATION_NORMAL
                return 0;
            case 2: // ORIENTATION_FLIP_HORIZONTAL
                return 0;
            case 3: // ORIENTATION_ROTATE_180
                return 180;
            case 4: // ORIENTATION_FLIP_VERTICAL
                return 0;
            case 5: // ORIENTATION_TRANSPOSE
                return 0;
            case 6: // ORIENTATION_ROTATE_90
                return 90;
            case 7: // ORIENTATION_TRANSVERSE
                return 0;
            case 8: // ORIENTATION_ROTATE_270
                return 270;
            default:
                throw new AssertionError("invalid: " + orientation);
        }
    }

	/**
	 * <p>Start activity for image editor</p>
	 * @param context - {@link Context}
	 * @param path - selected item path
	 * @param empty - empty page flag
	 */
	public static void startImageEditor(Context context, String path) {
		Intent intent = new Intent(context, ImageEditorActivity.class);
        intent.putExtra(ImageEditorActivity.EDITOR_TYPE_KEY_PATH, path);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
	}    
	
	/**
	 * <p>Check current include video mode</p>
	 * @return true - include video mode , false - exclude video mode
	 */
	public static boolean isIncludeVideoSettingChange() {
		return Setting.INSTANCE.getIncludeVideo() ? true : false;
	}

	/**
	 * <p>Start activity for GPS editor</p>
	 * @param context - {@link Context}
	 * @param mediaItem - selected item
	 * @param viewFlag - view flag
	 */
	public static void startLocationEdit(Context context, MediaItem mediaItem, int viewFlag) {
		Intent intent = new Intent(context, MapViewEdit.class);
		intent.putExtra("view_item", mediaItem);
		intent.putExtra("view_flag", viewFlag);
		context.startActivity(intent);
	}
	
	/**
	 * <p>Start activity for AMS player</p>
	 * @param context - {@link Context}
	 * @param path - selected item's path
	 */
	public static void startSamAnimation(Context context, String path) {
    	Intent intent = new Intent(context, AnimationImagePlayerActivity.class);
		SAMMDBHelper db = new SAMMDBHelper(context);
		AnimationData dump = db.getSAMMInfos(path);
		
		String midi = null, voice = null;
		if (dump != null) {
			midi = dump.getMidiPath();
			voice = dump.getVoicePath();
		}
		
		AnimationData data = new AnimationData(path, midi, voice/*, opt.outWidth, opt.outHeight*/);
		
		intent.putExtra(AnimationImagePlayerActivity.ANIMATION_DATA, data);
		context.startActivity(intent);
    }

	/**
	 * <p>Start activity for video player</p>
	 * @param context - {@link Context}
	 * @param path - selected item's path
	 */
	public static void startVideoPlayer(Context context, String path) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "video/*");
        context.startActivity(intent);
	}
}
