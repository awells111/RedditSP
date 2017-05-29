package com.wellsandwhistles.android.redditsp.reddit;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.wellsandwhistles.android.redditsp.adapters.GroupedRecyclerViewAdapter;
import com.wellsandwhistles.android.redditsp.fragments.PostListingFragment;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
import com.wellsandwhistles.android.redditsp.reddit.url.RedditURLParser;
import com.wellsandwhistles.android.redditsp.views.RedditPostView;

public class RedditPostListItem extends GroupedRecyclerViewAdapter.Item {

	private final PostListingFragment mFragment;
	private final AppCompatActivity mActivity;

	private final RedditPreparedPost mPost;

	public RedditPostListItem(
			final RedditPreparedPost post,
			final PostListingFragment fragment,
			final AppCompatActivity activity,
			final RedditURLParser.RedditURL postListingUrl) {

		mFragment = fragment;
		mActivity = activity;
		mPost = post;
	}

	@Override
	public Class getViewType() {
		return RedditPostView.class;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup) {

		final RedditPostView view = new RedditPostView(
				mActivity,
				mFragment,
				mActivity);

		return new RecyclerView.ViewHolder(view) {};
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder) {
		((RedditPostView)viewHolder.itemView).reset(mPost);
	}

	@Override
	public boolean isHidden() {
		return false;
	}

}
