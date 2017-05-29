package com.wellsandwhistles.android.redditsp.reddit.things;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


public final class RedditThing {

	public enum Kind {
		POST, USER, COMMENT, MESSAGE, SUBREDDIT, MORE_COMMENTS, LISTING
	}

	private static final Map<String, Kind> kinds;

	static {
		kinds = new HashMap<>();
		kinds.put("t1", Kind.COMMENT);
		kinds.put("t2", Kind.USER);
		kinds.put("t3", Kind.POST);
		kinds.put("t4", Kind.MESSAGE);
		kinds.put("t5", Kind.SUBREDDIT);
		kinds.put("more", Kind.MORE_COMMENTS);
		kinds.put("Listing", Kind.LISTING);
	}

	public String kind;
	public JsonBufferedObject data;

	public Kind getKind() {
		return kinds.get(kind);
	}

	public RedditMoreComments asMoreComments() throws InstantiationException, IllegalAccessException, InterruptedException, IOException, NoSuchMethodException, InvocationTargetException {
		return data.asObject(RedditMoreComments.class);
	}

	public RedditComment asComment() throws InstantiationException, IllegalAccessException, InterruptedException, IOException, NoSuchMethodException, InvocationTargetException {
		return data.asObject(RedditComment.class);
	}

	public RedditPost asPost() throws InstantiationException, IllegalAccessException, InterruptedException, IOException, NoSuchMethodException, InvocationTargetException {
		return data.asObject(RedditPost.class);
	}

	public RedditSubreddit asSubreddit() throws InstantiationException, IllegalAccessException, InterruptedException, IOException, NoSuchMethodException, InvocationTargetException {
		return data.asObject(RedditSubreddit.class);
	}

	public RedditUser asUser() throws InstantiationException, IllegalAccessException, InterruptedException, IOException, NoSuchMethodException, InvocationTargetException {
		return data.asObject(RedditUser.class);
	}

	public RedditMessage asMessage() throws IllegalAccessException, InterruptedException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
		return data.asObject(RedditMessage.class);
	}
}
