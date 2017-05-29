package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.MutableFloatPoint2D;
import com.wellsandwhistles.android.redditsp.views.glview.displaylist.*;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLContext;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

public class ImageViewScrollbars extends SRGLRenderable {

	private static final float EPSILON = 0.0001f;

	private final SRGLRenderableBlend mRenderable;

	// Vertical scroll bar
	private final SRGLRenderableGroup mVScroll;
	private final SRGLRenderableTranslation mVScrollMarkerTranslation;
	private final SRGLRenderableScale mVScrollMarkerScale;
	private final SRGLRenderableTranslation mVScrollBarTranslation;
	private final SRGLRenderableScale mVScrollBarScale;
	private final SRGLRenderableTranslation mVScrollBorderTranslation;
	private final SRGLRenderableScale mVScrollBorderScale;

	// Horizontal scroll bar
	private final SRGLRenderableGroup mHScroll;
	private final SRGLRenderableTranslation mHScrollMarkerTranslation;
	private final SRGLRenderableScale mHScrollMarkerScale;
	private final SRGLRenderableTranslation mHScrollBarTranslation;
	private final SRGLRenderableScale mHScrollBarScale;
	private final SRGLRenderableTranslation mHScrollBorderTranslation;
	private final SRGLRenderableScale mHScrollBorderScale;

	private final CoordinateHelper mCoordinateHelper;

	private int mResX, mResY;
	private final int mImageResX, mImageResY;

	private final int mDimMarginSides;
	private final int mDimMarginEnds;
	private final int mDimBarWidth;
	private final int mDimBorderWidth;

	private long mShowUntil = -1;
	private float mCurrentAlpha = 1;
	private static final float ALPHA_STEP = 0.05f;
	private boolean mIsVisible = true;

	public ImageViewScrollbars(SRGLContext glContext, CoordinateHelper coordinateHelper, int imageResX, int imageResY) {

		mCoordinateHelper = coordinateHelper;
		mImageResX = imageResX;
		mImageResY = imageResY;

		final SRGLRenderableGroup group = new SRGLRenderableGroup();
		mRenderable = new SRGLRenderableBlend(group);

		mDimMarginSides = glContext.dpToPixels(10);
		mDimMarginEnds = glContext.dpToPixels(20);
		mDimBarWidth = glContext.dpToPixels(6);
		mDimBorderWidth = glContext.dpToPixels(1);

		// Vertical scroll bar
		{
			mVScroll = new SRGLRenderableGroup();
			group.add(mVScroll);

			final SRGLRenderableColouredQuad vScrollMarker = new SRGLRenderableColouredQuad(glContext);
			final SRGLRenderableColouredQuad vScrollBar = new SRGLRenderableColouredQuad(glContext);
			final SRGLRenderableColouredQuad vScrollBorder = new SRGLRenderableColouredQuad(glContext);

			vScrollMarker.setColour(1, 1, 1, 0.8f);
			vScrollBar.setColour(0, 0, 0, 0.5f);
			vScrollBorder.setColour(1, 1, 1, 0.5f);

			mVScrollMarkerScale = new SRGLRenderableScale(vScrollMarker);
			mVScrollBarScale = new SRGLRenderableScale(vScrollBar);
			mVScrollBorderScale = new SRGLRenderableScale(vScrollBorder);

			mVScrollMarkerTranslation = new SRGLRenderableTranslation(mVScrollMarkerScale);
			mVScrollBarTranslation = new SRGLRenderableTranslation(mVScrollBarScale);
			mVScrollBorderTranslation = new SRGLRenderableTranslation(mVScrollBorderScale);

			mVScroll.add(mVScrollBorderTranslation);
			mVScroll.add(mVScrollBarTranslation);
			mVScroll.add(mVScrollMarkerTranslation);
		}

		// Horizontal scroll bar
		{
			mHScroll = new SRGLRenderableGroup();
			group.add(mHScroll);

			final SRGLRenderableColouredQuad hScrollMarker = new SRGLRenderableColouredQuad(glContext);
			final SRGLRenderableColouredQuad hScrollBar = new SRGLRenderableColouredQuad(glContext);
			final SRGLRenderableColouredQuad hScrollBorder = new SRGLRenderableColouredQuad(glContext);

			hScrollMarker.setColour(1, 1, 1, 0.8f);
			hScrollBar.setColour(0, 0, 0, 0.5f);
			hScrollBorder.setColour(1, 1, 1, 0.5f);

			mHScrollMarkerScale = new SRGLRenderableScale(hScrollMarker);
			mHScrollBarScale = new SRGLRenderableScale(hScrollBar);
			mHScrollBorderScale = new SRGLRenderableScale(hScrollBorder);

			mHScrollMarkerTranslation = new SRGLRenderableTranslation(mHScrollMarkerScale);
			mHScrollBarTranslation = new SRGLRenderableTranslation(mHScrollBarScale);
			mHScrollBorderTranslation = new SRGLRenderableTranslation(mHScrollBorderScale);

			mHScroll.add(mHScrollBorderTranslation);
			mHScroll.add(mHScrollBarTranslation);
			mHScroll.add(mHScrollMarkerTranslation);
		}
	}

