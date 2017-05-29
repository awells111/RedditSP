package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public final class SwipeHistory {

	private final float[] positions;
	private final long[] timestamps;
	private int start = 0, len = 0;

	public SwipeHistory(int len) {
		positions = new float[len];
		timestamps = new long[len];
	}

	public void add(float position, long timestamp) {

		if(len >= positions.length) {
			positions[start] = position;
			timestamps[start] = timestamp;
			start = (start + 1) % positions.length;

		} else {
			positions[(start + len) % positions.length] = position;
			timestamps[(start + len) % timestamps.length] = timestamp;
			len++;
		}
	}

	public float getMostRecent() {
		return positions[getNthMostRecentIndex(0)];
	}

	public float getAtTimeAgoMs(long timeAgo) {

		final long timestamp = timestamps[getNthMostRecentIndex(0)] - timeAgo;
		float result = getMostRecent();

		for(int i = 0; i < len; i++) {

			final int index = getNthMostRecentIndex(i);

			if(timestamp > timestamps[index]) {
				return result;
			} else {
				result = positions[index];
			}
		}

		return result;
	}

	private int getNthMostRecentIndex(int n) {
		if(n >= len || n < 0) throw new ArrayIndexOutOfBoundsException(n);
		return (start + len - n - 1) % positions.length;
	}

	public void clear() {
		len = 0;
		start = 0;
	}

	public int size() {
		return len;
	}
}
