package com.wellsandwhistles.android.redditsp.views.glview.program;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.opengl.GLES20;
import com.wellsandwhistles.android.redditsp.common.General;

import java.nio.FloatBuffer;

public final class SRGLContext {

	private final SRGLProgramTexture mProgramTexture;
	private final SRGLProgramColour mProgramColour;

	private float[] mPixelMatrix;
	private int mPixelMatrixOffset;

	private SRGLProgramVertices mProgramCurrent;

	private final Context mContext;

	public SRGLContext(Context context) {
		mProgramTexture = new SRGLProgramTexture();
		mProgramColour = new SRGLProgramColour();
		mContext = context;
	}

	public int dpToPixels(float dp) {
		return General.dpToPixels(mContext, dp);
	}

	public float getScreenDensity() {
		return mContext.getResources().getDisplayMetrics().density;
	}

	public void activateProgramColour() {
		if(mProgramCurrent != mProgramColour) {
			activateProgram(mProgramColour);
		}
	}

	public void activateProgramTexture() {
		if(mProgramCurrent != mProgramTexture) {
			activateProgram(mProgramTexture);
		}
	}

	private void activateProgram(final SRGLProgramVertices program) {

		if(mProgramCurrent != null) {
			mProgramCurrent.onDeactivated();
		}

		GLES20.glUseProgram(program.getHandle());
		mProgramCurrent = program;

		program.onActivated();

		if(mPixelMatrix != null) {
			program.activatePixelMatrix(mPixelMatrix, mPixelMatrixOffset);
		}
	}

	void activateTextureByHandle(final int textureHandle) {
		mProgramTexture.activateTextureByHandle(textureHandle);
	}

	public void activateVertexBuffer(FloatBuffer vertexBuffer) {
		mProgramCurrent.activateVertexBuffer(vertexBuffer);
	}

	public void activateColour(final float r, final float g, final float b, final float a) {
		mProgramColour.activateColour(r, g, b, a);
	}

	public void activateUVBuffer(FloatBuffer uvBuffer) {
		mProgramTexture.activateUVBuffer(uvBuffer);
	}

	public void drawTriangleStrip(int vertices) {
		mProgramCurrent.drawTriangleStrip(vertices);
	}

	public void activateMatrix(float[] buf, int offset) {
		mProgramCurrent.activateMatrix(buf, offset);
	}

	public void activatePixelMatrix(float[] buf, int offset) {

		mPixelMatrix = buf;
		mPixelMatrixOffset = offset;

		if(mProgramCurrent != null) {
			mProgramCurrent.activatePixelMatrix(buf, offset);
		}
	}

	public void setClearColor(float r, float g, float b, float a) {
		GLES20.glClearColor(r, g, b, a);
	}

	public void clear() {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
	}

	public void setViewport(int width, int height) {
		GLES20.glViewport(0, 0, width, height);
	}
}
