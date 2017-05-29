package com.wellsandwhistles.android.redditsp.views.liststatus;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class StatusListItemView extends FrameLayout {

	protected final float dpScale;

	private View contents = null;

	public StatusListItemView(final Context context) {
		super(context);
		dpScale = context.getResources().getDisplayMetrics().density; // TODO xml?
	}

	public void setContents(final View contents) {
		if(this.contents != null) removeView(this.contents);
		this.contents = contents;
		addView(contents);

		final ViewGroup.LayoutParams layoutParams = contents.getLayoutParams();
		layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
	}

	public void hideNoAnim() {

		setVisibility(GONE);
		removeAllViews();
		contents = null;

		requestLayout();
	}
}
