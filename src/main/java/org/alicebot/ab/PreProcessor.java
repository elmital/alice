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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.alicebot.ab.Bot;
import org.alicebot.ab.MagicNumbers;
import org.alicebot.ab.MagicStrings;

public class PreProcessor {
    public int normalCount = 0;
    public int denormalCount = 0;
    public int personCount = 0;
    public int person2Count = 0;
    public int genderCount = 0;
    public String[] normalSubs = new String[MagicNumbers.max_substitutions];
    public Pattern[] normalPatterns = new Pattern[MagicNumbers.max_substitutions];
    public String[] denormalSubs = new String[MagicNumbers.max_substitutions];
    public Pattern[] denormalPatterns = new Pattern[MagicNumbers.max_substitutions];
    public String[] personSubs = new String[MagicNumbers.max_substitutions];
    public Pattern[] personPatterns = new Pattern[MagicNumbers.max_substitutions];
    public String[] person2Subs = new String[MagicNumbers.max_substitutions];
    public Pattern[] person2Patterns = new Pattern[MagicNumbers.max_substitutions];
    public String[] genderSubs = new String[MagicNumbers.max_substitutions];
    public Pattern[] genderPatterns = new Pattern[MagicNumbers.max_substitutions];

    public PreProcessor(Bot bot) {
        this.normalCount = this.readSubstitutions(MagicStrings.config_path + "/normal.txt", this.normalPatterns, this.normalSubs);
        this.denormalCount = this.readSubstitutions(MagicStrings.config_path + "/denormal.txt", this.denormalPatterns, this.denormalSubs);
        this.personCount = this.readSubstitutions(MagicStrings.config_path + "/person.txt", this.personPatterns, this.personSubs);
        this.person2Count = this.readSubstitutions(MagicStrings.config_path + "/person2.txt", this.person2Patterns, this.person2Subs);
        this.genderCount = this.readSubstitutions(MagicStrings.config_path + "/gender.txt", this.genderPatterns, this.genderSubs);
        System.out.println("Preprocessor: " + this.normalCount + " norms " + this.personCount + " persons " + this.person2Count + " person2 ");
    }

    public String normalize(String request) {
        return this.substitute(request, this.normalPatterns, this.normalSubs, this.normalCount);
    }

    public String denormalize(String request) {
        return this.substitute(request, this.denormalPatterns, this.denormalSubs, this.denormalCount);
    }

    public String person(String input) {
        return this.substitute(input, this.personPatterns, this.personSubs, this.personCount);
    }

    public String person2(String input) {
        return this.substitute(input, this.person2Patterns, this.person2Subs, this.person2Count);
    }

    public String gender(String input) {
        return this.substitute(input, this.genderPatterns, this.genderSubs, this.genderCount);
    }

    String substitute(String request, Pattern[] patterns, String[] subs, int count) {
        String result = " " + request + " ";
        try {
            for (int i = 0; i < count; ++i) {
                String replacement = subs[i];
                Pattern p = patterns[i];
                Matcher m = p.matcher(result);
                if (!m.find()) continue;
                result = m.replaceAll(replacement);
            }
            while (result.contains("  ")) {
                result = result.replace("  ", " ");
            }
            result = result.trim();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.trim();
    }

    public int readSubstitutionsFromInputStream(InputStream in, Pattern[] patterns, String[] subs) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        int subCount = 0;
        try {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                strLine = strLine.trim();
                Pattern pattern = Pattern.compile("\"(.*?)\",\"(.*?)\"", 32);
                Matcher matcher = pattern.matcher(strLine);
                if (!matcher.find() || subCount >= MagicNumbers.max_substitutions) continue;
                subs[subCount] = matcher.group(2);
                String quotedPattern = Pattern.quote(matcher.group(1));
                patterns[subCount] = Pattern.compile(quotedPattern, 2);
                ++subCount;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return subCount;
    }

    int readSubstitutions(String filename, Pattern[] patterns, String[] subs) {
        int subCount = 0;
        try {
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream fstream = new FileInputStream(filename);
                subCount = this.readSubstitutionsFromInputStream(fstream, patterns, subs);
                fstream.close();
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return subCount;
    }

    public String[] sentenceSplit(String line) {
        line = line.replace("\u3002", ".");
        line = line.replace("\uff1f", "?");
        line = line.replace("\uff01", "!");
        String[] result = line.split("[\\.!\\?]");
        for (int i = 0; i < result.length; ++i) {
            result[i] = result[i].trim();
        }
        return result;
    }

    public void normalizeFile(String infile, String outfile) {
        try {
            String strLine;
            BufferedWriter bw = null;
            FileInputStream fstream = new FileInputStream(infile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            bw = new BufferedWriter(new FileWriter(outfile));
            while ((strLine = br.readLine()) != null) {
                strLine = this.normalize(strLine);
                bw.write(strLine);
                bw.newLine();
            }
            bw.close();
            br.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

