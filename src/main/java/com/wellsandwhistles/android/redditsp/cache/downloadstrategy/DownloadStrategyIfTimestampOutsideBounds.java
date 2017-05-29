package com.wellsandwhistles.android.redditsp.cache.downloadstrategy;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.cache.CacheEntry;
import com.wellsandwhistles.android.redditsp.common.TimestampBound;

public class DownloadStrategyIfTimestampOutsideBounds implements DownloadStrategy {

	private final TimestampBound mTimestampBound;

	public DownloadStrategyIfTimestampOutsideBounds(final TimestampBound timestampBound) {
		mTimestampBound = timestampBound;
	}

	@Override
	public boolean shouldDownloadWithoutCheckingCache() {
		return false;
	}

	@Override
	public boolean shouldDownloadIfCacheEntryFound(final CacheEntry entry) {
		return !mTimestampBound.verifyTimestamp(entry.timestamp);
	}

	@Override
	public boolean shouldDownloadIfNotCached() {
		return true;
	}
}
