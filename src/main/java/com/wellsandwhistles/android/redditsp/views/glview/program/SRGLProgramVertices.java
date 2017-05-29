package com.wellsandwhistles.android.redditsp.views.glview.program;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public abstract class SRGLProgramVertices extends SRGLProgram {

	private int mVertexBufferHandle;
	private int mMatrixUniformHandle;
	private int mPixelMatrixUniformHandle;

	public SRGLProgramVertices(String vertexShaderSource, String fragmentShaderSource) {
		super(vertexShaderSource, fragmentShaderSource);
	}

	public final void activateVertexBuffer(final FloatBuffer vertexBuffer) {
		GLES20.glVertexAttribPointer(mVertexBufferHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
	}

	public final void drawTriangleStrip(int vertices) {
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertices);
	}

	protected final void setVertexBufferHandle(int handle) {
		mVertexBufferHandle = handle;
	}

	protected final void setMatrixUniformHandle(int handle) {
		mMatrixUniformHandle = handle;
	}

	protected final void setPixelMatrixHandle(int handle) {
		mPixelMatrixUniformHandle = handle;
	}

	public final void activateMatrix(float[] buf, int offset) {
		GLES20.glUniformMatrix4fv(mMatrixUniformHandle, 1, false, buf, offset);
	}

	public final void activatePixelMatrix(float[] buf, int offset) {
		GLES20.glUniformMatrix4fv(mPixelMatrixUniformHandle, 1, false, buf, offset);
	}

	@Override
	public void onActivated() {
		GLES20.glEnableVertexAttribArray(mVertexBufferHandle);
	}

	@Override
	public void onDeactivated() {
		GLES20.glDisableVertexAttribArray(mVertexBufferHandle);
	}
}
