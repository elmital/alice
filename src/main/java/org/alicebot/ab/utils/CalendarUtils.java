
/*
 * The MIT License
 *
 * Copyright (c) 2011 Takeru Ohta <phjgt308@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.alicebot.ab.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarUtils {
    public static int timeZoneOffset() {
        Calendar cal = Calendar.getInstance();
        int offset = (cal.get(15) + cal.get(16)) / 60000;
        return offset;
    }

    public static String year() {
        Calendar cal = Calendar.getInstance();
        return String.valueOf(cal.get(1));
    }

    public static String date() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMMMMM dd, yyyy");
        dateFormat.setCalendar(cal);
        return dateFormat.format(cal.getTime());
    }

    public static String date(String jformat, String locale, String timezone) {
        if (jformat == null) {
            jformat = "EEE MMM dd HH:mm:ss zzz yyyy";
        }
        if (locale == null) {
            locale = Locale.US.getISO3Country();
        }
        if (timezone == null) {
            timezone = TimeZone.getDefault().getDisplayName();
        }
        String dateAsString = new Date().toString();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(jformat);
            dateAsString = simpleDateFormat.format(new Date());
        }
        catch (Exception ex) {
            System.out.println("CalendarUtils.date Bad date: Format = " + jformat + " Locale = " + locale + " Timezone = " + timezone);
        }
        System.out.println("CalendarUtils.date: " + dateAsString);
        return dateAsString;
    }
}

