
/*
 * The MIT License
 *
 * Copyright (c) 2011 Takeru Ohta <phjgt308@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

