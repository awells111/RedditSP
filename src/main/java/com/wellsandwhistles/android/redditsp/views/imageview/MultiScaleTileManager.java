package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.Bitmap;

public class MultiScaleTileManager {

	public static final int MAX_SAMPLE_SIZE = 32;

	private final ImageViewTileLoader[] mTileLoaders;

	private int mDesiredScaleIndex = -1;

	private final Object mLock = new Object();

	public static int scaleIndexToSampleSize(int scaleIndex) {
		return 1 << scaleIndex;
	}

	public static int sampleSizeToScaleIndex(int sampleSize) {
		return Integer.numberOfTrailingZeros(sampleSize);
	}

	public MultiScaleTileManager(
			final ImageTileSource imageTileSource,
			final ImageViewTileLoaderThread thread,
			final int x,
			final int y,
			final ImageViewTileLoader.Listener listener) {

		mTileLoaders = new ImageViewTileLoader[sampleSizeToScaleIndex(MAX_SAMPLE_SIZE) + 1];

		for(int s = 0; s < mTileLoaders.length; s++) {
			mTileLoaders[s] = new ImageViewTileLoader(imageTileSource, thread, x, y, scaleIndexToSampleSize(s), listener, mLock);
		}
	}

	public Bitmap getAtDesiredScale() {
		return mTileLoaders[mDesiredScaleIndex].get();
	}

	public void markAsWanted(int desiredScaleIndex) {

		if(desiredScaleIndex == mDesiredScaleIndex) {
			return;
		}

		mDesiredScaleIndex = desiredScaleIndex;

		synchronized(mLock) {

			mTileLoaders[desiredScaleIndex].markAsWanted();

			for(int s = 0; s < mTileLoaders.length; s++) {
				if(s != desiredScaleIndex) {
					mTileLoaders[s].markAsUnwanted();
				}
			}
		}
	}

	public void markAsUnwanted() {

		if(mDesiredScaleIndex == -1) {
			return;
		}

		mDesiredScaleIndex = -1;

		synchronized(mLock) {
			for(int s = 0; s < mTileLoaders.length; s++) {
				mTileLoaders[s].markAsUnwanted();
			}
		}
	}
}
