package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.adapters.GroupedRecyclerViewAdapter;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyAlways;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.common.SRThemeAttributes;
import com.wellsandwhistles.android.redditsp.common.SRTime;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedArray;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedObject;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;
import com.wellsandwhistles.android.redditsp.reddit.APIResponseHandler;
import com.wellsandwhistles.android.redditsp.reddit.RedditAPI;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditChangeDataManager;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditParsedComment;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditPreparedMessage;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditRenderableComment;
import com.wellsandwhistles.android.redditsp.reddit.prepared.RedditRenderableInboxItem;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditComment;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditMessage;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditThing;
import com.wellsandwhistles.android.redditsp.views.RedditInboxItemView;
import com.wellsandwhistles.android.redditsp.views.ScrollbarRecyclerViewManager;
import com.wellsandwhistles.android.redditsp.views.liststatus.ErrorView;
import com.wellsandwhistles.android.redditsp.views.liststatus.LoadingView;

import java.net.URI;
import java.util.UUID;

public final class InboxListingActivity extends BaseActivity {

	private static final int OPTIONS_MENU_MARK_ALL_AS_READ = 0;
	private static final int OPTIONS_MENU_SHOW_UNREAD_ONLY = 1;

	private static final String PREF_ONLY_UNREAD = "inbox_only_show_unread";

	private GroupedRecyclerViewAdapter adapter;

	private LoadingView loadingView;
	private LinearLayout notifications;

	private CacheRequest request;

	private boolean mOnlyShowUnread;

	private SRThemeAttributes mTheme;
	private RedditChangeDataManager mChangeDataManager;

