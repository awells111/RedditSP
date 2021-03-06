package com.wellsandwhistles.android.redditsp.activities;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.cache.CacheManager;
import com.wellsandwhistles.android.redditsp.cache.CacheRequest;
import com.wellsandwhistles.android.redditsp.common.General;
import com.wellsandwhistles.android.redditsp.common.PrefsUtility;
import com.wellsandwhistles.android.redditsp.common.SRError;
import com.wellsandwhistles.android.redditsp.fragments.MarkdownPreviewDialog;
import com.wellsandwhistles.android.redditsp.reddit.APIResponseHandler;
import com.wellsandwhistles.android.redditsp.reddit.RedditAPI;

import java.util.ArrayList;

public class CommentReplyActivity extends BaseActivity {

	private enum ParentType {
		MESSAGE, COMMENT_OR_POST
	}

	private Spinner usernameSpinner;
	private EditText textEdit;

	private String parentIdAndType = null;

	private ParentType mParentType;

	private static String lastText, lastParentIdAndType;

	public static final String PARENT_TYPE = "parentType";
	public static final String PARENT_TYPE_MESSAGE = "parentTypeMessage";

	public static final String PARENT_ID_AND_TYPE_KEY = "parentIdAndType";
	public static final String PARENT_MARKDOWN_KEY = "parent_markdown";
	private static final String COMMENT_TEXT_KEY = "comment_text";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();

		if(intent != null
				&& intent.hasExtra(PARENT_TYPE)
				&& intent.getStringExtra(PARENT_TYPE).equals(PARENT_TYPE_MESSAGE)) {

			mParentType = ParentType.MESSAGE;
			setTitle(R.string.submit_pmreply_actionbar);

		} else {
			mParentType = ParentType.COMMENT_OR_POST;
			setTitle(R.string.submit_comment_actionbar);
		}

