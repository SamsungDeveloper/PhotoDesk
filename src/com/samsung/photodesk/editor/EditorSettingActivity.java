package com.samsung.photodesk.editor;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.samsung.photodesk.BaseActivity;
import com.samsung.photodesk.R;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.view.SpenDialog;

/**
 * <p>Image Editor and Animation Image Player Setting Activity</p>
 * Image Editor Setting type : EDITOR_SETTING_TYPE_EDITOR, EDITOR_SETTING_TYPE_ANIMATION
 * EDITOR_SETTING_TYPE_EDITOR : SAMM Setting, Animation Setting.
 * EDITOR_SETTING_TYPE_ANIMATION : Animation Setting.
 */
public class EditorSettingActivity extends BaseActivity implements OnClickListener{
	
	public static final String KEY_IMAGE_SAVE_SIZE = "samm_img_size_prefkey";
	public static final String KEY_IMAGE_SAVE_QUALITY = "samm_save_image_quality";
	public static final String KEY_ANIMATION_SPEED = "samm_animation_speed";
	public static final String KEY_BG_PLAY = "background_audio_play";
	public static final String KEY_BG_AUTO_REPLAY = "background_audio_auto_replay";
	public static final String KEY_BG_ANIMATION_EFFECT_SOUND = "background_audio_play_stop";
	
	public static final String EDITOR_SETTING_TYPE = "type";
	
	public static final int EDITOR_SETTING_TYPE_EDITOR = 10;
	public static final int EDITOR_SETTING_TYPE_ANIMATION = 11;
	
