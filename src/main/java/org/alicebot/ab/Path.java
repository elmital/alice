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

import java.util.ArrayList;

public class Path
extends ArrayList<String> {
    public String word = null;
    public Path next = null;
    public int length = 0;

    private Path() {
    }

    public static Path sentenceToPath(String sentence) {
        sentence = sentence.trim();
        return Path.arrayToPath(sentence.split(" "));
    }

    public static String pathToSentence(Path path) {
        String result = "";
        Path p = path;
        while (p != null) {
            result = result + " " + path.word;
            p = p.next;
        }
        return result.trim();
    }

    private static Path arrayToPath(String[] array) {
        Path tail = null;
        Path head = null;
        for (int i = array.length - 1; i >= 0; --i) {
            head = new Path();
            head.word = array[i];
            head.next = tail;
            head.length = tail == null ? 1 : tail.length + 1;
            tail = head;
        }
        return head;
    }

    private static Path arrayToPath(String[] array, int index) {
        if (index >= array.length) {
            return null;
        }
        Path newPath = new Path();
        newPath.word = array[index];
        newPath.next = Path.arrayToPath(array, index + 1);
        newPath.length = newPath.next == null ? 1 : newPath.next.length + 1;
        return newPath;
    }

    public void print() {
        String result = "";
        Path p = this;
        while (p != null) {
            result = result + p.word + ",";
            p = p.next;
        }
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        System.out.println(result);
    }
}

