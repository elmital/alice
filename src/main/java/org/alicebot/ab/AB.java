
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
import java.util.Collections;
import org.alicebot.ab.AIMLSet;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Category;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicNumbers;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.Timer;
import org.alicebot.ab.utils.IOUtils;

public class AB {
    public static boolean shuffle_mode = false;
    public static boolean sort_mode = true;
    public static boolean filter_atomic_mode = true;
    public static boolean filter_wild_mode = false;
    public static String logfile = MagicStrings.root_path + "/data/" + MagicStrings.ab_sample_file;
    public static AIMLSet passed = new AIMLSet("passed");
    public static AIMLSet testSet = new AIMLSet("1000");
    public static int runCompletedCnt;

    public static void productivity(int runCompletedCnt, Timer timer) {
        float time = timer.elapsedTimeMins();
        System.out.println("Completed " + runCompletedCnt + " in " + time + " min. Productivity " + (float)runCompletedCnt / time + " cat/min");
    }

    public static void saveCategory(Bot bot, String pattern, String template, String filename) {
        String that = "*";
        String topic = "*";
        Category c = new Category(0, pattern, that, topic, template, filename);
        if (c.validate()) {
            bot.brain.addCategory(c);
            bot.writeAIMLIFFiles();
            ++runCompletedCnt;
        } else {
            System.out.println("Invalid Category " + c.validationMessage);
        }
    }

    public static void deleteCategory(Bot bot, Category c) {
        c.setFilename(MagicStrings.deleted_aiml_file);
        c.setTemplate(MagicStrings.deleted_template);
        bot.deletedGraph.addCategory(c);
        bot.writeDeletedIFCategories();
    }

    public static void skipCategory(Bot bot, Category c) {
        c.setFilename(MagicStrings.unfinished_aiml_file);
        c.setTemplate(MagicStrings.unfinished_template);
        bot.unfinishedGraph.addCategory(c);
        System.out.println(bot.unfinishedGraph.getCategories().size() + " unfinished categories");
        bot.writeUnfinishedIFCategories();
    }

    public static void abwq(Bot bot) {
        Timer timer = new Timer();
        timer.start();
        bot.classifyInputs(logfile);
        System.out.println(timer.elapsedTimeSecs() + " classifying inputs");
        bot.writeQuit();
    }

    public static void ab(Bot bot) {
        String logFile = logfile;
        MagicBooleans.trace_mode = false;
        MagicBooleans.enable_external_sets = false;
        Timer timer = new Timer();
        bot.brain.nodeStats();
        timer.start();
        System.out.println("Graphing inputs");
        bot.graphInputs(logFile);
        System.out.println(timer.elapsedTimeSecs() + " seconds Graphing inputs");
        timer.start();
        System.out.println("Finding Patterns");
        bot.findPatterns();
        System.out.println(bot.suggestedCategories.size() + " suggested categories");
        System.out.println(timer.elapsedTimeSecs() + " seconds finding patterns");
        timer.start();
        bot.patternGraph.nodeStats();
        System.out.println("Classifying Inputs");
        bot.classifyInputs(logFile);
        System.out.println(timer.elapsedTimeSecs() + " classifying inputs");
    }

