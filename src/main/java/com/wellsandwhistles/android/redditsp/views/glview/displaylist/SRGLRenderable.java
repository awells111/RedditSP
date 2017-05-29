package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

public abstract class SRGLRenderable {

	private boolean mVisible = true;
	private int mAttachmentCount = 0;

	public final void hide() {
		mVisible = false;
	}

	public final void show() {
		mVisible = true;
	}

	public final boolean isVisible() {
		return mVisible;
	}

	public final void startRender(final SRGLMatrixStack stack, final long time) {
		if(mVisible) renderInternal(stack, time);
	}

	public void onAdded() {
		mAttachmentCount++;
	}

	public boolean isAdded() {
		return mAttachmentCount > 0;
	}

	protected abstract void renderInternal(SRGLMatrixStack stack, final long time);

	public void onRemoved() {
		mAttachmentCount--;
	}

	public boolean isAnimating() {
		return false;
	}

	public void setOverallAlpha(float alpha) {
		throw new UnsupportedOperationException();
	}
}
