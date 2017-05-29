package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

public class SRGLRenderableScale extends SRGLRenderableRenderHooks {

	private float mScaleX = 1, mScaleY = 1;

	public SRGLRenderableScale(final SRGLRenderable entity) {
		super(entity);
	}

	public void setScale(float x, float y) {
		mScaleX = x;
		mScaleY = y;
	}

	@Override
	protected void preRender(final SRGLMatrixStack stack, final long time) {
		stack.pushAndScale(mScaleX, mScaleY);
	}

	@Override
	protected void postRender(final SRGLMatrixStack stack, final long time) {
		stack.pop();
	}
}