    public static void terminalInteraction(Bot bot) {
        Timer timer = new Timer();
        sort_mode = !shuffle_mode;
        Collections.sort(bot.suggestedCategories, Category.ACTIVATION_COMPARATOR);
        ArrayList<Category> topSuggestCategories = new ArrayList<Category>();
        for (int i = 0; i < 10000 && i < bot.suggestedCategories.size(); ++i) {
            topSuggestCategories.add(bot.suggestedCategories.get(i));
        }
        bot.suggestedCategories = topSuggestCategories;
        if (shuffle_mode) {
            Collections.shuffle(bot.suggestedCategories);
        }
        timer = new Timer();
        timer.start();
        runCompletedCnt = 0;
        ArrayList<Category> filteredAtomicCategories = new ArrayList<Category>();
        ArrayList<Category> filteredWildCategories = new ArrayList<Category>();
        for (Category c : bot.suggestedCategories) {
            if (!c.getPattern().contains("*")) {
                filteredAtomicCategories.add(c);
                continue;
            }
            filteredWildCategories.add(c);
        }
        ArrayList<Category> browserCategories = filter_atomic_mode ? filteredAtomicCategories : (filter_wild_mode ? filteredWildCategories : bot.suggestedCategories);
        for (Category c : browserCategories) {
            try {
                ArrayList<String> samples = new ArrayList<String>(c.getMatches());
                Collections.shuffle(samples);
                int sampleSize = Math.min(MagicNumbers.displayed_input_sample_size, c.getMatches().size());
                for (int i = 0; i < sampleSize; ++i) {
                    System.out.println("" + samples.get(i));
                }
                System.out.println("[" + c.getActivationCnt() + "] " + c.inputThatTopic());
                AB.productivity(runCompletedCnt, timer);
                String textLine = "" + IOUtils.readInputTextLine();
                AB.terminalInteractionStep(bot, "", textLine, c);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Returning to Category Browser");
            }
        }
    }

    public static void terminalInteractionStep(Bot bot, String request, String textLine, Category c) {
        String[] pronouns;
        String template = null;
        if (textLine.contains("<pattern>") && textLine.contains("</pattern>")) {
            int index = textLine.indexOf("<pattern>") + "<pattern>".length();
            int jndex = textLine.indexOf("</pattern>");
            int kndex = jndex + "</pattern>".length();
            if (index < jndex) {
                String pattern = textLine.substring(index, jndex);
                c.setPattern(pattern);
                textLine = textLine.substring(kndex, textLine.length());
                System.out.println("Got pattern = " + pattern + " template = " + textLine);
            }
        }
        String botThinks = "";
        for (String p : pronouns = new String[]{"he", "she", "it", "we", "they"}) {
            if (!textLine.contains("<" + p + ">")) continue;
            textLine = textLine.replace("<" + p + ">", "");
            botThinks = "<think><set name=\"" + p + "\"><set name=\"topic\"><star/></set></set></think>";
        }
        if (textLine.equals("q")) {
            System.exit(0);
        } else if (textLine.equals("wq")) {
            bot.writeQuit();
            System.exit(0);
        } else if (textLine.equals("skip") || textLine.equals("")) {
            AB.skipCategory(bot, c);
        } else if (textLine.equals("s") || textLine.equals("pass")) {
            passed.add(request);
            AIMLSet difference = new AIMLSet("difference");
            difference.addAll(testSet);
            difference.removeAll(passed);
            difference.writeAIMLSet();
            passed.writeAIMLSet();
        } else if (textLine.equals("d")) {
            AB.deleteCategory(bot, c);
        } else if (textLine.equals("x")) {
            template = "<sraix>" + c.getPattern().replace("*", "<star/>") + "</sraix>";
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.sraix_aiml_file);
        } else if (textLine.equals("p")) {
            template = "<srai>" + MagicStrings.inappropriate_filter + "</srai>";
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.inappropriate_aiml_file);
        } else if (textLine.equals("f")) {
            template = "<srai>" + MagicStrings.profanity_filter + "</srai>";
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.profanity_aiml_file);
        } else if (textLine.equals("i")) {
            template = "<srai>" + MagicStrings.insult_filter + "</srai>";
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.insult_aiml_file);
        } else if (textLine.contains("<srai>") || textLine.contains("<sr/>")) {
            template = textLine;
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.reductions_update_aiml_file);
        } else if (textLine.contains("<oob>")) {
            template = textLine;
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.oob_aiml_file);
        } else if (textLine.contains("<set name") || botThinks.length() > 0) {
            template = textLine;
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.predicates_aiml_file);
        } else if (textLine.contains("<get name") && !textLine.contains("<get name=\"name")) {
            template = textLine;
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.predicates_aiml_file);
        } else {
            template = textLine;
            template = template + botThinks;
            AB.saveCategory(bot, c.getPattern(), template, MagicStrings.personality_aiml_file);
        }
    }
}

