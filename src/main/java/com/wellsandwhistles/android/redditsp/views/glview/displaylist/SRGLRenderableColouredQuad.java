package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLContext;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SRGLRenderableColouredQuad extends SRGLRenderable {

	private final SRGLContext mGLContext;

	private float mRed, mGreen, mBlue, mAlpha;
	private float mOverallAlpha = 1;

	private static final FloatBuffer mVertexBuffer;

	private static final float[] vertexData = {
			0, 0, 0,
			0, 1, 0,
			1, 0, 0,
			1, 1, 0
	};

	static {
		mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mVertexBuffer.put(vertexData).position(0);
	}

	public SRGLRenderableColouredQuad(SRGLContext glContext) {
		mGLContext = glContext;
	}

	public void setColour(final float r, final float g, final float b, final float a) {
		mRed = r;
		mGreen = g;
		mBlue = b;
		mAlpha = a;
	}

	@Override
	public void setOverallAlpha(float alpha) {
		mOverallAlpha = alpha;
	}

	@Override
	protected void renderInternal(SRGLMatrixStack matrixStack, final long time) {

		mGLContext.activateProgramColour();

		matrixStack.flush();

		mGLContext.activateVertexBuffer(mVertexBuffer);
		mGLContext.activateColour(mRed, mGreen, mBlue, mAlpha * mOverallAlpha);

		mGLContext.drawTriangleStrip(4);
	}
}
