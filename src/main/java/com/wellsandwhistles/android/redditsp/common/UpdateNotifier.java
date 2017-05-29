package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class UpdateNotifier<E> {

	private final LinkedList<WeakReference<E>> listeners = new LinkedList<>();

	public synchronized void addListener(final E updateListener) {
		listeners.add(new WeakReference<>(updateListener));
	}

	public synchronized void updateAllListeners() {

		final Iterator<WeakReference<E>> iter = listeners.iterator();

		while(iter.hasNext()) {
			final E listener = iter.next().get();

			if(listener == null) {
				iter.remove();
			} else {
				notifyListener(listener);
			}
		}

	}

	protected abstract void notifyListener(E listener);
}
