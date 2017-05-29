package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.common.MutableFloatPoint2D;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

public class SRGLRenderableTranslation extends SRGLRenderableRenderHooks {

	private float mPositionX, mPositionY;

	public SRGLRenderableTranslation(final SRGLRenderable entity) {
		super(entity);

	}

	public void setPosition(float x, float y) {
		mPositionX = x;
		mPositionY = y;
	}

	@Override
	protected void preRender(final SRGLMatrixStack stack, final long time) {
		stack.pushAndTranslate(mPositionX, mPositionY);
	}

	@Override
	protected void postRender(final SRGLMatrixStack stack, final long time) {
		stack.pop();
	}

	public void setPosition(MutableFloatPoint2D mPositionOffset) {
		mPositionX = mPositionOffset.x;
		mPositionY = mPositionOffset.y;
	}
}
