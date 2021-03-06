package com.wellsandwhistles.android.redditsp.views.glview.program;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class SRGLProgramTexture extends SRGLProgramVertices {

	private final int mUVDataHandle;
	private final int mTextureUniformHandle;

	public SRGLProgramTexture() {

		super(vertexShaderSource, fragmentShaderSource);

		setVertexBufferHandle(getAttributeHandle("a_Position"));
		setMatrixUniformHandle(getUniformHandle("u_Matrix"));
		setPixelMatrixHandle(getUniformHandle("u_PixelMatrix"));

		mUVDataHandle = getAttributeHandle("a_TexCoordinate");
		mTextureUniformHandle = getUniformHandle("u_Texture");
	}

	public void activateTextureByHandle(final int textureHandle) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
		GLES20.glUniform1i(mTextureUniformHandle, 0);
	}

	public void activateUVBuffer(FloatBuffer uvBuffer) {
		GLES20.glVertexAttribPointer(mUVDataHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);
	}

	@Override
	public void onActivated() {
		super.onActivated();
		GLES20.glEnableVertexAttribArray(mUVDataHandle);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	}

	@Override
	public void onDeactivated() {
		super.onDeactivated();
		GLES20.glDisableVertexAttribArray(mUVDataHandle);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
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
					+ "uniform sampler2D u_Texture; \n"
					+ "varying vec2 v_TexCoordinate; \n"
					+ "void main() { \n"
					+ "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate); \n"
					+ "} \n";
}
