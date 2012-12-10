package com.samsung.photodesk;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsung.photodesk.MapContentFragment.MediaGroupItem;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.MediaLoader;

/**
 * <p>Overlay view class</p>
 * Display overlay item({@link MediaOverlayView}) if selected marker on MapView.
 * Used by MapSelectedItemActivity ({@link MapSelectedItemActivity})
 *
 */
public class MediaOverlayView extends FrameLayout {

	private LinearLayout mContainer;
	private TextView mTvTitle;
	private ImageView mIvMedia;
	
	public MediaOverlayView(Context context) {

		super(context);
		
		setPadding(10, 0, 10, 0);
		mContainer = new LinearLayout(context);
		mContainer.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.content_map_overlay, mContainer);
		mTvTitle = (TextView) v.findViewById(R.id.media_item_title);
		mIvMedia = (ImageView) v.findViewById(R.id.iVMedia);


		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(mContainer, params);

	}
	
	public void setData(MediaGroupItem groupItem) {
		
		mContainer.setVisibility(VISIBLE);
		
		if (groupItem.mDiaplayName != null) {
			mTvTitle.setVisibility(VISIBLE);
			mTvTitle.setText(String.format(getContext().getString(R.string.map_image_count), groupItem.getCount()));
		} else {
			mTvTitle.setVisibility(GONE);
		}
		
		MediaItem item = groupItem.getItem(0);
		if (item == null) return;
		Bitmap bm = ThumbnailCache.INSTANCE.getBitmap(item.getId());
		if (bm == null) {
			bm = MediaLoader.getThumbnail(getContext().getContentResolver(), 
					item.getType(), item.getId(), item.getPath());
		} 
		
		mIvMedia.setImageBitmap(bm);
	}

}
