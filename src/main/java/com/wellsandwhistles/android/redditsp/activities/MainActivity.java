package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.account.RedditAccountChangeListener;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.adapters.MainMenuSelectionListener;
import com.wellsandwhistles.android.redditsp.common.DialogUtils;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.fragments.AccountListDialog;
import com.wellsandwhistles.android.redditsp.fragments.CommentListingFragment;
import com.wellsandwhistles.android.redditsp.fragments.MainMenuFragment;
import com.wellsandwhistles.android.redditsp.fragments.PostListingFragment;
import com.wellsandwhistles.android.redditsp.listingcontrollers.CommentListingController;
import com.wellsandwhistles.android.redditsp.listingcontrollers.PostListingController;
import com.wellsandwhistles.android.redditsp.reddit.PostSort;
import com.wellsandwhistles.android.redditsp.reddit.RedditSubredditHistory;
import com.wellsandwhistles.android.redditsp.reddit.api.RedditSubredditSubscriptionManager;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditSubreddit;
import com.wellsandwhistles.android.redditsp.reddit.url.PostCommentListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.PostListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.RedditURLParser;
import com.wellsandwhistles.android.redditsp.reddit.url.SubredditPostListURL;
import com.wellsandwhistles.android.redditsp.reddit.url.UserCommentListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.UserPostListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.UserProfileURL;
import com.wellsandwhistles.android.redditsp.views.RedditPostView;

import java.util.Locale;
import java.util.UUID;

