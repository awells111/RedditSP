package com.wellsandwhistles.android.redditsp.cache;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

final class CacheDownloadThread extends Thread {
	private final CacheDownload singleDownload;

	public CacheDownloadThread(
			final CacheDownload singleDownload,
			final boolean start,
			final String name) {

		super(name);
		this.singleDownload = singleDownload;
		if(start) start();
	}

	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		singleDownload.doDownload();
	}
}
