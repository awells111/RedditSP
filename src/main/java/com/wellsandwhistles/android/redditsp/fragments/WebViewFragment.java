package com.wellsandwhistles.android.redditsp.fragments;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditPost;
import com.wellsandwhistles.android.redditsp.reddit.url.RedditURLParser;
import com.wellsandwhistles.android.redditsp.views.WebViewFixed;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class WebViewFragment extends Fragment implements RedditPreparedPost.PostSelectionListener {

	private AppCompatActivity mActivity;

	private String mUrl, html;
	private volatile String currentUrl;
	private volatile boolean goingBack;
	private volatile int lastBackDepthAttempt;

	private WebViewFixed webView;
	private ProgressBar progressView;
	private FrameLayout outer;

	public static WebViewFragment newInstance(final String url, final RedditPost post) {

		final WebViewFragment f = new WebViewFragment();

		final Bundle bundle = new Bundle(1);
		bundle.putString("url", url);
		if (post != null) bundle.putParcelable("post", post);
		f.setArguments(bundle);

		return f;
	}

	public static WebViewFragment newInstanceHtml(final String html) {

		final WebViewFragment f = new WebViewFragment();

		final Bundle bundle = new Bundle(1);
		bundle.putString("html", html);
		f.setArguments(bundle);

		return f;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		// TODO load position/etc?
		super.onCreate(savedInstanceState);
		mUrl = getArguments().getString("url");
		html = getArguments().getString("html");
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

		mActivity = (AppCompatActivity) getActivity();

		CookieSyncManager.createInstance(mActivity);

		outer = (FrameLayout) inflater.inflate(R.layout.web_view_fragment, null);

		webView = (WebViewFixed) outer.findViewById(R.id.web_view_fragment_webviewfixed);
		final FrameLayout loadingViewFrame = (FrameLayout) outer.findViewById(R.id.web_view_fragment_loadingview_frame);

		/*handle download links show an alert box to load this outside the internal browser*/
		webView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				{
					new AlertDialog.Builder(mActivity)
							.setTitle(R.string.download_link_title)
							.setMessage(R.string.download_link_message)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setData(Uri.parse(url));
									getContext().startActivity(i);
									mActivity.onBackPressed(); //get back from internal browser
								}
							})
							.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									mActivity.onBackPressed(); //get back from internal browser
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();
				}
			}
		});
		/*handle download links end*/

		progressView = new ProgressBar(mActivity, null, android.R.attr.progressBarStyleHorizontal);
		loadingViewFrame.addView(progressView);
		loadingViewFrame.setPadding(General.dpToPixels(mActivity, 10), 0, General.dpToPixels(mActivity, 10), 0);

		final WebSettings settings = webView.getSettings();

		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setDomStorageEnabled(true);
		settings.setDisplayZoomControls(false);

		// TODO handle long clicks

		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, final int newProgress) {

				super.onProgressChanged(view, newProgress);

				General.UI_THREAD_HANDLER.post(new Runnable() {
					@Override
					public void run() {
						progressView.setProgress(newProgress);
						progressView.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
					}
				});
			}
		});


		if (mUrl != null) {
			webView.loadUrl(mUrl);
		} else {
			webView.loadDataWithBaseURL("https://reddit.com/", html, "text/html; charset=UTF-8", null, null);
		}

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

				if (url == null) return false;

				if (url.startsWith("data:")) {
					// Prevent imgur bug where we're directed to some random data URI
					return true;
				}

				// Go back if loading same page to prevent redirect loops.
				if (goingBack && currentUrl != null && url.equals(currentUrl)) {

					General.quickToast(mActivity,
							String.format(Locale.US, "Handling redirect loop (level %d)", -lastBackDepthAttempt), Toast.LENGTH_SHORT);

					lastBackDepthAttempt--;

					if (webView.canGoBackOrForward(lastBackDepthAttempt)) {
						webView.goBackOrForward(lastBackDepthAttempt);
					} else {
						mActivity.finish();
					}
				} else {

					if (RedditURLParser.parse(Uri.parse(url)) != null) {
						LinkHandler.onLinkClicked(mActivity, url, false);
					} else {
						webView.loadUrl(url);
						currentUrl = url;
					}
				}

				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);

				if (mUrl != null && url != null) {

					final AppCompatActivity activity = mActivity;

					if (activity != null) {
						activity.setTitle(url);
					}
				}
			}

			@Override
			public void onPageFinished(final WebView view, final String url) {
				super.onPageFinished(view, url);

				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {

						General.UI_THREAD_HANDLER.post(new Runnable() {
							@Override
							public void run() {

								if (currentUrl == null || url == null) return;

								if (!url.equals(view.getUrl())) return;

								if (goingBack && url.equals(currentUrl)) {

									General.quickToast(mActivity,
											String.format(Locale.US, "Handling redirect loop (level %d)", -lastBackDepthAttempt));

									lastBackDepthAttempt--;

									if (webView.canGoBackOrForward(lastBackDepthAttempt)) {
										webView.goBackOrForward(lastBackDepthAttempt);
									} else {
										mActivity.finish();
									}

								} else {
									goingBack = false;
								}
							}
						});
					}
				}, 1000);
			}

			@Override
			public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
				super.doUpdateVisitedHistory(view, url, isReload);
			}
		});

		final FrameLayout outerFrame = new FrameLayout(mActivity);
		outerFrame.addView(outer);

		return outerFrame;
	}

	@Override
	public void onDestroyView() {

		webView.stopLoading();
		webView.loadData("<html></html>", "text/plain", "UTF-8");
		webView.reload();
		webView.loadUrl("about:blank");
		outer.removeAllViews();
		webView.destroy();

		final CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();

		super.onDestroyView();
	}

	public boolean onBackButtonPressed() {

		if (webView.canGoBack()) {
			goingBack = true;
			lastBackDepthAttempt = -1;
			webView.goBack();
			return true;
		}

		return false;
	}

	public void onPostSelected(final RedditPreparedPost post) {
		((RedditPreparedPost.PostSelectionListener) mActivity).onPostSelected(post);
	}

	public void onPostCommentsSelected(final RedditPreparedPost post) {
		((RedditPreparedPost.PostSelectionListener) mActivity).onPostCommentsSelected(post);
	}


	public String getCurrentUrl() {
		return (currentUrl != null) ? currentUrl : mUrl;
	}

	@Override
	public void onPause() {
		super.onPause();

		webView.onPause();
		webView.pauseTimers();
	}

	@Override
	public void onResume() {
		super.onResume();
		webView.resumeTimers();
		webView.onResume();
	}

	public void clearCache() {
		webView.clearBrowser();
	}
}
