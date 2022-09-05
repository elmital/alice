
package org.alicebot.ab;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;

public class JapaneseTokenizer {
    static final Pattern tagPattern = Pattern.compile("(<.*>.*</.*>)|(<.*/>)");
    static Set<Character.UnicodeBlock> japaneseUnicodeBlocks = new HashSet<Character.UnicodeBlock>(){
        {
            this.add(Character.UnicodeBlock.HIRAGANA);
            this.add(Character.UnicodeBlock.KATAKANA);
            this.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        }
    };

    public static String buildFragment(String fragment) {
        String result = "";
        for (Morpheme e : Tagger.parse(fragment)) {
            result = result + e.surface + " ";
        }
        return result.trim();
    }

    public static String morphSentence(String sentence) {
        if (!MagicBooleans.jp_morphological_analysis) {
            return sentence;
        }
        String result = "";
        Matcher matcher = tagPattern.matcher(sentence);
        while (matcher.find()) {
            int i = matcher.start();
            int j = matcher.end();
            String prefix = i > 0 ? sentence.substring(0, i - 1) : "";
            String tag = sentence.substring(i, j);
            result = result + " " + JapaneseTokenizer.buildFragment(prefix) + " " + tag;
            if (j < sentence.length()) {
                sentence = sentence.substring(j, sentence.length());
                continue;
            }
            sentence = "";
        }
        result = result + " " + JapaneseTokenizer.buildFragment(sentence);
        while (result.contains("$ ")) {
            result = result.replace("$ ", "$");
        }
        while (result.contains("  ")) {
            result = result.replace("  ", " ");
        }
        return result.trim();
    }
}

