package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

public final class SRGLDisplayList extends SRGLRenderableGroup {

	@Override
	protected void renderInternal(SRGLMatrixStack matrixStack, final long time) {
		super.renderInternal(matrixStack, time);
	}

	@Override
	public boolean isAdded() {
		return true;
	}
}
