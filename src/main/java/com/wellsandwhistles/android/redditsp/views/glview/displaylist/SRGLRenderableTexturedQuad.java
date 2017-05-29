package com.wellsandwhistles.android.redditsp.views.glview.displaylist;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLContext;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLMatrixStack;
import com.wellsandwhistles.android.redditsp.views.glview.program.SRGLTexture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SRGLRenderableTexturedQuad extends SRGLRenderable {

	private SRGLTexture mTexture;
	private final SRGLContext mGLContext;

	private static final FloatBuffer mVertexBuffer;

	private static final float[] vertexData = {
			0, 0, 0,
			0, 1, 0,
			1, 0, 0,
			1, 1, 0
	};

	private static final FloatBuffer mUVBuffer;

	private static final float[] uvData = {
			0f, 0f,
			0f, 1f,
			1f, 0f,
			1f, 1f
	};

	static {
		mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mVertexBuffer.put(vertexData).position(0);

		mUVBuffer = ByteBuffer.allocateDirect(uvData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mUVBuffer.put(uvData).position(0);
	}

	public SRGLRenderableTexturedQuad(SRGLContext glContext, SRGLTexture texture) {
		mGLContext = glContext;
		mTexture = texture;
	}

	public void setTexture(SRGLTexture newTexture) {

		if(isAdded()) {
			mTexture.releaseReference();
		}

		mTexture = newTexture;

		if(isAdded()) {
			mTexture.addReference();
		}
	}

	@Override
	public void onAdded() {
		super.onAdded();
		mTexture.addReference();
	}

	@Override
	public void onRemoved() {
		mTexture.releaseReference();
		super.onRemoved();
	}

	@Override
	protected void renderInternal(SRGLMatrixStack matrixStack, final long time) {

		mGLContext.activateProgramTexture();

		mTexture.activate();
		matrixStack.flush();

		mGLContext.activateVertexBuffer(mVertexBuffer);
		mGLContext.activateUVBuffer(mUVBuffer);

		mGLContext.drawTriangleStrip(4);
	}
}
