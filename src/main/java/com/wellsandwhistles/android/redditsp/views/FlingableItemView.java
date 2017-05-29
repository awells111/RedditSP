package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.General;

public abstract class FlingableItemView extends SwipableItemView {

	private final FrameLayout mFlingHintOuter;

	private final TextView mFlingHintLeft;
	private final TextView mFlingHintRight;

	private boolean mSwipeReady = false;
	private boolean mRightFlingHintShown = false;
	private boolean mLeftFlingHintShown = false;

	private FlingHintAnimation mFlingHintAnimation;
	private float mFlingHintYPos = 0;

	private final int mOffsetBeginAllowed;
	private final int mOffsetActionPerformed;

	private final Drawable srIconFfLeft, srIconFfRight, srIconTick;

	public FlingableItemView(@NonNull final Context context) {

		super(context);

		mOffsetBeginAllowed = General.dpToPixels(context, 50);
		mOffsetActionPerformed = General.dpToPixels(context, 150);

		final int srListBackgroundCol;

		{
			final TypedArray attr = context.obtainStyledAttributes(new int[]{
					R.attr.srIconFfLeft,
					R.attr.srIconFfRight,
					R.attr.srIconTick,
					R.attr.srListBackgroundCol
			});

			srIconFfLeft = attr.getDrawable(0);
			srIconFfRight = attr.getDrawable(1);
			srIconTick = attr.getDrawable(2);
			srListBackgroundCol = attr.getColor(3, General.COLOR_INVALID);

			attr.recycle();
		}

		mFlingHintOuter = (FrameLayout)LayoutInflater.from(context).inflate(R.layout.fling_hint, null);

		addView(mFlingHintOuter);
		final ViewGroup.LayoutParams flingHintLayoutParams = mFlingHintOuter.getLayoutParams();
		flingHintLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		flingHintLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		mFlingHintLeft = (TextView)mFlingHintOuter.findViewById(R.id.reddit_post_fling_text_left);
		mFlingHintRight = (TextView)mFlingHintOuter.findViewById(R.id.reddit_post_fling_text_right);

		mFlingHintLeft.setCompoundDrawablesWithIntrinsicBounds(null, srIconFfLeft, null, null);
		mFlingHintRight.setCompoundDrawablesWithIntrinsicBounds(null, srIconFfRight, null, null);

		setBackgroundColor(srListBackgroundCol);
	}

	public void setFlingingEnabled(final boolean flingingEnabled) {
		mFlingHintOuter.setVisibility(flingingEnabled ? VISIBLE : GONE);
		setSwipingEnabled(flingingEnabled);
	}

	protected abstract void onSetItemFlingPosition(final float position);

	@NonNull protected abstract String getFlingLeftText();
	@NonNull protected abstract String getFlingRightText();

	protected abstract boolean allowFlingingLeft();
	protected abstract boolean allowFlingingRight();

	protected abstract void onFlungLeft();
	protected abstract void onFlungRight();

	@Override
	protected final boolean allowSwipingLeft() {
		return allowFlingingLeft();
	}

	@Override
	protected final boolean allowSwipingRight() {
		return allowFlingingRight();
	}

	private void updateFlingHintPosition() {
		mFlingHintOuter.setTranslationY(mFlingHintYPos);
	}

	private class FlingHintAnimation extends SRDHMAnimation {

		private FlingHintAnimation(final LiveDHM.Params params) {
			super(params);
		}

		@Override
		protected void onUpdatedPosition(final float position) {
			mFlingHintYPos = position;
			updateFlingHintPosition();
		}

		@Override
		protected void onEndPosition(final float endPosition) {
			mFlingHintYPos = endPosition;
			updateFlingHintPosition();
			mFlingHintAnimation = null;
		}
	}

	@Override
	protected void onSwipeFingerDown(final int x, final int y, final float xOffsetPixels, final boolean wasOldSwipeInterrupted) {

		if(mOffsetBeginAllowed > Math.abs(xOffsetPixels)) {

			mFlingHintLeft.setText(getFlingLeftText());
			mFlingHintRight.setText(getFlingRightText());

			mFlingHintLeft.setCompoundDrawablesWithIntrinsicBounds(null, srIconFfLeft, null, null);
			mFlingHintRight.setCompoundDrawablesWithIntrinsicBounds(null, srIconFfRight, null, null);

			mSwipeReady = true;
		}

		final int height = mFlingHintOuter.getMeasuredHeight();
		final int parentHeight = getMeasuredHeight();

		final FlingHintAnimation oldAnimation = mFlingHintAnimation;

		if(mFlingHintAnimation != null) {
			mFlingHintAnimation.stop();
			mFlingHintAnimation = null;
		}

		if(parentHeight > height * 3) {

			int yPos = Math.min(Math.max(y - height / 2, 0), parentHeight - height);

			if(wasOldSwipeInterrupted) {
				if(Math.abs(yPos - mFlingHintYPos) < height) {
					yPos = (int)mFlingHintYPos;
				}

				final LiveDHM.Params params = new LiveDHM.Params();
				params.startPosition = mFlingHintYPos;
				params.endPosition = yPos;

				if(oldAnimation != null) {
					params.startVelocity = oldAnimation.getCurrentVelocity();
				}

				mFlingHintAnimation = new FlingHintAnimation(params);
				mFlingHintAnimation.start();

			} else {
				mFlingHintYPos = yPos;
				updateFlingHintPosition();
			}

		} else {
			mFlingHintYPos = (parentHeight - height) / 2;
			updateFlingHintPosition();
		}
	}

	@Override
	public void onSwipeDeltaChanged(final float xOffsetPixels) {

		onSetItemFlingPosition(xOffsetPixels);

		final float absOffset = Math.abs(xOffsetPixels);

		if(mSwipeReady && absOffset > mOffsetActionPerformed) {

			if(xOffsetPixels > 0) {
				onFlungRight();
				mFlingHintRight.setCompoundDrawablesWithIntrinsicBounds(null, srIconTick, null, null);

			} else {
				onFlungLeft();
				mFlingHintLeft.setCompoundDrawablesWithIntrinsicBounds(null, srIconTick, null, null);
			}

			mSwipeReady = false;

		} else if(absOffset > 5) {

			if(xOffsetPixels > 0) {

				// Right swipe

				if(!mRightFlingHintShown) {
					mRightFlingHintShown = true;
					mFlingHintRight.setVisibility(View.VISIBLE);
				}

				if(mLeftFlingHintShown) {
					mLeftFlingHintShown = false;
					mFlingHintLeft.setVisibility(View.INVISIBLE);
				}

			} else {

				// Left swipe

				if(!mLeftFlingHintShown) {
					mLeftFlingHintShown = true;
					mFlingHintLeft.setVisibility(View.VISIBLE);
				}

				if(mRightFlingHintShown) {
					mRightFlingHintShown = false;
					mFlingHintRight.setVisibility(View.INVISIBLE);
				}
			}

		} else {

			if(mRightFlingHintShown) {
				mRightFlingHintShown = false;
				mFlingHintRight.setVisibility(View.INVISIBLE);
			}

			if(mLeftFlingHintShown) {
				mLeftFlingHintShown = false;
				mFlingHintLeft.setVisibility(View.INVISIBLE);
			}
		}
	}
}
