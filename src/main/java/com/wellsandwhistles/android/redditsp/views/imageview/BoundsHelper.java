package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.MutableFloatPoint2D;

public class BoundsHelper {

	private final int mResolutionX, mResolutionY;
	private final int mImageResolutionX, mImageResolutionY;
	private final CoordinateHelper mCoordinateHelper;

	private final float mMinScale;

	public BoundsHelper(
			int resolutionX, int resolutionY,
			int imageResolutionX, int imageResolutionY,
			CoordinateHelper coordinateHelper) {

		mResolutionX = resolutionX;
		mResolutionY = resolutionY;
		mImageResolutionX = imageResolutionX;
		mImageResolutionY = imageResolutionY;
		mCoordinateHelper = coordinateHelper;

		mMinScale = Math.min(
				(float) mResolutionX / (float) mImageResolutionX,
				(float) mResolutionY / (float) mImageResolutionY
		);
	}

	public void applyMinScale() {
		mCoordinateHelper.setScale(mMinScale);
	}

	public boolean isMinScale() {
		return mCoordinateHelper.getScale() - 0.000001f <= mMinScale;
	}

	public void applyBounds() {

		if(mCoordinateHelper.getScale() < mMinScale) {
			applyMinScale();
		}

		final float scale = mCoordinateHelper.getScale();
		final MutableFloatPoint2D posOffset = mCoordinateHelper.getPositionOffset();

		final float scaledImageWidth = (float)mImageResolutionX * scale;
		final float scaledImageHeight = (float)mImageResolutionY * scale;

		if(scaledImageWidth <= mResolutionX) {
			posOffset.x = (mResolutionX - scaledImageWidth) / 2;

		} else if(posOffset.x > 0) {
			posOffset.x = 0;
		} else if(posOffset.x < mResolutionX - scaledImageWidth) {
			posOffset.x = mResolutionX - scaledImageWidth;
		}

		if(scaledImageHeight <= mResolutionY) {
			posOffset.y = (mResolutionY - scaledImageHeight) / 2;

		} else if(posOffset.y > 0) {
			posOffset.y = 0;
		} else if(posOffset.y < mResolutionY - scaledImageHeight) {
			posOffset.y = mResolutionY - scaledImageHeight;
		}
	}

	public float getMinScale() {
		return mMinScale;
	}
}
