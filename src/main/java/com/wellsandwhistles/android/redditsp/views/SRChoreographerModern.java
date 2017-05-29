package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.annotation.NonNull;
import android.view.Choreographer;

//todo delete suppresslint newapi's and targetapi's
//todo merge SRChoreographerModern and SRChoreographer since we got rid of the legacy one
public class SRChoreographerModern extends SRChoreographer implements Choreographer.FrameCallback {

	static final SRChoreographerModern INSTANCE = new SRChoreographerModern();

	private static final Choreographer CHOREOGRAPHER = Choreographer.getInstance();

	private final Callback[] mCallbacks = new Callback[128];
	private int mCallbackCount = 0;
	private boolean mPosted = false;

	private SRChoreographerModern() {}

	@Override
	public void postFrameCallback(@NonNull final Callback callback) {
		mCallbacks[mCallbackCount] = callback;
		mCallbackCount++;

		if(!mPosted) {
			CHOREOGRAPHER.postFrameCallback(this);
			mPosted = true;
		}
	}

	@Override
	public void doFrame(final long frameTimeNanos) {

		final int callbackCount = mCallbackCount;
		mPosted = false;
		mCallbackCount = 0;

		for(int i = 0; i < callbackCount; i++) {
			mCallbacks[i].doFrame(frameTimeNanos);
		}


	}
}
