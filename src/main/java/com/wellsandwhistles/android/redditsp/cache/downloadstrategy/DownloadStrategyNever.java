package com.wellsandwhistles.android.redditsp.cache.downloadstrategy;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.cache.CacheEntry;

public class DownloadStrategyNever implements DownloadStrategy {

	public static final DownloadStrategyNever INSTANCE = new DownloadStrategyNever();

	private DownloadStrategyNever() {}

	@Override
	public boolean shouldDownloadWithoutCheckingCache() {
		return false;
	}

	@Override
	public boolean shouldDownloadIfCacheEntryFound(final CacheEntry entry) {
		return false;
	}

	@Override
	public boolean shouldDownloadIfNotCached() {
		return false;
	}
}
