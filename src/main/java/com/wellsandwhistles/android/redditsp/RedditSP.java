package com.wellsandwhistles.android.redditsp;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.io.RedditChangeDataIO;
import com.wellsandwhistles.android.redditsp.receivers.NewMessageChecker;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditChangeDataManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.UUID;

public class RedditSP extends Application {

	@Override
	public void onCreate() {

		super.onCreate();

		Log.i("RedditSP", "Application created.");

		final Thread.UncaughtExceptionHandler androidHandler = Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread thread, Throwable t) {

				try {
					t.printStackTrace();

					File dir = Environment.getExternalStorageDirectory();

					if (dir == null) {
						dir = Environment.getDataDirectory();
					}

					final FileOutputStream fos = new FileOutputStream(new File(dir, "redditsp_crash_log_" + UUID.randomUUID().toString() + ".txt"));
					final PrintWriter pw = new PrintWriter(fos);
					t.printStackTrace(pw);
					pw.flush();
					pw.close();

				} catch (Throwable t1) {
				}

				androidHandler.uncaughtException(thread, t);
			}
		});

		final CacheManager cm = CacheManager.getInstance(this);

		cm.pruneTemp();

		new Thread() {
			@Override
			public void run() {

				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

				cm.pruneCache(); // Hope for the best :)
			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				RedditChangeDataIO.getInstance(RedditSP.this).runInitialReadInThisThread();
				RedditChangeDataManager.pruneAllUsers();
			}
		}.start();

		NewMessageChecker.checkForNewMessages(this);
	}
}
