package com.wellsandwhistles.android.redditsp.reddit.prepared;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.wellsandwhistles.android.redditsp.common.SRThemeAttributes;

public interface RedditRenderableCommentListItem {

	CharSequence getHeader(
			final SRThemeAttributes theme,
			final RedditChangeDataManager changeDataManager,
			final Context context);

	View getBody(
			final AppCompatActivity activity,
			final Integer textColor,
			final Float textSize,
			final boolean showLinkButtons);
}
