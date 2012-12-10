package com.samsung.photodesk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.photodesk.FolderFragment.ViewHolder;
import com.samsung.photodesk.MenuTask.OnOperationListener;
import com.samsung.photodesk.cache.ThumbnailCache;
import com.samsung.photodesk.data.ContentItem;
import com.samsung.photodesk.data.FolderItem;
import com.samsung.photodesk.data.ImageItem;
import com.samsung.photodesk.data.MediaDetails;
import com.samsung.photodesk.data.MediaItem;
import com.samsung.photodesk.data.MediaObject;
import com.samsung.photodesk.data.VideoItem;
import com.samsung.photodesk.editor.ImageEditorActivity;
import com.samsung.photodesk.loader.ExifThumbnailLoader;
import com.samsung.photodesk.loader.Future;
import com.samsung.photodesk.loader.FutureListener;
import com.samsung.photodesk.loader.ImageLoadTask;
import com.samsung.photodesk.loader.MediaLoader;
import com.samsung.photodesk.loader.ThreadPool;
import com.samsung.photodesk.util.MediaUtils;
import com.samsung.photodesk.util.PhotoDeskUtils;
import com.samsung.photodesk.util.ProtectUtil;
import com.samsung.photodesk.view.CustomGestureOverlayView;
import com.samsung.photodesk.view.DetailsHelper;
import com.samsung.photodesk.view.SpenDialog;
import com.samsung.photodesk.view.ViewTouchImage;
import com.samsung.sdraw.SDrawLibrary;
import com.samsung.spen.lib.input.SPenEventLibrary;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SPenDetachmentListener;

/**
 * <p>ImageView Activity</p>
 * display detail image when selected image.
 * ImageView using PaperAdapter({@link FragmentStatePagerAdapter}) for image load.
 * 
 */
public class ImageViewActivity extends BaseActivity  implements OnPageChangeListener, OnClickListener, CustomGestureOverlayView.OnGesturePerformedListener {
    private static final int IMAGE_EDITOR	= 0;
    private static final int SLIDE_SHOW		= 1;
    private static final int VIEW_UNPROTECT = 2;
    
    public static final String IS_EDIT = "is_edit";
    public static final String CURRENT_POSITION = "current_position";
    public static final String BUCKET_ID = "bucketId";
    public static final String POSITION = "position";
    public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
    
    private static ArrayList<MediaItem> sMediaItems;
    private static ShareActionProvider sShareActionProvider;
	
    private ViewPager mViewPager;
	
	private boolean mIsShare;
	private boolean mIsSingleShare;
	private int mSingleShareIndex;
	
    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    
    private ImageView mProtectImage;
    private LinearLayout mLLImageEdit;
    
    private boolean mIsAction = false;
    private boolean mIsEdit = false;
    
