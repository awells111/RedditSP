package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.fragments.WebViewFragment;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditPost;
import com.wellsandwhistles.android.redditsp.reddit.url.PostCommentListingURL;

//todo alexw I think I can get rid of this along with the rest of the legacy stuff because I upped the minApi to 16
public class WebViewActivity extends BaseActivity implements RedditPreparedPost.PostSelectionListener {

	private WebViewFragment webView;
	public static final int VIEW_IN_BROWSER = 10,
			CLEAR_CACHE = 20,
			USE_HTTPS = 30,
			SHARE = 40;

	private RedditPost mPost;

	public void onCreate(final Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();

		String url = intent.getStringExtra("url");
		mPost = intent.getParcelableExtra("post");

		if(url == null) {
			BugReportActivity.handleGlobalError(this, "No URL");
		}

		webView = WebViewFragment.newInstance(url, mPost);

		setBaseActivityContentView(View.inflate(this, R.layout.main_single, null));

		getSupportFragmentManager().beginTransaction().add(R.id.main_single_frame, webView).commit();
	}

	@Override
	public void onBackPressed() {

		if(General.onBackPressed() && !webView.onBackButtonPressed())
			super.onBackPressed();
	}

	public void onPostSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(this, post.src.getUrl(), false, post.src.getSrc());
	}

	public void onPostCommentsSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(this, PostCommentListingURL.forPostId(post.src.getIdAlone()).toString(), false);
	}


	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		final String currentUrl = webView.getCurrentUrl();

		switch(item.getItemId()) {

			case VIEW_IN_BROWSER:
				if(currentUrl != null) {
					try {
						final Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(currentUrl));
						startActivity(intent);
						finish(); //to clear from backstack

					} catch(Exception e) {
						Toast.makeText(this, "Error: could not launch browser.", Toast.LENGTH_LONG).show();
					}
				}
				return true;

			case CLEAR_CACHE:

				webView.clearCache();
				Toast.makeText(this, R.string.web_view_clear_cache_success_toast, Toast.LENGTH_LONG).show();
				return true;

			case USE_HTTPS:

				if(currentUrl != null) {

					if(currentUrl.startsWith("https://")) {
						General.quickToast(this, R.string.webview_https_already);
						return true;
					}

					if(!currentUrl.startsWith("http://")) {
						General.quickToast(this, R.string.webview_https_unknownprotocol);
						return true;
					}

					LinkHandler.onLinkClicked(this, currentUrl.replace("http://", "https://"), true, mPost);
					return true;
				}

			case SHARE:
				if (currentUrl != null){
					final Intent mailer = new Intent(Intent.ACTION_SEND);
					mailer.setType("text/plain");
					if (mPost != null){
						mailer.putExtra(Intent.EXTRA_SUBJECT, mPost.title);
					}
					mailer.putExtra(Intent.EXTRA_TEXT, currentUrl);
					startActivity(Intent.createChooser(mailer, getString(R.string.action_share)));
				}
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, VIEW_IN_BROWSER, 0, R.string.web_view_open_browser);
		menu.add(0, CLEAR_CACHE, 1, R.string.web_view_clear_cache);
		menu.add(0, USE_HTTPS, 2, R.string.webview_use_https);
		menu.add(0, SHARE, 3, R.string.action_share);
		return super.onCreateOptionsMenu(menu);
	}

	public String getCurrentUrl() {
		return webView.getCurrentUrl();
	}
}
