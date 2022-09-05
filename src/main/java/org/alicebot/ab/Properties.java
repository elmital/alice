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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.alicebot.ab.MagicStrings;

public class Properties
extends HashMap<String, String> {
    public String get(String key) {
        String result = (String)super.get(key);
        if (result == null) {
            return MagicStrings.unknown_property_value;
        }
        return result;
    }

    public void getPropertiesFromInputStream(InputStream in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (!strLine.contains(":")) continue;
                String property = strLine.substring(0, strLine.indexOf(":"));
                String value = strLine.substring(strLine.indexOf(":") + 1);
                this.put(property, value);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getProperties(String filename) {
        System.out.println("Get Properties: " + filename);
        try {
            File file = new File(filename);
            if (file.exists()) {
                System.out.println("Exists: " + filename);
                FileInputStream fstream = new FileInputStream(filename);
                this.getPropertiesFromInputStream(fstream);
                fstream.close();
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

