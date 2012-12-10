package com.samsung.photodesk;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.samsung.photodesk.data.ContentItem;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.MediaLoader;

/**
 *  <p>ContentFragment for MapView</p>
 *  Use generated MapView by Activity({@link BasePhotoDeskActivity}) because Fragment can't be generated map.
 *  Items of the short distance are managed by group.(({@link MediaGroupItem}))
 *  
 */
public class MapContentFragment extends ContentFragment {
	private static final int REQ_MAP_SELECTED_ITEM = 0;
	
	private static SetMapContentAsyncTask sGetMapContentAsyncTask;
	
	private MapView mMapView;
	private MapController mMapController;
	private MediaOverlay mMediaOverlay;
	private List<Overlay> mMapOverlays;
	private ArrayList<MediaGroupItem> mMediaGroupItems = new ArrayList<MediaGroupItem>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mFolder = (FolderItem) getArguments().getParcelable("folder");
		if (mFolder == null) return mContainer;
		
		mContainer = (ViewGroup)((PhotoDeskActivity)getActivity()).getMapViewContainer();
		initMapView();
		setOverlayItems();
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onStop() {
		if (sGetMapContentAsyncTask != null) {
			sGetMapContentAsyncTask.cancel(true);
		}
		super.onStop();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.map) {
			mMediaOverlay.hideOverlayView();
		}
		return super.onTouch(v, event);
	}
	
	@Override
	public int getItemCount() {
		return mMediaGroupItems.size();
	}
	
	@Override
	public void onDestroy() {
		if(mMediaOverlay != null) {
			mMediaOverlay.hideOverlayView();
		}
		super.onDestroy();
	}
	
	@Override
	public void refresh() {
		setOverlayItems();
	}
	
	/**
	 * Not supported fling on MapView
	 */
	@Override
	public void flingLeft() {}
	
	/**
	 * Not supported fling on MapView
	 */
	@Override
	public void flingRight() {}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_MAP_SELECTED_ITEM) {
			if (resultCode == Activity.RESULT_OK) {
				if (data.getBooleanExtra(ImageViewActivity.IS_EDIT, false)) {
					
					ContentItem.getInstance().clear();
					FolderFragment folder = getFolderFragment();
					if (folder != null) {
						folder.update(mFolder);
					}
					
					if (getItemCount() != 0) {
						refresh();
					}
				}
			}
			return;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	final MediaOverlay.OverlayClickListener mOverlayClickListener = new MediaOverlay.OverlayClickListener() {
		@Override
		public void onClick(ArrayList<MediaItem> items) {
			Activity activity = getActivity();
			if (activity == null) return;
					
			ContentItem.getInstance().add(items);
			Intent intent = new Intent(getActivity(), MapSelectedItemActivity.class);
			intent.putExtra(ITEM_UPDATE, false);
			intent.putExtra("index", getShownFolderIndex());
		    intent.putExtra("folder", mFolder);
			startActivityForResult(intent, REQ_MAP_SELECTED_ITEM);
		}
	};
	
	/**
	 * Display overlay items on MapView
	 */
	private void overlayMediaItems() {
		if (mMediaOverlay == null) return;
		if (mMediaOverlay.size() != 0) {
			mMediaOverlay.clear();
		}

		for (MediaGroupItem groupItem : mMediaGroupItems) {
			GeoPoint point = new GeoPoint(
					(int)(groupItem.mLatitude* 1E6), 
					(int)(groupItem.mLongitude* 1E6));
			
			OverlayItem overlayItem = new OverlayItem(point, groupItem.mDiaplayName, "");
			mMediaOverlay.addOverlay(groupItem, overlayItem);
		}
		
		if (mMediaOverlay.size() != 0) {
			mMapOverlays.add(mMediaOverlay);
		}

		if (mMapOverlays.size() == 0) {
			Toast.makeText(getActivity(), R.string.map_not_image, Toast.LENGTH_SHORT).show();
		} else {
			if (mMediaGroupItems.size() > 0) {
				MediaGroupItem groupItem = mMediaGroupItems.get(0);
				GeoPoint point = new GeoPoint(
						(int)(groupItem.mLatitude* 1E6), 
						(int)(groupItem.mLongitude* 1E6));
				mMapController.animateTo(point);
			}
		}
	}

	/**
	 * Set overlay items
	 */
	private void setOverlayItems() {
		if (sGetMapContentAsyncTask != null) {
			sGetMapContentAsyncTask.cancel(true);
			sGetMapContentAsyncTask = null;
		}		
		
		if (mMediaGroupItems != null) {
			mMediaGroupItems.clear();
		}
		
		ArrayList<MediaItem> items = MediaLoader.getMediaItems(mFolder.getId(), getActivity().getContentResolver());
		
		if (mMapOverlays == null) {
			mMapOverlays = mMapView.getOverlays();
			
			Drawable drawable = getResources().getDrawable(R.drawable.marker);
			mMediaOverlay = new MediaOverlay(getActivity(), mOverlayClickListener, mMapView, drawable);
		}
		
		if (mMapOverlays != null) {
			mMapOverlays.clear();
		}
		
		setMapContent(items);
	}
	
	/**
	 * Check invalid location
	 * @param latLong
	 * @return true - invalid location, false - valid location
	 */
	public boolean isInvalidLocation(float[] latLong) {
		return (latLong[0] == 0f && latLong[1] == 0f); 
	}

	/**
	 * Initialize Map View
	 */
	private void initMapView() {
		mMapView = (MapView) mContainer.findViewById( R.id.map );
		mMapView.setSatellite(false);
		mMapView.displayZoomControls(false);
		mMapView.setOnTouchListener(this);
		
		mMapController = mMapView.getController();
		mMapController.setZoom(11);
		
		final LocationManager locationManger = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		String provider = locationManger.getBestProvider(criteria, true);
		
		if (provider != null) {
			Location location = locationManger.getLastKnownLocation(provider);
			updateWithNewLocation(location);
		}
	}
	
	/**
	 * Create Group Item
	 */
	class MediaGroupItem {
		double mLatitude = 0f;
		double mLongitude = 0f;
		
		String mDiaplayName;
		
		ArrayList<MediaItem> mMediaItems = new ArrayList<MediaItem>();
		
		public MediaGroupItem(MediaItem item) {
			mLatitude = item.getLatitude();
			mLongitude = item.getLongitude();
			mDiaplayName = item.getDisplayName();
			add(item);
		}

		public boolean isGroup(MediaItem item) {
			boolean result = false;
			try {
				result = Double.parseDouble(String.format("%.3f", item.getLatitude())) == Double.parseDouble(String.format("%.3f", mLatitude)) &&
						Double.parseDouble(String.format("%.3f", item.getLongitude())) == Double.parseDouble(String.format("%.3f", mLongitude));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			}
			return result;		
		}

		public void add(MediaItem item) {
			mMediaItems.add(item);
		}

		public MediaItem getItem(int index) {
			if (index >= mMediaItems.size()) return null;  
			return mMediaItems.get(index);
		}

		public int getCount() {
			return mMediaItems.size();
		}
	}

	/**
	 * Update location
	 * @param location
	 */
	private void updateWithNewLocation(Location location) {
		if (location != null) {
			Double geoLat = location.getLatitude()*1E6;
			Double geoLng = location.getLongitude()*1E6;
			
			if (mMapController != null) {
				mMapController.animateTo(new GeoPoint(geoLat.intValue(), geoLng.intValue()));
			}
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		ViewGroup parentViewGroup = (ViewGroup) mContainer.getParent();
		if (null != parentViewGroup) {
			parentViewGroup.removeView(mContainer);
		}
	}
	
	@Override
	public int getViewType() {
		return VIEW_MAP;
	}

	@Override
	public void selectAllItem() {
	}

	@Override
	public void setAdapter() {
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		mMapView.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	
	@Override
	public void onClick(View v) {
	    super.onClick(v);
	    switch(v.getId()){
	        case R.id.btnOpen:
	            super.flingRight();
	            break;
	    }
	}

	@Override
	public int getFirstVisiblePosition() {
		return -1;
	}
    
	@Override
	public View getChildAt(int index) {
		return null;
	}
	
	/**
	 * Execute AsyncTask for set map content.
	 * @param items
	 */
	public void setMapContent(ArrayList<MediaItem> items) {		
		sGetMapContentAsyncTask = new SetMapContentAsyncTask(items);
		sGetMapContentAsyncTask.execute();
	}
	
	private class SetMapContentAsyncTask extends AsyncTask<Void, Integer, Void> {
		ArrayList<MediaItem> mItems;
		
		public SetMapContentAsyncTask(ArrayList<MediaItem> items) {
			mItems = items;
		}
		
		@Override
		protected void onCancelled() {
			if (mMediaGroupItems != null) {
				mMediaGroupItems.clear();
			}
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mMediaGroupItems == null) return null;
			for (MediaItem item : mItems) {
				if (isCancelled()) {
					break;
				}
				if (item == null || item.isInvaildLocation()) {
					continue;
				}
					
				boolean grouping = false;
				for (MediaGroupItem group: mMediaGroupItems) {
					if (group == null) continue;
					if (isCancelled()) {
						break;
					}
					if (group.isGroup(item)) {
						group.add(item);
						grouping = true;
						break;
					}
				}
				
				if (!grouping) {
					mMediaGroupItems.add(new MediaGroupItem(item));
				}
			}			
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		protected void onPostExecute(Void result) {		
			if (sGetMapContentAsyncTask != null) {
				sGetMapContentAsyncTask.cancel(true);
				sGetMapContentAsyncTask = null;
			}
			overlayMediaItems();
		}
	}	
}
