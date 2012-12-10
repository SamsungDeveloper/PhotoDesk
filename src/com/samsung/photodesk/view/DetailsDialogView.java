
package com.samsung.photodesk.view;

import java.util.ArrayList;
import java.util.Map.Entry;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.samsung.photodesk.R;
import com.samsung.photodesk.data.MediaDetails;
import com.samsung.photodesk.view.DetailsAddressResolver.AddressResolvingListener;
import com.samsung.photodesk.view.DetailsHelper.CloseListener;
import com.samsung.photodesk.view.DetailsHelper.DetailsSource;
import com.samsung.photodesk.view.DetailsHelper.DetailsViewContainer;

/**
 * <p>Dialog view for detail information</p>
 *
 */
public class DetailsDialogView implements DetailsViewContainer {

    private final Context mContext;

    private DetailsAdapter mAdapter;

    private MediaDetails mDetails;

    private final DetailsSource mSource;

    private int mIndex;

    private CustomDetailsDialog mDialog;

    /**
     * Constructor
     * @param context - {@link Context}
     * @param source - DetailsSource
     */
    public DetailsDialogView(Context context, DetailsSource source) {
        mContext = context;
        mSource = source;
    }

    /**
     * <p>Show dialog</p>
     */
    public void show() {
        reloadDetails(mSource.getIndex());
        mDialog.show();
    }

    /**
     * <p>Hide dialog</p>
     */
    public void hide() {
        mDialog.hide();
    }

    /**
     * <p>Reload detail information</p>
     */
    public void reloadDetails(int indexHint) {
        int index = mSource.findIndex(indexHint);
        if (index == -1)
            return;
        MediaDetails details = mSource.getDetails();
        if (details != null) {
            if (mIndex == index && mDetails == details)
                return;
            mIndex = index;
            mDetails = details;
            setDetails(details);
        }
    }

    /**
     * <p>Set detail information</p>
     * @param details - MediaDetails
     */
    private void setDetails(MediaDetails details) {
        

        mAdapter = new DetailsAdapter(details);

        LinearLayout LLdetailsList = (LinearLayout)LayoutInflater.from(mContext)
                .inflate(R.layout.custom_details_dlg, null, false);
        ListView detailsList = (ListView)LLdetailsList.findViewById(R.id.details_list);
        LinearLayout closeBtn = (LinearLayout)LLdetailsList.findViewById(R.id.btnClose);
        
        detailsList.setAdapter(mAdapter);
        
        
        mDialog = new CustomDetailsDialog(mContext, LLdetailsList);
        
        closeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 mDialog.dismiss();
			}
		});
    }

    private class DetailsAdapter extends BaseAdapter implements AddressResolvingListener {
        private final ArrayList<String> mItems;

        private int mLocationIndex;

        public DetailsAdapter(MediaDetails details) {
            Context context = mContext.getApplicationContext();
            mItems = new ArrayList<String>(details.size());
            mLocationIndex = -1;
            setDetails(context, details);
        }

        private void setDetails(Context context, MediaDetails details) {
            for (Entry<Integer, Object> detail : details) {
                String value;
                switch (detail.getKey()) {
                    case MediaDetails.INDEX_LOCATION: {

                        double[] latlng = (double[])detail.getValue();

                        mLocationIndex = mItems.size();
                        value = DetailsHelper.resolveAddress(mContext, latlng, this);
                        break;
                    }
                    case MediaDetails.INDEX_SIZE: {
                        value = Formatter.formatFileSize(context, (Long)detail.getValue());
                        break;
                    }
                    case MediaDetails.INDEX_WHITE_BALANCE: {
                        value = "1".equals(detail.getValue()) ? context.getString(R.string.manual)
                                : context.getString(R.string.auto);
                        break;
                    }
                    case MediaDetails.INDEX_FLASH: {
                        MediaDetails.FlashState flash = (MediaDetails.FlashState)detail.getValue();
                        // show more information
                        // when it is fixed.
                        if (flash.isFlashFired()) {
                            value = context.getString(R.string.flash_on);
                        } else {
                            value = context.getString(R.string.flash_off);
                        }
                        break;
                    }
                    case MediaDetails.INDEX_EXPOSURE_TIME: {
                        value = (String)detail.getValue();
                        double time = Double.valueOf(value);
                        if (time < 1.0f) {
                            value = String.format("1/%d", (int)(0.5f + 1 / time));
                        } else {
                            int integer = (int)time;
                            time -= integer;
                            value = String.valueOf(integer) + "''";
                            if (time > 0.0001) {
                                value += String.format(" 1/%d", (int)(0.5f + 1 / time));
                            }
                        }
                        break;
                    }

                    default: {
                        Object valueObj = detail.getValue();
                        // This shouldn't happen, log its key to help us
                        // diagnose the problem.
                        if (valueObj == null) {

                        }
                        value = valueObj.toString();
                    }
                }
                int key = detail.getKey();
                if (details.hasUnit(key)) {
                    value = String.format("%s : %s %s", DetailsHelper.getDetailsName(context, key),
                            value, context.getString(details.getUnit(key)));
                } else {
                    value = String.format("%s : %s", DetailsHelper.getDetailsName(context, key),
                            value);
                }
                mItems.add(value);

            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        public int getCount() {
            return mItems.size();
        }

        public Object getItem(int position) {
            return mDetails.getDetail(position);
        }

        public long getItemId(int position) {
            return position;
        }


        public void onAddressAvailable(String address) {
            mItems.set(mLocationIndex, address);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = (TextView)LayoutInflater.from(mContext.getApplicationContext()).inflate(
                        R.layout.details, parent, false);
            } else {
                tv = (TextView)convertView;
            }
            tv.setText(mItems.get(position));
            return tv;

        }
    }

    @Override
    public void setCloseListener(CloseListener listener) {}
}
