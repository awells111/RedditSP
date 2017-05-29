package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

public class WebViewFixed extends WebView {

	public WebViewFixed(final Context context) {
		super(context);
	}

	public WebViewFixed(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public WebViewFixed(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus) {
		try {
			super.onWindowFocusChanged(hasWindowFocus);
		} catch (NullPointerException ex) {
			Log.e("WebView", "WebView.onWindowFocusChanged", ex);
		}
	}

	public void clearBrowser() {
		this.clearCache(true);
		this.clearFormData();
		this.clearHistory();
		CookieManager.getInstance().removeAllCookie();
	}
}
