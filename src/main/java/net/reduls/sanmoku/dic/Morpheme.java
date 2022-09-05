
package net.reduls.sanmoku.dic;

import java.util.Iterator;
import net.reduls.sanmoku.util.Misc;

public final class Morpheme {
    private static final byte[] morps = Misc.readBytesFromFile("morp.info.bin", 2);
    private static final byte[] morpMap = Misc.readBytesFromFile("morp.info.map", 4);
    private static final byte[] leafs = Misc.readBytesFromFile("morp.leaf.bin", 8);
    private static final byte[] leafAccCounts = Misc.readBytesFromFile("morp.leaf.cnt.bin", 2);
    private static final int nextBase = Misc.readIntFromFile("morp.base.bin");

    public static Iterable<Entry> getMorphemes(final int n) {
        return new Iterable<Entry>(){

            @Override
            public Iterator<Entry> iterator() {
                return new MorphemeIterator(n);
            }
        };
    }

    private static final int nextNode(int n) {
        long l = Morpheme.getLeaf(n);
        if (!Morpheme.hasNext(l, n)) {
            return -1;
        }
        return Morpheme.nextNode(l, n);
    }

    private static final boolean hasNext(long l, int n) {
        long l2 = 1L << n % 64;
        return (l & l2) != 0L;
    }

    private static final int nextNode(long l, int n) {
        int n2 = n / 64;
        int n3 = (leafAccCounts[n2 * 2 + 0] & 0xFF) << 8 | leafAccCounts[n2 * 2 + 1] & 0xFF;
        long l2 = (1L << n % 64) - 1L;
        return nextBase + n3 + Long.bitCount(l & l2);
    }

    private static final long getLeaf(int n) {
        int n2 = n / 64;
        return (long)leafs[n2 * 8 + 0] << 56 | (long)(leafs[n2 * 8 + 1] & 0xFF) << 48 | (long)(leafs[n2 * 8 + 2] & 0xFF) << 40 | (long)(leafs[n2 * 8 + 3] & 0xFF) << 32 | (long)(leafs[n2 * 8 + 4] & 0xFF) << 24 | (long)(leafs[n2 * 8 + 5] & 0xFF) << 16 | (long)(leafs[n2 * 8 + 6] & 0xFF) << 8 | (long)(leafs[n2 * 8 + 7] & 0xFF);
    }

    static class Entry {
        public final short posId;
        public final short cost;
        public final int morphemeId;

        private Entry(int n) {
            int n2 = (morps[n * 2 + 0] & 0xFF) << 8 | morps[n * 2 + 1] & 0xFF;
            this.posId = (short)((short)(morpMap[n2 * 4 + 0] << 8) | (short)(morpMap[n2 * 4 + 1] & 0xFF));
            this.cost = (short)((short)(morpMap[n2 * 4 + 2] << 8) | (short)(morpMap[n2 * 4 + 3] & 0xFF));
            this.morphemeId = n;
        }
    }

    static class MorphemeIterator
    implements Iterator<Entry> {
        private int node;

        public MorphemeIterator(int n) {
            this.node = n;
        }

        @Override
        public boolean hasNext() {
            return this.node != -1;
        }

        @Override
        public Entry next() {
            Entry entry = new Entry(this.node);
            this.node = Morpheme.nextNode(this.node);
            return entry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

