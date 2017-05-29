package com.wellsandwhistles.android.redditsp.reddit.url;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.net.Uri;

public class UnknownCommentListURL extends CommentListingURL {

	private final Uri uri;

	UnknownCommentListURL(Uri uri) {
		this.uri = uri;
	}

	@Override
	public CommentListingURL after(String after) {
		return new UnknownCommentListURL(uri.buildUpon().appendQueryParameter("after", after).build());
	}

	@Override
	public CommentListingURL limit(Integer limit) {
		return new UnknownCommentListURL(uri.buildUpon().appendQueryParameter("limit", String.valueOf("limit")).build());
	}

	// TODO handle this better
	@Override
	public Uri generateJsonUri() {
		if(uri.getPath().endsWith(".json")) {
			return uri;
		} else {
			return uri.buildUpon().appendEncodedPath(".json").build();
		}
	}

	@Override
	public @RedditURLParser.PathType int pathType() {
		return RedditURLParser.UNKNOWN_COMMENT_LISTING_URL;
	}
}
