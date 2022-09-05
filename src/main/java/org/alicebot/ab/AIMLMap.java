
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
import org.alicebot.ab.Bot;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.Sraix;

public class AIMLMap
extends HashMap<String, String> {
    public String mapName;
    String host;
    String botid;
    boolean isExternal = false;

    public AIMLMap(String name) {
        this.mapName = name;
    }

    public String get(String key) {
        String value;
        if (this.mapName.equals(MagicStrings.map_successor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number + 1);
            }
            catch (Exception ex) {
                return MagicStrings.unknown_map_value;
            }
        }
        if (this.mapName.equals(MagicStrings.map_predecessor)) {
            try {
                int number = Integer.parseInt(key);
                return String.valueOf(number - 1);
            }
            catch (Exception ex) {
                return MagicStrings.unknown_map_value;
            }
        }
        if (this.isExternal && MagicBooleans.enable_external_sets) {
            String query = this.mapName.toUpperCase() + " " + key;
            String response = Sraix.sraix(null, query, MagicStrings.unknown_map_value, null, this.host, this.botid, null, "0");
            System.out.println("External " + this.mapName + "(" + key + ")=" + response);
            value = response;
        } else {
            value = (String)super.get(key);
        }
        if (value == null) {
            value = MagicStrings.unknown_map_value;
        }
        System.out.println("AIMLMap get " + key + "=" + value);
        return value;
    }

    @Override
    public String put(String key, String value) {
        return super.put(key, value);
    }

    public int readAIMLMapFromInputStream(InputStream in, Bot bot) {
        int cnt = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String strLine;
            while ((strLine = br.readLine()) != null && strLine.length() > 0) {
                String[] splitLine = strLine.split(":");
                if (splitLine.length < 2) continue;
                ++cnt;
                if (strLine.startsWith(MagicStrings.remote_map_key)) {
                    if (splitLine.length < 3) continue;
                    this.host = splitLine[1];
                    this.botid = splitLine[2];
                    this.isExternal = true;
                    System.out.println("Created external map at " + this.host + " " + this.botid);
                    continue;
                }
                String key = splitLine[0].toUpperCase();
                String value = splitLine[1];
                this.put(key, value);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return cnt;
    }

    public void readAIMLMap(Bot bot) {
        System.out.println("Reading AIML Map " + MagicStrings.maps_path + "/" + this.mapName + ".txt");
        try {
            File file = new File(MagicStrings.maps_path + "/" + this.mapName + ".txt");
            if (file.exists()) {
                FileInputStream fstream = new FileInputStream(MagicStrings.maps_path + "/" + this.mapName + ".txt");
                this.readAIMLMapFromInputStream(fstream, bot);
                fstream.close();
            } else {
                System.out.println(MagicStrings.maps_path + "/" + this.mapName + ".txt not found");
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

