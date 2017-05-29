package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import com.wellsandwhistles.android.redditsp.R;

import java.util.EnumSet;

public class SRThemeAttributes {

	public final int
			srCommentHeaderBoldCol,
			srCommentHeaderAuthorCol,
			srPostSubtitleUpvoteCol,
			srPostSubtitleDownvoteCol,
			srFlairBackCol,
			srFlairTextCol,
			srGoldBackCol,
			srGoldTextCol,
			srCommentHeaderCol,
			srCommentBodyCol,
			srMainTextCol,
			colorAccent;

	private final EnumSet<PrefsUtility.AppearanceCommentHeaderItem> mCommentHeaderItems;

	public final float srCommentFontScale;

	public SRThemeAttributes(final Context context) {

		final TypedArray appearance = context.obtainStyledAttributes(new int[]{
				R.attr.srCommentHeaderBoldCol,
				R.attr.srCommentHeaderAuthorCol,
				R.attr.srPostSubtitleUpvoteCol,
				R.attr.srPostSubtitleDownvoteCol,
				R.attr.srFlairBackCol,
				R.attr.srFlairTextCol,
				R.attr.srGoldBackCol,
				R.attr.srGoldTextCol,
				R.attr.srCommentHeaderCol,
				R.attr.srCommentBodyCol,
				R.attr.srMainTextCol,
				R.attr.colorAccent
		});

		srCommentHeaderBoldCol = appearance.getColor(0, 255);
		srCommentHeaderAuthorCol = appearance.getColor(1, 255);
		srPostSubtitleUpvoteCol = appearance.getColor(2, 255);
		srPostSubtitleDownvoteCol = appearance.getColor(3, 255);
		srFlairBackCol = appearance.getColor(4, 0);
		srFlairTextCol = appearance.getColor(5, 255);
		srGoldBackCol = appearance.getColor(6, 0);
		srGoldTextCol = appearance.getColor(7, 255);
		srCommentHeaderCol = appearance.getColor(8, 255);
		srCommentBodyCol = appearance.getColor(9, 255);
		srMainTextCol = appearance.getColor(10, 255);
		colorAccent = appearance.getColor(11, 255);

		appearance.recycle();

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		mCommentHeaderItems = PrefsUtility.appearance_comment_header_items(context, prefs);

		srCommentFontScale = PrefsUtility.appearance_fontscale_inbox(
				context,
				prefs);
	}

	public boolean shouldShow(final PrefsUtility.AppearanceCommentHeaderItem type) {
		return mCommentHeaderItems.contains(type);
	}
}
