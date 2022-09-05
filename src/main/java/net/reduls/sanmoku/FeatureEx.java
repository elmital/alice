
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

