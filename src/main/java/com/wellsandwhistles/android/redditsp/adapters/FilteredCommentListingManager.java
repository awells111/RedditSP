package com.wellsandwhistles.android.redditsp.adapters;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.support.annotation.Nullable;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.reddit.RedditCommentListItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FilteredCommentListingManager extends RedditListingManager {

	@Nullable
	private final String mSearchString;

	private int mCommentCount;

	public FilteredCommentListingManager(
			final Context context,
			@Nullable final String searchString) {

		super(context);
		mSearchString = searchString;
	}

	public void addComments(final Collection<RedditCommentListItem> comments) {
		final Collection<GroupedRecyclerViewAdapter.Item> filteredComments = filter(comments);
		addItems(filteredComments);
		mCommentCount += filteredComments.size();
	}

	private Collection<GroupedRecyclerViewAdapter.Item> filter(Collection<RedditCommentListItem> comments) {

		final Collection<RedditCommentListItem> searchComments;

		if (mSearchString == null) {
			searchComments = comments;

		} else {
		 	searchComments = new ArrayList<>();
			for (RedditCommentListItem comment : comments) {
				if (!comment.isComment()) continue;
				String commentStr = comment.asComment().getParsedComment().getRawComment().body;
				if (commentStr != null) {
					commentStr = General.asciiLowercase(commentStr);
					if (commentStr.contains(mSearchString)) {
						searchComments.add(comment);
					}
				}
			}
		}

		return Collections.<GroupedRecyclerViewAdapter.Item>unmodifiableCollection(searchComments);
	}

	public boolean isSearchListing() {
		return mSearchString != null;
	}

	public int getCommentCount() {
		return mCommentCount;
	}
}
