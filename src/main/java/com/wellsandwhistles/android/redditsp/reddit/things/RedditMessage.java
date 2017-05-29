package com.wellsandwhistles.android.redditsp.reddit.things;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import org.apache.commons.lang3.StringEscapeUtils;
import com.wellsandwhistles.android.redditsp.jsonwrap.JsonValue;

public class RedditMessage {

	public String author, body, body_html, context, name, parent_id, subject, subreddit;
	public boolean _json_new, was_comment;
	public JsonValue first_message, replies;
	public long created, created_utc;

	public String getUnescapedBodyMarkdown() {
		return StringEscapeUtils.unescapeHtml4(body);
	}
}
