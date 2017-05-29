package com.wellsandwhistles.android.redditsp.cache;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NotifyOutputStream extends FilterOutputStream {

	private final Listener listener;

	public NotifyOutputStream(final OutputStream out, final Listener listener) {
		super(out);
		this.listener = listener;
	}

	@Override
	public void close() throws IOException {
		super.close();
		listener.onClose();
	}

	public interface Listener {
		void onClose() throws IOException;
	}
}
