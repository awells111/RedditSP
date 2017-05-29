package com.wellsandwhistles.android.redditsp.fragments;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.activities.BugReportActivity;
import com.wellsandwhistles.android.redditsp.activities.CommentReplyActivity;
import com.wellsandwhistles.android.redditsp.activities.OptionsMenuUtility;
import com.wellsandwhistles.android.redditsp.adapters.FilteredCommentListingManager;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategy;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyAlways;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyIfNotCached;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.common.SRThemeAttributes;
import com.wellsandwhistles.android.redditsp.common.SRTime;
import com.wellsandwhistles.android.redditsp.reddit.CommentListingRequest;
import com.wellsandwhistles.android.redditsp.reddit.RedditCommentListItem;
import com.wellsandwhistles.android.redditsp.reddit.api.RedditAPICommentAction;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditChangeDataManager;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditRenderableComment;
import com.wellsandwhistles.android.redditsp.reddit.url.RedditURLParser;
import com.wellsandwhistles.android.redditsp.views.RedditCommentView;
import com.wellsandwhistles.android.redditsp.views.RedditPostHeaderView;
import com.wellsandwhistles.android.redditsp.views.RedditPostView;
import com.wellsandwhistles.android.redditsp.views.ScrollbarRecyclerViewManager;
import com.wellsandwhistles.android.redditsp.views.liststatus.CommentSubThreadView;
import com.wellsandwhistles.android.redditsp.views.liststatus.ErrorView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

