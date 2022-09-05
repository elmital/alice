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

