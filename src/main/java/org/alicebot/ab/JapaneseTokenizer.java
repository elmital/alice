
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org
        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.
        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.
        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/

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

