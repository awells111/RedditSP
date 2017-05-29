package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.common.SRThemeAttributes;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditChangeDataManager;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditRenderableInboxItem;

public class RedditInboxItemView extends LinearLayout {

	private final View mDivider;
	private final TextView mHeader;
	private final FrameLayout mBodyHolder;

	private final SRThemeAttributes mTheme;

	private final boolean showLinkButtons;

	private RedditRenderableInboxItem currentItem = null;

	private final AppCompatActivity mActivity;

	public RedditInboxItemView(
			final AppCompatActivity activity,
			final SRThemeAttributes theme) {

		super(activity);

		mActivity = activity;
		mTheme = theme;

		setOrientation(VERTICAL);

		mDivider = new View(activity);
		mDivider.setBackgroundColor(Color.argb(128, 128, 128, 128)); // TODO better
		addView(mDivider);

		mDivider.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
		mDivider.getLayoutParams().height = 1;

		final LinearLayout inner = new LinearLayout(activity);
		inner.setOrientation(VERTICAL);

		mHeader = new TextView(activity);
		mHeader.setTextSize(11.0f * theme.srCommentFontScale);
		mHeader.setTextColor(theme.srCommentHeaderCol);
		inner.addView(mHeader);
		mHeader.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

		mBodyHolder = new FrameLayout(activity);
		mBodyHolder.setPadding(0, General.dpToPixels(activity, 2), 0, 0);
		inner.addView(mBodyHolder);
		mBodyHolder.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

		final int paddingPixels = General.dpToPixels(activity, 8.0f);
		inner.setPadding(paddingPixels + paddingPixels, paddingPixels, paddingPixels, paddingPixels);

		addView(inner);
		inner.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

		setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

		showLinkButtons = PrefsUtility.pref_appearance_linkbuttons(activity, PreferenceManager.getDefaultSharedPreferences(activity));

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleInboxClick(mActivity);
			}
		});

		setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handleInboxLongClick(mActivity);
				return true;
			}
		});
	}

	public void reset(
			final AppCompatActivity context,
			final RedditChangeDataManager changeDataManager,
			final SRThemeAttributes theme,
			final RedditRenderableInboxItem item,
			final boolean showDividerAtTop) {

		currentItem = item;

		mDivider.setVisibility(showDividerAtTop ? VISIBLE : GONE);
		mHeader.setText(item.getHeader(theme, changeDataManager, context));

		final View body = item.getBody(
			context,
			mTheme.srCommentBodyCol,
			13.0f * mTheme.srCommentFontScale,
			showLinkButtons);

		mBodyHolder.removeAllViews();
		mBodyHolder.addView(body);
		body.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
	}

	public void handleInboxClick(AppCompatActivity activity) {
		if(currentItem != null) currentItem.handleInboxClick(activity);
	}

	public void handleInboxLongClick(AppCompatActivity activity) {
		if(currentItem != null) currentItem.handleInboxLongClick(activity);
	}
}
