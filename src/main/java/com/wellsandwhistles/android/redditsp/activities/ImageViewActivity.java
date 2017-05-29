package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyIfNotCached;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.image.GetAlbumInfoListener;
import com.wellsandwhistles.android.redditsp.image.GetImageInfoListener;
import com.wellsandwhistles.android.redditsp.image.ImageInfo;
import com.wellsandwhistles.android.redditsp.image.ImgurAPI;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditPost;
import com.wellsandwhistles.android.redditsp.reddit.url.PostCommentListingURL;
import com.wellsandwhistles.android.redditsp.views.GIFView;
import com.wellsandwhistles.android.redditsp.views.HorizontalSwipeProgressOverlay;
import com.wellsandwhistles.android.redditsp.views.MediaVideoView;
import com.wellsandwhistles.android.redditsp.views.RedditPostView;
import com.wellsandwhistles.android.redditsp.views.glview.SRGLSurfaceView;
import com.wellsandwhistles.android.redditsp.views.imageview.BasicGestureHandler;
import com.wellsandwhistles.android.redditsp.views.imageview.ImageTileSource;
import com.wellsandwhistles.android.redditsp.views.imageview.ImageTileSourceWholeBitmap;
import com.wellsandwhistles.android.redditsp.views.imageview.ImageViewDisplayListManager;
import com.wellsandwhistles.android.redditsp.views.liststatus.ErrorView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

public class ImageViewActivity extends BaseActivity implements RedditPostView.PostSelectionListener, ImageViewDisplayListManager.Listener {

	private static final String TAG = "ImageViewActivity";

	private TextView mProgressText;

	private GLSurfaceView surfaceView;

	private String mUrl;

	private boolean mIsPaused = true, mIsDestroyed = false;
	private CacheRequest mRequest;

	private boolean mHaveReverted = false;

	private ImageViewDisplayListManager mImageViewDisplayerManager;

	private HorizontalSwipeProgressOverlay mSwipeOverlay;
	private boolean mSwipeCancelled;

	private RedditPost mPost;

	private ImageInfo mImageInfo;
	private ImgurAPI.AlbumInfo mAlbumInfo;
	private int mAlbumImageIndex;

	private FrameLayout mLayout;

	private int mGallerySwipeLengthPx;

