package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.view.MotionEvent;

import com.wellsandwhistles.android.redditsp.common.MutableFloatPoint2D;

public class FingerTracker {

	public interface FingerListener {
		void onFingerDown(Finger finger);

		void onFingersMoved();

		void onFingerUp(Finger finger);
	}

	private final Finger[] mFingers = new Finger[10];
	private final FingerListener mListener;

	public FingerTracker(FingerListener mListener) {

		this.mListener = mListener;

		for(int i = 0; i < mFingers.length; i++) {
			mFingers[i] = new Finger();
		}
	}

	public void onTouchEvent(MotionEvent event) {

		switch(event.getActionMasked()) {

			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:

				for(final Finger f : mFingers) {
					if(!f.mActive) {
						f.onDown(event);
						mListener.onFingerDown(f);
						break;
					}
				}

				break;

			case MotionEvent.ACTION_MOVE:

				for(Finger finger : mFingers) {
					if(finger.mActive) {
						finger.onMove(event);
					}
				}

				mListener.onFingersMoved();

				break;

			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:

				final int id = event.getPointerId(event.getActionIndex());

				for(final Finger f : mFingers) {
					if(f.mActive && f.mAndroidId == id) {
						f.onUp(event);
						mListener.onFingerUp(f);
						break;
					}
				}

				break;
		}
	}

	public class Finger {

		boolean mActive = false;

		int mAndroidId;

		final MutableFloatPoint2D
				mStartPos = new MutableFloatPoint2D(),
				mCurrentPos = new MutableFloatPoint2D(),
				mLastPos = new MutableFloatPoint2D(),
				mPosDifference = new MutableFloatPoint2D(),
				mTotalPosDifference = new MutableFloatPoint2D();

		long mDownStartTime, mDownDuration;

		public void onDown(final MotionEvent event) {
			final int index = event.getActionIndex();
			mActive = true;
			mAndroidId = event.getPointerId(index);
			mCurrentPos.set(event, index);
			mLastPos.set(mCurrentPos);
			mStartPos.set(mCurrentPos);
			mPosDifference.reset();
			mTotalPosDifference.reset();
			mDownStartTime = event.getDownTime();
			mDownDuration = 0;
		}

		public void onMove(final MotionEvent event) {
			final int index = event.findPointerIndex(mAndroidId);
			if(index >= 0) {
				mLastPos.set(mCurrentPos);
				mCurrentPos.set(event, index);
				mCurrentPos.sub(mLastPos, mPosDifference);
				mCurrentPos.sub(mStartPos, mTotalPosDifference);
				mDownDuration = event.getEventTime() - mDownStartTime;
			}
		}

		public void onUp(final MotionEvent event) {

			mLastPos.set(mCurrentPos);
			mCurrentPos.set(event, event.getActionIndex());
			mCurrentPos.sub(mLastPos, mPosDifference);
			mCurrentPos.sub(mStartPos, mTotalPosDifference);
			mDownDuration = event.getEventTime() - mDownStartTime;

			mActive = false;
		}
	}
}
