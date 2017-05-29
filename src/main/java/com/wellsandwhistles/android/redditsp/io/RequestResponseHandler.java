package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public interface RequestResponseHandler<E, F> {
	void onRequestFailed(F failureReason);

	void onRequestSuccess(E result, long timeCached);
}
