package com.wellsandwhistles.android.redditsp.receivers;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditChangeDataManager;

public class RegularCachePruner extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {

		Log.i("RegularCachePruner", "Pruning cache...");

		new Thread() {
			@Override
			public void run() {
				RedditChangeDataManager.pruneAllUsers();
				CacheManager.getInstance(context).pruneCache();
			}
		}.start();
	}
}
