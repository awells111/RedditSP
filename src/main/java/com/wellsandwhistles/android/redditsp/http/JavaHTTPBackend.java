package com.wellsandwhistles.android.redditsp.http;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.util.Log;

import com.wellsandwhistles.android.redditsp.cache.CacheRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;

public class JavaHTTPBackend extends HTTPBackend {

    private static final String TAG = "JavaHTTPBackend";

	private List<PostField> postFields;

	@Override
	public void recreateHttpBackend() {}

	@Override
    public Request prepareRequest(Context context, RequestDetails details) {
        HttpURLConnection urlConn;
        try {
			urlConn = (HttpURLConnection) details.getUrl().toURL().openConnection();

			postFields = details.getPostFields();

            if (postFields != null) {
                urlConn.setDoOutput(true);
                urlConn.setRequestMethod("POST");
				urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            } else {
                urlConn.setRequestMethod("GET");
            }
        } catch (Exception e) {
			Log.e(TAG, "Error creating HTTP request for " + details.getUrl(), e);
            return null;
        }

        final HttpURLConnection conn = urlConn;
        return new Request() {
            @Override
            public void executeInThisThread(final Listener listener) {
                try {
                    try {
                        conn.connect();

						if (postFields != null) {
							OutputStream os = conn.getOutputStream();
							os.write(PostField.encodeList(postFields).getBytes());
						}
                    } catch(IOException e) {
                        listener.onError(CacheRequest.REQUEST_FAILURE_CONNECTION, e, null);
                        return;
                    }

                    final int status = conn.getResponseCode();
                    if (status == 200 || status == 202) {
                        final InputStream bodyStream = conn.getInputStream();
                        final Long bodyBytes = Long.valueOf(conn.getContentLength());
                        final String contentType = conn.getHeaderField("Content-Type");

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
                conn.disconnect();
            }

            @Override
            public void addHeader(final String name, final String value) {
                conn.addRequestProperty(name, value);
            }
        };
    }
}
