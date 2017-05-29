package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;
import com.wellsandwhistles.android.redditsp.R;

public class DialogUtils {
	public interface OnSearchListener {
		void onSearch(@Nullable String query);
	}

	public static void showSearchDialog (Context context, final OnSearchListener listener) {
		showSearchDialog(context, R.string.action_search, listener);
	}
	public static void showSearchDialog (Context context, int titleRes, final OnSearchListener listener) {
		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		final EditText editText = (EditText) LayoutInflater.from(context).inflate(R.layout.dialog_editbox, null);

		alertBuilder.setView(editText);
		alertBuilder.setTitle(titleRes);

		alertBuilder.setPositiveButton(R.string.action_search, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final String query = General.asciiLowercase(editText.getText().toString()).trim();
				if (StringUtils.isEmpty(query)) {
					listener.onSearch(null);
				} else {
					listener.onSearch(query);
				}
			}
		});

		alertBuilder.setNegativeButton(R.string.dialog_cancel, null);

		final AlertDialog alertDialog = alertBuilder.create();
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		alertDialog.show();
	}
}
