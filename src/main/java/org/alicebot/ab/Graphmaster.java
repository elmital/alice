
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
import java.util.HashSet;
import org.alicebot.ab.AIMLSet;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Category;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicNumbers;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.Nodemapper;
import org.alicebot.ab.NodemapperOperator;
import org.alicebot.ab.Path;
import org.alicebot.ab.StarBindings;
import org.alicebot.ab.Utilities;

public class Graphmaster {
    public Bot bot;
    public final Nodemapper root = new Nodemapper();
    public int matchCount = 0;
    public int upgradeCnt = 0;
    public HashSet<String> vocabulary;
    public String resultNote = "";
    public int categoryCnt = 0;
    public static boolean enableShortCuts = false;
    public static boolean verbose = false;
    int leafCnt;
    int nodeCnt;
    long nodeSize;
    int singletonCnt;
    int shortCutCnt;
    int naryCnt;

    public Graphmaster(Bot bot) {
        this.bot = bot;
        this.vocabulary = new HashSet();
    }

    public static String inputThatTopic(String input, String that, String topic) {
        return input.trim() + " <THAT> " + that.trim() + " <TOPIC> " + topic.trim();
    }

    public void addCategory(Category category) {
        Path p = Path.sentenceToPath(Graphmaster.inputThatTopic(category.getPattern(), category.getThat(), category.getTopic()));
        this.addPath(p, category);
        ++this.categoryCnt;
    }

    boolean thatStarTopicStar(Path path) {
        String tail = Path.pathToSentence(path).trim();
        return tail.equals("<THAT> * <TOPIC> *");
    }

    void addSets(String type, Bot bot, Nodemapper node) {
        String typeName = Utilities.tagTrim(type, "SET").toLowerCase();
        if (bot.setMap.containsKey(typeName)) {
            if (node.sets == null) {
                node.sets = new ArrayList();
            }
            node.sets.add(typeName);
        } else {
            System.out.println("AIML Set " + typeName + " not found.");
        }
    }

    void addPath(Path path, Category category) {
        this.addPath(this.root, path, category);
    }

    void addPath(Nodemapper node, Path path, Category category) {
        if (path == null) {
            node.category = category;
            node.height = 0;
        } else if (enableShortCuts && this.thatStarTopicStar(path)) {
            node.category = category;
            node.height = Math.min(4, node.height);
            node.shortCut = true;
        } else if (NodemapperOperator.containsKey(node, path.word)) {
            if (path.word.startsWith("<SET>")) {
                this.addSets(path.word, this.bot, node);
            }
            Nodemapper nextNode = NodemapperOperator.get(node, path.word);
            this.addPath(nextNode, path.next, category);
            int offset = 1;
            if (path.word.equals("#") || path.word.equals("^")) {
                offset = 0;
            }
            node.height = Math.min(offset + nextNode.height, node.height);
        } else {
            Nodemapper nextNode = new Nodemapper();
            if (path.word.startsWith("<SET>")) {
                this.addSets(path.word, this.bot, node);
            }
            if (node.key != null) {
                NodemapperOperator.upgrade(node);
                ++this.upgradeCnt;
            }
            NodemapperOperator.put(node, path.word, nextNode);
            this.addPath(nextNode, path.next, category);
            int offset = 1;
            if (path.word.equals("#") || path.word.equals("^")) {
                offset = 0;
            }
            node.height = Math.min(offset + nextNode.height, node.height);
        }
    }

    public boolean existsCategory(Category c) {
        return this.findNode(c) != null;
    }

    public Nodemapper findNode(Category c) {
        return this.findNode(c.getPattern(), c.getThat(), c.getTopic());
    }

    public Nodemapper findNode(String input, String that, String topic) {
        Nodemapper result = this.findNode(this.root, Path.sentenceToPath(Graphmaster.inputThatTopic(input, that, topic)));
        if (verbose) {
            System.out.println("findNode " + Graphmaster.inputThatTopic(input, that, topic) + " " + result);
        }
        return result;
    }

    Nodemapper findNode(Nodemapper node, Path path) {
        if (path == null && node != null) {
            if (verbose) {
                System.out.println("findNode: path is null, returning node " + node.category.inputThatTopic());
            }
            return node;
        }
        if (Path.pathToSentence(path).trim().equals("<THAT> * <TOPIC> *") && node.shortCut && path.word.equals("<THAT>")) {
            if (verbose) {
                System.out.println("findNode: shortcut, returning " + node.category.inputThatTopic());
            }
            return node;
        }
        if (NodemapperOperator.containsKey(node, path.word)) {
            if (verbose) {
                System.out.println("findNode: node contains " + path.word);
            }
            Nodemapper nextNode = NodemapperOperator.get(node, path.word.toUpperCase());
            return this.findNode(nextNode, path.next);
        }
        if (verbose) {
            System.out.println("findNode: returning null");
        }
        return null;
    }

