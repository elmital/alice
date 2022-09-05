
package net.reduls.sanmoku.bin;

import java.io.IOException;
import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;
import net.reduls.sanmoku.util.ReadLine;

public final class Sanmoku {
    public static void main(String[] arrstring) throws IOException {
        if (!(arrstring.length == 0 || arrstring.length == 1 && arrstring[0].equals("-wakati"))) {
            System.err.println("Usage: java net.reduls.igo.bin.Sanmoku [-wakati]");
            System.exit(1);
        }
        boolean bl = arrstring.length == 1;
        ReadLine readLine = new ReadLine(System.in);
        if (bl) {
            String string = readLine.read();
            while (string != null) {
                for (String string2 : Tagger.wakati(string)) {
                    System.out.print(string2 + " ");
                }
                System.out.println("");
                string = readLine.read();
            }
        } else {
            String string = readLine.read();
            while (string != null) {
                for (Morpheme morpheme : Tagger.parse(string)) {
                    System.out.println(morpheme.surface + "\t" + morpheme.feature);
                }
                System.out.println("EOS");
                string = readLine.read();
            }
        }
    }
}

