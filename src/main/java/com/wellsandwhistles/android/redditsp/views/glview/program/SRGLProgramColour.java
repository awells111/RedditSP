package com.wellsandwhistles.android.redditsp.views.glview.program;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.opengl.GLES20;

public class SRGLProgramColour extends SRGLProgramVertices {

	private final int mColorHandle;

	public SRGLProgramColour() {

		super(vertexShaderSource, fragmentShaderSource);

		setVertexBufferHandle(getAttributeHandle("a_Position"));
		setMatrixUniformHandle(getUniformHandle("u_Matrix"));
		setPixelMatrixHandle(getUniformHandle("u_PixelMatrix"));

		mColorHandle = getUniformHandle("u_Color");
	}

	public void activateColour(final float r, final float g, final float b, final float a) {
		GLES20.glUniform4f(mColorHandle, r, g, b, a);
	}

	@Override
	public void onActivated() {
		super.onActivated();
		GLES20.glEnableVertexAttribArray(mColorHandle);
	}

	@Override
	public void onDeactivated() {
		super.onDeactivated();
		GLES20.glDisableVertexAttribArray(mColorHandle);
	}

	private static final String vertexShaderSource =
			"uniform mat4 u_Matrix; \n"
					+ "uniform mat4 u_PixelMatrix; \n"
					+ "attribute vec4 a_Position; \n"
					+ "attribute vec2 a_TexCoordinate; \n"
					+ "varying vec2 v_TexCoordinate; \n"
					+ "void main() {\n"
					+ "  v_TexCoordinate = a_TexCoordinate; \n"
					+ "  gl_Position = u_PixelMatrix * (u_Matrix * a_Position);\n"
					+ "} \n";

	private static final String fragmentShaderSource =
			"precision mediump float; \n"
					+ "uniform vec4 u_Color; \n"
					+ "void main() { \n"
					+ "  gl_FragColor = u_Color; \n"
					+ "} \n";
}
