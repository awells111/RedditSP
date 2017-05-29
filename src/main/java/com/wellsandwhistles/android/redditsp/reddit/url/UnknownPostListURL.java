package com.wellsandwhistles.android.redditsp.reddit.url;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.net.Uri;

public class UnknownPostListURL extends PostListingURL {

	private final Uri uri;

	UnknownPostListURL(Uri uri) {
		this.uri = uri;
	}

	@Override
	public PostListingURL after(String after) {
		return new UnknownPostListURL(uri.buildUpon().appendQueryParameter("after", after).build());
	}

	@Override
	public PostListingURL limit(Integer limit) {
		return new UnknownPostListURL(uri.buildUpon().appendQueryParameter("limit", String.valueOf("limit")).build());
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
		return RedditURLParser.UNKNOWN_POST_LISTING_URL;
	}
}
