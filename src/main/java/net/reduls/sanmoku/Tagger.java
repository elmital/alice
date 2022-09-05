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

package net.reduls.sanmoku;

import java.util.ArrayList;
import java.util.List;
import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.dic.Matrix;
import net.reduls.sanmoku.dic.PartsOfSpeech;
import net.reduls.sanmoku.dic.Unknown;
import net.reduls.sanmoku.dic.ViterbiNode;
import net.reduls.sanmoku.dic.WordDic;

public final class Tagger {
    private static final ArrayList<ViterbiNode> BOS_NODES = new ArrayList(1);

    public static List<Morpheme> parse(String string) {
        return Tagger.parse(string, new ArrayList<Morpheme>(string.length() / 2));
    }

    public static List<Morpheme> parse(String string, List<Morpheme> list) {
        ViterbiNode viterbiNode = Tagger.parseImpl(string);
        while (viterbiNode != null) {
            String string2 = string.substring(viterbiNode.start, viterbiNode.start + viterbiNode.length());
            String string3 = PartsOfSpeech.get(viterbiNode.posId());
            list.add(new Morpheme(string2, string3, viterbiNode.start, viterbiNode.morphemeId));
            viterbiNode = viterbiNode.prev;
        }
        return list;
    }

    public static List<String> wakati(String string) {
        return Tagger.wakati(string, new ArrayList<String>(string.length() / 1));
    }

    public static List<String> wakati(String string, List<String> list) {
        ViterbiNode viterbiNode = Tagger.parseImpl(string);
        while (viterbiNode != null) {
            list.add(string.substring(viterbiNode.start, viterbiNode.start + viterbiNode.length()));
            viterbiNode = viterbiNode.prev;
        }
        return list;
    }

    public static ViterbiNode parseImpl(String string) {
        int n = string.length();
        ArrayList<ArrayList<ViterbiNode>> arrayList = new ArrayList<ArrayList<ViterbiNode>>(n + 1);
        arrayList.add(BOS_NODES);
        for (int i = 1; i <= n; ++i) {
            arrayList.add(new ArrayList());
        }
        MakeLattice makeLattice = new MakeLattice(arrayList);
        for (int i = 0; i < n; ++i) {
            if (arrayList.get(i).isEmpty()) continue;
            makeLattice.set(i);
            WordDic.search(string, i, makeLattice);
            Unknown.search(string, i, makeLattice);
            if (i <= 0) continue;
            arrayList.get(i).clear();
        }
        ViterbiNode viterbiNode = Tagger.setMincostNode((ViterbiNode)ViterbiNode.makeBOSEOS(), arrayList.get((int)n)).prev;
        ViterbiNode viterbiNode2 = null;
        while (viterbiNode.prev != null) {
            ViterbiNode viterbiNode3 = viterbiNode.prev;
            viterbiNode.prev = viterbiNode2;
            viterbiNode2 = viterbiNode;
            viterbiNode = viterbiNode3;
        }
        return viterbiNode2;
    }

    private static ViterbiNode setMincostNode(ViterbiNode viterbiNode, ArrayList<ViterbiNode> arrayList) {
        ViterbiNode viterbiNode2 = viterbiNode.prev = arrayList.get(0);
        int n = viterbiNode2.cost + Matrix.linkCost(viterbiNode2.posId(), viterbiNode.posId());
        for (int i = 1; i < arrayList.size(); ++i) {
            ViterbiNode viterbiNode3 = arrayList.get(i);
            int n2 = viterbiNode3.cost + Matrix.linkCost(viterbiNode3.posId(), viterbiNode.posId());
            if (n2 >= n) continue;
            n = n2;
            viterbiNode.prev = viterbiNode3;
        }
        viterbiNode.cost += n;
        return viterbiNode;
    }

    static {
        BOS_NODES.add(ViterbiNode.makeBOSEOS());
    }

    private static final class MakeLattice
    implements WordDic.Callback {
        private final ArrayList<ArrayList<ViterbiNode>> nodesAry;
        private int i;
        private ArrayList<ViterbiNode> prevs;
        private boolean empty = true;

        public MakeLattice(ArrayList<ArrayList<ViterbiNode>> arrayList) {
            this.nodesAry = arrayList;
        }

        public void set(int n) {
            this.i = n;
            this.prevs = this.nodesAry.get(n);
            this.empty = true;
        }

        @Override
        public void call(ViterbiNode viterbiNode) {
            this.empty = false;
            if (viterbiNode.isSpace()) {
                this.nodesAry.get(this.i + viterbiNode.length()).addAll(this.prevs);
            } else {
                this.nodesAry.get(this.i + viterbiNode.length()).add(Tagger.setMincostNode(viterbiNode, this.prevs));
            }
        }

        @Override
        public boolean isEmpty() {
            return this.empty;
        }
    }
}

