package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.TriggerableThread;
import com.wellsandwhistles.android.redditsp.common.collections.Stack;

public class ImageViewTileLoaderThread {

	private final InternalThread mThread = new InternalThread(new InternalRunnable(), 0);
	private final Stack<ImageViewTileLoader> mStack = new Stack<>(128);

	public void enqueue(ImageViewTileLoader tile) {

		synchronized(mStack) {
			mStack.push(tile);
			mThread.trigger();
		}
	}

	private class InternalRunnable implements Runnable {

		@Override
		public void run() {

			while(true) {

				final ImageViewTileLoader tile;

				synchronized(mStack) {

					if(mStack.isEmpty()) {
						return;
					}

					tile = mStack.pop();
				}

				tile.doPrepare();
			}

		}
	}

	private class InternalThread extends TriggerableThread {

		public InternalThread(Runnable task, long initialDelay) {
			super(task, initialDelay);
		}
	}
}
