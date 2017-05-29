package com.wellsandwhistles.android.redditsp.fragments;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import org.apache.commons.lang3.StringEscapeUtils;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.SRTime;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditComment;

public final class CommentPropertiesDialog extends PropertiesDialog {

	public static CommentPropertiesDialog newInstance(final RedditComment comment) {

		final CommentPropertiesDialog pp = new CommentPropertiesDialog();

		final Bundle args = new Bundle();
		args.putParcelable("comment", comment);
		pp.setArguments(args);

		return pp;
	}

	@Override
	protected String getTitle(Context context) {
		return context.getString(R.string.props_comment_title);
	}

	@Override
	protected void prepare(AppCompatActivity context, LinearLayout items) {

		final RedditComment comment = getArguments().getParcelable("comment");

		items.addView(propView(context, "ID", comment.name, true));

		items.addView(propView(context, R.string.props_author, comment.author, false));

		if(comment.author_flair_text != null && comment.author_flair_text.length() > 0) {
			items.addView(propView(context, R.string.props_author_flair, comment.author_flair_text, false));
		}

		items.addView(propView(context, R.string.props_created, SRTime.formatDateTime(comment.created_utc * 1000, context), false));

		if(comment.edited instanceof Long) {
			items.addView(propView(context, R.string.props_edited, SRTime.formatDateTime((Long) comment.edited * 1000, context), false));
		} else {
			items.addView(propView(context, R.string.props_edited, R.string.props_never, false));
		}

		items.addView(propView(context, R.string.props_score, String.valueOf(comment.ups - comment.downs), false));

		items.addView(propView(context, R.string.props_subreddit, comment.subreddit, false));

		if(comment.body != null && comment.body.length() > 0) {
			items.addView(propView(context, R.string.props_body_markdown, StringEscapeUtils.unescapeHtml4(comment.body), false));
		}
	}
}
