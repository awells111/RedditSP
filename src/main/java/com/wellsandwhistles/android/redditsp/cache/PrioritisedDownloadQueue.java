package com.wellsandwhistles.android.redditsp.cache;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import com.wellsandwhistles.android.redditsp.common.PrioritisedCachedThreadPool;

import java.util.HashSet;


class PrioritisedDownloadQueue {

	private final HashSet<CacheDownload> redditDownloadsQueued = new HashSet<>();

	private final PrioritisedCachedThreadPool mDownloadThreadPool = new PrioritisedCachedThreadPool(5, "Download");

	public PrioritisedDownloadQueue(final Context context) {
		new RedditQueueProcessor().start();
	}

	public synchronized void add(final CacheRequest request, final CacheManager manager) {

		final CacheDownload download = new CacheDownload(request, manager, this);

		if(request.queueType == CacheRequest.DOWNLOAD_QUEUE_REDDIT_API) {
			redditDownloadsQueued.add(download);
			notifyAll();

		} else if(request.queueType == CacheRequest.DOWNLOAD_QUEUE_IMMEDIATE
				|| request.queueType == CacheRequest.DOWNLOAD_QUEUE_IMGUR_API) {
			new CacheDownloadThread(download, true, "Cache Download Thread: Immediate");

		} else {
			mDownloadThreadPool.add(download);
		}
	}

	private synchronized CacheDownload getNextRedditInQueue() {

		while(redditDownloadsQueued.isEmpty()) {
			try { wait(); } catch (InterruptedException e) { throw new RuntimeException(e); }
		}

		CacheDownload next = null;

		for(final CacheDownload entry : redditDownloadsQueued) {
			if(next == null || entry.isHigherPriorityThan(next)) {
				next = entry;
			}
		}

		redditDownloadsQueued.remove(next);

		return next;
	}

	private class RedditQueueProcessor extends Thread {

		public RedditQueueProcessor() {
			super("Reddit Queue Processor");
		}

		@Override
		public void run() {

			while(true) {

				synchronized(this) {
					final CacheDownload download = getNextRedditInQueue();
					new CacheDownloadThread(download, true, "Cache Download Thread: Reddit");
				}

				try {
					sleep(1200); // Delay imposed by reddit API restrictions.
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

		}
	}
}
