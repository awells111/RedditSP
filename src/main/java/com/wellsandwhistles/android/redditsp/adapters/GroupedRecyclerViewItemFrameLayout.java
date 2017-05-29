package com.wellsandwhistles.android.redditsp.adapters;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

final class GroupedRecyclerViewItemFrameLayout extends GroupedRecyclerViewAdapter.Item {

	private final View mChildView;
	private boolean mHidden;

	private FrameLayout mParent;

	GroupedRecyclerViewItemFrameLayout(final View childView) {
		mChildView = childView;
	}

	@Override
	public Class getViewType() {
		return this.getClass();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup) {
		viewGroup.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

		final FrameLayout frameLayout = new FrameLayout(viewGroup.getContext());
		viewGroup.addView(frameLayout);
		frameLayout.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

		return new RecyclerView.ViewHolder(frameLayout) {};
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder) {

		final FrameLayout view = (FrameLayout)viewHolder.itemView;
		view.removeAllViews();

		if(mParent != null && mChildView.getParent() == mParent) {
			mParent.removeAllViews();
		}

		mParent = view;

		view.addView(mChildView);
		mChildView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
	}

	@Override
	public boolean isHidden() {
		return mHidden;
	}

	public void setHidden(final boolean hidden) {
		mHidden = hidden;
	}
}
