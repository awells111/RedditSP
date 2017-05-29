package com.wellsandwhistles.android.redditsp.adapters;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wellsandwhistles.android.redditsp.R;
import com.wellsandwhistles.android.redditsp.account.RedditAccount;
import com.wellsandwhistles.android.redditsp.account.RedditAccountManager;
import com.wellsandwhistles.android.redditsp.activities.OAuthLoginActivity;
import com.wellsandwhistles.android.redditsp.common.BetterSSB;
import com.wellsandwhistles.android.redditsp.viewholders.VH;
import com.wellsandwhistles.android.redditsp.viewholders.VH1Text;

import java.util.ArrayList;

public class AccountListAdapter extends HeaderRecyclerAdapter<VH> {

	private final Context context;
	private final Fragment fragment;

	private final ArrayList<RedditAccount> accounts;
	private final Drawable rrIconAdd;

	public AccountListAdapter(final Context context, final Fragment fragment) {
		this.context = context;
		this.fragment = fragment;

		accounts = RedditAccountManager.getInstance(context).getAccounts();

		final TypedArray attr = context.obtainStyledAttributes(new int[]{R.attr.srIconAdd});
		rrIconAdd = ContextCompat.getDrawable(context, attr.getResourceId(0, 0));
		attr.recycle();
	}

	@Override
	protected VH onCreateHeaderItemViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item_1_text, parent, false);
		return new VH1Text(v);
	}

	@Override
	protected VH onCreateContentItemViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item_1_text, parent, false);
		return new VH1Text(v);
	}

	@Override
	protected void onBindHeaderItemViewHolder(VH holder, int position) {
		final VH1Text vh = (VH1Text) holder;
		vh.text.setText(context.getString(R.string.accounts_add));
		vh.text.setCompoundDrawablesWithIntrinsicBounds(rrIconAdd, null, null, null);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent loginIntent = new Intent(context, OAuthLoginActivity.class);
				fragment.startActivityForResult(loginIntent, 123);
			}
		});
	}

	@Override
	protected void onBindContentItemViewHolder(VH holder, final int position) {
		final VH1Text vh = (VH1Text) holder;
		final RedditAccount account = accounts.get(position);
		final BetterSSB username = new BetterSSB();

		if (account.isAnonymous()) {
			username.append(context.getString(R.string.accounts_anon), 0);
		} else {
			username.append(account.username, 0);
		}

		if (account.equals(RedditAccountManager.getInstance(context).getDefaultAccount())) {
			final TypedArray attr = context.obtainStyledAttributes(new int[]{R.attr.srListSubtitleCol});
			final int col = attr.getColor(0, 0);
			attr.recycle();

			username.append("  (" + context.getString(R.string.accounts_active) + ")", BetterSSB.FOREGROUND_COLOR | BetterSSB.SIZE, col, 0, 0.8f);
		}

		vh.text.setText(username.get());

		vh.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final RedditAccount account = accounts.get(position);
				final String[] items = account.isAnonymous()
						? new String[]{context.getString(R.string.accounts_setactive)}
						: new String[]{
						context.getString(R.string.accounts_setactive),
						context.getString(R.string.accounts_delete)
				};

				final AlertDialog.Builder builder = new AlertDialog.Builder(context);

				builder.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String selected = items[which];

						if (selected.equals(context.getString(R.string.accounts_setactive))) {
							RedditAccountManager.getInstance(context).setDefaultAccount(account);
						} else if (selected.equals(context.getString(R.string.accounts_delete))) {
							new AlertDialog.Builder(context)
									.setTitle(R.string.accounts_delete)
									.setMessage(R.string.accounts_delete_sure)
									.setPositiveButton(R.string.accounts_delete,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(final DialogInterface dialog, final int which) {
													RedditAccountManager.getInstance(context).deleteAccount(account);
												}
											})
									.setNegativeButton(R.string.dialog_cancel, null)
									.show();
						}
					}
				});

				builder.setNeutralButton(R.string.dialog_cancel, null);

				final AlertDialog alert = builder.create();
				alert.setTitle(account.isAnonymous() ? context.getString(R.string.accounts_anon) : account.username);
				alert.setCanceledOnTouchOutside(true);
				alert.show();
			}
		});
	}

	@Override
	protected int getContentItemCount() {
		return accounts.size();
	}
}