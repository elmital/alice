
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

package org.alicebot.ab;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.LenientChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Interval {
    public static void test() {
        String date1 = "23:59:59.00";
        String date2 = "12:00:00.00";
        String format = "HH:mm:ss.SS";
        int hours = Interval.getHoursBetween(date2, date1, format);
        System.out.println("Hours = " + hours);
        date1 = "January 30, 2013";
        date2 = "August 2, 1960";
        format = "MMMMMMMMM dd, yyyy";
        int years = Interval.getYearsBetween(date2, date1, format);
        System.out.println("Years = " + years);
    }

    public static int getHoursBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withChronology(LenientChronology.getInstance(GregorianChronology.getInstance()));
            return Hours.hoursBetween(fmt.parseDateTime(date1), fmt.parseDateTime(date2)).getHours();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public static int getYearsBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withChronology(LenientChronology.getInstance(GregorianChronology.getInstance()));
            return Years.yearsBetween(fmt.parseDateTime(date1), fmt.parseDateTime(date2)).getYears();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public static int getMonthsBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withChronology(LenientChronology.getInstance(GregorianChronology.getInstance()));
            return Months.monthsBetween(fmt.parseDateTime(date1), fmt.parseDateTime(date2)).getMonths();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public static int getDaysBetween(String date1, String date2, String format) {
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(format).withChronology(LenientChronology.getInstance(GregorianChronology.getInstance()));
            return Days.daysBetween(fmt.parseDateTime(date1), fmt.parseDateTime(date2)).getDays();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}

