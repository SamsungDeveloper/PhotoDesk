package com.samsung.photodesk;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.samsung.photodesk.MapContentFragment.MediaGroupItem;
import com.samsung.photodesk.data.MediaItem;

/**
 * <p>Media overlay item class</p>
 * Display overlay item ({@link MediaOverlayView}) if selected marker on MapView.
 */
public class MediaOverlay extends ItemizedOverlay<OverlayItem> {
	private MapView mMapView;
	private Context mContext;
	private MediaOverlayView mMediaOvierView;
	private final MapController mMapController;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private ArrayList<MediaGroupItem> mGroupItems = new ArrayList<MediaGroupItem>();
	private OverlayClickListener mClickListener;

	public MediaOverlay(Context context, OverlayClickListener clickListener, MapView mapView, Drawable drawable) {
		super(boundCenterBottom(drawable));
		mMapView = mapView;
		mMapController = mapView.getController();
		mClickListener = clickListener;
		mContext = context;
		populate();
	}
	
	@Override
	protected OverlayItem createItem(int index) {
		return mOverlays.get(index);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	public void clear() {
		mOverlays.clear();
		mGroupItems.clear();
	}
	
	public void hideOverlayView() {
		if (mMediaOvierView != null) {
			mMediaOvierView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected boolean onTap(int index) {
		boolean isRecycled;
		GeoPoint point;
		final int position = index;
		
		point = createItem(index).getPoint();
		
		if (mMediaOvierView == null) {
			mMediaOvierView = new MediaOverlayView(mContext);
			isRecycled = false;
		} else {
			isRecycled = true;
		}
		mMediaOvierView.findViewById(R.id.meida_inner_layout).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mClickListener != null) {
					mClickListener.onClick(mGroupItems.get(position).mMediaItems);
				}
			}
		});
		
	
		mMediaOvierView.setVisibility(View.GONE);
		mMediaOvierView.setData(mGroupItems.get(index));
		
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
				MapView.LayoutParams.BOTTOM_CENTER);
		params.mode = MapView.LayoutParams.MODE_MAP;
		
		mMediaOvierView.setVisibility(View.VISIBLE);

		if (isRecycled) {
			mMediaOvierView.setLayoutParams(params);
		} else {
			mMapView.addView(mMediaOvierView, params);
		}
		
		mMapController.animateTo(point);
		mMapView.invalidate();
		
		return true;
	}

	public void addOverlay(MediaGroupItem groupItem, OverlayItem overlayItem) {
		mOverlays.add(overlayItem);
		mGroupItems.add(groupItem);
		populate();
		
	}
	
	/**
	 * Interface for Overlay Click listener
	 *
	 */
	interface OverlayClickListener {
		void onClick(ArrayList<MediaItem> items);
	}
	
}