    private GestureLibrary mGestureLibrary;
    private SPenEventLibrary mSpenEvent;
 
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view);
        
        initItems();
        setViewPager();
        initGesture();
        initSpenEvent();
         
        mLLImageEdit = (LinearLayout) findViewById(R.id.lLImageEdit);
        mLLImageEdit.setOnClickListener(this);
        
        if (!SDrawLibrary.isSupportedModel() || 
        		sMediaItems.get(mViewPager.getCurrentItem()).getType() != MediaObject.VIDEO) {
        	mLLImageEdit.setVisibility(View.GONE);
        }
        mProtectImage = (ImageView) findViewById(R.id.iVProtect);
    }
	
	/**
	 * Initialize items
	 */
	private void initItems() {
		Intent intent = getIntent();
        int slideComeType =intent.getIntExtra(SlideShowActivity.SLIDE_MODE, SlideShowActivity.SLIDE_IMAGE_VIEW);

        if (intent.getData() != null && slideComeType == SlideShowActivity.SLIDE_IMAGE_VIEW) {
        	String action = intent.getAction();
        	if (Intent.ACTION_VIEW.equalsIgnoreCase(action)|| ACTION_REVIEW.equalsIgnoreCase(action)){
        	    mIsAction = true;
        		getMediaItemInIntent(intent, action);     	
        	}
        } else if (slideComeType == SlideShowActivity.SLIDE_FOLDER_VIEW) {
            sMediaItems = getSelectedFolderItems(FolderFragment.getFolderItems());
            if (sMediaItems == null) {
                finish();
                return;
            } 
        } else if (slideComeType == SlideShowActivity.SLIDE_CONTENT_VIEW) {
            sMediaItems = ContentItem.getInstance().getItem();
            long currentIndex = intent.getLongExtra("position_item", -1);
            for (int mediaCnt = 0; mediaCnt < sMediaItems.size(); mediaCnt++) {
                if (sMediaItems.get(mediaCnt).getId() == currentIndex) {
                	getIntent().putExtra(POSITION, mediaCnt);
                	break;
                }
            }
        } else {
        	sMediaItems = ContentItem.getInstance().getItem();
        }
	}

	/**
	 * Initialize gesture
	 */
	private void initGesture() {
		mGestureLibrary = GestureLibraries.fromRawResource(this, R.raw.rotate_gestures);
        if (!mGestureLibrary.load()) {
        	mGestureLibrary = null;
        	return;
        }

        CustomGestureOverlayView gestures = (CustomGestureOverlayView) findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(this);
	}

	/**
	 * Initialize Spen event
	 */
	private void initSpenEvent() {
		mSpenEvent = new SPenEventLibrary();
		mSpenEvent.registerSPenDetachmentListener(this, new SPenDetachmentListener() {
			boolean mFirst = false;
			@Override
			public void onSPenDetached(boolean detached) {
				if (!mFirst) {
					mFirst = true;
					return;
				}
				if (!detached) return;
				final SpenDialog dialog = new SpenDialog(ImageViewActivity.this);
				dialog.setContentView(
						getResources().getString(R.string.move_image_editor), 
						getResources().getDimension(R.dimen.base_text_size));
				dialog.setRightBtn(R.string.yes, new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ImageViewActivity.this, ImageEditorActivity.class);
						intent.putExtra("path", sMediaItems.get(mViewPager.getCurrentItem()).getPath());
						intent.putExtra("empty", false);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivityForResult(intent, IMAGE_EDITOR);
						dialog.dismiss();
					}
				});
				dialog.setLeftBtn(R.string.no, null);
				dialog.show();
			}
		});
	}

	@Override
	public void onResume() {
		if (sMediaItems == null) {
			finish();
		}
		super.onResume();
	}
    
	private void getMediaItemInIntent(Intent intent, String action){
		String contentType = getContentType(intent);
		if (contentType == null) {
			Toast.makeText(this, "no search Item", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		Uri uri = intent.getData();
		String path = getPathInMediaStore(intent);
		
        if (path == null){  
        	path = getPathInIntent(intent, uri);
        	if (path != null) {
        		sMediaItems = new ArrayList<MediaItem>();		        	
            	sMediaItems.add(new ImageItem(path));
        	}
        } else {
	    	ContentResolver cr = getContentResolver();
	    	FolderItem folder = MediaLoader.getFolder(path, cr);
			if (folder != null) {
				sMediaItems = MediaLoader.getMediaItems(folder.getId(), cr);
				mIsSingleShare = true;
			}
		}
    }

	/**
	 * Set ViewPager
	 */
	private void setViewPager() {
        mViewPager = (ViewPager)findViewById(R.id.vpImageView);
        updateView(getFirstPostion());
        mViewPager.setOnPageChangeListener(this);
	}
	
	/**
	 * Update ViewPager
	 * @param index - page index
	 */
	private void updateView(int index) {
		mViewPager.setAdapter(new ImageViewPagerAdapter(getFragmentManager()));
        mViewPager.setCurrentItem(index, false);
	}
	
	/**
	 * Get first position
	 * @return position
	 */
	private int getFirstPostion() {
		Intent intent = getIntent();
        int slideComeType = intent.getIntExtra(SlideShowActivity.SLIDE_MODE, SlideShowActivity.SLIDE_IMAGE_VIEW);
        if (intent.getData() != null && slideComeType == SlideShowActivity.SLIDE_IMAGE_VIEW) {
        	long currentIndex = intent.getLongExtra("position_item", -1);
        	int postion = 0;
            if (currentIndex != -1){
            	for (int mediaCnt = 0; mediaCnt < sMediaItems.size(); mediaCnt++) {
            		if (sMediaItems.get(mediaCnt).getId() == currentIndex){
            			postion = mediaCnt;
                        mSingleShareIndex = mediaCnt;
                        break;
                	}      
            	}
            } else {          
            	String path = getPathInMediaStore(intent);
    	        for (int index = 0; index < sMediaItems.size(); index++) {
    	            MediaItem item = sMediaItems.get(index);
    	            if (item.getPath().equals(path)) {   
    	            	mViewPager.setCurrentItem(index, false);
                        mSingleShareIndex = index;
                        postion = index;
    	            	break;
    	            }
    	        }
            }
            return postion;
        } else {
        	return getIntent().getIntExtra("position", 0);
        }
	}

	/**
	 * Get path from MediaStore
	 * @param intent {@link Intent}
	 * @return path
	 */
    private String getPathInMediaStore(Intent intent) {
		String path = null;
		Cursor c = getContentResolver().query(Uri.parse(intent.getDataString()), null, null, null, null);
		if (c != null) {
			if (c.moveToNext()) {
				if (c.getColumnIndex(MediaStore.MediaColumns.DATA) > 0) {
					path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
				}
			}
			c.close();
		}
		return path;
	}

	private String getPathInIntent(Intent intent, Uri uri) {
    	if (intent.getScheme().compareTo("file")==0) {
        	//Already download image show in web.        	
        	sMediaItems = new ArrayList<MediaItem>();		        	
        	return uri.getPath();  
        } else if (intent.getScheme().compareTo("content")==0) { 
        	InputStream inStream = null;
        	FileOutputStream fos = null;
        	Bitmap bmp = null;
        	//show image direct in Web
			try {	
				inStream = getContentResolver().openInputStream(uri);
				bmp = BitmapFactory.decodeStream(inStream); 
				File file = new File(PhotoDeskUtils.getDefualtFodler() + "temp.cache");
				fos = new FileOutputStream(file);
				bmp.compress(CompressFormat.PNG, 100, fos);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (bmp != null) {
					bmp.recycle();
					bmp = null;
				}
			}
			return PhotoDeskUtils.getDefualtFodler() + "temp.cache";
        }
    	
    	return null;
    }

	private String getContentType(Intent intent) {
        String type = intent.getType();
        if (type != null) return type;

        Uri uri = intent.getData();
        try {
            return getContentResolver().getType(uri);
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }
    
    
    @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	PhotoDeskActionBar actionBar = getPhotoDeskActionBar();
    	actionBar.createMenu(R.menu.viewer_menu, menu);
    	actionBar.setActionCallback(mOperationListener);
        if (actionBar.isShowing()) {
            actionBar.hide();
            btnEditVisibility(View.GONE);
        }
    	
    	MenuItem item = menu.findItem(R.id.share);
    	 if ((item != null && !mIsShare) && sMediaItems != null) {
         	sShareActionProvider = (ShareActionProvider) item.getActionProvider();
         	if (!mIsSingleShare) { 
         		sShareActionProvider.setShareIntent(PhotoDeskUtils.createShareIntent(sMediaItems, getIntent().getIntExtra("position", 0)));
         	} else {
         		mIsSingleShare = false;
         		sShareActionProvider.setShareIntent(PhotoDeskUtils.createShareIntent(sMediaItems, mSingleShareIndex));
         	}
         	mIsShare = true;
         }
    	 
    	return true;
 	}
    
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
    	if (sMediaItems == null) return false;
    	if (sMediaItems.get(mViewPager.getCurrentItem()).getId() == -1)return false;
    	TypedArray a = this.obtainStyledAttributes(R.styleable.AppTheme);
    	getActionBar().setBackgroundDrawable(a.getDrawable(R.styleable.AppTheme_imageDetailActionBar));
		MediaItem selectedItem = sMediaItems.get(mViewPager.getCurrentItem());
		boolean isProtectedItem = ProtectUtil.getInstance().isProtected(selectedItem.getPath());
		boolean isProtectedFolder = ProtectUtil.getInstance().isProtected(selectedItem.getPath().replace(selectedItem.getDisplayName(), ""));
		boolean isSAMMFile = SCanvasView.isSAMMFile(selectedItem.getPath());
		setTitle(selectedItem.getDisplayName());
	
        for (int i = 0; i < menu.size(); ++i) {
        	int menuId = menu.getItem(i).getItemId();
        	MenuItem menuItem = menu.getItem(i);
            if (menuId == R.id.protect || menuId == R.id.unprotect) {
            	if (!SCanvasView.isSignatureExist(this)) {
            		menuItem.setVisible(false);
            	} else {
	            	if (isProtectedItem) {
	            		if (menuId == R.id.protect) menuItem.setVisible(false);
	            		if (menuId == R.id.unprotect) menuItem.setVisible(true);   
	                } else {
	            		if (menuId == R.id.protect) menuItem.setVisible(true);
	            		if (menuId == R.id.unprotect) menuItem.setVisible(false);                	
	                }
            	}
            } else if (menuId == R.id.delete || menuId == R.id.rename) {
        		if(isProtectedFolder || isProtectedItem) {
        			menuItem.setVisible(false);
        		} else {
        			menuItem.setVisible(true);
        		}
	    	}            
            else if (menuId == R.id.rotation) {
               if (isSAMMFile || isProtectedFolder || isProtectedItem || PhotoDeskUtils.isRotationSupported(selectedItem.getMimeType()) == false) {
                   menuItem.setVisible(false);
               } else {
                   menuItem.setVisible(true);
               }
            }
        }
        return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			
		case R.id.slide:
		    slideShowStart();
		    finish();
			break;
			
		case R.id.detail:
			showDetails();
			break;
			
		case R.id.set_as:
			PhotoDeskUtils.startSetAsActivity(this, sMediaItems.get(mViewPager.getCurrentItem()).getPath());
			break;
			
		case R.id.unprotect:
			ProtectUtil.getInstance().checkSignatureUnprotect(this, VIEW_UNPROTECT);
			break;
			
		case R.id.location_edit:
			PhotoDeskUtils.startLocationEdit(this, sMediaItems.get(mViewPager.getCurrentItem()), 1);
			break;	
			
		default:
			MenuTask menuTask = new MenuTask(this, mOperationListener);
			menuTask.setSelectedItems(sMediaItems.get(mViewPager.getCurrentItem()));
			menuTask.onItemClicked(item.getItemId());
			break;

		}

		return true;
    }
	
	/**
     * Processing the results of menu task.
     */
	OnOperationListener mOperationListener = new OnOperationListener() {

		@Override
		public void onDone(long menuId, FolderItem selectedFolder, ArrayList<MediaObject> selectedItems,
				ArrayList<MediaObject> doneItems, boolean canceled) {
			if (menuId == R.id.delete || menuId == R.id.move) {
				for (MediaObject item : doneItems) {
					removeItem(item.getId());
				}
				removeEmptyFolder(doneItems);
			} else if (menuId == R.id.protect) {
				ActionBar actionBar = getActionBar();

		        if (ProtectUtil.getInstance().isProtected(sMediaItems.get(mViewPager.getCurrentItem()).getPath())
		        		&& actionBar.isShowing()) {
		            mProtectImage.setVisibility(View.VISIBLE);
		        } else {
		        	mProtectImage.setVisibility(View.GONE);
		        }
			} else if (menuId == R.id.rename) {
				MediaItem item = MediaLoader.getMediaItem(getContentResolver(), sMediaItems.get(mViewPager.getCurrentItem()).getType(), selectedFolder.getFilePath());
				if (item != null) {
					sMediaItems.set(mViewPager.getCurrentItem(), item);
					setTitle(item.getDisplayName());
				} 
			}
			mIsEdit = true;
			mViewPager.getAdapter().notifyDataSetChanged();
		}
	};

	/**
	 * Remove empty folder
	 * @param items - selected items
	 */
	private void removeEmptyFolder(ArrayList<MediaObject> items) {
		for (MediaObject item : items) {
			if (item.getId() == FolderItem.NEW_FOLDER_ID)	return ;
			
			File file = new File(item.getPath());
			if (!file.exists()) {
				if (item.getType() == MediaObject.FOLDER) {
					file = new File(((FolderItem)item).getFolderPath());
				} else {
					file = new File(((MediaItem)item).getFolderPath());
				}
			}
			
			if (file.listFiles() != null && file.listFiles().length == 0) {
				file.delete();
				super.onBackPressed();
			}
		}
	}	

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  
    	if (resultCode == RESULT_OK) {
            switch (requestCode) {
	        	case IMAGE_EDITOR:
	        		updateView(mViewPager.getCurrentItem());
	        		if (data != null) {
            			String path = data.getStringExtra(ImageEditorActivity.EDITOR_SAVED_IMAGE_PATH);
          
            			if (path != null) {
                			String where = MediaStore.Images.ImageColumns.DATA + " like '" + path +"'";
                			ContentResolver cr = getContentResolver();
                			Cursor cursor = cr.query(MediaUtils.getContentUri(MediaItem.IMAGE), 
                								MediaUtils.IMAGE_COLUMNS, where, null, null);
                			
                			if (cursor.moveToNext()) {
                				sMediaItems.add(new ImageItem(cursor));
                				mViewPager.getAdapter().notifyDataSetChanged();
                			} 
            			}
            		}
            		mIsEdit = true;
	        		break;
                case VIEW_UNPROTECT:
                	Toast.makeText(this, R.string.unprotect, Toast.LENGTH_SHORT).show();
        			ArrayList<MediaObject> selectedItem = new ArrayList<MediaObject>();
        			selectedItem.add(sMediaItems.get(mViewPager.getCurrentItem()));
        			ProtectUtil.getInstance().changeProtect(selectedItem, new ProtectUtil.ProtectCompleteListener() {
        				
        				@Override
        				public void onComplete() {
        					mIsEdit = true;
        					ActionBar actionBar = getActionBar();
        			        if (ProtectUtil.getInstance().isProtected(sMediaItems.get(mViewPager.getCurrentItem()).getPath())
        			        		&& actionBar.isShowing()) {
        			            mProtectImage.setVisibility(View.VISIBLE);
        			        } else {
        			        	mProtectImage.setVisibility(View.GONE);
        			        }        					
        				}
        			});
                	break;
                case SLIDE_SHOW:
                    int position = mViewPager.getCurrentItem();
                    if (data != null)
                        position = data.getIntExtra("position", position);
                    updateView(position);
                    mViewPager.setCurrentItem(position, false);
                    break;
            }
        } 
    }
    
	/**
	 * Remove item
	 * @param id - item id
	 */
	private void removeItem(long id) {
		if (sMediaItems == null) return;
		for (int index = 0; index < sMediaItems.size(); index++) {
			MediaItem item = sMediaItems.get(index);
			if (item.getId() == id) {
				sMediaItems.remove(index);
				break;
			}
		}
	}
	
	/**
	 * Show details
	 */
	private void showDetails() {
		if (mDetailsSource == null) {
			mDetailsSource = new MyDetailsSource();
		}
		mDetailsSource.findIndex(mViewPager.getCurrentItem());

		if (mDetailsHelper == null) {
			mDetailsHelper = new DetailsHelper(mViewPager.getContext(), mDetailsSource);
		}
		mDetailsHelper.show();
	}

    private class MyDetailsSource implements DetailsHelper.DetailsSource {
        private int mIndex;

        public int size() {
            return sMediaItems.size();
        }

        public int getIndex() {
            return mIndex;
        }

        public int findIndex(int indexHint) {
            mIndex = indexHint;
            return mIndex;
        }

        public MediaDetails getDetails() {

            MediaDetails details = sMediaItems.get(mViewPager.getCurrentItem()).getDetails();
            if (details != null) {
                return details;
            } else {
                return null;
            }
        }
    }
    
    /**
     * <p>ViewPager Adapter class</p>
     * Previous , Current , Next image load
     * 
     */
	public class ImageViewPagerAdapter extends FragmentStatePagerAdapter {
	    private boolean mSlideShowMode = false;
		
		public ImageViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {
			return ImageViewFragment.newInstance(index);
		}

		@Override
		public int getCount() {
			return sMediaItems.size();
		}
		
		@Override
	    public int getItemPosition(Object object){
	        return PagerAdapter.POSITION_NONE;
	    }
		
		@Override
		public Object instantiateItem(ViewGroup view, int arg1) {
            if (mSlideShowMode) {
                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(1000);
                view.startAnimation(animation);
            }
		    return super.instantiateItem(view, arg1);
		}
		
		public void setSlideShowMode(boolean slideShowMode) {
            this.mSlideShowMode = slideShowMode;
        }
	}
    
	/**
	 * <p>ImageViewFragment class</p>
	 * Display image on Viewer
	 * Display thumbnail image because delay time during loading image.
	 *
	 */
    public static class ImageViewFragment extends Fragment {
    	public static final String INDEX = "index";
    	
    	private View mContainer;
    	private ThreadPool mThreadPool;
    	private ViewTouchImage mIvImage;
    	private TextView mTvMessage;
    	private ImageView mIvPlay;
    	private ImageView mIvAniPlay;
    	private ProgressBar mLodingProgress;
    	private Future<Bitmap> mFuture;
    	private ExifThumbnailLoader mThumbnailLoader;
    	private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        
		public static Fragment newInstance(int index) {
			ImageViewFragment ivf = new ImageViewFragment();
			Bundle args = new Bundle();
            args.putInt(INDEX, index);
            ivf.setArguments(args);
			return ivf;
	    }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			mContainer = inflater.inflate(R.layout.image_view_fragment, null);
			
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
			mThreadPool =  ((PhotoDeskApplication)(getActivity()).getApplication()).getThreadPool();
			mTvMessage = (TextView) mContainer.findViewById(R.id.tvMessage);
			mIvImage = (ViewTouchImage) mContainer.findViewById(R.id.ivImage);
	    	mIvPlay = (ImageView) mContainer.findViewById(R.id.iVPlay);
	    	mIvAniPlay = (ImageView) mContainer.findViewById(R.id.iVAniPlay);
	    	mLodingProgress = (ProgressBar) mContainer.findViewById(R.id.progressBar);
	    	
	    	setImage(getArguments().getInt(INDEX));
			return mContainer;
		}
		
		@Override
		public void onDestroy() {
			if (mFuture != null) {
				mFuture.cancel();
			}
			
			if (mThumbnailLoader != null) {
				mThumbnailLoader.cancel(true);
			}
			super.onDestroy();
		}

		/**
		 * <p>Set image data</p>
		 * Display thumbnail image because delay time during loading image.
		 * SAMM data display play icon
		 * @param position - selected item position
		 */
		public void setImage(final int position) {	
			if (sMediaItems == null || position >= sMediaItems.size()) return;
			final MediaItem item = sMediaItems.get(position);
	    	getArguments().putInt(INDEX, position);

	    	mTvMessage.setVisibility(View.GONE);
            mIvImage.setOnClickListener((ImageViewActivity)getActivity());
			if (item.getType() == MediaItem.VIDEO) {
				setVideoImage((VideoItem)item);
			} else {
				mLodingProgress.setVisibility(View.VISIBLE);
				
				loadExifImage(item);
				loadImage(item);
		        
				if (((ImageItem)item).updateSAM()) {
	                mIvImage.setVideoFlag(true);
					mIvAniPlay.setVisibility(View.VISIBLE);
					mIvAniPlay.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							PhotoDeskUtils.startSamAnimation(getActivity(), item.getPath());
						}
					});
				} else {
					mIvAniPlay.setVisibility(View.GONE);
				}
			}
		}
		
		/**
		 * <p>Set video data</p>
		 * Video image is created use first frame data.
		 * @param item {@link VideoItem}
		 */
		private void setVideoImage(final VideoItem item) {
			Bitmap bm = ThumbnailCache.INSTANCE.getBitmap(item.getId());
			if (bm == null) {
				bm = MediaLoader.createVideoThumbnail(item.getPath());
			}
			mIvImage.setImageBitmap(bm);
			mIvImage.setVideoFlag(true);
			mLodingProgress.setVisibility(View.GONE);
			mIvPlay.setVisibility(View.VISIBLE);
			
			mIvPlay.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View view) {
                	PhotoDeskUtils.startVideoPlayer(getActivity(), item.getPath());
                }
            });
		}

		/**
		 * <p>Load Image</p>
		 * Load original image.
		 * @param item - selected item
		 */
		private void loadImage(MediaItem item) {
			if (mFuture != null) {
				mFuture.cancel();
			}

			mFuture = mThreadPool.submit(
					new ImageLoadTask(item, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels),
					
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
	
						if (mThumbnailLoader != null) mThumbnailLoader.cancel(true);
						mHandler.sendMessage(mHandler.obtainMessage(ViewHolder.IMAGE_LOAD, bitmap));
					}
				});
		}

		/**
		 * <p>Load exif thumbnail image</p>
		 * @param item - selected item
		 * 
		 */
		private void loadExifImage(final MediaItem item) {
			if (mThumbnailLoader != null) mThumbnailLoader.cancel(true);
			mThumbnailLoader = new ExifThumbnailLoader(item, mIvImage,
					mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
		        
	        try {
	        	mThumbnailLoader.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, item.getId());
	        } catch (RejectedExecutionException e) {
	        	e.printStackTrace();
	        }
		}

		@SuppressLint("HandlerLeak") final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				if (message.what == ViewHolder.IMAGE_LOAD) {
					if (mIvImage != null) {
						Bitmap bm = (Bitmap) message.obj;
						
						if (bm != null) {
							WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(mIvImage);
							imageViewReference.get().setImageBitmap(bm);
						} else {
							mTvMessage.setVisibility(View.VISIBLE);
						}
					} 
				} 
				mLodingProgress.setVisibility(View.GONE);
			}
		};
	}

	@Override
	public void onPageScrollStateChanged(int state) {}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

	@Override
	public void onPageSelected(int position) {
		setTitle(sMediaItems.get(mViewPager.getCurrentItem()).getDisplayName());

		if (sShareActionProvider != null && sMediaItems != null) {
			sShareActionProvider.setShareIntent(PhotoDeskUtils.createShareIntent(sMediaItems, position));
		}
        ActionBar actionBar = getActionBar();
        if (actionBar.isShowing()) {
			if (ProtectUtil.getInstance().isProtected(sMediaItems.get(mViewPager.getCurrentItem()).getPath())) {
				mProtectImage.setVisibility(View.VISIBLE);
			} else {		
				mProtectImage.setVisibility(View.GONE);
			}			
        }
	}
	    
	/**
	 * Start slide show
	 */
	private void slideShowStart(){
        Intent intent = getIntent();
        intent.setClass(this, SlideShowActivity.class);
        intent.putExtra("slide_position", mViewPager.getCurrentItem());
        intent.putExtra("extends_action", mIsAction);
        if (mIsAction) startActivity(intent);
        else startActivityForResult(intent, SLIDE_SHOW);
    }
    
    @Override
	public void onBackPressed() {
		if (sMediaItems.get(mViewPager.getCurrentItem()).getId() == -1){
			try{
				File file = new File(PhotoDeskUtils.getDefualtFodler() + "temp.cache");
				if (file.exists()) {
					file.delete();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onBackPressed();
	}

	@Override
    public void onClick(View v) {
        switch (v.getId()) {
        
        case R.id.lLImageEdit:
            Intent intent = new Intent(this, ImageEditorActivity.class);
            intent.putExtra(ImageEditorActivity.EDITOR_TYPE_KEY_PATH, sMediaItems.get(mViewPager.getCurrentItem()).getPath());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, IMAGE_EDITOR);
            break;

        default:
            ActionBar actionBar = getActionBar();
             if (actionBar.isShowing()) {
                 actionBar.hide();
                 btnEditVisibility(View.GONE);
             } else {
                 actionBar.show();
                 if (sMediaItems != null) {
                	 if (sMediaItems.get(mViewPager.getCurrentItem()).getId() != -1) {
                    	 btnEditVisibility(View.VISIBLE);
                     } 
                 }
             }
            break;
        }
    }
    
    private void btnEditVisibility(int visibility){
        int aniId = R.anim.actionbar_fade_out;
        if (visibility == View.VISIBLE) {
        	aniId = R.anim.actionbar_fade_in;
        }
            
        Animation animation = AnimationUtils.loadAnimation(this, aniId);
        animation.setDuration(300);
        if (sMediaItems.get(mViewPager.getCurrentItem()).getType() != MediaObject.VIDEO) {
            mLLImageEdit.startAnimation(animation);
            if (SDrawLibrary.isSupportedModel()) {
            	mLLImageEdit.setVisibility(visibility);
            }
        }
        
        mProtectImage.setVisibility(View.GONE);
        if (ProtectUtil.getInstance().isProtected(sMediaItems.get(mViewPager.getCurrentItem()).getPath())) {
            mProtectImage.startAnimation(animation);
            mProtectImage.setVisibility(visibility);
        } 
        
    }
    
    @Override
    public void finish() {
		Intent intent = getIntent();
		intent.putExtra(IS_EDIT, mIsEdit);
		intent.putExtra(CURRENT_POSITION, mViewPager.getCurrentItem());
		setResult(RESULT_OK, intent);
    	
    	super.finish();
    }


    public ArrayList<MediaItem> getSelectedFolderItems(ArrayList<FolderItem> arrFolderItems) {
        if (arrFolderItems == null) return null;
        ArrayList<MediaItem> select = new ArrayList<MediaItem>();
        for (FolderItem item : arrFolderItems) {
            if (!item.isSelected()) continue;
            ArrayList<MediaItem> imageItems = MediaLoader.getMediaItems(item.getId(), getContentResolver());
            select.addAll(imageItems);
        }

        return select;
    }
    
    @Override
    protected void onDestroy() {
    	mSpenEvent.unregisterSPenDetachmentListener(this);
    	super.onDestroy();
    }
    
	@Override
	public void onGesturePerformed(CustomGestureOverlayView overlay,
			Gesture gesture) {
		if (mGestureLibrary == null) return;
		ArrayList<Prediction> predictions = mGestureLibrary.recognize(gesture);

		MediaItem selectedItem = sMediaItems.get(mViewPager.getCurrentItem());
		boolean isProtectedItemt = ProtectUtil.getInstance().isProtected(selectedItem.getPath());
		boolean isProtectedFolder = ProtectUtil.getInstance().isProtected(selectedItem.getPath().replace(selectedItem.getDisplayName(), ""));

		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			if (prediction.score > 3.0) {

				Log.d("ImageView", "prediction.score = " + prediction.score
						+ " predictions.size() = " + predictions.size());
				if (isProtectedFolder 
						|| isProtectedItemt 
						|| PhotoDeskUtils.isRotationSupported(selectedItem.getMimeType()) == false) {

					 if (isProtectedFolder || isProtectedItemt) {
						 Toast.makeText(this, getResources().getText(R.string.rotation_warning1), Toast.LENGTH_SHORT).show();
					 }else {
						 Toast.makeText(this, getResources().getText(R.string.rotation_warning2), Toast.LENGTH_SHORT).show();
					 }
				}
				else {
					MenuTask menuTask = new MenuTask(this, mOperationListener);
					menuTask.setSelectedItems(sMediaItems.get(mViewPager.getCurrentItem()));
					
					if (prediction.name.equals("R_rotate")) {
						menuTask.onItemClicked(R.id.rotation_right);
					} else if (prediction.name.equals("L_rotate")) {
						menuTask.onItemClicked(R.id.rotation_left);
					}
				}
			}
		}
	}

	/**
	 * Get ImageView item
	 * @return ImageView items
	 */
	public static ArrayList<MediaItem> getItems() {
		return sMediaItems;
	}
}
