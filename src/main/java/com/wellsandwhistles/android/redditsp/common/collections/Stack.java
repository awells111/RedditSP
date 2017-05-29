package com.wellsandwhistles.android.redditsp.common.collections;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import java.util.ArrayList;

public class Stack<E> {

	private final ArrayList<E> mData;

	public Stack(int initialCapacity) {
		mData = new ArrayList<>(initialCapacity);
	}

	public void push(E obj) {
		mData.add(obj);
	}

	public E pop() {
		return mData.remove(mData.size() - 1);
	}

	public boolean isEmpty() {
		return mData.isEmpty();
	}

	public boolean remove(E obj) {
		return mData.remove(obj);
	}
}
