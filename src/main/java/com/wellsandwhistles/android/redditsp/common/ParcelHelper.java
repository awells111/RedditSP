package com.wellsandwhistles.android.redditsp.common;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import android.os.Parcel;
import com.wellsandwhistles.android.redditsp.image.ImageInfo;

public class ParcelHelper {

	public static boolean readBoolean(final Parcel in) {
		return in.readByte() == 1;
	}

	public static String readNullableString(final Parcel in) {

		final boolean isNull = readBoolean(in);
		if(isNull) return null;

		return in.readString();
	}

	public static ImageInfo.MediaType readNullableEnum(final Parcel in) {

		final boolean isNull = readBoolean(in);
		if(isNull) return null;

		return ImageInfo.MediaType.valueOf(in.readString());
	}

	public static void writeNullableEnum(final Parcel parcel, final ImageInfo.MediaType value) {

		if(value == null) {
			writeBoolean(parcel, false);
		} else {
			writeBoolean(parcel, true);
			parcel.writeString(value.name());
		}
	}

	public static Integer readNullableInt(final Parcel in) {

		final boolean isNull = readBoolean(in);
		if(isNull) return null;

		return in.readInt();
	}

	public static Long readNullableLong(final Parcel in) {

		final boolean isNull = readBoolean(in);
		if(isNull) return null;

		return in.readLong();
	}

	public static Boolean readNullableBoolean(final Parcel in) {

		final boolean isNull = readBoolean(in);
		if(isNull) return null;

		return readBoolean(in);
	}

	public static void writeBoolean(final Parcel parcel, final boolean b) {
		parcel.writeByte((byte)(b ? 1 : 0));
	}

	public static void writeNullableString(final Parcel parcel, final String value) {

		if(value == null) {
			writeBoolean(parcel, false);
		} else {
			writeBoolean(parcel, true);
			parcel.writeString(value);
		}
	}

	public static void writeNullableLong(final Parcel parcel, final Long value) {

		if(value == null) {
			writeBoolean(parcel, false);
		} else {
			writeBoolean(parcel, true);
			parcel.writeLong(value);
		}
	}

	public static void writeNullableBoolean(final Parcel parcel, final Boolean value) {

		if(value == null) {
			writeBoolean(parcel, false);
		} else {
			writeBoolean(parcel, true);
			writeBoolean(parcel, value);
		}
	}
}
