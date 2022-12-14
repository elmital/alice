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
package net.reduls.sanmoku.dic;

import java.io.BufferedReader;
import java.util.ArrayList;
import net.reduls.sanmoku.util.Misc;

public final class PartsOfSpeech {
    private static final String[] posArray;

    public static final String get(int n) {
        return posArray[n];
    }

    static {
        BufferedReader bufferedReader = Misc.openDictionaryDataAsBR("pos.bin");
        ArrayList<String> arrayList = new ArrayList<String>();
        String string = Misc.readLine(bufferedReader);
        while (string != null) {
            arrayList.add(string);
            string = Misc.readLine(bufferedReader);
        }
        Misc.close(bufferedReader);
        posArray = new String[arrayList.size()];
        for (int i = 0; i < posArray.length; ++i) {
            PartsOfSpeech.posArray[i] = (String)arrayList.get(i);
        }
    }
}

