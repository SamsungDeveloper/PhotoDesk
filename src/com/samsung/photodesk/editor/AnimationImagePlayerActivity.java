package com.samsung.photodesk.editor;

import java.io.IOException;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.samsung.photodesk.BaseActivity;
import com.samsung.photodesk.R;
import com.samsung.photodesk.util.Setting;
import com.samsung.samm.common.SAMMLibConstants;
import com.samsung.spensdk.applistener.AnimationProcessListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

/**
 * <p>SAMM file Play Activity</p>
 * Play SAMM  file with background music and voice audio.
 */
public class AnimationImagePlayerActivity extends BaseActivity 
		implements AnimationProcessListener, OnClickListener, SPenTouchListener{

	public static final String ANIMATION_DATA = "animation_data";
	public static final int REFRESH_ANIMATION_SETTING = 0;
	
	private RelativeLayout mAnimationFrame;
	
	private PhotoDeskScanvasView mSCanvas;
	private AnimationData mData;
	private MediaPlayer mMediaPlayer;
	
	private ProgressBar mProgressBar;
	private ImageButton mStart;
	private ImageButton mStop;
	
	private boolean mIsPlay;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.animation_player);
		
		mData= getIntent().getParcelableExtra(ANIMATION_DATA);
		init();
	}
	
	/**
	 * <p>Initialization function of views in AnimationImagePlayerActivity</p>
	 */
	public void init() {
		if (mData == null)	finish();
		
		mSCanvas = (PhotoDeskScanvasView)findViewById(R.id.preview_scanvas);
		mSCanvas.initSCanvas(0.0f, mData.getAnimationPath(), true);
		mSCanvas.setSPenTouchListener(this);
		mSCanvas.setAnimationProcessListener(this);
		
		mMediaPlayer = new MediaPlayer();
		
		updateMediaPlayerSettings();
		
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(100);
		mProgressBar.setVisibility(View.VISIBLE);
		
		mStart = (ImageButton)findViewById(R.id.startBtn);
		mStop = (ImageButton)findViewById(R.id.stopBtn);
		mStart.setOnClickListener(this);
		mStop.setOnClickListener(this);

		mAnimationFrame = (RelativeLayout) findViewById(R.id.animation_frame);
		
		((RelativeLayout) findViewById(R.id.preview)).setOnClickListener(this);
		RelativeLayout sCanvasContainer = (RelativeLayout) findViewById(R.id.preview_scanvas_container);
		sCanvasContainer.setOnClickListener(this);
		
		((ImageButton) findViewById(R.id.back_btn)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.setting_btn)).setOnClickListener(this);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == REFRESH_ANIMATION_SETTING) {
			updateMediaPlayerSettings();
			mSCanvas.reLoadSAMMFile();
			
			
			mStart.setVisibility(View.GONE);
			mStop.setVisibility(View.VISIBLE);
		}
	};
	
	/**
	 * <p>Toggle animation control frame</p>
	 */
	public void toggleAnimationFrame() {
		if (mAnimationFrame.getVisibility() == View.VISIBLE) {
			setAnimationFrameVisibility(View.GONE);
		} else {
			setAnimationFrameVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * <p>set animation activate animation</p>
	 * @param visibility	frame state ({@link View.GONE}, {@link View.VISIBLE})
	 */
	public void setAnimationFrameVisibility(int visibility){
		int aniId = R.anim.actionbar_fade_out;
		if (visibility == View.VISIBLE) {
        	aniId = R.anim.actionbar_fade_in;
        }
            
        Animation animation = AnimationUtils.loadAnimation(this, aniId);
        animation.setDuration(300);
        mAnimationFrame.startAnimation(animation);
        mAnimationFrame.setVisibility(visibility);	        
    }
	
	/**
	 * <p>Stop animation play</p>
	 * When animation stopped, background music and voice audio is stopped
	 */
	public void stopAnimation() {
		mSCanvas.doAnimationStop(false);
		
		mStart.setVisibility(View.VISIBLE);
		mStop.setVisibility(View.GONE);
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
		} 
	}

	/**
	 * <p>Update background music setting</p>
	 * When change media setting, update mediaPlayer state.
	 */
	public void updateMediaPlayerSettings() {
		boolean replay = Setting.INSTANCE.isBgAutoRePlay();
		final String midiPath = mData.getMidiPath();
		if (replay && mMediaPlayer != null && midiPath != null) {
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					
				@Override
				public void onCompletion(MediaPlayer mp) {
					try {
						mMediaPlayer.setDataSource(midiPath);
						mMediaPlayer.prepare();
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
		} else {
			mMediaPlayer.setOnCompletionListener(null);
		}
		
		mIsPlay = Setting.INSTANCE.isBgPlay();
		if (!mIsPlay) {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
	}

	@Override
	protected void onPause() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		
		if (mSCanvas.getAnimationState() == SAMMLibConstants.ANIMATION_STATE_ON_RUNNING) {
			mSCanvas.doAnimationPause();
			
			mStart.setVisibility(View.VISIBLE);
			mStop.setVisibility(View.GONE);
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			} 
		}
		
		super.onPause();
	}
	
	
	/**
	 * Before finish activity, call it for return resource of scanvas and mediaplayer.
	 * it must be called before finish activity.
	 */
	public void finishActivity() {		
		if (mSCanvas.getAnimationState() ==  SAMMLibConstants.ANIMATION_STATE_ON_RUNNING) {
			mSCanvas.doAnimationPause();
		}
		
		if (mMediaPlayer != null  /*&& mMediaPlayer.isPlaying()*/)	mMediaPlayer.stop();
		finish();
	}
	
	@Override
	public void onBackPressed() {
		finishActivity();
	};
	
	@Override
	protected void onDestroy() {
		mSCanvas.doAnimationClose();
		mSCanvas.closeSCanvasView();
		super.onDestroy();
	};
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mSCanvas.clearSCanvasDataForResizeing(newConfig);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable("key", mData);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);
		
		if (savedInstanceState != null) {
			mData = savedInstanceState.getParcelable("key");
		} 
		
		mSCanvas.initSCanvas(0.0f, mData.getAnimationPath(), true);
	}

	@Override
	public void onPlayComplete() {}
	
	@Override
	public void onChangeProgress(int level) {	
		if (level == 100) {
			mStart.setVisibility(View.VISIBLE);
			mStop.setVisibility(View.GONE);
		}
		mProgressBar.setProgress(level);
		
		if (!mIsPlay)	return;
		
		String midiPath = mData.getMidiPath();
		if(level == 0 && midiPath != null && !mMediaPlayer.isPlaying()) {
		
			try {
				mMediaPlayer.setDataSource(midiPath);
				mMediaPlayer.prepare();
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.startBtn:
			if (mSCanvas.getAnimationState() == SAMMLibConstants.ANIMATION_STATE_ON_PAUSED) {
				mSCanvas.doAnimationResume();
				if (mMediaPlayer != null) {
					try {
						mMediaPlayer.prepare();
						mMediaPlayer.start();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} 
			} else {
				mSCanvas.doAnimationStart();
			}
			
			mStart.setVisibility(View.GONE);
			mStop.setVisibility(View.VISIBLE);
			
			break;
		case R.id.stopBtn:
			mSCanvas.doAnimationPause();
			
			mStart.setVisibility(View.VISIBLE);
			mStop.setVisibility(View.GONE);
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			} 
			break;
			
		case R.id.back_btn:
			finishActivity();	
			break;
			
		case R.id.setting_btn:
			stopAnimation();
			
			Intent intent = new Intent(AnimationImagePlayerActivity.this, EditorSettingActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra(EditorSettingActivity.EDITOR_SETTING_TYPE, EditorSettingActivity.EDITOR_SETTING_TYPE_ANIMATION);
			startActivityForResult(intent, 0);
			break;
		case R.id.preview:			
		case R.id.preview_scanvas_container:
			toggleAnimationFrame();
			break;
		}
	}
	
	@Override
	public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {return false;}
	
	@Override
	public boolean onTouchPen(View arg0, MotionEvent arg1) {
		if (arg1.getAction() == MotionEvent.ACTION_DOWN) {			
			toggleAnimationFrame();
		}
		return false;
	}
	
	@Override
	public boolean onTouchFinger(View arg0, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			toggleAnimationFrame();
		} else if (event.getAction() == (MotionEvent.ACTION_POINTER_DOWN | 0x0100)) {
			if (mSCanvas.getCanvasZoomEnable())	mSCanvas.setZoomEnable(false);			
		}
		return false;
	}
	
	@Override
	public void onTouchButtonUp(View arg0, MotionEvent arg1) {}
	
	@Override
	public void onTouchButtonDown(View arg0, MotionEvent arg1) {}
	
}
