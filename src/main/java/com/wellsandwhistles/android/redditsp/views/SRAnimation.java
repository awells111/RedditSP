package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public abstract class SRAnimation implements SRChoreographer.Callback {

	private static final SRChoreographer CHOREOGRAPHER = SRChoreographer.getInstance();

	private long mFirstFrameNanos = -1;

	private boolean mStarted = false;
	private boolean mStopped = false;

	public final void start() {

		if(mStarted) {
			throw new RuntimeException("Attempted to start animation twice!");
		}

		mStarted = true;

		CHOREOGRAPHER.postFrameCallback(this);
	}

	public final void stop() {

		if(!mStarted) {
			throw new RuntimeException("Attempted to stop animation before it's started!");
		}

		if(mStopped) {
			throw new RuntimeException("Attempted to stop animation twice!");
		}

		mStopped = true;
	}

	// Return true to continue animating
	protected abstract boolean handleFrame(final long nanosSinceAnimationStart);

	@Override
	public final void doFrame(final long frameTimeNanos) {

		if(mStopped) {
			return;
		}

		if(mFirstFrameNanos == -1) {
			mFirstFrameNanos = frameTimeNanos;
		}

		if(handleFrame(frameTimeNanos - mFirstFrameNanos)) {
			CHOREOGRAPHER.postFrameCallback(this);
		}
	}
}
