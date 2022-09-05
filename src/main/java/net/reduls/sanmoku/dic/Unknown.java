
package net.reduls.sanmoku.dic;

import net.reduls.sanmoku.dic.Char;
import net.reduls.sanmoku.dic.WordDic;

public final class Unknown {
    private static final Char.Category space = Char.category(' ');

    public static void search(String string, int n, WordDic.Callback callback) {
        int n2;
        char c = string.charAt(n);
        Char.Category category = Char.category(c);
        if (!callback.isEmpty() && !category.invoke) {
            return;
        }
        boolean bl = category == space;
        int n3 = Math.min(string.length(), category.length + n);
        for (n2 = n; n2 < n3; ++n2) {
            WordDic.eachViterbiNode(callback, category.id, n, n2 - n + 1, bl);
            if (n2 + 1 == n3 || Char.isCompatible(c, string.charAt(n2 + 1))) continue;
            return;
        }
        if (category.group && n2 < string.length()) {
            while (n2 < string.length()) {
                if (!Char.isCompatible(c, string.charAt(n2))) {
                    WordDic.eachViterbiNode(callback, category.id, n, n2 - n, bl);
                    return;
                }
                ++n2;
            }
            WordDic.eachViterbiNode(callback, category.id, n, string.length() - n, bl);
        }
    }
}

