package com.wellsandwhistles.android.redditsp.views.glview.program;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.opengl.Matrix;

public class SRGLMatrixStack {

	private int mTopMatrixPos = 0;
	private final float[] mMatrices = new float[16 * 128];
	private final SRGLContext mGLContext;

	public SRGLMatrixStack(SRGLContext glContext) {
		mGLContext = glContext;
		setIdentity();
	}

	public int pushAndTranslate(float offsetX, float offsetY) {
		mTopMatrixPos += 16;
		Matrix.translateM(mMatrices, mTopMatrixPos, mMatrices, mTopMatrixPos - 16, offsetX, offsetY, 0);
		return mTopMatrixPos - 16;
	}

	public int pushAndScale(float factorX, float factorY) {
		mTopMatrixPos += 16;
		Matrix.scaleM(mMatrices, mTopMatrixPos, mMatrices, mTopMatrixPos - 16, factorX, factorY, 0);
		return mTopMatrixPos - 16;
	}

	public int pop() {
		mTopMatrixPos -= 16;
		return mTopMatrixPos;
	}

	public void setIdentity() {
		Matrix.setIdentityM(mMatrices, mTopMatrixPos);
	}

	public void scale(float factorX, float factorY, float factorZ) {
		Matrix.scaleM(mMatrices, mTopMatrixPos, factorX, factorY, factorZ);
	}

	public void flush() {
		mGLContext.activateMatrix(mMatrices, mTopMatrixPos);
	}

	public void assertAtRoot() {

		if(mTopMatrixPos != 0) {
			throw new RuntimeException("assertAtRoot() failed!");
		}

		for(int i = 0; i < 16; i++) {
			switch(i) {
				case 0:
				case 5:
				case 10:
				case 15:
					if(mMatrices[i] != 1) throw new RuntimeException("Root matrix is not identity!");
					break;
				default:
					if(mMatrices[i] != 0) throw new RuntimeException("Root matrix is not identity!");
					break;
			}
		}
	}
}
