package com.wellsandwhistles.android.redditsp.reddit;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.util.Log;

import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyAlways;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.TimestampBound;
import com.wellsandwhistles.android.redditsp.io.*;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;
import com.wellsandwhistles.android.redditsp.reddit.api.SubredditRequestFailure;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditSubreddit;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditThing;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

	private class RedditAPIIndividualSubredditDataRequester implements CacheDataSource<String, RedditSubreddit, SubredditRequestFailure> {

        private static final String TAG = "IndividualSRDataReq";

        private final Context context;
        private final RedditAccount user;

        RedditAPIIndividualSubredditDataRequester(Context context, RedditAccount user) {
            this.context = context;
            this.user = user;
        }

        public void performRequest(final String subredditCanonicalName,
                                   final TimestampBound timestampBound,
                                   final RequestResponseHandler<RedditSubreddit, SubredditRequestFailure> handler) {

            final CacheRequest aboutSubredditCacheRequest = new CacheRequest(
                    Constants.Reddit.getUri("/r/" + subredditCanonicalName + "/about.json"),
                    user,
                    null,
                    Constants.Priority.API_SUBREDDIT_INVIDIVUAL,
                    0,
                    DownloadStrategyAlways.INSTANCE,
                    Constants.FileType.SUBREDDIT_ABOUT,
                    CacheRequest.DOWNLOAD_QUEUE_REDDIT_API,
                    true,
                    false,
                    context
            ) {

                @Override
                protected void onCallbackException(Throwable t) {
                    handler.onRequestFailed(new SubredditRequestFailure(CacheRequest.REQUEST_FAILURE_PARSE, t, null, "Parse error", url));
                }

                @Override protected void onDownloadNecessary() {}
                @Override protected void onDownloadStarted() {}
                @Override protected void onProgress(final boolean authorizationInProgress, long bytesRead, long totalBytes) {}

                @Override
                protected void onFailure(@RequestFailureType int type, Throwable t, Integer status, String readableMessage) {
                    handler.onRequestFailed(new SubredditRequestFailure(type, t, status, readableMessage, url));
                }

                @Override
                protected void onSuccess(CacheManager.ReadableCacheFile cacheFile, long timestamp, UUID session,
										 boolean fromCache, String mimetype) {}

                @Override
                public void onJsonParseStarted(JsonValue result, long timestamp, UUID session, boolean fromCache) {

                    try {
                        final RedditThing subredditThing = result.asObject(RedditThing.class);
                        final RedditSubreddit subreddit = subredditThing.asSubreddit();
                        subreddit.downloadTime = timestamp;
                        handler.onRequestSuccess(subreddit, timestamp);

                        RedditSubredditHistory.addSubreddit(user, subredditCanonicalName);

                    } catch(Exception e) {
                        handler.onRequestFailed(new SubredditRequestFailure(CacheRequest.REQUEST_FAILURE_PARSE, e, null, "Parse error", url));
                    }
                }
            };

            CacheManager.getInstance(context).makeRequest(aboutSubredditCacheRequest);
        }

        public void performRequest(final Collection<String> subredditCanonicalIds,
                                   final TimestampBound timestampBound,
                                   final RequestResponseHandler<HashMap<String, RedditSubreddit>, SubredditRequestFailure> handler) {

            // TODO if there's a bulk API to do this, that would be good... :)

            final HashMap<String, RedditSubreddit> result = new HashMap<>();
            final AtomicBoolean stillOkay = new AtomicBoolean(true);
            final AtomicInteger requestsToGo = new AtomicInteger(subredditCanonicalIds.size());
            final AtomicLong oldestResult = new AtomicLong(Long.MAX_VALUE);

            final RequestResponseHandler <RedditSubreddit, SubredditRequestFailure> innerHandler
                    = new RequestResponseHandler<RedditSubreddit, SubredditRequestFailure>() {
                @Override
                public void onRequestFailed(SubredditRequestFailure failureReason) {
                    synchronized(result) {
                        if(stillOkay.get()) {
                            stillOkay.set(false);
                            handler.onRequestFailed(failureReason);
                        }
                    }
                }

                @Override
                public void onRequestSuccess(RedditSubreddit innerResult, long timeCached) {
                    synchronized(result) {
                        if(stillOkay.get()) {

                            result.put(innerResult.getKey(), innerResult);
                            oldestResult.set(Math.min(oldestResult.get(), timeCached));

                            try
                            {
                                RedditSubredditHistory.addSubreddit(user, innerResult.getCanonicalName());
                            }
                            catch(RedditSubreddit.InvalidSubredditNameException e)
                            {
                                Log.e(TAG, "Invalid subreddit name " + innerResult.name, e);
                            }

                            if(requestsToGo.decrementAndGet() == 0) {
                                handler.onRequestSuccess(result, oldestResult.get());
                            }
                        }
                    }
                }
            };

            for(String subredditCanonicalId : subredditCanonicalIds) {
                performRequest(subredditCanonicalId, timestampBound, innerHandler);
            }
        }

        public void performWrite(RedditSubreddit value) {
            throw new UnsupportedOperationException();
        }

        public void performWrite(Collection<RedditSubreddit> values) {
            throw new UnsupportedOperationException();
        }
    }
}
