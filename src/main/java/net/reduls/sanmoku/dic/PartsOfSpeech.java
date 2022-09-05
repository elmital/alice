
package net.reduls.sanmoku.dic;

import java.io.BufferedReader;
import java.util.ArrayList;
import net.reduls.sanmoku.util.Misc;

public final class PartsOfSpeech {
    private static final String[] posArray;

    public static final String get(int n) {
        return posArray[n];
    }

    static {
        BufferedReader bufferedReader = Misc.openDictionaryDataAsBR("pos.bin");
        ArrayList<String> arrayList = new ArrayList<String>();
        String string = Misc.readLine(bufferedReader);
        while (string != null) {
            arrayList.add(string);
            string = Misc.readLine(bufferedReader);
        }
        Misc.close(bufferedReader);
        posArray = new String[arrayList.size()];
        for (int i = 0; i < posArray.length; ++i) {
            PartsOfSpeech.posArray[i] = (String)arrayList.get(i);
        }
    }
}

