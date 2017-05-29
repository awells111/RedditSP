package com.wellsandwhistles.android.redditsp.io;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.annotation.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExtendedDataOutputStream extends DataOutputStream {

	public ExtendedDataOutputStream(OutputStream out) {
		super(out);
	}

	public void writeNullableBoolean(@Nullable final Boolean value) throws IOException {

		if(value == null) {
			writeBoolean(false);

		} else {
			writeBoolean(true);
			writeBoolean(value);
		}
	}
}
