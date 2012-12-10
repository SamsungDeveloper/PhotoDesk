package com.samsung.photodesk.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.photodesk.R;
import com.samsung.photodesk.view.CustomProgressDialog;
import com.samsung.spen.lib.image.SPenImageFilter;
import com.samsung.spen.lib.image.SPenImageFilterConstants;

/**
 * <p>Filter Operation GridView</p>
 * Showing kind of filter and managing filter operation. 
 */
public class EditorFilterGridView extends GridView{

	private final int FILTER_KIND_INDEX = 0;
	private final int FILTER_LEVEL_INDEX = 1;
	
	private Context mContext;	
	private PhotoDeskScanvasView mSCanvas;
	
	private Bitmap mSCanvasBg;
	
	private String[] mFilterNames;
	
	private CustomProgressDialog mProgressDialog;
	
	private int[][] mFilterIndex = {
			{SPenImageFilterConstants.FILTER_ORIGINAL, 2}, {SPenImageFilterConstants.FILTER_VIVID, 2}, 
			{SPenImageFilterConstants.FILTER_GRAY, 2}, {SPenImageFilterConstants.FILTER_SEPIA, 2}, 
			{SPenImageFilterConstants.FILTER_COLORIZE, 2}, {SPenImageFilterConstants.FILTER_NEGATIVE, 2}, 
			{SPenImageFilterConstants.FILTER_BRIGHT, 0}, {SPenImageFilterConstants.FILTER_DARK, 0}, 
			{SPenImageFilterConstants.FILTER_VINTAGE, 2}, {SPenImageFilterConstants.FILTER_BLUR, 0}, 
			{SPenImageFilterConstants.FILTER_RETRO, 2}, {SPenImageFilterConstants.FILTER_FUSAIN, 2}, 
			{SPenImageFilterConstants.FILTER_COLORSKETCH, 0}, {SPenImageFilterConstants.FILTER_SUNSHINE, 2}, 
			{SPenImageFilterConstants.FILTER_MOSAIC, 2}, {SPenImageFilterConstants.FILTER_POPART, 2}, 
			{SPenImageFilterConstants.FILTER_MAGICPEN, 2}, {SPenImageFilterConstants.FILTER_OILPAINT, 2}, 
			{SPenImageFilterConstants.FILTER_CARTOONIZE, 2}, {SPenImageFilterConstants.FILTER_CLASSIC, 2}, 
	};
	private final int res[] = {
			R.drawable.filter_effect_automatic, R.drawable.filter_effect_automatic, R.drawable.filter_effect_gray,
			R.drawable.filter_effect_sepia, R.drawable.filter_effect_mint, R.drawable.filter_effect_reversal,
			R.drawable.filter_effect_brightly, R.drawable.filter_effect_darkly, R.drawable.filter_effect_vintage, 
			R.drawable.filter_effect_blurring, R.drawable.filter_effect_retro, R.drawable.filter_effect_charcoal, 
			R.drawable.filter_effect_color_sketch, R.drawable.filter_effect_sunshine, R.drawable.filter_effect_mosaic,
			R.drawable.filter_effect_pop_art, R.drawable.filter_effect_masicpen, R.drawable.filter_effect_oil_painting,
			R.drawable.filter_effect_cartoon, R.drawable.filter_effect_classic
	};
	
	public EditorFilterGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		initView();
	}

	/**
	 * <p>Initialization gridview</p>
	 */
	private void initView() {
		mFilterNames = mContext.getResources().getStringArray(R.array.filters_name);
		setAdapter(new FilterAdapter());
	}
	
	/**
	 * <p>Set SCanvas datas</p>
	 * Set SCanvas Instance and backgound Image of SCanvas.
	 * @param sc			{@link PhotoDeskScanvasView}
	 * @param rotation		Image rotation
	 */
	public void setSCanvasData(PhotoDeskScanvasView sc, int rotation) {
		mSCanvas = sc;
		loadSCanvasBackgroundImage(rotation);
	}
	
	/**
	 * <p>Destroy background Image resource</p>
	 */
	public void destroy() {
		if (mSCanvasBg != null) {
			mSCanvasBg.recycle();
			mSCanvasBg = null;
		}
	}
	
	/**
	 * <p>Load background Image of SCanvas</p>
	 * @param rotation		Image rotation
	 */
	public void loadSCanvasBackgroundImage(int rotation) {
		if(mSCanvasBg != null)	return;

		PhotoDeskSCanvasManager util = PhotoDeskSCanvasManager.getInstence(mContext);	
		BitmapFactory.Options options  = PhotoDeskSCanvasManager.getInstence(mContext)
				.getResizeImageOption((String)mSCanvas.getTag(), util.getSCanvasParentWidth(), 
						util.getSCanvasParentHeight());

		
		Bitmap baseBitmap = BitmapFactory.decodeFile((String)mSCanvas.getImagepath(), options);
		
		Matrix m = new Matrix();
		m.setRotate(rotation);
		mSCanvasBg = Bitmap.createBitmap(baseBitmap, 0, 0, baseBitmap.getWidth(), baseBitmap.getHeight(), m, false);
	
	}
	
	/**
	 * <p>Return Apply filter to background image</p>
	 * @param kind	filter index
	 * @return		Apply filter to background image
	 */
	public Bitmap applyFilter(int index) {
		int filterIdx = mFilterIndex[index][FILTER_KIND_INDEX];
		if(filterIdx == 10){
			return mSCanvasBg;
		}

		return SPenImageFilter.filterImageCopy(mSCanvasBg, filterIdx, mFilterIndex[index][FILTER_LEVEL_INDEX]);
	}
	
	/**
	 * <p>Apply filter thread</p>
	 */
	class SCanvasBgChangeAsyncTask extends AsyncTask<Integer, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Integer... params) {			
			return applyFilter(params[0]);
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			mSCanvas.setBackgroundImage(result);

			if (mProgressDialog != null)	mProgressDialog.dismiss();
		}
		
	}
	
	/**
	 * <p>Filter gridview adapter class</p>
	 */
	private class FilterAdapter extends BaseAdapter {
		int selectedPosition;
		
		FilterAdapter() {
			selectedPosition = 0;
		}
		
		
		@Override
		public int getCount() {
			return res.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.editor_filter_item, null);
				
				ImageView iv = (ImageView) convertView.findViewById(R.id.iVFilterImage);
				TextView tv = (TextView) convertView.findViewById(R.id.tVFilterName);
				
				convertView.setTag(new ViewHolder(iv, tv, position));
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mProgressDialog = CustomProgressDialog.show(getContext(), 
								getResources().getString(R.string.filtering_title), 
								getResources().getString(R.string.filtering_ing));
						
						final ViewHolder holder = (ViewHolder)v.getTag();
						int position = holder.mPosition;
						selectedPosition = position;
						
						SCanvasBgChangeAsyncTask task = new SCanvasBgChangeAsyncTask();
						task.execute(position);
						
						notifyDataSetChanged();
					}
				});
				
			}

			final ViewHolder holder = (ViewHolder)convertView.getTag();
			
			((FilterItemImageView)holder.mIv).setImageDrawable(getResources().getDrawable(res[position]));
			((TextView)holder.mTv).setText(mFilterNames[position]);
			((FilterItemImageView)holder.mIv).setSelected( (selectedPosition == position) ? true : false);
			
			return convertView;
		}
		
	}


	class ViewHolder{
		private ImageView mIv;
		private TextView mTv;
		private int mPosition;
		
		ViewHolder(ImageView iv, TextView tv, int position) {
			mIv = iv;
			mTv = tv;
			mPosition = position;
		}
	}

}
