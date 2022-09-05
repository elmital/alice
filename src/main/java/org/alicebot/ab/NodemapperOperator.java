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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.alicebot.ab.Nodemapper;

public class NodemapperOperator {
    public static int size(Nodemapper node) {
        HashSet<String> set = new HashSet<String>();
        if (node.shortCut) {
            set.add("<THAT>");
        }
        if (node.key != null) {
            set.add(node.key);
        }
        if (node.map != null) {
            set.addAll(node.map.keySet());
        }
        return set.size();
    }

    public static void put(Nodemapper node, String key, Nodemapper value) {
        if (node.map != null) {
            node.map.put(key, value);
        } else {
            node.key = key;
            node.value = value;
        }
    }

    public static Nodemapper get(Nodemapper node, String key) {
        if (node.map != null) {
            return node.map.get(key);
        }
        if (key.equals(node.key)) {
            return node.value;
        }
        return null;
    }

    public static boolean containsKey(Nodemapper node, String key) {
        if (node.map != null) {
            return node.map.containsKey(key);
        }
        return key.equals(node.key);
    }

    public static void printKeys(Nodemapper node) {
        Set<String> set = NodemapperOperator.keySet(node);
        Iterator<String> iter = set.iterator();
        while (iter.hasNext()) {
            System.out.println("" + iter.next());
        }
    }

    public static Set<String> keySet(Nodemapper node) {
        if (node.map != null) {
            return node.map.keySet();
        }
        HashSet<String> set = new HashSet<String>();
        if (node.key != null) {
            set.add(node.key);
        }
        return set;
    }

    public static boolean isLeaf(Nodemapper node) {
        return node.category != null;
    }

    public static void upgrade(Nodemapper node) {
        node.map = new HashMap();
        node.map.put(node.key, node.value);
        node.key = null;
        node.value = null;
    }
}

