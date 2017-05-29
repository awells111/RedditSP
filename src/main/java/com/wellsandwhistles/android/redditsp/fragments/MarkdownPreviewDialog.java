package com.wellsandwhistles.android.redditsp.fragments;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.reddit.prepared.markdown.MarkdownParagraphGroup;
import com.wellsandwhistles.android.redditsp.reddit.prepared.markdown.MarkdownParser;

public class MarkdownPreviewDialog extends PropertiesDialog {

	public static MarkdownPreviewDialog newInstance(String markdown) {

		final MarkdownPreviewDialog dialog = new MarkdownPreviewDialog();

		final Bundle args = new Bundle(1);
		args.putString("markdown", markdown);
		dialog.setArguments(args);

		return dialog;
	}

	@Override
	protected String getTitle(Context context) {
		return context.getString(R.string.comment_reply_preview);
	}

	@Override
	protected void prepare(AppCompatActivity context, LinearLayout items) {

		final MarkdownParagraphGroup parsedGen
				= MarkdownParser.parse(getArguments().getString("markdown").toCharArray());

		final ViewGroup parsed = parsedGen.buildView(context, null, 14f, false);

		final int paddingPx = General.dpToPixels(context, 10);
		parsed.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

		items.addView(parsed);
	}
}
