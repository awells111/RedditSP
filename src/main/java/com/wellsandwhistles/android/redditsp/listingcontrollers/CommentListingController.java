package com.wellsandwhistles.android.redditsp.listingcontrollers;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.fragments.CommentListingFragment;
import com.wellsandwhistles.android.redditsp.reddit.url.CommentListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.PostCommentListingURL;
import com.wellsandwhistles.android.redditsp.reddit.url.RedditURLParser;
import com.wellsandwhistles.android.redditsp.reddit.url.UserCommentListingURL;

import java.util.UUID;

// TODO add notification/header for abnormal sort order
public class CommentListingController {

	private CommentListingURL mUrl;
	private UUID mSession = null;
	private String mSearchString = null;

	public UUID getSession() {
		return mSession;
	}

	public void setSession(UUID session) {
		mSession = session;
	}

	public CommentListingController(RedditURLParser.RedditURL url, final Context context) {

		if(url.pathType() == RedditURLParser.POST_COMMENT_LISTING_URL) {
			if(url.asPostCommentListURL().order == null) {
				url = url.asPostCommentListURL().order(defaultOrder(context));
			}
		} else if(url.pathType() == RedditURLParser.USER_COMMENT_LISTING_URL) {
			if(url.asUserCommentListURL().order == null) {
				url = url.asUserCommentListURL().order(UserCommentListingURL.Sort.NEW);
			}
		}

		if(!(url instanceof CommentListingURL)) {
			throw new RuntimeException("Not comment listing URL");
		}

		this.mUrl = (CommentListingURL)url;
	}

	private PostCommentListingURL.Sort defaultOrder(final Context context) {
		return PrefsUtility.pref_behaviour_commentsort(context, PreferenceManager.getDefaultSharedPreferences(context));
	}

	public void setSort(final PostCommentListingURL.Sort s) {
		if(mUrl.pathType() == RedditURLParser.POST_COMMENT_LISTING_URL) {
			mUrl = mUrl.asPostCommentListURL().order(s);
		}
	}

	public void setSort(final UserCommentListingURL.Sort s) {
		if(mUrl.pathType() == RedditURLParser.USER_COMMENT_LISTING_URL) {
			mUrl = mUrl.asUserCommentListURL().order(s);
		}
	}

	public PostCommentListingURL.Sort getSort() {

		if(mUrl.pathType() == RedditURLParser.POST_COMMENT_LISTING_URL) {
			return mUrl.asPostCommentListURL().order;
		}

		return null;
	}

	public void setSearchString(String searchString) {
		mSearchString = searchString;
	}

	public String getSearchString() {
		return mSearchString;
	}

	public Uri getUri() {
		return mUrl.generateJsonUri();
	}

	public CommentListingURL getCommentListingUrl() {
		return mUrl;
	}

	public CommentListingFragment get(final AppCompatActivity parent, final boolean force, final Bundle savedInstanceState) {
		if(force) mSession = null;
		return new CommentListingFragment(
				parent,
				savedInstanceState,
				General.listOfOne((RedditURLParser.RedditURL)mUrl),
				mSession,
				mSearchString,
				force);
	}

	public boolean isSortable() {
		return mUrl.pathType() == RedditURLParser.POST_COMMENT_LISTING_URL
				|| mUrl.pathType() == RedditURLParser.USER_COMMENT_LISTING_URL;
	}

	public boolean isUserCommentListing() {
		return mUrl.pathType() == RedditURLParser.USER_COMMENT_LISTING_URL;
	}
}
