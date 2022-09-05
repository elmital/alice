
package org.alicebot.ab;

import java.io.BufferedWriter;
import java.io.FileWriter;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.History;
import org.alicebot.ab.JapaneseTokenizer;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.Predicates;
import org.alicebot.ab.utils.IOUtils;

public class Chat {
    public Bot bot;
    public String customerId = MagicStrings.unknown_customer_id;
    public History<History> thatHistory = new History("that");
    public History<String> requestHistory = new History("request");
    public History<String> responseHistory = new History("response");
    public History<String> inputHistory = new History("input");
    public Predicates predicates = new Predicates();
    public static String matchTrace = "";
    public static boolean locationKnown = false;
    public static String longitude;
    public static String latitude;

    public Chat(Bot bot) {
        this(bot, "0");
    }

    public Chat(Bot bot, String customerId) {
        this.customerId = customerId;
        this.bot = bot;
        History<String> contextThatHistory = new History<String>();
        contextThatHistory.add(MagicStrings.default_that);
        this.thatHistory.add(contextThatHistory);
        this.addPredicates();
        this.predicates.put("topic", MagicStrings.default_topic);
    }

    void addPredicates() {
        try {
            this.predicates.getPredicateDefaults(MagicStrings.config_path + "/predicates.txt");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void chat() {
        BufferedWriter bw = null;
        String logFile = MagicStrings.log_path + "/log_" + this.customerId + ".txt";
        try {
            bw = new BufferedWriter(new FileWriter(logFile, true));
            String request = "SET PREDICATES";
            String response = this.multisentenceRespond(request);
            while (!request.equals("quit")) {
                System.out.print("Human: ");
                request = IOUtils.readInputTextLine();
                response = this.multisentenceRespond(request);
                System.out.println("Robot: " + response);
                bw.write("Human: " + request);
                bw.newLine();
                bw.write("Robot: " + response);
                bw.newLine();
                bw.flush();
            }
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    String respond(String input, String that, String topic, History contextThatHistory) {
        this.inputHistory.add(input);
        String response = AIMLProcessor.respond(input, that, topic, this);
        String normResponse = this.bot.preProcessor.normalize(response);
        normResponse = JapaneseTokenizer.morphSentence(normResponse);
        String[] sentences = this.bot.preProcessor.sentenceSplit(normResponse);
        for (int i = 0; i < sentences.length; ++i) {
            that = sentences[i];
            if (that.trim().equals("")) {
                that = MagicStrings.default_that;
            }
            contextThatHistory.add(that);
        }
        return response.trim() + "  ";
    }

    String respond(String input, History<String> contextThatHistory) {
        History hist = this.thatHistory.get(0);
        String that = hist == null ? MagicStrings.default_that : hist.getString(0);
        return this.respond(input, that, this.predicates.get("topic"), contextThatHistory);
    }

    public String multisentenceRespond(String request) {
        String response = "";
        matchTrace = "";
        try {
            String norm = this.bot.preProcessor.normalize(request);
            norm = JapaneseTokenizer.morphSentence(norm);
            if (MagicBooleans.trace_mode) {
                System.out.println("normalized = " + norm);
            }
            String[] sentences = this.bot.preProcessor.sentenceSplit(norm);
            History<String> contextThatHistory = new History<String>("contextThat");
            for (int i = 0; i < sentences.length; ++i) {
                AIMLProcessor.trace_count = 0;
                String reply = this.respond(sentences[i], contextThatHistory);
                response = response + "  " + reply;
            }
            this.requestHistory.add(request);
            this.responseHistory.add(response);
            this.thatHistory.add(contextThatHistory);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return MagicStrings.error_bot_response;
        }
        this.bot.writeLearnfIFCategories();
        return response.trim();
    }

    public static void setMatchTrace(String newMatchTrace) {
        matchTrace = newMatchTrace;
    }
}

