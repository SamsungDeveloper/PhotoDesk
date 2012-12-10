package com.samsung.photodesk;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.samsung.photodesk.FolderFragment.ViewHolder;
import com.samsung.photodesk.data.ContentItem;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.FutureListener;
import com.samsung.photodesk.loader.ImageLoadTask;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool;

/**
 * <p>Slide show activity</p>
 * Activity about slide show function
 *
 */
public class SlideShowActivity extends Activity implements ViewSwitcher.ViewFactory, OnClickListener {

    public static final String SLIDE_MODE = "slide_mode";

    public static final int SLIDE_SHOW_MILI_SEC = 3000;
    public static final int SLIDE_SHOW_MIN_COUNT = 2;

    public static final int SLIDE_IMAGE_VIEW = 0;
    public static final int SLIDE_CONTENT_VIEW = 1;
    public static final int SLIDE_FOLDER_VIEW = 2;

    private ArrayList <MediaItem> mArrMediaItem;
    private ArrayList <String> mArrPathItem;
    private ImageSwitcher mSwitcher;
    private MediaPlayer mMediaPlayer;

    private Future<Bitmap> mFuture;
    private ThreadPool mThreadPool;

    private int mSlideCount;
    private int mSlideItemType;
    private int mArraySize;
    private int mMusicPosition;
    private int mBackgroundRes;
    
