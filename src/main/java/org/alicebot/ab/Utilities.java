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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.alicebot.ab.Bot;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.utils.CalendarUtils;

public class Utilities {
    public static String fixCSV(String line) {
        while (line.endsWith(";")) {
            line = line.substring(0, line.length() - 1);
        }
        if (line.startsWith("\"")) {
            line = line.substring(1, line.length());
        }
        if (line.endsWith("\"")) {
            line = line.substring(0, line.length() - 1);
        }
        line = line.replaceAll("\"\"", "\"");
        return line;
    }

    public static String tagTrim(String xmlExpression, String tagName) {
        String stag = "<" + tagName + ">";
        String etag = "</" + tagName + ">";
        if (xmlExpression.length() >= (stag + etag).length()) {
            xmlExpression = xmlExpression.substring(stag.length());
            xmlExpression = xmlExpression.substring(0, xmlExpression.length() - etag.length());
        }
        return xmlExpression;
    }

    public static HashSet<String> stringSet(String ... strings) {
        HashSet<String> set = new HashSet<String>();
        for (String s : strings) {
            set.add(s);
        }
        return set;
    }

    public static String getFileFromInputStream(InputStream in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String contents = "";
        try {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.length() == 0) {
                    contents = contents + "\n";
                    continue;
                }
                contents = contents + strLine + "\n";
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return contents.trim();
    }

    public static String getFile(String filename) {
        String contents = "";
        try {
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream fstream = new FileInputStream(filename);
                contents = Utilities.getFileFromInputStream(fstream);
                fstream.close();
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return contents;
    }

    public static String getCopyrightFromInputStream(InputStream in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String copyright = "";
        try {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.length() == 0) {
                    copyright = copyright + "\n";
                    continue;
                }
                copyright = copyright + "<!-- " + strLine + " -->\n";
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return copyright;
    }

    public static String getCopyright(Bot bot, String AIMLFilename) {
        String copyright = "";
        String year = CalendarUtils.year();
        String date = CalendarUtils.date();
        try {
            copyright = Utilities.getFile(MagicStrings.config_path + "/copyright.txt");
            String[] splitCopyright = copyright.split("\n");
            copyright = "";
            for (int i = 0; i < splitCopyright.length; ++i) {
                copyright = copyright + "<!-- " + splitCopyright[i] + " -->\n";
            }
            copyright = copyright.replace("[url]", bot.properties.get("url"));
            copyright = copyright.replace("[date]", date);
            copyright = copyright.replace("[YYYY]", year);
            copyright = copyright.replace("[version]", bot.properties.get("version"));
            copyright = copyright.replace("[botname]", bot.name.toUpperCase());
            copyright = copyright.replace("[filename]", AIMLFilename);
            copyright = copyright.replace("[botmaster]", bot.properties.get("botmaster"));
            copyright = copyright.replace("[organization]", bot.properties.get("organization"));
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return copyright;
    }

    public static String getPannousAPIKey() {
        String apiKey = Utilities.getFile(MagicStrings.config_path + "/pannous-apikey.txt");
        if (apiKey.equals("")) {
            apiKey = MagicStrings.pannous_api_key;
        }
        return apiKey;
    }

    public static String getPannousLogin() {
        String login = Utilities.getFile(MagicStrings.config_path + "/pannous-login.txt");
        if (login.equals("")) {
            login = MagicStrings.pannous_login;
        }
        return login;
    }

    public static boolean isCharCJK(char c) {
        return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS;
    }
}

