package com.samsung.photodesk.editor;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.photodesk.PhotoDeskActivity;
import com.samsung.photodesk.R;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.editor.PhotoDeskScanvasView.OnFinishImageLoad;
import com.samsung.photodesk.editor.RecordView.RecordingListener;
import com.samsung.photodesk.util.FileControlUtil;
import com.samsung.photodesk.util.PhotoDeskUtils;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.view.SelectedFolderDialog;
import com.samsung.photodesk.view.SpenDialog;
import com.samsung.photodesk.view.SelectedFolderDialog.SelectedFolderCallback;
import com.samsung.photodesk.view.SelectedFolderDialog.SelectedNewFolderCallback;
import com.samsung.samm.common.SAMMLibConstants;
import com.samsung.samm.common.SObject;
import com.samsung.samm.common.SObjectImage;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.samm.common.SObjectText;
import com.samsung.samm.common.SOptionSAMM;
import com.samsung.samm.common.SOptionSCanvas;
import com.samsung.sdraw.CanvasView;
import com.samsung.spen.lib.input.SPenEvent;
import com.samsung.spen.lib.input.SPenEventLibrary;
import com.samsung.spen.lib.input.SPenLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spen.settings.SettingTextInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SObjectSelectListener;
import com.samsung.spensdk.applistener.SPenHoverListener;
import com.samsung.spensdk.applistener.SPenTouchListener;
import com.samsung.spensdk.applistener.SettingStrokeChangeListener;
import com.samsung.spensdk.applistener.SettingViewShowListener;

/**
 * <p>Image Edit Activity</p>
 * This class offer editing image function and inserting drawing animation function. 
 */
public class ImageEditorActivity extends TouchActivity{
	public static final String EDITOR_SAVED_IMAGE_PATH = "Editor_saved_iamge_path";
	public static final String EDITOR_DUMP_SAMM_DATA = "dump_data";
	
	public static final String EDITOR_TYPE_KEY_PATH = "path";
	
	public static final int EDITOR_RESULT_IMAGE_SELECT = 10;
	
	protected RelativeLayout mSCanvasContainer;
	protected PhotoDeskScanvasView mSCanvas;
	
	private AMSMediaData mAMSMediaData;
	private MediaPlayer mMediaPlayer;
	
	private EditorToolUtil mEditor;
	
	private String mInsertImgPath;
	private boolean mRestartActivity;
	
	private TypedArray mStyleArray;
	
	private EditorPopupWindow mPopupWindow;
	
	private boolean mDrawing;
	private boolean mInsertingClipArt;	
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		mStyleArray = obtainStyledAttributes(R.styleable.AppTheme);
		mEditor = EditorToolUtil.getInstence();
		mAMSMediaData = new AMSMediaData(this);
		
		if (getIntent().getStringExtra(EDITOR_TYPE_KEY_PATH).equals("")
				|| getIntent().getStringExtra(EDITOR_TYPE_KEY_PATH) == null) {
			mSCanvasUtil.setEmptyMode(true);
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.editor_view);
		
		((TextView) findViewById(R.id.title_text)).setText(FileControlUtil.createFileName(getResources().getString(R.string.default_file_name)));
		
		changeIconToDrawingDivision();
		initImageEditorOperation();
		FileControlUtil.createTempFolder();	

