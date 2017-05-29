package com.wellsandwhistles.android.redditsp.test.general;

/** This file was either copied or modified from https://github.com/QuantumBadger/RedReader
 * under the Free Software Foundation General Public License version 3*/

import org.junit.Test;
import com.wellsandwhistles.android.redditsp.common.General;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class GeneralTest {

	@Test
	public void testAsciiUppercase() {

		for(char c = 0; c < 128; c++) {
			{
				final String str = "This is a test" + new String(new char[]{c});
				assertEquals(str.toUpperCase(Locale.ENGLISH), General.asciiUppercase(str));
			}

			{
				String str = "" + c + c + c + c + c + "A" + c + "A";
				assertEquals(str.toUpperCase(Locale.ENGLISH), General.asciiUppercase(str));
			}
		}
	}
}
