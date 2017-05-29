package com.wellsandwhistles.android.redditsp.views.imageview;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.Bitmap;

public interface ImageTileSource {
	int getWidth();

	int getHeight();

	int getHTileCount();

	int getVTileCount();

	int getTileSize();

	Bitmap getTile(int sampleSize, int tileX, int tileY);

	void dispose();
}
