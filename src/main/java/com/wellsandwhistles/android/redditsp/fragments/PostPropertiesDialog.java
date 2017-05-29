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
import com.wellsandwhistles.android.redditsp.reddit.things.RedditPost;

public final class PostPropertiesDialog extends PropertiesDialog {

	public static PostPropertiesDialog newInstance(final RedditPost post) {

		final PostPropertiesDialog pp = new PostPropertiesDialog();

		final Bundle args = new Bundle();
		args.putParcelable("post", post);
		pp.setArguments(args);

		return pp;
	}

	@Override
	protected String getTitle(Context context) {
		return context.getString(R.string.props_post_title);
	}

	@Override
	protected void prepare(AppCompatActivity context, LinearLayout items) {

		final RedditPost post = getArguments().getParcelable("post");

		items.addView(propView(context, R.string.props_title, StringEscapeUtils.unescapeHtml4(post.title.trim()), true));
		items.addView(propView(context, R.string.props_author, post.author, false));
		items.addView(propView(context, R.string.props_url, StringEscapeUtils.unescapeHtml4(post.url), false));
		items.addView(propView(context, R.string.props_created, SRTime.formatDateTime(post.created_utc * 1000, context), false));

		if(post.edited instanceof Long) {
			items.addView(propView(context, R.string.props_edited, SRTime.formatDateTime((Long) post.edited * 1000, context), false));
		} else {
			items.addView(propView(context, R.string.props_edited, R.string.props_never, false));
		}

		items.addView(propView(context, R.string.props_subreddit, post.subreddit, false));
		items.addView(propView(context, R.string.props_score, String.valueOf(post.score), false));
		items.addView(propView(context, R.string.props_num_comments, String.valueOf(post.num_comments), false));

		if(post.selftext != null && post.selftext.length() > 0) {
			items.addView(propView(context, R.string.props_self_markdown, StringEscapeUtils.unescapeHtml4(post.selftext), false));
		}
	}
}
