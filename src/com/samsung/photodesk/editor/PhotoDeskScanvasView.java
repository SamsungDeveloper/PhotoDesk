package com.samsung.photodesk.editor;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.samsung.photodesk.R;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.view.CustomProgressDialog;
import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;

/**
 * <p>Custom SCanvas</p>
 * PhotoDeskScanvasView have Auto resizing and 
 * Initialization by file type(SAMM, JPG) by default
 */
public class PhotoDeskScanvasView extends SCanvasView {
	
	public static final String EDITOR_EXTRA_DATA_ANI = "Able_To_Animation";
	
	protected float mSCanvasWidth;
	protected float mSCanvasHeight;
	
	private float mPreSCanvasWidth;

	protected float mRotation;

	protected String mImagePath;
	
	protected boolean initSize;
	
	protected PhotoDeskSCanvasManager mSCanvasUtil;

	public OnFinishImageLoad mOnFinishLoad;
	
	protected int mFirstOrientation = -1;
	
	public int curTouchEventType = -1;

	public PhotoDeskScanvasView(Context context, AttributeSet as) {
		super(context, as);
		
		initSize = false;
		mSCanvasUtil = PhotoDeskSCanvasManager.getInstence(context);
		
		mSCanvasUtil.setMultipleNum(1.0f);
		setFirstOrientation(context);
	}
	
