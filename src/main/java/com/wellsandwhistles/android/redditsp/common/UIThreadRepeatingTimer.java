package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;

public class UIThreadRepeatingTimer implements Runnable {

	public interface Listener {
		void onUIThreadRepeatingTimer(UIThreadRepeatingTimer timer);
	}

	private final Handler mHandler = new Handler(Looper.getMainLooper());

	private final long mIntervalMs;
	private final Listener mListener;

	private boolean mShouldTimerRun = false;

	public UIThreadRepeatingTimer(long mIntervalMs, Listener mListener) {
		this.mIntervalMs = mIntervalMs;
		this.mListener = mListener;
	}

	@UiThread
	public void startTimer() {

		General.checkThisIsUIThread();

		mShouldTimerRun = true;
		mHandler.postDelayed(this, mIntervalMs);
	}

	@UiThread
	public void stopTimer() {

		General.checkThisIsUIThread();

		mShouldTimerRun = false;
	}


	@Override
	public void run() {

		if(mShouldTimerRun) {

			mListener.onUIThreadRepeatingTimer(this);

			if(mShouldTimerRun) {
				mHandler.postDelayed(this, mIntervalMs);
			}
		}
	}
}
