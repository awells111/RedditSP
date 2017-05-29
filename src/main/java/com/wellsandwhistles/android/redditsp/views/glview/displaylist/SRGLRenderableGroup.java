package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

import java.util.ArrayList;

public class SRGLRenderableGroup extends SRGLRenderable {

	private final ArrayList<SRGLRenderable> mChildren = new ArrayList<>(16);

	public final void add(SRGLRenderable child) {
		mChildren.add(child);
		if(isAdded()) child.onAdded();
	}

	public final void remove(SRGLRenderable child) {
		if(isAdded()) child.onRemoved();
		mChildren.remove(child);
	}

	@Override
	public void onAdded() {

		if(!isAdded()) {
			for(SRGLRenderable entity : mChildren) {
				entity.onAdded();
			}
		}

		super.onAdded();
	}

	@Override
	protected void renderInternal(final SRGLMatrixStack matrixStack, final long time) {
		for(int i = 0; i < mChildren.size(); i++) {
			SRGLRenderable entity = mChildren.get(i);
			entity.startRender(matrixStack, time);
		}
	}

	@Override
	public void onRemoved() {

		super.onRemoved();

		if(!isAdded()) {
			for(SRGLRenderable entity : mChildren) entity.onRemoved();
		}
	}

	@Override
	public boolean isAnimating() {
		for(int i = 0; i < mChildren.size(); i++) {
			SRGLRenderable entity = mChildren.get(i);
			if(entity.isAnimating()) return true;
		}
		return false;
	}

	@Override
	public void setOverallAlpha(float alpha) {
		for(int i = 0; i < mChildren.size(); i++) {
			SRGLRenderable entity = mChildren.get(i);
			entity.setOverallAlpha(alpha);
		}
	}
}
