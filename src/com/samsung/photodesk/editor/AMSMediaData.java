package com.samsung.photodesk.editor;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

import com.samsung.photodesk.R;
import com.samsung.photodesk.util.FileControlUtil;

/**
 * <p>Media File Info Class that used in Image Editor.</p>
 * this class has audio file, path and background music index, voice file path
 *
 */
public class AMSMediaData {
	private ArrayList<String> mMediaTitles;
	private ArrayList<String> mMediaPaths;

	private int mSelectedMediaIdx;
	
	private String mVoicePath;


	public AMSMediaData(Context context) {
		mMediaTitles = new ArrayList<String>();
		mMediaPaths = new ArrayList<String>();
		mSelectedMediaIdx = 0;
		
		initAudioData(context);
	}
	
	/**
	 * <p>Initialization audio file datas</p>
	 * @param context	{@link Context}
	 */
	public void initAudioData(Context context) {
		String[] mCursorCols = new String[]{
				BaseColumns._ID,
				MediaColumns.TITLE, 
				MediaColumns.DATA, 
				MediaColumns.MIME_TYPE};
		Cursor cur = context.getContentResolver()
				.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols, null, null, null);

		setPath(null);
		setTitle(context.getResources().getString(R.string.not_exist));
		
		int titleColumn = cur.getColumnIndex(MediaColumns.TITLE);
		int dataColumn = cur.getColumnIndex(MediaColumns.DATA);

		String midePath = FileControlUtil
				.getFileDirPath(FileControlUtil.getRecordFilePath());
		
		while (cur.moveToNext()) {
			String path = cur.getString(dataColumn);
			if (!path.contains(midePath)) {
				setPath(cur.getString(dataColumn));
				setTitle(cur.getString(titleColumn));
			}
		}

		cur.close();
	}

	/**
	 * <p>Set audio title</p>
	 * @param title	audio title
	 */
	public void setTitle(String title) {
		mMediaTitles.add(title);
	}

	/**
	 * <p>Get audio title</p>
	 * @param idx	audio index
	 * @return		audio title
	 */
	public String getTitle(int idx) {
		return mMediaTitles.get(idx);
	}

	/**
	 * <p>Get array of audio title</p>
	 * @return	array of audio title
	 */
	public ArrayList<String> getTitles() {
		return mMediaTitles;
	}

	/**
	 * <p>Set audio file path</p>
	 * @param path	audio file path
	 */
	public void setPath(String path) {
		mMediaPaths.add(path);
	}

	/**
	 * <p>Get audio file path</p>
	 * @param idx	audio file index
	 * @return		audio file path
	 */
	public String getPath(int idx) {
		return mMediaPaths.get(idx);
	}
	
	/**
	 * <p>Get voice file path</p>
	 * @return	voice file path
	 */
	public String getVoicePath() {
		return mVoicePath;
	}

	/**
	 * <p>Set voice file path</p>
	 * @param voicePath		voice file path
	 */
	public void setVoicePath(String voicePath) {
		mVoicePath = voicePath;
	}

	/**
	 * <p>Set selected audio file index</p>
	 * @param idx		audio file index
	 */
	public void setIdx(int idx) {
		mSelectedMediaIdx = idx;
	}

	/**
	 * <p>Get seletected audio file index</p>
	 * @return		audio file index
	 */
	public int getIdx() {
		return mSelectedMediaIdx;
	}
	
	/**
	 * <p>Get array of audio path</p>
	 * @return	array of audio path
	 */
	public ArrayList<String> getMediaPaths() {
		return mMediaPaths;
	}
}
