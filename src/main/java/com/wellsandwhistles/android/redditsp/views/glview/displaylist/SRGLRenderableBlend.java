package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.opengl.GLES20;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

public class SRGLRenderableBlend extends SRGLRenderableRenderHooks {

	public SRGLRenderableBlend(SRGLRenderable entity) {
		super(entity);
	}

	@Override
	protected void preRender(SRGLMatrixStack stack, long time) {
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	protected void postRender(SRGLMatrixStack stack, long time) {
		GLES20.glDisable(GLES20.GL_BLEND);
	}
}
