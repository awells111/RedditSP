package com.wellsandwhistles.android.redditsp.reddit.api;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.annotation.SuppressLint;
import android.content.Context;

import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.SRError;

import java.net.URI;

public class SubredditRequestFailure {
	public final @CacheRequest.RequestFailureType int requestFailureType;
	public final Throwable t;
	public final Integer statusLine;
	public final String readableMessage;
	public final String url;

	public SubredditRequestFailure(@CacheRequest.RequestFailureType int requestFailureType, Throwable t,
								   Integer statusLine, String readableMessage, String url) {
		this.requestFailureType = requestFailureType;
		this.t = t;
		this.statusLine = statusLine;
		this.readableMessage = readableMessage;
		this.url = url;
	}

	public SubredditRequestFailure(@CacheRequest.RequestFailureType int requestFailureType, Throwable t,
								   Integer statusLine, String readableMessage, URI url) {
		this(requestFailureType, t, statusLine, readableMessage, url != null ? url.toString() : null);
	}

	@SuppressLint("WrongConstant")
	public SRError asError(Context context) {
		return General.getGeneralErrorForFailure(context, requestFailureType, t, statusLine, url);
	}
}
