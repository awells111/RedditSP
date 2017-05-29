package com.wellsandwhistles.android.redditsp.receivers;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.activities.BugReportActivity;
import com.wellsandwhistles.android.redditsp.activities.InboxListingActivity;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.cache.downloadstrategy.DownloadStrategyAlways;
import com.wellsandwhistles.android.redditsp.common.Constants;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedArray;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonBufferedObject;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditComment;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditMessage;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditThing;

import java.net.URI;
import java.util.UUID;

public class NewMessageChecker extends BroadcastReceiver {

	private static final String PREFS_SAVED_MESSAGE_ID = "LastMessageId";
	private static final String PREFS_SAVED_MESSAGE_TIMESTAMP = "LastMessageTimestamp";


	public void onReceive(Context context, Intent intent) {
		checkForNewMessages(context);
	}

	public static void checkForNewMessages(Context context) {

		Log.i("RedReader", "Checking for new messages.");

		boolean notificationsEnabled = PrefsUtility.pref_behaviour_notifications(context, PreferenceManager.getDefaultSharedPreferences(context));
		if (!notificationsEnabled) return;

		final RedditAccount user = RedditAccountManager.getInstance(context).getDefaultAccount();

		if(user.isAnonymous()) {
			return;
		}

		final CacheManager cm = CacheManager.getInstance(context);

		final URI url = Constants.Reddit.getUri("/message/unread.json?limit=2");

		final CacheRequest request = new CacheRequest(
				url,
				user,
				null,
				Constants.Priority.API_INBOX_LIST,
				0,
				DownloadStrategyAlways.INSTANCE,
				Constants.FileType.INBOX_LIST,
				CacheRequest.DOWNLOAD_QUEUE_REDDIT_API,
				true,
				true,
				context) {

			@Override
			protected void onDownloadNecessary() {}

			@Override
			protected void onDownloadStarted() {}

			@Override
			protected void onCallbackException(final Throwable t) {
				BugReportActivity.handleGlobalError(context, t);
			}

			@Override
			protected void onFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {
				Log.e("NewMessageChecker", "Request failed", t);
			}

			@Override
			protected void onProgress(final boolean authorizationInProgress, final long bytesRead, final long totalBytes) {}

			@Override
			protected void onSuccess(final CacheManager.ReadableCacheFile cacheFile, final long timestamp, final UUID session, final boolean fromCache, final String mimetype) {}

			@Override
			public void onJsonParseStarted(final JsonValue value, final long timestamp, final UUID session, final boolean fromCache) {

				try {

					final JsonBufferedObject root = value.asObject();
					final JsonBufferedObject data = root.getObject("data");
					final JsonBufferedArray children = data.getArray("children");

					children.join();
					final int messageCount = children.getCurrentItemCount();

					if(messageCount < 1) {
						return;
					}

					final RedditThing thing = children.get(0).asObject(RedditThing.class);

					String title;
					final String text = context.getString(R.string.notification_message_action);

					final String messageID;
					final long messageTimestamp;

					switch(thing.getKind()) {
						case COMMENT: {
							final RedditComment comment = thing.asComment();
							title = context.getString(R.string.notification_comment, comment.author);
							messageID = comment.name;
							messageTimestamp = comment.created_utc;
							break;
						}

						case MESSAGE: {
							final RedditMessage message = thing.asMessage();
							title =context.getString(R.string.notification_message, message.author);
							messageID = message.name;
							messageTimestamp = message.created_utc;
							break;
						}

						default: {
							throw new RuntimeException("Unknown item in list.");
						}
					}

					// Check if the previously saved message is the same as the one we just received

					final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					final String oldMessageId = prefs.getString(PREFS_SAVED_MESSAGE_ID, "");
					final long oldMessageTimestamp = prefs.getLong(PREFS_SAVED_MESSAGE_TIMESTAMP, 0);

					if(oldMessageId == null || (!messageID.equals(oldMessageId) && oldMessageTimestamp <= messageTimestamp)) {

						prefs.edit()
								.putString(PREFS_SAVED_MESSAGE_ID, messageID)
								.putLong(PREFS_SAVED_MESSAGE_TIMESTAMP, messageTimestamp)
								.apply();

						if(messageCount > 1) {
							title = context.getString(R.string.notification_message_multiple);
						}

						createNotification(title, text, context);
					}

				} catch(Throwable t) {
					notifyFailure(CacheRequest.REQUEST_FAILURE_PARSE, t, null, "Parse failure");
				}
			}
		};

		cm.makeRequest(request);
	}

	private static void createNotification(String title, String text, Context context) {

		final Notification.Builder notification = new Notification.Builder(context)
				.setSmallIcon(R.drawable.icon_notif)
				.setContentTitle(title)
				.setContentText(text)
				.setAutoCancel(true);

		final Intent intent = new Intent(context, InboxListingActivity.class);
		notification.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));

		final NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(0, notification.getNotification());
	}
}
