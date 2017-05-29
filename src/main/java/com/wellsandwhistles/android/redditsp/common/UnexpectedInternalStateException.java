package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

public class UnexpectedInternalStateException extends RuntimeException {

	public UnexpectedInternalStateException() {
		super("The application's internal state is invalid");
	}

	public UnexpectedInternalStateException(String message) {
		super(message);
	}
}
