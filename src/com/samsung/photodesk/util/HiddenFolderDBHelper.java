package com.samsung.photodesk.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * DB control class for Hidden Folder
 */
public class HiddenFolderDBHelper {

	public static SQLiteDatabase mDB;
	public static final String TABLENAME_HIDDENFOLDER = "HiddenFolderTable";
	public static final String COL__ID = "_id";
	public static final String COL_FOLDER_THUMBNAIL = "data_thumbnail";
	public static final String COL_DATA_PATH = "data_path";
	
	private DatabaseHelper mDBHelper;
	private Context mCtx;

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);

		}
		
		/**
		 *  DB Table create if doesn't exist
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {

			StringBuilder HidenFolderTableQuery = new StringBuilder();
			HidenFolderTableQuery.append("create table ")
					.append(TABLENAME_HIDDENFOLDER).append("(").append(COL__ID) .append(" integer primary key autoincrement, ")
					.append(COL_FOLDER_THUMBNAIL).append(" blob, ")
					.append(COL_DATA_PATH).append(" text ); ");

			db.execSQL(HidenFolderTableQuery.toString());

		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_HIDDENFOLDER);
			onCreate(db);
		}
	}

	public HiddenFolderDBHelper(Context context) {
		this.mCtx = context;
	}

	/**
	 * <p>DB access<p>
	 * 
	 */
	public HiddenFolderDBHelper open() throws SQLException {

		mDBHelper = new DatabaseHelper(mCtx, ProtectDBHelper.DATABASE_NAME, null, ProtectDBHelper.DATABASE_VERSION);
		
		if(mDBHelper != null)
			mDB = mDBHelper.getWritableDatabase();

		return this;
	}

	/**
	 * <p>get DB list of Hidden folder<p>
	 * 
	 * @return HashMap HashMap<String, byte[]>
	 */
	public HashMap<String, byte[]> getHiddenFolderDataHashMap() {

		HashMap<String, byte[]> HideFolderMap = new HashMap<String, byte[]>();
		HideFolderMap.clear();

		Cursor cur = null;
		mDB = mDBHelper.getWritableDatabase();

		try {

			String query = "SELECT  data_path, data_thumbnail "
					+ "FROM HiddenFolderTable ORDER BY _id ";

			cur = mDB.rawQuery(query, null);
			cur.moveToFirst();

			while (!cur.isAfterLast()) {
				HideFolderMap.put(
						cur.getString(cur.getColumnIndex(COL_DATA_PATH)),
						cur.getBlob(cur.getColumnIndex(COL_FOLDER_THUMBNAIL)));

				cur.moveToNext();
			}
		} catch (Exception ex) {
			return HideFolderMap;
		} finally {
			if (null != cur && !cur.isClosed()) {
				cur.close();
			}
		}

		return HideFolderMap;
	}

	/**
	 * <p>Store to DB after hide a folder item<p>
	 * 
	 * @param path path of hidden folder
	 * @param image First thumbnail of hidden folder
	 * @return rowID Stored DB ID
	 */
	public long insertHiddenFolderData(String path, byte[] image) {
		ContentValues cv = new ContentValues();
		cv.put(COL_FOLDER_THUMBNAIL, image);
		cv.put(COL_DATA_PATH, path);

		if (mDB == null) {
			mDB = mDBHelper.getWritableDatabase();
		}
		long rowID = mDB.insert(TABLENAME_HIDDENFOLDER, null, cv);

		cv.clear();

		return rowID;
	}

	/**
	 * <p>DB list delete after unhide hidden folder <p>
	 * 
	 * @param cpath folder path of the item to be deleted
	 * @param image folder thumbnail of the item to be deleted
	 */
	public void deleteHiddenFolderData(String cpath, byte[] image) {
		String where = COL_DATA_PATH + "=?";

		mDB.delete(TABLENAME_HIDDENFOLDER, where,
				new String[] { String.valueOf(cpath) });
	}

	/**
	 * <p>All DB list remove  <p>
	 */
	public void deleteAllHiddenFolderData() {
		mDB.delete(TABLENAME_HIDDENFOLDER, null, null);
	}

	/**
	 * DB close.
	 */
	public void close() {
		mDB.close();
	}

	/**
	 * DB test
	 */
	public static void runBackup(Context context) {
		File file = context.getDatabasePath("PhotoDeskHiddenFolder.db");
		int size = (int) file.length();

		String path = Environment.getExternalStorageDirectory() + "/PhotoDesk/";
		try {
			byte[] buffer = new byte[size];
			InputStream inputStream = new FileInputStream(file);
			inputStream.read(buffer);
			inputStream.close();

			File outputDBDirectory = new File(path);
			if (!outputDBDirectory.isDirectory())
				outputDBDirectory.mkdir();

			path += "test.db";

			File outputFile = new File(path);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			outputStream.write(buffer);
			outputStream.flush();
			outputStream.close();

		} catch (Exception e) {
		}
	}

}
