package com.wellsandwhistles.android.redditsp.reddit.url;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.reddit.PostSort;

public abstract class PostListingURL extends RedditURLParser.RedditURL {

	public abstract PostListingURL after(String after);
	public abstract PostListingURL limit(Integer limit);

	public PostSort getOrder() {
		return null;
	}
}
