package com.wellsandwhistles.android.redditsp.views;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.fragments.PostListingFragment;
import com.wellsandwhistles.android.redditsp.reddit.RedditAPI;
import com.wellsandwhistles.android.redditsp.reddit.api.RedditAPICommentAction;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedPost;
//todo missing constructor warning?
public final class RedditPostView extends FlingableItemView implements RedditPreparedPost.ThumbnailLoadedCallback {

	private final float dpScale;

	private RedditPreparedPost post = null;
	private final TextView title, subtitle;

	private final ImageView thumbnailView, overlayIcon;
	private final ImageView upvoteArrow, downvoteArrow;
	private final TextView postKarma;

	private final LinearLayout mOuterView;
	private final LinearLayout commentsButton;
	private final TextView commentsText;

	private int usageId = 0;

	private final Handler thumbnailHandler;

	private final AppCompatActivity mActivity;

	private final PrefsUtility.PostFlingAction mLeftFlingPref, mRightFlingPref;
	private ActionDescriptionPair mLeftFlingAction, mRightFlingAction;

	private final int
			srPostTitleReadCol,
			srPostTitleCol,
			srListItemBackgroundCol,
			srPostCommentsButtonBackCol;

	@Override
	protected void onSetItemFlingPosition(final float position) {
		mOuterView.setTranslationX(position);
	}

	@NonNull
	@Override
	protected String getFlingLeftText() {

		mLeftFlingAction = chooseFlingAction(mLeftFlingPref);

		if(mLeftFlingAction != null) {
			return mActivity.getString(mLeftFlingAction.descriptionRes);
		} else {
			return "Disabled";
		}
	}

	@NonNull
	@Override
	protected String getFlingRightText() {

		mRightFlingAction = chooseFlingAction(mRightFlingPref);

		if(mRightFlingAction != null) {
			return mActivity.getString(mRightFlingAction.descriptionRes);
		} else {
			return "Disabled";
		}
	}

	@Override
	protected boolean allowFlingingLeft() {
		return mLeftFlingAction != null;
	}

	@Override
	protected boolean allowFlingingRight() {
		return mRightFlingAction != null;
	}

	@Override
	protected void onFlungLeft() {
		RedditPreparedPost.onActionMenuItemSelected(post, mActivity, mLeftFlingAction.action);
	}

	@Override
	protected void onFlungRight() {
		RedditPreparedPost.onActionMenuItemSelected(post, mActivity, mRightFlingAction.action);
	}

	private final class ActionDescriptionPair {
		public final RedditPreparedPost.Action action;
		public final int descriptionRes;

		private ActionDescriptionPair(RedditPreparedPost.Action action, int descriptionRes) {
			this.action = action;
			this.descriptionRes = descriptionRes;
		}
	}

	private ActionDescriptionPair chooseFlingAction(PrefsUtility.PostFlingAction pref) {

		switch(pref) {

			case UPVOTE:
				if(post.isUpvoted()) {
					return new ActionDescriptionPair(RedditPreparedPost.Action.UNVOTE, R.string.action_vote_remove);
				} else {
					return new ActionDescriptionPair(RedditPreparedPost.Action.UPVOTE, R.string.action_upvote);
				}

			case DOWNVOTE:
				if(post.isDownvoted()) {
					return new ActionDescriptionPair(RedditPreparedPost.Action.UNVOTE, R.string.action_vote_remove);
				} else {
					return new ActionDescriptionPair(RedditPreparedPost.Action.DOWNVOTE, R.string.action_downvote);
				}

			case SAVE:
				if(post.isSaved()) {
					return new ActionDescriptionPair(RedditPreparedPost.Action.UNSAVE, R.string.action_unsave);
				} else {
					return new ActionDescriptionPair(RedditPreparedPost.Action.SAVE, R.string.action_save);
				}

			case HIDE:
				if(post.isHidden()) {
					return new ActionDescriptionPair(RedditPreparedPost.Action.UNHIDE, R.string.action_unhide);
				} else {
					return new ActionDescriptionPair(RedditPreparedPost.Action.HIDE, R.string.action_hide);
				}

			case COMMENTS:
				return new ActionDescriptionPair(RedditPreparedPost.Action.COMMENTS, R.string.action_comments_short);

			case LINK:
				return new ActionDescriptionPair(RedditPreparedPost.Action.LINK, R.string.action_link_short);

			case BROWSER:
				return new ActionDescriptionPair(RedditPreparedPost.Action.EXTERNAL, R.string.action_external_short);

			case ACTION_MENU:
				return new ActionDescriptionPair(RedditPreparedPost.Action.ACTION_MENU, R.string.action_actionmenu_short);

			case BACK:
				return new ActionDescriptionPair(RedditPreparedPost.Action.BACK, R.string.action_back);
		}

		return null;
	}

	public RedditPostView(
			final Context context,
			final PostListingFragment fragmentParent,
			final AppCompatActivity activity) {

		super(context);
		mActivity = activity;

		thumbnailHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(final Message msg) {
				if(usageId != msg.what) return;
				thumbnailView.setImageBitmap((Bitmap)msg.obj);
			}
		};

		dpScale = context.getResources().getDisplayMetrics().density; // TODO xml?

		final float fontScale = PrefsUtility.appearance_fontscale_posts(context, PreferenceManager.getDefaultSharedPreferences(context));

		final View rootView = LayoutInflater.from(context).inflate(R.layout.reddit_post_with_votes, this, true);

