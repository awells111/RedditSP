package com.wellsandwhistles.android.redditsp.image;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.Bitmap;

public final class ThumbnailScaler {

	private static final float maxHeightWidthRatio = 3.0f;

	private static Bitmap scaleAndCrop(final Bitmap src, final int w, final int h, final int newWidth) {

		final float scaleFactor = (float)newWidth / (float)w;
		final Bitmap scaled = Bitmap.createScaledBitmap(src, Math.round(scaleFactor * src.getWidth()), Math.round(scaleFactor * src.getHeight()), true);

		final Bitmap result = Bitmap.createBitmap(scaled, 0, 0, newWidth, Math.round((float)h * scaleFactor));

		if(result != scaled) scaled.recycle();

		return result;
	}

	public static Bitmap scale(final Bitmap image, final int width) {

		final float heightWidthRatio = (float)image.getHeight() / (float)image.getWidth();

		if(heightWidthRatio >= 1.0f && heightWidthRatio <= maxHeightWidthRatio) {

			// Use as-is.
			return Bitmap.createScaledBitmap(image, width, Math.round(heightWidthRatio * width), true);

		} else if(heightWidthRatio < 1.0f) {

			// Wide image. Crop horizontally.
			return scaleAndCrop(image, image.getHeight(), image.getHeight(), width);

		} else {

			// Tall image.
			return scaleAndCrop(image, image.getWidth(), Math.round(image.getWidth() * maxHeightWidthRatio), width);
		}
	}

	public static Bitmap scaleNoCrop(final Bitmap image, final int desiredSquareSizePx) {

		final int currentSquareSizePx = Math.max(image.getWidth(), image.getHeight());

		final float scale = (float)desiredSquareSizePx / (float)currentSquareSizePx;

		return Bitmap.createScaledBitmap(image, Math.round(scale * image.getWidth()), Math.round(scale * image.getHeight()), true);
	}
}