public class MainActivity extends RefreshableActivity
		implements MainMenuSelectionListener,
		RedditAccountChangeListener,
		RedditPostView.PostSelectionListener,
		OptionsMenuUtility.OptionsMenuSubredditsListener,
		OptionsMenuUtility.OptionsMenuPostsListener,
		OptionsMenuUtility.OptionsMenuCommentsListener,
		SessionChangeListener,
		RedditSubredditSubscriptionManager.SubredditSubscriptionStateChangeListener {

	private boolean twoPane;

	private MainMenuFragment mainMenuFragment;

	private PostListingController postListingController;
	private PostListingFragment postListingFragment;

	private CommentListingController commentListingController;
	private CommentListingFragment commentListingFragment;

	private View mainMenuView;
	private View postListingView;
	private View commentListingView;

	private FrameLayout mSinglePane;

	private FrameLayout mLeftPane;
	private FrameLayout mRightPane;

	private boolean isMenuShown = true;

	private SharedPreferences sharedPreferences;

	@Override
	protected boolean baseActivityIsActionBarBackEnabled() {
		return false;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (savedInstanceState == null) {
			if(PrefsUtility.pref_behaviour_skiptofrontpage(this, sharedPreferences))
				onSelected(SubredditPostListURL.getFrontPage());
		}

		setTitle(R.string.app_name);

		twoPane = General.isTablet(this, sharedPreferences);

		doRefresh(RefreshableFragment.MAIN_RELAYOUT, false, null);

		RedditAccountManager.getInstance(this).addUpdateListener(this);

		if(!sharedPreferences.contains("firstRunMessageShown")) {

			new AlertDialog.Builder(this)
					.setTitle(R.string.firstrun_login_title)
					.setMessage(R.string.firstrun_login_message)
					.setPositiveButton(R.string.firstrun_login_button_now,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int which) {
									new AccountListDialog().show(MainActivity.this.getSupportFragmentManager(), null);
								}
							})
					.setNegativeButton(R.string.firstrun_login_button_later, null)
					.show();

			final SharedPreferences.Editor edit = sharedPreferences.edit();
			edit.putString("firstRunMessageShown", "true");
			edit.putBoolean(getString(R.string.pref_behaviour_skiptofrontpage_key), true);
			edit.apply();

		}

		addSubscriptionListener();

		Boolean startInbox = getIntent().getBooleanExtra("isNewMessage", false);
		if(startInbox) {
			startActivity(new Intent(this, InboxListingActivity.class));
		}
	}

	private void addSubscriptionListener() {
		RedditSubredditSubscriptionManager
				.getSingleton(this, RedditAccountManager.getInstance(this).getDefaultAccount())
				.addListener(this);
	}

	@Override
	public void onSelected(final @MainMenuFragment.MainMenuAction int type) {

		final String username = RedditAccountManager.getInstance(this).getDefaultAccount().username;

		switch(type) {

			case MainMenuFragment.MENU_MENU_ACTION_FRONTPAGE:
				onSelected(SubredditPostListURL.getFrontPage());
				break;

			case MainMenuFragment.MENU_MENU_ACTION_ALL:
				onSelected(SubredditPostListURL.getAll());
				break;

			case MainMenuFragment.MENU_MENU_ACTION_SUBMITTED:
				onSelected(UserPostListingURL.getSubmitted(username));
				break;

			case MainMenuFragment.MENU_MENU_ACTION_SAVED:
				onSelected(UserPostListingURL.getSaved(username));
				break;

			case MainMenuFragment.MENU_MENU_ACTION_PROFILE:
				LinkHandler.onLinkClicked(this, new UserProfileURL(username).toString());
				break;

			case MainMenuFragment.MENU_MENU_ACTION_CUSTOM: {

				final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
				final View root = getLayoutInflater().inflate(R.layout.dialog_mainmenu_custom, null);

				final Spinner destinationType = (Spinner)root.findViewById(R.id.dialog_mainmenu_custom_type);
				final AutoCompleteTextView editText = (AutoCompleteTextView)root.findViewById(R.id.dialog_mainmenu_custom_value);

				final String[] typeReturnValues
						= getResources().getStringArray(R.array.mainmenu_custom_destination_type_return);

				final ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<>(
						this,
						android.R.layout.simple_dropdown_item_1line,
						RedditSubredditHistory.getSubredditsSorted(RedditAccountManager.getInstance(this).getDefaultAccount()).toArray(new String[] {}));

				editText.setAdapter(autocompleteAdapter);
				editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						boolean handled = false;
						if(actionId == EditorInfo.IME_ACTION_GO) {
							openCustomLocation(typeReturnValues, destinationType, editText);
							handled = true;
						}
						return handled;
					}
				});

				alertBuilder.setView(root);

				destinationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{
					@Override
					public void onItemSelected(
							final AdapterView<?> adapterView,
							final View view,
							final int i,
							final long l)
					{
						final String typeName = typeReturnValues[destinationType.getSelectedItemPosition()];

						switch(typeName)
						{
							case "subreddit":
								editText.setAdapter(autocompleteAdapter);
								break;

							default:
								editText.setAdapter(null);
								break;
						}
					}

					@Override
					public void onNothingSelected(final AdapterView<?> adapterView)
					{
						editText.setAdapter(null);
					}
				});

				alertBuilder.setPositiveButton(R.string.dialog_go, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						openCustomLocation(typeReturnValues, destinationType, editText);
					}
				});

				alertBuilder.setNegativeButton(R.string.dialog_cancel, null);

				final AlertDialog alertDialog = alertBuilder.create();
				alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				alertDialog.show();

				break;
			}

			case MainMenuFragment.MENU_MENU_ACTION_INBOX:
				startActivity(new Intent(this, InboxListingActivity.class));
				break;

		}
	}

	private void openCustomLocation(String[] typeReturnValues, Spinner destinationType, AutoCompleteTextView editText) {

		final String typeName = typeReturnValues[destinationType.getSelectedItemPosition()];

		switch(typeName) {
            case "subreddit": {

                final String subredditInput = editText.getText().toString().trim().replace(" ", "");

                try {
                    final String normalizedName = RedditSubreddit.stripRPrefix(subredditInput);
                    final RedditURLParser.RedditURL redditURL = SubredditPostListURL.getSubreddit(normalizedName);
                    if(redditURL == null || redditURL.pathType() != RedditURLParser.SUBREDDIT_POST_LISTING_URL) {
                        General.quickToast(this, R.string.mainmenu_custom_invalid_name);
                    } else {
                        onSelected(redditURL.asSubredditPostListURL());
                    }
                } catch(RedditSubreddit.InvalidSubredditNameException e){
                    General.quickToast(this, R.string.mainmenu_custom_invalid_name);
                }
                break;
            }

            case "user":

                String userInput = editText.getText().toString().trim().replace(" ", "");

                if(!userInput.startsWith("/u/")
                        && !userInput.startsWith("/user/")) {

                    if(userInput.startsWith("u/")
                        || userInput.startsWith("user/")) {

                        userInput = "/" + userInput;

                    } else {
                        userInput = "/u/" + userInput;
                    }
                }

                LinkHandler.onLinkClicked(this, userInput);

                break;

            case "url": {
                LinkHandler.onLinkClicked(this, editText.getText().toString().trim());
                break;
            }
        }
	}

	public void onSelected(final PostListingURL url) {

		if(url == null) {
			return;
		}

		if(twoPane) {

			postListingController = new PostListingController(url, this);
			requestRefresh(RefreshableFragment.POSTS, false);

		} else {
			final Intent intent = new Intent(this, PostListingActivity.class);
			intent.setData(url.generateJsonUri());
			startActivityForResult(intent, 1);
		}
	}

	public void onRedditAccountChanged() {
		addSubscriptionListener();
		postInvalidateOptionsMenu();
		requestRefresh(RefreshableFragment.ALL, false);
	}

	@Override
	protected void doRefresh(final RefreshableFragment which, final boolean force, final Bundle savedInstanceState) {

		if(which == RefreshableFragment.MAIN_RELAYOUT) {

			mainMenuFragment = null;
			postListingFragment = null;
			commentListingFragment = null;

			mainMenuView = null;
			postListingView = null;
			commentListingView = null;

			if(mLeftPane != null) mLeftPane.removeAllViews();
			if(mRightPane != null) mRightPane.removeAllViews();

			twoPane = General.isTablet(this, sharedPreferences);

			final View layout;

			if(twoPane) {
				layout = getLayoutInflater().inflate(R.layout.main_double, null);
				mLeftPane = (FrameLayout)layout.findViewById(R.id.main_left_frame);
				mRightPane = (FrameLayout)layout.findViewById(R.id.main_right_frame);
				mSinglePane = null;
			} else {
				layout = getLayoutInflater().inflate(R.layout.main_single, null);
				mLeftPane = null;
				mRightPane = null;
				mSinglePane = (FrameLayout)layout.findViewById(R.id.main_single_frame);
			}

			setBaseActivityContentView(layout);

			invalidateOptionsMenu();
			requestRefresh(RefreshableFragment.ALL, false);

			return;
		}

		if(twoPane) {

			final FrameLayout postContainer = isMenuShown ? mRightPane : mLeftPane;

			if(isMenuShown && (which == RefreshableFragment.ALL || which == RefreshableFragment.MAIN)) {
				mainMenuFragment = new MainMenuFragment(this, null, force);
				mainMenuView = mainMenuFragment.getView();
				mLeftPane.removeAllViews();
				mLeftPane.addView(mainMenuView);
			}

			if(postListingController != null && (which == RefreshableFragment.ALL || which == RefreshableFragment.POSTS)) {
				if(force && postListingFragment != null) postListingFragment.cancel();
				postListingFragment = postListingController.get(this, force, null);
				postListingView = postListingFragment.getView();
				postContainer.removeAllViews();
				postContainer.addView(postListingView);
			}

			if(commentListingController != null && (which == RefreshableFragment.ALL || which == RefreshableFragment.COMMENTS)) {
				commentListingFragment = commentListingController.get(this, force, null);
				commentListingView = commentListingFragment.getView();
				mRightPane.removeAllViews();
				mRightPane.addView(commentListingView);
			}

		} else {

			if(which == RefreshableFragment.ALL || which == RefreshableFragment.MAIN) {
				mainMenuFragment = new MainMenuFragment(this, null, force);
				mainMenuView = mainMenuFragment.getView();
				mSinglePane.removeAllViews();
				mSinglePane.addView(mainMenuView);
			}
		}

		invalidateOptionsMenu();
	}

	@Override
	public void onBackPressed() {

		if(!General.onBackPressed()) return;

		if(!twoPane || isMenuShown) {
			super.onBackPressed();
			return;
		}

		isMenuShown = true;

		mainMenuFragment = new MainMenuFragment(this, null, false); // TODO preserve position
		mainMenuView = mainMenuFragment.getView();

		commentListingFragment = null;
		commentListingView = null;

		mLeftPane.removeAllViews();
		mRightPane.removeAllViews();

		mLeftPane.addView(mainMenuView);
		mRightPane.addView(postListingView);

		showBackButton(false);
		invalidateOptionsMenu();
	}

	public void onPostCommentsSelected(final RedditPreparedPost post) {

		if(twoPane) {

			commentListingController = new CommentListingController(PostCommentListingURL.forPostId(post.src.getIdAlone()), this);
			showBackButton(true);

			if(isMenuShown) {

				commentListingFragment = commentListingController.get(this, false, null);
				commentListingView = commentListingFragment.getView();

				mLeftPane.removeAllViews();
				mRightPane.removeAllViews();

				mLeftPane.addView(postListingView);
				mRightPane.addView(commentListingView);

				mainMenuFragment = null;
				mainMenuView = null;

				isMenuShown = false;

				invalidateOptionsMenu();

			} else {
				requestRefresh(RefreshableFragment.COMMENTS, false);
			}

		} else {
			LinkHandler.onLinkClicked(this, PostCommentListingURL.forPostId(post.src.getIdAlone()).toString(), false);
		}
	}

	public void onPostSelected(final RedditPreparedPost post) {
		if(post.isSelf()) {
			onPostCommentsSelected(post);
		} else {
			LinkHandler.onLinkClicked(this, post.src.getUrl(), false, post.src.getSrc());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		final boolean postsVisible = postListingFragment != null;
		final boolean commentsVisible = commentListingFragment != null;

		final boolean postsSortable = postListingController != null && postListingController.isSortable();
		final boolean commentsSortable = commentListingController != null && commentListingController.isSortable();

		final RedditAccount user = RedditAccountManager.getInstance(this).getDefaultAccount();
		final RedditSubredditSubscriptionManager.SubredditSubscriptionState subredditSubscriptionState;
		final RedditSubredditSubscriptionManager subredditSubscriptionManager
				= RedditSubredditSubscriptionManager.getSingleton(this, user);

		Boolean subredditPinState = null;
		Boolean subredditBlockedState = null;

		if(postsVisible
				&& !user.isAnonymous()
				&& postListingController.isSubreddit()
				&& subredditSubscriptionManager.areSubscriptionsReady()
				&& postListingFragment != null
				&& postListingFragment.getSubreddit() != null) {

			subredditSubscriptionState = subredditSubscriptionManager.getSubscriptionState(
					postListingController.subredditCanonicalName());

		} else {
			subredditSubscriptionState = null;
		}

		if(postsVisible
				&& postListingController.isSubreddit()
				&& postListingFragment != null
				&& postListingFragment.getSubreddit() != null) {

			try {
				subredditPinState = PrefsUtility.pref_pinned_subreddits_check(
						this,
						sharedPreferences,
						postListingFragment.getSubreddit().getCanonicalName());

				subredditBlockedState = PrefsUtility.pref_blocked_subreddits_check(
						this,
						sharedPreferences,
						postListingFragment.getSubreddit().getCanonicalName());

			} catch(RedditSubreddit.InvalidSubredditNameException e) {
				subredditPinState = null;
				subredditBlockedState = null;
			}
		}

		final String subredditDescription = postListingFragment != null && postListingFragment.getSubreddit() != null
				? postListingFragment.getSubreddit().description_html : null;

		OptionsMenuUtility.prepare(
				this,
				menu,
				isMenuShown,
				postsVisible,
				commentsVisible,
				false,
				false,
				false, postsSortable,
				commentsSortable,
				subredditSubscriptionState,
				postsVisible && subredditDescription != null && subredditDescription.length() > 0,
				subredditPinState,
				subredditBlockedState);

		if(commentListingFragment != null) {
			commentListingFragment.onCreateOptionsMenu(menu);
		}

		return true;
	}

	public void onRefreshComments() {
		commentListingController.setSession(null);
		requestRefresh(RefreshableFragment.COMMENTS, true);
	}

	public void onSortSelected(final PostCommentListingURL.Sort order) {
		commentListingController.setSort(order);
		requestRefresh(RefreshableFragment.COMMENTS, false);
	}

	public void onSortSelected(final UserCommentListingURL.Sort order) {
		commentListingController.setSort(order);
		requestRefresh(RefreshableFragment.COMMENTS, false);
	}

	@Override
	public void onSearchComments() {
		DialogUtils.showSearchDialog(this, R.string.action_search_comments, new DialogUtils.OnSearchListener() {
			@Override
			public void onSearch(@Nullable String query) {
				Intent searchIntent = new Intent(MainActivity.this, CommentListingActivity.class);
				searchIntent.setData(commentListingController.getUri());
				searchIntent.putExtra(CommentListingActivity.EXTRA_SEARCH_STRING, query);
				startActivity(searchIntent);
			}
		});
	}

	public void onRefreshPosts() {
		postListingController.setSession(null);
		requestRefresh(RefreshableFragment.POSTS, true);
	}

	public void onSubmitPost() {
		final Intent intent = new Intent(this, PostSubmitActivity.class);
		if(postListingController.isSubreddit()) {
			intent.putExtra("subreddit", postListingController.subredditCanonicalName());
		}
		startActivity(intent);
	}

	public void onSortSelected(final PostSort order) {
		postListingController.setSort(order);
		requestRefresh(RefreshableFragment.POSTS, false);
	}

	public void onSearchPosts() {
		PostListingActivity.onSearchPosts(postListingController, this);
	}

	@Override
	public void onSubscribe() {
		if(postListingFragment != null) postListingFragment.onSubscribe();
	}

	@Override
	public void onUnsubscribe() {
		if(postListingFragment != null) postListingFragment.onUnsubscribe();
	}

	@Override
	public void onSidebar() {
		final Intent intent = new Intent(this, HtmlViewActivity.class);
		intent.putExtra("html", postListingFragment.getSubreddit().getSidebarHtml(PrefsUtility.isNightMode(this)));
		intent.putExtra("title", String.format(
				Locale.US, "%s: %s",
				getString(R.string.sidebar_activity_title),
				postListingFragment.getSubreddit().url));
		startActivityForResult(intent, 1);
	}

	@Override
	public void onPin() {

		if(postListingFragment == null) return;

		try {
			PrefsUtility.pref_pinned_subreddits_add(
					this,
					sharedPreferences,
					postListingFragment.getSubreddit().getCanonicalName());

		} catch(RedditSubreddit.InvalidSubredditNameException e) {
			throw new RuntimeException(e);
		}

		invalidateOptionsMenu();
	}

	@Override
	public void onUnpin() {

		if(postListingFragment == null) return;

		try {
			PrefsUtility.pref_pinned_subreddits_remove(
					this,
					sharedPreferences,
					postListingFragment.getSubreddit().getCanonicalName());

		} catch(RedditSubreddit.InvalidSubredditNameException e) {
			throw new RuntimeException(e);
		}

		invalidateOptionsMenu();
	}

	@Override
	public void onBlock() {
		if(postListingFragment == null) return;

		try {
			PrefsUtility.pref_blocked_subreddits_add(
					this,
					sharedPreferences,
					postListingFragment.getSubreddit().getCanonicalName());

		} catch(RedditSubreddit.InvalidSubredditNameException e) {
			throw new RuntimeException(e);
		}

		invalidateOptionsMenu();
	}

	@Override
	public void onUnblock() {
		if(postListingFragment == null) return;

		try {
			PrefsUtility.pref_blocked_subreddits_remove(
					this,
					sharedPreferences,
					postListingFragment.getSubreddit().getCanonicalName());

		} catch(RedditSubreddit.InvalidSubredditNameException e) {
			throw new RuntimeException(e);
		}

		invalidateOptionsMenu();
	}

	public void onRefreshSubreddits() {
		requestRefresh(RefreshableFragment.MAIN, true);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if(commentListingFragment != null) {
			if(commentListingFragment.onOptionsItemSelected(item)) {
				return true;
			}
		}

		switch(item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void onSessionSelected(UUID session, SessionChangeType type) {

		switch(type) {
			case POSTS:
				postListingController.setSession(session);
				requestRefresh(RefreshableFragment.POSTS, false);
				break;
			case COMMENTS:
				commentListingController.setSession(session);
				requestRefresh(RefreshableFragment.COMMENTS, false);
				break;
		}
	}

	public void onSessionRefreshSelected(SessionChangeType type) {
		switch(type) {
			case POSTS:
				onRefreshPosts();
				break;
			case COMMENTS:
				onRefreshComments();
				break;
		}
	}

	public void onSessionChanged(UUID session, SessionChangeType type, long timestamp) {

		switch(type) {
			case POSTS:
				if(postListingController != null) postListingController.setSession(session);
				break;
			case COMMENTS:
				if(commentListingController != null) commentListingController.setSession(session);
				break;
		}
	}

	@Override
	public void onSubredditSubscriptionListUpdated(RedditSubredditSubscriptionManager subredditSubscriptionManager) {
		postInvalidateOptionsMenu();
	}

	@Override
	public void onSubredditSubscriptionAttempted(RedditSubredditSubscriptionManager subredditSubscriptionManager) {
		postInvalidateOptionsMenu();
	}

	@Override
	public void onSubredditUnsubscriptionAttempted(RedditSubredditSubscriptionManager subredditSubscriptionManager) {
		postInvalidateOptionsMenu();
	}

	private void postInvalidateOptionsMenu() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				invalidateOptionsMenu();
			}
		});
	}

	private void showBackButton(boolean isVisible) {
		configBackButton(isVisible, new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				onBackPressed();
			}
		});
	}
}
