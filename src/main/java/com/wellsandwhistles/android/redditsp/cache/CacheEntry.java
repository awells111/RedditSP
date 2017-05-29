package com.wellsandwhistles.android.redditsp.cache;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.database.Cursor;

import java.util.UUID;

public final class CacheEntry {

	public final long id;
	//private final URI url;
	//private final String user;
	public final UUID session;

	public final long timestamp;
	//private final int status;
	//private final int type;
	public final String mimetype;

	CacheEntry(final Cursor cursor) {

		id = cursor.getLong(0);
		//url = General.uriFromString(cursor.getString(1));
		//user = cursor.getString(2);
		session = UUID.fromString(cursor.getString(3));
		timestamp = cursor.getLong(4);
		//status = cursor.getInt(5);
		//type = cursor.getInt(6);
		mimetype = cursor.getString(7);
	}
}
