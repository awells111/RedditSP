package com.wellsandwhistles.android.redditsp.cache.downloadstrategy;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.cache.CacheEntry;

public class DownloadStrategyAlways implements DownloadStrategy {

	public static final DownloadStrategyAlways INSTANCE = new DownloadStrategyAlways();

	private DownloadStrategyAlways() {}

	@Override
	public boolean shouldDownloadWithoutCheckingCache() {
		return true;
	}

	@Override
	public boolean shouldDownloadIfCacheEntryFound(final CacheEntry entry) {
		// Should never get here
		return true;
	}

	@Override
	public boolean shouldDownloadIfNotCached() {
		// Should never get here
		return true;
	}
}
