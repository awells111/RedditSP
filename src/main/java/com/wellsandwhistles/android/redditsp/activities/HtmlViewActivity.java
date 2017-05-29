package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.fragments.WebViewFragment;

public class HtmlViewActivity extends BaseActivity {

	private WebViewFragment webView;

	public void onCreate(final Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();

		final String html = intent.getStringExtra("html");
		final String title = intent.getStringExtra("title");
		setTitle(title);

		if(html == null) {
			BugReportActivity.handleGlobalError(this, "No HTML");
		}

		webView = WebViewFragment.newInstanceHtml(html);

		setBaseActivityContentView(View.inflate(this, R.layout.main_single, null));

		getSupportFragmentManager().beginTransaction().add(R.id.main_single_frame, webView).commit();
	}

	@Override
	public void onBackPressed() {

		if(General.onBackPressed() && !webView.onBackButtonPressed())
			super.onBackPressed();
	}
}