	private final Handler itemHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(final Message msg) {
			adapter.appendToGroup(0, (GroupedRecyclerViewAdapter.Item)msg.obj);
		}
	};

	private final class InboxItem extends GroupedRecyclerViewAdapter.Item {

		private final int mListPosition;
		private final RedditRenderableInboxItem mItem;

		private InboxItem(int listPosition, RedditRenderableInboxItem item) {
			this.mListPosition = listPosition;
			this.mItem = item;
		}

		@Override
		public Class getViewType() {
			return RedditInboxItemView.class;
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup) {

			final RedditInboxItemView view = new RedditInboxItemView(InboxListingActivity.this, mTheme);

			final RecyclerView.LayoutParams layoutParams
					= new RecyclerView.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
			view.setLayoutParams(layoutParams);

			return new RecyclerView.ViewHolder(view) {};
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
			((RedditInboxItemView)viewHolder.itemView).reset(
					InboxListingActivity.this,
					mChangeDataManager,
					mTheme,
					mItem,
					mListPosition != 0);
		}

		@Override
		public boolean isHidden() {
			return false;
		}
	}

	// TODO load more on scroll to bottom?

	@Override
	public void onCreate(Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);
		super.onCreate(savedInstanceState);

		mTheme = new SRThemeAttributes(this);
		mChangeDataManager = RedditChangeDataManager.getInstance(
				RedditAccountManager.getInstance(this).getDefaultAccount());

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final String title;

		mOnlyShowUnread = sharedPreferences.getBoolean(PREF_ONLY_UNREAD, false);

		title = getString(R.string.mainmenu_inbox);

		setTitle(title);

		final LinearLayout outer = new LinearLayout(this);
		outer.setOrientation(LinearLayout.VERTICAL);

		loadingView = new LoadingView(this, getString(R.string.download_waiting), true, true);

		notifications = new LinearLayout(this);
		notifications.setOrientation(LinearLayout.VERTICAL);
		notifications.addView(loadingView);

		final ScrollbarRecyclerViewManager recyclerViewManager
				= new ScrollbarRecyclerViewManager(this, null, false);

		adapter = new GroupedRecyclerViewAdapter(1);
		recyclerViewManager.getRecyclerView().setAdapter(adapter);

		outer.addView(notifications);
		outer.addView(recyclerViewManager.getOuterView());

		makeFirstRequest(this);

		setBaseActivityContentView(outer);
	}

	public void cancel() {
		if(request != null) request.cancel();
	}

	private void makeFirstRequest(final Context context) {

		final RedditAccount user = RedditAccountManager.getInstance(context).getDefaultAccount();
		final CacheManager cm = CacheManager.getInstance(context);

		final URI url;


		if(mOnlyShowUnread) {
			url = Constants.Reddit.getUri("/message/unread.json?mark=true&limit=100");
		}else{
			url = Constants.Reddit.getUri("/message/inbox.json?mark=true&limit=100");
		}


		// TODO parameterise limit
		request = new CacheRequest(url, user, null, Constants.Priority.API_INBOX_LIST, 0,
				DownloadStrategyAlways.INSTANCE, Constants.FileType.INBOX_LIST,
				CacheRequest.DOWNLOAD_QUEUE_REDDIT_API, true, true, context) {

			@Override
			protected void onDownloadNecessary() {}

			@Override
			protected void onDownloadStarted() {}

			@Override
			protected void onCallbackException(final Throwable t) {
				request = null;
				BugReportActivity.handleGlobalError(context, t);
			}

			@Override
			protected void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {

				request = null;

				if(loadingView != null) loadingView.setDone(R.string.download_failed);

				final SRError error = General.getGeneralErrorForFailure(context, type, t, status, url.toString());
				General.UI_THREAD_HANDLER.post(new Runnable() {
					@Override
					public void run() {
						notifications.addView(new ErrorView(InboxListingActivity.this, error));
					}
				});

				if(t != null) t.printStackTrace();
			}

			@Override protected void onProgress(final boolean authorizationInProgress, final long bytesRead, final long totalBytes) {}

			@Override
			protected void onSuccess(final CacheManager.ReadableCacheFile cacheFile, final long timestamp, final UUID session, final boolean fromCache, final String mimetype) {
				request = null;
			}

			@Override
			public void onJsonParseStarted(final JsonValue value, final long timestamp, final UUID session, final boolean fromCache) {

				if(loadingView != null) loadingView.setIndeterminate(R.string.download_downloading);

				// TODO pref (currently 10 mins)
				// TODO xml
				if(fromCache && SRTime.since(timestamp) > 10 * 60 * 1000) {
					General.UI_THREAD_HANDLER.post(new Runnable() {
						@Override
						public void run() {
							final TextView cacheNotif = new TextView(context);
							cacheNotif.setText(context.getString(R.string.listing_cached, SRTime.formatDateTime(timestamp, context)));
							final int paddingPx = General.dpToPixels(context, 6);
							final int sidePaddingPx = General.dpToPixels(context, 10);
							cacheNotif.setPadding(sidePaddingPx, paddingPx, sidePaddingPx, paddingPx);
							cacheNotif.setTextSize(13f);
							notifications.addView(cacheNotif);
							adapter.notifyDataSetChanged();
						}
					});
				}

				// TODO {"error": 403} is received for unauthorized subreddits

				try {
					final JsonBufferedObject root = value.asObject();
					final JsonBufferedObject data = root.getObject("data");
					final JsonBufferedArray children = data.getArray("children");

					int listPosition = 0;

					for(JsonValue child : children) {

						final RedditThing thing = child.asObject(RedditThing.class);

						switch(thing.getKind()) {
							case COMMENT:
								final RedditComment comment = thing.asComment();
								final RedditParsedComment parsedComment = new RedditParsedComment(comment);
								final RedditRenderableComment renderableComment = new RedditRenderableComment(parsedComment, null, -100000, false);
								itemHandler.sendMessage(General.handlerMessage(0, new InboxItem(listPosition, renderableComment)));
								listPosition++;

								break;

							case MESSAGE:
								final RedditPreparedMessage message = new RedditPreparedMessage(
										InboxListingActivity.this, thing.asMessage(), timestamp);
								itemHandler.sendMessage(General.handlerMessage(0, new InboxItem(listPosition, message)));
								listPosition++;

								if(message.src.replies != null && message.src.replies.getType() == JsonValue.TYPE_OBJECT) {

									final JsonBufferedArray replies = message.src.replies.asObject().getObject("data").getArray("children");

									for(JsonValue childMsgValue : replies) {
										final RedditMessage childMsgRaw = childMsgValue.asObject(RedditThing.class).asMessage();
										final RedditPreparedMessage childMsg = new RedditPreparedMessage(InboxListingActivity.this, childMsgRaw, timestamp);
										itemHandler.sendMessage(General.handlerMessage(0, new InboxItem(listPosition, childMsg)));
										listPosition++;
									}
								}

								break;

							default:
								throw new RuntimeException("Unknown item in list.");
						}
					}

				} catch (Throwable t) {
					notifyFailure(CacheRequest.REQUEST_FAILURE_PARSE, t, null, "Parse failure");
					return;
				}

				if(loadingView != null) loadingView.setDone(R.string.download_done);
			}
		};

		cm.makeRequest(request);
	}

	@Override
	public void onBackPressed() {
		if(General.onBackPressed()) super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		menu.add(0, OPTIONS_MENU_MARK_ALL_AS_READ, 0, R.string.mark_all_as_read);
		menu.add(0, OPTIONS_MENU_SHOW_UNREAD_ONLY, 1, R.string.inbox_unread_only);
		menu.getItem(1).setCheckable(true);
		if(mOnlyShowUnread){
			menu.getItem(1).setChecked(true);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()) {
			case OPTIONS_MENU_MARK_ALL_AS_READ:
				RedditAPI.markAllAsRead(
						CacheManager.getInstance(this),
						new APIResponseHandler.ActionResponseHandler(this) {
							@Override
							protected void onSuccess() {
								General.quickToast(context, R.string.mark_all_as_read_success);
							}

							@Override
							protected void onCallbackException(final Throwable t) {
								BugReportActivity.addGlobalError(new SRError("Mark all as Read failed", "Callback exception", t));
							}

							@Override
							protected void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {
								final SRError error = General.getGeneralErrorForFailure(context, type, t, status,
										"Reddit API action: Mark all as Read");
								General.UI_THREAD_HANDLER.post(new Runnable() {
									@Override
									public void run() {
										General.showResultDialog(InboxListingActivity.this, error);
									}
								});
							}

							@Override
							protected void onFailure(final APIFailureType type) {

								final SRError error = General.getGeneralErrorForFailure(context, type);
								General.UI_THREAD_HANDLER.post(new Runnable() {
									@Override
									public void run() {
										General.showResultDialog(InboxListingActivity.this, error);
									}
								});
							}
						},
						RedditAccountManager.getInstance(this).getDefaultAccount(),
						this);

				return true;

			case OPTIONS_MENU_SHOW_UNREAD_ONLY: {

				final boolean enabled = !item.isChecked();

				item.setChecked(enabled);
				mOnlyShowUnread = enabled;

				PreferenceManager
						.getDefaultSharedPreferences(this)
						.edit()
						.putBoolean(PREF_ONLY_UNREAD, enabled)
						.apply();

				General.recreateActivityNoAnimation(this);
				return true;
			}

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
