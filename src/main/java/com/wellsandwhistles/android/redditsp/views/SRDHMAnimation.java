package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public abstract class SRDHMAnimation extends SRAnimation {

	private final LiveDHM mDHM;

	public SRDHMAnimation(final LiveDHM.Params params) {
		mDHM = new LiveDHM(params);
	}

	@Override
	protected boolean handleFrame(final long nanosSinceAnimationStart) {

		final long microsSinceAnimationStart = nanosSinceAnimationStart / 1000;
		final long stepLengthMicros = (long)(mDHM.getParams().stepLengthSeconds * 1000.0 * 1000.0);

		final int desiredStepNumber = (int)((microsSinceAnimationStart + (stepLengthMicros / 2)) / stepLengthMicros);

		while(mDHM.getCurrentStep() < desiredStepNumber) {

			mDHM.calculateStep();

			if(mDHM.isEndThresholdReached()) {
				onEndPosition(mDHM.getParams().endPosition);
				return false;
			}
		}

		onUpdatedPosition(mDHM.getCurrentPosition());
		return true;
	}

	public final float getCurrentVelocity() {
		return mDHM.getCurrentVelocity();
	}

	protected abstract void onUpdatedPosition(float position);

	protected abstract void onEndPosition(float endPosition);
}