	@Override
	protected boolean baseActivityIsToolbarActionBarEnabled() {
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		final int gallerySwipeLengthDp = PrefsUtility.pref_behaviour_gallery_swipe_length_dp(this, sharedPreferences);
		mGallerySwipeLengthPx = General.dpToPixels(this, gallerySwipeLengthDp);

		final Intent intent = getIntent();

		mUrl = intent.getDataString();

		if (mUrl == null) {
			finish();
			return;
		}

		mPost = intent.getParcelableExtra("post");

		if (intent.hasExtra("album")) {
			LinkHandler.getImgurAlbumInfo(
					this,
					intent.getStringExtra("album"),
					Constants.Priority.IMAGE_VIEW,
					0,
					new GetAlbumInfoListener() {

						@Override
						public void onFailure(
								final @CacheRequest.RequestFailureType int type,
								final Throwable t,
								final Integer status,
								final String readableMessage) {

							// Do nothing
						}

						@Override
						public void onSuccess(final ImgurAPI.AlbumInfo info) {
							General.UI_THREAD_HANDLER.post(new Runnable() {
								@Override
								public void run() {
									mAlbumInfo = info;
									mAlbumImageIndex = intent.getIntExtra("albumImageIndex", 0);
								}
							});
						}
					}
			);
		}

		Log.i(TAG, "Loading URL " + mUrl);

		final DonutProgress progressBar = new DonutProgress(this);
		progressBar.setIndeterminate(true);
		progressBar.setFinishedStrokeColor(Color.rgb(200, 200, 200));
		progressBar.setUnfinishedStrokeColor(Color.rgb(50, 50, 50));
		final int progressStrokeWidthPx = General.dpToPixels(this, 15);
		progressBar.setUnfinishedStrokeWidth(progressStrokeWidthPx);
		progressBar.setFinishedStrokeWidth(progressStrokeWidthPx);
		progressBar.setStartingDegree(-90);
		progressBar.initPainters();

		final LinearLayout progressTextLayout = new LinearLayout(this);
		progressTextLayout.setOrientation(LinearLayout.VERTICAL);
		progressTextLayout.setGravity(Gravity.CENTER_HORIZONTAL);

		progressTextLayout.addView(progressBar);
		final int progressDimensionsPx = General.dpToPixels(this, 150);
		progressBar.getLayoutParams().width = progressDimensionsPx;
		progressBar.getLayoutParams().height = progressDimensionsPx;

		mProgressText = new TextView(this);
		mProgressText.setText(R.string.download_loading);
		mProgressText.setAllCaps(true);
		mProgressText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		mProgressText.setGravity(Gravity.CENTER_HORIZONTAL);
		progressTextLayout.addView(mProgressText);
		mProgressText.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
		mProgressText.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
		((ViewGroup.MarginLayoutParams) mProgressText.getLayoutParams()).topMargin
				= General.dpToPixels(this, 10);

		final RelativeLayout progressLayout = new RelativeLayout(this);
		progressLayout.addView(progressTextLayout);
		((RelativeLayout.LayoutParams) progressTextLayout.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);
		progressTextLayout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
		progressTextLayout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

		mLayout = new FrameLayout(this);
		mLayout.addView(progressLayout);

		LinkHandler.getImageInfo(this, mUrl, Constants.Priority.IMAGE_VIEW, 0, new GetImageInfoListener() {

			@Override
			public void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {
				revertToWeb();
			}

			@Override
			public void onSuccess(final ImageInfo info) {

				Log.i(TAG, "Got image URL: " + info.urlOriginal);

				Log.i(TAG, "Got image Type: " + info.type);

				Log.i(TAG, "Got media Type: " + info.mediaType);

				mImageInfo = info;

				final URI uri = General.uriFromString(info.urlOriginal);

				if (uri == null) {
					revertToWeb();
					return;
				}

				if (mImageInfo.mediaType != null) {
					Log.i(TAG, "Media type " + mImageInfo.mediaType + " detected");
				}

				Log.i(TAG, "Proceeding with download");
				makeCacheRequest(progressBar, uri);
			}

			@Override
			public void onNotAnImage() {
				revertToWeb();
			}
		});

		final FrameLayout outerFrame = new FrameLayout(this);
		outerFrame.addView(mLayout);
		mLayout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
		mLayout.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

		setBaseActivityContentView(outerFrame);
	}

	private void setMainView(View v) {

		mLayout.removeAllViews();
		mLayout.addView(v);

		mSwipeOverlay = new HorizontalSwipeProgressOverlay(this);
		mLayout.addView(mSwipeOverlay);

		v.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
		v.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
	}

