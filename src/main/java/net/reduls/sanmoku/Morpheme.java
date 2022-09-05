
package net.reduls.sanmoku;

public final class Morpheme {
    public final String surface;
    public final String feature;
    public final int start;
    final int morphemeId;

    public Morpheme(String string, String string2, int n, int n2) {
        this.surface = string;
        this.feature = string2;
        this.start = n;
        this.morphemeId = n2;
    }
}

