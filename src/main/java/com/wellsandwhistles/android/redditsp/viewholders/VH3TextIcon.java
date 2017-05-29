package com.wellsandwhistles.android.redditsp.viewholders;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;

/**
 * A view holder for a two line, text and icon list item.
 */
public class VH3TextIcon extends VH {

	public final TextView text;
	public final TextView text2;
	public final TextView text3;
	public final ImageView icon;

	public long bindingId = 0;

	public VH3TextIcon(View itemView) {
		super(itemView);

		text = (TextView) itemView.findViewById(R.id.recycler_item_text);
		text2 = (TextView) itemView.findViewById(R.id.recycler_item_2_text);
		text3 = (TextView) itemView.findViewById(R.id.recycler_item_3_text);
		icon = (ImageView) itemView.findViewById(R.id.recycler_item_icon);
	}
}
