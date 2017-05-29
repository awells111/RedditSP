package com.wellsandwhistles.android.redditsp.reddit.things;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedArray;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;
import com.wellsandwhistles.android.redditsp.reddit.url.PostCommentListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.RedditURLParser;

import java.util.ArrayList;
import java.util.List;

public class RedditMoreComments {
	public int count;
	public JsonBufferedArray children;
	public String parent_id;

	public List<PostCommentListingURL> getMoreUrls(final RedditURLParser.RedditURL commentListingURL) {

		final ArrayList<PostCommentListingURL> urls = new ArrayList<>(16);

		if(commentListingURL.pathType() == RedditURLParser.POST_COMMENT_LISTING_URL) {

			if(count > 0) {
				for(JsonValue child : children) {
					if(child.getType() == JsonValue.TYPE_STRING) {
						urls.add(commentListingURL.asPostCommentListURL().commentId(child.asString()));
					}
				}

			} else {
				urls.add(commentListingURL.asPostCommentListURL().commentId(parent_id));
			}
		}

		return urls;
	}

	public int getCount() {
		return count;
	}
}
