package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.SRError;

import java.util.LinkedList;

public class BugReportActivity extends BaseActivity {

	private static final LinkedList<SRError> errors = new LinkedList<>();

	public static synchronized void addGlobalError(SRError error) {
		errors.add(error);
	}

	public static synchronized void handleGlobalError(Context context, String text) {
		handleGlobalError(context, new SRError(text, null, new RuntimeException()));
	}

	public static synchronized void handleGlobalError(Context context, Throwable t) {

		if(t != null) {
			Log.e("BugReportActivity", "Handling exception", t);
		}

		handleGlobalError(context, new SRError(null, null, t));
	}

	public static synchronized void handleGlobalError(final Context context, final SRError error) {

		addGlobalError(error);

		General.UI_THREAD_HANDLER.post(new Runnable() {
			@Override
			public void run() {
				final Intent intent = new Intent(context, BugReportActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		});

	}

	private static synchronized LinkedList<SRError> getErrors() {
		final LinkedList<SRError> result = new LinkedList<>(errors);
		errors.clear();
		return result;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		final TextView title = new TextView(this);
		title.setText(R.string.bug_title);
		layout.addView(title);
		title.setTextSize(20.0f);

		final TextView text = new TextView(this);
		text.setText(R.string.bug_message);

		layout.addView(text);
		text.setTextSize(15.0f);

		final int paddingPx = General.dpToPixels(this, 20);
		title.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
		text.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

		final Button send = new Button(this);
		send.setText(R.string.bug_button_send);

		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final LinkedList<SRError> errors = BugReportActivity.getErrors();

				StringBuilder sb = new StringBuilder(1024);

				sb.append("Error report -- RedditSP v").append(Constants.version(BugReportActivity.this)).append("\r\n\r\n");

				for(SRError error : errors) {
					sb.append("-------------------------------");
					if(error.title != null) sb.append("Title: ").append(error.title).append("\r\n");
					if(error.message != null) sb.append("Message: ").append(error.message).append("\r\n");
					if(error.httpStatus != null) sb.append("HTTP Status: ").append(error.httpStatus).append("\r\n");
					appendException(sb, error.t, 25);
				}

				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"awells111" + '@' + "gmail" + '.' + "com"}); // no spam, thanks
				intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");
				intent.putExtra(Intent.EXTRA_TEXT, sb.toString());

				try {
					startActivity(Intent.createChooser(intent, "Email bug report"));
				} catch (android.content.ActivityNotFoundException ex) {
					General.quickToast(BugReportActivity.this, "No email apps installed!");
				}

				finish();
			}
		});

		final Button ignore = new Button(this);
		ignore.setText(R.string.bug_button_ignore);

		ignore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		layout.addView(send);
		layout.addView(ignore);

		final ScrollView sv = new ScrollView(this);
		sv.addView(layout);

		setBaseActivityContentView(sv);
	}

	public static void appendException(StringBuilder sb, Throwable t, int recurseLimit) {
		if(t != null) {

			sb.append("Exception: ");
			sb.append(t.getClass().getCanonicalName()).append("\r\n");
			sb.append(t.getMessage()).append("\r\n");

			for(StackTraceElement elem : t.getStackTrace()) {
				sb.append("  ").append(elem.toString()).append("\r\n");
			}

			if(recurseLimit > 0 && t.getCause() != null) {
				sb.append("Caused by: ");
				appendException(sb, t.getCause(), recurseLimit - 1);
			}
		}
	}
}
