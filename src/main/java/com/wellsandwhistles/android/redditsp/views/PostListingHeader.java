package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.General;


public final class PostListingHeader extends LinearLayout {

	public PostListingHeader(final Context context, final String titleText, final String subtitleText) {

		super(context);

		{
			final TypedArray appearance = context.obtainStyledAttributes(new int[]{
					R.attr.srPostListHeaderBackgroundCol});

			setBackgroundColor(appearance.getColor(0, General.COLOR_INVALID));

			appearance.recycle();
		}

		final float dpScale = context.getResources().getDisplayMetrics().density;

		setOrientation(LinearLayout.VERTICAL);

		final int sidesPadding = (int)(15.0f * dpScale);
		final int topPadding = (int)(10.0f * dpScale);

		setPadding(sidesPadding, topPadding, sidesPadding, topPadding);

		final Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");

		final TextView title = new TextView(context);
		title.setText(titleText);
		title.setTextSize(22.0f);
		title.setTypeface(tf);
		title.setTextColor(Color.WHITE);
		addView(title);

		final TextView subtitle = new TextView(context);
		subtitle.setTextSize(14.0f);
		subtitle.setText(subtitleText);
		subtitle.setTextColor(Color.rgb(200, 200, 200));
		addView(subtitle);
	}
}
