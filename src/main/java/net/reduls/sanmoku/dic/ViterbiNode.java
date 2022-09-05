
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

