package com.wellsandwhistles.android.redditsp.cache;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.wellsandwhistles.android.redditsp.activities.BugReportActivity;
import com.wellsandwhistles.android.redditsp.common.SRTime;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;

final class CacheDbManager extends SQLiteOpenHelper {

	private static final String CACHE_DB_FILENAME = "cache.db",
			TABLE = "web",
			FIELD_URL = "url",
			FIELD_ID = "id",
			FIELD_TIMESTAMP = "timestamp",
			FIELD_SESSION = "session",
			FIELD_USER = "user",
			FIELD_STATUS = "status",
			FIELD_TYPE = "type",
			FIELD_MIMETYPE = "mimetype";

	private static final int STATUS_MOVING = 1, STATUS_DONE = 2;

	private static final int CACHE_DB_VERSION = 1;

	private final Context context;

	CacheDbManager(final Context context) {
		super(context, CACHE_DB_FILENAME, null, CACHE_DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {

		final String queryString = String.format(
				"CREATE TABLE %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT," +
						"%s TEXT NOT NULL," +
						"%s TEXT NOT NULL," +
						"%s TEXT NOT NULL," +
						"%s INTEGER," +
						"%s INTEGER," +
						"%s INTEGER," +
						"%s TEXT," +
						"UNIQUE (%s, %s, %s) ON CONFLICT REPLACE)",
				TABLE,
				FIELD_ID,
				FIELD_URL,
				FIELD_USER,
				FIELD_SESSION,
				FIELD_TIMESTAMP,
				FIELD_STATUS,
				FIELD_TYPE,
				FIELD_MIMETYPE,
				FIELD_USER, FIELD_URL, FIELD_SESSION);

		db.execSQL(queryString);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		throw new RuntimeException("Attempt to upgrade database in first version of the app!");
	}

	synchronized LinkedList<CacheEntry> select(final URI url, final String user, final UUID session) {

		final String[] fields = {FIELD_ID, FIELD_URL, FIELD_USER, FIELD_SESSION, FIELD_TIMESTAMP, FIELD_STATUS, FIELD_TYPE, FIELD_MIMETYPE};

		final SQLiteDatabase db = this.getReadableDatabase();

		final String queryString;
		final String[] queryParams;

		if(session == null) {
			queryString = String.format(Locale.US, "%s=%d AND %s=? AND %s=?", FIELD_STATUS, STATUS_DONE, FIELD_URL, FIELD_USER);
			queryParams = new String[] {url.toString(), user};

		} else {
			queryString = String.format(Locale.US, "%s=%d AND %s=? AND %s=? AND %s=?", FIELD_STATUS, STATUS_DONE, FIELD_URL, FIELD_USER, FIELD_SESSION);
			queryParams = new String[] {url.toString(), user, session.toString()};
		}

		final Cursor cursor = db.query(TABLE, fields, queryString, queryParams, null, null, FIELD_TIMESTAMP + " DESC");

		final LinkedList<CacheEntry> result = new LinkedList<>();

		if (cursor == null) {
			BugReportActivity.handleGlobalError(context, "Cursor was null after query");
			return null;
		}

		while(cursor.moveToNext()) {
			result.add(new CacheEntry(cursor));
		}

		cursor.close();

		return result;
	}

	synchronized long newEntry(final CacheRequest request, final UUID session, final String mimetype) throws IOException {

		if(session == null) {
			throw new RuntimeException("No session to write");
		}

		final SQLiteDatabase db = this.getWritableDatabase();

		final ContentValues row = new ContentValues();

		row.put(FIELD_URL, request.url.toString());
		row.put(FIELD_USER, request.user.username);
		row.put(FIELD_SESSION, session.toString());
		row.put(FIELD_TYPE, request.fileType);
		row.put(FIELD_STATUS, STATUS_MOVING);
		row.put(FIELD_TIMESTAMP, SRTime.utcCurrentTimeMillis());
		row.put(FIELD_MIMETYPE, mimetype);

		final long result = db.insert(TABLE, null, row);

		if(result < 0) throw new IOException("DB insert failed");

		return result;
	}

	synchronized void setEntryDone(final long id) {
		final SQLiteDatabase db = this.getWritableDatabase();

		final ContentValues row = new ContentValues();
		row.put(FIELD_STATUS, STATUS_DONE);

		db.update(TABLE, row, FIELD_ID + "=?", new String[] {String.valueOf(id)});
	}

	synchronized int delete(final long id) {
		final SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE, FIELD_ID + "=?", new String[] {String.valueOf(id)});
	}

	protected synchronized int deleteAllBeforeTimestamp(final long timestamp) {
		final SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE, FIELD_TIMESTAMP + "<?", new String[] {String.valueOf(timestamp)});
	}

	public synchronized ArrayList<Long> getFilesToPrune(HashSet<Long> currentFiles, final HashMap<Integer, Long> maxAge, final long defaultMaxAge) {

		final SQLiteDatabase db = this.getWritableDatabase();

		final long currentTime = SRTime.utcCurrentTimeMillis();

		final Cursor cursor = db.query(TABLE, new String[] {FIELD_ID, FIELD_TIMESTAMP, FIELD_TYPE}, null, null, null, null, null, null);

		final HashSet<Long> currentEntries = new HashSet<>();
		final ArrayList<Long> entriesToDelete = new ArrayList<>();
		final ArrayList<Long> filesToDelete = new ArrayList<>(32);

		while(cursor.moveToNext()) {

			final long id = cursor.getLong(0);
			final long timestamp = cursor.getLong(1);
			final int type = cursor.getInt(2);

			final long pruneIfBeforeMs;

			if(maxAge.containsKey(type)) {
				pruneIfBeforeMs = currentTime - maxAge.get(type);
			} else {
				Log.e("SR DEBUG cache", "Using default age! Filetype " + type);
				pruneIfBeforeMs = currentTime - defaultMaxAge;
			}

			if(!currentFiles.contains(id)) {
				entriesToDelete.add(id);

			} else if(timestamp < pruneIfBeforeMs) {
				entriesToDelete.add(id);
				filesToDelete.add(id);

			} else {
				currentEntries.add(id);
			}
		}

		for(final long id : currentFiles) {
			if(!currentEntries.contains(id)) {
				filesToDelete.add(id);
			}
		}

		if(!entriesToDelete.isEmpty()) {

			final StringBuilder query = new StringBuilder(String.format(Locale.US, "DELETE FROM %s WHERE %s IN (", TABLE, FIELD_ID));

			query.append(entriesToDelete.remove(entriesToDelete.size() - 1));

			for(final long id : entriesToDelete) {
				query.append(",").append(id);
				if(query.length() > 512 * 1024) break;
			}

			query.append(')');

			db.execSQL(query.toString());
		}

		cursor.close();

		return filesToDelete;
	}

	public synchronized void emptyTheWholeCache() {
		final SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(String.format(Locale.US, "DELETE FROM %s", TABLE));
	}
}