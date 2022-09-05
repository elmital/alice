
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

