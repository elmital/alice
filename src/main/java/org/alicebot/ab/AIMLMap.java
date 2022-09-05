
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

