
package net.reduls.sanmoku.dic;

import net.reduls.sanmoku.dic.CodeStream;
import net.reduls.sanmoku.dic.WordDic;
import net.reduls.sanmoku.util.Misc;

public final class SurfaceId {
    private static final int idOffset;
    private static final byte[] nodes;
    private static final byte[] exts;
    private static final byte[] char_to_chck;

    public static void eachCommonPrefix(String string, int n, WordDic.Callback callback) {
        long l = SurfaceId.getNode(0);
        int n2 = idOffset;
        CodeStream codeStream = new CodeStream(string, n);
        while (true) {
            if (SurfaceId.isTerminal(l)) {
                WordDic.eachViterbiNode(callback, n2++, n, codeStream.position() - n, false);
            }
            if (codeStream.isEos()) {
                return;
            }
            if (!SurfaceId.checkEncodedChildren(codeStream, l)) {
                return;
            }
            char c = SurfaceId.read(codeStream);
            long l2 = SurfaceId.getNode(SurfaceId.base(l) + c);
            if (SurfaceId.chck(l2) != c) {
                return;
            }
            l = l2;
            n2 += SurfaceId.siblingTotal(l);
        }
    }

    private static char read(CodeStream codeStream) {
        return (char)(char_to_chck[codeStream.read()] & 0xFF);
    }

    private static boolean checkEncodedChildren(CodeStream codeStream, long l) {
        switch (SurfaceId.type(l)) {
            case 0: {
                return SurfaceId.checkEC(codeStream, l);
            }
        }
        return true;
    }

    private static boolean checkEC(CodeStream codeStream, long l) {
        char c = (char)(l >> 27 & 0x7FL);
        return c == '\u0000' || SurfaceId.read(codeStream) == c && !codeStream.isEos();
    }

    private static char chck(long l) {
        return (char)(l >> 20 & 0x7FL);
    }

    private static int base(long l) {
        return (int)(l & 0x7FFFFL);
    }

    private static boolean isTerminal(long l) {
        return (l >> 19 & 1L) == 1L;
    }

    private static int type(long l) {
        if ((l >> 39 & 1L) == 1L) {
            return 2 + (int)(l >> 38 & 1L);
        }
        return 0;
    }

    private static int siblingTotal(long l) {
        switch (SurfaceId.type(l)) {
            case 0: {
                return (int)(l >> 34 & 0x1FL);
            }
            case 2: {
                return (int)(l >> 27 & 0x7FFL);
            }
        }
        int n = (int)(l >> 27 & 0x7FFL);
        return (exts[n * 4 + 0] & 0xFF) << 24 | (exts[n * 4 + 1] & 0xFF) << 16 | (exts[n * 4 + 2] & 0xFF) << 8 | (exts[n * 4 + 3] & 0xFF) << 0;
    }

    private static long getNode(int n) {
        return (long)(nodes[n * 5 + 0] & 0xFF) << 32 | (long)(nodes[n * 5 + 1] & 0xFF) << 24 | (long)(nodes[n * 5 + 2] & 0xFF) << 16 | (long)(nodes[n * 5 + 3] & 0xFF) << 8 | (long)(nodes[n * 5 + 4] & 0xFF);
    }

    static {
        nodes = Misc.readBytesFromFile("surface-id.bin.node", 1);
        exts = Misc.readBytesFromFile("surface-id.bin.ext", 1);
        char_to_chck = Misc.readBytesFromFile("surface-id.bin.char", 256, 1);
        idOffset = Misc.readIntFromFile("category.bin");
    }
}