	public PhotoDeskScanvasView(Context context) {
		super(context);
		
		initSize = false;
		mSCanvasUtil = PhotoDeskSCanvasManager.getInstence(context);
		
		mSCanvasUtil.setMultipleNum(1.0f);
		setFirstOrientation(context);
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (initSize == false) {
			float parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			float parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			
			boolean isXLargeScreen = 
					(getResources().getConfiguration().screenLayout 
							& Configuration.SCREENLAYOUT_SIZE_MASK) 
							== Configuration.SCREENLAYOUT_SIZE_XLARGE;
			
			if (isXLargeScreen) {
				parentHeight -= (int) getResources().getDimension(R.dimen.actionbar_size);
			}
			
			mSCanvasUtil.setSCanvasParentSize((int) parentWidth, (int) parentHeight);

			initSCanvasSize(parentWidth, parentHeight);
			setMultipleNum();

	 		initSize = true;
		}

		
		if (mSCanvasWidth != 0.0f)	{
			setMeasuredDimension((int) mSCanvasWidth, (int) mSCanvasHeight);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setZoomEnable(false);
		setPanEnable(false);
	}
	

	/**
	 * <p>Initialization SCanvas size</p>
	 * @param parentWidth	parent view width
	 * @param parentHeight	parent view height
	 */
	public void initSCanvasSize(float parentWidth, float parentHeight) {
		BitmapFactory.Options options = mSCanvasUtil.getBitmapSize(mImagePath);
		mSCanvasWidth = options.outWidth;
		mSCanvasHeight = options.outHeight;
		
		if (mRotation == 90 || mRotation == 270) {
			float dump = mSCanvasWidth;
			mSCanvasWidth = mSCanvasHeight;
			mSCanvasHeight = dump;

		}
		
		resizingSCanvsWidth(parentWidth, parentHeight); 		
	}
	
	/**
	 * <p>Resizing SCanvas</p>
	 * @param parentWidth	parent view width
	 * @param parentHeight	parent view height
	 */
	public void resizingSCanvsWidth(float parentWidth, float parentHeight) {
		float scale = (mSCanvasWidth > mSCanvasHeight) ? (parentWidth / mSCanvasWidth)
				: (parentHeight / mSCanvasHeight);

		if (scale <= 1) {
			mSCanvasWidth *= scale;
			mSCanvasHeight *= scale;
		}
		
		scale = 1.0f;
		if (mSCanvasWidth > parentWidth)	scale = parentWidth / mSCanvasWidth;
 		if (mSCanvasHeight > parentHeight)	scale = parentHeight / mSCanvasHeight;
 		
 		if (scale <= 1) {
 			mSCanvasWidth *= scale;
 			mSCanvasHeight *= scale;
 		}
	}

	/**
	 * <p>Initialization by file type</p>
	 * @param rotation		Image rotation
	 * @param path			Image path
	 * @param isAnimation	whether is SAMM file
	 */
	public void initSCanvas(float rotation, String path, final boolean isAnimation) {
		mRotation = rotation;
		mImagePath = path;

		if (SCanvasView.isSAMMFile(mImagePath)) {
			setSCanvasInitializeListener(new SCanvasInitializeListener() {

				@Override
				public void onInitialized() {
					setRemoveLongPressStroke(false);
					setTextLongClickSelectOption(false);
					
					SAMMDataLoader loader = new SAMMDataLoader(isAnimation);
					loader.execute();
				}
			});

		} else {
			putExtra(EDITOR_EXTRA_DATA_ANI, 0);
			setSCanvasInitializeListener(new SCanvasInitializeListener() {

				@Override
				public void onInitialized() {
					setRemoveLongPressStroke(false);
					setTextLongClickSelectOption(false);

					ImageLoader loader = new ImageLoader();
					loader.execute();
				}
			});
		}
		
		setRemoveLongPressStroke(false);
		setEraserCursorVisible(true);
		setScrollDrawing(true);
		setTextLongClickSelectOption(false);
	
	}
	
	
	public HashMap<String, Integer> getSettingRes() {
		HashMap<String, Integer> settingRes = new HashMap<String, Integer>();
		settingRes.put(SCanvasConstants.LAYOUT_PEN_SPINNER, R.layout.mspinner);
		settingRes.put(SCanvasConstants.LAYOUT_TEXT_SPINNER, R.layout.mspinnertext);
		settingRes.put(SCanvasConstants.LAYOUT_TEXT_SPINNER_TABLET, R.layout.mspinnertext_tablet);

		settingRes.put(SCanvasConstants.LOCALE_PEN_SETTING_TITLE, R.string.pen_setting);
		settingRes.put(SCanvasConstants.LOCALE_ERASER_SETTING_TITLE, R.string.eraser_setting);
		settingRes.put(SCanvasConstants.LOCALE_TEXT_SETTING_TITLE, R.string.text_setting);
		
		settingRes.put(SCanvasConstants.LOCALE_ERASER_SETTING_CLEARALL, R.string.clear_all);
		
		settingRes.put(SCanvasConstants.LOCALE_TEXT_SETTING_TAB_FONT, R.string.text_settings_tab_font);
		settingRes.put(SCanvasConstants.LOCALE_TEXT_SETTING_TAB_PARAGRAPH, R.string.text_settings_tab_paragraph);
		settingRes.put(SCanvasConstants.LOCALE_TEXT_SETTING_TAB_PARAGRAPH_ALIGN, R.string.text_settings_tab_paragraph_align);
		settingRes.put(SCanvasConstants.LOCALE_TEXTBOX_HINT, R.string.textbox_hint);
		
		return settingRes;
	}
	
	/**
	 * <p>Reload SAMM file</p>
	 */
	public void reLoadSAMMFile() {
		SAMMDataLoader loader = new SAMMDataLoader(true);
		loader.execute();
	}
	
	/**
	 * <p>SAMM file loader</p>
	 * SAMM file load in background by AsyncTask.
	 */
	class SAMMDataLoader extends AsyncTask<Void, Void, Void> {

		boolean isAnimation;
		CustomProgressDialog mProgressDialog;
		
		public SAMMDataLoader(boolean isAnimation) {
			mProgressDialog = CustomProgressDialog.show(getContext(), 
					getResources().getString(R.string.editor_image_loading_title), 
					getResources().getString(R.string.editor_image_loading_msg));
			this.isAnimation = isAnimation;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			loadSAMMFile(mImagePath, false, false);
			
			if (isAnimation) {
				updateAnimationSettings();
				setAnimationMode(true);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (isAnimation)	doAnimationStart();
			else loadSAMMFile(mImagePath, true, true);
			
			if (mOnFinishLoad != null)	mOnFinishLoad.onFinish();
			if (mProgressDialog != null)	mProgressDialog.dismiss();
			setVisibility(View.GONE);
			setVisibility(View.VISIBLE);
			
		}
		
	}

	/**
	 * <p>jpg file loader</p>
	 * jpg file load in background by AsyncTask.
	 */
	class ImageLoader extends AsyncTask<Void, Void, Bitmap> {
		CustomProgressDialog mProgressDialog;
		
		public ImageLoader() {
			mProgressDialog = CustomProgressDialog.show(getContext(), 
					getResources().getString(R.string.editor_image_loading_title), 
					getResources().getString(R.string.editor_image_loading_msg));
		}

		protected Bitmap doInBackground(Void... param) {
			BitmapFactory.Options opts = mSCanvasUtil.getResizeImageOption(
					mImagePath, (int) mSCanvasWidth, (int) mSCanvasHeight);
			Bitmap bitmap = BitmapFactory.decodeFile(mImagePath, opts);

			if (mRotation != 0) {
				Matrix m = new Matrix();
				m.setRotate(mRotation);
				Bitmap image = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(), bitmap.getHeight(), m, false);
				bitmap.recycle();

				setBackgroundImage(image);
				
				return image;
			}

			
			setBackgroundImage(bitmap);
	    
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap image) {	
			if (mOnFinishLoad != null)	mOnFinishLoad.onFinish();
			if (mProgressDialog != null)	mProgressDialog.dismiss();
			
			setVisibility(View.GONE);
			setVisibility(View.VISIBLE);
		}
		
	}
	
	/**
	 * <p>Set orientation when first enter</p>
	 * @param context
	 */
	public void setFirstOrientation(Context context) {
		if (mFirstOrientation == -1) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(displayMetrics);

			mFirstOrientation = (displayMetrics.widthPixels > displayMetrics.heightPixels) ?
					Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
		}		
	}
	
