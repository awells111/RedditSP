package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;

public class LinkifiedTextView extends TextView {

	private final AppCompatActivity mActivity;

	public LinkifiedTextView(final AppCompatActivity activity) {
		super(activity);
		mActivity = activity;
	}

	public AppCompatActivity getActivity() {
		return mActivity;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		final CharSequence text = getText();

		if(!(text instanceof Spannable)) {
			return false;
		}

		if(!PrefsUtility.pref_appearance_link_text_clickable(
				mActivity,
				PreferenceManager.getDefaultSharedPreferences(mActivity))) {

			return false;
		}

		final Spannable buffer = (Spannable)text;

		int action = event.getAction();

		if (action == MotionEvent.ACTION_UP ||
				action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= getTotalPaddingLeft();
			y -= getTotalPaddingTop();

			x += getScrollX();
			y += getScrollY();

			final Layout layout = getLayout();
			final int line = layout.getLineForVertical(y);
			final int off = layout.getOffsetForHorizontal(line, x);

			final ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

			if (links.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					links[0].onClick(this);
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(
							buffer,
							buffer.getSpanStart(links[0]),
							buffer.getSpanEnd(links[0]));
				}

				return true;

			} else {
				Selection.removeSelection(buffer);
			}
		}

		return false;
	}
}
