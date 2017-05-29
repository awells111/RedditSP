package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;

/**
 * Draws the left margin for comments based on the
 * RedditPreparedComment#indentation number
 */
class IndentView extends View {

	private final Paint mPaint = new Paint();
	private int mIndent;

	private final int mPixelsPerIndent;
	private final float mHalfALine;

	private final boolean mPrefDrawLines;

	public IndentView(Context context) {
		this(context, null);
	}

	public IndentView(final Context context, @Nullable final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IndentView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {

		super(context, attrs, defStyleAttr);

		mPixelsPerIndent = General.dpToPixels(context, 10.0f);
		int mPixelsPerLine = General.dpToPixels(context, 2);
		mHalfALine = mPixelsPerLine / 2;

		final int rrIndentBackgroundCol;
		final int rrIndentLineCol;

		{
			final TypedArray attr = context.obtainStyledAttributes(new int[]{
					R.attr.srIndentBackgroundCol,
					R.attr.srIndentLineCol
			});

			rrIndentBackgroundCol = attr.getColor(0, General.COLOR_INVALID);
			rrIndentLineCol = attr.getColor(1, General.COLOR_INVALID);

			attr.recycle();
		}

		this.setBackgroundColor(rrIndentBackgroundCol);
		mPaint.setColor(rrIndentLineCol);
		mPaint.setStrokeWidth(mPixelsPerLine);

		mPrefDrawLines = PrefsUtility.pref_appearance_indentlines(context, PreferenceManager.getDefaultSharedPreferences(context));
	}

	@Override
	protected void onDraw(final Canvas canvas) {

		super.onDraw(canvas);

		final int height = getMeasuredHeight();

		if(mPrefDrawLines) {
			final float[] lines = new float[mIndent * 4];
			float x;
			// i keeps track of indentation, and
			// l is to populate the float[] with line co-ordinates
			for (int i = 0, l = 0; i < mIndent; ++l) {
				x = (mPixelsPerIndent * ++i) - mHalfALine;
				lines[l]   = x;      // start-x
				lines[++l] = 0;      // start-y
				lines[++l] = x;      // stop-x
				lines[++l] = height; // stop-y
			}
			canvas.drawLines(lines, mPaint);

		} else {
			final float rightLine = getWidth() - mHalfALine;
			canvas.drawLine(rightLine, 0, rightLine, getHeight(), mPaint);
		}
	}

	/**
	 * Sets the indentation for the View
	 * @param indent comment indentation number
	 */
	public void setIndentation(int indent) {
		this.getLayoutParams().width = (mPixelsPerIndent * indent);
		this.mIndent = indent;
		this.invalidate();
		this.requestLayout();
	}
}