	public void update() {

		// TODO avoid GC

		final MutableFloatPoint2D tmp1 = new MutableFloatPoint2D();
		final MutableFloatPoint2D tmp2 = new MutableFloatPoint2D();

		mCoordinateHelper.convertScreenToScene(tmp1, tmp2);
		final float xStart = tmp2.x / (float)mImageResX;
		final float yStart = tmp2.y / (float)mImageResY;

		tmp1.set(mResX, mResY);

		mCoordinateHelper.convertScreenToScene(tmp1, tmp2);
		final float xEnd = tmp2.x / (float)mImageResX;
		final float yEnd = tmp2.y / (float)mImageResY;

		// Vertical scroll bar

		if(yStart < EPSILON && yEnd > 1-EPSILON) {
			mVScroll.hide();

		} else {
			mVScroll.show();

			final float vScrollTotalHeight = mResY - 2 * mDimMarginEnds;

			final float vScrollHeight = (yEnd - yStart) * vScrollTotalHeight;
			final float vScrollTop = yStart * vScrollTotalHeight + mDimMarginEnds;
			final float vScrollLeft = mResX - mDimBarWidth - mDimMarginSides;

			mVScrollBorderTranslation.setPosition(vScrollLeft - mDimBorderWidth, mDimMarginEnds - mDimBorderWidth);
			mVScrollBorderScale.setScale(mDimBarWidth + 2 * mDimBorderWidth, vScrollTotalHeight + 2 * mDimBorderWidth);

			mVScrollBarTranslation.setPosition(vScrollLeft, mDimMarginEnds);
			mVScrollBarScale.setScale(mDimBarWidth, vScrollTotalHeight);

			mVScrollMarkerTranslation.setPosition(vScrollLeft, vScrollTop);
			mVScrollMarkerScale.setScale(mDimBarWidth, vScrollHeight);
		}

		// Horizontal scroll bar

		if(xStart < EPSILON && xEnd > 1-EPSILON) {
			mHScroll.hide();

		} else {
			mHScroll.show();

			final float hScrollTotalWidth = mResX - 2 * mDimMarginEnds;

			final float hScrollWidth = (xEnd - xStart) * hScrollTotalWidth;
			final float hScrollLeft = xStart * hScrollTotalWidth + mDimMarginEnds;
			final float hScrollTop = mResY - mDimBarWidth - mDimMarginSides;

			mHScrollBorderTranslation.setPosition(mDimMarginEnds - mDimBorderWidth, hScrollTop - mDimBorderWidth);
			mHScrollBorderScale.setScale(hScrollTotalWidth + 2 * mDimBorderWidth, mDimBarWidth + mDimBorderWidth * 2);

			mHScrollBarTranslation.setPosition(mDimMarginEnds, hScrollTop);
			mHScrollBarScale.setScale(hScrollTotalWidth, mDimBarWidth);

			mHScrollMarkerTranslation.setPosition(hScrollLeft, hScrollTop);
			mHScrollMarkerScale.setScale(hScrollWidth, mDimBarWidth);
		}
	}

	public synchronized void setResolution(int x, int y) {
		mResX = x;
		mResY = y;
	}

	@Override
	public void onAdded() {
		super.onAdded();
		mRenderable.onAdded();
	}

	@Override
	public void onRemoved() {
		mRenderable.onRemoved();
		super.onRemoved();
	}

	@Override
	public synchronized boolean isAnimating() {
		return mIsVisible;
	}

	public synchronized void showBars() {
		mShowUntil = System.currentTimeMillis() + 600;
		mIsVisible = true;
		mCurrentAlpha = 1;
	}

	@Override
	protected synchronized void renderInternal(SRGLMatrixStack stack, long time) {

		if(mIsVisible && time > mShowUntil) {
			mCurrentAlpha -= ALPHA_STEP;

			if(mCurrentAlpha < 0) {
				mIsVisible = false;
				mCurrentAlpha = 0;
			}
		}

		mRenderable.setOverallAlpha(mCurrentAlpha);

		mRenderable.startRender(stack, time);
	}
}
