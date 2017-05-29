package com.wellsandwhistles.android.redditsp.reddit.api;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyAlways;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.SRTime;
import com.wellsandwhistles.android.redditsp.common.TimestampBound;
import com.wellsandwhistles.android.redditsp.io.CacheDataSource;
import com.wellsandwhistles.android.redditsp.io.RequestResponseHandler;
import com.wellsandwhistles.android.redditsp.io.WritableHashSet;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedArray;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class RedditAPIMultiredditListRequester
		implements CacheDataSource<RedditAPIMultiredditListRequester.Key, WritableHashSet, SubredditRequestFailure> {

	public static class Key {
		public static final Key INSTANCE = new Key();
		private Key() {}
	}

	private final Context context;
	private final RedditAccount user;

	public RedditAPIMultiredditListRequester(Context context, RedditAccount user) {
		this.context = context;
		this.user = user;
	}

	@Override
	public void performRequest(final Key key,
							   final TimestampBound timestampBound,
							   final RequestResponseHandler<WritableHashSet, SubredditRequestFailure> handler) {

		if(user.isAnonymous()) {

			final long now = SRTime.utcCurrentTimeMillis();

			handler.onRequestSuccess(
					new WritableHashSet(
							new HashSet<String>(),
							now,
							user.getCanonicalUsername()),
					now);

		} else {
			doRequest(handler);
		}
	}

	private void doRequest(final RequestResponseHandler<WritableHashSet, SubredditRequestFailure> handler) {

		final URI uri = Constants.Reddit.getUri(Constants.Reddit.PATH_MULTIREDDITS_MINE);

		final CacheRequest request = new CacheRequest(
				uri,
				user,
				null,
				Constants.Priority.API_SUBREDDIT_LIST,
				0,
				DownloadStrategyAlways.INSTANCE,
				Constants.FileType.MULTIREDDIT_LIST,
				CacheRequest.DOWNLOAD_QUEUE_REDDIT_API,
				true,
				false,
				context
		) {

			@Override
			protected void onCallbackException(Throwable t) {
				handler.onRequestFailed(new SubredditRequestFailure(CacheRequest.REQUEST_FAILURE_PARSE, t, null, "Internal error", url));
			}

			@Override protected void onDownloadNecessary() {}
			@Override protected void onDownloadStarted() {}
			@Override protected void onProgress(final boolean authorizationInProgress, long bytesRead, long totalBytes) {}

			@Override
			protected void onFailure(@RequestFailureType int type, Throwable t, Integer status, String readableMessage) {
				handler.onRequestFailed(new SubredditRequestFailure(type, t, status, readableMessage, url.toString()));
			}

			@Override
			protected void onSuccess(CacheManager.ReadableCacheFile cacheFile, long timestamp, UUID session,
									 boolean fromCache, String mimetype) {}

			@Override
			public void onJsonParseStarted(JsonValue result, long timestamp, UUID session, boolean fromCache) {

				try {
					final HashSet<String> output = new HashSet<>();

					final JsonBufferedArray multiredditList = result.asArray();

					for(final JsonValue multireddit : multiredditList) {
						final String name = multireddit.asObject().getObject("data").getString("name");
						output.add(name);
					}

					handler.onRequestSuccess(new WritableHashSet(output, timestamp, user.getCanonicalUsername()), timestamp);

				} catch(Exception e) {
					handler.onRequestFailed(
							new SubredditRequestFailure(
									CacheRequest.REQUEST_FAILURE_PARSE, e, null, "Parse error", url.toString()));
				}
			}
		};

		CacheManager.getInstance(context).makeRequest(request);
	}

	@Override
	public void performRequest(Collection<Key> keys, TimestampBound timestampBound,
							   RequestResponseHandler<HashMap<Key, WritableHashSet>,
									   SubredditRequestFailure> handler) {
		throw new UnsupportedOperationException();
	}

	public void performWrite(WritableHashSet value) {
		throw new UnsupportedOperationException();
	}

	public void performWrite(Collection<WritableHashSet> values) {
		throw new UnsupportedOperationException();
	}
}
