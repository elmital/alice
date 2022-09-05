
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