		initPopupWindow();
		initSpenHoverEvent();
		initSCanvas();
	}	
	
	/**
	 * <p>Initialization operation of menu</p> 
	 */
	public void initImageEditorOperation() {
		((ImageButton) findViewById(R.id.redo_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.undo_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.save_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.reset_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.back_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.animation_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.audio_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.voicerecord_btn)).setOnClickListener(mMenuE);	
		((ImageButton) findViewById(R.id.filter_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.image_btn)).setOnClickListener(mMenuE);
		((ImageButton) findViewById(R.id.setting_btn)).setOnClickListener(mMenuE);
		
		((ImageButton) findViewById(R.id.note_btn)).setOnTouchListener(mSCanvasDrawingEvent);
		((ImageButton) findViewById(R.id.pencil_btn)).setOnTouchListener(mSCanvasDrawingEvent);
		((ImageButton) findViewById(R.id.clipart_btn)).setOnTouchListener(mSCanvasDrawingEvent);
		((ImageButton) findViewById(R.id.eraser_btn)).setOnTouchListener(mSCanvasDrawingEvent);
		((TextView) findViewById(R.id.title_text)).setOnClickListener(mMenuE);
		
		if (findViewById(R.id.edit_menu_bar) != null) {
			((ImageButton) findViewById(R.id.edit_menu_open_btn)).setOnClickListener(mMenuE);
		}
	}
	
	/**
	 * <p>Initialization popup window</p>
	 */
	public void initPopupWindow() {
		RelativeLayout editArea = (RelativeLayout) findViewById(R.id.editor_area);
		editArea.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPopupWindow.dismissAllPopup();
			}
		});
		
		mPopupWindow = new EditorPopupWindow(editArea, ImageEditorActivity.this);
		mPopupWindow.setPopupWindowShowListener(new EditorPopupWindow.PopupWindowShowListener() {
			
			@Override
			public void show() {
				hideMenuBar();
			}

		});
		
		mPopupWindow.initRecordView(ImageEditorActivity.this, mRecordListener);
	}
	
	/**
	 * <p>Initialization Spen hover event</p>
	 */
	public void initSpenHoverEvent() {
		SPenEventLibrary spenHoverEvent = new SPenEventLibrary();
		spenHoverEvent.setSPenHoverListener(findViewById(R.id.editor), mHoverEvent);
		spenHoverEvent.setSPenHoverListener(findViewById(R.id.editor_area), mHoverEvent);
	}
	

	/**
	 * <p>Change pen/hand activation of button by drawing division option</p>
	 */
	public void changeIconToDrawingDivision() {
		if (!mEditor.isDrawingToolDivision()) {
			((ImageButton) findViewById(R.id.pencil_btn))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconPenOnHand));
			
			((ImageButton) findViewById(R.id.eraser_btn)).setBackgroundResource(R.drawable.default_btn);
			((ImageButton) findViewById(R.id.eraser_btn))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconEraser));
		}
	}

	/**
	 * <p>Initialization AnimationData for SAMM file</p>
	 * @param data 	{@linkAnimationData}
	 */
	public void initAnimationData(AnimationData data) {
		if (data != null) {
			if (data.getMidiPath() != null) {
				for (int i = 1; i < mAMSMediaData.getMediaPaths().size(); i++) {
					if (mAMSMediaData.getMediaPaths().get(i).equals(data.getMidiPath())) {
						mAMSMediaData.setIdx(i);
						break;
					}
				}
			} 
			
			if (data.getVoicePath() != null) {
				if (mPopupWindow.getRecoredView().updateStateWhenEixistFile(data.getVoicePath())) {
					mSCanvas.setBGAudioFile(data.getVoicePath());
					mAMSMediaData.setVoicePath(data.getVoicePath());
				}
			}
		}
	}
	
	/**
	 * <p>Create SCanvas by SCavnas mode</p>
	 * normal mode : {@link PhotoDeskScanvasView}
	 * empty mode : {@link EmptyPhotoDeskSCanvasView}
	 */
	public void createSCanvas() {
		mSCanvasContainer = (RelativeLayout) findViewById(R.id.scanvas_container);
		if (mSCanvasUtil.isEmptyMode()) {
			mSCanvas = new EmptyPhotoDeskSCanvasView(this);
		} else {
 			mSCanvas = new PhotoDeskScanvasView(this);
 		}
		
		mSCanvasContainer.addView(mSCanvas);
	}
	
	/**
	 * <p>Initialization SCanvas</p>
	 */
	public void initSCanvas() {				
		createSCanvas();
		createSettingView();
		
		final String path = getIntent().getStringExtra("path");
		mSCanvas.initSCanvas(mSCanvasUtil.getRotation(path), path, false);			
		mSCanvas.setOnFinishImageLoad(new OnFinishImageLoad() {
			
			@Override
			public void onFinish() {
				if (SCanvasView.isSAMMFile(path)) {	
					initAnimationData(getSAMMInfoToDB(path));			
					boolean exist = mPopupWindow.getRecoredView()
							.updateStateWhenEixistFile(mSCanvas.getBGAudioFile());
					
					if (exist) {
						ImageButton btn = (ImageButton)findViewById(R.id.voicerecord_btn);
						btn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconVoicePlay));
					} else {
						mSCanvas.clearBGAudio();
					}

		 			changeToPlayAbleAniBtn();
				}
				initSCanvasStrokeSetting();
			}
		});
	
		mSCanvas.setSObjectSelectListener(mSObjectSelectListener);
		mSCanvas.setSPenTouchListener(mSPenTouchEvent);
		mSCanvas.setSCanvasHoverPointerStyle(0);
		if (mEditor.isDrawingToolDivision()) 	mSCanvas.setSPenHoverListener(mHoverEvent);
		
	}
	
	/**
	 * <p>Initialization stroke of SCanvas setting in stroke setting view</p>
	 */
	public void initSCanvasStrokeSetting() {
		SettingTextInfo textInfo = new SettingTextInfo();
		textInfo.setTextColor(Color.BLACK);
		textInfo.setTextSize(10);
		textInfo.setTextAlign(SAMMLibConstants.SAMM_ALIGN_NORMAL);
		textInfo.setTextStyle(SObjectText.SAMM_TEXT_STYLE_NONE);
		textInfo.setTextFont("Sans serif");
		mSCanvas.setSettingTextInfo(textInfo);
		
		SettingStrokeInfo eraserInfo = new SettingStrokeInfo();
		eraserInfo.setStrokeStyle(SObjectStroke.SAMM_STROKE_STYLE_ERASER);
		eraserInfo.setStrokeWidth(30);
		mSCanvas.setSettingStrokeInfo(eraserInfo);
	}
	
	/**
	 * <p>Create SCanvas Setting view</p>
	 */
	public void createSettingView() {
		LinearLayout settingContainer = (LinearLayout) findViewById(R.id.setting_container);
		mSCanvas.createSettingView(settingContainer, mSCanvas.getSettingRes(), null);
		setSettingEvent();
	}

	/**
	 * <p>Setting setting view event</p>
	 */
	public void setSettingEvent() {	
		mSCanvas.setSettingStrokeChangeListener(mSettingStrokeChangeEvent);
		mSCanvas.setSettingViewShowListener(new SettingViewShowListener() {
			boolean mFirstEnter = true;
			@Override
			public void onTextSettingViewShow(boolean bVisible) {
				if (bVisible == false)	mEditor.setCurSettingViewStateToHide();
				else 					hideMenuBar();
			}
			
			@Override
			public void onPenSettingViewShow(boolean bVisible) {
				if (mFirstEnter) {
					mFirstEnter = false;
					return;
				}
				if (bVisible == false)	mEditor.setCurSettingViewStateToHide();
				else 					hideMenuBar();
			}
			
			@Override
			public void onFillingSettingViewShow(boolean bVisible) {}
			
			@Override
			public void onEraserSettingViewShow(boolean bVisible) {
				if (bVisible == false)	mEditor.setCurSettingViewStateToHide();
				else 					hideMenuBar();
			}
		});
	
	}

	/**
	 * <p>Set SAMM Option to SCanvas</p>
	 */
	public void setSAMMOption() {
		SOptionSCanvas canvasOption = new SOptionSCanvas();					
		canvasOption.mSAMMOption.setSaveImageSize(Setting.INSTANCE.getSaveSize());
		canvasOption.mSAMMOption.setContentsQuality(Setting.INSTANCE.getSaveQuality());
		mSCanvas.setOption(canvasOption);		
	}
	
	/**
	 * <p>Set SAMM Option to SCanvas</p>
	 * this function is used when save current scanvas state
	 */
	public void setSAMMOriginalOption() {
		SOptionSCanvas canvasOption = new SOptionSCanvas();					
		canvasOption.mSAMMOption.setSaveImageSize(SOptionSAMM.SAMM_SAVE_OPTION_ORIGINAL_SIZE);
		canvasOption.mSAMMOption.setContentsQuality(SOptionSAMM.SAMM_CONTETNS_QUALITY_ORIGINAL);
		mSCanvas.setOption(canvasOption);		
	}
	
	/**
	 * <p>Change Animation play button according to animation play ability</p>
	 */
	public void changeToPlayAbleAniBtn() {
		ImageButton btn = (ImageButton)findViewById(R.id.animation_btn);
		if (btn != null && mStyleArray != null) {
			btn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconAnimaPlay));
		}	
	}

	/**
	 * <p>Show select that save format dialog</p>
	 * @param path	image save path
	 */
	public void showSaveDialog(final String path) {
		String[] saveType = getResources().getStringArray(R.array.image_editor_save_kinds);

		View v = LayoutInflater.from(this).inflate(R.layout.folder_list_dlg, null);
		ArrayList<String> arSaveType = new ArrayList<String>();

		for (int i = 0; i < saveType.length; i++) {
			arSaveType.add(saveType[i]);
		}

		ArrayAdapter<String> Adapter;
		Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, arSaveType);

		final ListView list = (ListView) v.findViewById(R.id.folder_list);
		list.setAdapter(Adapter);
		list.setItemChecked(0, true);

		final SpenDialog dialog = new SpenDialog(this);
		dialog.setContentView(v);
		dialog.setTitle(R.string.save_type);
		dialog.setRightBtn(R.string.save, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				int idx = list.getCheckedItemPosition();

				String fullPath = null;
				if (idx < 1) 	fullPath = saveImage(path);
				else			fullPath = saveSAMMData(path);

				StringBuilder msg = new StringBuilder();
				msg.append(getResources().getString(R.string.save_sucess_filename_is)).append(fullPath);

				Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
			}
		});
		dialog.setLeftBtn(R.string.cancel, null);
		dialog.show();
	}

	
	/**
	 * <p>Set result value</p>
	 * Set refresh setting value that pre Activity
	 * @param savePath 		image save path
	 */
	public void setResult(String savePath) {		
		if (mSCanvasUtil.isEmptyMode()) {
			setResult(RESULT_OK);
		} else {
			if (FileControlUtil.isSameFolder(mSCanvas.getImagepath(), savePath)) {
				Intent intent = new Intent();
				intent.putExtra(EDITOR_SAVED_IMAGE_PATH, savePath);
				setResult(RESULT_OK, intent);
			} else {
				setResult(RESULT_OK);
			}
		}
	}
	
	/**
	 * <p>Save SAMM file info to DB</p>
	 * @param path 	image save path
	 */
	public void saveSAMMInfoToDB(String path) {
		SAMMDBHelper db = new SAMMDBHelper(this);
		db.addSAMMInfo(path, getCurMidiPath(), getCurVoicePath());
	}
	
	/**
	 * <p>Getting AnimationData on DB</p>
	 * @param path	save image path
	 * @return	AnimationData.
	 */
	public AnimationData getSAMMInfoToDB(String path) {
		SAMMDBHelper db = new SAMMDBHelper(this);
		return db.getSAMMInfos(path);
	}
	
	/**
	 * <p>Save SAMM Data</p>
	 * @param path	save image path
	 * @return 		save image full path
	 */
	public String saveSAMMData(String path) {
		String savedPath = FileControlUtil.getAnimationFilePath(path);
		
		if (FileControlUtil.makeDirOfFullPath(path)) {
			setSAMMOption();			
			mSCanvas.saveSAMMFile(savedPath);
		
			FileControlUtil.runMediaScanner(getApplicationContext(), savedPath);

			saveSAMMInfoToDB(savedPath);
			setResult(savedPath);
			
			return savedPath;
		}
		
		return null;
	}

	/**
	 * <p>Save JPG Data</p>
	 * @param path	save image path
	 * @return 		save image full path
	 */
	public String saveImage(String path) {
		StringBuilder savedPath = new StringBuilder();
		savedPath.append(path)
		.append(FileControlUtil.getSaveFileName())
		.append(FileControlUtil.DEFAULE_IMAGE_FORMAT);

		if (FileControlUtil.makeDirOfFullPath(path)) {
			setSAMMOption();			
			FileControlUtil.saveBitmapImage(getApplicationContext(), 
					mSCanvas.getCanvasBitmap(false), savedPath.toString());
			
			saveSAMMInfoToDB(savedPath.toString());
			setResult(savedPath.toString());
			
			return savedPath.toString();
		}
		
		return null;
	}

	/**
	 * <p>Getting selected media file path</p>
	 * @return		selected media file path
	 */
	public String getCurMidiPath() {
		return mAMSMediaData.getPath(mAMSMediaData.getIdx());
	}

	/**
	 * <p>Getting selected voice file path</p>
	 * @return		selected voice file path
	 */
	public String getCurVoicePath() {
		return mSCanvas.getBGAudioFile();
	}
	
	/**
	 * <p>play animation on {@link AnimationImagePlayer}</p>
	 */
	public void playAnimation() {	
		String tempVoicePath = mPopupWindow.getRecoredView().stopPlayerOrRecorder();
		if (tempVoicePath != null)	mSCanvas.setBGAudioFile(tempVoicePath);
		
		closeAllWindows();
		
		String midiPath = getCurMidiPath();
		String voicePath = getCurVoicePath();
		String path = FileControlUtil.getTempAnimationFilePath(FileControlUtil.DEFAULE_IMAGE_FORMAT);

		setSAMMOption();
		mSCanvas.saveSAMMFile(path);

		AnimationData data = new AnimationData(path, midiPath, voicePath);

		Intent intent = new Intent(this, AnimationImagePlayerActivity.class);
		intent.putExtra(AnimationImagePlayerActivity.ANIMATION_DATA, data);
		startActivity(intent);
	}

	/**
	 * <p>Initialization media player</p>
	 */
	public void initMediaPlayer() {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
		} else {
			mMediaPlayer.reset();
		}
	}
	
	/**
	 * <p>Create background music list dialog</p>
	 * @param seletedIdx		selected background music index
	 * @param musicList			background music array list
	 * @return					background music list dialog
	 */
	public View createDialogBgListView(int seletedIdx, ArrayList<String> musicList) {
		ArrayAdapter<String> adapter 
			= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, musicList);
		
		View v = LayoutInflater.from(this).inflate(R.layout.folder_list_dlg, null);
		
		ListView list = (ListView) v.findViewById(R.id.folder_list);
		list.setAdapter(adapter);
		list.setItemChecked(seletedIdx, true);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AudioManager audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
				final float vol = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				
				initMediaPlayer();
				mAMSMediaData.setIdx(position);
				mMediaPlayer.reset();

				if (position == 0)	return;

				try {
					mMediaPlayer.setDataSource(mAMSMediaData.getPath(position));
					mMediaPlayer.prepare();
					mMediaPlayer.setVolume(vol, vol);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mMediaPlayer.start();
			}

		});
		
		return v;
	}
	
	/**
	 * <p>Show Background music list dialog</p>
	 */
	public void showBgListDialog() {
		View v = createDialogBgListView(mAMSMediaData.getIdx(), mAMSMediaData.getTitles());
		
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setContentView(v);
		dialog.setTitle(R.string.music);
		dialog.setRightBtn(R.string.select, new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAMSMediaData.setIdx(mAMSMediaData.getIdx());
				initMediaPlayer();
				mMediaPlayer.stop();
				dialog.dismiss();
			}
		});
		dialog.setLeftBtn(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				initMediaPlayer();
				mMediaPlayer.stop();
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	/**
	 * <p>Show drawing reset dialog</p>
	 */
	public void showResetWringDialog() {
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setTitle(getResources().getString(R.string.warning));
		dialog.setLeftBtn(R.string.no, null);
		dialog.setRightBtn(R.string.yes, new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSCanvas.clearScreen();
				changeAnimationPlayState(false);
				dialog.dismiss();

			}
		});
		dialog.setContentView(getResources().getString(R.string.editor_quest_reset_drawing_data), 
				getResources().getDimension(R.dimen.base_text_size));
		dialog.show();
	}

	/**
	 * <p>Show Image Editor finish warning dialog</p>setb
	 */
	public void showEndWarningDialog() {
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setTitle(getResources().getString(R.string.warning));
		dialog.setLeftBtn(R.string.no, null);
		dialog.setRightBtn(R.string.yes, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
		dialog.setContentView(getResources().getString(R.string.editor_question_exit),
				getResources().getDimension(R.dimen.base_text_size));
		dialog.show();
	}
	
	/**
	 * <p>Show changing title dialog</p>
	 */
	public void showChangeTitleDialog(){
		final TextView titleTv = (TextView) findViewById(R.id.title_text);
		View v = LayoutInflater.from(this).inflate(R.layout.input_text_dlg, null);
		final EditText edit = (EditText) v.findViewById(R.id.etFoldername);

		edit.setText(titleTv.getText());
		edit.selectAll();

		final SpenDialog dialog = new SpenDialog(this);
		dialog.setContentView(v);
		dialog.setTitle(R.string.please_enter_a_name);
		dialog.setmWindowType(SpenDialog.CUSTOM_INPUT_DIALOG);
		dialog.setLeftBtn(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setRightBtn(R.string.save, new OnClickListener() {

			@Override
			public void onClick(View v) {
				String fileName = edit.getText().toString();
				FileControlUtil.setSaveFileName(fileName);
				titleTv.setText(fileName);
				dialog.dismiss();
			}
		});
		dialog.show();

	}
	
	/**
	 * <p>Return touch type</p> 
	 * @param event		{@link SPenEvent}
	 * @return			touch type (EditorToolUtil.TYPE_PEN, EditorToolUtil.TYPE_HAND)
	 */
	public int getEventType(SPenEvent event) {
		return (event.isPen()) ? EditorToolUtil.TYPE_PEN : EditorToolUtil.TYPE_HAND;
	}
	
	@Override
	public void onBackPressed() {		
		showEndWarningDialog();
	}

	
	@Override
	protected void onPause() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		
		if (mPopupWindow.getRecoredView().isPlay())	{
			mPopupWindow.getRecoredView().stopPlayer();
		}
 		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		MemoryClearUtils.recursiveRecycle(getWindow().getDecorView());
		mPopupWindow.destroyView();

		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}

		mSCanvas.closeSCanvasView();
		mSCanvasUtil.destoryData();
		mEditor.clearData();
		
		System.gc();
		super.onDestroy();
	}
	
	private OnClickListener mMenuE = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.save_btn:
				showFolderListDialog();
				break;

			case R.id.animation_btn:
				if (mSCanvas.isUndoable())	playAnimation();
				break;

			case R.id.audio_btn:
				showBgListDialog();
				break;

			case R.id.voicerecord_btn:		
				if (mPopupWindow.getRecoredView() == null) {
					mPopupWindow.initRecordView(ImageEditorActivity.this, mRecordListener);
				}
				mPopupWindow.showPopupWindow(EditorPopupWindow.POPUP_WINDOW_TYPE_VOICE);
				break;

			case R.id.redo_btn:
				mSCanvas.redo();
				break;

			case R.id.undo_btn:
				mSCanvas.undo();
				break;

			case R.id.reset_btn:
				showResetWringDialog();
				break;

			case R.id.back_btn:
				showEndWarningDialog();
				break;

			case R.id.setting_btn:
				Intent intent = new Intent(ImageEditorActivity.this, EditorSettingActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				break;

			case R.id.filter_btn:
				if (mPopupWindow.getFilterView() == null) {
					mPopupWindow.initFilterView(ImageEditorActivity.this, mSCanvas,
							mSCanvasUtil.getRotation(mSCanvas.getImagepath()));	
				}
				mPopupWindow.showPopupWindow(EditorPopupWindow.POPUP_WINDOW_TYPE_FILTER);
				break;
				
			case R.id.image_btn:
				startSelectImage();
				break;
				
			case R.id.title_text:
				showChangeTitleDialog();
				break;
				
			case R.id.edit_menu_open_btn:
				if ((findViewById(R.id.edit_menu_bar)).getVisibility() == View.VISIBLE) {
					((ImageButton)v).setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconTopBarOpenArrow));
					setMenuBarVisibility(View.GONE);
				} else {
					closeAllWindows();
					((ImageButton)v).setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconTopBarCloseArrow));
					setMenuBarVisibility(View.VISIBLE);
				}
					
				break;
			}

		}
	};
	
	/**
	 * <p>Start Other Gallery</p>
	 */
	public void startSelectImage() {
		Intent intent = new Intent(); 
		intent.setAction(Intent.ACTION_GET_CONTENT);				
		intent.putExtra(PhotoDeskActivity.CHANGE_VIDEO_SETTING, 
				PhotoDeskUtils.isIncludeVideoSettingChange());
		intent.setType("image/*");
		startActivityForResult(intent, EDITOR_RESULT_IMAGE_SELECT);
		
		hideMenuBar();
	}
	
	/**
	 * <p>Change menuBar visibility state</p>
	 * @param visibility menuBar visibility
	 */
	public void setMenuBarVisibility(int visibility){
		int aniId = R.anim.actionbar_fade_out;
		if (visibility == View.VISIBLE) {
        	aniId = R.anim.actionbar_fade_in;
        }
            
        Animation animation = AnimationUtils.loadAnimation(this, aniId);
        animation.setDuration(300);
        
        LinearLayout menuBar = (LinearLayout)findViewById(R.id.edit_menu_bar);
        menuBar.startAnimation(animation);
        menuBar.setVisibility(visibility);	        
    }
	
	/**
	 * <p>Close all popup and close setting view</p>
	 */
	public void closeAllWindows() {
		mPopupWindow.dismissAllPopup();
		mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
	}
	
	/**
	 * <p>Change animation button state after checking animation play possibility</p>
	 * @param state	whether animation play possibility
	 */
	private void changeAnimationPlayState(boolean state) {		
		if (state && getAnimationPlayState() == 0) {
			mSCanvas.putExtra(PhotoDeskScanvasView.EDITOR_EXTRA_DATA_ANI, 1);
			
			ImageButton animationBtn = (ImageButton)findViewById(R.id.animation_btn);
			if (animationBtn != null)	animationBtn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconAnimaPlay));
		} else if (!state && getAnimationPlayState() == 1) {
			mSCanvas.putExtra(PhotoDeskScanvasView.EDITOR_EXTRA_DATA_ANI, 0);
			
			ImageButton animationBtn = (ImageButton)findViewById(R.id.animation_btn);
			if (animationBtn != null)	animationBtn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconAnima));
		}
	}
	
	/**
	 * <p>Getting animation play possibility</p>
	 * @return	animation play possibility
	 */
	private int getAnimationPlayState() {
		return mSCanvas.getIntExtra(PhotoDeskScanvasView.EDITOR_EXTRA_DATA_ANI, 0); 
	}

	private SObjectSelectListener mSObjectSelectListener = new SObjectSelectListener() {
		
		@Override
		public void onSObjectSelected(SObject sObject, boolean bSelected) {
		
			if (mEditor.isGalleryImageSelectMode())	return;
			if (bSelected &&
					mEditor.getMode(mSCanvas.getCurTouchEventType()) 
						!= SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
				mSCanvas.setCanvasMode(mEditor.getMode(mSCanvas.getCurTouchEventType()));
			}
			
		}
	};
	
	
	private SPenTouchListener mSPenTouchEvent = new SPenTouchListener() {
		
		@Override
		public boolean onTouchPenEraser(View view, MotionEvent event) { return false; }
		
		@Override
		public boolean onTouchPen(View view, MotionEvent event) {	
			
			changeAnimationPlayState(true);
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onePointTouchDown(event, EditorToolUtil.TYPE_PEN);
				break;
				
			case MotionEvent.ACTION_UP:
				if (mInsertingClipArt) {
					int idx = mEditor.getClipArtPosition(getEventType(SPenLibrary.getEvent(event)));
					mSCanvas.insertClipArt((int)event.getX(), (int)event.getY(), 
							mPopupWindow.getSelctedClipArtImage(idx));
					mInsertingClipArt = false;
				}
				mDrawing = false;
				break;
				
			case MotionEvent.ACTION_MOVE:
				if(mSCanvas.isDrawing() && mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_NONE) {
					mSCanvas.cancelDrawing();
				}
				break;

			}
			
			return false;
		}
		
		/**
		 * <p>Whether Two fingers in SObjectImage location.</p>
		 * @param event		{@link MotionEvent}
		 * @return			Whether Two fingers in SObjectImage location
		 */
		public boolean isSObjectDoublePointTouch(MotionEvent event) {
			SObject sobject = mSCanvas.getSelectedSObject();
			if (sobject == null)	return false;
			
			if (sobject instanceof SObjectImage) {
				RectF rect = sobject.getRect();
				if (rect.contains(event.getX(0), event.getY(0)) &&
						rect.contains(event.getX(1), event.getY(1))) 	return true;
			}
			
			return false;
		}
		
		@Override
		public boolean onTouchFinger(View view, MotionEvent event) {
			if (event.getPointerCount() == 2 && !isSObjectDoublePointTouch(event)) {
				mInsertingClipArt = false;
				if (mSCanvas.getSelectedSObjectRect() != null)	{
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_DEFAULT);
				}
				return doubleTouchEvent(mSCanvasContainer, event);
			}
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				changeAnimationPlayState(true);
				onePointTouchDown(event, EditorToolUtil.TYPE_HAND);
				break;
				
			case MotionEvent.ACTION_UP:
				if (mInsertingClipArt) {
					int idx = mEditor.getClipArtPosition(getEventType(SPenLibrary.getEvent(event)));
					mSCanvas.insertClipArt((int)event.getX(), (int)event.getY(), 
							mPopupWindow.getSelctedClipArtImage(idx));
					mInsertingClipArt = false;
				}

				mDrawing = false;
				break;
				
			case MotionEvent.ACTION_MOVE:
				if(mSCanvas.isDrawing() && mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_NONE) {
					mSCanvas.cancelDrawing();
				}
				break;
			}
			
			return false;
		}
		
		/**
		 * <p>Change SCanvas mode by mode of pen or hand</p>
		 * @param touchType		touch type (EditorToolUtil.TYPE_PEN, EditorToolUtil.TYPE_HAND)
		 */
		private void setSCanvasMode(int touchType) {
			if (mSCanvas.getCanvasMode() != mEditor.getMode(touchType)) {
				mSCanvas.setCanvasMode(mEditor.getMode(touchType));			
			}
		}
		
		/**
		 * <p>It called when one point touch down</p>
		 * @param event			{@link MotionEvent}
		 * @param touchType		touch type (EditorToolUtil.TYPE_PEN, EditorToolUtil.TYPE_HAND)
		 */
		@SuppressWarnings("deprecation")
		private void onePointTouchDown(MotionEvent event, int eventTouchType) {
 			mSCanvas.setCurTouchEventType(eventTouchType);
			mDrawing = true;
		
			if (changeModeWhenNoneDrawingDivision(event))	return;
			if (mEditor.isGalleryImageSelectMode()) {
				if (isExistPositionInSelectedImage(event))		return;
			}
			
			switch(mEditor.getMode(eventTouchType)) {
			case SCanvasConstants.SCANVAS_MODE_INPUT_PEN:
			case SCanvasConstants.SCANVAS_MODE_INPUT_ERASER:
			case SCanvasConstants.SCANVAS_MODE_INPUT_TEXT:
				setSCanvasMode(eventTouchType);
				changeStrokeSetting(eventTouchType);
				break;
			case SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE:
				mSCanvas.changeModeTo(CanvasView.SELECT_MODE);
				mInsertingClipArt = true;
				break;

			}	
			
			changeScanvasSettingView(eventTouchType);
			dismissPopupWindow();
		}
		
		@Override
		public void onTouchButtonUp(View view, MotionEvent event) {}
		
		@Override
		public void onTouchButtonDown(View view, MotionEvent event) {}
		

		/**
		 * <p>Change Setting view by mode pen or hand</p>
		 * @param touchType	touch type (EditorToolUtil.TYPE_PEN, EditorToolUtil.TYPE_HAND)
		 */
		public void changeScanvasSettingView(int touchType) {
			if (findViewById(R.id.edit_menu_bar) != null && 
				mEditor.getCurSettingViewEventType() != -1) {
				mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
				return ;
			}

			int reverseType = mEditor.getReverseType(touchType);
			if (mEditor.getMode(touchType) == SCanvasConstants.SCANVAS_MODE_INPUT_PEN &&
					mEditor.getMode(reverseType) == SCanvasConstants.SCANVAS_MODE_INPUT_PEN && 
					(mEditor.isShowSettingView(touchType) | mEditor.isShowSettingView(reverseType)))	{

				mEditor.setSettingViewState(touchType, true);
				mEditor.changeStrokeSettingView(mSCanvas, touchType);
			}
		}
	};
	
	/**
	 * <p>Change mode when none drawing division</p>
	 * @param event		{@link MotionEvent}
	 * @return			whether change
	 */
	@SuppressWarnings("deprecation")
	public boolean changeModeWhenNoneDrawingDivision(MotionEvent event) {
		if (!mEditor.isDrawingToolDivision()) {
			if (mEditor.getMode(EditorToolUtil.TYPE_HAND) == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
				mSCanvas.changeModeTo(CanvasView.SELECT_MODE);
				mInsertingClipArt = true;

			}
			return false;
		}
		
		return false;
	}
	
	public void dismissPopupWindow() {
		if (findViewById(R.id.edit_menu_bar) != null) {
			mPopupWindow.dismissAllPopup();
		} else {
			mPopupWindow.dismissNoDrawPopup();
		}
	}
	
	public void hideMenuBar() {
		LinearLayout menuBar = (LinearLayout)findViewById(R.id.edit_menu_bar);
		if (menuBar != null)	menuBar.setVisibility(View.GONE);
	}
	
	/**
	 * <p>if Scanvas mode is stroke mode, chanage Scanvas stroke setting by touch type (pen, hand)</p>
	 * @param touchType		touch type (EditorToolUtil.TYPE_PEN, EditorToolUtil.TYPE_HAND)
	 */
	public void changeStrokeSetting(int touchType) {
		if (mSCanvas.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN &&
				!mEditor.isSameStroke(touchType, mSCanvas.getSettingStrokeInfo())) {
			mEditor.changeStrokeSetting(mSCanvas, touchType);
		}
	}
	
	/**
	 * <p>Whether position is exist in selected image</p>
	 * @param e		{@link MotionEvent}
	 * @return		Whether position is exist in selected image
	 */
	public boolean isExistPositionInSelectedImage(MotionEvent e) {
		RectF rec = mSCanvas.getSelectedSObjectRect();
		
		if (!rec.contains(e.getX() * mSCanvasUtil.getMultipleNum(), e.getY() * mSCanvasUtil.getMultipleNum())) {
			mEditor.setGalleryImageSelectMode(false);
			return false;
		}
		
		return true;
	}
	
	private OnTouchListener mSCanvasDrawingEvent = new OnTouchListener() {
		final int BOTH_IDX = 2;
		
		final int penIconIds[] = {R.styleable.AppTheme_editIconPenOnPen, R.styleable.AppTheme_editIconPenOnHand, R.styleable.AppTheme_editIconPenOnBoth, R.styleable.AppTheme_editIconPen};
		final int eraserIconIds[] = {R.styleable.AppTheme_editIconEraserOnPen, R.styleable.AppTheme_editIconEraserOnHand, R.styleable.AppTheme_editIconEraserOnBoth, R.styleable.AppTheme_editIconEraser};
		final int textIconIds[] = {R.styleable.AppTheme_editIconTextOnPen, R.styleable.AppTheme_editIconTextOnHand, R.styleable.AppTheme_editIconTextOnBoth, R.styleable.AppTheme_editIconText};
		final int clipartIconIds[] = {R.styleable.AppTheme_editIconAddPicOnPen, R.styleable.AppTheme_editIconAddPicOnHand, R.styleable.AppTheme_editIconAddPicOnBoth, R.styleable.AppTheme_editIconAddPic};
		
		@Override
		public boolean onTouch(View v, MotionEvent e) {
			if(e.getAction() == MotionEvent.ACTION_DOWN) {
				SPenEvent event = SPenLibrary.getEvent(e);
				int selectMode = SCanvasConstants.SCANVAS_MODE_INPUT_PEN;
				
				switch (v.getId()) {
				case R.id.pencil_btn:
					selectMode = SCanvasConstants.SCANVAS_MODE_INPUT_PEN;
					break;
					
				case R.id.eraser_btn:
					selectMode = SCanvasConstants.SCANVAS_MODE_INPUT_ERASER;
					break;
					
				case R.id.note_btn:
					selectMode = SCanvasConstants.SCANVAS_MODE_INPUT_TEXT;
					break;
					
				case R.id.clipart_btn:
					selectMode = SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE;
					break;
				}
				
				changeDrawingState(event, (ImageView)v, selectMode);
				mPopupWindow.dismissNoDrawPopup();
				return true;
			}

			return false;
		}
		
		/**
		 * <p>Change drawing state when none division</p>
		 * @param event			{@link SPenEvent}
		 * @param iv			pen or hand Image view in menu icon
		 * @param selectMode	select mode
		 */
		private void changeDrawingStateWhenNoneDivision(SPenEvent event, ImageView iv, int selectMode) {
			if (mEditor.getMode(EditorToolUtil.TYPE_HAND) == selectMode) {	
				mSCanvas.toggleShowSettingView(getSettingMode(selectMode));
				mEditor.setSettingViewState(EditorToolUtil.TYPE_HAND, true);
				
				if (selectMode == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
					if (mPopupWindow.getClipArtView() == null) {
						mPopupWindow.initClipArtView(ImageEditorActivity.this);
					}
					
					mPopupWindow.showPopupWindow(EditorPopupWindow.POPUP_WINDOW_TYPE_CLIPART);
					mEditor.setSettingViewState(EditorToolUtil.TYPE_HAND, true);
					if (mEditor.isShowSettingView(EditorToolUtil.TYPE_HAND)) {
						mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
					}
				}
			} else {					
				rollbackButtonImage(mEditor.getMode(EditorToolUtil.TYPE_HAND));
				mSCanvas.setCanvasMode(selectMode);
				
				if (selectMode == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
					if (mPopupWindow.getClipArtView() == null) {
						mPopupWindow.initClipArtView(ImageEditorActivity.this);
					}
					
					mPopupWindow.showPopupWindow(EditorPopupWindow.POPUP_WINDOW_TYPE_CLIPART);
					mEditor.setSettingViewState(EditorToolUtil.TYPE_HAND, true);
					if (mEditor.isShowSettingView(EditorToolUtil.TYPE_HAND)) {
						mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
					}
				} else {
					mPopupWindow.dismissAllPopup();
					mEditor.setSettingViewState(EditorToolUtil.TYPE_HAND, false);
				}
				
				mEditor.setMode(EditorToolUtil.TYPE_HAND, selectMode);
				iv.setBackgroundDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editToolsLayerBG));
				iv.setImageDrawable(mStyleArray.getDrawable(getImageResourceId(selectMode, EditorToolUtil.TYPE_HAND)));
			}
		}
		
		/**
		 * <p>Change drawing state when drawing division</p>
		 * @param event			{@link SPenEvent}
		 * @param iv			pen or hand Image view in menu icon
		 * @param selectMode	select mode
		 */
		private void changeDrawingState(SPenEvent event, ImageView iv, int selectMode) {
			if (!mEditor.isDrawingToolDivision()) {
				changeDrawingStateWhenNoneDivision(event, iv, selectMode);				
				return;
			}
			
			int eventType = getEventType(event);			
			if (selectMode == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) { 
				changeButtonImage(iv, eventType, selectMode);
				mEditor.setMode(eventType, selectMode);
				toggleSettingView(eventType, selectMode);
			} else if(selectMode != mEditor.getMode(eventType)) {
				changeButtonImage(iv, eventType, selectMode);
				mEditor.setMode(eventType, selectMode);
				
				mPopupWindow.dismissAllPopup();
				if (mSCanvas.isSettingViewVisible(selectMode)) {
					toggleSettingView(eventType, selectMode);
				}
				
				if(mSCanvas.isSObjectSelected()) {
					mSCanvas.cancelDrawing();
				}
				
			} else {
				toggleSettingView(eventType, selectMode);
			}

		}
		
		private boolean changeButtonImage(ImageView iv, int event, int selectMode) {
			int reverseType = mEditor.getReverseType(event);
			int resourceId = -1;
			
			if (mEditor.getMode(reverseType) != selectMode) {
				resourceId = getImageResourceId(selectMode, event);
			} else {				
				resourceId = getImageResourceId(selectMode, BOTH_IDX);
			}
			
			rollbackButtonImage(event, reverseType);
			
			iv.setBackgroundDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editToolsLayerBG));
			iv.setImageDrawable(mStyleArray.getDrawable(resourceId));
			
			return true;
		}
		
		private void rollbackButtonImage(int preMode) {
			int id = getResourceId(preMode);
			int res[] = getResourceArray(preMode);

			ImageView iv = (ImageView)findViewById(id);
			iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.default_btn));
			iv.setImageDrawable(mStyleArray.getDrawable(res[res.length-1]));

		}
		
		private int[] getResourceArray(int event) {
			switch (mEditor.getMode(event)) {
			case SCanvasConstants.SCANVAS_MODE_INPUT_PEN:
				return penIconIds;
				
			case SCanvasConstants.SCANVAS_MODE_INPUT_ERASER:
				return eraserIconIds;
				
			case SCanvasConstants.SCANVAS_MODE_INPUT_TEXT:
				return textIconIds;
				
			case SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE:
				return clipartIconIds;
			}
			
			return null;
		}
		
		private int getResourceId(int event) {
			switch (mEditor.getMode(event)) {
			case SCanvasConstants.SCANVAS_MODE_INPUT_PEN:
				return R.id.pencil_btn;
				
			case SCanvasConstants.SCANVAS_MODE_INPUT_ERASER:
				return R.id.eraser_btn;
				
			case SCanvasConstants.SCANVAS_MODE_INPUT_TEXT:
				return R.id.note_btn;
				
			case SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE:
				return R.id.clipart_btn;
			}
			
			return -1;
		}
		
		private void rollbackButtonImage(int event, int reverseEvent) {
			int id = getResourceId(event);
			int res[] = getResourceArray(event);

			boolean changeBg = true;
			int resourceId = res.length-1;
			if (mEditor.getMode(reverseEvent) == mEditor.getMode(event)) {
				changeBg = false;
				resourceId = reverseEvent; 
			}
			
			ImageView iv = (ImageView)findViewById(id);
			if (changeBg) {
				iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.default_btn));
			}
			
			iv.setImageDrawable(mStyleArray.getDrawable(res[resourceId]));

		}

		private int getImageResourceId(int canvasMode, int eventType) {
			int id = -1;
			
			switch (canvasMode) {
			case SCanvasConstants.SCANVAS_MODE_INPUT_PEN:
				return penIconIds[eventType];
			case SCanvasConstants.SCANVAS_MODE_INPUT_ERASER:
				return eraserIconIds[eventType];
			case SCanvasConstants.SCANVAS_MODE_INPUT_TEXT:
				return textIconIds[eventType];
			case SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE:
				return clipartIconIds[eventType];
			}
			
			return id;
		}		
	};
	
	/**
	 * <p>Toggle setting view type</p>
	 * @param touchType			touch type (EditorToolUtil.TYPE_PEN, EditorToolUtil.TYPE_HAND)
	 * @param selectMode		SCanvas mode
	 */
	private void toggleSettingView(int touchType, int selectMode) {
		if (selectMode == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
			toggleImageSettingView(touchType, selectMode);
		} else {
			int settingMode = getSettingMode(selectMode);
			boolean visible = false;

			if (!mSCanvas.isSettingViewVisible(settingMode) 
					|| mEditor.getCurSettingViewEventType() != touchType) {	
				visible = true;
			} 
			
			mEditor.setSettingViewState(touchType, visible);		
			
			if (visible == true) {
				hideMenuBar();				
				
				mSCanvas.setCanvasMode(selectMode);
				mSCanvas.showSettingView(getSettingMode(mEditor.getMode(touchType)), true);
				mEditor.changeStrokeSettingView(mSCanvas, touchType);
			} else if(visible == false) {
				mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
			}
			
			mPopupWindow.dismissAllPopup();
		}
	}
	
	private void toggleImageSettingView(int touchType, int selectMode) {
		if (mPopupWindow.isShowing(EditorPopupWindow.POPUP_WINDOW_TYPE_CLIPART)
				&& mEditor.getClipArtSelectState() == EditorClipArtGridView.BOTH) {
			mPopupWindow.dismissAllPopup();
			
		} else {
			changeStrokeSetting(touchType);
			mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
			if (mPopupWindow.getClipArtView() == null) {
				mPopupWindow.initClipArtView(ImageEditorActivity.this);
			}
			mPopupWindow.showPopupWindow(EditorPopupWindow.POPUP_WINDOW_TYPE_CLIPART);
		}
	}
	
	/**
	 * <p>Get setting mode</p>
	 * @param selectMode	Scanvas mode
	 * @return				SCanvas Setting mode
	 */
	private int getSettingMode(int selectMode) {
		switch (selectMode) {
		case SCanvasConstants.SCANVAS_MODE_INPUT_PEN:
			return SCanvasConstants.SCANVAS_SETTINGVIEW_PEN;

		case SCanvasConstants.SCANVAS_MODE_INPUT_ERASER:
			return SCanvasConstants.SCANVAS_SETTINGVIEW_ERASER;

		case SCanvasConstants.SCANVAS_MODE_INPUT_TEXT:
			return SCanvasConstants.SCANVAS_SETTINGVIEW_TEXT;

		case SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE:
			return SCanvasConstants.SCANVAS_SETTINGVIEW_UNKNOWN;
		}
		
		return SCanvasConstants.SCANVAS_SETTINGVIEW_PEN;
	}
	
	private SettingStrokeChangeListener mSettingStrokeChangeEvent = new SettingStrokeChangeListener() {
		
		@Override
		public void onStrokeWidthChanged(int strokeWidth) {
			int touchType = mEditor.getCurSettingViewEventType();
			if (touchType != -1 && mDrawing == false) {
				mEditor.setStrokeInfo(touchType, PhotoDeskStrokeInfo.STROKE_WIDTH, strokeWidth);
			}
			
		}
		
		@Override
		public void onStrokeStyleChanged(int strokeStyle) {
			int touchType = mEditor.getCurSettingViewEventType();
			if (touchType != -1 && mDrawing == false) {
				mEditor.setStrokeInfo(touchType, PhotoDeskStrokeInfo.STROKE_STYLE, strokeStyle);
				mEditor.changeStrokeSettingView(mSCanvas, touchType);

			}
		}
		
		@Override
		public void onStrokeColorChanged(int strokeColor) {
			int touchType = mEditor.getCurSettingViewEventType();
			if (touchType != -1 && mDrawing == false) {
				mEditor.setStrokeInfo(touchType, PhotoDeskStrokeInfo.STROKE_COLOR, strokeColor);
			}
		}
		
		@Override
		public void onStrokeAlphaChanged(int strokeAlpha) {}
		
		@Override
		public void onEraserWidthChanged(int eraserWidth) {}
		
		@Override
		public void onClearAll(boolean arg0) {}

	};
	
	SPenHoverListener mHoverEvent = new SPenHoverListener() {
		@Override
		public boolean onHover(View arg0, MotionEvent arg1) {
			return false;
		}


		@Override
		public void onHoverButtonDown(View arg0, MotionEvent arg1) {}


		@Override
		public void onHoverButtonUp(View arg0, MotionEvent arg1) {
			if (mSCanvas.isSettingViewVisible(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN)) {
				mSCanvas.showSettingView(SCanvasConstants.SCANVAS_SETTINGVIEW_PEN, false);
				mEditor.setSettingViewState(EditorToolUtil.TYPE_PEN, false);
			} else {
				int mode = mEditor.getMode(EditorToolUtil.TYPE_PEN);
				int settingMode = getSettingMode(mode);
				
				if (settingMode == SCanvasConstants.SCANVAS_SETTINGVIEW_UNKNOWN) {
					mPopupWindow.showPopupWindow(EditorPopupWindow.POPUP_WINDOW_TYPE_CLIPART);
					return;
				}
				
				mPopupWindow.dismissAllPopup();
				mSCanvas.showSettingView(settingMode, true);
				mEditor.setSettingViewState(EditorToolUtil.TYPE_PEN, true);
				mEditor.changeStrokeSettingView(mSCanvas, EditorToolUtil.TYPE_PEN);
				hideMenuBar();
			}
			
		}

	};
	
	
	/**
	 * <p>Show folder list dialog</p>
	 */
	public void showFolderListDialog() {
		SelectedFolderDialog dlg = new SelectedFolderDialog(this);
		final SpenDialog dialog = new SpenDialog(this);
		dlg.setOnSelectedFolder(new SelectedFolderCallback() {
			
			@Override
			public void onSelectedFolder(int position, FolderItem folderItem) {
				showSaveDialog(folderItem.getPath());
			}
		});
		
		dlg.setOnSelectedNewFolder(new SelectedNewFolderCallback() {

			@Override
			public void onSelectedNewFolder() {
				final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.input_text_dlg, null);
				dialog.initContentView();
				dialog.setContentView(view);
				dialog.setTitle(R.string.folder_name);
				dialog.setLeftBtn(R.string.cancel, null);
				dialog.setRightBtn(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						EditText edit = (EditText) view.findViewById(R.id.etFoldername);
						showSaveDialog(PhotoDeskUtils.getDefualtFodler() + edit.getText().toString().trim() + "/");
					}
				});
				dialog.show();
			}
		});
		
		dlg.show();
	}		

	@Override
	protected void onSaveInstanceState(Bundle outState) {		
		if (mSCanvas.isUndoable()) {
			setSAMMOriginalOption();
			String path = FileControlUtil.getTempAnimationFilePath(FileControlUtil.DEFAULE_IMAGE_FORMAT);
			mSCanvas.saveSAMMFile(path);

			AnimationData data = new AnimationData(
					path, 
					getCurMidiPath(), 
					getCurVoicePath());
			
			outState.putParcelable(EDITOR_DUMP_SAMM_DATA, data);
		}
		
		super.onSaveInstanceState(outState);
	}
	

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {		
		final AnimationData data = savedInstanceState.getParcelable(EDITOR_DUMP_SAMM_DATA);
		
		mRestartActivity = true;
		
		if (data != null) {			
			initAnimationData(data);

			mSCanvas.initSCanvas(0.0f, data.getAnimationPath(), false);
			mSCanvas.setOnFinishImageLoad(new OnFinishImageLoad() {
					
				@Override
				public void onFinish() {
					initAnimationData(getSAMMInfoToDB(data.getAnimationPath()));			
					boolean exist = mPopupWindow.getRecoredView()
							.updateStateWhenEixistFile(mSCanvas.getBGAudioFile());
					
					if (exist) {
						ImageButton btn = (ImageButton)findViewById(R.id.voicerecord_btn);
						btn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconVoicePlay));
					} else {
						mSCanvas.clearBGAudio();
					}

					changeToPlayAbleAniBtn();
					insertImageWhenRestart();
					
					mSCanvas.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
				}
			});
		} else if (data == null && mSCanvasUtil.isEmptyMode()){
			mSCanvas.setOnFinishImageLoad(new OnFinishImageLoad() {
				
				@Override
				public void onFinish() {
					insertImageWhenRestart();
				}
			});
		}
		
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	/**
	 * <p>re insert image when restart activity</p>
	 */
	public void insertImageWhenRestart() {
		if (mRestartActivity && mInsertImgPath != null) {
			mSCanvas.insertImage(mInsertImgPath);
			mEditor.setGalleryImageSelectMode(true);
			mRestartActivity = false;
			mInsertImgPath = null;
		}
	}
	
	private RecordingListener mRecordListener = new RecordingListener() {
		ImageButton mVoiceBtn;
		
		@Override
		public void start() {
			initVoiceBtn();
			mVoiceBtn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconVoiceing));
		}
		
		@Override
		public void complete(String path) {
			initVoiceBtn();
			
			mVoiceBtn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconVoicePlay));				
			mSCanvas.setBGAudioFile(path);
		}

		@Override
		public void deleteRecordingData() {
			initVoiceBtn();
			
			mVoiceBtn.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_editIconVoice));
			mSCanvas.clearBGAudio();
		
			mAMSMediaData.setVoicePath(null);
		}

		public void initVoiceBtn() {
			if (mVoiceBtn == null) {
				mVoiceBtn = (ImageButton)findViewById(R.id.voicerecord_btn);
			}
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == EDITOR_RESULT_IMAGE_SELECT && resultCode == RESULT_OK) {	
			Uri imageUri = data.getData();
			mInsertImgPath = FileControlUtil.getRealPathFromURI(this, imageUri);
			if (!FileControlUtil.isValidImage(mInsertImgPath)) {
				mInsertImgPath = null;
				return;
			}
			if (!mRestartActivity)	{
				mEditor.setGalleryImageSelectMode(true);
				mSCanvas.insertImage(mInsertImgPath);
			}
		} 
	}
	
	/**
	 * <p>Initialization SCanvasContainer location</p>
	 */
	public void initSCanvasContainerLocation() {
		mSCanvasContainer.setTranslationX(0);
		mSCanvasContainer.setTranslationY(0);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mSCanvas.clearSCanvasDataForResizeing(newConfig);
		initSCanvasContainerLocation();
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void finish() {
	    if (mStyleArray != null) mStyleArray.recycle();
	    super.finish();
	}

}
