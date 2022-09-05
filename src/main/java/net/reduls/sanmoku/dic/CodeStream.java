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

public final class CodeStream {
    private final CharSequence src;
    private final int end;
    private int pos;
    private int code;
    private int octetPos;
    private int octetLen;

    public CodeStream(CharSequence charSequence, int n) {
        this.src = charSequence;
        this.end = this.src.length();
        this.pos = n;
        this.code = n == this.end ? 0 : (int)charSequence.charAt(n);
        this.octetPos = this.octetLen = this.octetLength(this.code);
    }

    public boolean isEos() {
        return this.pos == this.end;
    }

    public char read() {
        char c = this.peek();
        this.eat();
        return c;
    }

    public int position() {
        return this.pos;
    }

    private int octetLength(int n) {
        if (n < 128) {
            return 1;
        }
        if (n < 2048) {
            return 2;
        }
        if (n < 65536) {
            return 3;
        }
        return 4;
    }

    private char peek() {
        if (this.octetPos == this.octetLen) {
            switch (this.octetLen) {
                case 1: {
                    return (char)this.code;
                }
                case 2: {
                    return (char)(192 + (byte)(this.code >> 6 & 0x1F));
                }
                case 3: {
                    return (char)(224 + (byte)(this.code >> 12 & 0xF));
                }
            }
            return (char)(240 + (byte)(this.code >> 18 & 7));
        }
        int n = (this.octetPos - 1) * 6;
        return (char)(128 + (byte)(this.code >> n & 0x3F));
    }

    private void eat() {
        --this.octetPos;
        if (this.octetPos == 0) {
            ++this.pos;
            if (!this.isEos()) {
                this.code = this.src.charAt(this.pos);
                this.octetPos = this.octetLen = this.octetLength(this.code);
            }
        }
    }
}

