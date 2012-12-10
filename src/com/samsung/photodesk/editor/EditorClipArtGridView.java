package com.samsung.photodesk.editor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.samsung.photodesk.R;
import com.samsung.photodesk.editor.EditorToolUtil.ChangeModeListener;
import com.samsung.spen.lib.input.SPenLibrary;
import com.samsung.spensdk.SCanvasConstants;

/**
 * <p>Clip art a GridView</p>
 * Clip art, you can choose and show.
 */
public class EditorClipArtGridView extends GridView{
	public static final int PEN = EditorToolUtil.TYPE_PEN;
	public static final int HAND = EditorToolUtil.TYPE_HAND;
	public static final int BOTH = EditorToolUtil.TYPE_HAND + 1;
	
	private static int sHand;
	private static int sPen;
	
	private Context mContext;
	private EditorToolUtil mEditorTool;
	
	private ConcurrentHashMap<Integer, Bitmap> mClipArtImage;
	private HashMap<Integer, Bitmap> mSeletorImage;

	public static final int[] itemRes = { 
			R.drawable.clipart_speech_bubble3, R.drawable.clipart_water, R.drawable.clipart_heart2, 
			R.drawable.clipart_pow, R.drawable.clipart_king, R.drawable.clipart_coffee, 
			R.drawable.clipart_ribon, R.drawable.clipart_lip, R.drawable.clipart_skeleton,
			R.drawable.clipart_frame, R.drawable.clipart_frame2, R.drawable.clipart_frame3,
			R.drawable.clipart_frame4, R.drawable.clipart_speech_bubble, R.drawable.clipart_speech_bubble2, 
			R.drawable.clipart_speech_bubble4, R.drawable.clipart_memo, R.drawable.clipart_memo2, 
			R.drawable.clipart_sun, R.drawable.clipart_star2, R.drawable.clipart_star,  
			R.drawable.clipart_weather, R.drawable.clipart_weather2, R.drawable.clipart_weather3, 
			R.drawable.clipart_heart, R.drawable.clipart_heart3, R.drawable.clipart_heart4, 
			R.drawable.clipart_cake, R.drawable.clipart_footprint, R.drawable.clipart_hand, 
			R.drawable.clipart_fingerprint,R.drawable.clipart_clover, R.drawable.clipart_clip
	};


