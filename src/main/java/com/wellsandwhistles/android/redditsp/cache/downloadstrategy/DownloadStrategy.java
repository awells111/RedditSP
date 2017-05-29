package com.wellsandwhistles.android.redditsp.cache.downloadstrategy;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.cache.CacheEntry;

public interface DownloadStrategy {

	boolean shouldDownloadWithoutCheckingCache();

	boolean shouldDownloadIfCacheEntryFound(final CacheEntry entry);

	boolean shouldDownloadIfNotCached();
}