	private TypedArray mStyleArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor_setting_view);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		int settingType = getIntent().getIntExtra(EDITOR_SETTING_TYPE, EDITOR_SETTING_TYPE_EDITOR);
		mStyleArray = obtainStyledAttributes(R.styleable.AppTheme);
		
		if (settingType == EDITOR_SETTING_TYPE_ANIMATION) {
			findViewById(R.id.SAMMSettingCategory).setVisibility(View.GONE);
			findViewById(R.id.EdSettingSaveSize).setVisibility(View.GONE);
			findViewById(R.id.EdSettingSaveQuality).setVisibility(View.GONE);
		}  else {
			findViewById(R.id.EdSettingSaveSize).setOnClickListener(this);
			findViewById(R.id.EdSettingSaveQuality).setOnClickListener(this);
		}
		
		findViewById(R.id.EdSettingAniSpeed).setOnClickListener(this);
		findViewById(R.id.EdSettingBgPlay).setOnClickListener(this);
		findViewById(R.id.EdSettingBgReplay).setOnClickListener(this);
		findViewById(R.id.EdSettingEffectSound).setOnClickListener(this);
		
		init(settingType);
	
	}

	/**
	 * Initialization views by setting value
	 * @param type	Setting type (EDITOR_SETTING_TYPE_EDITOR, EDITOR_SETTING_TYPE_ANIMATION)
	 */
	public void init(int type) {
		autoCheckBgPlay();
		autoCheckBgRePlay();
		autoCheckEffectSound();
		
		if (type == EDITOR_SETTING_TYPE_EDITOR) {
			setSaveSizeDescription();
			setSaveQualityDescription();
		}
	
		setAniSpeedDescription();
		setBgPlayDescription();
		setBgRePlayDescription();
		setEffectSoundDescription();
	}
	
	

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		case R.id.EdSettingSaveSize:
			showSaveSizeDialog();
			break;
		case R.id.EdSettingSaveQuality:
			showSaveQualityDialog();
			break;
		case R.id.EdSettingAniSpeed:
			showAnimationSpeedDialog();
			break;
		case R.id.EdSettingBgPlay:
			toggleBgPlayOption();
			break;
		case R.id.EdSettingBgReplay:
			toggleBgReplayOption();
			break;
		case R.id.EdSettingEffectSound:
			toggleEffectSound();
			break;
		}
		
	}
	
	/**
	 * <p>Show SAMM file save size dialog</p> 
	 */
	public void showSaveSizeDialog() {
		LayoutInflater inflate = LayoutInflater.from(this);
		View v =  inflate.inflate(R.layout.setting_check_list, null);
		final RadioGroup rgStyle = (RadioGroup) v.findViewById(R.id.rgStyle);
		
		String speed[] = getResources().getStringArray(R.array.save_image_size);
		final String speedValue[] = getResources().getStringArray(R.array.save_image_size_value);
		
		for (int i = 0; i < speed.length; i++) {
			RadioButton btn = (RadioButton) inflate.inflate(R.layout.setting_check_list_item, null);
			btn.setText(speed[i]);
			btn.setId(i);
			rgStyle.addView(btn, i);
		}

		int index = Setting.INSTANCE.getSaveSize();
		rgStyle.check(Integer.parseInt(speedValue[index]));
		
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setTitle(getString(R.string.samm_img_size_dialogtitle));
		dialog.setLeftBtn(R.string.cancel, null);
        dialog.setRightBtn(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				int themeId = rgStyle.getCheckedRadioButtonId();
				Setting.INSTANCE.setSaveSize(EditorSettingActivity.this, Integer.parseInt(speedValue[themeId]));
				
				setSaveSizeDescription();
				
				dialog.dismiss();
			}
        	
        });
        
        dialog.setContentView(v, new RadioGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        dialog.show();
	}
	
	/**
	 * <p>Show SAMM file save quality dialog</p> 
	 */
	public void showSaveQualityDialog() {
		LayoutInflater inflate = LayoutInflater.from(this);
		View v =  inflate.inflate(R.layout.setting_check_list, null);
		final RadioGroup rgStyle = (RadioGroup) v.findViewById(R.id.rgStyle);
		
		String quality[] = getResources().getStringArray(R.array.save_image_quality);
		final String qualityValue[] = getResources().getStringArray(R.array.save_image_quality_value);
		
		for (int i = 0; i < quality.length; i++) {
			RadioButton btn = (RadioButton) inflate.inflate(R.layout.setting_check_list_item, null);
			btn.setText(quality[i]);
			btn.setId(i);
			rgStyle.addView(btn, i);
		}

		int index = Setting.INSTANCE.getSaveQuality();
		rgStyle.check(Integer.parseInt(qualityValue[index]));
		
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setTitle(getString(R.string.samm_img_size_dialogtitle));
		dialog.setLeftBtn(R.string.cancel, null);
        dialog.setRightBtn(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				int themeId = rgStyle.getCheckedRadioButtonId();
				Setting.INSTANCE.setSaveQuality(EditorSettingActivity.this, Integer.parseInt(qualityValue[themeId]));
				
				setSaveQualityDescription();
				
				dialog.dismiss();
			}
        	
        });
        
        dialog.setContentView(v, new RadioGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        dialog.show();
	}
	
	/**
	 * <p>Show SAMM file play speed dialog</p> 
	 */
	public void showAnimationSpeedDialog() {
		LayoutInflater inflate = LayoutInflater.from(this);
		View v =  inflate.inflate(R.layout.setting_check_list, null);
		final RadioGroup rgStyle = (RadioGroup) v.findViewById(R.id.rgStyle);
		
		String speed[] = getResources().getStringArray(R.array.animation_speed);
		final String speedValue[] = getResources().getStringArray(R.array.animation_speed_value);
		
		for (int i = 0; i < speed.length; i++) {
			RadioButton btn = (RadioButton) inflate.inflate(R.layout.setting_check_list_item, null);
			btn.setText(speed[i]);
			btn.setId(i);
			rgStyle.addView(btn, i);
		}

		int index = Setting.INSTANCE.getAnimationSpeed();
		rgStyle.check(Integer.parseInt(speedValue[index]));
		
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setTitle(getString(R.string.samm_img_size_dialogtitle));
		dialog.setLeftBtn(R.string.cancel, null);
        dialog.setRightBtn(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				int themeId = rgStyle.getCheckedRadioButtonId();
				Setting.INSTANCE.setAnimationSpeed(EditorSettingActivity.this, Integer.parseInt(speedValue[themeId]));
				
				setAniSpeedDescription();
				
				dialog.dismiss();
			}
        	
        });
        
        dialog.setContentView(v, new RadioGroup.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        dialog.show();
	}
	
	
	/**
	 * <p>Toggle background music on/off state when play animation</p>  
	 */
	public void toggleBgPlayOption() {
		boolean state = Setting.INSTANCE.isBgPlay();
		Setting.INSTANCE.setBgPlay(EditorSettingActivity.this, !state);

		autoCheckBgPlay();
		
		setBgPlayDescription();
	}
	
	/**
	 * <p>Toggle background music replay on/off state when play animation</p>
	 */
	public void toggleBgReplayOption() {
		boolean state = Setting.INSTANCE.isBgAutoRePlay();
		Setting.INSTANCE.setBgAutoRePlay(EditorSettingActivity.this, !state);
		
		autoCheckBgRePlay();
		
		setBgRePlayDescription();
	}
	
	/**
	 * <p>Toggle drawing effect sound on/off state when play animation</p>
	 */
	public void toggleEffectSound() {
		boolean state = Setting.INSTANCE.isAnimationEffectSound();
		Setting.INSTANCE.setAnimationEffectSound(EditorSettingActivity.this, !state);
			
		autoCheckEffectSound();
		
		setEffectSoundDescription();
	}
	

	/**
	 * <p>Change checkbox state by background music value</p>
	 */
	public void autoCheckBgPlay() {
		if (Setting.INSTANCE.isBgPlay()) {
			((ImageView) findViewById(R.id.BgPlayCheck))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_contentViewImageCheckBox));
		} else {
			((ImageView) findViewById(R.id.BgPlayCheck))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_itemNoneCheckBox));
		}
	}
	
	/**
	 * <p>Change checkbox state by background replay music value</p>
	 */
	public void autoCheckBgRePlay() {
		if (Setting.INSTANCE.isBgAutoRePlay()) {
			((ImageView) findViewById(R.id.BgReplayCheck))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_contentViewImageCheckBox));
		} else {
			((ImageView) findViewById(R.id.BgReplayCheck))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_itemNoneCheckBox));
		}
	}
	
	/**
	 * <p>Change checkbox state by drawing effect value</p>
	 */
	public void autoCheckEffectSound() {
		if (Setting.INSTANCE.isAnimationEffectSound()) {
			((ImageView) findViewById(R.id.EffectSoundCheck))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_contentViewImageCheckBox));
		} else {
			((ImageView) findViewById(R.id.EffectSoundCheck))
			.setImageDrawable(mStyleArray.getDrawable(R.styleable.AppTheme_itemNoneCheckBox));
		}
	}
	
	/**
	 * <p>Set description of SAMM file save size</p>
	 */
	private void setSaveSizeDescription() {
		String size[] = getResources().getStringArray(R.array.save_image_size);
		((TextView)findViewById(R.id.SaveSizeDescription)).setText(size[Setting.INSTANCE.getSaveSize()]);
	}
	
	/**
	 * <p>Set description of SAMM file save quality</p>
	 */
	private void setSaveQualityDescription() {
		String quality[] = getResources().getStringArray(R.array.save_image_quality);
		((TextView)findViewById(R.id.SaveQualityDescript)).setText(quality[Setting.INSTANCE.getSaveQuality()]);
	}

	/**
	 * <p>Set description of animation play speed</p>
	 */
	private void setAniSpeedDescription() {
		String speed[] = getResources().getStringArray(R.array.animation_speed);
		((TextView)findViewById(R.id.AniSpeedDescript)).setText(speed[Setting.INSTANCE.getAnimationSpeed()]);
	}

	/**
	 * <p>Set description of whether background play</p>
	 */
	private void setBgPlayDescription() {
		String desc = Setting.INSTANCE.isBgPlay() ? getString(R.string.samm_bg_audio_play_summary_on)
				: getString(R.string.samm_bg_audio_play_summary_off);
		((TextView)findViewById(R.id.BgPlayDescript)).setText(desc);
	}
	
	/**
	 * <p>Set description of whether background replay</p>
	 */
	private void setBgRePlayDescription() {
		String desc = Setting.INSTANCE.isBgAutoRePlay() ? getString(R.string.samm_bg_audio_replay_summary_on)
				: getString(R.string.samm_bg_audio_replay_summary_off);
		((TextView)findViewById(R.id.BgReplayDescript)).setText(desc);
	}
	
	/**
	 * <p>Set description of whether drawing effect play</p>
	 */
	private void setEffectSoundDescription() {
		String desc = Setting.INSTANCE.isAnimationEffectSound() ? getString(R.string.samm_bg_audio_sound_effect_summary_on)
				: getString(R.string.samm_bg_audio_sound_effect_summary_off);
		((TextView)findViewById(R.id.EffectSoundDescript)).setText(desc);
	}

	@Override
	public void finish() {
	    if (mStyleArray != null) mStyleArray.recycle();
	    super.finish();
	}
	
}
