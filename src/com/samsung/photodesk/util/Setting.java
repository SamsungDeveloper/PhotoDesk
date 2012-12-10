package com.samsung.photodesk.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.samsung.photodesk.R;
import com.samsung.samm.common.SOptionPlay;
import com.samsung.samm.common.SOptionSAMM;

/**
 * <p>Setting Instance</p>
 * Set Shared Preference
 */

public enum Setting  {
	INSTANCE;
    
    //true - supported pen mode , false - does not supported pen mode
    public static final boolean PEN_MODE = true; // --> galuxy S3 set false , Note set true
	
	public static final String PREF_NAME = "PHOTO_DESK_SETTING";
	public static final String KEY_STYLE = "STYLE";
	public static final String KEY_PASSWORD = "PASSWORD";
	public static final String KEY_COMPARE_MODE = "COMPARE_MODE";
	public static final String KEY_INCLUDE_VIDEO = "INCLUDE_VIDEO";
	public static final String KEY_CONTENT_VIEW_MODE = "CONTENT_VIEW_MODE";
	public static final String KEY_FOLDER_VIEW_MODE = "FOLDER_VIEW_MODE";
	public static final String KEY_IMAGE_SAVE_SIZE = "SAMMImagSaveSize";
	public static final String KEY_IMAGE_SAVE_QUALITY = "SAMMImageSaveQuality";
	public static final String KEY_ANIMATION_SPEED = "AnimationSpeed";
	public static final String KEY_BG_PLAY = "BackgroundPlay";
	public static final String KEY_BG_AUTO_REPLAY = "BackgroundAutoReplay";
	public static final String KEY_ANIMATION_EFFECT_SOUND = "AnimationEffectSound";
	public static final String KEY_FIRST_RUN = "KEY_FIRST_RUN";

	public static final int STYLE_BASIC = 0;
	public static final int STYLE_STITCH = 1;
	public static final int STYLE_MINT_CHECK = 2;
	public static final int MAX_STYLE_CNT = 3;
	
	public static final int COMPARE_NAME_ASC = 0;
	public static final int COMPARE_NAME_DESC = 1;
	public static final int COMPARE_DATE_ASC = 2;
	public static final int COMPARE_DATE_DESC = 3;
	
	public static final boolean INCLUDE_VIDEO_ON = true;
	public static final boolean INCLUDE_VIDEO_OFF = false;
	
	public static final int CONTENT_GRID_VIEW_MODE = 0;
	public static final int CONTENT_SLIDE_VIEW_MODE = 1;
	public static final int CONTENT_MAP_VIEW_MODE = 2;
	
	public static final int FOLDER_SIMPLE_VIEW_MODE = 0;
	public static final int FOLDER_LIST_VIEW_MODE = 1;
	public static final int FOLDER_GRID_VIEW_MODE = 2;
	
	private int mStyle = STYLE_BASIC;
	private String mPassword;
	private int mCompareMode = COMPARE_NAME_ASC;
	private boolean mIncludeVideo = INCLUDE_VIDEO_ON;
	private int mContentViewMode = CONTENT_GRID_VIEW_MODE;
	private int mFolderViewMode = FOLDER_SIMPLE_VIEW_MODE;

	private int mSaveSize;
	private int mSaveQuality;
	private int mAnimationSpeed;
	private boolean mBgPlay;
	private boolean mBgAutoRePlay;
	private boolean mAnimationEffectSound;
	private boolean mFristRun;
	
	/**
	 * <p>Instance initialize</p>
	 * Set shared preference value and default value
	 * @param context {@link Context} 
	 */
	public void initialize(Context context) {
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		
		mStyle = settingPref.getInt(KEY_STYLE, STYLE_BASIC);
		mPassword = settingPref.getString(KEY_PASSWORD, "");
		mIncludeVideo = settingPref.getBoolean(KEY_INCLUDE_VIDEO, INCLUDE_VIDEO_ON);
		mCompareMode = settingPref.getInt(KEY_COMPARE_MODE, COMPARE_NAME_ASC);
		mContentViewMode = settingPref.getInt(KEY_CONTENT_VIEW_MODE, CONTENT_GRID_VIEW_MODE);
		mFolderViewMode = settingPref.getInt(KEY_FOLDER_VIEW_MODE, FOLDER_SIMPLE_VIEW_MODE);
		mSaveSize = settingPref.getInt(KEY_IMAGE_SAVE_SIZE, SOptionSAMM.SAMM_SAVE_OPTION_ORIGINAL_SIZE);
		mSaveQuality = settingPref.getInt(KEY_IMAGE_SAVE_QUALITY, SOptionSAMM.SAMM_CONTETNS_QUALITY_MAX);
		mAnimationSpeed = settingPref.getInt(KEY_ANIMATION_SPEED, SOptionPlay.ANIMATION_SPEED_FAST);
		mBgPlay = settingPref.getBoolean(KEY_BG_PLAY, true);
		mBgAutoRePlay = settingPref.getBoolean(KEY_BG_AUTO_REPLAY, false);
		mAnimationEffectSound = settingPref.getBoolean(KEY_ANIMATION_EFFECT_SOUND, true);
		mFristRun = settingPref.getBoolean(KEY_FIRST_RUN, true);
	}
	
