package com.wellsandwhistles.android.redditsp.reddit.url;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public abstract class CommentListingURL extends RedditURLParser.RedditURL {

	public abstract CommentListingURL after(String after);
	public abstract CommentListingURL limit(Integer limit);
}
