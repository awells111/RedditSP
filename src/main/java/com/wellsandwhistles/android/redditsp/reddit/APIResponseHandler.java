package com.wellsandwhistles.android.redditsp.reddit;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.support.v7.app.AppCompatActivity;
import com.wellsandwhistles.android.redditsp.activities.BugReportActivity;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditSubreddit;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditUser;

import java.util.List;

public abstract class APIResponseHandler {

	protected final AppCompatActivity context;

	public enum APIFailureType {
		INVALID_USER, BAD_CAPTCHA, NOTALLOWED, SUBREDDIT_REQUIRED, URL_REQUIRED, UNKNOWN, TOO_FAST, TOO_LONG
	}

	private APIResponseHandler(AppCompatActivity context) {
		this.context = context;
	}

	protected abstract void onCallbackException(Throwable t);

	protected abstract void onFailure(@CacheRequest.RequestFailureType int type, Throwable t, Integer status, String readableMessage);
	protected abstract void onFailure(APIFailureType type);

	public final void notifyFailure(final @CacheRequest.RequestFailureType int type, final Throwable t, final Integer status, final String readableMessage) {
		try {
			onFailure(type, t, status, readableMessage);
		} catch(Throwable t1) {
			try {
				onCallbackException(t1);
			} catch(Throwable t2) {
				BugReportActivity.addGlobalError(new SRError(null, null, t1));
				BugReportActivity.handleGlobalError(context, t2);
			}
		}
	}

	public final void notifyFailure(final APIFailureType type) {
		try {
			onFailure(type);
		} catch(Throwable t1) {
			try {
				onCallbackException(t1);
			} catch(Throwable t2) {
				BugReportActivity.addGlobalError(new SRError(null, null, t1));
				BugReportActivity.handleGlobalError(context, t2);
			}
		}
	}

	public static abstract class ActionResponseHandler extends APIResponseHandler {

		protected ActionResponseHandler(AppCompatActivity context) {
			super(context);
		}

		public final void notifySuccess() {
			try {
				onSuccess();
			} catch(Throwable t1) {
				try {
					onCallbackException(t1);
				} catch(Throwable t2) {
					BugReportActivity.addGlobalError(new SRError(null, null, t1));
					BugReportActivity.handleGlobalError(context, t2);
				}
			}
		}

		protected abstract void onSuccess();
	}

	public static abstract class NewCaptchaResponseHandler extends APIResponseHandler {

		protected NewCaptchaResponseHandler(AppCompatActivity context) {
			super(context);
		}

		public final void notifySuccess(final String captchaId) {
			try {
				onSuccess(captchaId);
			} catch(Throwable t1) {
				try {
					onCallbackException(t1);
				} catch(Throwable t2) {
					BugReportActivity.addGlobalError(new SRError(null, null, t1));
					BugReportActivity.handleGlobalError(context, t2);
				}
			}
		}

		protected abstract void onSuccess(String captchaId);
	}

	public static abstract class SubredditResponseHandler extends APIResponseHandler {

		protected SubredditResponseHandler(AppCompatActivity context) {
			super(context);
		}

		public final void notifySuccess(final List<RedditSubreddit> result, final long timestamp) {
			try {
				onSuccess(result, timestamp);
			} catch(Throwable t1) {
				try {
					onCallbackException(t1);
				} catch(Throwable t2) {
					BugReportActivity.addGlobalError(new SRError(null, null, t1));
					BugReportActivity.handleGlobalError(context, t2);
				}
			}
		}

		public final void notifyDownloadNecessary() {
			try {
				onDownloadNecessary();
			} catch(Throwable t1) {
				try {
					onCallbackException(t1);
				} catch(Throwable t2) {
					BugReportActivity.addGlobalError(new SRError(null, null, t1));
					BugReportActivity.handleGlobalError(context, t2);
				}
			}
		}

		public final void notifyDownloadStarted() {
			try {
				onDownloadStarted();
			} catch(Throwable t1) {
				try {
					onCallbackException(t1);
				} catch(Throwable t2) {
					BugReportActivity.addGlobalError(new SRError(null, null, t1));
					BugReportActivity.handleGlobalError(context, t2);
				}
			}
		}

		protected abstract void onDownloadNecessary();
		protected abstract void onDownloadStarted();
		protected abstract void onSuccess(List<RedditSubreddit> result, long timestamp);
	}

	public static abstract class UserResponseHandler extends APIResponseHandler {

		protected UserResponseHandler(AppCompatActivity context) {
			super(context);
		}

		public final void notifySuccess(final RedditUser result, final long timestamp) {
			try {
				onSuccess(result, timestamp);
			} catch(Throwable t1) {
				try {
					onCallbackException(t1);
				} catch(Throwable t2) {
					BugReportActivity.addGlobalError(new SRError(null, null, t1));
					BugReportActivity.handleGlobalError(context, t2);
				}
			}
		}

		public final void notifyDownloadStarted() {
			try {
				onDownloadStarted();
			} catch(Throwable t1) {
				try {
					onCallbackException(t1);
				} catch(Throwable t2) {
					BugReportActivity.addGlobalError(new SRError(null, null, t1));
					BugReportActivity.handleGlobalError(context, t2);
				}
			}
		}

		protected abstract void onDownloadStarted();

		protected abstract void onSuccess(RedditUser result, long timestamp);
	}
}