	/**
	 * <p>Set first run</p>
	 * Set when first run.
	 * @param context {@link Context}
	 * @param fristRun Set true when first run. Otherwise false
	 */
	public void setFristRun(Context context, boolean fristRun) {
		mFristRun = fristRun;
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putBoolean(KEY_FIRST_RUN, fristRun);
		edit.commit();
	}
	
	/**
	 * Check first run
	 * @return If first run return true. Otherwise false
	 */
	public boolean isFristRun() {
		return mFristRun;
	}
	
	/**
	 * <p>Set theme style</p>
	 * @param context {@link Context}
	 * @param style - selected style index  
	 */
	public void setStyle(Context context, int style) {
		if (style < 0 || style >= MAX_STYLE_CNT) {
			style = STYLE_BASIC;
		}
		
		mStyle = style;
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putInt(KEY_STYLE, style);
		edit.commit();
	}
	
	/**
 	 * <p>Get current style</p>
	 * @return current style
	 */
	public int getStyle() {
		return mStyle;
	}
	
	/**
 	 * <p>Get current theme ID</p>
	 * @return current theme ID
	 */
	public int getThemeId() {
		switch (getStyle()) {
		case STYLE_BASIC: 		return R.style.AppTheme_Type1;
		case STYLE_STITCH: 		return R.style.AppTheme_Type2;
		case STYLE_MINT_CHECK: 	return R.style.AppTheme_Type3;
		default: 				return R.style.AppTheme_Type1;
		}
	}
	
	/**
	 * <p>Get current theme dialog ID</p>
	 * @return current theme dialog ID
	 */
	public int getDialogId(){
	    switch (getStyle()) {
	        case STYLE_BASIC:       return R.style.CustomDialog_Type1;
	        case STYLE_STITCH:      return R.style.CustomDialog_Type2;
	        case STYLE_MINT_CHECK:  return R.style.CustomDialog_Type3;
	        default:                return R.style.CustomDialog_Type1;
	    }
	}
	
	/**
	 * <p>Set sort mode</p>
	 * @param context {@link Context}
	 * @param compareMode - selected sort mode index
	 */
	public void setCompareMode(Context context, int compareMode) {
		mCompareMode = compareMode;
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putInt(KEY_COMPARE_MODE, compareMode);
		edit.commit();
	}
	
	/**
	 * <p>Get current sort mode</p>
	 * @return current sort mode
	 */
	public int getCompareMode() {
		return mCompareMode;
	}
	
	/**
	 * <p>Set content view mode</p>
	 * @param context {@link Context}
	 * @param viewMode - selected content view mode index
	 */
	public void setContentViewMode(Context context, int viewMode) {
		mContentViewMode = viewMode;
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putInt(KEY_CONTENT_VIEW_MODE, viewMode);
		edit.commit();
	}
	
	/**
	 * <p>Get current content view mode</p>
	 * @return current content view mode
	 */
	public int getContentViewMode() {
		return mContentViewMode;
	}	
	
	/**
	 * <p>Set folder view mode</p>
	 * @param context {@link Context}
	 * @param viewMode - selected folder view mode index
	 */
	public void setFolderViewMode(Context context, int viewMode) {
		mFolderViewMode = viewMode;
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putInt(KEY_FOLDER_VIEW_MODE, viewMode);
		edit.commit();
	}
	
	/**
	 * <p>Get current folder view mode</p>
	 * @return current folder view mode
	 */
	public int getFolderViewMode() {
		return mFolderViewMode;
	}		
	
