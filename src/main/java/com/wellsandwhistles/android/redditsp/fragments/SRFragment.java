package com.wellsandwhistles.android.redditsp.fragments;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public abstract class SRFragment {

	@NonNull private final AppCompatActivity mParent;

	protected SRFragment(
			@NonNull final AppCompatActivity parent,
			final Bundle savedInstanceState) {
		mParent = parent;
	}

	@NonNull
	protected final Context getContext() {
		return mParent;
	}

	@NonNull
	protected final AppCompatActivity getActivity() {
		return mParent;
	}

	protected final String getString(final int resource) {
		return mParent.getApplicationContext().getString(resource);
	}

	protected final void startActivity(final Intent intent) {
		mParent.startActivity(intent);
	}

	protected final void startActivityForResult(final Intent intent, final int requestCode) {
		mParent.startActivityForResult(intent, requestCode);
	}

	public void onCreateOptionsMenu(Menu menu) {}
	public boolean onOptionsItemSelected(MenuItem item) {return false;}

	public abstract View getView();

	public abstract Bundle onSaveInstanceState();
}
