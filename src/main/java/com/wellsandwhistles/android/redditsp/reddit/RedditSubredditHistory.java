package com.wellsandwhistles.android.redditsp.reddit;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditSubreddit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


// Keeps an in-memory list of all known subreddits per account
public class RedditSubredditHistory
{
	private static final HashMap<RedditAccount, HashSet<String>> SUBREDDITS = new HashMap<>();

	public static synchronized void addSubreddit(final RedditAccount account, final String name) throws RedditSubreddit.InvalidSubredditNameException
	{
		putDefaultSubreddits(account);
		SUBREDDITS.get(account).add(General.asciiLowercase(RedditSubreddit.stripRPrefix(name)));
	}

	public static synchronized ArrayList<String> getSubredditsSorted(final RedditAccount account)
	{
		putDefaultSubreddits(account);
		final ArrayList<String> result = new ArrayList<>(SUBREDDITS.get(account));
		Collections.sort(result);
		return result;
	}

	private static void putDefaultSubreddits(final RedditAccount account){
		if (!SUBREDDITS.containsKey(account) || SUBREDDITS.get(account) == null) {
			SUBREDDITS.put(account, new HashSet<String>());
		}
		HashSet<String> personalizedSubreddits = SUBREDDITS.get(account);
		if (personalizedSubreddits.isEmpty()) {
			for (final String subreddit : Constants.Reddit.DEFAULT_SUBREDDITS) {
				try {
					personalizedSubreddits.add(General.asciiLowercase(RedditSubreddit.stripRPrefix(subreddit)));
				} catch (final RedditSubreddit.InvalidSubredditNameException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
