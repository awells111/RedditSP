package com.wellsandwhistles.android.redditsp.adapters;

import com.wellsandwhistles.android.redditsp.fragments.MainMenuFragment;
import com.wellsandwhistles.android.redditsp.reddit.url.PostListingURL;

public interface MainMenuSelectionListener {
	void onSelected(@MainMenuFragment.MainMenuAction int type);

	void onSelected(PostListingURL url);
}
