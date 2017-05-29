package com.wellsandwhistles.android.redditsp.settings;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import com.wellsandwhistles.android.redditsp.BuildConfig;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		final Context context = getActivity();

		final String panel = getArguments().getString("panel");
		final int resource;

		try {
			resource = R.xml.class.getDeclaredField("prefs_" + panel).getInt(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

		addPreferencesFromResource(resource);

		final int[] listPrefsToUpdate = {
				R.string.pref_appearance_twopane_key,
				R.string.pref_behaviour_fling_post_left_key,
				R.string.pref_behaviour_fling_post_right_key,
				R.string.pref_behaviour_fling_comment_left_key,
				R.string.pref_behaviour_fling_comment_right_key,
				R.string.pref_appearance_theme_key,
				R.string.pref_appearance_navbar_color_key,
				R.string.pref_cache_maxage_listing_key,
				R.string.pref_cache_maxage_thumb_key,
				R.string.pref_cache_maxage_image_key,
				R.string.pref_appearance_fontscale_posts_key,
				R.string.pref_appearance_fontscale_comments_key,
				R.string.pref_appearance_fontscale_inbox_key,
				R.string.pref_behaviour_actions_comment_tap_key,
				R.string.pref_behaviour_actions_comment_longclick_key,
				R.string.pref_behaviour_commentsort_key,
				R.string.pref_behaviour_postsort_key,
				R.string.pref_appearance_langforce_key,
				R.string.pref_behaviour_postcount_key,
				R.string.pref_behaviour_bezel_toolbar_swipezone_key,

				R.string.pref_behaviour_albumview_mode_key,

				R.string.pref_behaviour_screenorientation_key,
				R.string.pref_behaviour_gallery_swipe_length_key,
				R.string.pref_behaviour_pinned_subredditsort_key,
				R.string.pref_behaviour_blocked_subredditsort_key,
				R.string.pref_cache_rerequest_postlist_age_key
		};

		final int[] editTextPrefsToUpdate = {
				R.string.pref_behaviour_comment_min_key
		};

		for(int pref : listPrefsToUpdate) {

			final ListPreference listPreference = (ListPreference)findPreference(getString(pref));

			if(listPreference == null) continue;

			final int index = listPreference.findIndexOfValue(listPreference.getValue());
			if(index < 0) continue;

			listPreference.setSummary(listPreference.getEntries()[index]);

			listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					final int index = listPreference.findIndexOfValue((String)newValue);
					listPreference.setSummary(listPreference.getEntries()[index]);
					return true;
				}
			});
		}

		for(final int pref : editTextPrefsToUpdate) {

			final EditTextPreference editTextPreference = (EditTextPreference)findPreference(getString(pref));

			if(editTextPreference == null) continue;

			editTextPreference.setSummary(editTextPreference.getText());

			editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if(newValue != null) {
						editTextPreference.setSummary(newValue.toString());
					} else {
						editTextPreference.setSummary("(null)");
					}
					return true;
				}
			});
		}

		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			final Preference pref = findPreference(getString(R.string.pref_appearance_navbar_color_key));

			if(pref != null) {
				pref.setEnabled(false);
				pref.setSummary(R.string.pref_not_supported_before_lollipop);
			}
		}

		Preference cacheLocationPref = findPreference(getString(R.string.pref_cache_location_key));
		if (cacheLocationPref != null) {
			cacheLocationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					showChooseStorageLocationDialog();
					return true;
				}
			});
			updateStorageLocationText(PrefsUtility.pref_cache_location(context,
					PreferenceManager.getDefaultSharedPreferences(context)));
		}
	}
	private void showChooseStorageLocationDialog() {
		final Context context = getActivity();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String currentStorage = PrefsUtility.pref_cache_location(context, prefs);

		List<File> checkPaths = CacheManager.getCacheDirs(context);

		final List<File> folders = new ArrayList<>(checkPaths.size());

		List<CharSequence> choices = new ArrayList<>(checkPaths.size());
		int selectedIndex = 0;

		for (int i = 0; i < checkPaths.size(); i++) {
			File dir = checkPaths.get(i);
			if (dir == null || !dir.exists() || !dir.canRead() || !dir.canWrite()) {
				continue;
			}
			folders.add(dir);
			if (currentStorage.equals(dir.getAbsolutePath())) {
				selectedIndex = i;
			}

			String path = dir.getAbsolutePath();
			long bytes = General.getFreeSpaceAvailable(path);
			String freeSpace = General.addUnits(bytes);
			if (!path.endsWith("/")) {
				path += "/";
			}
			String appCachePostfix = BuildConfig.APPLICATION_ID + "/cache/";
			if (path.endsWith("Android/data/" + appCachePostfix)) {
				path = path.substring(0, path.length() - appCachePostfix.length() - 14);
			} else if (path.endsWith(appCachePostfix)) {
				path = path.substring(0, path.length() - appCachePostfix.length() - 1);
			}
			choices.add(Html.fromHtml("<small>" + path +
					" [" + freeSpace + "]</small>"));
		}
		new AlertDialog.Builder(context)
				.setTitle(R.string.pref_cache_location_title)
				.setSingleChoiceItems(choices.toArray(new CharSequence[choices.size()]),
						selectedIndex, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int i) {
								dialog.dismiss();
								String path = folders.get(i).getAbsolutePath();
								PrefsUtility.pref_cache_location(context, prefs, path);
								updateStorageLocationText(path);
							}
						})
				.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.dismiss();
					}
				})
				.create()
				.show();
	}
	private void updateStorageLocationText(String path) {
		findPreference(getString(R.string.pref_cache_location_key)).setSummary(path);
	}
}
