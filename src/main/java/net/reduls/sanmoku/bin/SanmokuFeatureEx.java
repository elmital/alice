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

package net.reduls.sanmoku.bin;

import java.io.IOException;
import net.reduls.sanmoku.FeatureEx;
import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;
import net.reduls.sanmoku.util.ReadLine;

public final class SanmokuFeatureEx {
    public static void main(String[] arrstring) throws IOException {
        if (arrstring.length != 0) {
            System.err.println("Usage: java net.reduls.igo.bin.SanmokuFeatureEx");
            System.exit(1);
        }
        ReadLine readLine = new ReadLine(System.in);
        String string = readLine.read();
        while (string != null) {
            for (Morpheme morpheme : Tagger.parse(string)) {
                FeatureEx featureEx = new FeatureEx(morpheme);
                String string2 = featureEx.baseform.length() == 0 ? "*" : featureEx.baseform;
                String string3 = featureEx.reading.length() == 0 ? "*" : featureEx.reading;
                String string4 = featureEx.pronunciation.length() == 0 ? "*" : featureEx.pronunciation;
                System.out.println(morpheme.surface + "\t" + morpheme.feature + "," + string2 + "," + string3 + "," + string4);
            }
            System.out.println("EOS");
            string = readLine.read();
        }
    }
}

