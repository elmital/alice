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

import net.reduls.sanmoku.dic.Char;
import net.reduls.sanmoku.dic.WordDic;

public final class Unknown {
    private static final Char.Category space = Char.category(' ');

    public static void search(String string, int n, WordDic.Callback callback) {
        int n2;
        char c = string.charAt(n);
        Char.Category category = Char.category(c);
        if (!callback.isEmpty() && !category.invoke) {
            return;
        }
        boolean bl = category == space;
        int n3 = Math.min(string.length(), category.length + n);
        for (n2 = n; n2 < n3; ++n2) {
            WordDic.eachViterbiNode(callback, category.id, n, n2 - n + 1, bl);
            if (n2 + 1 == n3 || Char.isCompatible(c, string.charAt(n2 + 1))) continue;
            return;
        }
        if (category.group && n2 < string.length()) {
            while (n2 < string.length()) {
                if (!Char.isCompatible(c, string.charAt(n2))) {
                    WordDic.eachViterbiNode(callback, category.id, n, n2 - n, bl);
                    return;
                }
                ++n2;
            }
            WordDic.eachViterbiNode(callback, category.id, n, string.length() - n, bl);
        }
    }
}

