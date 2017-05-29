package com.wellsandwhistles.android.redditsp.views.list;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.adapters.GroupedRecyclerViewAdapter;

public class GroupedRecyclerViewItemListSectionHeaderView extends GroupedRecyclerViewAdapter.Item {

	@NonNull private final CharSequence mText;

	public GroupedRecyclerViewItemListSectionHeaderView(
			@NonNull final CharSequence text) {

		mText = text;
	}

	@Override
	public Class getViewType() {
		// There's no wrapper class for this view, so just use the item class
		return GroupedRecyclerViewItemListSectionHeaderView.class;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup) {
		return new RecyclerView.ViewHolder(
				LayoutInflater.from(viewGroup.getContext()).inflate(
						R.layout.list_sectionheader,
						viewGroup,
						false)) {};
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder) {

		final TextView view = (TextView)viewHolder.itemView;
		view.setText(mText);
	}

	@Override
	public boolean isHidden() {
		return false;
	}
}
