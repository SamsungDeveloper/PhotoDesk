package com.samsung.photodesk.view;

import android.content.Context;

import com.samsung.photodesk.R;
import com.samsung.photodesk.data.MediaDetails;
import com.samsung.photodesk.view.DetailsAddressResolver.AddressResolvingListener;

/**
 * <p>Managing for detail information</p>
 *
 */
public class DetailsHelper {
    private static DetailsAddressResolver sAddressResolver;
    private DetailsViewContainer mContainer;

    /**
     * Interface for DetailsSource
     *
     */
    public interface DetailsSource {
        public int size();
        public int getIndex();
        public int findIndex(int indexHint);
        public MediaDetails getDetails();
    }

    /**
     * Interface for CloseListener
     *
     */
    public interface CloseListener {
        public void onClose();
    }

    /**
     * Interface for DetailsViewContainer
     *
     */
    public interface DetailsViewContainer {
        public void reloadDetails(int indexHint);
        public void setCloseListener(CloseListener listener);
        public void show();
        public void hide();
    }

    public DetailsHelper(Context context, DetailsSource source) {
        mContainer = new DetailsDialogView(context, source);
    }

    public void reloadDetails(int indexHint) {
        mContainer.reloadDetails(indexHint);
    }

    public void setCloseListener(CloseListener listener) {
        mContainer.setCloseListener(listener);
    }

    public static String resolveAddress(Context context, double[] latlng,
            AddressResolvingListener listener) {
        if (sAddressResolver == null) {
            sAddressResolver = new DetailsAddressResolver(context);
        } else {
            sAddressResolver.cancel();
        }
        return sAddressResolver.resolveAddress(latlng, listener);
    }

    public static void pause() {
        if (sAddressResolver != null) sAddressResolver.cancel();
    }

    public void show() {
        mContainer.show();
    }

    public void hide() {
        mContainer.hide();
    }

    public static String getDetailsName(Context context, int key) {
        switch (key) {
            case MediaDetails.INDEX_TITLE:
                return context.getString(R.string.title);
            case MediaDetails.INDEX_DESCRIPTION:
                return context.getString(R.string.description);
            case MediaDetails.INDEX_DATETIME:
                return context.getString(R.string.time);
            case MediaDetails.INDEX_LOCATION:
                return context.getString(R.string.location);
            case MediaDetails.INDEX_PATH:
                return context.getString(R.string.path);
            case MediaDetails.INDEX_WIDTH:
                return context.getString(R.string.width);
            case MediaDetails.INDEX_HEIGHT:
                return context.getString(R.string.height);
            case MediaDetails.INDEX_ORIENTATION:
                return context.getString(R.string.orientation);
            case MediaDetails.INDEX_DURATION:
                return context.getString(R.string.duration);
            case MediaDetails.INDEX_MIMETYPE:
                return context.getString(R.string.mimetype);
            case MediaDetails.INDEX_SIZE:
                return context.getString(R.string.file_size);
            case MediaDetails.INDEX_MAKE:
                return context.getString(R.string.maker);
            case MediaDetails.INDEX_MODEL:
                return context.getString(R.string.model);
            case MediaDetails.INDEX_FLASH:
                return context.getString(R.string.flash);
            case MediaDetails.INDEX_APERTURE:
                return context.getString(R.string.aperture);
            case MediaDetails.INDEX_FOCAL_LENGTH:
                return context.getString(R.string.focal_length);
            case MediaDetails.INDEX_WHITE_BALANCE:
                return context.getString(R.string.white_balance);
            case MediaDetails.INDEX_EXPOSURE_TIME:
                return context.getString(R.string.exposure_time);
            case MediaDetails.INDEX_ISO:
                return context.getString(R.string.iso);
            default:
                return "Unknown key" + key;
        }
    }
}
