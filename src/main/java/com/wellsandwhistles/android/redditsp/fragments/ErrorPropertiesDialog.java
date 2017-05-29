package com.wellsandwhistles.android.redditsp.fragments;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.activities.BugReportActivity;
import com.wellsandwhistles.android.redditsp.common.SRError;

public final class ErrorPropertiesDialog extends PropertiesDialog {

	public static ErrorPropertiesDialog newInstance(final SRError error) {

		final ErrorPropertiesDialog dialog = new ErrorPropertiesDialog();

		final Bundle args = new Bundle();

		args.putString("title", error.title);
		args.putString("message", error.message);

		if(error.t != null) {
			final StringBuilder sb = new StringBuilder(1024);
			BugReportActivity.appendException(sb, error.t, 10);
			args.putString("t", sb.toString());
		}

		if(error.httpStatus != null) {
			args.putString("httpStatus", error.httpStatus.toString());
		}

		if(error.url != null) {
			args.putString("url", error.url);
		}

		dialog.setArguments(args);

		return dialog;
	}

	@Override
	protected String getTitle(Context context) {
		return context.getString(R.string.props_error_title);
	}

	@Override
	protected void prepare(AppCompatActivity context, LinearLayout items) {

		items.addView(propView(context, R.string.props_title, getArguments().getString("title"), true));
		items.addView(propView(context, "Message", getArguments().getString("message"), false));

		if(getArguments().containsKey("httpStatus")) {
			items.addView(propView(context, "HTTP status", getArguments().getString("httpStatus"), false));
		}

		if(getArguments().containsKey("url")) {
			items.addView(propView(context, "URL", getArguments().getString("url"), false));
		}

		if(getArguments().containsKey("t")) {
			items.addView(propView(context, "Exception", getArguments().getString("t"), false));
		}
	}
}
