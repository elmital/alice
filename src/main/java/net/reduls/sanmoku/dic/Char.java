
package net.reduls.sanmoku.dic;

import java.io.DataInputStream;
import net.reduls.sanmoku.util.Misc;

public final class Char {
    private static final Category[] charCategorys;
    private static final byte[] charInfos;

    public static final Category category(char c) {
        return charCategorys[Char.findNode(c) >> 16];
    }

    public static final boolean isCompatible(char c, char c2) {
        return (Char.compatibleMask(c) & Char.compatibleMask(c2)) != 0;
    }

    private static final int compatibleMask(char c) {
        return Char.findNode(c) & 0xFFFF;
    }

    public static final int findNode(char c) {
        int n = 0;
        int n2 = charInfos.length / 6;
        while (true) {
            int n3 = n + (n2 - n) / 2;
            if (n2 - n == 1) {
                return Char.nodeValue(n);
            }
            if (c < Char.nodeCode(n3)) {
                n2 = n3;
                continue;
            }
            if (c < Char.nodeCode(n3)) continue;
            n = n3;
        }
    }

    public static final int nodeCode(int n) {
        return (charInfos[n * 6 + 0] & 0xFF) << 16 | (charInfos[n * 6 + 1] & 0xFF) << 8 | (charInfos[n * 6 + 2] & 0xFF) << 0;
    }

    public static final int nodeValue(int n) {
        return (charInfos[n * 6 + 3] & 0xFF) << 16 | (charInfos[n * 6 + 4] & 0xFF) << 8 | (charInfos[n * 6 + 5] & 0xFF) << 0;
    }

    static {
        DataInputStream dataInputStream = Misc.openDictionaryDataAsDIS("category.bin");
        int n = Misc.readInt(dataInputStream);
        charCategorys = new Category[n];
        for (int i = 0; i < n; ++i) {
            Char.charCategorys[i] = new Category(i, Misc.readByte(dataInputStream) == 1, Misc.readByte(dataInputStream) == 1, Misc.readByte(dataInputStream));
        }
        Misc.close(dataInputStream);
        charInfos = Misc.readBytesFromFile("code.bin", 6);
    }

    public static final class Category {
        public final int id;
        public final boolean invoke;
        public final boolean group;
        public final byte length;

        public Category(int n, boolean bl, boolean bl2, byte by) {
            this.id = n;
            this.invoke = bl;
            this.group = bl2;
            this.length = by;
        }
    }
}

