package com.samsung.photodesk.editor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <p>SAMM DB</p>
 * SAMM file path, background music path, voice file path save in DB.
 */
public class SAMMDBHelper {
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

	public static final String TABLENAME_SAMM_INFO = "SAMMFileInfo";
    
	public static final String COL_SAMM_PATH = "path";
	public static final String COL_SAMM_MIDI_PATH = "midi";
	public static final String COL_SAMM_VOICE_PATH = "voice";

   
	private static final String DATABASE_NAME = "SAMM.db";
    private static final int DATABASE_VERSION = 1;
    

    /**
     * <p>Create and Update DB class</p>
     */
    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
 
        @Override
        public void onCreate(SQLiteDatabase db) {
        	StringBuilder imagePersonNumTableQuery = new StringBuilder();
        	imagePersonNumTableQuery.append("create table ").append(TABLENAME_SAMM_INFO).append("(")
        	.append(COL_SAMM_PATH).append(" text, ")
        	.append(COL_SAMM_MIDI_PATH).append(" text, ")
        	.append(COL_SAMM_VOICE_PATH).append(" text) ");
        	
            db.execSQL(imagePersonNumTableQuery.toString());
 
        }
        

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	db.execSQL("DROP TABLE IF EXISTS "+ TABLENAME_SAMM_INFO);
            onCreate(db);
        }
    }
 

    public SAMMDBHelper(Context context){
        this.mCtx = context;
    }
 

    /**
     * <p>Open DB</p>
     * @throws SQLException	{@link SQLException}
     */
    public void open() throws SQLException{
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }
 
    /**
     * <p>insert SAMM data</p>
     * @param path		SAMM file path
     * @param midi		background music path
     * @param voice		voice file path
     */
    public void addSAMMInfo(String path, String midi, String voice) {
    	open();
    	
    	ContentValues cv = new ContentValues();
		cv.put(COL_SAMM_PATH, path);
		cv.put(COL_SAMM_MIDI_PATH, midi);
		cv.put(COL_SAMM_VOICE_PATH, voice);
				
		mDB.insert(TABLENAME_SAMM_INFO, null, cv);
		
		close();
    }
    
    /**
     * <p>Copy SAMM data</p>
     * @param basePath	 base SAMM file path
     * @param copyPath	 copy SAMM file path
     */
    public void copySAMMInfo(String basePath, String copyPath) {
    	AnimationData data = getSAMMInfos(basePath);
		
		String midi = null, voice = null;
		if (data != null) {
			midi = data.getMidiPath();
			voice = data.getVoicePath();
		}					
		addSAMMInfo(copyPath, midi, voice);
    }
    
    /**
     * <p>Move SAMM data</p>
     * @param basePath	 base SAMM file path
     * @param movePath	 move SAMM file path
     */
    public void moveSAMMInfo(String basePath, String movePath) {
    	AnimationData data = getSAMMInfos(basePath);
		
		String midi = null, voice = null;
		if (data != null) {
			midi = data.getMidiPath();
			voice = data.getVoicePath();
		}					
		addSAMMInfo(movePath, midi, voice);		
		removeSAMMInfo(basePath);
    }
    

    /**
     * <p>Get SAMM file info</p>
     * @param path		samm file path
     * @return			{@link AnimationData}
     */
    public AnimationData getSAMMInfos(String path) {
    	open();
    	
    	AnimationData data = null;
    	
    	String[] columns = {COL_SAMM_PATH, COL_SAMM_MIDI_PATH, COL_SAMM_VOICE_PATH};
    	String where = COL_SAMM_PATH + " = '" + path +"'";
    	Cursor c = mDB.query(TABLENAME_SAMM_INFO, columns, where, null, null, null, null);
    	
    	if (c.moveToNext()) {
    		data = new AnimationData(path, c.getString(1), c.getString(2)/*, 0, 0*/);
    	}
    	
    	c.close();
    	close();
    	
    	return data;
    }

    /**
     * <p>Get voice path</p>
     * @param path	samm file path
     * @return		voice path
     */
    public String getVoiceData(String path) {
    	AnimationData data = getSAMMInfos(path);
    	
    	if (data != null && data.getVoicePath() != null) {
    		open();
    		String[] columns = {COL_SAMM_PATH, COL_SAMM_MIDI_PATH, COL_SAMM_VOICE_PATH};
        	String where = COL_SAMM_VOICE_PATH + " = '" + data.getVoicePath() +"'";
        	Cursor c = mDB.query(TABLENAME_SAMM_INFO, columns, where, null, null, null, null);
        	
        	if (c.getCount() == 1)	return data.getVoicePath();
        	
        	c.close();
        	close();
    	}
    	
    	return null;
    }
    
    /**
     * <p>Remove SAMM info</p>
     * @param path	samm file path
     */
    public void removeSAMMInfo(String path) {
    	open();
    	String where = COL_SAMM_PATH + " = '" + path + "'";
    	mDB.delete(TABLENAME_SAMM_INFO, where, null);
    	close();
    }
	
	/**
	 * <p>close DB</p>
	 */
    public void close(){
        mDB.close();
    }
    
}
