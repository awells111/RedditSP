package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.wellsandwhistles.android.redditsp.R;

public class ScrollbarRecyclerViewManager {

	private final View mOuter;
	private final SwipeRefreshLayout mSwipeRefreshLayout;
	private final RecyclerView mRecyclerView;
	private final FrameLayout mScrollbarFrame;
	private final View mScrollbar;

	private boolean mScrollUnnecessary = false;

	public ScrollbarRecyclerViewManager(
			final Context context,
			final ViewGroup root,
			final boolean attachToRoot) {

		mOuter = LayoutInflater.from(context).inflate(R.layout.scrollbar_recyclerview, root, attachToRoot);
		mSwipeRefreshLayout = (SwipeRefreshLayout) mOuter.findViewById(R.id.scrollbar_recyclerview_refreshlayout);
		mRecyclerView = (RecyclerView) mOuter.findViewById(R.id.scrollbar_recyclerview_recyclerview);
		mScrollbar = mOuter.findViewById(R.id.scrollbar_recyclerview_scrollbar);
		mScrollbarFrame = (FrameLayout) mOuter.findViewById(R.id.scrollbar_recyclerview_scrollbarframe);

		mSwipeRefreshLayout.setEnabled(false);

		final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
		mRecyclerView.setLayoutManager(linearLayoutManager);
		mRecyclerView.setHasFixedSize(true);
		linearLayoutManager.setSmoothScrollbarEnabled(false);

		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

			private void updateScroll() {

				final int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
				final int lastVisible = linearLayoutManager.findLastVisibleItemPosition();
				final int itemsVisible = lastVisible - firstVisible + 1;
				final int totalCount = linearLayoutManager.getItemCount();

				final boolean scrollUnnecessary = (itemsVisible == totalCount);

				if(scrollUnnecessary != mScrollUnnecessary) {
					mScrollbar.setVisibility(scrollUnnecessary ? View.INVISIBLE : View.VISIBLE);
				}

				mScrollUnnecessary = scrollUnnecessary;

				if(!scrollUnnecessary) {
					final int recyclerViewHeight = mRecyclerView.getMeasuredHeight();
					final int scrollBarHeight = mScrollbar.getMeasuredHeight();

					final double topPadding = ((double) firstVisible / (double) (totalCount - itemsVisible)) * (recyclerViewHeight - scrollBarHeight);

					mScrollbarFrame.setPadding(0, (int) Math.round(topPadding), 0, 0);
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				updateScroll();
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

				switch(newState) {
					case RecyclerView.SCROLL_STATE_IDLE:
						hideScrollbar();
						break;
					case RecyclerView.SCROLL_STATE_DRAGGING:
						showScrollbar();
						break;
					case RecyclerView.SCROLL_STATE_SETTLING:
						break;
				}

				updateScroll();
			}
		});
	}

	public void enablePullToRefresh(@NonNull final SwipeRefreshLayout.OnRefreshListener listener) {
		mSwipeRefreshLayout.setOnRefreshListener(listener);
		mSwipeRefreshLayout.setEnabled(true);
	}

	private void showScrollbar() {
		mScrollbar.animate().cancel();
		mScrollbar.setAlpha(1f);
	}

	private void hideScrollbar() {
		mScrollbar.animate().alpha(0).setStartDelay(500).setDuration(500).start();
	}

	public View getOuterView() {
		return mOuter;
	}

	public RecyclerView getRecyclerView() {
		return mRecyclerView;
	}
}