    public final Nodemapper match(String input, String that, String topic) {
        Nodemapper n = null;
        try {
            String inputThatTopic = Graphmaster.inputThatTopic(input, that, topic);
            Path p = Path.sentenceToPath(inputThatTopic);
            n = this.match(p, inputThatTopic);
            if (MagicBooleans.trace_mode) {
                if (n != null) {
                    System.out.println("Matched: " + n.category.inputThatTopic() + " " + n.category.getFilename());
                } else {
                    System.out.println("No match.");
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            n = null;
        }
        if (MagicBooleans.trace_mode && Chat.matchTrace.length() < MagicNumbers.max_trace_length && n != null) {
            Chat.setMatchTrace(Chat.matchTrace + n.category.inputThatTopic() + "\n");
        }
        return n;
    }

    final Nodemapper match(Path path, String inputThatTopic) {
        try {
            String[] inputStars = new String[MagicNumbers.max_stars];
            String[] thatStars = new String[MagicNumbers.max_stars];
            String[] topicStars = new String[MagicNumbers.max_stars];
            String starState = "inputStar";
            String matchTrace = "";
            Nodemapper n = this.match(path, this.root, inputThatTopic, starState, 0, inputStars, thatStars, topicStars, matchTrace);
            if (n != null) {
                int i;
                StarBindings sb = new StarBindings();
                for (i = 0; inputStars[i] != null && i < MagicNumbers.max_stars; ++i) {
                    sb.inputStars.add(inputStars[i]);
                }
                for (i = 0; thatStars[i] != null && i < MagicNumbers.max_stars; ++i) {
                    sb.thatStars.add(thatStars[i]);
                }
                for (i = 0; topicStars[i] != null && i < MagicNumbers.max_stars; ++i) {
                    sb.topicStars.add(topicStars[i]);
                }
                n.starBindings = sb;
            }
            if (n != null) {
                n.category.addMatch(inputThatTopic);
            }
            return n;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    final Nodemapper match(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        ++this.matchCount;
        Nodemapper matchedNode = this.nullMatch(path, node, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        if (path.length < node.height) {
            return null;
        }
        matchedNode = this.dollarMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        matchedNode = this.sharpMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        matchedNode = this.underMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        matchedNode = this.wordMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        matchedNode = this.setMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        matchedNode = this.shortCutMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        matchedNode = this.caretMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        matchedNode = this.starMatch(path, node, inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        return null;
    }

    void fail(String mode, String trace) {
    }

    final Nodemapper nullMatch(Path path, Nodemapper node, String matchTrace) {
        if (path == null && node != null && NodemapperOperator.isLeaf(node) && node.category != null) {
            return node;
        }
        this.fail("null", matchTrace);
        return null;
    }

    final Nodemapper shortCutMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        if (node != null && node.shortCut && path.word.equals("<THAT>") && node.category != null) {
            String tail = Path.pathToSentence(path).trim();
            String that = tail.substring(tail.indexOf("<THAT>") + "<THAT>".length(), tail.indexOf("<TOPIC>")).trim();
            String topic = tail.substring(tail.indexOf("<TOPIC>") + "<TOPIC>".length(), tail.length()).trim();
            thatStars[0] = that;
            topicStars[0] = topic;
            return node;
        }
        this.fail("shortCut", matchTrace);
        return null;
    }

    final Nodemapper wordMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        try {
            Nodemapper matchedNode;
            String uword = path.word.toUpperCase();
            if (uword.equals("<THAT>")) {
                starIndex = 0;
                starState = "thatStar";
            } else if (uword.equals("<TOPIC>")) {
                starIndex = 0;
                starState = "topicStar";
            }
            matchTrace = matchTrace + "[" + uword + "," + uword + "]";
            if (path != null && NodemapperOperator.containsKey(node, uword) && (matchedNode = this.match(path.next, NodemapperOperator.get(node, uword), inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) {
                return matchedNode;
            }
            this.fail("word", matchTrace);
            return null;
        }
        catch (Exception ex) {
            System.out.println("wordMatch: " + Path.pathToSentence(path) + ": " + ex);
            ex.printStackTrace();
            return null;
        }
    }

    final Nodemapper dollarMatch(Path path, Nodemapper node, String inputThatTopic, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode;
        String uword = "$" + path.word.toUpperCase();
        if (path != null && NodemapperOperator.containsKey(node, uword) && (matchedNode = this.match(path.next, NodemapperOperator.get(node, uword), inputThatTopic, starState, starIndex, inputStars, thatStars, topicStars, matchTrace)) != null) {
            return matchedNode;
        }
        this.fail("dollar", matchTrace);
        return null;
    }

    final Nodemapper starMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        return this.wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "*", matchTrace);
    }

    final Nodemapper underMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        return this.wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "_", matchTrace);
    }

    final Nodemapper caretMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode = this.zeroMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "^", matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        return this.wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "^", matchTrace);
    }

