package com.samsung.photodesk.editor;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.samsung.photodesk.R;
import com.samsung.photodesk.util.FileControlUtil;

/**
 * <p>Voice Record and play view</p>
 */
public class RecordView extends LinearLayout implements OnClickListener{

	private static final int PROGRESS_MAX = 18000;	

	public static final int RECORD_STATE_POSSIBLE_RECORD = 10;
	public static final int RECORD_STATE_RECORDING = 11;
	public static final int RECORD_STATE_EXIST_FILE = 12; 
	
	private Button mRecordingStartBtn, mRecordingSaveBtn, mMidiStopBtn, mPlayBtn, mRemoveBtn;
	private TextView mRecordedTime, mMaxTime;
	private ProgressBar mProgressBar;

	private MediaRecorder mMediaRecorder;
	private MediaPlayer mPlayer;
	private DisplayProgressBarChangingTask mTask;

	public String mSavedPath;

	private int mState = RECORD_STATE_POSSIBLE_RECORD;
	
	private RecordingListener mRecordingLinstener;

	public RecordView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context);
		FileControlUtil.createTempRecordFolder(context);
	}
	
	public RecordView(Context context) {
		super(context);
		initView(context);
		
		FileControlUtil.createTempRecordFolder(context);
	}
	
	/**
	 * <p>Set state of recording</p>
	 * @param state	 state of recording(RECORD_STATE_POSSIBLE_RECORD, RECORD_STATE_RECORDING
	 * , RECORD_STATE_EXIST_FILE)
	 */
	public void setState(int state) {
		mState = state;
	}
	
	/**
	 * <p>Get state of recording</p>
	 * @return		state of recording(RECORD_STATE_POSSIBLE_RECORD, RECORD_STATE_RECORDING
	 * , RECORD_STATE_EXIST_FILE)
	 */
	public int getState() {
		return mState;
	}
	
	/**
	 * <p>Whether RecordView is show</p>
	 * @return		Whether RecordView is show<
	 */
	public boolean isShowRecordView() {
		return (getVisibility() == View.VISIBLE) ? true : false;
	}
	
	/**
	 * <p>Set Visibility on View.VISIBLE</p>
	 */
	public void showRecordView() {
		setVisibility(View.VISIBLE);
	}
	
	/**
	 * <p>Set Visibility on View.GONE</p>
	 */
	public void hideRecordView() {
		setVisibility(View.GONE);
	}
	
	/**
	 * <p>Initialization RecoredView</p>
	 * @param context	{@link Context}
	 */
	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.editor_recode_view, this);
		
		Button startRecordBtn = (Button)findViewById(R.id.start_record);
		startRecordBtn.setOnClickListener(this);
		
		mRecordedTime = (TextView)findViewById(R.id.recorded_time);
		mMaxTime = (TextView)findViewById(R.id.max_time);
		
		mProgressBar = (ProgressBar)findViewById(R.id.record_seekbar);
		mProgressBar.setProgress(0);
		mProgressBar.setMax(PROGRESS_MAX);
		
		mRecordingStartBtn = (Button)findViewById(R.id.start_record);
        mMidiStopBtn = (Button)findViewById(R.id.stop_record);
        mPlayBtn = (Button)findViewById(R.id.play_record);
        mRemoveBtn = (Button)findViewById(R.id.remove_record);
        mRecordingSaveBtn = (Button)findViewById(R.id.save_record);
        
        mRecordingStartBtn.setOnClickListener(this);
        mMidiStopBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mRemoveBtn.setOnClickListener(this);
        mRecordingSaveBtn.setOnClickListener(this);        
	}
	
	/**
	 * <p>Initialization progress bar</p>
	 */
	public void initProgress() {
		mRecordedTime.setText(getResources().getString(R.string.voice_min_length));
		mProgressBar.setProgress(0);		
	}
	
	/**
	 * <p>Initialization record time text</p>
	 */
	public void initRecordTime() {
		mMaxTime.setText(getResources().getString(R.string.voice_max_length));
		initProgress();
		mProgressBar.setMax(PROGRESS_MAX);
	}

	/**
	 * <p>Initialization media player time text</p>
	 */
	public void initMediaPlayTime() {
		initProgress();
		
		mMaxTime.setText(getCustomFormatTime(mProgressBar.getMax()));
	}

	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_record:
			setState(RECORD_STATE_RECORDING);
			startRecord();
			mRecordingStartBtn.setVisibility(View.GONE);
			mRecordingSaveBtn.setVisibility(View.VISIBLE);
			break;
			
		case R.id.save_record:
			saveRecord();
			break;
			
		case R.id.play_record:
			playRecord();
			break;
		
		case R.id.stop_record:
			stopPlayer();
			break;
			
		case R.id.remove_record:
			setState(RECORD_STATE_POSSIBLE_RECORD);
			removeRecord();
			break;		

		}
		
	}
	
	/**
	 * <p>Remove Recorded file and change Recordview state</p>
	 */
	private void removeRecord() {
		removeRecordData();
		initRecordTime();
		mPlayBtn.setVisibility(View.GONE);
		mRemoveBtn.setVisibility(View.GONE);
		mRecordingStartBtn.setVisibility(View.VISIBLE);
	}
	 
	/**
	 * <p>Show start button</p>
	 */
	public void showStartButton() {
		mRecordingStartBtn.setVisibility(View.VISIBLE);
	} 
	
	/**
	 * <p>Play record operation</p>
	 */
	public void playRecord() {
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
		} else {
			mPlayer.reset();
		}
		
		try {
			if (mSavedPath != null) {
				FileInputStream fis = new FileInputStream(new File(mSavedPath));
				FileDescriptor fd = fis.getFD();
				mPlayer.setDataSource(fd);
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			} else {
			String path = FileControlUtil.getSavedVoiceFullPath();
			mPlayer.setDataSource(path);
			}
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mTask = new DisplayProgressBarChangingTask(DisplayProgressBarChangingTask.STATE_PLAYING);
		mTask.execute();
		
		mPlayBtn.setVisibility(View.GONE);
		mRemoveBtn.setVisibility(View.GONE);
		mMidiStopBtn.setVisibility(View.VISIBLE);
		
	}
	
	/**
	 * <p>Stop record file play</p>
	 */
	public void stopPlayer() {
		mPlayer.stop();
		mTask.cancel(true);
		initProgress();
		
		mMidiStopBtn.setVisibility(View.GONE);
		mPlayBtn.setVisibility(View.VISIBLE);
		mRemoveBtn.setVisibility(View.VISIBLE);
	}
	
	/**
	 * <p>Return whether voice file play</p>
	 * @return		Whether voice file play
	 */
	public boolean isPlay() {
		if (mPlayer != null) {
			return mPlayer.isPlaying();
		}
		
		return false;
		
	}
	
	/**
	 * <p>start record operation</p>
	 */
	public void startRecord() {
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.reset();
		} else {
			mMediaRecorder.reset();
		}
		
		try {
			String path = FileControlUtil.getSavedVoiceFullPath();
			
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mMediaRecorder.setOutputFile(path);
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mMediaRecorder.start();	
		
		if (mRecordingLinstener != null)	mRecordingLinstener.start();
		
		mTask = new DisplayProgressBarChangingTask(DisplayProgressBarChangingTask.STATE_RECORDING);
		mTask.execute();
	
	}
	
	/**
	 * <p>Stop Player of Recording</p>
	 * @return	saved voice file path when stop recording
	 */
	public String stopPlayerOrRecorder() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			stopPlayer();
			
			return null;
		} else if (mMediaRecorder != null && mState == RECORD_STATE_RECORDING) {
			saveRecord();
			
			return FileControlUtil.getSavedVoiceFullPath();
		}
		return null;
	}
	
	/**
	 * <p>Save recording data</p>
	 */
	private void saveRecordingData() {
		try {
			mMediaRecorder.stop();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} finally {
			mTask.cancel(true);
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}
	
	/**
	 * <p>Save record file and change RecordView state</p>
	 */
	public void saveRecord() {
		setState(RECORD_STATE_EXIST_FILE);
		saveRecordingData();
		mRecordingSaveBtn.setVisibility(View.GONE);
		mPlayBtn.setVisibility(View.VISIBLE);
		mRemoveBtn.setVisibility(View.VISIBLE);
	}
	
	/**
	 * <p>Remove recorded file</p>
	 */
	public void removeRecordData() {
		if (mRecordingLinstener != null)	mRecordingLinstener.deleteRecordingData();
		
		String path = FileControlUtil.getSavedVoiceFullPath();
		File mediaFile = new File(path);
		if (mediaFile.exists()) {
			mediaFile.delete();
		}
		
		mSavedPath = null;
	}
	
	/**
	 * <p>Get custome time format String</p>
	 * @param curTime	current time
	 * @return			time String
	 */
	private String getCustomFormatTime(int curTime){
		int time = curTime/10;
		
		StringBuilder result = new StringBuilder();
		
		result.append(time/60);
		if (time%60 < 10) {
			result.append(":").append("0").append(time%60);
		} else {
			result.append(":").append(time%60);
		}
		
		return result.toString();
	}	
	
	/**
	 * <p>Progress bar state changing Thread</p>
	 */
	private class DisplayProgressBarChangingTask extends AsyncTask<Void, String, Void> {
		
		private static final int STATE_RECORDING = 0;
		private static final int STATE_PLAYING = 1;
		
		private int mState = -1; 
		
		public DisplayProgressBarChangingTask(int state) {
			mState = state;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			int curTime = 0;
			final int max = mProgressBar.getMax();
			
			while (isCancelled() == false && curTime != max) {
				mProgressBar.setProgress(++curTime);
				
				if (curTime % 10 == 0) {
					publishProgress(getCustomFormatTime(curTime));
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (max == PROGRESS_MAX) {
				mProgressBar.setMax(curTime);
			}
			
			return null;
		}

		@Override
		protected void onCancelled() {
			initMediaPlayTime();
			
			if (mState == STATE_RECORDING) {
				String path = FileControlUtil.getSavedVoiceFullPath();
				if (mRecordingLinstener != null)	mRecordingLinstener.complete(path);
			}
			
			super.onCancelled();
		}
		
		@Override
		protected void onProgressUpdate(String... time) {
			mRecordedTime.setText(time[0]);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (mProgressBar.getProgress() == mProgressBar.getMax()) {				
				if (mState == STATE_RECORDING) {
					String path = FileControlUtil.getSavedVoiceFullPath();
					if (mRecordingLinstener != null)	mRecordingLinstener.complete(path);
					
					initMediaPlayTime();
					mRecordingSaveBtn.setVisibility(View.GONE);
				} else if (mState == STATE_PLAYING) {
					mMidiStopBtn.setVisibility(View.GONE);
				}
				
				mPlayBtn.setVisibility(View.VISIBLE);
				mRemoveBtn.setVisibility(View.VISIBLE);				
			}
			initProgress();
			super.onPostExecute(result);
		}
	}
	
	/**
	 * <p>Update state of record view when exist voice file</p>
	 * @param path		voice file path
	 * @return			whether change state
	 */
	public boolean updateStateWhenEixistFile(String path) {				
		if (FileControlUtil.isExistFile(path)) {
			mRecordingStartBtn.setVisibility(View.GONE);
			mPlayBtn.setVisibility(View.VISIBLE);
			mRemoveBtn.setVisibility(View.VISIBLE);
			mSavedPath = path;
		} else {
			return false;
		}
		
		mPlayer = new MediaPlayer();
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			FileDescriptor fd = fis.getFD();
			mPlayer.setDataSource(fd);
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.prepare();
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setState(RECORD_STATE_EXIST_FILE);
		
		int duration = mPlayer.getDuration()+1000;
		
		String maxTime  = getCustomFormatTime(duration/100);
		mMaxTime.setText(maxTime);
		
		mProgressBar.setMax(duration/100);
		
		return true;
	}
	
	public void setRecordingListener(RecordingListener recording) {
		mRecordingLinstener = recording;
	}
	
	/**
	 * Interface for RecordingListener
	 *
	 */
	public interface RecordingListener {
		public abstract void start();
		public abstract void complete(String path);
		public abstract void deleteRecordingData();
	}

}
