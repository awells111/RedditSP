package com.wellsandwhistles.android.redditsp.views.liststatus;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.fragments.ErrorPropertiesDialog;

public final class ErrorView extends StatusListItemView {

	public ErrorView(final AppCompatActivity activity, final SRError error) {

		super(activity);

		final TextView textView = new TextView(activity);
		textView.setText(error.title);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(15.0f);
		textView.setPadding((int) (15 * dpScale), (int) (10 * dpScale), (int) (10 * dpScale), (int) (4 * dpScale));

		final TextView messageView = new TextView(activity);
		messageView.setText(error.message);

		messageView.setTextColor(Color.WHITE);
		messageView.setTextSize(12.0f);
		messageView.setPadding((int) (15 * dpScale), 0, (int) (10 * dpScale), (int) (10 * dpScale));

		final LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(textView);
		layout.addView(messageView);

		setContents(layout);

		setBackgroundColor(Color.rgb(0xCC, 0x00, 0x00));

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ErrorPropertiesDialog.newInstance(error).show(activity.getSupportFragmentManager(), null);
			}
		});
	}
}
