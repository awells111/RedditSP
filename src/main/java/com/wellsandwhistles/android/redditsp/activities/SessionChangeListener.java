package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import java.util.UUID;

public interface SessionChangeListener {

	enum SessionChangeType {
		POSTS, COMMENTS
	}

	void onSessionSelected(UUID session, SessionChangeType type);

	void onSessionRefreshSelected(SessionChangeType type);

	void onSessionChanged(UUID session, SessionChangeType type, long timestamp);
}
