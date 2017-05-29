package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface WritableObject<K> {

	class CreationData {
		public final String key;
		public final long timestamp;

		public CreationData(String key, long timestamp) {
			this.key = key;
			this.timestamp = timestamp;
		}
	}

	K getKey();
	long getTimestamp();

	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface WritableObjectVersion {}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface WritableObjectKey {}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface WritableObjectTimestamp {}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface WritableField {}
}
