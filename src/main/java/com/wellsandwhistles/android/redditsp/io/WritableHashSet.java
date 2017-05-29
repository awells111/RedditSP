package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.UnexpectedInternalStateException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class WritableHashSet implements WritableObject<String> {

	@WritableObjectVersion
	public static int DB_VERSION = 1;

	private transient HashSet<String> hashSet = null;
	@WritableField
	private String serialised;

	@WritableObjectKey
	private final String key;
	@WritableObjectTimestamp
	private final long timestamp;

	public WritableHashSet(HashSet<String> data, long timestamp, String key) {
		this.hashSet = data;
		this.timestamp = timestamp;
		this.key = key;
		serialised = listToEscapedString(hashSet);
	}

	private WritableHashSet(String serializedData, long timestamp, String key) {
		this.timestamp = timestamp;
		this.key = key;
		serialised = serializedData;
	}

	public WritableHashSet(CreationData creationData) {
		this.timestamp = creationData.timestamp;
		this.key = creationData.key;
	}

	@Override
	public String toString() {
		throw new UnexpectedInternalStateException("Using toString() is the wrong way to serialise a WritableHashSet");
	}

	public String serializeWithMetadata() {
		final ArrayList<String> result = new ArrayList<>(3);
		result.add(serialised);
		result.add(String.valueOf(timestamp));
		result.add(key);
		return listToEscapedString(result);
	}

	public static WritableHashSet unserializeWithMetadata(String raw) {
		final ArrayList<String> data = escapedStringToList(raw);
		return new WritableHashSet(data.get(0), Long.valueOf(data.get(1)), data.get(2));
	}

	public synchronized HashSet<String> toHashset() {
		if(hashSet != null) return hashSet;
		return (hashSet = new HashSet<>(escapedStringToList(serialised)));
	}

	public String getKey() {
		return key;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public static String listToEscapedString(final Collection<String> list) {

		if(list.size() == 0) return "";

		final StringBuilder sb = new StringBuilder();

		for(final String str : list) {
			for(int i = 0; i < str.length(); i++) {

				final char c = str.charAt(i);

				switch(c) {
					case '\\':
						sb.append("\\\\");
						break;
					case ';':
						sb.append("\\;");
						break;
					default:
						sb.append(c);
						break;
				}
			}

			sb.append(';');
		}

		return sb.toString();
	}

	public static ArrayList<String> escapedStringToList(String str) {

		// Workaround to improve parsing of lists saved by older versions of the app
		if(str.length() > 0 && !str.endsWith(";")) str += ";";

		final ArrayList<String> result = new ArrayList<>();

		if(str != null) {

			boolean isEscaped = false;
			final StringBuilder sb = new StringBuilder();

			for(int i = 0; i < str.length(); i++) {

				final char c = str.charAt(i);

				if(c == ';' && !isEscaped) {
					result.add(sb.toString());
					sb.setLength(0);

				} else if(c == '\\') {
					if(isEscaped) sb.append('\\');

				} else {
					sb.append(c);
				}

				isEscaped = c == '\\' && !isEscaped;
			}
		}

		return result;
	}
}
