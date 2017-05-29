package com.wellsandwhistles.android.redditsp.viewholders;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.view.View;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;

/**
 * A view holder for a one line, text only list item.
 */
public class VH1Text extends VH {

	public final TextView text;

	public VH1Text(View itemView) {
		super(itemView);

		text = (TextView) itemView.findViewById(R.id.recycler_item_text);
	}
}
