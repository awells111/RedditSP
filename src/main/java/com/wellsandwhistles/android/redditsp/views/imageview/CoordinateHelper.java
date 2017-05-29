package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.MutableFloatPoint2D;

public class CoordinateHelper {

	private float mScale = 1.0f;
	private final MutableFloatPoint2D mPositionOffset = new MutableFloatPoint2D();

	public void setScale(final float scale) {
		mScale = scale;
	}

	public float getScale() {
		return mScale;
	}

	public MutableFloatPoint2D getPositionOffset() {
		return mPositionOffset;
	}

	public void getPositionOffset(MutableFloatPoint2D result) {
		result.set(mPositionOffset);
	}

	public void convertScreenToScene(final MutableFloatPoint2D screenPos, final MutableFloatPoint2D output) {
		output.x = (screenPos.x - mPositionOffset.x) / mScale;
		output.y = (screenPos.y - mPositionOffset.y) / mScale;
	}

	public void convertSceneToScreen(final MutableFloatPoint2D scenePos, final MutableFloatPoint2D output) {
		output.x = scenePos.x * mScale + mPositionOffset.x;
		output.y = scenePos.y * mScale + mPositionOffset.y;
	}

	public void scaleAboutScreenPoint(final MutableFloatPoint2D screenPos, final float scaleFactor) {
		setScaleAboutScreenPoint(screenPos, mScale * scaleFactor);
	}

	public void setScaleAboutScreenPoint(final MutableFloatPoint2D screenPos, final float scale) {

		final MutableFloatPoint2D oldScenePos = new MutableFloatPoint2D();
		convertScreenToScene(screenPos, oldScenePos);

		mScale = scale;

		final MutableFloatPoint2D newScreenPos = new MutableFloatPoint2D();
		convertSceneToScreen(oldScenePos, newScreenPos);

		translateScreen(newScreenPos, screenPos);
	}

	public void translateScreen(final MutableFloatPoint2D oldScreenPos, final MutableFloatPoint2D newScreenPos) {
		mPositionOffset.add(newScreenPos);
		mPositionOffset.sub(oldScreenPos);
	}
}
