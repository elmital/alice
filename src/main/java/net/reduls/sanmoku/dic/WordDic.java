
package net.reduls.sanmoku.dic;

import net.reduls.sanmoku.dic.Morpheme;
import net.reduls.sanmoku.dic.SurfaceId;
import net.reduls.sanmoku.dic.ViterbiNode;

public final class WordDic {
    public static void search(String string, int n, Callback callback) {
        SurfaceId.eachCommonPrefix(string, n, callback);
    }

    public static void eachViterbiNode(Callback callback, int n, int n2, int n3, boolean bl) {
        for (Morpheme.Entry entry : Morpheme.getMorphemes(n)) {
            callback.call(new ViterbiNode(n2, (short)n3, entry.cost, entry.posId, bl, entry.morphemeId));
        }
    }

    public static interface Callback {
        public void call(ViterbiNode var1);

        public boolean isEmpty();
    }
}

