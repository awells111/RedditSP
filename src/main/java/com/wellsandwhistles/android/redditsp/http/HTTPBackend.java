package com.wellsandwhistles.android.redditsp.http;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.http.okhttp.OKHTTPBackend;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

public abstract class HTTPBackend {

	private static boolean useJavaBackend = false;

	/**
	 * Factory method can read configuration information to choose a backend
	 */
	public static HTTPBackend getBackend() {
		return useJavaBackend ? new JavaHTTPBackend() : OKHTTPBackend.getHttpBackend();
	}

	public static class RequestDetails {

		private final URI mUrl;
		private final List<PostField> mPostFields;

		public RequestDetails(final URI url, final List<PostField> postFields) {
			mUrl = url;
			mPostFields = postFields;
		}

		public URI getUrl() {
			return mUrl;
		}

		public List<PostField> getPostFields() {
			return mPostFields;
		}
	}

	public static class PostField {

		public final String name;
		public final String value;

		public PostField(final String name, final String value) {
			this.name = name;
			this.value = value;
		}

		public String encode() {
			try {
				return URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		public static String encodeList(final List<PostField> fields) {

			final StringBuilder result = new StringBuilder();

			for (final PostField field : fields) {

				if (result.length() > 0) {
					result.append('&');
				}

				result.append(field.encode());
			}

			return result.toString();
		}
	}

	public interface Request {
		void executeInThisThread(final Listener listener);

		void cancel();

		void addHeader(String name, String value);
	}

	public interface Listener {
		void onError(@CacheRequest.RequestFailureType int failureType, Throwable exception, Integer httpStatus);

		void onSuccess(String mimetype, Long bodyBytes, InputStream body);
	}

	public abstract Request prepareRequest(Context context, RequestDetails details);

	public abstract void recreateHttpBackend();

}
