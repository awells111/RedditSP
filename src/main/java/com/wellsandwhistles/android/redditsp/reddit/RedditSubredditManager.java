package com.wellsandwhistles.android.redditsp.reddit;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.TimestampBound;
import com.wellsandwhistles.android.redditsp.io.*;
import com.wellsandwhistles.android.redditsp.reddit.api.RedditAPIIndividualSubredditDataRequester;
import com.wellsandwhistles.android.redditsp.reddit.api.SubredditRequestFailure;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditSubreddit;

import java.util.Collection;
import java.util.HashMap;

public class RedditSubredditManager {

	public void offerRawSubredditData(Collection<RedditSubreddit> toWrite, long timestamp) {
		subredditCache.performWrite(toWrite);
	}

	// TODO need way to cancel web update and start again?
	// TODO anonymous user

	// TODO Ability to temporarily flag subreddits as subscribed/unsubscribed
	// TODO Ability to temporarily add/remove subreddits from multireddits

	// TODO store favourites in preference

	public enum SubredditListType { SUBSCRIBED, MODERATED, MULTIREDDITS, MOST_POPULAR, DEFAULTS }

	private static RedditSubredditManager singleton;
	private static RedditAccount singletonUser;

	private final WeakCache<String, RedditSubreddit, SubredditRequestFailure> subredditCache;

	public static synchronized RedditSubredditManager getInstance(Context context, RedditAccount user) {

		if(singleton == null || !user.equals(singletonUser)) {
			singletonUser = user;
			singleton = new RedditSubredditManager(context, user);
		}

		return singleton;
	}

	private RedditSubredditManager(Context context, RedditAccount user) {

		// Subreddit cache

		final RawObjectDB<String, RedditSubreddit> subredditDb
				= new RawObjectDB<>(context, getDbFilename("subreddits", user), RedditSubreddit.class);

		final ThreadedRawObjectDB<String, RedditSubreddit, SubredditRequestFailure> subredditDbWrapper
				= new ThreadedRawObjectDB<>(subredditDb, new RedditAPIIndividualSubredditDataRequester(context, user));

		subredditCache = new WeakCache<>(subredditDbWrapper);
	}

	private static String getDbFilename(String type, RedditAccount user) {
		return General.sha1(user.username.getBytes()) + "_" + type + "_subreddits.db";
	}

	public void getSubreddit(String subredditCanonicalId,
							 TimestampBound timestampBound,
							 RequestResponseHandler<RedditSubreddit, SubredditRequestFailure> handler,
							 UpdatedVersionListener<String, RedditSubreddit> updatedVersionListener) {

		final String subredditDisplayName = RedditSubreddit.getDisplayNameFromCanonicalName(subredditCanonicalId);
		subredditCache.performRequest(subredditDisplayName, timestampBound, handler, updatedVersionListener);
	}

	public void getSubreddits(Collection<String> ids,
							 TimestampBound timestampBound,
							 RequestResponseHandler<HashMap<String, RedditSubreddit>, SubredditRequestFailure> handler) {

		subredditCache.performRequest(ids, timestampBound, handler);
	}
}
