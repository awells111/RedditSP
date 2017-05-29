package com.wellsandwhistles.android.redditsp.views.glview.program;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class SRGLTexture {

	private final int mTextureHandle;
	private final SRGLContext mGLContext;
	private int mRefCount = 1;

	public SRGLTexture(SRGLContext glContext, Bitmap bitmap) {
		mTextureHandle = loadTexture(bitmap);
		mGLContext = glContext;
	}

	public void addReference() {
		mRefCount++;
	}

	public void releaseReference() {
		mRefCount--;
		if(mRefCount == 0) {
			deleteTexture(mTextureHandle);
		}
	}

	public void activate() {
		mGLContext.activateTextureByHandle(mTextureHandle);
	}

	private static int loadTexture(final Bitmap bitmap) {

		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);

		if(textureHandle[0] == 0) {
			throw new RuntimeException("OpenGL error: glGenTextures failed.");
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR); // TODO bicubic?
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		return textureHandle[0];
	}

	private static void deleteTexture(final int handle) {
		final int[] handles = {handle};
		GLES20.glDeleteTextures(1, handles, 0);
	}
}
