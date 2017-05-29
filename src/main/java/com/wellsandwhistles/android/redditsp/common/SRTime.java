package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.content.Context;
import android.text.format.DateFormat;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import com.wellsandwhistles.android.redditsp.R;

import java.util.Arrays;
import java.util.List;

public class SRTime {

	private static final DateTimeFormatter
			dtFormatter12hr = DateTimeFormat.forPattern("yyyy-MM-dd h:mm a"),
			dtFormatter24hr = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");


	public static long utcCurrentTimeMillis() {
		return DateTime.now(DateTimeZone.UTC).getMillis();
	}

	public static String formatDateTime(final long utc_ms, final Context context) {

		final DateTime dateTime = new DateTime(utc_ms);
		final DateTime localDateTime = dateTime.withZone(DateTimeZone.getDefault());

		if (DateFormat.is24HourFormat(context)) {
			return dtFormatter24hr.print(localDateTime);
		} else {
			return dtFormatter12hr.print(localDateTime);
		}
	}

	public static String formatDurationFrom(final Context context, final long startTime) {
		final String space = " ";
		final String comma = ",";
		final String separator = comma + space;

		final long endTime = utcCurrentTimeMillis();
		final DateTime dateTime = new DateTime(endTime);
		final DateTime localDateTime = dateTime.withZone(DateTimeZone.getDefault());
		Period period = new Duration(startTime, endTime).toPeriodTo(localDateTime);

		PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
				.appendYears().appendSuffix(space).appendSuffix(context.getString(R.string.time_year), context.getString(R.string.time_years)).appendSeparator(separator)
				.appendMonths().appendSuffix(space).appendSuffix(context.getString(R.string.time_month), context.getString(R.string.time_months)).appendSeparator(separator)
				.appendDays().appendSuffix(space).appendSuffix(context.getString(R.string.time_day), context.getString(R.string.time_days)).appendSeparator(separator)
				.appendHours().appendSuffix(space).appendSuffix(context.getString(R.string.time_hour), context.getString(R.string.time_hours)).appendSeparator(separator)
				.appendMinutes().appendSuffix(space).appendSuffix(context.getString(R.string.time_min), context.getString(R.string.time_mins)).appendSeparator(separator)
				.appendSeconds().appendSuffix(space).appendSuffix(context.getString(R.string.time_sec), context.getString(R.string.time_secs)).appendSeparator(separator)
				.appendMillis().appendSuffix(space).appendSuffix(context.getString(R.string.time_ms))
				.toFormatter();

		String duration = periodFormatter.print(period.normalizedStandard(PeriodType.yearMonthDayTime()));

		List<String> parts = Arrays.asList(duration.split(comma));
		if (parts.size() >= 2) {
			duration = parts.get(0) + comma + parts.get(1);
		}

		return String.format(context.getString(R.string.time_ago), duration);
	}

	public static long since(long timestamp) {
		return utcCurrentTimeMillis() - timestamp;
	}
}
