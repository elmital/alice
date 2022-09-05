
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

