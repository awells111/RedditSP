package com.wellsandwhistles.android.redditsp.reddit.prepared;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.v7.app.AppCompatActivity;

//todo consolidate this with RedditRenderableCommentListItem
public interface RedditRenderableInboxItem extends RedditRenderableCommentListItem {
	void handleInboxClick(AppCompatActivity activity);

	void handleInboxLongClick(AppCompatActivity activity);
}