		upvoteArrow = (ImageView) rootView.findViewById(R.id.reddit_post_upvote);
		downvoteArrow = (ImageView) rootView.findViewById(R.id.reddit_post_downvote);


		upvoteArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				post.handleVote(mActivity, RedditAPI.ACTION_UPVOTE);
			}
		});

		downvoteArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				post.handleVote(mActivity, RedditAPI.ACTION_DOWNVOTE);
			}
		});

		mOuterView = (LinearLayout)rootView.findViewById(R.id.reddit_post_layout);

		mOuterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				fragmentParent.onPostSelected(post);
			}
		});

		mOuterView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				RedditPreparedPost.showActionMenu(mActivity, post);
				return true;
			}
		});

		thumbnailView = (ImageView) rootView.findViewById(R.id.reddit_post_thumbnail_view);
		//todo alex overlayIcon is where the upvote and downvote are currently going.  they are placed over the thumbnail for the posts
		overlayIcon = (ImageView) rootView.findViewById(R.id.reddit_post_overlay_icon);

		title = (TextView) rootView.findViewById(R.id.reddit_post_title);
		subtitle = (TextView) rootView.findViewById(R.id.reddit_post_subtitle);
		postKarma = (TextView) rootView.findViewById(R.id.reddit_post_karma);

		commentsButton = (LinearLayout) rootView.findViewById(R.id.reddit_post_comments_button);
		commentsText = (TextView)commentsButton.findViewById(R.id.reddit_post_comments_text);

		commentsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				fragmentParent.onPostCommentsSelected(post);
			}
		});

		title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize() * fontScale);
		subtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, subtitle.getTextSize() * fontScale);
		postKarma.setTextSize(TypedValue.COMPLEX_UNIT_PX, postKarma.getTextSize() * fontScale);

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mLeftFlingPref = PrefsUtility.pref_behaviour_fling_post_left(context, sharedPreferences);
		mRightFlingPref = PrefsUtility.pref_behaviour_fling_post_right(context, sharedPreferences);

		{
			final TypedArray attr = context.obtainStyledAttributes(new int[]{
					R.attr.srPostTitleCol,
					R.attr.srPostTitleReadCol,
					R.attr.srListItemBackgroundCol,
					R.attr.srPostCommentsButtonBackCol
			});

			srPostTitleCol = attr.getColor(0, 0);
			srPostTitleReadCol = attr.getColor(1, 0);
			srListItemBackgroundCol = attr.getColor(2, 0);
			srPostCommentsButtonBackCol = attr.getColor(3, 0);
			attr.recycle();
		}
	}

	@UiThread
	public void reset(final RedditPreparedPost data) {

		if(data != post) {

			usageId++;

			resetSwipeState();

			final Bitmap thumbnail = data.getThumbnail(this, usageId);
			thumbnailView.setImageBitmap(thumbnail);

			title.setText(data.src.getTitle());
			commentsText.setText(String.valueOf(data.src.getSrc().num_comments));

			if(data.hasThumbnail) {
				thumbnailView.setVisibility(VISIBLE);
				thumbnailView.setMinimumWidth((int)(64.0f * dpScale)); // TODO remove constant, customise
				thumbnailView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
			} else {
				thumbnailView.setMinimumWidth(0);
				thumbnailView.setVisibility(GONE);
			}
		}

		if(post != null) post.unbind(this);
		data.bind(this);

		this.post = data;

		updateAppearance();
	}

	public void updateAppearance() {

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mOuterView.setBackgroundResource(R.drawable.sr_postlist_item_selector_main);
			commentsButton.setBackgroundResource(R.drawable.sr_postlist_commentbutton_selector_main);

		} else {
			// On KitKat and lower, we can't do easily themed highlighting
			mOuterView.setBackgroundColor(srListItemBackgroundCol);
			commentsButton.setBackgroundColor(srPostCommentsButtonBackCol);
		}

		if(post.isRead()) {
			title.setTextColor(srPostTitleReadCol);
		} else {
			title.setTextColor(srPostTitleCol);
		}

		subtitle.setText(post.postListDescription);
		postKarma.setText(post.postKarma);

		boolean overlayVisible = true;

		if(post.isSaved()) {
			overlayIcon.setImageResource(R.drawable.ic_action_star_filled_dark);
		} else if(post.isHidden()) {
			overlayIcon.setImageResource(R.drawable.ic_action_cross_dark);
		}

		if(post.isUpvoted()) {
			upvoteArrow.setColorFilter(mActivity.getResources().getColor(R.color.upvoteColor));
			overlayIcon.setImageResource(R.drawable.action_upvote_dark);
		} else if(post.isDownvoted()) {
			downvoteArrow.setColorFilter(mActivity.getResources().getColor(R.color.downvoteColor));
			overlayIcon.setImageResource(R.drawable.action_downvote_dark);
		} else {

			overlayVisible = false;
		}

		if (!post.isUpvoted()) {
			upvoteArrow.clearColorFilter();
		}
		if (!post.isDownvoted()) {
			downvoteArrow.clearColorFilter();
		}

		if(overlayVisible) {
			overlayIcon.setVisibility(VISIBLE);
		} else {
			overlayIcon.setVisibility(GONE);
		}
	}

	public void betterThumbnailAvailable(final Bitmap thumbnail, final int callbackUsageId) {
		final Message msg = Message.obtain();
		msg.obj = thumbnail;
		msg.what = callbackUsageId;
		thumbnailHandler.sendMessage(msg);
	}

}
