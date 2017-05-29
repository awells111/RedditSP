package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public abstract class TimestampBound {

	public abstract boolean verifyTimestamp(long timestamp);

	public static final TimestampBound ANY = new TimestampBound() {
		@Override
		public boolean verifyTimestamp(long timestamp) {
			return true;
		}
	};
	public static final TimestampBound NONE = new TimestampBound() {
		@Override
		public boolean verifyTimestamp(long timestamp) {
			return false;
		}
	};

	public static final class MoreRecentThanBound extends TimestampBound {

		private final long minTimestamp;

		public MoreRecentThanBound(long minTimestamp) {
			this.minTimestamp = minTimestamp;
		}

		@Override
		public boolean verifyTimestamp(long timestamp) {
			return timestamp >= minTimestamp;
		}
	}

	public static MoreRecentThanBound notOlderThan(long ageMs) {
		return new MoreRecentThanBound(System.currentTimeMillis() - ageMs);
	}
}
