package com.samsung.photodesk;

import android.os.Bundle;
import android.view.MenuItem;

import com.samsung.photodesk.util.Setting;
import com.samsung.sdraw.SDrawLibrary;

/**
 * <p>Setting Menu - Help activity</p>
 * 
 */
public class HelpInfoActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!SDrawLibrary.isSupportedModel()) {
			setContentView(R.layout.helpinfo_subtract_pen);
		} else if (Setting.PEN_MODE) {
            setContentView(R.layout.helpinfo);
		} else {
			setContentView(R.layout.helpinfo_exclude_pen);			
		}
        getActionBar().setDisplayHomeAsUpEnabled(true);
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
}
