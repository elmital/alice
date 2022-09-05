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
import java.util.HashMap;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;

public class Predicates
extends HashMap<String, String> {
    @Override
    public String put(String key, String value) {
        if (MagicBooleans.trace_mode) {
            System.out.println("Setting predicate " + key + " to " + value);
        }
        return super.put(key, value);
    }

    public String get(String key) {
        String result = (String)super.get(key);
        if (result == null) {
            return MagicStrings.unknown_predicate_value;
        }
        return result;
    }

    public void getPredicateDefaultsFromInputStream(InputStream in) {
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

    public void getPredicateDefaults(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream fstream = new FileInputStream(filename);
                this.getPredicateDefaultsFromInputStream(fstream);
                fstream.close();
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

