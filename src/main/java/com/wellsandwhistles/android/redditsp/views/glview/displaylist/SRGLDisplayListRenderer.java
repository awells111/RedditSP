package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.wellsandwhistles.android.redditsp.views.glview.SRGLSurfaceView;
import com.wellsandwhistles.android.redditsp.views.glview.Refreshable;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLContext;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;
import com.wellsandwhistles.android.redditsp.views.imageview.FingerTracker;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SRGLDisplayListRenderer implements GLSurfaceView.Renderer, Refreshable {

	public interface DisplayListManager extends FingerTracker.FingerListener {
		void onGLSceneCreate(SRGLDisplayList scene, SRGLContext context, Refreshable refreshable);

		void onGLSceneResolutionChange(SRGLDisplayList scene, SRGLContext context, int width, int height);

		boolean onGLSceneUpdate(SRGLDisplayList scene, SRGLContext context);

		void onUIAttach();

		void onUIDetach();
	}

	private final float[] mPixelMatrix = new float[16];

	private SRGLDisplayList mScene;
	private SRGLContext mGLContext;
	private SRGLMatrixStack mMatrixStack;

	private final DisplayListManager mDisplayListManager;
	private final SRGLSurfaceView mSurfaceView;

	public SRGLDisplayListRenderer(DisplayListManager displayListManager, SRGLSurfaceView surfaceView) {
		mDisplayListManager = displayListManager;
		mSurfaceView = surfaceView;
	}

	@Override
	public void onSurfaceCreated(GL10 ignore, EGLConfig config) {

		mGLContext = new SRGLContext(mSurfaceView.getContext());
		mMatrixStack = new SRGLMatrixStack(mGLContext);
		mScene = new SRGLDisplayList();

		mGLContext.setClearColor(0f, 0f, 0f, 1);

		mDisplayListManager.onGLSceneCreate(mScene, mGLContext, this);
	}

	@Override
	public void onSurfaceChanged(GL10 ignore, int width, int height) {

		mGLContext.setViewport(width, height);

		final float hScale = 2f / (float)width;
		final float vScale = -2f / (float)height;

		Matrix.setIdentityM(mPixelMatrix, 0);
		Matrix.translateM(mPixelMatrix, 0, -1, 1, 0);
		Matrix.scaleM(mPixelMatrix, 0, hScale, vScale, 1f);

		mDisplayListManager.onGLSceneResolutionChange(mScene, mGLContext, width, height);
	}

	private int frames = 0;
	private long startTime = -1;

	@Override
	public void onDrawFrame(GL10 ignore) {

		final long time = System.currentTimeMillis();

		if(startTime == -1) {
			startTime = time;
		}

		frames++;

		if(time - startTime >= 1000) {
			startTime = time;
			Log.i("FPS", "Frames: " + frames);
			frames = 0;
		}

		final boolean animating = mDisplayListManager.onGLSceneUpdate(mScene, mGLContext);

		mGLContext.clear();

		mGLContext.activatePixelMatrix(mPixelMatrix, 0);

		mMatrixStack.assertAtRoot();
		mScene.startRender(mMatrixStack, time);
		mMatrixStack.assertAtRoot();

		if(animating || mScene.isAnimating()) {
			mSurfaceView.requestRender();
		}
	}

	@Override
	public void refresh() {
		mSurfaceView.requestRender();
	}
}