	private void onImageLoaded(
			final CacheManager.ReadableCacheFile cacheFile,
			final String mimetype) {

		if (mimetype == null || (!Constants.Mime.isImage(mimetype) && !Constants.Mime.isVideo(mimetype))) {
			revertToWeb();
			return;
		}

		final InputStream cacheFileInputStream;
		try {
			cacheFileInputStream = cacheFile.getInputStream();
		} catch (IOException e) {
			revertToWeb();
			return;
		}

		if (cacheFileInputStream == null) {
			revertToWeb();
			return;
		}

		if (Constants.Mime.isVideo(mimetype)) {

			General.UI_THREAD_HANDLER.post(new Runnable() {
				@Override
				public void run() {

					if (mIsDestroyed) return;
					mRequest = null;
					try {
						final RelativeLayout layout = new RelativeLayout(ImageViewActivity.this);
						layout.setGravity(Gravity.CENTER);

						final MediaVideoView videoView = new MediaVideoView(ImageViewActivity.this);

						videoView.setVideoURI(cacheFile.getUri());
						videoView.setMediaController(null);

						layout.addView(videoView);
						setMainView(layout);

						videoView.requestFocus();

						layout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
						layout.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
						videoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

						videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								mp.setLooping(true);
								videoView.start();
							}
						});

						videoView.setOnErrorListener(
								new MediaPlayer.OnErrorListener() {
									@Override
									public boolean onError(final MediaPlayer mediaPlayer, final int i, final int i1) {
										revertToWeb();
										return true;
									}
								});

						final BasicGestureHandler gestureHandler = new BasicGestureHandler(ImageViewActivity.this);
						videoView.setOnTouchListener(gestureHandler);
						layout.setOnTouchListener(gestureHandler);

					} catch (OutOfMemoryError e) {
						General.quickToast(ImageViewActivity.this, R.string.imageview_oom);
						revertToWeb();

					} catch (Throwable e) {
						General.quickToast(ImageViewActivity.this, R.string.imageview_invalid_video);
						revertToWeb();
					}

				}
			});
		} else if (Constants.Mime.isImageGif(mimetype)) {

			General.UI_THREAD_HANDLER.post(new Runnable() {
				@Override
				public void run() {

					if (mIsDestroyed) return;
					mRequest = null;

					try {
						final GIFView gifView = new GIFView(ImageViewActivity.this, cacheFileInputStream);
						setMainView(gifView);
						gifView.setOnTouchListener(new BasicGestureHandler(ImageViewActivity.this));

					} catch (OutOfMemoryError e) {
						General.quickToast(ImageViewActivity.this, R.string.imageview_oom);
						revertToWeb();

					} catch (Throwable e) {
						General.quickToast(ImageViewActivity.this, R.string.imageview_invalid_gif);
						revertToWeb();
					}
				}
			});
		} else {

			final ImageTileSource imageTileSource;
			try {

				final long bytes = cacheFile.getSize();
				final byte[] buf = new byte[(int) bytes];

				try {
					new DataInputStream(cacheFileInputStream).readFully(buf);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				try {
					imageTileSource = new ImageTileSourceWholeBitmap(buf);

				} catch (Throwable t) {
					Log.e(TAG, "Exception when creating ImageTileSource", t);
					General.quickToast(this, R.string.imageview_decode_failed);
					revertToWeb();
					return;
				}

			} catch (OutOfMemoryError e) {
				General.quickToast(this, R.string.imageview_oom);
				revertToWeb();
				return;
			}

			General.UI_THREAD_HANDLER.post(new Runnable() {
				@Override
				public void run() {

					if (mIsDestroyed) return;
					mRequest = null;
					mImageViewDisplayerManager = new ImageViewDisplayListManager(
							ImageViewActivity.this,
							imageTileSource,
							ImageViewActivity.this);
					surfaceView = new SRGLSurfaceView(ImageViewActivity.this, mImageViewDisplayerManager);
					setMainView(surfaceView);

					if (mIsPaused) {
						surfaceView.onPause();
					} else {
						surfaceView.onResume();
					}
				}
			});
		}
	}

	public void onPostSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(this, post.src.getUrl(), false, post.src.getSrc());
	}

	public void onPostCommentsSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(this, PostCommentListingURL.forPostId(post.src.getIdAlone()).generateJsonUri().toString(), false);
	}

	@Override
	public void onBackPressed() {
		if (General.onBackPressed()) super.onBackPressed();
	}

	private void revertToWeb() {

		Log.i(TAG, "Using internal browser");

		final Runnable r = new Runnable() {
			@Override
			public void run() {
				if (!mHaveReverted) {
					mHaveReverted = true;
					LinkHandler.onLinkClicked(ImageViewActivity.this, mUrl, true);
					finish();
				}
			}
		};

		if (General.isThisUIThread()) {
			r.run();
		} else {
			General.UI_THREAD_HANDLER.post(r);
		}
	}

	@Override
	public void onPause() {

		if (mIsPaused) throw new RuntimeException();

		mIsPaused = true;

		super.onPause();
		if (surfaceView != null) {
			surfaceView.onPause();
		}
	}

	@Override
	public void onResume() {

		if (!mIsPaused) throw new RuntimeException();

		mIsPaused = false;

		super.onResume();
		if (surfaceView != null) {
			surfaceView.onResume();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mIsDestroyed = true;
		if (mRequest != null) mRequest.cancel();
	}

	@Override
	public void onSingleTap() {
		finish();
	}

	@Override
	public void onHorizontalSwipe(final float pixels) {

		if (mSwipeCancelled) return;

		if (mSwipeOverlay != null && mAlbumInfo != null) {
			mSwipeOverlay.onSwipeUpdate(pixels, mGallerySwipeLengthPx);

			if (pixels >= mGallerySwipeLengthPx) {
				// Back

				mSwipeCancelled = true;
				if (mSwipeOverlay != null) {
					mSwipeOverlay.onSwipeEnd();
				}

				if (mAlbumImageIndex > 0) {

					LinkHandler.onLinkClicked(
							this,
							mAlbumInfo.images.get(mAlbumImageIndex - 1).urlOriginal,
							false,
							mPost,
							mAlbumInfo,
							mAlbumImageIndex - 1);

					finish();

				} else {
					General.quickToast(this, R.string.album_already_first_image);
				}

			} else if (pixels <= -mGallerySwipeLengthPx) {
				// Forwards

				mSwipeCancelled = true;
				if (mSwipeOverlay != null) {
					mSwipeOverlay.onSwipeEnd();
				}

				if (mAlbumImageIndex < mAlbumInfo.images.size() - 1) {

					LinkHandler.onLinkClicked(
							this,
							mAlbumInfo.images.get(mAlbumImageIndex + 1).urlOriginal,
							false,
							mPost,
							mAlbumInfo,
							mAlbumImageIndex + 1);

					finish();

				} else {
					General.quickToast(this, R.string.album_already_last_image);
				}
			}
		}
	}

	@Override
	public void onHorizontalSwipeEnd() {

		mSwipeCancelled = false;

		if (mSwipeOverlay != null) {
			mSwipeOverlay.onSwipeEnd();
		}
	}

	@Override
	public void onImageViewDLMOutOfMemory() {
		if (!mHaveReverted) {
			General.quickToast(this, R.string.imageview_oom);
			revertToWeb();
		}
	}

	@Override
	public void onImageViewDLMException(Throwable t) {
		if (!mHaveReverted) {
			General.quickToast(this, R.string.imageview_decode_failed);
			revertToWeb();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mImageViewDisplayerManager != null) {
			mImageViewDisplayerManager.resetTouchState();
		}
	}

	private void makeCacheRequest(final DonutProgress progressBar, URI uri) {
		CacheManager.getInstance(this).makeRequest(
				mRequest = new CacheRequest(
						uri,
						RedditAccountManager.getAnon(),
						null,
						Constants.Priority.IMAGE_VIEW,
						0,
						DownloadStrategyIfNotCached.INSTANCE,
						Constants.FileType.IMAGE,
						CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
						false,
						false,
						this) {

					private boolean mProgressTextSet = false;

					@Override
					protected void onCallbackException(Throwable t) {
						BugReportActivity.handleGlobalError(context.getApplicationContext(), new SRError(null, null, t));
					}

					@Override
					protected void onDownloadNecessary() {
						General.UI_THREAD_HANDLER.post(new Runnable() {
							@Override
							public void run() {
								progressBar.setVisibility(View.VISIBLE);
								progressBar.setIndeterminate(true);
							}
						});
					}

					@Override
					protected void onDownloadStarted() {
					}

					@Override
					protected void onFailure(final @RequestFailureType int type, Throwable t, Integer status, final String readableMessage) {

						final SRError error = General.getGeneralErrorForFailure(context, type, t, status, url.toString());

						General.UI_THREAD_HANDLER.post(new Runnable() {
							@Override
							public void run() {
								// TODO handle properly
								mRequest = null;
								final LinearLayout layout = new LinearLayout(context);
								final ErrorView errorView = new ErrorView(ImageViewActivity.this, error);
								layout.addView(errorView);
								errorView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
								setMainView(layout);
							}
						});
					}

					@Override
					protected void onProgress(final boolean authorizationInProgress, final long bytesRead, final long totalBytes) {
						General.UI_THREAD_HANDLER.post(new Runnable() {
							@Override
							public void run() {
								progressBar.setVisibility(View.VISIBLE);
								progressBar.setIndeterminate(authorizationInProgress);
								progressBar.setProgress(((float) ((1000 * bytesRead) / totalBytes)) / 1000);

								if (!mProgressTextSet) {
									mProgressText.setText(General.bytesToMegabytes(totalBytes));
									mProgressTextSet = true;
								}
							}
						});
					}

					@Override
					protected void onSuccess(
							final CacheManager.ReadableCacheFile cacheFile,
							long timestamp,
							UUID session,
							boolean fromCache,
							final String mimetype) {

						onImageLoaded(cacheFile, mimetype);
					}
				});
	}
}