	/**
	 * <p>get Orientation when first enter</p>
	 * @return	first orientation
	 */
	public int getFisrtOrientation() {
		return mFirstOrientation;
	}

	/**
	 * <p>Set SCanvas Animation Setting</p>
	 */
	public void updateAnimationSettings() {
		SOptionSCanvas option = new SOptionSCanvas();
		
		AudioManager audioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
		float vol = (float)audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		option.mPlayOption.setBGAudioVolume(vol);
		option.mPlayOption.setInvisibleBGImageAnimationOption(false);
		option.mPlayOption.setAnimationSpeed(Setting.INSTANCE.getAnimationSpeed());
		
		boolean replay = Setting.INSTANCE.isBgAutoRePlay();
		option.mPlayOption.setRepeatBGAudioOption(replay);
		
		option.mPlayOption.setStopBGAudioOption(true);
		option.mPlayOption.setSoundEffectOption(Setting.INSTANCE.isAnimationEffectSound());
		
		boolean isPlay = Setting.INSTANCE.isBgPlay();
		option.mPlayOption.setPlayBGAudioOption(isPlay);
	
		setOption(option);
	}
	
	public void setOnFinishImageLoad(OnFinishImageLoad finish) {
		mOnFinishLoad = finish;
	}

	/**
	 * <p>Set image rotation</p>
	 */
	public void setRotation(float rotation) {
		this.mRotation = rotation;
	}

	/**
	 * Interface for OnFinishImageLoad
	 *
	 */
	public interface OnFinishImageLoad {
		public abstract void onFinish();
	}

	/**
	 * <p>Remove SCanvas size</p>
	 */
	public void clearSCanvasSize() {
		initSize = false;
	}
	
	/**
	 * <p>Set Multiple Num</p>
	 */
	public void setMultipleNum() {
		if (mSCanvasUtil.getMultipleNum() == -1.0f) {
			mSCanvasUtil.setMultipleNum(mPreSCanvasWidth/mSCanvasWidth);
		}
	}
	
	/**
	 * <p>clear SCanvas data that related size</p>
	 * when rotate Phone, clear SCanvas data that related size for Resize.
	 * @param newConfig		new Configuration.
	 */
	public void clearSCanvasDataForResizeing(Configuration newConfig) {
		if (newConfig.orientation == mFirstOrientation) {
			mSCanvasUtil.setMultipleNum(1.0f);
		} else {
			mSCanvasUtil.setMultipleNum(-1.0f);
			mPreSCanvasWidth = mSCanvasWidth;			
		}
	
		clearSCanvasSize();
		setZoomEnable(true);
	} 

	/**
	 * 
	 * @param centerX
	 * @param centerY
	 * @param clipArtBitmap
	 */
	public void insertClipArt(int centerX, int centerY, Bitmap clipArtBitmap) {
		RectF rect = mSCanvasUtil.getClipArtRect(clipArtBitmap, centerX, centerY);		
		
		SObjectImage img = new SObjectImage();
		img.setImageBitmap(clipArtBitmap);			
		img.setRect(rect);

		RectF rec = getSelectedSObjectRect();
		if (rec.left == 0.0f && rec.right == 0.0f && rec.top == 0.0f && rec.bottom == 0.0f) {
			if (selectSAMMObject(centerX, centerY) == null) {
				insertSAMMImage(img, false);
			}
		}
	}
	
	public void insertImage(String path) {	
		int maxSize = mSCanvasUtil.getInsertedImageMaxSize();
		Bitmap bitmap = BitmapFactory.decodeFile(path, 
				mSCanvasUtil.getResizeImageOption(path, maxSize, maxSize));
	
		RectF rect = mSCanvasUtil.getInsertedImageRect(bitmap.getWidth(), 
				bitmap.getHeight(), getFisrtOrientation());	

		SObjectImage img = new SObjectImage();
		img.setImageBitmap(bitmap);			
		img.setRect(rect);
		
		insertSAMMImage(img, true);
	}
	

	public int getCurTouchEventType() {
		return curTouchEventType;
	}

	public void setCurTouchEventType(int curTouchEventType) {
		this.curTouchEventType = curTouchEventType;
	}
	
	public String getImagepath() {
		return mImagePath;
	}
}
