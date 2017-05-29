package com.wellsandwhistles.android.redditsp.http.okhttp;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.http.HTTPBackend;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class OKHTTPBackend extends HTTPBackend {

	private final OkHttpClient mClient;
	private static HTTPBackend httpBackend;

	private OKHTTPBackend() {

		final OkHttpClient.Builder builder = new OkHttpClient.Builder();

		builder.followRedirects(true);
		builder.followSslRedirects(true);

		builder.connectTimeout(15000, TimeUnit.SECONDS);
		builder.readTimeout(10000, TimeUnit.SECONDS);

		builder.connectionPool(new ConnectionPool(1, 5, TimeUnit.SECONDS));

		builder.retryOnConnectionFailure(true);

		mClient = builder.build();
	}

	public static synchronized HTTPBackend getHttpBackend() {
		if(httpBackend == null) {
			httpBackend = new OKHTTPBackend();
		}
		return httpBackend;
	}

	@Override
	public synchronized void recreateHttpBackend() {
		httpBackend = new OKHTTPBackend();
	}

	@Override
	public Request prepareRequest(final Context context, final RequestDetails details) {

		final okhttp3.Request.Builder builder = new okhttp3.Request.Builder();

		builder.header("User-Agent", Constants.ua(context));

		final List<PostField> postFields = details.getPostFields();

		if(postFields != null) {
			builder.post(RequestBody.create(
					MediaType.parse("application/x-www-form-urlencoded"),
					PostField.encodeList(postFields)));

		} else {
			builder.get();
		}

		builder.url(details.getUrl().toString());
		builder.cacheControl(CacheControl.FORCE_NETWORK);

		final AtomicReference<Call> callRef = new AtomicReference<>();

		return new Request() {

			public void executeInThisThread(final Listener listener) {

				final Call call = mClient.newCall(builder.build());
				callRef.set(call);

				try {

					final Response response;

					try {
						response = call.execute();
					} catch(IOException e) {
						listener.onError(CacheRequest.REQUEST_FAILURE_CONNECTION, e, null);
						return;
					}

					final int status = response.code();

					if(status == 200 || status == 202) {

						final ResponseBody body = response.body();
						final InputStream bodyStream;
						final Long bodyBytes;

						if(body != null) {
							bodyStream = body.byteStream();
							bodyBytes = body.contentLength();

						} else {
							bodyStream = null;
							bodyBytes = null;
						}

						final String contentType = response.header("Content-Type");

						listener.onSuccess(contentType, bodyBytes, bodyStream);

					} else {
						listener.onError(CacheRequest.REQUEST_FAILURE_REQUEST, null, status);
					}

				} catch(Throwable t) {
					listener.onError(CacheRequest.REQUEST_FAILURE_CONNECTION, t, null);
				}
			}

			@Override
			public void cancel() {
				final Call call = callRef.getAndSet(null);
				if(call != null) {
					call.cancel();
				}
			}

			@Override
			public void addHeader(final String name, final String value) {
				builder.addHeader(name, value);
			}
		};
	}
}
