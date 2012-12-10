package com.samsung.photodesk.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * <p>File control utility class</p>
 * 
 */
public class FileControlUtil {
	private static String mSdcardPath;
	private static String mSaveFileName;
	
	public static final String ANIMATION_TEMP_FILE_NAME = "animation_tempfile";
	
	public static final String SUB_IMAGE_FOLDER_NAME = "images/";
	public static final String SUB_RECORDE_FOLDER_NAME = "mic/";
	public static final String ANIMATION_TAGNAME = "_ani";
	
	public static final String DEFAULE_IMAGE_FORMAT = ".jpg";
	public static final String DEFAULE_VOICE_FORMAT = ".amr";

	/**
	 * <p>Initialize</p>
	 */
	public static void initPath() {
		mSdcardPath = null;
		mSaveFileName = null;
	}
	
	/**
	 * <p>Check SD card state</p>
	 * @return ture - SD card exist, false - SD card empty
	 */
	public static boolean checkSdcard(){
		
		String sdcardState = Environment.getExternalStorageState();
		
		if (!sdcardState.equals(Environment.MEDIA_MOUNTED)) {
			mSdcardPath = Environment.MEDIA_UNMOUNTED;
			return false;
		} else {
			mSdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		return true;
	}

	/**
	 * <p>Create directory</p>
	 * Create directory using full path
	 * @param fullPath
	 * @return true - success , false - fail
	 */
	public static boolean makeDirOfFullPath(String fullPath){
		File folder = new File(fullPath);
		if (!folder.exists()) {
			if (folder.mkdirs() == false) {
				Log.e("", "folder creat error!!!");
				return false;                            
			}
		}
		
		return true;
	}
	
	/**
	 * <p>Create file name</p>
	 * Create new file name.
	 * @param fileHeadName
	 * @return file name
	 */
	public static String createFileName(String fileHeadName) {
		java.util.Date date = new java.util.Date();
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
		StringBuilder curDate = new StringBuilder(format.format(date));
		format = new SimpleDateFormat("hhmmss");
		curDate.append(format.format(date));
		curDate.insert(0, fileHeadName);
		
		String name = curDate.toString();
		setSaveFileName(name);
		return name;
	}
	
	/**
	 * <p>Get saved file full path</p>
	 * @param defaultPath
	 * @param format
	 * @return file full path
	 */
	public static String getSavedFileFullPath(String defaultPath, String format) {
		if (!checkSdcard()) {
			return null;
		}
		if (mSaveFileName == null || defaultPath == null || format == null) {
			return null;
		}

		return new StringBuilder().append(defaultPath)
				.append(mSaveFileName).append(format)
				.toString();
	}
	
	/**
	 * <p>Get saved voice file full path</p>
	 * @return voice file full path
	 */
	public static String getSavedVoiceFullPath() {
		if (!checkSdcard()) {
			return null;
		}

		return new StringBuilder().append(getRecordFilePath())
				.append(mSaveFileName).append(DEFAULE_VOICE_FORMAT)
				.toString();
	}
	
	/**
	 * <p>Get file full path</p>
	 * @param defaultPath
	 * @param filename
	 * @param format
	 * @return file full path
	 */
	public static String getFileFullPath(String defaultPath, String filename, String format) {
		if (filename == null || defaultPath == null || format == null) {
			return null;
		}
		
		if (mSdcardPath == null) {
			if (!checkSdcard()) {
				return null;
			}
		}

		return new StringBuilder().append(mSdcardPath)
				.append(defaultPath)
				.append(filename).append(format)
				.toString();
	}
	
	/**
	 * <p>Get file directory path</p>
	 * @param defaultPath
	 * @return file directory path
	 */
	public static String getFileDirPath(String defaultPath) {
		if (defaultPath == null) {
			return null;
		}
		
		if (mSdcardPath == null) {
			if (!checkSdcard()) {
				return null;
			}
		}

		return new StringBuilder().append(mSdcardPath)
				.append(defaultPath)
				.toString();
	}
	
	/**
	 * <p>Get file name</p>
	 * @param path
	 * @return file name
	 */
	public static String getFileName(String path) {
		String result = null;
		
		int lengh = path.length();
		for (int i = lengh-1; i > 0; i--) {
			if(path.charAt(i) == '/') {
				result = path.substring(i+1, lengh - 4);
				break;
			}
		}
		return result;
	}
	
	/**
	 * <p>Check file</p>
	 * Check for file existence.
	 * @param filePath
	 * @param fileFormat
	 * @return true - exist , false - empty
	 */
	public static boolean isExistFile(String filePath, String fileFormat) {
		String pp = FileControlUtil.getSavedFileFullPath(filePath, fileFormat);
		File file = new File(pp);
		return file.exists();
	}
	
	/**
	 * <p>Check file</p>
	 * Check for file existence.
	 * @param fullPath
	 * @return true - exist , false - empty
	 */
	public static boolean isExistFile(String fullPath) {
		if (fullPath == null) {
			return false;
		}
		
		File file = new File(fullPath);
		return file.exists();
	}
	
	/**
	 * <p>Set save file name</p>
	 * @param name
	 */
	public static void setSaveFileName(String name) {
		mSaveFileName = name;
	}
	
	/**
	 * <p>Get save file name</p>
	 * @return save file name
	 */
	public static String getSaveFileName() {
		return mSaveFileName;
	}
	
	/**
	 * <p>Get SD card path</p>
	 * @return SD card path
	 */
	public static String getSdcardPath(){
		if (mSdcardPath == null) {
			checkSdcard();
		}
		return mSdcardPath;
	}
	
	/**
	 * <p>Check save folder path</p>
	 * @param scanvasPath - old path
	 * @param selectFolderPath - new path
	 * @return true - same folder path , false - different folder path
	 */
	public static boolean isSameFolder(String scanvasPath, String selectFolderPath) {
		String basePath = scanvasPath.substring(0, scanvasPath.lastIndexOf("/"));
		String newPath = selectFolderPath.substring(0, selectFolderPath.lastIndexOf("/"));
		
		return basePath.equals(newPath);
	}
	
	/**
	 * <p>Check special character</p>
	 * @param base - full string
	 * @return true - Contain special characters. , false - Does not contain special characters.
	 */
	public static boolean isContainSpecialStr(String base) {
		String[] filter_words = {"#", "[$]", "%", "\\^", "&"
									, "[*]", "\\(", "\\)", "~", ";", ":", "[|]", "<", ">", "\\{", "\\}", "\\["
									, "\\]", "=", "[+]", "!", "@"};
		
		int size = filter_words.length;
		for (int i = 0; i < size; i++) {
			if (base.contains(filter_words[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * <p>Get file path from URI</p>
	 * @param activity - {@link Activity}
	 * @param contentUri - URI
	 * @return file path
	 */
	public static String getRealPathFromURI(Activity activity, Uri contentUri) { 		
		String[] proj = { MediaStore.Images.Media.DATA };
		String filePath = "";
		CursorLoader cursorLoader = new CursorLoader(activity, contentUri, proj, null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			
			cursor.moveToFirst();
			if (cursor.getCount() > 0) 		filePath = cursor.getString(column_index);
			cursor.close();
		}

		return filePath;
	}
	
	/**
	 * <p>Check image</p>
	 * Check image using mimeType.
	 * @param strImagePath - image path
	 * @return true - vaild image , false - invalid image
	 */
	public static boolean isValidImage(String strImagePath){		
		if (strImagePath == null) {			
			return false;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(strImagePath, options);

		return (options.outMimeType != null);	
	}
	
	/**
	 * <p>Get animation file path</p>
	 * @param path - file path
	 * @return animation file path
	 */
	public static String getAnimationFilePath(String path) {
		StringBuilder savedPath = new StringBuilder();
		savedPath.append(path)
		.append(FileControlUtil.getSaveFileName())
		.append(ANIMATION_TAGNAME)
		.append(FileControlUtil.DEFAULE_IMAGE_FORMAT);
		
		return savedPath.toString();
	}
	
	/**
	 * <p>Get temp animation file path</p>
	 * @param format - file format
	 * @return temp animation file path
	 */
	public static String getTempAnimationFilePath(String format) {
		StringBuilder savedPath = new StringBuilder();
		savedPath.append(PhotoDeskUtils.getDefualtFodler() + SUB_IMAGE_FOLDER_NAME)
		.append(ANIMATION_TEMP_FILE_NAME)
		.append(ANIMATION_TAGNAME)
		.append(format);
		
		return savedPath.toString();
	}
	
	/**
	 * <p>Create nomedia file</p>
	 * @param path
	 */
	public static void createNoMediaFile(String path) {
		File noMediaFile = new File(path + ".nomedia");
		try {
			if(noMediaFile.createNewFile()) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>Create temp record folder</p>
	 * @param context {@link Context}
	 */
	public static void createTempRecordFolder(Context context) {
		String path = PhotoDeskUtils.getDefualtFodler() + SUB_RECORDE_FOLDER_NAME;
		if (!checkSdcard()) {
			File file = new File(path);
			file.mkdirs();
		} 
		
		createNoMediaFile(path);
	}
	
	/**
	 * <p>Create temp folder</p>
	 */
	public static void createTempFolder() {
		String path = PhotoDeskUtils.getDefualtFodler() + SUB_IMAGE_FOLDER_NAME;
		File file = new File(path);
		file.mkdirs();
		
		file = new File(getRecordFilePath());
		file.mkdirs();
		
		createNoMediaFile(path);
	}
	
	/**
	 * <p>Get record file path</p>
	 * @return record file path
	 */
	public static String getRecordFilePath() {
		return PhotoDeskUtils.getDefualtFodler() + SUB_RECORDE_FOLDER_NAME;
	}
	
	/**
	 * <p>Remove temp items</p>
	 */
	public static void removeTempItems() {
		String path = PhotoDeskUtils.getDefualtFodler() + SUB_RECORDE_FOLDER_NAME + mSaveFileName;
		
		File file = new File(path + DEFAULE_IMAGE_FORMAT);
		file.delete();
		
		file = new File(path + DEFAULE_VOICE_FORMAT);
		file.delete();
	}
	
	/**
	 * <p>Start media scanner</p>
	 * Sendbroadcast to media scanner.
	 * @param context - {@link Context}
	 * @param path - file path
	 */
	public static void runMediaScanner(Context context, String path) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path.toString()));
        intent.putExtra("Photo Desk", 1);

        context.sendBroadcast(intent);		
	}
	
	public static void saveBitmapImage(Context context, Bitmap data, String savedPath) {
		OutputStream out = null;

		try {
			out = new FileOutputStream(savedPath);
			data.compress(CompressFormat.JPEG, 100, out);
		} catch (FileNotFoundException e) {
			Log.e("", "file not found exception");
		} finally {
			data.recycle();
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savedPath.toString()));
            intent.putExtra("Photo Desk", 1);

            context.sendBroadcast(intent);				
		}
	}
}

