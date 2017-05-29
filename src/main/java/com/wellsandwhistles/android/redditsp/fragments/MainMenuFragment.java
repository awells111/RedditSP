package com.wellsandwhistles.android.redditsp.fragments;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.adapters.MainMenuListingManager;
import com.wellsandwhistles.android.redditsp.adapters.MainMenuSelectionListener;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.common.TimestampBound;
import com.wellsandwhistles.android.redditsp.io.RequestResponseHandler;
import com.wellsandwhistles.android.redditsp.reddit.api.RedditMultiredditSubscriptionManager;
import com.wellsandwhistles.android.redditsp.reddit.api.RedditSubredditSubscriptionManager;
import com.wellsandwhistles.android.redditsp.reddit.api.SubredditRequestFailure;
import com.wellsandwhistles.android.redditsp.reddit.url.PostListingURL;
import com.wellsandwhistles.android.redditsp.views.ScrollbarRecyclerViewManager;
import com.wellsandwhistles.android.redditsp.views.liststatus.ErrorView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.HashSet;

public class MainMenuFragment extends SRFragment
		implements MainMenuSelectionListener,
		RedditSubredditSubscriptionManager.SubredditSubscriptionStateChangeListener,
		RedditMultiredditSubscriptionManager.MultiredditListChangeListener {

	public static final int MENU_MENU_ACTION_FRONTPAGE = 0;
	public static final int MENU_MENU_ACTION_PROFILE = 1;
	public static final int MENU_MENU_ACTION_INBOX = 2;
	public static final int MENU_MENU_ACTION_SUBMITTED = 3;
	public static final int MENU_MENU_ACTION_SAVED = 4;
	public static final int MENU_MENU_ACTION_CUSTOM = 5;
	public static final int MENU_MENU_ACTION_ALL = 6;

	@IntDef({MENU_MENU_ACTION_FRONTPAGE, MENU_MENU_ACTION_PROFILE, MENU_MENU_ACTION_INBOX,
		MENU_MENU_ACTION_SUBMITTED, MENU_MENU_ACTION_SAVED, MENU_MENU_ACTION_CUSTOM, MENU_MENU_ACTION_ALL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface MainMenuAction {}

	private final MainMenuListingManager mManager;

	private final View mOuter;

	public MainMenuFragment(
			final AppCompatActivity parent,
			final Bundle savedInstanceState,
			final boolean force) {

		super(parent, savedInstanceState);
		final Context context = getActivity();

		final RedditAccount user = RedditAccountManager.getInstance(context).getDefaultAccount();

		ScrollbarRecyclerViewManager recyclerViewManager = new ScrollbarRecyclerViewManager(parent, null, false);

		mOuter = recyclerViewManager.getOuterView();
		final RecyclerView recyclerView = recyclerViewManager.getRecyclerView();

		mManager = new MainMenuListingManager(getActivity(), this, user);

		recyclerView.setAdapter(mManager.getAdapter());

		final int paddingPx = General.dpToPixels(context, 8);
		recyclerView.setPadding(paddingPx, 0, paddingPx, 0);
		recyclerView.setClipToPadding(false);

		{
			final TypedArray appearance = context.obtainStyledAttributes(new int[]{
					R.attr.srListItemBackgroundCol});

			getActivity().getWindow().setBackgroundDrawable(
					new ColorDrawable(appearance.getColor(0, General.COLOR_INVALID)));

			appearance.recycle();
		}

		final RedditMultiredditSubscriptionManager multiredditSubscriptionManager
				= RedditMultiredditSubscriptionManager.getSingleton(context, user);

		final RedditSubredditSubscriptionManager subredditSubscriptionManager
				= RedditSubredditSubscriptionManager.getSingleton(context, user);

		if(force) {
			multiredditSubscriptionManager.triggerUpdate(new RequestResponseHandler<HashSet<String>, SubredditRequestFailure>() {
				@Override
				public void onRequestFailed(SubredditRequestFailure failureReason) {
					onMultiredditError(failureReason.asError(context));
				}

				@Override
				public void onRequestSuccess(HashSet<String> result, long timeCached) {
					multiredditSubscriptionManager.addListener(MainMenuFragment.this);
					onMultiredditSubscriptionsChanged(result);
				}
			}, TimestampBound.NONE);

			subredditSubscriptionManager.triggerUpdate(new RequestResponseHandler<HashSet<String>, SubredditRequestFailure>() {
				@Override
				public void onRequestFailed(SubredditRequestFailure failureReason) {
					onSubredditError(failureReason.asError(context));
				}

				@Override
				public void onRequestSuccess(HashSet<String> result, long timeCached) {
					subredditSubscriptionManager.addListener(MainMenuFragment.this);
					onSubredditSubscriptionsChanged(result);
				}
			}, TimestampBound.NONE);

		} else {

			multiredditSubscriptionManager.addListener(this);
			subredditSubscriptionManager.addListener(this);

			if(multiredditSubscriptionManager.areSubscriptionsReady()) {
				onMultiredditSubscriptionsChanged(multiredditSubscriptionManager.getSubscriptionList());
			}

			if(subredditSubscriptionManager.areSubscriptionsReady()) {
				onSubredditSubscriptionsChanged(subredditSubscriptionManager.getSubscriptionList());
			}

			final TimestampBound.MoreRecentThanBound oneHour = TimestampBound.notOlderThan(1000 * 60 * 60);
			multiredditSubscriptionManager.triggerUpdate(null, oneHour);
			subredditSubscriptionManager.triggerUpdate(null, oneHour);
		}
	}

	public enum MainMenuUserItems {
		PROFILE, INBOX, SUBMITTED, SAVED
	}

	@Override
	public View getView() {
		return mOuter;
	}

	@Override
	public Bundle onSaveInstanceState() {
		return null;
	}

	public void onSubredditSubscriptionsChanged(final Collection<String> subscriptions) {
		mManager.setSubreddits(subscriptions);
	}

	public void onMultiredditSubscriptionsChanged(final Collection<String> subscriptions) {
		mManager.setMultireddits(subscriptions);
	}

	private void onSubredditError(final SRError error) {
		mManager.setSubredditsError(new ErrorView(getActivity(), error));
	}

	private void onMultiredditError(final SRError error) {
		mManager.setMultiredditsError(new ErrorView(getActivity(), error));
	}

	@Override
	public void onSelected(final @MainMenuAction int type) {
		((MainMenuSelectionListener)getActivity()).onSelected(type);
	}

	@Override
	public void onSelected(final PostListingURL postListingURL) {
		((MainMenuSelectionListener)getActivity()).onSelected(postListingURL);
	}

	@Override
	public void onSubredditSubscriptionListUpdated(RedditSubredditSubscriptionManager subredditSubscriptionManager) {
		onSubredditSubscriptionsChanged(subredditSubscriptionManager.getSubscriptionList());
	}

	@Override
	public void onMultiredditListUpdated(final RedditMultiredditSubscriptionManager multiredditSubscriptionManager) {
		onMultiredditSubscriptionsChanged(multiredditSubscriptionManager.getSubscriptionList());
	}

	@Override
	public void onSubredditSubscriptionAttempted(RedditSubredditSubscriptionManager subredditSubscriptionManager) {
	}

	@Override
	public void onSubredditUnsubscriptionAttempted(RedditSubredditSubscriptionManager subredditSubscriptionManager) {
	}
}
