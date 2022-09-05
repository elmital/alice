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

package net.reduls.sanmoku;

import java.io.UnsupportedEncodingException;
import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.util.Misc;

public final class FeatureEx {
    public final String baseform;
    public final String reading;
    public final String pronunciation;
    private static final byte[] info = Misc.readBytesFromFile("feature.info.bin", 6);
    private static final byte[] data = Misc.readBytesFromFile("feature.text.bin", 2);

    public FeatureEx(Morpheme morpheme) {
        long l = FeatureEx.info(morpheme.morphemeId);
        this.baseform = FeatureEx.baseform(l, morpheme);
        String string = FeatureEx.reading_pronunciation(l);
        int n = string.indexOf(",");
        if (n == -1) {
            this.reading = this.pronunciation = string;
        } else {
            this.reading = string.substring(0, n);
            this.pronunciation = string.substring(n + 1);
        }
    }

    private static String baseform(long l, Morpheme morpheme) {
        int n = FeatureEx.baseformOffset(l);
        if (n == 131071) {
            return morpheme.surface;
        }
        int n2 = FeatureEx.baseformLength(l);
        return FeatureEx.text(n, n2);
    }

    private static String reading_pronunciation(long l) {
        return FeatureEx.text(FeatureEx.rpOffset(l), FeatureEx.rpLength(l));
    }

    private static long info(int n) {
        return (long)(info[n * 6 + 0] & 0xFF) << 40 | (long)(info[n * 6 + 1] & 0xFF) << 32 | (long)(info[n * 6 + 2] & 0xFF) << 24 | (long)(info[n * 6 + 3] & 0xFF) << 16 | (long)(info[n * 6 + 4] & 0xFF) << 8 | (long)(info[n * 6 + 5] & 0xFF);
    }

    private static int baseformOffset(long l) {
        return (int)(l & 0x1FFFFL);
    }

    private static int baseformLength(long l) {
        return (int)(l >> 38 & 0xFL);
    }

    private static int rpOffset(long l) {
        return (int)(l >> 17 & 0x1FFFFFL);
    }

    private static int rpLength(long l) {
        return (int)(l >> 42 & 0x3FL);
    }

    private static String text(int n, int n2) {
        try {
            return new String(data, n * 2, n2 * 2, "UTF-16BE");
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            throw new RuntimeException(unsupportedEncodingException);
        }
    }
}

