package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Base64OutputStream;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyAlways;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.common.HTTPBackend;
import com.wellsandwhistles.android.redditsp.image.ThumbnailScaler;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedObject;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;
import com.wellsandwhistles.android.redditsp.views.LoadingSpinnerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

public class ImgurUploadActivity extends BaseActivity {

	private static final int REQUEST_CODE = 1;

	private TextView mTextView;

	private ImageView mThumbnailView;

	private String mBase64Data;

	private Button mUploadButton;

	private View mLoadingOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		setTitle(R.string.upload_to_imgur);

		final FrameLayout outerLayout = new FrameLayout(this);

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		mTextView = new TextView(this);
		mTextView.setText(R.string.no_file_selected);
		layout.addView(mTextView);

		General.setAllMarginsDp(this, mTextView, 10);

		mUploadButton = new Button(this);
		mUploadButton.setText(R.string.button_upload);
		mUploadButton.setEnabled(false);
		layout.addView(mUploadButton);
		updateUploadButtonVisibility();

		final Button browseButton = new Button(this);
		browseButton.setText(R.string.button_browse);
		layout.addView(browseButton);

		mThumbnailView = new ImageView(this);
		layout.addView(mThumbnailView);
		General.setAllMarginsDp(this, mThumbnailView, 20);

		browseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, REQUEST_CODE);
			}
		});

		mUploadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(mBase64Data != null) {
					uploadImage();
				} else {
					General.quickToast(ImgurUploadActivity.this, R.string.no_file_selected);
				}
			}
		});

		final ScrollView sv = new ScrollView(this);
		sv.addView(layout);
		outerLayout.addView(sv);

		{
			mLoadingOverlay = new LoadingSpinnerView(this);
			outerLayout.addView(mLoadingOverlay);
			mLoadingOverlay.setBackgroundColor(Color.argb(220, 50, 50, 50));

			final ViewGroup.LayoutParams layoutParams = mLoadingOverlay.getLayoutParams();
			layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
			layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

			mLoadingOverlay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					// Do nothing
				}
			});

			mLoadingOverlay.setVisibility(View.GONE);
		}

		setBaseActivityContentView(outerLayout);

		General.setAllMarginsDp(this, layout, 20);
	}

	private void showLoadingOverlay() {
		mLoadingOverlay.setVisibility(View.VISIBLE);
	}

	private void hideLoadingOverlay() {
		mLoadingOverlay.setVisibility(View.GONE);
	}

	private void updateUploadButtonVisibility() {
		mUploadButton.setVisibility(
				mBase64Data != null
						? View.VISIBLE
						: View.GONE);

	}

	@Override
	protected void onActivityResult(final int requestCode, final int result, final Intent data) {

		if(data == null || data.getData() == null) {
			return;
		}

		if(requestCode != REQUEST_CODE) {
			return;
		}

		if(result != RESULT_OK) {
			return;
		}

		onImageSelected(data.getData());
	}

	private void onImageSelected(final Uri uri) {

		showLoadingOverlay();

		new Thread("Image selected thread") {
			@Override
			public void run() {

				try {
					final ParcelFileDescriptor file = getContentResolver().openFileDescriptor(uri, "r");
					final long statSize = file.getStatSize();

					if(statSize >= 10 * 1000 * 1000) { // Use base 10 just to be safe...
						General.showResultDialog(ImgurUploadActivity.this,
								new SRError(
										getString(R.string.error_file_too_big_title),
										getString(
												R.string.error_file_too_big_message,
												statSize / 1024 + "kB",
												"10MB")));
						return;
					}

					final int thumbnailSizePx = General.dpToPixels(ImgurUploadActivity.this, 200);

					final Bitmap rawBitmap = BitmapFactory.decodeFileDescriptor(file.getFileDescriptor());
					final Bitmap thumbnailBitmap = ThumbnailScaler.scaleNoCrop(rawBitmap, thumbnailSizePx);
					rawBitmap.recycle();

					final InputStream inputStream = getContentResolver().openInputStream(uri);

					final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
					final Base64OutputStream output = new Base64OutputStream(byteOutput, 0);

					try {
						General.copyStream(inputStream, output);
						output.flush();

					} catch(IOException e) {
						throw new RuntimeException(e);
					}

					final String base64String = new String(byteOutput.toByteArray());

					General.UI_THREAD_HANDLER.post(new Runnable() {
						@Override
						public void run() {
							mBase64Data = base64String;
							mUploadButton.setEnabled(true);
							mThumbnailView.setImageBitmap(thumbnailBitmap);
							mTextView.setText(getString(
									R.string.image_selected_summary,
									rawBitmap.getWidth(),
									rawBitmap.getHeight(),
									statSize / 1024 + "kB"));
							hideLoadingOverlay();
							updateUploadButtonVisibility();
						}
					});

				} catch(Exception e) {
					General.showResultDialog(ImgurUploadActivity.this,
							new SRError(
									getString(R.string.error_file_open_failed_title),
									getString(R.string.error_file_open_failed_message),
									e));

					General.UI_THREAD_HANDLER.post(new Runnable() {
						@Override
						public void run() {
							mBase64Data = null;
							mUploadButton.setEnabled(false);
							mThumbnailView.setImageBitmap(null);
							mTextView.setText(R.string.no_file_selected);
							hideLoadingOverlay();
							updateUploadButtonVisibility();
						}
					});
				}
			}
		}.start();
	}

	private void uploadImage() {

		showLoadingOverlay();

		final ArrayList<HTTPBackend.PostField> postFields = new ArrayList<>(1);
		postFields.add(new HTTPBackend.PostField("image", mBase64Data));

		CacheManager.getInstance(this).makeRequest(new CacheRequest(
				General.uriFromString("https://api.imgur.com/3/image"),
				RedditAccountManager.getInstance(this).getDefaultAccount(),
				null,
				Constants.Priority.API_ACTION,
				0,
				DownloadStrategyAlways.INSTANCE,
				Constants.FileType.NOCACHE,
				CacheRequest.DOWNLOAD_QUEUE_IMGUR_API,
				true,
				postFields,
				false,
				false,
				this) {

			@Override
			protected void onCallbackException(final Throwable t) {
				BugReportActivity.handleGlobalError(ImgurUploadActivity.this, t);
			}

			@Override
			protected void onDownloadNecessary() {
			}

			@Override
			protected void onDownloadStarted() {
			}

			@Override
			protected void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer httpStatus, final String readableMessage) {

				General.showResultDialog(ImgurUploadActivity.this, General.getGeneralErrorForFailure(
						ImgurUploadActivity.this,
						type,
						t,
						httpStatus,
						url.toString()));

				General.UI_THREAD_HANDLER.post(new Runnable() {
					@Override
					public void run() {
						hideLoadingOverlay();
					}
				});
			}

			@Override
			public void onJsonParseStarted(final JsonValue result, final long timestamp, final UUID session, final boolean fromCache) {

				try {
					result.join();
				} catch(final InterruptedException e) {
					throw new RuntimeException(e);
				}

				final Uri imageUri;

				try {
					final JsonBufferedObject root = result.asObject();

					if(root == null) {
						throw new RuntimeException("Response root object is null");
					}

					final Boolean success = root.getBoolean("success");

					if(!Boolean.TRUE.equals(success)) {
						onFailure(CacheRequest.REQUEST_FAILURE_UPLOAD_FAIL_IMGUR, null, null, null);
						return;
					}

					final String id = root.getObject("data").getString("id");
					imageUri = Uri.parse("https://imgur.com/" + id);

				} catch(final Throwable t) {
					onFailure(CacheRequest.REQUEST_FAILURE_PARSE_IMGUR, t, null, t.toString());
					return;
				}

				General.UI_THREAD_HANDLER.post(new Runnable() {
					@Override
					public void run() {

						final Intent resultIntent = new Intent();
						resultIntent.setData(imageUri);
						setResult(0, resultIntent);
						finish();
					}
				});
			}

			@Override
			protected void onProgress(final boolean authorizationInProgress, final long bytesRead, final long totalBytes) {
			}

			@Override
			protected void onSuccess(
					final CacheManager.ReadableCacheFile cacheFile,
					final long timestamp,
					final UUID session,
					final boolean fromCache,
					final String mimetype) {
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(General.onBackPressed()) super.onBackPressed();
	}
}
