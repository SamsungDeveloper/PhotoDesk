package com.samsung.photodesk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.view.SpenDialog;

/**
 * <p>Managing for Setting menu</p>
 * Set Theme, Delete cache, Help, Include videos, Program information.
 * Set data is managed by Shared preference. {@link Setting}
 */
public class SettingActivity extends BaseActivity implements OnClickListener, CompoundButton.OnCheckedChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.setting);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		findViewById(R.id.llTypeChange).setOnClickListener(this);
		findViewById(R.id.llCacheDelete).setOnClickListener(this);
		findViewById(R.id.llHelp).setOnClickListener(this);
		findViewById(R.id.llAbout).setOnClickListener(this);
		
        Switch s = (Switch)findViewById(R.id.tvIncludeVideo);
        if (s != null) {
	        s.setOnCheckedChangeListener(this);
	        s.setChecked(Setting.INSTANCE.getIncludeVideo());
        }
	}
	
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
       Setting.INSTANCE.setIncludeVideo(this, isChecked);       
    }
    
	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
				
		default:
			return false;
		}
		return true;
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llTypeChange:
			changeStyle(this);
			break;
			
		case R.id.llCacheDelete:
			deleteCache();
			break;
			
		case R.id.llHelp:
			showHelp();
			break;
			
		case R.id.llAbout:
			showAbout();

		default:
			break;
		}
	}

	private void showAbout() {

		View v =  LayoutInflater.from(this).inflate(R.layout.about, null);
		
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setTitle("About");
        dialog.setLeftBtn(R.string.ok, null);
        dialog.setContentView(v);
        dialog.setSummaryGravity(Gravity.CENTER);
        dialog.show();
	}

	private void showHelp() {
		Intent intent = getIntent();
	    intent.setClass(this, HelpInfoActivity.class);
	    startActivity(intent);
	}

	private void deleteCache() {
		final SpenDialog dialog = new SpenDialog(this);
		dialog.setTitle(getString(R.string.thumbnail_remove));
		dialog.setLeftBtn(R.string.cancel, null);
        dialog.setRightBtn(R.string.ok, new OnClickListener() {
            
            @Override
            public void onClick(View v) {
				ThumbnailCache.INSTANCE.clearAll();
				dialog.dismiss();
				Toast.makeText(getApplicationContext(), getString(R.string.remove_chash_image),Toast.LENGTH_LONG).show();		
            }
        });
        dialog.setContentView(getString(R.string.thumbnail_cache_remove_question), getResources().getDimension(R.dimen.base_text_size));
        dialog.show();
	}

	public static void changeStyle(final Context context) {
		View v =  LayoutInflater.from(context).inflate(R.layout.setting_style, null);

		final SpenDialog dialog = new SpenDialog(context);
		dialog.setTitle(context.getString(R.string.type_change_dialogtitle));
	
        dialog.setContentView(v, new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        dialog.show();
        
        v.findViewById(R.id.btnBasic).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedStyle(context, Setting.STYLE_BASIC);
				dialog.dismiss();
			}
		});
		
		v.findViewById(R.id.btnStitch).setOnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) {
				selectedStyle(context, Setting.STYLE_STITCH);
				dialog.dismiss();
			}
		});
		
		v.findViewById(R.id.btnMintCheck).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedStyle(context, Setting.STYLE_MINT_CHECK);
				dialog.dismiss();
			}
		});
	}
	
	public static void selectedStyle(final Context context, int style) {
		int oldStyle = Setting.INSTANCE.getStyle();
		if (oldStyle == style) return;
		Setting.INSTANCE.setStyle(context, style);
		restartApplication(context);
	}
	
	private static void restartApplication(final Context context) {
        Intent intent = new Intent(context, PhotoDeskActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
   
}
