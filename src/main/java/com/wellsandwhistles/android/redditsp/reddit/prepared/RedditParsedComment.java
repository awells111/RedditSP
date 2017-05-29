package com.wellsandwhistles.android.redditsp.reddit.prepared;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import org.apache.commons.lang3.StringEscapeUtils;
import com.wellsandwhistles.android.redditsp.reddit.prepared.markdown.MarkdownParagraphGroup;
import com.wellsandwhistles.android.redditsp.reddit.prepared.markdown.MarkdownParser;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditComment;
import com.wellsandwhistles.android.redditsp.reddit.things.RedditThingWithIdAndType;

public class RedditParsedComment implements RedditThingWithIdAndType {

	private final RedditComment mSrc;

	private final MarkdownParagraphGroup mBody;

	private final String mFlair;

	public RedditParsedComment(final RedditComment comment) {

		mSrc = comment;

		mBody = MarkdownParser.parse(StringEscapeUtils.unescapeHtml4(comment.body).toCharArray());
		if(comment.author_flair_text != null) {
			mFlair = StringEscapeUtils.unescapeHtml4(comment.author_flair_text);
		} else {
			mFlair = null;
		}
	}

	public MarkdownParagraphGroup getBody() {
		return mBody;
	}

	public String getFlair() {
		return mFlair;
	}

	@Override
	public String getIdAlone() {
		return mSrc.getIdAlone();
	}

	@Override
	public String getIdAndType() {
		return mSrc.getIdAndType();
	}

	public RedditComment getRawComment() {
		return mSrc;
	}
}
