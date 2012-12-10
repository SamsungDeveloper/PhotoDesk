
package com.samsung.photodesk;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * <p>Display item that selected on MapView({@link MediaOverlayView}) to grid view</p>
 * 
 */
public class MapSelectedItemActivity extends BaseActivity {

	public static final String IS_EDIT = "is_edit";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_view);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        ContentFragment contentView = ContentFragment.createView(ContentFragment.VIEW_GRID);
        contentView.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(R.id.contentView, contentView).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
    
    @Override
    public void finish() {
    	Intent intent = new Intent();
		intent.putExtra(IS_EDIT, true);
		setResult(RESULT_OK, intent);
    	super.finish();
    }
}
