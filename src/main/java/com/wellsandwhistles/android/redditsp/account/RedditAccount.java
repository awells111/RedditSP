package com.wellsandwhistles.android.redditsp.account;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.reddit.api.RedditOAuth;

public class RedditAccount {

	public final String username;
	public final RedditOAuth.RefreshToken refreshToken;

	private RedditOAuth.AccessToken accessToken;

	public final long priority;

	public RedditAccount(
			final String username,
			final RedditOAuth.RefreshToken refreshToken,
			final long priority) {

		if(username == null) throw new RuntimeException("Null user in RedditAccount");

		this.username = username.trim();
		this.refreshToken = refreshToken;
		this.priority = priority;
	}

	public boolean isAnonymous() {
		return username.length() == 0;
	}

	public String getCanonicalUsername() {
		return General.asciiLowercase(username.trim());
	}

	public synchronized RedditOAuth.AccessToken getMostRecentAccessToken() {
		return accessToken;
	}

	public synchronized void setAccessToken(RedditOAuth.AccessToken token) {
		accessToken = token;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof RedditAccount && username.equalsIgnoreCase(((RedditAccount) o).username);
	}

	@Override
	public int hashCode() {
		return getCanonicalUsername().hashCode();
	}
}
