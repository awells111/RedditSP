package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.Bitmap;
import android.support.annotation.UiThread;
import android.util.Log;

import com.wellsandwhistles.android.redditsp.common.LinkHandler;

public class ImageViewTileLoader {

	@UiThread
	public interface Listener {
		void onTileLoaded(int x, int y, int sampleSize);

		void onTileLoaderOutOfMemory();

		void onTileLoaderException(Throwable t);
	}

	private final ImageTileSource mSource;
	private final ImageViewTileLoaderThread mThread;
	private final int mX, mY, mSampleSize;

	private boolean mWanted;

	private Bitmap mResult;

	private final Listener mListener;

	private final Runnable mNotifyRunnable;

	private final Object mLock;

	public ImageViewTileLoader(
			ImageTileSource source,
			ImageViewTileLoaderThread thread,
			int x,
			int y,
			int sampleSize,
			Listener listener,
			final Object lock) {

		mSource = source;
		mThread = thread;
		mX = x;
		mY = y;
		mSampleSize = sampleSize;
		mListener = listener;
		mLock = lock;

		mNotifyRunnable = new Runnable() {
			@Override
			public void run() {
				mListener.onTileLoaded(mX, mY, mSampleSize);
			}
		};
	}

	// Caller must synchronize on mLock
	public void markAsWanted() {

		if(mWanted) {
			return;
		}

		if(mResult != null) {
			throw new RuntimeException("Not wanted, but the image is loaded anyway!");
		}

		mThread.enqueue(this);
		mWanted = true;
	}

	public void doPrepare() {

		synchronized(mLock) {

			if(!mWanted) {
				return;
			}

			if(mResult != null) {
				return;
			}
		}

		final Bitmap tile;

		try {
			tile = mSource.getTile(mSampleSize, mX, mY);

		} catch(OutOfMemoryError e) {
			LinkHandler.UI_THREAD_HANDLER.post(new NotifyOOMRunnable());
			return;

		} catch(Throwable t) {
			Log.e("ImageViewTileLoader", "Exception in getTile()", t);
			LinkHandler.UI_THREAD_HANDLER.post(new NotifyErrorRunnable(t));
			return;
		}

		synchronized(mLock) {
			if(mWanted) {
				mResult = tile;
			} else if(tile != null) {
				tile.recycle();
			}
		}

		LinkHandler.UI_THREAD_HANDLER.post(mNotifyRunnable);
	}

	public Bitmap get() {

		synchronized(mLock) {

			if(!mWanted) {
				throw new RuntimeException("Attempted to get unwanted image!");
			}

			return mResult;
		}
	}

	// Caller must synchronize on mLock
	public void markAsUnwanted() {

		mWanted = false;

		if(mResult != null) {
			mResult.recycle();
			mResult = null;
		}
	}

	private class NotifyOOMRunnable implements Runnable {
		@Override
		public void run() {
			mListener.onTileLoaderOutOfMemory();
		}
	}

	private class NotifyErrorRunnable implements Runnable {

		private final Throwable mError;

		private NotifyErrorRunnable(Throwable mError) {
			this.mError = mError;
		}

		@Override
		public void run() {
			mListener.onTileLoaderException(mError);
		}
	}
}
