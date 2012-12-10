package com.samsung.photodesk;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.samsung.photodesk.MenuTask.OnOperationListener;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.ContentItem;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.util.Setting;
import com.samsung.photodesk.view.SpenDialog;

/**
 * 
 * <p>Photo location on the GPS screen to register</p>
 * Do long press key operation at that location is  photo registered.
 * 
 */

public class MapViewEdit extends MapActivity implements ActivityInterface, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener  {
	private List<Overlay> overlays = null;
	private MapView mMap;
	private MapController mControl;
	private MyLocationOverlay mLocation;
	private GestureDetector mGestureDetector;
	private SelectLocationItemizedOverlay mSelectLocationOverlay = null; 
	private GeoPoint mGPoint;
	private int mImgCnt, mViewFlag;
	private StringBuffer mStrBuf;
	private static MenuTask sMenuTask;
	private ArrayList<MediaObject> mMediaObject;
	private MediaItem mItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTheme(Setting.INSTANCE.getThemeId());
		setContentView(R.layout.map_view_edit);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(R.string.location_edit_title);
		
		mGestureDetector = new GestureDetector(this);         
		mGestureDetector.setOnDoubleTapListener(this);          
		     
		mMap = (MapView) findViewById(R.id.mapedit);  
		mControl = mMap.getController();
		mControl.setZoom(16);
		mMap.setBuiltInZoomControls(true);     
		mMap.setSatellite(false);
		mMap.setHapticFeedbackEnabled(true);                 
		overlays = mMap.getOverlays();         
		mLocation = new MyLocationOverlay(this, mMap);
		
		overlays.add(mLocation); 
		
		Intent intent = getIntent();
		mViewFlag = intent.getIntExtra("view_flag", 0);
		if(mViewFlag == 1){
			mMediaObject = new ArrayList<MediaObject>();
			mItem = intent.getParcelableExtra("view_item");			
			mMediaObject.add(mItem);
		}		
		mLocation.runOnFirstFix(new Runnable() {			
			@Override
			public void run() {
				mMap.getController().animateTo(mLocation.getMyLocation());
			}
		});
		
