
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

