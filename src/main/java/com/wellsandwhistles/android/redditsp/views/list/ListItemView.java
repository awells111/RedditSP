package com.wellsandwhistles.android.redditsp.views.list;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;

// TODO just make this a linear layout
public class ListItemView extends FrameLayout {

	private final TextView textView;
	private final ImageView imageView;
	private final View divider;

	public ListItemView(final Context context) {

		super(context);

		final LinearLayout ll = (LinearLayout)inflate(context, R.layout.list_item, null);

		divider = ll.findViewById(R.id.list_item_divider);
		textView = (TextView)ll.findViewById(R.id.list_item_text);
		imageView = (ImageView)ll.findViewById(R.id.list_item_icon);

		setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

		addView(ll);
	}

	public void reset(
			@Nullable final Drawable icon,
			@NonNull final CharSequence text,
			final boolean hideDivider) {

		if(hideDivider) {
			divider.setVisibility(View.GONE);
		} else {
			divider.setVisibility(View.VISIBLE);
		}

		textView.setText(text);

		if(icon != null) {
			imageView.setImageDrawable(icon);
			imageView.setVisibility(VISIBLE);
		} else {
			imageView.setImageBitmap(null);
			imageView.setVisibility(GONE);
		}
	}
}
