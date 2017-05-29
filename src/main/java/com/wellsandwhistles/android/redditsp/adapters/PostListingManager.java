package com.wellsandwhistles.android.redditsp.adapters;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import com.wellsandwhistles.android.redditsp.reddit.RedditPostListItem;

import java.util.Collection;
import java.util.Collections;

public class PostListingManager extends RedditListingManager {

	private int mPostCount;

	public PostListingManager(final Context context) {
		super(context);
	}

	public void addPosts(final Collection<RedditPostListItem> posts) {
		addItems(Collections.<GroupedRecyclerViewAdapter.Item>unmodifiableCollection(posts));
		mPostCount += posts.size();
	}

	public int getPostCount() {
		return mPostCount;
	}
}
