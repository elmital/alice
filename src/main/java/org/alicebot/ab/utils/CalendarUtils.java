
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org
        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.
        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.
        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
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