		Drawable drawable = getResources().getDrawable(R.drawable.marker_green); 
		mSelectLocationOverlay = new SelectLocationItemizedOverlay(drawable, this);	 
	}
	
	/**
     * Return ArrayList to put selected photos.
     * 
     * @return items
     */
	public ArrayList<MediaObject> getSelectedItems() {
		ArrayList<MediaObject> items = new ArrayList<MediaObject>();
		synchronized (ContentItem.getInstance()) {
			for (MediaObject item: ContentItem.getInstance().getItem()) {
				if (item.isSelected()) {
					items.add(item);
				}
			}
		}
		return items;
	}

	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onDoubleTap(MotionEvent me) { 
		return true;
	}
	@Override
	public boolean onDoubleTapEvent(MotionEvent me) {
		return false;
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}


	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case android.R.id.home:
	        	finish();
	        	break;
        }
        return true;
	}
	 
	
	@Override 
	public void onLongPress(MotionEvent me) {
		mSelectLocationOverlay.getOverlays().clear();
		mGPoint= mMap.getProjection().fromPixels((int)me.getX(), (int)me.getY()-((int)getResources().getDimension(R.dimen.map_position_y)));
		if(mViewFlag == 1){
			mImgCnt = 1;			
		}else{
			ArrayList<MediaObject> items = getSelectedItems(); 
			mImgCnt = items.size();
		}
		
		mStrBuf = new StringBuffer();
		mStrBuf.append(String.format(getResources().getString(R.string.location_qustion), mImgCnt));
		
		mSelectLocationOverlay.addOverlay(new OverlayItem(mGPoint,mStrBuf.toString() , null));
		overlays.add(mSelectLocationOverlay); 
	}
	 
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}


	@Override
	public void onShowPress(MotionEvent e) {
	}


	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
 

	@Override
	protected void onPause() { 
		super.onPause();
		mLocation.disableMyLocation();
		mLocation.disableCompass();
	} 


	@Override
	protected void onResume() {
		if (sMenuTask != null) {
			sMenuTask.onResume();
		}	
		super.onResume();
		mLocation.enableMyLocation();
		mLocation.enableCompass();
	}
	
	@Override
	protected void onStop() {
		if (sMenuTask != null) {
			sMenuTask.onStop();
		}
		super.onStop();
	}	

	@Override
	protected boolean isRouteDisplayed(){
		return false;
	}

	@Override
	public int getThemeId() {
		return Setting.INSTANCE.getThemeId();
	}
	
	class MyLocationOverlay2 extends MyLocationOverlay{
		public MyLocationOverlay2(Context context, MapView mapView){
			super(context, mapView);
		}
		protected boolean dispatchTap(){
			return false;
		}
		
	}
	/**
     * GPS photos of the dialog
     * 
     * @return true
     */	 
	public class SelectLocationItemizedOverlay extends ItemizedOverlay<OverlayItem> {
		private Context context = null;
		private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
		final ArrayList<MediaObject> items = (ArrayList<MediaObject>) getSelectedItems();
		final ArrayList<MediaObject> item = (ArrayList<MediaObject>) mMediaObject;		
		final ImageView lctImage[] = new ImageView[4]; 		
        
		public SelectLocationItemizedOverlay(Drawable marker, Context context) {
			super(boundCenterBottom(marker));
			this.context = context;
		}
		@Override
		protected boolean onTap(int index) {
			int posX = -5, posY = 5;;
	        int imgCnt, iCnt;
	        int POS_INCREASE = 10;
	        final SpenDialog dialog = new SpenDialog(context);
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View imgView = inflater.inflate(R.layout.edit_location_image, null);
			OverlayItem itemClicked = overlays.get(index);
			
			if(mViewFlag == 1){
				imgCnt = 1;				
				dialog.setTitle(itemClicked.getTitle());         
				dialog.setCancelable(true);				
				lctImage[0] = (ImageView)imgView.findViewById(R.id.location_image1);				
				lctImage[0].setImageBitmap(ThumbnailCache.INSTANCE.getBitmap(item.get(0).getId()));					
	        }else{	        
				imgCnt = items.size();	
				dialog.setTitle(itemClicked.getTitle());         
				dialog.setCancelable(true);
			
				if(imgCnt==1){
					lctImage[0] = (ImageView)imgView.findViewById(R.id.location_image1);
				}
				else if(imgCnt==2){
					lctImage[0] = (ImageView)imgView.findViewById(R.id.location_image1);
					lctImage[1] = (ImageView)imgView.findViewById(R.id.location_image2);
				}
				else if(imgCnt==3){
					lctImage[0] = (ImageView)imgView.findViewById(R.id.location_image1);
					lctImage[1] = (ImageView)imgView.findViewById(R.id.location_image2);
					lctImage[2] = (ImageView)imgView.findViewById(R.id.location_image3);
				}
				else{
					lctImage[0] = (ImageView)imgView.findViewById(R.id.location_image1);
					lctImage[1] = (ImageView)imgView.findViewById(R.id.location_image2);
					lctImage[2] = (ImageView)imgView.findViewById(R.id.location_image3);
					lctImage[3] = (ImageView)imgView.findViewById(R.id.location_image4);
				}
				
				if(imgCnt>4){
					iCnt = 4;
				}else{
					 iCnt = imgCnt;
				}
				
				for( int i=0 ; i<iCnt ; i++){
					lctImage[i].setImageBitmap(ThumbnailCache.INSTANCE.getBitmap(items.get(i).getId()));
				}
				for (int i = 0; i < iCnt; i++) {			
					if (lctImage[i] != null) {
						lctImage[i].setTranslationX(posX);
						lctImage[i].setTranslationY(posY); 
					}			
					posX += (POS_INCREASE*2);
					posY -= POS_INCREASE;
				}
			}
			
	        dialog.setContentView(imgView); 
	        		
			dialog.setRightBtn(R.string.yes, new OnClickListener() {

				@Override
				public void onClick(View v) {
					mMap.getController().animateTo(mGPoint);
					dialog.dismiss();				
					
					sMenuTask = new MenuTask(context, getMenuOperationLinstener());
					if(mViewFlag==1){
						sMenuTask.setSelectedItems(item);
					}else{
						sMenuTask.setSelectedItems(items);
					}
					sMenuTask.setGeoPoint(mGPoint);
					sMenuTask.onItemClicked(R.id.action_location_edit);						
 				}});    		
			
			dialog.setLeftBtn(R.string.no, null);         
			dialog.show();        
			return true; 
		} 

		protected OnOperationListener getMenuOperationLinstener() {
			return mOperationListener;
		}
		
		OnOperationListener mOperationListener = new OnOperationListener() {
			@Override
			public void onDone(long menuId, FolderItem selectedFolder, ArrayList<MediaObject> selectedItems,
					ArrayList<MediaObject> doneItems, boolean canceled) {
				((MapViewEdit) context).finish(); 
			}		
		};
		
		protected void sleep() {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		@Override
		public boolean onTouchEvent(MotionEvent me, MapView mapView) {
			return super.onTouchEvent(me, mapView); 
		}
	
		public List<OverlayItem> getOverlays() {         
			return overlays;     
		}      

		public void addOverlay(OverlayItem overlay) {         
			overlays.add(overlay);         
			populate();     
		} 

		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i); 
		}

		@Override
		public int size() {
			return overlays.size(); 
		}
	}


	public ThreadPool getThreadPool() {
		return null;
	}

	public PhotoDeskActionBar getPhotoDeskActionBar() {
		return null;
	}	

}