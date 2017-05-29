package com.wellsandwhistles.android.redditsp.image;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.activities.BugReportActivity;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyIfNotCached;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedObject;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;

import java.util.UUID;

public final class ImgurAPIV3 {

	public static void getAlbumInfo(
			final Context context,
			final String albumId,
			final int priority,
			final int listId,
			final boolean withAuth,
			final GetAlbumInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/3/album/" + albumId;

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				listId,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				withAuth
						? CacheRequest.DOWNLOAD_QUEUE_IMGUR_API
						: CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
				true,
				false,
				context
		) {
			@Override
			protected void onCallbackException(final Throwable t) {
				BugReportActivity.handleGlobalError(context, t);
			}

			@Override
			protected void onDownloadNecessary() {}

			@Override
			protected void onDownloadStarted() {}

			@Override
			protected void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {
				listener.onFailure(type, t, status, readableMessage);
			}

			@Override
			protected void onProgress(final boolean authorizationInProgress, final long bytesRead, final long totalBytes) {}

			@Override
			protected void onSuccess(final CacheManager.ReadableCacheFile cacheFile, final long timestamp, final UUID session, final boolean fromCache, final String mimetype) {}

			@Override
			public void onJsonParseStarted(final JsonValue result, final long timestamp, final UUID session, final boolean fromCache) {

				try {
					final JsonBufferedObject outer = result.asObject().getObject("data");
					listener.onSuccess(ImgurAPI.AlbumInfo.parseV3(albumId, outer));

				} catch(Throwable t) {
					listener.onFailure(CacheRequest.REQUEST_FAILURE_PARSE, t, null, "Imgur data parse failed");
				}
			}
		});
	}

	public static void getImageInfo(
			final Context context,
			final String imageId,
			final int priority,
			final int listId,
			final boolean withAuth,
			final GetImageInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/3/image/" + imageId;

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				listId,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				withAuth
					? CacheRequest.DOWNLOAD_QUEUE_IMGUR_API
					: CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
				true,
				false,
				context
		) {
			@Override
			protected void onCallbackException(final Throwable t) {
				BugReportActivity.handleGlobalError(context, t);
			}

			@Override
			protected void onDownloadNecessary() {}

			@Override
			protected void onDownloadStarted() {}

			@Override
			protected void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {
				listener.onFailure(type, t, status, readableMessage);
			}

			@Override
			protected void onProgress(final boolean authorizationInProgress, final long bytesRead, final long totalBytes) {}

			@Override
			protected void onSuccess(final CacheManager.ReadableCacheFile cacheFile, final long timestamp, final UUID session, final boolean fromCache, final String mimetype) {}

			@Override
			public void onJsonParseStarted(final JsonValue result, final long timestamp, final UUID session, final boolean fromCache) {

				try {
					final JsonBufferedObject outer = result.asObject().getObject("data");
					listener.onSuccess(ImageInfo.parseImgurV3(outer));

				} catch(Throwable t) {
					listener.onFailure(CacheRequest.REQUEST_FAILURE_PARSE, t, null, "Imgur data parse failed");
				}
			}
		});
	}
}
