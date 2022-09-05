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

import java.io.DataInputStream;
import net.reduls.sanmoku.util.Misc;

public final class Matrix {
    private static final byte[] matrix;
    private static final int leftNum;
    private static final byte[] posid_map;
    private static final byte[] val;

    public static short linkCost(short s, short s2) {
        int n = Matrix.posid(s) * leftNum + Matrix.posid(s2);
        long l = Matrix.node(n / 4);
        int n2 = (int)(l >> n % 4 * 14) & 0x3FFF;
        return (short)(val[n2 * 2] << 8 | val[n2 * 2 + 1] & 0xFF);
    }

    private static short posid(short s) {
        return (short)(posid_map[s * 2] << 8 | posid_map[s * 2 + 1] & 0xFF);
    }

    private static long node(int n) {
        return (long)(matrix[n * 7 + 0] & 0xFF) << 48 | (long)(matrix[n * 7 + 1] & 0xFF) << 40 | (long)(matrix[n * 7 + 2] & 0xFF) << 32 | (long)(matrix[n * 7 + 3] & 0xFF) << 24 | (long)(matrix[n * 7 + 4] & 0xFF) << 16 | (long)(matrix[n * 7 + 5] & 0xFF) << 8 | (long)(matrix[n * 7 + 6] & 0xFF);
    }

    static {
        posid_map = Misc.readBytesFromFile("posid-map.bin", 2);
        val = Misc.readBytesFromFile("matrix.map", 2);
        DataInputStream dataInputStream = Misc.openDictionaryDataAsDIS("matrix.bin");
        int n = Misc.readInt(dataInputStream);
        leftNum = Misc.readInt(dataInputStream);
        matrix = new byte[n * 7];
        try {
            dataInputStream.readFully(matrix, 0, matrix.length);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        Misc.close(dataInputStream);
    }
}

