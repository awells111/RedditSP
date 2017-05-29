package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public class LiveDHM {

	public static class Params {

		public float startPosition = 0;
		public float endPosition = 0;

		public float startVelocity = 0;

		public float accelerationCoefficient = 30;
		public float velocityDamping = 0.87f;

		public float stepLengthSeconds = 1f / 60f;

		public float thresholdPositionDifference = 0.49f;
		public float thresholdVelocity = 15;
		public int thresholdMaxSteps = 1000;
	}

	private final Params mParams;

	private int mStep = 0;

	private float mPosition;
	private float mVelocity;

	public LiveDHM(final Params params) {
		mParams = params;
		mPosition = params.startPosition;
		mVelocity = params.startVelocity;
	}

	public void calculateStep() {
		mVelocity -= mParams.stepLengthSeconds * ((mPosition - mParams.endPosition) * mParams.accelerationCoefficient);
		mVelocity *= mParams.velocityDamping;
		mPosition += mVelocity * mParams.stepLengthSeconds;
		mStep++;
	}

	public int getCurrentStep() {
		return mStep;
	}

	public float getCurrentPosition() {
		return mPosition;
	}

	public float getCurrentVelocity() {
		return mVelocity;
	}

	public Params getParams() {
		return mParams;
	}

	public boolean isEndThresholdReached() {

		if(mStep >= mParams.thresholdMaxSteps) {
			return true;
		}

		if(Math.abs(mPosition) > mParams.thresholdPositionDifference) {
			return false;
		}

		if(Math.abs(mVelocity) > mParams.thresholdVelocity) {
			return false;
		}

		return true;
	}
}
