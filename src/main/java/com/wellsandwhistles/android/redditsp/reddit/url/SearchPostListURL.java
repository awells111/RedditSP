package com.wellsandwhistles.android.redditsp.reddit.url;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.net.Uri;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.reddit.PostSort;

import java.util.ArrayList;
import java.util.List;

public class SearchPostListURL extends PostListingURL {

	public final String subreddit, query;

	public final PostSort order;
	public final Integer limit;
	public final String before, after;

	SearchPostListURL(String subreddit, String query, PostSort order, Integer limit, String before, String after) {
		this.subreddit = subreddit;
		this.query = query;
		this.order = order;
		this.limit = limit;
		this.before = before;
		this.after = after;
	}

	SearchPostListURL(String subreddit, String query, Integer limit, String before, String after) {
		this(subreddit, query, PostSort.RELEVANCE, limit, before, after);
	}

	public static SearchPostListURL build(String subreddit, String query) {
		if(subreddit != null) {
			while(subreddit.startsWith("/")) subreddit = subreddit.substring(1);
			while(subreddit.startsWith("r/")) subreddit = subreddit.substring(2);
		}
		return new SearchPostListURL(subreddit, query, null, null, null);
	}

	@Override
	public PostListingURL after(String after) {
		return new SearchPostListURL(subreddit, query, order, limit, before, after);
	}

	@Override
	public PostListingURL limit(Integer limit) {
		return new SearchPostListURL(subreddit, query, order, limit, before, after);
	}

	public SearchPostListURL sort(PostSort newOrder) {
		return new SearchPostListURL(subreddit, query, newOrder, limit, before, after);
	}

	@Override
	public Uri generateJsonUri() {

		Uri.Builder builder = new Uri.Builder();
		builder.scheme(Constants.Reddit.getScheme()).authority(Constants.Reddit.getDomain());

		if(subreddit != null) {
			builder.encodedPath("/r/");
			builder.appendPath(subreddit);
			builder.appendQueryParameter("restrict_sr", "on");
		} else {
			builder.encodedPath("/");
		}

		builder.appendEncodedPath("search");

		if(query != null) {
			builder.appendQueryParameter("q", query);
		}

		if(order != null) {
			switch(order) {
				case RELEVANCE:
				case NEW:
				case HOT:
				case TOP:
				case COMMENTS:
					builder.appendQueryParameter("sort", General.asciiLowercase(order.name()));
					break;
			}
		}

		if(before != null) {
			builder.appendQueryParameter("before", before);
		}

		if(after != null) {
			builder.appendQueryParameter("after", after);
		}

		if(limit != null) {
			builder.appendQueryParameter("limit", String.valueOf(limit));
		}

		builder.appendEncodedPath(".json");

		return builder.build();
	}

	@Override
	public @RedditURLParser.PathType int pathType() {
		return RedditURLParser.SEARCH_POST_LISTING_URL;
	}

	public static SearchPostListURL parse(final Uri uri) {

		boolean restrict_sr = false;
		String query = "";
		PostSort order = null;
		Integer limit = null;
		String before = null, after = null;

		for(final String parameterKey : General.getUriQueryParameterNames(uri)) {

			if(parameterKey.equalsIgnoreCase("after")) {
				after = uri.getQueryParameter(parameterKey);

			} else if(parameterKey.equalsIgnoreCase("before")) {
				before = uri.getQueryParameter(parameterKey);

			} else if(parameterKey.equalsIgnoreCase("limit")) {
				try {
					limit = Integer.parseInt(uri.getQueryParameter(parameterKey));
				} catch(Throwable ignored) {}

			} else if(parameterKey.equalsIgnoreCase("sort")) {
				order = PostSort.valueOfOrNull(uri.getQueryParameter(parameterKey));

			} else if(parameterKey.equalsIgnoreCase("q")) {
				query = uri.getQueryParameter(parameterKey);

			} else if(parameterKey.equalsIgnoreCase("restrict_sr")) {
				restrict_sr = "on".equalsIgnoreCase(uri.getQueryParameter(parameterKey));
			}
		}

		final String[] pathSegments;
		{
			final List<String> pathSegmentsList = uri.getPathSegments();

			final ArrayList<String> pathSegmentsFiltered = new ArrayList<>(pathSegmentsList.size());
			for(String segment : pathSegmentsList) {

				while(General.asciiLowercase(segment).endsWith(".json") || General.asciiLowercase(segment).endsWith(".xml")) {
					segment = segment.substring(0, segment.lastIndexOf('.'));
				}

				if(segment.length() > 0) {
					pathSegmentsFiltered.add(segment);
				}
			}

			pathSegments = pathSegmentsFiltered.toArray(new String[pathSegmentsFiltered.size()]);
		}

		if(pathSegments.length != 1 && pathSegments.length != 3) return null;
		if(!pathSegments[pathSegments.length - 1].equalsIgnoreCase("search")) return null;

		switch(pathSegments.length) {

			case 1: {
				return new SearchPostListURL(null, query, order, limit, before, after);
			}

			case 3: {

				if(!pathSegments[0].equals("r")) return null;

				final String subreddit = pathSegments[1];
				return new SearchPostListURL(restrict_sr ? subreddit : null, query, order, limit, before, after);
			}

			default:
				return null;
		}
	}

	@Override
	public String humanReadableName(Context context, boolean shorter) {

		if(shorter) return "Search Results";

		// TODO strings
		final StringBuilder builder = new StringBuilder("Search");

		if(query != null) {
			builder.append(" for \"").append(query).append("\"");
		}

		if(subreddit != null) {
			builder.append(" on /r/").append(subreddit);
		}

		return builder.toString();
	}

	@Override
	public String humanReadablePath() {
		final StringBuilder builder = new StringBuilder(super.humanReadablePath());

		if(query != null) {
			builder.append("?q=").append(query);
		}

		return builder.toString();
	}
}
