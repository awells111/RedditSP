package com.wellsandwhistles.android.redditsp.image;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import org.apache.commons.lang3.StringEscapeUtils;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.activities.BugReportActivity;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyIfNotCached;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedArray;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedObject;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public final class ImgurAPI {

	public static class AlbumInfo {

		public final String id;
		public final String title;
		public final String description;

		public final ArrayList<ImageInfo> images;

		public AlbumInfo(final String id, final String title, final String description, final ArrayList<ImageInfo> images) {
			this.id = id;
			this.title = title;
			this.description = description;
			this.images = new ArrayList<>(images);
		}

		public static AlbumInfo parse(final String id, final JsonBufferedObject object)
				throws IOException, InterruptedException {

			String title = object.getString("title");
			String description = object.getString("description");

			if(title != null) {
				title = StringEscapeUtils.unescapeHtml4(title);
			}

			if(description != null) {
				description = StringEscapeUtils.unescapeHtml4(description);
			}

			final JsonBufferedArray imagesJson = object.getArray("images");
			final ArrayList<ImageInfo> images = new ArrayList<>();

			for(final JsonValue imageJson : imagesJson) {
				images.add(ImageInfo.parseImgur(imageJson.asObject()));
			}

			return new AlbumInfo(id, title, description, images);
		}

		public static AlbumInfo parseV3(final String id, final JsonBufferedObject object)
				throws IOException, InterruptedException {

			String title = object.getString("title");
			String description = object.getString("description");

			if(title != null) {
				title = StringEscapeUtils.unescapeHtml4(title);
			}

			if(description != null) {
				description = StringEscapeUtils.unescapeHtml4(description);
			}

			final JsonBufferedArray imagesJson = object.getArray("images");
			final ArrayList<ImageInfo> images = new ArrayList<>();

			for(final JsonValue imageJson : imagesJson) {
				images.add(ImageInfo.parseImgurV3(imageJson.asObject()));
			}

			return new AlbumInfo(id, title, description, images);
		}
	}

	public static void getAlbumInfo(
			final Context context,
			final String albumId,
			final int priority,
			final int listId,
			final GetAlbumInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/2/album/" + albumId + ".json";

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				listId,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
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
					final JsonBufferedObject outer = result.asObject().getObject("album");
					listener.onSuccess(AlbumInfo.parse(albumId, outer));

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
			final GetImageInfoListener listener) {

		final String apiUrl = "https://api.imgur.com/2/image/" + imageId + ".json";

		CacheManager.getInstance(context).makeRequest(new CacheRequest(
				General.uriFromString(apiUrl),
				RedditAccountManager.getAnon(),
				null,
				priority,
				listId,
				DownloadStrategyIfNotCached.INSTANCE,
				Constants.FileType.IMAGE_INFO,
				CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE,
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
					final JsonBufferedObject outer = result.asObject().getObject("image");
					listener.onSuccess(ImageInfo.parseImgur(outer));

				} catch(Throwable t) {
					listener.onFailure(CacheRequest.REQUEST_FAILURE_PARSE, t, null, "Imgur data parse failed");
				}
			}
		});
	}
}
