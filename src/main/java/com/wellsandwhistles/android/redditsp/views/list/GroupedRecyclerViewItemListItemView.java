package com.wellsandwhistles.android.redditsp.views.list;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.wellsandwhistles.android.redditsp.adapters.GroupedRecyclerViewAdapter;

public class GroupedRecyclerViewItemListItemView extends GroupedRecyclerViewAdapter.Item {

	@Nullable private final Drawable mIcon;
	@NonNull private final CharSequence mText;
	private final boolean mHideDivider;

	@Nullable private final View.OnClickListener mClickListener;
	@Nullable private final View.OnLongClickListener mLongClickListener;

	public GroupedRecyclerViewItemListItemView(
			@Nullable final Drawable icon,
			@NonNull final CharSequence text,
			final boolean hideDivider,
			@Nullable final View.OnClickListener clickListener,
			@Nullable final View.OnLongClickListener longClickListener) {

		mIcon = icon;
		mText = text;
		mHideDivider = hideDivider;
		mClickListener = clickListener;
		mLongClickListener = longClickListener;
	}

	@Override
	public Class getViewType() {
		return ListItemView.class;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup) {
		return new RecyclerView.ViewHolder(new ListItemView(viewGroup.getContext())) {};
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder) {

		final ListItemView view = (ListItemView)viewHolder.itemView;

		view.reset(mIcon, mText, mHideDivider);
		view.setOnClickListener(mClickListener);
		view.setOnLongClickListener(mLongClickListener);

		if(mClickListener == null) {
			view.setClickable(false);
		}

		if(mLongClickListener == null) {
			view.setLongClickable(false);
		}
	}

	@Override
	public boolean isHidden() {
		return false;
	}
}
