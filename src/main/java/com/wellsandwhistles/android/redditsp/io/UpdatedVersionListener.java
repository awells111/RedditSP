package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public interface UpdatedVersionListener<K, V extends WritableObject<K>> {
	void onUpdatedVersion(V data);
}