public class CommentListingFragment extends SRFragment
		implements RedditPostView.PostSelectionListener,
		RedditCommentView.CommentListener,
		CommentListingRequest.Listener {

	private static final String SAVEDSTATE_FIRST_VISIBLE_POS = "firstVisiblePosition";

	private final RedditAccount mUser;
	private final ArrayList<RedditURLParser.RedditURL> mAllUrls;
	private final LinkedList<RedditURLParser.RedditURL> mUrlsToDownload;
	private final UUID mSession;
	private final DownloadStrategy mDownloadStrategy;

	private RedditPreparedPost mPost = null;
	private boolean isArchived;

	private final FilteredCommentListingManager mCommentListingManager;

	private final RecyclerView mRecyclerView;

	private final FrameLayout mOuterFrame;

	private final float mCommentFontScale;
	private final boolean mShowLinkButtons;

	private Long mCachedTimestamp = null;

	private Integer mPreviousFirstVisibleItemPosition;

	public CommentListingFragment(
			final AppCompatActivity parent,
			final Bundle savedInstanceState,
			final ArrayList<RedditURLParser.RedditURL> urls,
			final UUID session,
			final String searchString,
			final boolean forceDownload) {

		super(parent, savedInstanceState);

		if (savedInstanceState != null) {
			mPreviousFirstVisibleItemPosition = savedInstanceState.getInt(SAVEDSTATE_FIRST_VISIBLE_POS);
		}

		mCommentListingManager = new FilteredCommentListingManager(parent, searchString);
		mAllUrls = urls;

		mUrlsToDownload = new LinkedList<>(mAllUrls);

		this.mSession = session;

		if (forceDownload) {
			mDownloadStrategy = DownloadStrategyAlways.INSTANCE;

		} else {
			mDownloadStrategy = DownloadStrategyIfNotCached.INSTANCE;
		}

		mUser = RedditAccountManager.getInstance(getActivity()).getDefaultAccount();

		parent.invalidateOptionsMenu();

		final Context context = getActivity();

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mCommentFontScale = PrefsUtility.appearance_fontscale_comments(context, prefs);
		mShowLinkButtons = PrefsUtility.pref_appearance_linkbuttons(context, prefs);

		mOuterFrame = new FrameLayout(context);

		final ScrollbarRecyclerViewManager recyclerViewManager
				= new ScrollbarRecyclerViewManager(context, null, false);

		if (parent instanceof OptionsMenuUtility.OptionsMenuCommentsListener
				&& PrefsUtility.pref_behaviour_enable_swipe_refresh(context, prefs)) {

			recyclerViewManager.enablePullToRefresh(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					((OptionsMenuUtility.OptionsMenuCommentsListener) parent).onRefreshComments();
				}
			});
		}

		mRecyclerView = recyclerViewManager.getRecyclerView();
		mCommentListingManager.setLayoutManager((LinearLayoutManager) mRecyclerView.getLayoutManager());

		mRecyclerView.setAdapter(mCommentListingManager.getAdapter());
		mOuterFrame.addView(recyclerViewManager.getOuterView());

		mRecyclerView.setItemAnimator(null);

		/* TODO
		{
			final RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
			itemAnimator.setRemoveDuration(80);
			itemAnimator.setChangeDuration(80);
			itemAnimator.setAddDuration(80);
			itemAnimator.setMoveDuration(80);
		}
		*/

		makeNextRequest(context);
	}

	public void handleCommentVisibilityToggle(final RedditCommentView view) {

		final RedditChangeDataManager changeDataManager = RedditChangeDataManager.getInstance(mUser);
		final RedditCommentListItem item = view.getComment();

		if (item.isComment()) {

			final RedditRenderableComment comment = item.asComment();

			changeDataManager.markHidden(
					SRTime.utcCurrentTimeMillis(),
					comment,
					!comment.isCollapsed(changeDataManager));

			mCommentListingManager.updateHiddenStatus();

			final LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
			final int position = layoutManager.getPosition(view);

			if (position == layoutManager.findFirstVisibleItemPosition()) {
				layoutManager.scrollToPositionWithOffset(position, 0);
			}
		}
	}

	@Override
	public View getView() {
		return mOuterFrame;
	}

	@Override
	public Bundle onSaveInstanceState() {

		final Bundle bundle = new Bundle();

		final LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
		bundle.putInt(SAVEDSTATE_FIRST_VISIBLE_POS, layoutManager.findFirstVisibleItemPosition());

		return bundle;
	}

	@SuppressLint("WrongConstant")
	private void makeNextRequest(final Context context) {

		if (!mUrlsToDownload.isEmpty()) {
			new CommentListingRequest(
					context,
					this,
					getActivity(),
					mUrlsToDownload.getFirst(),
					mAllUrls.size() == 1,
					mUrlsToDownload.getFirst(),
					mUser,
					mSession,
					mDownloadStrategy,
					this
			);
		}
	}

	@Override
	public void onCommentClicked(final RedditCommentView view) {
		switch (PrefsUtility.pref_behaviour_actions_comment_tap(
				getActivity(),
				PreferenceManager.getDefaultSharedPreferences(getActivity()))) {

			case COLLAPSE:
				handleCommentVisibilityToggle(view);
				break;

			case ACTION_MENU: {
				final RedditCommentListItem item = view.getComment();
				if (item != null && item.isComment()) {
					RedditAPICommentAction.showActionMenu(
							getActivity(),
							this,
							item.asComment(),
							view,
							RedditChangeDataManager.getInstance(mUser),
							isArchived);
				}
				break;
			}
		}
	}

	@Override
	public void onCommentLongClicked(final RedditCommentView view) {
		switch (PrefsUtility.pref_behaviour_actions_comment_longclick(
				getActivity(),
				PreferenceManager.getDefaultSharedPreferences(getActivity()))) {

			case ACTION_MENU: {
				final RedditCommentListItem item = view.getComment();
				if (item != null && item.isComment()) {
					RedditAPICommentAction.showActionMenu(
							getActivity(),
							this,
							item.asComment(),
							view,
							RedditChangeDataManager.getInstance(mUser),
							isArchived);
				}
				break;
			}

			case COLLAPSE:
				handleCommentVisibilityToggle(view);
				break;

			case NOTHING:
				break;
		}
	}

	@Override
	public void onCommentListingRequestDownloadNecessary() {
		mCommentListingManager.setLoadingVisible(true);
	}

	@Override
	public void onCommentListingRequestDownloadStarted() {
	}

	@Override
	public void onCommentListingRequestException(final Throwable t) {
		BugReportActivity.handleGlobalError(getActivity(), t);
	}

	@Override
	public void onCommentListingRequestFailure(final SRError error) {
		mCommentListingManager.setLoadingVisible(false);
		mCommentListingManager.addFooterError(new ErrorView(getActivity(), error));
	}

	@Override
	public void onCommentListingRequestCachedCopy(final long timestamp) {
		mCachedTimestamp = timestamp;
	}

	@Override
	public void onCommentListingRequestParseStart() {
		mCommentListingManager.setLoadingVisible(true);
	}


	@Override
	public void onCommentListingRequestAuthorizing() {
		mCommentListingManager.setLoadingVisible(true);
	}

	@Override
	public void onCommentListingRequestPostDownloaded(final RedditPreparedPost post) {

		final Context context = getActivity();

		if (mPost == null) {

			final SRThemeAttributes attr = new SRThemeAttributes(context);

			mPost = post;
			isArchived = post.isArchived;

			final RedditPostHeaderView postHeader = new RedditPostHeaderView(
					getActivity(),
					this.mPost);

			mCommentListingManager.addPostHeader(postHeader);
			((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, 0);

			if (post.src.getSelfText() != null) {
				final ViewGroup selfText = post.src.getSelfText().buildView(
						getActivity(), attr.srMainTextCol, 14f * mCommentFontScale, mShowLinkButtons);
				selfText.setFocusable(false);
				selfText.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

				final int paddingPx = General.dpToPixels(context, 10);
				final FrameLayout paddingLayout = new FrameLayout(context);
				final TextView collapsedView = new TextView(context);
				collapsedView.setText("[ + ]  " + getActivity().getString(R.string.collapsed_self_post));
				collapsedView.setVisibility(View.GONE);
				collapsedView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
				paddingLayout.addView(selfText);
				paddingLayout.addView(collapsedView);
				paddingLayout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

				paddingLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (selfText.getVisibility() == View.GONE) {
							selfText.setVisibility(View.VISIBLE);
							collapsedView.setVisibility(View.GONE);
						} else {
							selfText.setVisibility(View.GONE);
							collapsedView.setVisibility(View.VISIBLE);
						}

					}
				});
				// TODO mListHeaderNotifications.setBackgroundColor(Color.argb(35, 128, 128, 128));

				mCommentListingManager.addPostSelfText(paddingLayout);
			}

			if (!General.isTablet(context, PreferenceManager.getDefaultSharedPreferences(context))) {
				getActivity().setTitle(post.src.getTitle());
			}

			if (mCommentListingManager.isSearchListing()) {
				final CommentSubThreadView searchCommentThreadView = new CommentSubThreadView(
						getActivity(),
						mAllUrls.get(0).asPostCommentListURL(),
						R.string.comment_header_search_thread_title
				);

				mCommentListingManager.addNotification(searchCommentThreadView);
			} else if (!mAllUrls.isEmpty()
					&& mAllUrls.get(0).pathType() == RedditURLParser.POST_COMMENT_LISTING_URL
					&& mAllUrls.get(0).asPostCommentListURL().commentId != null) {

				final CommentSubThreadView specificCommentThreadView = new CommentSubThreadView(
						getActivity(),
						mAllUrls.get(0).asPostCommentListURL(),
						R.string.comment_header_specific_thread_title);

				mCommentListingManager.addNotification(specificCommentThreadView);
			}

			// TODO pref (currently 10 mins)
			if (mCachedTimestamp != null && SRTime.since(mCachedTimestamp) > 10 * 60 * 1000) {

				final TextView cacheNotif = (TextView) LayoutInflater.from(getActivity())
						.inflate(R.layout.cached_header, null, false);
				cacheNotif.setText(getActivity().getString(R.string.listing_cached,
						SRTime.formatDateTime(mCachedTimestamp, getActivity())));
				mCommentListingManager.addNotification(cacheNotif);
			}
		}
	}

	@Override
	public void onCommentListingRequestAllItemsDownloaded(final ArrayList<RedditCommentListItem> items) {

		mCommentListingManager.addComments(items);

		mUrlsToDownload.removeFirst();

		final LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

		if (mPreviousFirstVisibleItemPosition != null
				&& layoutManager.getItemCount() > mPreviousFirstVisibleItemPosition) {

			layoutManager.scrollToPositionWithOffset(
					mPreviousFirstVisibleItemPosition,
					0);

			mPreviousFirstVisibleItemPosition = null;
		}

		if (mUrlsToDownload.isEmpty()) {

			if (mCommentListingManager.getCommentCount() == 0) {

				final View emptyView = LayoutInflater.from(getContext()).inflate(
						R.layout.no_comments_yet,
						mRecyclerView,
						false);

				if (mCommentListingManager.isSearchListing()) {
					((TextView) emptyView.findViewById(R.id.empty_view_text)).setText(R.string.no_search_results);
				}

				mCommentListingManager.addViewToItems(emptyView);

			} else {

				final View blankView = new View(getContext());
				blankView.setMinimumWidth(1);
				blankView.setMinimumHeight(General.dpToPixels(getContext(), 96));
				mCommentListingManager.addViewToItems(blankView);
			}

			mCommentListingManager.setLoadingVisible(false);

		} else {
			makeNextRequest(getActivity());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		if (mAllUrls != null && mAllUrls.size() > 0 && mAllUrls.get(0).pathType() == RedditURLParser.POST_COMMENT_LISTING_URL) {
			menu.add(R.string.action_reply);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getTitle() != null
				&& item.getTitle().equals(getActivity().getString(R.string.action_reply))) {

			onParentReply();
			return true;
		}

		return false;
	}

	private void onParentReply() {

		if (mPost != null) {
			final Intent intent = new Intent(getActivity(), CommentReplyActivity.class);
			intent.putExtra(CommentReplyActivity.PARENT_ID_AND_TYPE_KEY, mPost.src.getIdAndType());
			intent.putExtra(CommentReplyActivity.PARENT_MARKDOWN_KEY, mPost.src.getUnescapedSelfText());
			startActivity(intent);

		} else {
			General.quickToast(getActivity(), R.string.error_toast_parent_post_not_downloaded);
		}
	}

	public void onPostSelected(final RedditPreparedPost post) {
		((RedditPostView.PostSelectionListener) getActivity()).onPostSelected(post);
	}

	public void onPostCommentsSelected(final RedditPreparedPost post) {
		((RedditPostView.PostSelectionListener) getActivity()).onPostCommentsSelected(post);
	}
}
