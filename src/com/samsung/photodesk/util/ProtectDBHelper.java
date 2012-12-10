package com.samsung.photodesk.util;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <p>Protect DataBase Class</p>
 * 
 */
public class ProtectDBHelper {
	private SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mContext;
	
	protected final String TABLENAME_PROTECT = "ProtectTable";
    
	protected final String COL__ID = "_id";
	protected final String COL_SDCARD_CID = "sdcard_cid";
	protected final String COL_DATA_PATH = "data_path";
   
	protected static final String DATABASE_NAME = "PhotoDeskProtect.db";
	protected static final int DATABASE_VERSION = 2;
    
 
    /**
     * <p>Create / Upgrade DataBase Class</p>
     */
    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
        	StringBuilder protectTableQuery = new StringBuilder();
        	protectTableQuery.append("create table ").append(TABLENAME_PROTECT).append("(")
        	.append(COL__ID).append(" integer primary key autoincrement, ")
        	.append(COL_SDCARD_CID).append(" text, ")
        	.append(COL_DATA_PATH).append(" text ); ");
        	
            db.execSQL(protectTableQuery.toString());
            createHiddenFolderDB(db);
        }
        
        private void createHiddenFolderDB(SQLiteDatabase db) {
            StringBuilder HidenFolderTableQuery = new StringBuilder();
            HidenFolderTableQuery.append("create table ")
                    .append(HiddenFolderDBHelper.TABLENAME_HIDDENFOLDER).append("(").append(COL__ID)
                    .append(" integer primary key autoincrement, ")
                    .append(HiddenFolderDBHelper.COL_FOLDER_THUMBNAIL).append(" blob, ")
                    .append(COL_DATA_PATH).append(" text ); ");

            db.execSQL(HidenFolderTableQuery.toString());
        }
        

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1) {
                createHiddenFolderDB(db);
            }
        }
    }
 
    /**
     * <p>Constructor</p>
     * @param context {@link Context}
     */
    public ProtectDBHelper(Context context){
        this.mContext = context;
    }
 
    /**
     * <p>Create DataBase</p>
     * @throws SQLException
     */
    public ProtectDBHelper open() throws SQLException{
        mDBHelper = new DatabaseHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();

        return this;
    }
 
    /**
     * <p>Create HashMap</p>
     * @param cid - external memory's CID
     * @return HashMap
     */
	public HashMap<String, String> getProtectDataHashMap(String cid) {
		HashMap<String, String> protectMap = new HashMap<String, String>();
		Cursor cur = null;

		try {
			String query = "SELECT data_path FROM ProtectTable where sdcard_cid = '" + cid + "'";
			
			cur = mDB.rawQuery(query, null);
			cur.moveToFirst();
			
			while(!cur.isAfterLast()) {
				protectMap.put(cur.getString(0), "Protected");
				
				cur.moveToNext();
			}
		} catch(Exception ex) {
			return protectMap;
		} finally {
			if(null != cur && !cur.isClosed()) {
				cur.close();
			}
		}
		
		return protectMap;
	}

	/**
	 * <p>Insert DataBase</p>
	 * @param path - selected item's file path
	 * @param cid - external memory's CID
	 */
	public void insertProtectData(String path , String cid) {
		ContentValues cv = new ContentValues();
		cv.put(COL_SDCARD_CID, cid);
		cv.put(COL_DATA_PATH, path);
		mDB.insert(TABLENAME_PROTECT, null, cv);
	}

	/**
	 * <p>Delete DataBase</p>
	 * @param path - selected item's file path
	 * @param cid - external memory's CID
	 */
	public void deleteProtectData(String path , String cid) {
		String where = COL_DATA_PATH + "=? AND " + COL_SDCARD_CID + "=?";

		mDB.delete(TABLENAME_PROTECT, where, new String[] {String.valueOf(path) , String.valueOf(cid)});
	}	
	
	/**
	 * <p>Delete all DataBase</p>
	 * @param cid - external memory's CID
	 */
	public void deleteAllProtectData(String cid) {
		String where = COL_SDCARD_CID + "=?";

		mDB.delete(TABLENAME_PROTECT, where, new String[] {String.valueOf(cid)});
	}		
	
	/**
	 * <p>Close DataBase</p>
	 */
    public void close(){
        mDB.close();
    }
}
