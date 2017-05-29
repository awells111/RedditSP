package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.BetterSSB;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.LinkHandler;
import com.wellsandwhistles.android.redditsp.common.SRTime;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;

public class RedditPostHeaderView extends LinearLayout {

	private final RedditPreparedPost post;

	private final TextView subtitle;

	public RedditPostHeaderView(final AppCompatActivity activity, final RedditPreparedPost post) {

		super(activity);
		this.post = post;

		final float dpScale = activity.getResources().getDisplayMetrics().density;

		setOrientation(LinearLayout.VERTICAL);

		final int sidesPadding = (int)(15.0f * dpScale);
		final int topPadding = (int)(10.0f * dpScale);

		setPadding(sidesPadding, topPadding, sidesPadding, topPadding);

		final Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Light.ttf");

		final TextView title = new TextView(activity);
		title.setTextSize(19.0f);
		title.setTypeface(tf);
		title.setText(post.src.getTitle());
		title.setTextColor(Color.WHITE);
		addView(title);

		subtitle = new TextView(activity);
		subtitle.setTextSize(13.0f);
		rebuildSubtitle(activity);

		subtitle.setTextColor(Color.rgb(200, 200, 200));
		addView(subtitle);

		{
			final TypedArray appearance = activity.obtainStyledAttributes(new int[]{
					R.attr.srPostListHeaderBackgroundCol});

			setBackgroundColor(appearance.getColor(0, General.COLOR_INVALID));

			appearance.recycle();
		}

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if(!post.isSelf()) {
					LinkHandler.onLinkClicked(activity, post.src.getUrl(), false, post.src.getSrc());
				}
			}
		});

		setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				RedditPreparedPost.showActionMenu(activity, post);
				return true;
			}
		});
	}

	private void rebuildSubtitle(Context context) {

		// TODO customise display
		// TODO preference for the X days, X hours thing

		final int boldCol = Color.WHITE;
		final int rrPostSubtitleUpvoteCol;
		final int rrPostSubtitleDownvoteCol;

		{
			final TypedArray appearance = context.obtainStyledAttributes(new int[]{
					R.attr.srPostSubtitleBoldCol,
					R.attr.srPostSubtitleUpvoteCol,
					R.attr.srPostSubtitleDownvoteCol
			});

			rrPostSubtitleUpvoteCol = appearance.getColor(1, 255);
			rrPostSubtitleDownvoteCol = appearance.getColor(2, 255);

			appearance.recycle();
		}

		final BetterSSB postListDescSb = new BetterSSB();

		final int pointsCol;
		if(post.isUpvoted()) {
			pointsCol = rrPostSubtitleUpvoteCol;
		} else if(post.isDownvoted()) {
			pointsCol = rrPostSubtitleDownvoteCol;
		} else {
			pointsCol = boldCol;
		}

		if(post.src.isNsfw()) {
			postListDescSb.append(" NSFW ", BetterSSB.BOLD | BetterSSB.FOREGROUND_COLOR | BetterSSB.BACKGROUND_COLOR,
					Color.WHITE, Color.RED, 1f); // TODO color?
			postListDescSb.append("  ", 0);
		}

		postListDescSb.append(String.valueOf(post.computeScore()), BetterSSB.BOLD | BetterSSB.FOREGROUND_COLOR, pointsCol, 0, 1f);
		postListDescSb.append(" " + context.getString(R.string.subtitle_points) + " ", 0);
		postListDescSb.append(SRTime.formatDurationFrom(context, post.src.getCreatedTimeSecsUTC() * 1000), BetterSSB.BOLD | BetterSSB.FOREGROUND_COLOR, boldCol, 0, 1f);
		postListDescSb.append(" " + context.getString(R.string.subtitle_by) + " ", 0);
		postListDescSb.append(post.src.getAuthor(), BetterSSB.BOLD | BetterSSB.FOREGROUND_COLOR, boldCol, 0, 1f);
		postListDescSb.append(" " + context.getString(R.string.subtitle_to) + " ", 0);
		postListDescSb.append(post.src.getSubreddit(), BetterSSB.BOLD | BetterSSB.FOREGROUND_COLOR, boldCol, 0, 1f);

		postListDescSb.append(" (" + post.src.getDomain() + ")", 0);

		subtitle.setText(postListDescSb.get());
	}
}
