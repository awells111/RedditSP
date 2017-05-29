package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;

public class LinkDispatchActivity extends AppCompatActivity {

	private static final String TAG = "LinkDispatchActivity";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();

		if(intent == null) {
			Log.e(TAG, "Got null intent");
			finish();
			return;
		}

		final Uri data = intent.getData();

		if(data == null) {
			Log.e(TAG, "Got null intent data");
			finish();
			return;
		}

		LinkHandler.onLinkClicked(this, data.toString(), false, null, null, 0, true);
		finish();
	}
}