    private boolean mExtendsAction;
    private String mMusicPath;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(R.layout.slide_show);
        if (!init()) return;
        if (savedInstanceState != null) {
            mMusicPosition = savedInstanceState.getInt("music_msec", 0);
            mSlideCount = savedInstanceState.getInt("position");
        }
        loadImage(mSlideCount);
        musicPlay(mMusicPath);
    }
    
    private void initMemberVariable() {
        Bundle data = getIntent().getExtras();
        mSlideCount = data.getInt("slide_position", 0);
        mSlideItemType = data.getInt(SLIDE_MODE, SLIDE_IMAGE_VIEW);
        mExtendsAction = data.getBoolean("extends_action", false);
    }
    
    private boolean init() {
        boolean success = true;
        initMemberVariable();
        setArrMediaItem();
        
        if ((mArrMediaItem == null && mArrPathItem == null) || (mArrMediaItem != null && mArrMediaItem.size() < SLIDE_SHOW_MIN_COUNT) || (mArrPathItem != null && mArrPathItem.size() < SLIDE_SHOW_MIN_COUNT)) {
            Toast.makeText(this, getResources().getString(R.string.slide_show_err), Toast.LENGTH_SHORT).show();
            success = false;
            finish();
        } else {
            mSwitcher = (ImageSwitcher)findViewById(R.id.isSlide);
            mSwitcher.setFactory(this);
            mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_show_fade_in));
            mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_show_fade_out));
            mThreadPool = ((PhotoDeskApplication)getApplication()).getThreadPool();
            mSwitcher.setOnClickListener(this);
        }
        return success;
    }

    @Override
    public View makeView() {
        ImageView i = new ImageView(this);
        if (mBackgroundRes != 0) i.setBackgroundResource(mBackgroundRes);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return i;
    }
    
    private void setArrMediaItem(){
    	 if (mExtendsAction) {
             mArrMediaItem = ImageViewActivity.getItems();           
         } else if (mSlideItemType == SLIDE_FOLDER_VIEW) {
            mArrMediaItem = getSelectedFolderItems(FolderFragment.getFolderItems());
        } else {
            mArrMediaItem = ContentItem.getInstance().getItem();
        }
        mArrMediaItem = getSelectedItems(mArrMediaItem);
        mArraySize = mArrMediaItem.size();
    }
    
    private void nextImage(){
        mSlideCount++;
        if(mSlideCount >= mArraySize) mSlideCount = 0;
        loadImage(mSlideCount);
    }

    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == ViewHolder.IMAGE_LOAD) {
                Bitmap bm = (Bitmap) message.obj;
                
                if (bm != null) {
                    BitmapDrawable bmDraw = new BitmapDrawable(bm);
                    Drawable draw = (Drawable)bmDraw;
                    mSwitcher.setImageDrawable(draw);
                    postDelayed(nextPage, SLIDE_SHOW_MILI_SEC);
                } else {
                    post(nextPage);
                }
            }
        }
    };
    
    final Runnable nextPage = new Runnable() {
        @Override
        public void run() {
            nextImage();
        }
    };

    private void loadImage(int position) {
        if (mFuture != null) {
            mFuture.cancel();
        }
        MediaItem item = null;
        ImageLoadTask imageLoadTask = null;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        item = mArrMediaItem.get(position);
        imageLoadTask = new ImageLoadTask(item, dm.widthPixels, dm.heightPixels);

        mFuture = mThreadPool.submit(imageLoadTask,

        new FutureListener<Bitmap>() {

            public void onFutureDone(Future<Bitmap> future) {

                Bitmap bitmap = future.get();
                if (future.isCancelled()) {
                    if (bitmap != null) {
                        mHandler.sendMessage(mHandler.obtainMessage(ViewHolder.IMAGE_LOAD, bitmap));
                    } else {
                        mHandler.sendMessage(mHandler.obtainMessage());
                    }
                    return;
                }
                mHandler.sendMessage(mHandler.obtainMessage(ViewHolder.IMAGE_LOAD, bitmap));
            }
        });
    }
    
    @Override
    public void onDestroy() {
        if (mFuture != null) mFuture.cancel();
        mHandler.removeCallbacks(nextPage);
        super.onDestroy();
    }
    
    public ArrayList<MediaItem> getSelectedItems(ArrayList<MediaItem> arrItem) {
        if (arrItem == null) return null;
        ArrayList<MediaItem> items = new ArrayList<MediaItem>();
        for (MediaItem item: arrItem) {
            if(mSlideItemType == SLIDE_CONTENT_VIEW){
                if (item.isSelected() && item.getType() == MediaItem.IMAGE) {
                    items.add(item);
                }
            } else {
                if (item.getType() == MediaItem.IMAGE) {
                    items.add(item);
                }
            }
        }
        return items;
    }
    
    @Override
    public void finish() {
        musicStop();
        Intent intent = getIntent();
        intent.setClass(this, ImageViewActivity.class);
        intent.putExtra("position", mSlideCount);
        if (mSlideItemType == SLIDE_CONTENT_VIEW ||mExtendsAction) intent.putExtra("position_item", mArrMediaItem.get(mSlideCount).getId());
        intent.putExtra(SlideShowActivity.SLIDE_MODE, mSlideItemType);
        int requestCode = 0;
        if (mSlideItemType == SLIDE_CONTENT_VIEW) {
            requestCode = ContentFragment.REQ_SLIDE_SHOW;
            startActivityForResult(intent, requestCode);
        } else if (mSlideItemType == SLIDE_FOLDER_VIEW) {
            requestCode = FolderFragment.REQ_SLIDE_SHOW;
            startActivityForResult(intent, requestCode);
        } else {
        	startActivity(intent);
        }
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.isSlide:
                finish();
                break;
        }
    }
    
    public ArrayList<MediaItem> getSelectedFolderItems(ArrayList<FolderItem> arrFolderItems) {
        if (arrFolderItems == null) return null;
        ArrayList<MediaItem> select = new ArrayList<MediaItem>();
        for (FolderItem item : arrFolderItems) {
            if (!item.isSelected()) continue;
            ArrayList<MediaItem> imageItems = MediaLoader.getMediaItems(item.getId(),
                    getContentResolver());
            select.addAll(imageItems);
        }

        return select;
    }
    
    private void musicPlay(final String musicPath) {
        if (musicPath == null) return;
        if (mMediaPlayer == null) mMediaPlayer = new MediaPlayer();
        if (mMediaPlayer.isPlaying()) return;
        try {
            mMediaPlayer.setDataSource(musicPath);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(mMusicPosition);
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void musicStop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        musicStop();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMediaPlayer != null) outState.putInt("music_msec", mMediaPlayer.getCurrentPosition());
        outState.putInt("position", mSlideCount);
        musicStop();
    }
}