	/**
	 * <p>Set signature password</p>
	 * @param context {@link Context}
	 * @param password - registered password
	 */
	public void setPassword(Context context, String password) {
		mPassword = password;
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putString(KEY_PASSWORD, password);
		edit.commit();
	}
	
	/**
	 * <p>Get registered password</p>
	 * @return registered password
	 */
	public String getPassword() {
        return mPassword;
    }
	
	/**
	 * <p>Set include video flag</p>
	 * @param context {@link Context}
	 * @param invludeVideo true - include video , false - exclude video
	 */
	public void setIncludeVideo(Context context, boolean invludeVideo) {
		mIncludeVideo = invludeVideo;
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putBoolean(KEY_INCLUDE_VIDEO, invludeVideo);
		edit.commit();
	}
	
	/**
	 * <p>Get include video flag</p>
	 * @return true - include video , false - exclude video
	 */
	public boolean getIncludeVideo() {
		return mIncludeVideo;
	}	

	/**
	 * <p>Set save size</p>
	 * @param context {@link Context}
	 * @param saveSize - selected size
	 */
	public void setSaveSize(Context context, int saveSize) {
		mSaveSize = saveSize;
		
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putInt(KEY_IMAGE_SAVE_SIZE, saveSize);
		edit.commit();
	}
	
	/**
	 * <p>Get current save size</p>
	 * @return current save size
	 */
	public int getSaveSize() {
		return mSaveSize;
	}

	/**
	 * <p>Set save quality</p>
	 * @param context {@link Context}
	 * @param saveQuality - selected save quality
	 */
	public void setSaveQuality(Context context, int saveQuality) {
		mSaveQuality = saveQuality;
		
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putInt(KEY_IMAGE_SAVE_QUALITY, saveQuality);
		edit.commit();
	}

	/**
	 * <p>Get current save quality</p>
	 * @return current save quality
	 */
	public int getSaveQuality() {
		return mSaveQuality;
	}

	/**
	 * <p>Set animation speed</p>
	 * @param context {@link Context}
	 * @param animationSpeed - selected animation speed
	 */
	public void setAnimationSpeed(Context context, int animationSpeed) {
		mAnimationSpeed = animationSpeed;
		
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putInt(KEY_ANIMATION_SPEED, animationSpeed);
		edit.commit();
	}

	/**
	 * <p>Get current animation speed</p>
	 * @return current animation speed
	 */
	public int getAnimationSpeed() {
		return mAnimationSpeed;
	}

	/**
	 * <p>Set background music play flag</p>
	 * @param context {@link Context}
	 * @param bgPlay true - include background music , false - exclude background music
	 */
	public void setBgPlay(Context context, boolean bgPlay) {
		mBgPlay = bgPlay;
		
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putBoolean(KEY_BG_PLAY, bgPlay);
		edit.commit();
	}

	/**
	 * <p>Get current background music play flag</p>
	 * @return true - include background music , false - exclude background music
	 */
	public boolean isBgPlay() {
		return mBgPlay;
	}

	/**
	 * <p>Set background music repeat flag</p>
	 * @param context {@link Context}
	 * @param bgAutoRePlay true - repeat , false - once
	 */
	public void setBgAutoRePlay(Context context, boolean bgAutoRePlay) {
		mBgAutoRePlay = bgAutoRePlay;
		
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putBoolean(KEY_BG_AUTO_REPLAY, bgAutoRePlay);
		edit.commit();
	}
	
	/**
	 * <p>Get current repeat flag</p>
	 * @return true - repeat , false - once
	 */
	public boolean isBgAutoRePlay() {
		return mBgAutoRePlay;
	}

	/**
	 * <p>Set animation effect sound flag</p>
	 * @param context {@link Context}
	 * @param animationEffectSound true - animation effect sound on , false - animation effect sound off
	 */
	public void setAnimationEffectSound(Context context, boolean animationEffectSound) {
		mAnimationEffectSound = animationEffectSound;
		
		SharedPreferences settingPref = context.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor edit = settingPref.edit();
		edit.putBoolean(KEY_ANIMATION_EFFECT_SOUND, animationEffectSound);
		edit.commit();
	}

	/**
	 * <p>Get current animation effect sound flag</p>
	 * @return true - animation effect sound on , false - animation effect sound off
	 */
	public boolean isAnimationEffectSound() {
		return mAnimationEffectSound;
	}


}

