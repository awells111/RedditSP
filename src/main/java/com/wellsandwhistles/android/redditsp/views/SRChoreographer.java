package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.annotation.NonNull;

public abstract class SRChoreographer {

	public interface Callback {
		void doFrame(long frameTimeNanos);
	}

	@NonNull
	public static SRChoreographer getInstance() {
		return SRChoreographerModern.INSTANCE;
	}

	public abstract void postFrameCallback(@NonNull final Callback callback);
}
