
package net.reduls.sanmoku.bin;

import java.io.IOException;
import net.reduls.sanmoku.FeatureEx;
import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;
import net.reduls.sanmoku.util.ReadLine;

public final class SanmokuFeatureEx {
    public static void main(String[] arrstring) throws IOException {
        if (arrstring.length != 0) {
            System.err.println("Usage: java net.reduls.igo.bin.SanmokuFeatureEx");
            System.exit(1);
        }
        ReadLine readLine = new ReadLine(System.in);
        String string = readLine.read();
        while (string != null) {
            for (Morpheme morpheme : Tagger.parse(string)) {
                FeatureEx featureEx = new FeatureEx(morpheme);
                String string2 = featureEx.baseform.length() == 0 ? "*" : featureEx.baseform;
                String string3 = featureEx.reading.length() == 0 ? "*" : featureEx.reading;
                String string4 = featureEx.pronunciation.length() == 0 ? "*" : featureEx.pronunciation;
                System.out.println(morpheme.surface + "\t" + morpheme.feature + "," + string2 + "," + string3 + "," + string4);
            }
            System.out.println("EOS");
            string = readLine.read();
        }
    }
}

