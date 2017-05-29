package com.wellsandwhistles.android.redditsp.reddit.prepared;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.activities.CommentReplyActivity;
import com.wellsandwhistles.android.redditsp.common.BetterSSB;
import com.wellsandwhistles.android.redditsp.common.SRThemeAttributes;
import com.wellsandwhistles.android.redditsp.common.SRTime;
import com.wellsandwhistles.android.redditsp.reddit.prepared.markdown.MarkdownParagraphGroup;
import com.wellsandwhistles.android.redditsp.reddit.prepared.markdown.MarkdownParser;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditMessage;

import org.apache.commons.lang3.StringEscapeUtils;

public final class RedditPreparedMessage implements RedditRenderableInboxItem {

	public SpannableStringBuilder header;
	public final MarkdownParagraphGroup body;
	public final String idAndType;
	public final RedditMessage src;

	public RedditPreparedMessage(final Context context, final RedditMessage message, final long timestamp) {

		this.src = message;

		final int srCommentHeaderBoldCol;
		final int srCommentHeaderAuthorCol;

		{
			final TypedArray appearance = context.obtainStyledAttributes(new int[]{
					R.attr.srCommentHeaderBoldCol,
					R.attr.srCommentHeaderAuthorCol,
			});

			srCommentHeaderBoldCol = appearance.getColor(0, 255);
			srCommentHeaderAuthorCol = appearance.getColor(1, 255);

			appearance.recycle();
		}

		body = MarkdownParser.parse(message.getUnescapedBodyMarkdown().toCharArray());

		idAndType = message.name;

		final BetterSSB sb = new BetterSSB();

		if (src.author == null) {
			sb.append("[" + context.getString(R.string.general_unknown) + "]", BetterSSB.FOREGROUND_COLOR | BetterSSB.BOLD, srCommentHeaderAuthorCol, 0, 1f);
		} else {
			sb.append(src.author, BetterSSB.FOREGROUND_COLOR | BetterSSB.BOLD, srCommentHeaderAuthorCol, 0, 1f);
		}

		sb.append("   ", 0);
		sb.append(SRTime.formatDurationFrom(context, src.created_utc * 1000L), BetterSSB.FOREGROUND_COLOR | BetterSSB.BOLD, srCommentHeaderBoldCol, 0, 1f);

		header = sb.get();
	}

	public SpannableStringBuilder getHeader() {
		return header;
	}

	private void openReplyActivity(final AppCompatActivity activity) {

		final Intent intent = new Intent(activity, CommentReplyActivity.class);
		intent.putExtra(CommentReplyActivity.PARENT_ID_AND_TYPE_KEY, idAndType);
		intent.putExtra(CommentReplyActivity.PARENT_MARKDOWN_KEY, src.getUnescapedBodyMarkdown());
		intent.putExtra(CommentReplyActivity.PARENT_TYPE, CommentReplyActivity.PARENT_TYPE_MESSAGE);
		activity.startActivity(intent);
	}

	public void handleInboxClick(AppCompatActivity activity) {
		openReplyActivity(activity);
	}

	@Override
	public void handleInboxLongClick(final AppCompatActivity activity) {
		openReplyActivity(activity);
	}

	@Override
	public CharSequence getHeader(final SRThemeAttributes theme, final RedditChangeDataManager changeDataManager, final Context context) {
		return header;
	}

	@Override
	public View getBody(final AppCompatActivity activity, final Integer textColor, final Float textSize, final boolean showLinkButtons) {

		final LinearLayout subjectLayout = new LinearLayout(activity);
		subjectLayout.setOrientation(LinearLayout.VERTICAL);

		final TextView subjectText = new TextView(activity);
		subjectText.setText(StringEscapeUtils.unescapeHtml4(src.subject != null ? src.subject : "(no subject)"));
		subjectText.setTextColor(textColor);
		subjectText.setTextSize(textSize);
		subjectText.setTypeface(null, Typeface.BOLD);

		subjectLayout.addView(subjectText);
		subjectLayout.addView(body.buildView(activity, textColor, textSize, showLinkButtons));

		return subjectLayout;
	}
}
