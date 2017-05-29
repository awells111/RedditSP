package com.wellsandwhistles.android.redditsp.reddit.things;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.os.Parcel;
import android.os.Parcelable;

public final class RedditPost implements Parcelable, RedditThingWithIdAndType {

	public String id, name;
	public String title, url, author, domain, subreddit, subreddit_id;
	public int num_comments, score, ups, downs;
	public boolean archived, over_18, hidden, saved, is_self, clicked, stickied;
	public Object edited;
	public Boolean likes;
	public Boolean spoiler;

	public long created, created_utc;

	public String selftext, permalink, link_flair_text, author_flair_text;
	public String thumbnail; // an image URL

	public RedditPost() {}

	// one of the many reasons why the Android API is awful
	private RedditPost(final Parcel in) {
		id = in.readString();
		name = in.readString();
		title = in.readString();
		url = in.readString();
		author = in.readString();
		domain = in.readString();
		subreddit = in.readString();
		subreddit_id = in.readString();
		num_comments = in.readInt();
		score = in.readInt();
		ups = in.readInt();
		downs = in.readInt();
		archived = in.readInt() == 1;
		over_18 = in.readInt() == 1;
		hidden = in.readInt() == 1;
		saved = in.readInt() == 1;
		is_self = in.readInt() == 1;
		clicked = in.readInt() == 1;
		stickied = in.readInt() == 1;

		final long in_edited = in.readLong();
		if(in_edited == -1) {
			edited = false;
		} else {
			edited = in_edited;
		}

		switch(in.readInt()) {
			case -1: likes = false; break;
			case 0: likes = null; break;
			case 1: likes = true; break;
		}

		created = in.readLong();
		created_utc = in.readLong();
		selftext = in.readString();
		permalink = in.readString();
		link_flair_text = in.readString();
		author_flair_text = in.readString();
		thumbnail = in.readString();

		switch(in.readInt()) {
			case -1:
				spoiler = false;
				break;
			case 0:
				spoiler = null;
				break;
			case 1:
				spoiler = true;
				break;
		}
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(final Parcel parcel, final int flags) {

		parcel.writeString(id);
		parcel.writeString(name);
		parcel.writeString(title);
		parcel.writeString(url);
		parcel.writeString(author);
		parcel.writeString(domain);
		parcel.writeString(subreddit);
		parcel.writeString(subreddit_id);
		parcel.writeInt(num_comments);
		parcel.writeInt(score);
		parcel.writeInt(ups);
		parcel.writeInt(downs);
		parcel.writeInt(archived ? 1 : 0);
		parcel.writeInt(over_18 ? 1 : 0);
		parcel.writeInt(hidden ? 1 : 0);
		parcel.writeInt(saved ? 1 : 0);
		parcel.writeInt(is_self ? 1 : 0);
		parcel.writeInt(clicked ? 1 : 0);
		parcel.writeInt(stickied ? 1 : 0);

		if(edited instanceof Long) {
			parcel.writeLong((Long)edited);
		} else {
			parcel.writeLong(-1);
		}

		if(likes == null) {
			parcel.writeInt(0);
		} else {
			parcel.writeInt(likes ? 1 : -1);
		}

		parcel.writeLong(created);
		parcel.writeLong(created_utc);
		parcel.writeString(selftext);
		parcel.writeString(permalink);
		parcel.writeString(link_flair_text);
		parcel.writeString(author_flair_text);
		parcel.writeString(thumbnail);

		if(spoiler == null) {
			parcel.writeInt(0);
		} else {
			parcel.writeInt(spoiler ? 1 : -1);
		}
	}

	public static final Parcelable.Creator<RedditPost> CREATOR = new Parcelable.Creator<RedditPost>() {
		public RedditPost createFromParcel(final Parcel in) {
			return new RedditPost(in);
		}

		public RedditPost[] newArray(final int size) {
			return new RedditPost[size];
		}
	};

	@Override
	public String getIdAlone() {
		return id;
	}

	@Override
	public String getIdAndType() {
		return name;
	}
}
