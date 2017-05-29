package com.wellsandwhistles.android.redditsp.jsonwrap;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.annotation.IntDef;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * An abstract class, extended by objects which may be partially received/parsed
 * at the time they are used.
 *
 */
public abstract class JsonBuffered {

	/**
	 * This object or array is not fully parsed yet. Because of this,
	 * attempts to access some properties or elements may cause the current
	 * thread to block.
	 */
	public static final int STATUS_LOADING = 0;

	/**
	 * This object or array is now fully loaded. All properties and elements
	 * can be accessed without blocking.
	 */
	public static final int STATUS_LOADED = 1;

	/**
	 * There was a problem parsing this object/array (or one of its
	 * descendants). Attempting to access any of the data it contains may
	 * cause an exception.
	 */
	public static final int STATUS_FAILED = 2;

	@IntDef({STATUS_LOADING, STATUS_LOADED, STATUS_FAILED})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Status {}

	private volatile @Status int status = STATUS_LOADING;
	private Throwable failReason = null;

	/**
	 * @return The current status of this object: STATUS_LOADING, STATUS_LOADED, or STATUS_FAILED.
	 */
	public final @Status int getStatus() {
		return status;
	}

	/**
	 * Causes the current thread to wait until this object and all its children
	 * fully received.
	 *
	 * @return The final status of the object (either STATUS_LOADED or STATUS_FAILED).
	 * @throws InterruptedException
	 */
	public final synchronized @Status int join() throws InterruptedException {

		while(status == STATUS_LOADING) {
			wait();
		}

		return status;
	}

	private synchronized void setLoaded() {
		status = STATUS_LOADED;
		notifyAll();
	}

	private synchronized void setFailed(final Throwable t) {
		status = STATUS_FAILED;
		failReason = t;
		notifyAll();
	}

	/**
	 * @return If this object or one of its children failed to parse, the
	 *		 exception that occurred at the time of failure is returned.
	 *		 Otherwise, null.
	 */
	public final Throwable getFailReason() {
		return failReason;
	}

	protected final void throwFailReasonException() throws IOException {

		final Throwable t = getFailReason();

		if(t instanceof JsonParseException)
			throw (JsonParseException)t;

		else if(t instanceof IOException)
			throw (IOException)t;

		else
			throw new RuntimeException(t);
	}

	protected final void build(final JsonParser jp) throws IOException {

		try {
			buildBuffered(jp);
			setLoaded();

		} catch (final IOException e) {
			setFailed(e);
			throw e;

		} catch (final Throwable t) {
			setFailed(t);
			throw new RuntimeException(t);
		}
	}

	protected abstract void buildBuffered(JsonParser jp) throws IOException;

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		try {
			prettyPrint(0, sb);

		} catch (InterruptedException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	protected abstract void prettyPrint(int indent, StringBuilder sb) throws InterruptedException, IOException;
}
