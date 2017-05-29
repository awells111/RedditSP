package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public class SRError {

	public final String title, message;
	public final Throwable t;
	public final Integer httpStatus;
	public final String url;

	public SRError(String title, String message) {
		this(title, message, null, null, null);
	}

	public SRError(String title, String message, Throwable t) {
		this(title, message, t, null, null);
	}

	public SRError(String title, String message, Throwable t, Integer httpStatus, String url) {

		this.title = title;
		this.message = message;
		this.t = t;
		this.httpStatus = httpStatus;
		this.url = url;
	}
}
