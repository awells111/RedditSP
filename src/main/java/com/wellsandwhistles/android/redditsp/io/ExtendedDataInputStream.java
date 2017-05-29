package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.annotation.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExtendedDataInputStream extends DataInputStream {

	public ExtendedDataInputStream(InputStream in) {
		super(in);
	}

	@Nullable
	public Boolean readNullableBoolean() throws IOException {

		if(!readBoolean()) {
			return null;
		}

		return readBoolean();
	}
}