	public EditorClipArtGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
		init(context);

	}

	/**
	 * <p>Initializes the resources used in clip art.</p>
	 * @param context {@link Context}
	 */
	private void init(Context context) {
		mClipArtImage = new ConcurrentHashMap<Integer, Bitmap>();
		mSeletorImage = new HashMap<Integer, Bitmap>();
		mSeletorImage.put(PEN, ((BitmapDrawable)getResources().getDrawable(R.drawable.pop_clipart_up_icon_pen)).getBitmap());
		mSeletorImage.put(HAND, ((BitmapDrawable)getResources().getDrawable(R.drawable.pop_clipart_up_icon_hand)).getBitmap());
		mSeletorImage.put(BOTH, ((BitmapDrawable)getResources().getDrawable(R.drawable.pop_clipart_up_icon_both)).getBitmap());
		
		setAdapter(new ClipArtItemAdapter(context));
		initEditorTool();		
	}
	
	/**
	 * <p>Clip art image is to store a hash map is returned.</p>
	 * @return	Clip art image hash map
	 */
	public ConcurrentHashMap<Integer, Bitmap> getClipArtImage() {
		return mClipArtImage;
	}
	
	/**
	 * <p>EditorTool initialize.</p>
	 */
	public void initEditorTool() {
		mEditorTool = EditorToolUtil.getInstence();
		mEditorTool.setChangeModeListener(new ChangeModeListener() {
			
			@Override
			public void onChangeMode(int type) {
				if (type == EditorToolUtil.TYPE_HAND) {
					sHand = -1;
				} else {
					sPen = -1;
				}
				((BaseAdapter)getAdapter()).notifyDataSetChanged();
			}
		});	
	}
	
	/**
	 * <p>The index of the selected clip art is stored in a global variable.</p>
	 */
	public void setClipArtPosition() {
		sPen = -1;
		sHand = -1;
		if (mEditorTool.getMode(EditorToolUtil.TYPE_HAND) == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
			sHand = mEditorTool.getClipArtPosition(EditorToolUtil.TYPE_HAND);
		} 
		if (mEditorTool.isDrawingToolDivision()) {
			if (mEditorTool.getMode(EditorToolUtil.TYPE_PEN) == SCanvasConstants.SCANVAS_MODE_INPUT_IMAGE) {
				sPen = mEditorTool.getClipArtPosition(EditorToolUtil.TYPE_PEN);
			}
		}
		
	}
	
	/**
	 * <p>Clip art of memory is released.</p>
	 */
	public void recycle() {
		((ClipArtItemAdapter)getAdapter()).recycle();
	}

	/**
	 * <P>ClipArt GridView Adapter</P>
	 */
	class ClipArtItemAdapter extends BaseAdapter implements OnTouchListener{
		private ArrayList<WeakReference<View>> mChildViews;
		private int curEventType = -1;		

		ClipArtItemAdapter(Context context) {
			mChildViews = new ArrayList<WeakReference<View>>();
		}

		@Override
		public int getCount() {
			return itemRes.length;
		}

		@Override
		public Object getItem(int position) {
			return mChildViews.get(position).get();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		/**
		 * <P>Memory of child view in GridView and memory of clipart frees.</P>
		 */
		public void recycle() { 
			for (WeakReference<View> view : mChildViews) {
				MemoryClearUtils.recursiveRecycle(view.get());
			}
			
			int size = mSeletorImage.size();
			for(int i = 0; i < size; i++) {
				mSeletorImage.get(i).recycle();
				mSeletorImage.put(i, null);
			}
			
			size = mClipArtImage.size();
			for(int i = 0; i < size; i++) {
				mClipArtImage.get(i).recycle();
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.editor_clipart_item, null);
				convertView.setLayoutParams(new GridView.LayoutParams(
						(int) getResources().getDimension(R.dimen.clipart_gv_item_img_width), 
						(int) getResources().getDimension(R.dimen.clipart_gv_item_img_height)));
				convertView.setPadding(0, 0, 1, 1);
				
				ViewHolder holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.clipart_img);
				holder.selectorLayout = (FrameLayout) convertView.findViewById(R.id.clipart_selector_container);
				holder.selectorImage = (ImageView) convertView.findViewById(R.id.clipart_selector);
				
				convertView.setOnTouchListener(this);				
				convertView.setTag(holder);
			}
			
			if (position == 0)	setClipArtPosition();

			final ViewHolder holder = (ViewHolder)convertView.getTag();
			
			if (!mClipArtImage.containsKey(position)) {
				loadAsync load = new loadAsync(holder.image);
				load.execute(position);
			} else {
				holder.image.setImageBitmap(mClipArtImage.get(position));
			}
			
			holder.position = position;
			setVisibleSelectIcon(holder, position);
			mChildViews.add(new WeakReference<View>(convertView));

			return convertView;
		}
		
		/**
		 * <P>ImageView loads ClipArt Image in the background.</P>
		 */
		class loadAsync extends AsyncTask<Integer, Bitmap, Bitmap> {

			WeakReference<ImageView> iv;
			public loadAsync(ImageView iv) {
				this.iv = new WeakReference<ImageView>(iv);
			}
			
			@Override
			protected Bitmap doInBackground(Integer... params) {
				if (!mClipArtImage.containsKey(params[0])) {
					BitmapDrawable drawble = (BitmapDrawable)getResources().getDrawable(itemRes[params[0]]);
					
					PhotoDeskSCanvasManager util = PhotoDeskSCanvasManager.getInstence(mContext);		
					BitmapFactory.Options opt = util.getBitmapSize(getResources(), itemRes[params[0]]);
					
					BitmapFactory.decodeResource(getResources(), itemRes[params[0]], opt);
					mClipArtImage.put(params[0], drawble.getBitmap());
				}
				return mClipArtImage.get(params[0]);
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				if (iv.get() != null)	iv.get().setImageBitmap(result);
				super.onPostExecute(result);
			}
			
		}
		
		/**
		 * <P>set Select Icon(Pen/Han Select Icon) visible.</P>
		 * @param holder	childe view in gridView.
		 * @param position	currunt childView position
		 */
		private void setVisibleSelectIcon(ViewHolder holder, int position) {
			if (sPen != position && sHand != position && holder.selectorImage.getVisibility() == View.VISIBLE) {
				holder.selectorLayout.setVisibility(View.GONE);
			} else if (sPen == position && sHand == position) {
				holder.selectorLayout.setVisibility(View.VISIBLE);
				holder.selectorImage.setImageBitmap(mSeletorImage.get(BOTH));
			} else if (sPen == position) {
				holder.selectorLayout.setVisibility(View.VISIBLE);
				holder.selectorImage.setImageBitmap(mSeletorImage.get(PEN));
			} else if (sHand == position) {
				holder.selectorLayout.setVisibility(View.VISIBLE);
				holder.selectorImage.setImageBitmap(mSeletorImage.get(HAND));
			}
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {		
			if (event.getAction() == MotionEvent.ACTION_UP) {
				curEventType = (SPenLibrary.getEvent(event).isPen() ? 0 : 1);
				int position = ((ViewHolder)v.getTag()).position;
				
				if (!mEditorTool.isDrawingToolDivision()) {
					sHand = position;
					mEditorTool.setClipArtPosition(EditorToolUtil.TYPE_HAND, position);
					notifyDataSetChanged();
					return false;
				}
				
				if (!mEditorTool.isAbleSelectClipart(curEventType))	return false;
				
				if (curEventType == EditorToolUtil.TYPE_HAND) {
					sHand = position;
				}
				else {
					sPen = position;
				}
				
				mEditorTool.setClipArtPosition(curEventType, position);
				notifyDataSetChanged();
				
				return false;
			}			
			return true;
		}
	
	}
	
	/**
	 * <P>Child view holder Class in GridView</P> 
	 */
	private class ViewHolder{
		int position;
		
		ImageView image;
		ImageView selectorImage;
		FrameLayout selectorLayout;
	}
	
}