		final LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.comment_reply, null);

		usernameSpinner = (Spinner)layout.findViewById(R.id.comment_reply_username);
		textEdit = (EditText)layout.findViewById(R.id.comment_reply_text);

		if(intent != null && intent.hasExtra(PARENT_ID_AND_TYPE_KEY)) {
			parentIdAndType = intent.getStringExtra(PARENT_ID_AND_TYPE_KEY);

		} else if(savedInstanceState != null && savedInstanceState.containsKey(PARENT_ID_AND_TYPE_KEY)) {
			parentIdAndType = savedInstanceState.getString(PARENT_ID_AND_TYPE_KEY);

		} else {
			throw new RuntimeException("No parent ID in CommentReplyActivity");
		}

		final String existingCommentText;

		if(savedInstanceState != null && savedInstanceState.containsKey(COMMENT_TEXT_KEY)) {
			existingCommentText = savedInstanceState.getString(COMMENT_TEXT_KEY);

		} else if(lastText != null && parentIdAndType.equals(lastParentIdAndType)) {
			existingCommentText = lastText;

		} else {
			existingCommentText = null;
		}

		if(existingCommentText != null) {
			textEdit.setText(existingCommentText);
		}

		if(intent != null && intent.hasExtra(PARENT_MARKDOWN_KEY)) {
			TextView parentMarkdown = (TextView)layout.findViewById(R.id.comment_parent_text);
			parentMarkdown.setText(intent.getStringExtra(PARENT_MARKDOWN_KEY));
		}

		final ArrayList<RedditAccount> accounts = RedditAccountManager.getInstance(this).getAccounts();
		final ArrayList<String> usernames = new ArrayList<>();

		for(RedditAccount account : accounts) {
			if(!account.isAnonymous()) {
				usernames.add(account.username);
			}
		}

		if(usernames.size() == 0) {
			General.quickToast(this, getString(R.string.error_toast_notloggedin));
			finish();
		}

		usernameSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernames));

		final ScrollView sv = new ScrollView(this);
		sv.addView(layout);
		setBaseActivityContentView(sv);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(COMMENT_TEXT_KEY, textEdit.getText().toString());
		outState.putString(PARENT_ID_AND_TYPE_KEY, parentIdAndType);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		final MenuItem send = menu.add(R.string.comment_reply_send);
		send.setIcon(R.drawable.ic_action_send_dark);
		send.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(R.string.comment_reply_preview);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getTitle().equals(getString(R.string.comment_reply_send))) {

			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(getString(R.string.comment_reply_submitting_title));
			progressDialog.setMessage(getString(R.string.comment_reply_submitting_message));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.setCanceledOnTouchOutside(false);

			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(final DialogInterface dialogInterface) {
					General.quickToast(CommentReplyActivity.this, getString(R.string.comment_reply_oncancel));
					General.safeDismissDialog(progressDialog);
				}
			});

			progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(final DialogInterface dialogInterface, final int keyCode, final KeyEvent keyEvent) {

					if(keyCode == KeyEvent.KEYCODE_BACK) {
						General.quickToast(CommentReplyActivity.this, getString(R.string.comment_reply_oncancel));
						General.safeDismissDialog(progressDialog);
					}

					return true;
				}
			});

			final APIResponseHandler.ActionResponseHandler handler = new APIResponseHandler.ActionResponseHandler(this) {
				@Override
				protected void onSuccess() {
					General.UI_THREAD_HANDLER.post(new Runnable() {
						@Override
						public void run() {
							General.safeDismissDialog(progressDialog);

							if(mParentType == ParentType.MESSAGE) {
								General.quickToast(CommentReplyActivity.this, getString(R.string.pm_reply_done));
							} else {
								General.quickToast(CommentReplyActivity.this, getString(R.string.comment_reply_done));
							}

							lastText = null;
							lastParentIdAndType = null;
							finish();
						}
					});
				}

				@Override
				protected void onCallbackException(Throwable t) {
					BugReportActivity.handleGlobalError(CommentReplyActivity.this, t);
				}

				@Override
				protected void onFailure(@CacheRequest.RequestFailureType int type, Throwable t, Integer status, String readableMessage) {

					final SRError error = General.getGeneralErrorForFailure(context, type, t, status, null);

					General.UI_THREAD_HANDLER.post(new Runnable() {
						@Override
						public void run() {
							General.showResultDialog(CommentReplyActivity.this, error);
							General.safeDismissDialog(progressDialog);
						}
					});
				}

				@Override
				protected void onFailure(final APIFailureType type) {

					final SRError error = General.getGeneralErrorForFailure(context, type);

					General.UI_THREAD_HANDLER.post(new Runnable() {
						@Override
						public void run() {
							General.showResultDialog(CommentReplyActivity.this, error);
							General.safeDismissDialog(progressDialog);
						}
					});
				}
			};

			final CacheManager cm = CacheManager.getInstance(this);

			final ArrayList<RedditAccount> accounts = RedditAccountManager.getInstance(this).getAccounts();
			RedditAccount selectedAccount = null;

			for(RedditAccount account : accounts) {
				if(!account.isAnonymous() && account.username.equalsIgnoreCase((String)usernameSpinner.getSelectedItem())) {
					selectedAccount = account;
					break;
				}
			}

			RedditAPI.comment(cm, handler, selectedAccount, parentIdAndType, textEdit.getText().toString(), this);

			progressDialog.show();

		} else if(item.getTitle().equals(getString(R.string.comment_reply_preview))) {
			MarkdownPreviewDialog.newInstance(textEdit.getText().toString()).show(getSupportFragmentManager(), "MarkdownPreviewDialog");
		}

		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(textEdit != null) {
			lastText = textEdit.getText().toString();
			lastParentIdAndType = parentIdAndType;
		}
	}

	@Override
	public void onBackPressed() {
		if(General.onBackPressed()) super.onBackPressed();
	}
}