    final Nodemapper sharpMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        Nodemapper matchedNode = this.zeroMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "#", matchTrace);
        if (matchedNode != null) {
            return matchedNode;
        }
        return this.wildMatch(path, node, input, starState, starIndex, inputStars, thatStars, topicStars, "#", matchTrace);
    }

    final Nodemapper zeroMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String wildcard, String matchTrace) {
        matchTrace = matchTrace + "[" + wildcard + ",]";
        if (path != null && NodemapperOperator.containsKey(node, wildcard)) {
            this.setStars(this.bot.properties.get(MagicStrings.null_star), starIndex, starState, inputStars, thatStars, topicStars);
            Nodemapper nextNode = NodemapperOperator.get(node, wildcard);
            return this.match(path, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace);
        }
        this.fail("zero " + wildcard, matchTrace);
        return null;
    }

    final Nodemapper wildMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String wildcard, String matchTrace) {
        if (path.word.equals("<THAT>") || path.word.equals("<TOPIC>")) {
            this.fail("wild1 " + wildcard, matchTrace);
            return null;
        }
        try {
            if (path != null && NodemapperOperator.containsKey(node, wildcard)) {
                matchTrace = matchTrace + "[" + wildcard + "," + path.word + "]";
                String currentWord = path.word;
                String starWords = currentWord + " ";
                Path pathStart = path.next;
                Nodemapper nextNode = NodemapperOperator.get(node, wildcard);
                if (NodemapperOperator.isLeaf(nextNode) && !nextNode.shortCut) {
                    Nodemapper matchedNode = nextNode;
                    starWords = Path.pathToSentence(path);
                    this.setStars(starWords, starIndex, starState, inputStars, thatStars, topicStars);
                    return matchedNode;
                }
                path = pathStart;
                while (path != null && !currentWord.equals("<THAT>") && !currentWord.equals("<TOPIC>")) {
                    Nodemapper matchedNode = this.match(path, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace = matchTrace + "[" + wildcard + "," + path.word + "]");
                    if (matchedNode != null) {
                        this.setStars(starWords, starIndex, starState, inputStars, thatStars, topicStars);
                        return matchedNode;
                    }
                    currentWord = path.word;
                    starWords = starWords + currentWord + " ";
                    path = path.next;
                }
                this.fail("wild2 " + wildcard, matchTrace);
                return null;
            }
        }
        catch (Exception ex) {
            System.out.println("wildMatch: " + Path.pathToSentence(path) + ": " + ex);
        }
        this.fail("wild3 " + wildcard, matchTrace);
        return null;
    }

    final Nodemapper setMatch(Path path, Nodemapper node, String input, String starState, int starIndex, String[] inputStars, String[] thatStars, String[] topicStars, String matchTrace) {
        if (node.sets == null || path.word.equals("<THAT>") || path.word.equals("<TOPIC>")) {
            return null;
        }
        for (String setName : node.sets) {
            Nodemapper nextNode = NodemapperOperator.get(node, "<SET>" + setName.toUpperCase() + "</SET>");
            AIMLSet aimlSet = this.bot.setMap.get(setName);
            String currentWord = path.word;
            String starWords = currentWord + " ";
            matchTrace = matchTrace + "[<set>" + setName + "</set>," + path.word + "]";
            Path qath = path.next;
            for (int length = 1; qath != null && !currentWord.equals("<THAT>") && !currentWord.equals("<TOPIC>") && length <= aimlSet.maxLength; ++length) {
                Nodemapper matchedNode;
                String phrase = this.bot.preProcessor.normalize(starWords.trim()).toUpperCase();
                if (aimlSet.contains(phrase) && (matchedNode = this.match(qath, nextNode, input, starState, starIndex + 1, inputStars, thatStars, topicStars, matchTrace)) != null) {
                    this.setStars(starWords, starIndex, starState, inputStars, thatStars, topicStars);
                    return matchedNode;
                }
                currentWord = qath.word;
                starWords = starWords + currentWord + " ";
                qath = qath.next;
            }
        }
        this.fail("set", matchTrace);
        return null;
    }

    public void setStars(String starWords, int starIndex, String starState, String[] inputStars, String[] thatStars, String[] topicStars) {
        if (starIndex < MagicNumbers.max_stars) {
            starWords = starWords.trim();
            if (starState.equals("inputStar")) {
                inputStars[starIndex] = starWords;
            } else if (starState.equals("thatStar")) {
                thatStars[starIndex] = starWords;
            } else if (starState.equals("topicStar")) {
                topicStars[starIndex] = starWords;
            }
        }
    }

    public void printgraph() {
        this.printgraph(this.root, "");
    }

    void printgraph(Nodemapper node, String partial) {
        if (node == null) {
            System.out.println("Null graph");
        } else {
            String template = "";
            if (NodemapperOperator.isLeaf(node) || node.shortCut) {
                template = Category.templateToLine(node.category.getTemplate());
                template = template.substring(0, Math.min(16, template.length()));
                if (node.shortCut) {
                    System.out.println(partial + "(" + NodemapperOperator.size(node) + "[" + node.key + "," + node.value + "])--<THAT>-->X(1)--*-->X(1)--<TOPIC>-->X(1)--*-->" + template + "...");
                } else {
                    System.out.println(partial + "(" + NodemapperOperator.size(node) + "[" + node.key + "," + node.value + "]) " + template + "...");
                }
            }
            for (String key : NodemapperOperator.keySet(node)) {
                this.printgraph(NodemapperOperator.get(node, key), partial + "(" + NodemapperOperator.size(node) + "[" + node.height + "])--" + key + "-->");
            }
        }
    }

    public ArrayList<Category> getCategories() {
        ArrayList<Category> categories = new ArrayList<Category>();
        this.getCategories(this.root, categories);
        return categories;
    }

    void getCategories(Nodemapper node, ArrayList<Category> categories) {
        if (node == null) {
            return;
        }
        if ((NodemapperOperator.isLeaf(node) || node.shortCut) && node.category != null) {
            categories.add(node.category);
        }
        for (String key : NodemapperOperator.keySet(node)) {
            this.getCategories(NodemapperOperator.get(node, key), categories);
        }
    }

    public void nodeStats() {
        this.leafCnt = 0;
        this.nodeCnt = 0;
        this.nodeSize = 0L;
        this.singletonCnt = 0;
        this.shortCutCnt = 0;
        this.naryCnt = 0;
        this.nodeStatsGraph(this.root);
        this.resultNote = this.nodeCnt + " nodes " + this.singletonCnt + " singletons " + this.leafCnt + " leaves " + this.shortCutCnt + " shortcuts " + this.naryCnt + " n-ary " + this.nodeSize + " branches " + (float)this.nodeSize / (float)this.nodeCnt + " average branching ";
        System.out.println(this.resultNote);
    }

    public void nodeStatsGraph(Nodemapper node) {
        if (node != null) {
            ++this.nodeCnt;
            this.nodeSize += (long)NodemapperOperator.size(node);
            if (NodemapperOperator.size(node) == 1) {
                ++this.singletonCnt;
            }
            if (NodemapperOperator.isLeaf(node) && !node.shortCut) {
                ++this.leafCnt;
            }
            if (NodemapperOperator.size(node) > 1) {
                ++this.naryCnt;
            }
            if (node.shortCut) {
                ++this.shortCutCnt;
            }
            for (String key : NodemapperOperator.keySet(node)) {
                this.nodeStatsGraph(NodemapperOperator.get(node, key));
            }
        }
    }

    public HashSet<String> getVocabulary() {
        this.vocabulary = new HashSet();
        this.getBrainVocabulary(this.root);
        for (String set : this.bot.setMap.keySet()) {
            this.vocabulary.addAll(this.bot.setMap.get(set));
        }
        return this.vocabulary;
    }

    public void getBrainVocabulary(Nodemapper node) {
        if (node != null) {
            for (String key : NodemapperOperator.keySet(node)) {
                this.vocabulary.add(key);
                this.getBrainVocabulary(NodemapperOperator.get(node, key));
            }
        }
    }
}

