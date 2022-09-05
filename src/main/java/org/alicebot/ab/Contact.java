
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

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contact {
    public static int contactCount = 0;
    public static HashMap<String, Contact> idContactMap = new HashMap();
    public static HashMap<String, String> nameIdMap = new HashMap();
    public String contactId = "ID" + contactCount;
    public String displayName;
    public String birthday;
    public HashMap<String, String> phones;
    public HashMap<String, String> emails;

    public static String multipleIds(String contactName) {
        String patternString = " (" + contactName.toUpperCase() + ") ";
        while (patternString.contains(" ")) {
            patternString = patternString.replace(" ", "(.*)");
        }
        Pattern pattern = Pattern.compile(patternString);
        Set<String> keys = nameIdMap.keySet();
        String result = "";
        int idCount = 0;
        for (String key : keys) {
            Matcher m = pattern.matcher(key);
            if (!m.find()) continue;
            result = result + nameIdMap.get(key.toUpperCase()) + " ";
            ++idCount;
        }
        if (idCount <= 1) {
            result = "false";
        }
        return result.trim();
    }

    public static String contactId(String contactName) {
        String patternString = " " + contactName.toUpperCase() + " ";
        while (patternString.contains(" ")) {
            patternString = patternString.replace(" ", ".*");
        }
        Pattern pattern = Pattern.compile(patternString);
        Set<String> keys = nameIdMap.keySet();
        String result = "unknown";
        for (String key : keys) {
            Matcher m = pattern.matcher(key);
            if (!m.find()) continue;
            result = nameIdMap.get(key.toUpperCase()) + " ";
        }
        return result.trim();
    }

    public static String displayName(String id) {
        Contact c = idContactMap.get(id.toUpperCase());
        String result = "unknown";
        if (c != null) {
            result = c.displayName;
        }
        return result;
    }

    public static String dialNumber(String type, String id) {
        String dialNumber;
        String result = "unknown";
        Contact c = idContactMap.get(id.toUpperCase());
        if (c != null && (dialNumber = c.phones.get(type.toUpperCase())) != null) {
            result = dialNumber;
        }
        return result;
    }

    public static String emailAddress(String type, String id) {
        String emailAddress;
        String result = "unknown";
        Contact c = idContactMap.get(id.toUpperCase());
        if (c != null && (emailAddress = c.emails.get(type.toUpperCase())) != null) {
            result = emailAddress;
        }
        return result;
    }

    public static String birthday(String id) {
        Contact c = idContactMap.get(id.toUpperCase());
        if (c == null) {
            return "unknown";
        }
        return c.birthday;
    }

    public Contact(String displayName, String phoneType, String dialNumber, String emailType, String emailAddress, String birthday) {
        ++contactCount;
        this.phones = new HashMap();
        this.emails = new HashMap();
        idContactMap.put(this.contactId.toUpperCase(), this);
        this.addPhone(phoneType, dialNumber);
        this.addEmail(emailType, emailAddress);
        this.addName(displayName);
        this.addBirthday(birthday);
    }

    public void addPhone(String type, String dialNumber) {
        this.phones.put(type.toUpperCase(), dialNumber);
    }

    public void addEmail(String type, String emailAddress) {
        this.emails.put(type.toUpperCase(), emailAddress);
    }

    public void addName(String name) {
        this.displayName = name;
        nameIdMap.put(this.displayName.toUpperCase(), this.contactId);
    }

    public void addBirthday(String birthday) {
        this.birthday = birthday;
    }
}

