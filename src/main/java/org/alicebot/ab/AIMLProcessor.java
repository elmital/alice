
package org.alicebot.ab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.alicebot.ab.AIMLMap;
import org.alicebot.ab.AIMLProcessorExtension;
import org.alicebot.ab.Category;
import org.alicebot.ab.Chat;
import org.alicebot.ab.History;
import org.alicebot.ab.Interval;
import org.alicebot.ab.JapaneseTokenizer;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicNumbers;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.Nodemapper;
import org.alicebot.ab.ParseState;
import org.alicebot.ab.Sraix;
import org.alicebot.ab.Utilities;
import org.alicebot.ab.utils.CalendarUtils;
import org.alicebot.ab.utils.DomUtils;
import org.alicebot.ab.utils.IOUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AIMLProcessor {
    public static AIMLProcessorExtension extension;
    public static int sraiCount;
    public static int repeatCount;
    public static int trace_count;

    private static void categoryProcessor(Node n, ArrayList<Category> categories, String topic, String aimlFile, String language) {
        NodeList children = n.getChildNodes();
        String pattern = "*";
        String that = "*";
        String template = "";
        for (int j = 0; j < children.getLength(); ++j) {
            Node m = children.item(j);
            String mName = m.getNodeName();
            if (mName.equals("#text")) continue;
            if (mName.equals("pattern")) {
                pattern = DomUtils.nodeToString(m);
                continue;
            }
            if (mName.equals("that")) {
                that = DomUtils.nodeToString(m);
                continue;
            }
            if (mName.equals("topic")) {
                topic = DomUtils.nodeToString(m);
                continue;
            }
            if (mName.equals("template")) {
                template = DomUtils.nodeToString(m);
                continue;
            }
            System.out.println("categoryProcessor: unexpected " + mName);
        }
        pattern = AIMLProcessor.trimTag(pattern, "pattern");
        that = AIMLProcessor.trimTag(that, "that");
        topic = AIMLProcessor.trimTag(topic, "topic");
        template = AIMLProcessor.trimTag(template, "template");
        if (language.equals("JP") || language.equals("jp")) {
            String morphPattern = JapaneseTokenizer.morphSentence(pattern);
            System.out.println("<pattern>" + pattern + "</pattern> --> <pattern>" + morphPattern + "</pattern>");
            pattern = morphPattern;
            String morphThatPattern = JapaneseTokenizer.morphSentence(that);
            System.out.println("<that>" + that + "</that> --> <that>" + morphThatPattern + "</that>");
            that = morphThatPattern;
            String morphTopicPattern = JapaneseTokenizer.morphSentence(topic);
            System.out.println("<topic>" + topic + "</topic> --> <topic>" + morphTopicPattern + "</topic>");
            topic = morphTopicPattern;
        }
        Category c = new Category(0, pattern, that, topic, template, aimlFile);
        categories.add(c);
    }

    public static String trimTag(String s, String tagName) {
        String stag = "<" + tagName + ">";
        String etag = "</" + tagName + ">";
        if (s.startsWith(stag) && s.endsWith(etag)) {
            s = s.substring(stag.length());
            s = s.substring(0, s.length() - etag.length());
        }
        return s.trim();
    }

    public static ArrayList<Category> AIMLToCategories(String directory, String aimlFile) {
        try {
            int i;
            ArrayList<Category> categories = new ArrayList<Category>();
            Node root = DomUtils.parseFile(directory + "/" + aimlFile);
            String language = MagicStrings.default_language;
            if (root.hasAttributes()) {
                NamedNodeMap XMLAttributes = root.getAttributes();
                for (i = 0; i < XMLAttributes.getLength(); ++i) {
                    if (!XMLAttributes.item(i).getNodeName().equals("language")) continue;
                    language = XMLAttributes.item(i).getNodeValue();
                }
            }
            NodeList nodelist = root.getChildNodes();
            for (i = 0; i < nodelist.getLength(); ++i) {
                Node n = nodelist.item(i);
                if (n.getNodeName().equals("category")) {
                    AIMLProcessor.categoryProcessor(n, categories, "*", aimlFile, language);
                    continue;
                }
                if (!n.getNodeName().equals("topic")) continue;
                String topic = n.getAttributes().getNamedItem("name").getTextContent();
                NodeList children = n.getChildNodes();
                for (int j = 0; j < children.getLength(); ++j) {
                    Node m = children.item(j);
                    if (!m.getNodeName().equals("category")) continue;
                    AIMLProcessor.categoryProcessor(m, categories, topic, aimlFile, language);
                }
            }
            return categories;
        }
        catch (Exception ex) {
            System.out.println("AIMLToCategories: " + ex);
            ex.printStackTrace();
            return null;
        }
    }

    public static int checkForRepeat(String input, Chat chatSession) {
        if (input.equals(chatSession.inputHistory.get(1))) {
            return 1;
        }
        return 0;
    }

    public static String respond(String input, String that, String topic, Chat chatSession) {
        return AIMLProcessor.respond(input, that, topic, chatSession, 0);
    }

    public static String respond(String input, String that, String topic, Chat chatSession, int srCnt) {
        if (input == null || input.length() == 0) {
            input = MagicStrings.null_input;
        }
        sraiCount = srCnt;
        String response = MagicStrings.default_bot_response;
        try {
            Nodemapper leaf = chatSession.bot.brain.match(input, that, topic);
            if (leaf == null) {
                return response;
            }
            ParseState ps = new ParseState(0, chatSession, input, that, topic, leaf);
            response = AIMLProcessor.evalTemplate(leaf.category.getTemplate(), ps);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    private static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; ++i) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
                continue;
            }
            if (!Character.isWhitespace(chars[i])) continue;
            found = false;
        }
        return String.valueOf(chars);
    }

    private static String explode(String input) {
        String result = "";
        for (int i = 0; i < input.length(); ++i) {
            result = result + " " + input.charAt(i);
        }
        return result.trim();
    }

    public static String evalTagContent(Node node, ParseState ps, Set<String> ignoreAttributes) {
        String result = "";
        try {
            NodeList childList = node.getChildNodes();
            for (int i = 0; i < childList.getLength(); ++i) {
                Node child = childList.item(i);
                if (ignoreAttributes != null && ignoreAttributes.contains(child.getNodeName())) continue;
                result = result + AIMLProcessor.recursEval(child, ps);
            }
        }
        catch (Exception ex) {
            System.out.println("Something went wrong with evalTagContent");
            ex.printStackTrace();
        }
        return result;
    }

    public static String genericXML(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        return AIMLProcessor.unevaluatedXML(result, node, ps);
    }

    private static String unevaluatedXML(String result, Node node, ParseState ps) {
        String nodeName = node.getNodeName();
        String attributes = "";
        if (node.hasAttributes()) {
            NamedNodeMap XMLAttributes = node.getAttributes();
            for (int i = 0; i < XMLAttributes.getLength(); ++i) {
                attributes = attributes + " " + XMLAttributes.item(i).getNodeName() + "=\"" + XMLAttributes.item(i).getNodeValue() + "\"";
            }
        }
        if (result.equals("")) {
            return "<" + nodeName + attributes + "/>";
        }
        return "<" + nodeName + attributes + ">" + result + "</" + nodeName + ">";
    }

    private static String srai(Node node, ParseState ps) {
        if (++sraiCount > MagicNumbers.max_recursion) {
            return MagicStrings.too_much_recursion;
        }
        String response = MagicStrings.default_bot_response;
        try {
            Nodemapper leaf;
            String result = AIMLProcessor.evalTagContent(node, ps, null);
            result = result.trim();
            result = result.replaceAll("(\r\n|\n\r|\r|\n)", " ");
            result = ps.chatSession.bot.preProcessor.normalize(result);
            String topic = ps.chatSession.predicates.get("topic");
            if (MagicBooleans.trace_mode) {
                System.out.println(trace_count + ". <srai>" + result + "</srai> from " + ps.leaf.category.inputThatTopic() + " topic=" + topic + ") ");
                ++trace_count;
            }
            if ((leaf = ps.chatSession.bot.brain.match(result, ps.that, topic)) == null) {
                return response;
            }
            response = AIMLProcessor.evalTemplate(leaf.category.getTemplate(), new ParseState(ps.depth + 1, ps.chatSession, ps.input, ps.that, topic, leaf));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return response.trim();
    }

    private static String getAttributeOrTagValue(Node node, ParseState ps, String attributeName) {
        String result = "";
        Node m = node.getAttributes().getNamedItem(attributeName);
        if (m == null) {
            NodeList childList = node.getChildNodes();
            result = null;
            for (int i = 0; i < childList.getLength(); ++i) {
                Node child = childList.item(i);
                if (!child.getNodeName().equals(attributeName)) continue;
                result = AIMLProcessor.evalTagContent(child, ps, null);
            }
        } else {
            result = m.getNodeValue();
        }
        return result;
    }

    private static String sraix(Node node, ParseState ps) {
        HashSet<String> attributeNames = Utilities.stringSet("botid", "host");
        String host = AIMLProcessor.getAttributeOrTagValue(node, ps, "host");
        String botid = AIMLProcessor.getAttributeOrTagValue(node, ps, "botid");
        String hint = AIMLProcessor.getAttributeOrTagValue(node, ps, "hint");
        String limit = AIMLProcessor.getAttributeOrTagValue(node, ps, "limit");
        String defaultResponse = AIMLProcessor.getAttributeOrTagValue(node, ps, "default");
        String result = AIMLProcessor.evalTagContent(node, ps, attributeNames);
        return Sraix.sraix(ps.chatSession, result, defaultResponse, hint, host, botid, null, limit);
    }

    private static String map(Node node, ParseState ps) {
        String result = MagicStrings.unknown_map_value;
        HashSet<String> attributeNames = Utilities.stringSet("name");
        String mapName = AIMLProcessor.getAttributeOrTagValue(node, ps, "name");
        String contents = AIMLProcessor.evalTagContent(node, ps, attributeNames);
        if (mapName == null) {
            result = "<map>" + contents + "</map>";
        } else {
            AIMLMap map = ps.chatSession.bot.mapMap.get(mapName);
            if (map != null) {
                result = map.get(contents.toUpperCase());
            }
            if (result == null) {
                result = MagicStrings.unknown_map_value;
            }
            result = result.trim();
        }
        return result;
    }

    private static String set(Node node, ParseState ps) {
        HashSet<String> attributeNames = Utilities.stringSet("name", "var");
        String predicateName = AIMLProcessor.getAttributeOrTagValue(node, ps, "name");
        String varName = AIMLProcessor.getAttributeOrTagValue(node, ps, "var");
        String value = AIMLProcessor.evalTagContent(node, ps, attributeNames).trim();
        value = value.replaceAll("(\r\n|\n\r|\r|\n)", " ");
        if (predicateName != null) {
            ps.chatSession.predicates.put(predicateName, value);
        }
        if (varName != null) {
            ps.vars.put(varName, value);
        }
        return value;
    }

    private static String get(Node node, ParseState ps) {
        String result = MagicStrings.unknown_predicate_value;
        String predicateName = AIMLProcessor.getAttributeOrTagValue(node, ps, "name");
        String varName = AIMLProcessor.getAttributeOrTagValue(node, ps, "var");
        if (predicateName != null) {
            result = ps.chatSession.predicates.get(predicateName).trim();
        } else if (varName != null) {
            result = ps.vars.get(varName).trim();
        }
        return result;
    }

    private static String bot(Node node, ParseState ps) {
        String result = MagicStrings.unknown_property_value;
        String propertyName = AIMLProcessor.getAttributeOrTagValue(node, ps, "name");
        if (propertyName != null) {
            result = ps.chatSession.bot.properties.get(propertyName).trim();
        }
        return result;
    }

    private static String date(Node node, ParseState ps) {
        String jformat = AIMLProcessor.getAttributeOrTagValue(node, ps, "jformat");
        String locale = AIMLProcessor.getAttributeOrTagValue(node, ps, "locale");
        String timezone = AIMLProcessor.getAttributeOrTagValue(node, ps, "timezone");
        String dateAsString = CalendarUtils.date(jformat, locale, timezone);
        return dateAsString;
    }

    private static String interval(Node node, ParseState ps) {
        HashSet<String> attributeNames = Utilities.stringSet("style", "jformat", "from", "to");
        String style = AIMLProcessor.getAttributeOrTagValue(node, ps, "style");
        String jformat = AIMLProcessor.getAttributeOrTagValue(node, ps, "jformat");
        String from = AIMLProcessor.getAttributeOrTagValue(node, ps, "from");
        String to = AIMLProcessor.getAttributeOrTagValue(node, ps, "to");
        if (style == null) {
            style = "years";
        }
        if (jformat == null) {
            jformat = "MMMMMMMMM dd, yyyy";
        }
        if (from == null) {
            from = "January 1, 1970";
        }
        if (to == null) {
            to = CalendarUtils.date(jformat, null, null);
        }
        String result = "unknown";
        if (style.equals("years")) {
            result = "" + Interval.getYearsBetween(from, to, jformat);
        }
        if (style.equals("months")) {
            result = "" + Interval.getMonthsBetween(from, to, jformat);
        }
        if (style.equals("days")) {
            result = "" + Interval.getDaysBetween(from, to, jformat);
        }
        if (style.equals("hours")) {
            result = "" + Interval.getHoursBetween(from, to, jformat);
        }
        return result;
    }

    private static int getIndexValue(Node node, ParseState ps) {
        int index = 0;
        String value = AIMLProcessor.getAttributeOrTagValue(node, ps, "index");
        if (value != null) {
            try {
                index = Integer.parseInt(value) - 1;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return index;
    }

    private static String inputStar(Node node, ParseState ps) {
        int index = AIMLProcessor.getIndexValue(node, ps);
        if (ps.leaf.starBindings.inputStars.star(index) == null) {
            return "";
        }
        return ps.leaf.starBindings.inputStars.star(index).trim();
    }

    private static String thatStar(Node node, ParseState ps) {
        int index = AIMLProcessor.getIndexValue(node, ps);
        if (ps.leaf.starBindings.thatStars.star(index) == null) {
            return "";
        }
        return ps.leaf.starBindings.thatStars.star(index).trim();
    }

    private static String topicStar(Node node, ParseState ps) {
        int index = AIMLProcessor.getIndexValue(node, ps);
        if (ps.leaf.starBindings.topicStars.star(index) == null) {
            return "";
        }
        return ps.leaf.starBindings.topicStars.star(index).trim();
    }

    private static String id(Node node, ParseState ps) {
        return ps.chatSession.customerId;
    }

    private static String size(Node node, ParseState ps) {
        int size = ps.chatSession.bot.brain.getCategories().size();
        return String.valueOf(size);
    }

    private static String vocabulary(Node node, ParseState ps) {
        int size = ps.chatSession.bot.brain.getVocabulary().size();
        return String.valueOf(size);
    }

    private static String program(Node node, ParseState ps) {
        return MagicStrings.programNameVersion;
    }

    private static String that(Node node, ParseState ps) {
        int index = 0;
        int jndex = 0;
        String value = AIMLProcessor.getAttributeOrTagValue(node, ps, "index");
        if (value != null) {
            try {
                String pair = value;
                String[] spair = pair.split(",");
                index = Integer.parseInt(spair[0]) - 1;
                jndex = Integer.parseInt(spair[1]) - 1;
                System.out.println("That index=" + index + "," + jndex);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String that = MagicStrings.unknown_history_item;
        History hist = ps.chatSession.thatHistory.get(index);
        if (hist != null) {
            that = (String)hist.get(jndex);
        }
        return that.trim();
    }

    private static String input(Node node, ParseState ps) {
        int index = AIMLProcessor.getIndexValue(node, ps);
        return ps.chatSession.inputHistory.getString(index);
    }

    private static String request(Node node, ParseState ps) {
        int index = AIMLProcessor.getIndexValue(node, ps);
        return ps.chatSession.requestHistory.getString(index).trim();
    }

    private static String response(Node node, ParseState ps) {
        int index = AIMLProcessor.getIndexValue(node, ps);
        return ps.chatSession.responseHistory.getString(index).trim();
    }

    private static String system(Node node, ParseState ps) {
        HashSet<String> attributeNames = Utilities.stringSet("timeout");
        String evaluatedContents = AIMLProcessor.evalTagContent(node, ps, attributeNames);
        String result = IOUtils.system(evaluatedContents, MagicStrings.system_failed);
        return result;
    }

    private static String think(Node node, ParseState ps) {
        AIMLProcessor.evalTagContent(node, ps, null);
        return "";
    }

    private static String explode(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        return AIMLProcessor.explode(result);
    }

    private static String normalize(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        return ps.chatSession.bot.preProcessor.normalize(result);
    }

    private static String denormalize(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        return ps.chatSession.bot.preProcessor.denormalize(result);
    }

    private static String uppercase(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        return result.toUpperCase();
    }

    private static String lowercase(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        return result.toLowerCase();
    }

    private static String formal(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        return AIMLProcessor.capitalizeString(result);
    }

    private static String sentence(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        if (result.length() > 1) {
            return result.substring(0, 1).toUpperCase() + result.substring(1, result.length());
        }
        return "";
    }

    private static String person(Node node, ParseState ps) {
        String result = node.hasChildNodes() ? AIMLProcessor.evalTagContent(node, ps, null) : ps.leaf.starBindings.inputStars.star(0);
        result = " " + result + " ";
        result = ps.chatSession.bot.preProcessor.person(result);
        return result.trim();
    }

    private static String person2(Node node, ParseState ps) {
        String result = node.hasChildNodes() ? AIMLProcessor.evalTagContent(node, ps, null) : ps.leaf.starBindings.inputStars.star(0);
        result = " " + result + " ";
        result = ps.chatSession.bot.preProcessor.person2(result);
        return result.trim();
    }

    private static String gender(Node node, ParseState ps) {
        String result = AIMLProcessor.evalTagContent(node, ps, null);
        result = " " + result + " ";
        result = ps.chatSession.bot.preProcessor.gender(result);
        return result.trim();
    }

    private static String random(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        ArrayList<Node> liList = new ArrayList<Node>();
        for (int i = 0; i < childList.getLength(); ++i) {
            if (!childList.item(i).getNodeName().equals("li")) continue;
            liList.add(childList.item(i));
        }
        return AIMLProcessor.evalTagContent((Node)liList.get((int)(Math.random() * (double)liList.size())), ps, null);
    }

    private static String unevaluatedAIML(Node node, ParseState ps) {
        String result = AIMLProcessor.learnEvalTagContent(node, ps);
        return AIMLProcessor.unevaluatedXML(result, node, ps);
    }

    private static String recursLearn(Node node, ParseState ps) {
        String nodeName = node.getNodeName();
        if (nodeName.equals("#text")) {
            return node.getNodeValue();
        }
        if (nodeName.equals("eval")) {
            return AIMLProcessor.evalTagContent(node, ps, null);
        }
        return AIMLProcessor.unevaluatedAIML(node, ps);
    }

    private static String learnEvalTagContent(Node node, ParseState ps) {
        String result = "";
        NodeList childList = node.getChildNodes();
        for (int i = 0; i < childList.getLength(); ++i) {
            Node child = childList.item(i);
            result = result + AIMLProcessor.recursLearn(child, ps);
        }
        return result;
    }

    private static String learn(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        String pattern = "";
        String that = "*";
        String template = "";
        for (int i = 0; i < childList.getLength(); ++i) {
            Category c;
            if (!childList.item(i).getNodeName().equals("category")) continue;
            NodeList grandChildList = childList.item(i).getChildNodes();
            for (int j = 0; j < grandChildList.getLength(); ++j) {
                if (grandChildList.item(j).getNodeName().equals("pattern")) {
                    pattern = AIMLProcessor.recursLearn(grandChildList.item(j), ps);
                    continue;
                }
                if (grandChildList.item(j).getNodeName().equals("that")) {
                    that = AIMLProcessor.recursLearn(grandChildList.item(j), ps);
                    continue;
                }
                if (!grandChildList.item(j).getNodeName().equals("template")) continue;
                template = AIMLProcessor.recursLearn(grandChildList.item(j), ps);
            }
            pattern = pattern.substring("<pattern>".length(), pattern.length() - "</pattern>".length());
            if (template.length() >= "<template></template>".length()) {
                template = template.substring("<template>".length(), template.length() - "</template>".length());
            }
            if (that.length() >= "<that></that>".length()) {
                that = that.substring("<that>".length(), that.length() - "</that>".length());
            }
            pattern = pattern.toUpperCase();
            that = that.toUpperCase();
            if (MagicBooleans.trace_mode) {
                System.out.println("Learn Pattern = " + pattern);
                System.out.println("Learn That = " + that);
                System.out.println("Learn Template = " + template);
            }
            if (node.getNodeName().equals("learn")) {
                c = new Category(0, pattern, that, "*", template, MagicStrings.null_aiml_file);
            } else {
                c = new Category(0, pattern, that, "*", template, MagicStrings.learnf_aiml_file);
                ps.chatSession.bot.learnfGraph.addCategory(c);
            }
            ps.chatSession.bot.brain.addCategory(c);
        }
        return "";
    }

    private static String loopCondition(Node node, ParseState ps) {
        boolean loop = true;
        String result = "";
        int loopCnt = 0;
        while (loop && loopCnt < MagicNumbers.max_loops) {
            String loopResult = AIMLProcessor.condition(node, ps);
            if (loopResult.trim().equals(MagicStrings.too_much_recursion)) {
                return MagicStrings.too_much_recursion;
            }
            if (loopResult.contains("<loop/>")) {
                loopResult = loopResult.replace("<loop/>", "");
                loop = true;
            } else {
                loop = false;
            }
            result = result + loopResult;
        }
        if (loopCnt >= MagicNumbers.max_loops) {
            result = MagicStrings.too_much_looping;
        }
        return result;
    }

    private static String condition(Node node, ParseState ps) {
        int i;
        String result = "";
        NodeList childList = node.getChildNodes();
        ArrayList<Node> liList = new ArrayList<Node>();
        String predicate = null;
        String varName = null;
        String value = null;
        HashSet<String> attributeNames = Utilities.stringSet("name", "var", "value");
        predicate = AIMLProcessor.getAttributeOrTagValue(node, ps, "name");
        varName = AIMLProcessor.getAttributeOrTagValue(node, ps, "var");
        for (i = 0; i < childList.getLength(); ++i) {
            if (!childList.item(i).getNodeName().equals("li")) continue;
            liList.add(childList.item(i));
        }
        if (liList.size() == 0 && (value = AIMLProcessor.getAttributeOrTagValue(node, ps, "value")) != null && predicate != null && ps.chatSession.predicates.get(predicate).equals(value)) {
            return AIMLProcessor.evalTagContent(node, ps, attributeNames);
        }
        if (liList.size() == 0 && (value = AIMLProcessor.getAttributeOrTagValue(node, ps, "value")) != null && varName != null && ps.vars.get(varName).equals(value)) {
            return AIMLProcessor.evalTagContent(node, ps, attributeNames);
        }
        for (i = 0; i < liList.size() && result.equals(""); ++i) {
            Node n = (Node)liList.get(i);
            String liPredicate = predicate;
            String liVarName = varName;
            if (liPredicate == null) {
                liPredicate = AIMLProcessor.getAttributeOrTagValue(n, ps, "name");
            }
            if (liVarName == null) {
                liVarName = AIMLProcessor.getAttributeOrTagValue(n, ps, "var");
            }
            if ((value = AIMLProcessor.getAttributeOrTagValue(n, ps, "value")) != null) {
                if (liPredicate != null && value != null && (ps.chatSession.predicates.get(liPredicate).equals(value) || ps.chatSession.predicates.containsKey(liPredicate) && value.equals("*"))) {
                    return AIMLProcessor.evalTagContent(n, ps, attributeNames);
                }
                if (liVarName == null || value == null || !ps.vars.get(liVarName).equals(value) && (!ps.vars.containsKey(liPredicate) || !value.equals("*"))) continue;
                return AIMLProcessor.evalTagContent(n, ps, attributeNames);
            }
            return AIMLProcessor.evalTagContent(n, ps, attributeNames);
        }
        return "";
    }

    public static boolean evalTagForLoop(Node node) {
        NodeList childList = node.getChildNodes();
        for (int i = 0; i < childList.getLength(); ++i) {
            if (!childList.item(i).getNodeName().equals("loop")) continue;
            return true;
        }
        return false;
    }

    private static String recursEval(Node node, ParseState ps) {
        try {
            String nodeName = node.getNodeName();
            if (nodeName.equals("#text")) {
                return node.getNodeValue();
            }
            if (nodeName.equals("#comment")) {
                return "";
            }
            if (nodeName.equals("template")) {
                return AIMLProcessor.evalTagContent(node, ps, null);
            }
            if (nodeName.equals("random")) {
                return AIMLProcessor.random(node, ps);
            }
            if (nodeName.equals("condition")) {
                return AIMLProcessor.loopCondition(node, ps);
            }
            if (nodeName.equals("srai")) {
                return AIMLProcessor.srai(node, ps);
            }
            if (nodeName.equals("sr")) {
                return AIMLProcessor.respond(ps.leaf.starBindings.inputStars.star(0), ps.that, ps.topic, ps.chatSession, sraiCount);
            }
            if (nodeName.equals("sraix")) {
                return AIMLProcessor.sraix(node, ps);
            }
            if (nodeName.equals("set")) {
                return AIMLProcessor.set(node, ps);
            }
            if (nodeName.equals("get")) {
                return AIMLProcessor.get(node, ps);
            }
            if (nodeName.equals("map")) {
                return AIMLProcessor.map(node, ps);
            }
            if (nodeName.equals("bot")) {
                return AIMLProcessor.bot(node, ps);
            }
            if (nodeName.equals("id")) {
                return AIMLProcessor.id(node, ps);
            }
            if (nodeName.equals("size")) {
                return AIMLProcessor.size(node, ps);
            }
            if (nodeName.equals("vocabulary")) {
                return AIMLProcessor.vocabulary(node, ps);
            }
            if (nodeName.equals("program")) {
                return AIMLProcessor.program(node, ps);
            }
            if (nodeName.equals("date")) {
                return AIMLProcessor.date(node, ps);
            }
            if (nodeName.equals("interval")) {
                return AIMLProcessor.interval(node, ps);
            }
            if (nodeName.equals("think")) {
                return AIMLProcessor.think(node, ps);
            }
            if (nodeName.equals("system")) {
                return AIMLProcessor.system(node, ps);
            }
            if (nodeName.equals("explode")) {
                return AIMLProcessor.explode(node, ps);
            }
            if (nodeName.equals("normalize")) {
                return AIMLProcessor.normalize(node, ps);
            }
            if (nodeName.equals("denormalize")) {
                return AIMLProcessor.denormalize(node, ps);
            }
            if (nodeName.equals("uppercase")) {
                return AIMLProcessor.uppercase(node, ps);
            }
            if (nodeName.equals("lowercase")) {
                return AIMLProcessor.lowercase(node, ps);
            }
            if (nodeName.equals("formal")) {
                return AIMLProcessor.formal(node, ps);
            }
            if (nodeName.equals("sentence")) {
                return AIMLProcessor.sentence(node, ps);
            }
            if (nodeName.equals("person")) {
                return AIMLProcessor.person(node, ps);
            }
            if (nodeName.equals("person2")) {
                return AIMLProcessor.person2(node, ps);
            }
            if (nodeName.equals("gender")) {
                return AIMLProcessor.gender(node, ps);
            }
            if (nodeName.equals("star")) {
                return AIMLProcessor.inputStar(node, ps);
            }
            if (nodeName.equals("thatstar")) {
                return AIMLProcessor.thatStar(node, ps);
            }
            if (nodeName.equals("topicstar")) {
                return AIMLProcessor.topicStar(node, ps);
            }
            if (nodeName.equals("that")) {
                return AIMLProcessor.that(node, ps);
            }
            if (nodeName.equals("input")) {
                return AIMLProcessor.input(node, ps);
            }
            if (nodeName.equals("request")) {
                return AIMLProcessor.request(node, ps);
            }
            if (nodeName.equals("response")) {
                return AIMLProcessor.response(node, ps);
            }
            if (nodeName.equals("learn") || nodeName.equals("learnf")) {
                return AIMLProcessor.learn(node, ps);
            }
            if (extension != null && extension.extensionTagSet().contains(nodeName)) {
                return extension.recursEval(node, ps);
            }
            return AIMLProcessor.genericXML(node, ps);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    private static String evalTemplate(String template, ParseState ps) {
        String response = MagicStrings.template_failed;
        try {
            template = "<template>" + template + "</template>";
            Node root = DomUtils.parseString(template);
            response = AIMLProcessor.recursEval(root, ps);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static boolean validTemplate(String template) {
        try {
            template = "<template>" + template + "</template>";
            DomUtils.parseString(template);
            return true;
        }
        catch (Exception e) {
            System.out.println("Invalid Template " + template);
            return false;
        }
    }

    static {
        sraiCount = 0;
        repeatCount = 0;
        trace_count = 0;
    }
}

