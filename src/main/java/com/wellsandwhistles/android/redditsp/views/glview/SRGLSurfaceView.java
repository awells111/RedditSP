package com.wellsandwhistles.android.redditsp.views.glview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import com.wellsandwhistles.android.redditsp.views.glview.displaylist.SRGLDisplayListRenderer;
import com.wellsandwhistles.android.redditsp.views.imageview.FingerTracker;

public class SRGLSurfaceView extends GLSurfaceView {

	private final FingerTracker mFingerTracker;
	private final SRGLDisplayListRenderer.DisplayListManager mDisplayListManager;

	public SRGLSurfaceView(Context context, SRGLDisplayListRenderer.DisplayListManager displayListManager) {
		super(context);

		setEGLContextClientVersion(2);
		setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		setRenderer(new SRGLDisplayListRenderer(displayListManager, this));
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		mFingerTracker = new FingerTracker(displayListManager);
		mDisplayListManager = displayListManager;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mFingerTracker.onTouchEvent(event);
		requestRender();
		return true;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mDisplayListManager.onUIAttach();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mDisplayListManager.onUIDetach();
	}
}
