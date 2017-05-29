package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

public abstract class SRGLRenderableRenderHooks extends SRGLRenderable {

	private final SRGLRenderable mEntity;

	public SRGLRenderableRenderHooks(final SRGLRenderable entity) {
		this.mEntity = entity;
	}

	@Override
	protected void renderInternal(final SRGLMatrixStack stack, final long time) {
		preRender(stack, time);
		mEntity.startRender(stack, time);
		postRender(stack, time);
	}

	@Override
	public void onAdded() {
		mEntity.onAdded();
		super.onAdded();
	}

	@Override
	public void onRemoved() {
		super.onRemoved();
		mEntity.onRemoved();
	}

	@Override
	public boolean isAnimating() {
		return mEntity.isAnimating();
	}

	protected abstract void preRender(SRGLMatrixStack stack, final long time);
	protected abstract void postRender(SRGLMatrixStack stack, final long time);

	@Override
	public void setOverallAlpha(float alpha) {
		mEntity.setOverallAlpha(alpha);
	}
}
