package com.wellsandwhistles.android.redditsp.image;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.cache.CacheRequest;

public interface GetAlbumInfoListener {

	void onFailure(
		final @CacheRequest.RequestFailureType int type,
		final Throwable t,
		final Integer status,
		final String readableMessage);

	void onSuccess(ImgurAPI.AlbumInfo info);
}
