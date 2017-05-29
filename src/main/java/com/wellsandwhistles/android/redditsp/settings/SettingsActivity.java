package com.wellsandwhistles.android.redditsp.settings;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.Window;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;

import java.util.List;

public final class SettingsActivity
		extends AppCompatPreferenceActivity
		implements SharedPreferences.OnSharedPreferenceChangeListener {

	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		PrefsUtility.applySettingsTheme(this);
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		setOrientationFromPrefs();

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onBuildHeaders(final List<Header> target) {
		loadHeadersFromResource(R.xml.prefheaders, target);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return false;
		}
	}

	private void setOrientationFromPrefs() {
		PrefsUtility.ScreenOrientation orientation = PrefsUtility.pref_behaviour_screen_orientation(this, sharedPreferences);
		if (orientation == PrefsUtility.ScreenOrientation.AUTO)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		else if (orientation == PrefsUtility.ScreenOrientation.PORTRAIT)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else if (orientation == PrefsUtility.ScreenOrientation.LANDSCAPE)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {

		if(key.equals(getString(R.string.pref_behaviour_screenorientation_key))) {
			setOrientationFromPrefs();
		}
	}

	@Override
	protected boolean isValidFragment(final String fragmentName) {
		return fragmentName.equals(SettingsFragment.class.getCanonicalName());
	}
}
