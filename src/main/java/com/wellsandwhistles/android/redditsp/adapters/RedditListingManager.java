package com.wellsandwhistles.android.redditsp.adapters;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.views.LoadingSpinnerView;
import com.wellsandwhistles.android.redditsp.views.PostListingHeader;
import com.wellsandwhistles.android.redditsp.views.RedditPostHeaderView;
import com.wellsandwhistles.android.redditsp.views.liststatus.ErrorView;

import java.util.Collection;

public abstract class RedditListingManager {

	private final GroupedRecyclerViewAdapter mAdapter = new GroupedRecyclerViewAdapter(7);
	private LinearLayoutManager mLayoutManager;

	private static final int
			GROUP_HEADER = 0,
			GROUP_NOTIFICATIONS = 1,
			GROUP_POST_SELFTEXT = 2,
			GROUP_ITEMS = 3,
			GROUP_LOAD_MORE_BUTTON = 4,
			GROUP_LOADING = 5,
			GROUP_FOOTER_ERRORS = 6;

	private final GroupedRecyclerViewItemFrameLayout mLoadingItem;
	private boolean mWorkaroundDone = false;

	public RedditListingManager(final Context context) {
		General.checkThisIsUIThread();
		final LoadingSpinnerView loadingSpinnerView = new LoadingSpinnerView(context);
		final int paddingPx = General.dpToPixels(context, 30);
		loadingSpinnerView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

		mLoadingItem = new GroupedRecyclerViewItemFrameLayout(loadingSpinnerView);
		mAdapter.appendToGroup(GROUP_LOADING, mLoadingItem);
	}

	public void setLayoutManager(final LinearLayoutManager layoutManager) {
		General.checkThisIsUIThread();
		mLayoutManager = layoutManager;
	}

	// Workaround for RecyclerView scrolling behaviour
	private void doWorkaround() {
		if(!mWorkaroundDone && mLayoutManager != null) {
			mLayoutManager.scrollToPositionWithOffset(0, 0);
			mWorkaroundDone = true;
		}
	}

	public void addFooterError(final ErrorView view) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_FOOTER_ERRORS, new GroupedRecyclerViewItemFrameLayout(view));
	}

	public void addPostHeader(final RedditPostHeaderView view) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_HEADER, new GroupedRecyclerViewItemFrameLayout(view));
		doWorkaround();
	}

	public void addPostListingHeader(final PostListingHeader view) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_HEADER, new GroupedRecyclerViewItemFrameLayout(view));
		doWorkaround();
	}

	public void addPostSelfText(final View view) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_POST_SELFTEXT, new GroupedRecyclerViewItemFrameLayout(view));
		doWorkaround();
	}

	public void addNotification(final View view) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_NOTIFICATIONS, new GroupedRecyclerViewItemFrameLayout(view));
		doWorkaround();
	}

	public void addItems(final Collection<GroupedRecyclerViewAdapter.Item> items) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_ITEMS, items);
		doWorkaround();
	}

	public void addViewToItems(final View view) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_ITEMS, new GroupedRecyclerViewItemFrameLayout(view));
		doWorkaround();
	}

	public void addLoadMoreButton(final View view) {
		General.checkThisIsUIThread();
		mAdapter.appendToGroup(GROUP_LOAD_MORE_BUTTON, new GroupedRecyclerViewItemFrameLayout(view));
		doWorkaround();
	}

	public void removeLoadMoreButton() {
		General.checkThisIsUIThread();
		mAdapter.removeAllFromGroup(GROUP_LOAD_MORE_BUTTON);
	}

	public void setLoadingVisible(final boolean visible) {
		General.checkThisIsUIThread();
		mLoadingItem.setHidden(!visible);
		mAdapter.updateHiddenStatus();
	}

	public GroupedRecyclerViewAdapter getAdapter() {
		General.checkThisIsUIThread();
		return mAdapter;
	}

	public void updateHiddenStatus() {
		General.checkThisIsUIThread();
		mAdapter.updateHiddenStatus();
	}

	public GroupedRecyclerViewAdapter.Item getItemAtPosition(final int position) {
		return mAdapter.getItemAtPosition(position);
	}
}
