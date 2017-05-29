package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.TimestampBound;

import java.util.Collection;
import java.util.HashMap;

public interface CacheDataSource<K, V, F> {
	void performRequest(K key, final TimestampBound timestampBound, RequestResponseHandler<V, F> handler);

	void performRequest(Collection<K> keys, final TimestampBound timestampBound, RequestResponseHandler<HashMap<K, V>, F> handler);

	void performWrite(V value);

	void performWrite(Collection<V> values);
}
