package me.apoudra.alice;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.alicebot.ab.AB;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Category;
import org.alicebot.ab.Chat;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.alicebot.ab.utils.IOUtils;

public class Alice {
    public static void main(String[] args) {
        MagicStrings.root_path = System.getProperty("user.dir");
        System.out.println("Working Directory = " + MagicStrings.root_path);
        AIMLProcessor.extension = new PCAIMLProcessorExtension();
        Alice.mainFunction(args);
    }

    public static void mainFunction(String[] args) {
        String botName = "super";
        String action = "chat";
        System.out.println(MagicStrings.programNameVersion);
        for (String s : args) {
            System.out.println(s);
            String[] splitArg = s.split("=");
            if (splitArg.length < 2) continue;
            String option = splitArg[0];
            String value = splitArg[1];
            if (option.equals("bot")) {
                botName = value;
            }
            if (option.equals("action")) {
                action = value;
            }
            MagicBooleans.trace_mode = option.equals("trace") && value.equals("true");
        }
        System.out.println("trace mode = " + MagicBooleans.trace_mode);
        Graphmaster.enableShortCuts = true;
        Bot bot = new Bot(botName, MagicStrings.root_path, action);
        if (bot.brain.getCategories().size() < 100) {
            bot.brain.printgraph();
        }
        switch (action) {
            case "chat" -> Alice.testChat(bot, MagicBooleans.trace_mode);
            case "test" -> Alice.testSuite(bot, MagicStrings.root_path + "/data/find.txt");
            case "ab" -> Alice.testAB(bot);
            case "aiml2csv", "csv2aiml" -> Alice.convert(bot, action);
            case "abwq" -> AB.abwq(bot);
        }
    }

    public static void convert(Bot bot, String action) {
        if (action.equals("aiml2csv")) {
            bot.writeAIMLIFFiles();
        } else if (action.equals("csv2aiml")) {
            bot.writeAIMLFiles();
        }
    }

    public static void testAB(Bot bot) {
        MagicBooleans.trace_mode = true;
        AB.ab(bot);
        AB.terminalInteraction(bot);
    }

    public static void testChat(Bot bot, boolean traceMode) {
        Chat chatSession = new Chat(bot);
        bot.brain.nodeStats();
        MagicBooleans.trace_mode = traceMode;
        String textLine = "";
        while (true) {
            System.out.print("Human: ");
            textLine = IOUtils.readInputTextLine();
            if (textLine == null || textLine.length() < 1) {
                textLine = MagicStrings.null_input;
            }
            if (textLine.equals("q")) {
                System.exit(0);
                continue;
            }
            if (textLine.equals("wq")) {
                bot.writeQuit();
                System.exit(0);
                continue;
            }
            if (textLine.equals("ab")) {
                Alice.testAB(bot);
                continue;
            }
            String request = textLine;
            if (MagicBooleans.trace_mode) {
                System.out.println("STATE=" + request + ":THAT=" + chatSession.thatHistory.get(0).get(0) + ":TOPIC=" + chatSession.predicates.get("topic"));
            }
            String response = chatSession.multisentenceRespond(request);
            while (response.contains("&lt;")) {
                response = response.replace("&lt;", "<");
            }
            while (response.contains("&gt;")) {
                response = response.replace("&gt;", ">");
            }
            System.out.println("Robot: " + response);
        }
    }

    public static void testBotChat() {
        Bot bot = new Bot("alice");
        System.out.println(bot.brain.upgradeCnt + " brain upgrades");
        bot.brain.nodeStats();
        Chat chatSession = new Chat(bot);
        String request = "Hello.  How are you?  What is your name?  Tell me about yourself.";
        String response = chatSession.multisentenceRespond(request);
        System.out.println("Human: " + request);
        System.out.println("Robot: " + response);
    }

    public static void testSuite(Bot bot, String filename) {
        try {
            String strLine;
            AB.passed.readAIMLSet(bot);
            AB.testSet.readAIMLSet(bot);
            System.out.println("Passed " + AB.passed.size() + " samples.");
            String textLine = "";
            Chat chatSession = new Chat(bot);
            FileInputStream fstream = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            int count = 0;
            HashSet<String> samples = new HashSet<String>();
            while ((strLine = br.readLine()) != null) {
                samples.add(strLine);
            }
            ArrayList<String> sampleArray = new ArrayList<>(samples);
            Collections.sort(sampleArray);
            for (String request : sampleArray) {
                if (request.startsWith("Human: ")) {
                    request = request.substring("Human: ".length(), request.length());
                }
                Category c = new Category(0, bot.preProcessor.normalize(request), "*", "*", MagicStrings.blank_template, MagicStrings.null_aiml_file);
                if (AB.passed.contains(request)) {
                    System.out.println("--> Already passed " + request);
                    continue;
                }
                if (bot.deletedGraph.existsCategory(c) || AB.passed.contains(request)) continue;
                String response = chatSession.multisentenceRespond(request);
                System.out.println(count + ". Human: " + request);
                System.out.println(count + ". Robot: " + response);
                textLine = IOUtils.readInputTextLine();
                AB.terminalInteractionStep(bot, request, textLine, c);
                ++count;
            }
            br.close();
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

