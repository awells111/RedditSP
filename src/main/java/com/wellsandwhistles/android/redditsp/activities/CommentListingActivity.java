package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccountChangeListener;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.common.DialogUtils;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.fragments.CommentListingFragment;
import com.wellsandwhistles.android.redditsp.listingcontrollers.CommentListingController;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
import com.wellsandwhistles.android.redditsp.reddit.url.PostCommentListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.RedditURLParser;
import com.wellsandwhistles.android.redditsp.reddit.url.UserCommentListingURL;

import java.util.UUID;

public class CommentListingActivity extends RefreshableActivity
		implements RedditAccountChangeListener,
		OptionsMenuUtility.OptionsMenuCommentsListener,
		RedditPreparedPost.PostSelectionListener,
		SessionChangeListener {

	private static final String TAG = "CommentListingActivity";

	public static final String EXTRA_SEARCH_STRING = "cla_search_string";

	private static final String SAVEDSTATE_SESSION = "cla_session";
	private static final String SAVEDSTATE_SORT = "cla_sort";
	private static final String SAVEDSTATE_FRAGMENT = "cla_fragment";

	private CommentListingController controller;

	private CommentListingFragment mFragment;

	public void onCreate(final Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		setTitle(getString(R.string.app_name));

		RedditAccountManager.getInstance(this).addUpdateListener(this);

		if(getIntent() != null) {

			final Intent intent = getIntent();

			final String url = intent.getDataString();
			final String searchString = intent.getStringExtra(EXTRA_SEARCH_STRING);
			controller = new CommentListingController(RedditURLParser.parseProbableCommentListing(Uri.parse(url)), this);
			controller.setSearchString(searchString);

			Bundle fragmentSavedInstanceState = null;

			if(savedInstanceState != null) {

				if(savedInstanceState.containsKey(SAVEDSTATE_SESSION)) {
					controller.setSession(UUID.fromString(savedInstanceState.getString(SAVEDSTATE_SESSION)));
				}

				if(savedInstanceState.containsKey(SAVEDSTATE_SORT)) {
					controller.setSort(PostCommentListingURL.Sort.valueOf(
							savedInstanceState.getString(SAVEDSTATE_SORT)));
				}

				if(savedInstanceState.containsKey(SAVEDSTATE_FRAGMENT)) {
					fragmentSavedInstanceState = savedInstanceState.getBundle(SAVEDSTATE_FRAGMENT);
				}
			}

			doRefresh(RefreshableFragment.COMMENTS, false, fragmentSavedInstanceState);

		} else {
			throw new RuntimeException("Nothing to show!");
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		final UUID session = controller.getSession();
		if(session != null) {
			outState.putString(SAVEDSTATE_SESSION, session.toString());
		}

		final PostCommentListingURL.Sort sort = controller.getSort();
		if(sort != null) {
			outState.putString(SAVEDSTATE_SORT, sort.name());
		}

		if(mFragment != null) {
			outState.putBundle(SAVEDSTATE_FRAGMENT, mFragment.onSaveInstanceState());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		OptionsMenuUtility.prepare(
				this,
				menu,
				false,
				false,
				true,
				false,
				false,
				controller.isUserCommentListing(),
				false,
				controller.isSortable(),
				null,
				false,
				null,
				null);

		if(mFragment != null) {
			mFragment.onCreateOptionsMenu(menu);
		}

		return true;
	}

	public void onRedditAccountChanged() {
		requestRefresh(RefreshableFragment.ALL, false);
	}

	@Override
	protected void doRefresh(final RefreshableFragment which, final boolean force, final Bundle savedInstanceState) {
		mFragment = controller.get(this, force, savedInstanceState);

		final View view = mFragment.getView();
		setBaseActivityContentView(view);
		General.setLayoutMatchParent(view);

		setTitle(controller.getCommentListingUrl().humanReadableName(this, false));
		invalidateOptionsMenu();
	}

	public void onRefreshComments() {
		controller.setSession(null);
		requestRefresh(RefreshableFragment.COMMENTS, true);
	}

	public void onSortSelected(final PostCommentListingURL.Sort order) {
		controller.setSort(order);
		requestRefresh(RefreshableFragment.COMMENTS, false);
	}

	public void onSortSelected(final UserCommentListingURL.Sort order) {
		controller.setSort(order);
		requestRefresh(RefreshableFragment.COMMENTS, false);
	}

	@Override
	public void onSearchComments() {
		DialogUtils.showSearchDialog(this, new DialogUtils.OnSearchListener() {
			@Override
			public void onSearch(@Nullable String query) {
				Intent searchIntent = getIntent();
				searchIntent.putExtra(EXTRA_SEARCH_STRING, query);
				startActivity(searchIntent);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if(mFragment != null) {
			if(mFragment.onOptionsItemSelected(item)) {
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public void onSessionChanged(UUID session, SessionChangeType type, long timestamp) {
		Log.i(TAG, type.name() + " session changed to " + (session != null ? session.toString() : "<null>"));
		controller.setSession(session);
	}

	public void onPostSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(this, post.src.getUrl(), false, post.src.getSrc());
	}

	public void onPostCommentsSelected(final RedditPreparedPost post) {
		LinkHandler.onLinkClicked(this, PostCommentListingURL.forPostId(post.src.getIdAlone()).toString(), false);
	}

	@Override
	public void onBackPressed() {
		if(General.onBackPressed()) super.onBackPressed();
	}
}
