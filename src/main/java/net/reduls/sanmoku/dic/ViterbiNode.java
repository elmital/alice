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

public final class ViterbiNode {
    public int cost;
    public ViterbiNode prev = null;
    public final int start;
    private final int length_posId_isSpace;
    public final int morphemeId;

    public ViterbiNode(int n, short s, short s2, short s3, boolean bl, int n2) {
        this.cost = s2;
        this.start = n;
        this.length_posId_isSpace = (s << 17) + (s3 << 1) + (bl ? 1 : 0);
        this.morphemeId = n2;
    }

    public short length() {
        return (short)(this.length_posId_isSpace >> 17);
    }

    public short posId() {
        return (short)(this.length_posId_isSpace >> 1 & 0xFFFF);
    }

    public boolean isSpace() {
        return (this.length_posId_isSpace & 1) == 1;
    }

    public static ViterbiNode makeBOSEOS() {
        return new ViterbiNode(0, (short) 0, (short) 0, (short) 0, false, 0);
    }
}

