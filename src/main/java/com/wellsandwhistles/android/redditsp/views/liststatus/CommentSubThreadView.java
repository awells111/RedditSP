package com.wellsandwhistles.android.redditsp.views.liststatus;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;
import com.wellsandwhistles.android.redditsp.reddit.url.PostCommentListingURL;

public final class CommentSubThreadView extends StatusListItemView {

	private final PostCommentListingURL mUrl;

	public CommentSubThreadView(
			final AppCompatActivity activity,
			final PostCommentListingURL url,
			int messageRes) {

		super(activity);

		mUrl = url;

		final TypedArray attr = activity.obtainStyledAttributes(new int[] {
				R.attr.srCommentSpecificThreadHeaderBackCol,
				R.attr.srCommentSpecificThreadHeaderTextCol
		});

		final int rrCommentSpecificThreadHeaderBackCol = attr.getColor(0, 0);
		final int rrCommentSpecificThreadHeaderTextCol = attr.getColor(1, 0);

		attr.recycle();

		final TextView textView = new TextView(activity);
		textView.setText(messageRes);
		textView.setTextColor(rrCommentSpecificThreadHeaderTextCol);
		textView.setTextSize(15.0f);
		textView.setPadding((int) (15 * dpScale), (int) (10 * dpScale), (int) (10 * dpScale), (int) (4 * dpScale));

		final TextView messageView = new TextView(activity);
		messageView.setText(R.string.comment_header_specific_thread_message);
		messageView.setTextColor(rrCommentSpecificThreadHeaderTextCol);
		messageView.setTextSize(12.0f);
		messageView.setPadding((int) (15 * dpScale), 0, (int) (10 * dpScale), (int) (10 * dpScale));

		final LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(textView);
		layout.addView(messageView);

		setContents(layout);
		setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

		setBackgroundColor(rrCommentSpecificThreadHeaderBackCol);

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final PostCommentListingURL allComments = mUrl.commentId(null);
				LinkHandler.onLinkClicked(activity, allComments.toString());
			}
		});
	}

}
