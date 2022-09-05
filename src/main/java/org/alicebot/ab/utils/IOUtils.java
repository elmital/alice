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

package org.alicebot.ab.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtils {
    public static String readInputTextLine() {
        BufferedReader lineOfText = new BufferedReader(new InputStreamReader(System.in));
        String textLine = null;
        try {
            textLine = lineOfText.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return textLine;
    }

    public static String system(String evaluatedContents, String failedString) {
        Runtime rt = Runtime.getRuntime();
        System.out.println("System " + evaluatedContents);
        try {
            Process p = rt.exec(evaluatedContents);
            InputStream istrm = p.getInputStream();
            InputStreamReader istrmrdr = new InputStreamReader(istrm);
            BufferedReader buffrdr = new BufferedReader(istrmrdr);
            String result = "";
            String data = "";
            while ((data = buffrdr.readLine()) != null) {
                result = result + data + "\n";
            }
            System.out.println("Result = " + result);
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return failedString;
        }
    }
}

