package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.MutableFloatPoint2D;

public class ImageViewScaleAnimation {

	private final float mStepSize;
	private final float mTargetScale;
	private final CoordinateHelper mCoordinateHelper;
	private final MutableFloatPoint2D mScreenCoord = new MutableFloatPoint2D();

	public ImageViewScaleAnimation(float targetScale, CoordinateHelper coordinateHelper, int stepCount, MutableFloatPoint2D screenCoord) {

		mTargetScale = targetScale;
		mCoordinateHelper = coordinateHelper;
		mStepSize = (float)Math.pow((targetScale / coordinateHelper.getScale()), (1.0 / (double)stepCount));
		mScreenCoord.set(screenCoord);
	}

	public boolean onStep() {

		mCoordinateHelper.scaleAboutScreenPoint(mScreenCoord, mStepSize);

		if(mStepSize > 1) {
			if(mTargetScale <= mCoordinateHelper.getScale()) {
				mCoordinateHelper.setScaleAboutScreenPoint(mScreenCoord, mTargetScale);
				return false;
			}

		} else {
			if(mTargetScale >= mCoordinateHelper.getScale()) {
				mCoordinateHelper.setScaleAboutScreenPoint(mScreenCoord, mTargetScale);
				return false;
			}
		}

		return true;
	}
}
