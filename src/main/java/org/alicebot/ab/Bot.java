
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import org.alicebot.ab.AIMLMap;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.AIMLSet;
import org.alicebot.ab.Category;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicNumbers;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.Nodemapper;
import org.alicebot.ab.NodemapperOperator;
import org.alicebot.ab.PreProcessor;
import org.alicebot.ab.Properties;
import org.alicebot.ab.Timer;
import org.alicebot.ab.Utilities;

public class Bot {
    public final Properties properties = new Properties();
    public final PreProcessor preProcessor;
    public final Graphmaster brain;
    public final Graphmaster inputGraph;
    public final Graphmaster learnfGraph;
    public final Graphmaster patternGraph;
    public final Graphmaster deletedGraph;
    public Graphmaster unfinishedGraph;
    public ArrayList<Category> suggestedCategories;
    public String name = MagicStrings.unknown_bot_name;
    public HashMap<String, AIMLSet> setMap = new HashMap();
    public HashMap<String, AIMLMap> mapMap = new HashMap();
    static int leafPatternCnt = 0;
    static int starPatternCnt = 0;

    public void setAllPaths(String root, String name) {
        MagicStrings.bot_path = root + "/bots";
        MagicStrings.bot_name_path = MagicStrings.bot_path + "/" + name;
        System.out.println("Name = " + name + " Path = " + MagicStrings.bot_name_path);
        MagicStrings.aiml_path = MagicStrings.bot_name_path + "/aiml";
        MagicStrings.aimlif_path = MagicStrings.bot_name_path + "/aimlif";
        MagicStrings.config_path = MagicStrings.bot_name_path + "/config";
        MagicStrings.log_path = MagicStrings.bot_name_path + "/logs";
        MagicStrings.sets_path = MagicStrings.bot_name_path + "/sets";
        MagicStrings.maps_path = MagicStrings.bot_name_path + "/maps";
        System.out.println(MagicStrings.root_path);
        System.out.println(MagicStrings.bot_path);
        System.out.println(MagicStrings.bot_name_path);
        System.out.println(MagicStrings.aiml_path);
        System.out.println(MagicStrings.aimlif_path);
        System.out.println(MagicStrings.config_path);
        System.out.println(MagicStrings.log_path);
        System.out.println(MagicStrings.sets_path);
        System.out.println(MagicStrings.maps_path);
    }

    public Bot() {
        this(MagicStrings.default_bot);
    }

    public Bot(String name) {
        this(name, MagicStrings.root_path);
    }

    public Bot(String name, String path) {
        this(name, path, "auto");
    }

    public Bot(String name, String path, String action) {
        this.name = name;
        this.setAllPaths(path, name);
        this.brain = new Graphmaster(this);
        this.inputGraph = new Graphmaster(this);
        this.learnfGraph = new Graphmaster(this);
        this.deletedGraph = new Graphmaster(this);
        this.patternGraph = new Graphmaster(this);
        this.unfinishedGraph = new Graphmaster(this);
        this.suggestedCategories = new ArrayList();
        this.preProcessor = new PreProcessor(this);
        this.addProperties();
        this.addAIMLSets();
        this.addAIMLMaps();
        AIMLSet number = new AIMLSet(MagicStrings.natural_number_set_name);
        this.setMap.put(MagicStrings.natural_number_set_name, number);
        AIMLMap successor = new AIMLMap(MagicStrings.map_successor);
        this.mapMap.put(MagicStrings.map_successor, successor);
        AIMLMap predecessor = new AIMLMap(MagicStrings.map_predecessor);
        this.mapMap.put(MagicStrings.map_predecessor, predecessor);
        Date aimlDate = new Date(new File(MagicStrings.aiml_path).lastModified());
        Date aimlIFDate = new Date(new File(MagicStrings.aimlif_path).lastModified());
        System.out.println("AIML modified " + aimlDate + " AIMLIF modified " + aimlIFDate);
        this.readDeletedIFCategories();
        this.readUnfinishedIFCategories();
        MagicStrings.pannous_api_key = Utilities.getPannousAPIKey();
        MagicStrings.pannous_login = Utilities.getPannousLogin();
        if (action.equals("aiml2csv")) {
            this.addCategoriesFromAIML();
        } else if (action.equals("csv2aiml")) {
            this.addCategoriesFromAIMLIF();
        } else if (aimlDate.after(aimlIFDate)) {
            System.out.println("AIML modified after AIMLIF");
            this.addCategoriesFromAIML();
            this.writeAIMLIFFiles();
        } else {
            this.addCategoriesFromAIMLIF();
            if (this.brain.getCategories().size() == 0) {
                System.out.println("No AIMLIF Files found.  Looking for AIML");
                this.addCategoriesFromAIML();
            }
        }
        System.out.println("--> Bot " + name + " " + this.brain.getCategories().size() + " completed " + this.deletedGraph.getCategories().size() + " deleted " + this.unfinishedGraph.getCategories().size() + " unfinished");
    }

    void addMoreCategories(String file, ArrayList<Category> moreCategories) {
        if (file.contains(MagicStrings.deleted_aiml_file)) {
            for (Category c : moreCategories) {
                this.deletedGraph.addCategory(c);
            }
        } else if (file.contains(MagicStrings.unfinished_aiml_file)) {
            for (Category c : moreCategories) {
                if (this.brain.findNode(c) == null) {
                    this.unfinishedGraph.addCategory(c);
                    continue;
                }
                System.out.println("unfinished " + c.inputThatTopic() + " found in brain");
            }
        } else if (file.contains(MagicStrings.learnf_aiml_file)) {
            System.out.println("Reading Learnf file");
            for (Category c : moreCategories) {
                this.brain.addCategory(c);
                this.learnfGraph.addCategory(c);
                this.patternGraph.addCategory(c);
            }
        } else {
            for (Category c : moreCategories) {
                this.brain.addCategory(c);
                this.patternGraph.addCategory(c);
            }
        }
    }

    void addCategoriesFromAIML() {
        Timer timer = new Timer();
        timer.start();
        try {
            File folder = new File(MagicStrings.aiml_path);
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles();
                System.out.println("Loading AIML files from " + MagicStrings.aiml_path);
                for (File listOfFile : listOfFiles) {
                    String file;
                    if (!listOfFile.isFile() || !(file = listOfFile.getName()).endsWith(".aiml") && !file.endsWith(".AIML")) continue;
                    System.out.println(file);
                    try {
                        ArrayList<Category> moreCategories = AIMLProcessor.AIMLToCategories(MagicStrings.aiml_path, file);
                        this.addMoreCategories(file, moreCategories);
                    }
                    catch (Exception iex) {
                        System.out.println("Problem loading " + file);
                        iex.printStackTrace();
                    }
                }
            } else {
                System.out.println("addCategories: " + MagicStrings.aiml_path + " does not exist.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Loaded " + this.brain.getCategories().size() + " categories in " + timer.elapsedTimeSecs() + " sec");
    }

    void addCategoriesFromAIMLIF() {
        Timer timer = new Timer();
        timer.start();
        try {
            File folder = new File(MagicStrings.aimlif_path);
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles();
                System.out.println("Loading AIML files from " + MagicStrings.aimlif_path);
                for (File listOfFile : listOfFiles) {
                    String file;
                    if (!listOfFile.isFile() || !(file = listOfFile.getName()).endsWith(MagicStrings.aimlif_file_suffix) && !file.endsWith(MagicStrings.aimlif_file_suffix.toUpperCase())) continue;
                    try {
                        ArrayList<Category> moreCategories = this.readIFCategories(MagicStrings.aimlif_path + "/" + file);
                        this.addMoreCategories(file, moreCategories);
                    }
                    catch (Exception iex) {
                        System.out.println("Problem loading " + file);
                        iex.printStackTrace();
                    }
                }
            } else {
                System.out.println("addCategories: " + MagicStrings.aimlif_path + " does not exist.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Loaded " + this.brain.getCategories().size() + " categories in " + timer.elapsedTimeSecs() + " sec");
    }

    public void readDeletedIFCategories() {
        this.readCertainIFCategories(this.deletedGraph, MagicStrings.deleted_aiml_file);
    }

    public void readUnfinishedIFCategories() {
        this.readCertainIFCategories(this.unfinishedGraph, MagicStrings.unfinished_aiml_file);
    }

    public void updateUnfinishedCategories() {
        ArrayList<Category> unfinished = this.unfinishedGraph.getCategories();
        this.unfinishedGraph = new Graphmaster(this);
        for (Category c : unfinished) {
            if (this.brain.existsCategory(c)) continue;
            this.unfinishedGraph.addCategory(c);
        }
    }

    public void writeQuit() {
        this.writeAIMLIFFiles();
        System.out.println("Wrote AIMLIF Files");
        this.writeAIMLFiles();
        System.out.println("Wrote AIML Files");
        this.writeDeletedIFCategories();
        this.updateUnfinishedCategories();
        this.writeUnfinishedIFCategories();
    }

    public void readCertainIFCategories(Graphmaster graph, String fileName) {
        File file = new File(MagicStrings.aimlif_path + "/" + fileName + MagicStrings.aimlif_file_suffix);
        if (file.exists()) {
            try {
                ArrayList<Category> deletedCategories = this.readIFCategories(MagicStrings.aimlif_path + "/" + fileName + MagicStrings.aimlif_file_suffix);
                for (Category d : deletedCategories) {
                    graph.addCategory(d);
                }
                System.out.println("readCertainIFCategories " + graph.getCategories().size() + " categories from " + fileName + MagicStrings.aimlif_file_suffix);
            }
            catch (Exception iex) {
                System.out.println("Problem loading " + fileName);
                iex.printStackTrace();
            }
        } else {
            System.out.println("No " + MagicStrings.deleted_aiml_file + MagicStrings.aimlif_file_suffix + " file found");
        }
    }

    public void writeCertainIFCategories(Graphmaster graph, String file) {
        if (MagicBooleans.trace_mode) {
            System.out.println("writeCertainIFCaegories " + file + " size= " + graph.getCategories().size());
        }
        this.writeIFCategories(graph.getCategories(), file + MagicStrings.aimlif_file_suffix);
        File dir = new File(MagicStrings.aimlif_path);
        dir.setLastModified(new Date().getTime());
    }

    public void writeDeletedIFCategories() {
        this.writeCertainIFCategories(this.deletedGraph, MagicStrings.deleted_aiml_file);
    }

    public void writeLearnfIFCategories() {
        this.writeCertainIFCategories(this.learnfGraph, MagicStrings.learnf_aiml_file);
    }

    public void writeUnfinishedIFCategories() {
        this.writeCertainIFCategories(this.unfinishedGraph, MagicStrings.unfinished_aiml_file);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeIFCategories(ArrayList<Category> cats, String filename) {
        BufferedWriter bw = null;
        File existsPath = new File(MagicStrings.aimlif_path);
        if (existsPath.exists()) {
            try {
                bw = new BufferedWriter(new FileWriter(MagicStrings.aimlif_path + "/" + filename));
                for (Category category : cats) {
                    bw.write(Category.categoryToIF(category));
                    bw.newLine();
                }
            }
            catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            finally {
                try {
                    if (bw != null) {
                        bw.flush();
                        bw.close();
                    }
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void writeAIMLIFFiles() {
        System.out.println("writeAIMLIFFiles");
        HashMap<String, BufferedWriter> fileMap = new HashMap<String, BufferedWriter>();
        if (this.deletedGraph.getCategories().size() > 0) {
            this.writeDeletedIFCategories();
        }
        ArrayList<Category> brainCategories = this.brain.getCategories();
        Collections.sort(brainCategories, Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {
            try {
                BufferedWriter bw;
                String fileName = c.getFilename();
                if (fileMap.containsKey(fileName)) {
                    bw = (BufferedWriter)fileMap.get(fileName);
                } else {
                    bw = new BufferedWriter(new FileWriter(MagicStrings.aimlif_path + "/" + fileName + MagicStrings.aimlif_file_suffix));
                    fileMap.put(fileName, bw);
                }
                bw.write(Category.categoryToIF(c));
                bw.newLine();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Set set = fileMap.keySet();
        for (Object aSet : set) {
            BufferedWriter bw = (BufferedWriter)fileMap.get(aSet);
            try {
                if (bw == null) continue;
                bw.flush();
                bw.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        File dir = new File(MagicStrings.aimlif_path);
        dir.setLastModified(new Date().getTime());
    }

    public void writeAIMLFiles() {
        HashMap<String, BufferedWriter> fileMap = new HashMap<String, BufferedWriter>();
        Category b = new Category(0, "BUILD", "*", "*", new Date().toString(), "update.aiml");
        this.brain.addCategory(b);
        b = new Category(0, "DELEVLOPMENT ENVIRONMENT", "*", "*", MagicStrings.programNameVersion, "update.aiml");
        this.brain.addCategory(b);
        ArrayList<Category> brainCategories = this.brain.getCategories();
        Collections.sort(brainCategories, Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {
            if (c.getFilename().equals(MagicStrings.null_aiml_file)) continue;
            try {
                BufferedWriter bw;
                String fileName = c.getFilename();
                if (fileMap.containsKey(fileName)) {
                    bw = (BufferedWriter)fileMap.get(fileName);
                } else {
                    String copyright = Utilities.getCopyright(this, fileName);
                    bw = new BufferedWriter(new FileWriter(MagicStrings.aiml_path + "/" + fileName));
                    fileMap.put(fileName, bw);
                    bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<aiml>\n");
                    bw.write(copyright);
                }
                bw.write(Category.categoryToAIML(c) + "\n");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Set set = fileMap.keySet();
        for (Object aSet : set) {
            BufferedWriter bw = (BufferedWriter)fileMap.get(aSet);
            try {
                if (bw == null) continue;
                bw.write("</aiml>\n");
                bw.flush();
                bw.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        File dir = new File(MagicStrings.aiml_path);
        dir.setLastModified(new Date().getTime());
    }

    void addProperties() {
        try {
            this.properties.getProperties(MagicStrings.config_path + "/properties.txt");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void findPatterns() {
        this.findPatterns(this.inputGraph.root, "");
        System.out.println(leafPatternCnt + " Leaf Patterns " + starPatternCnt + " Star Patterns");
    }

    void findPatterns(Nodemapper node, String partialPatternThatTopic) {
        if (NodemapperOperator.isLeaf(node) && node.category.getActivationCnt() > MagicNumbers.node_activation_cnt) {
            ++leafPatternCnt;
            try {
                String categoryPatternThatTopic = "";
                categoryPatternThatTopic = node.shortCut ? partialPatternThatTopic + " <THAT> * <TOPIC> *" : partialPatternThatTopic;
                Category c = new Category(0, categoryPatternThatTopic, MagicStrings.blank_template, MagicStrings.unknown_aiml_file);
                if (!(this.brain.existsCategory(c) || this.deletedGraph.existsCategory(c) || this.unfinishedGraph.existsCategory(c))) {
                    this.patternGraph.addCategory(c);
                    this.suggestedCategories.add(c);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (NodemapperOperator.size(node) > MagicNumbers.node_size) {
            ++starPatternCnt;
            try {
                Category c = new Category(0, partialPatternThatTopic + " * <THAT> * <TOPIC> *", MagicStrings.blank_template, MagicStrings.unknown_aiml_file);
                if (!(this.brain.existsCategory(c) || this.deletedGraph.existsCategory(c) || this.unfinishedGraph.existsCategory(c))) {
                    this.patternGraph.addCategory(c);
                    this.suggestedCategories.add(c);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (String key : NodemapperOperator.keySet(node)) {
            Nodemapper value = NodemapperOperator.get(node, key);
            this.findPatterns(value, partialPatternThatTopic + " " + key);
        }
    }

    public void classifyInputs(String filename) {
        try {
            String strLine;
            FileInputStream fstream = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            int count = 0;
            while ((strLine = br.readLine()) != null) {
                if (strLine.startsWith("Human: ")) {
                    strLine = strLine.substring("Human: ".length(), strLine.length());
                }
                Nodemapper match = this.patternGraph.match(strLine, "unknown", "unknown");
                match.category.incrementActivationCnt();
                ++count;
            }
            br.close();
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void graphInputs(String filename) {
        try {
            String strLine;
            FileInputStream fstream = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            while ((strLine = br.readLine()) != null) {
                Category c = new Category(0, strLine, "*", "*", "nothing", MagicStrings.unknown_aiml_file);
                Nodemapper node = this.inputGraph.findNode(c);
                if (node == null) {
                    this.inputGraph.addCategory(c);
                    c.incrementActivationCnt();
                    continue;
                }
                node.category.incrementActivationCnt();
            }
            br.close();
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public ArrayList<Category> readIFCategories(String filename) {
        ArrayList<Category> categories = new ArrayList<Category>();
        try {
            String strLine;
            FileInputStream fstream = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            while ((strLine = br.readLine()) != null) {
                try {
                    Category c = Category.IFToCategory(strLine);
                    categories.add(c);
                }
                catch (Exception ex) {
                    System.out.println("Invalid AIMLIF in " + filename + " line " + strLine);
                }
            }
            br.close();
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return categories;
    }

    public void shadowChecker() {
        this.shadowChecker(this.brain.root);
    }

    void shadowChecker(Nodemapper node) {
        if (NodemapperOperator.isLeaf(node)) {
            String topic;
            String that;
            String input = node.category.getPattern().replace("*", "XXX").replace("_", "XXX");
            Nodemapper match = this.brain.match(input, that = node.category.getThat().replace("*", "XXX").replace("_", "XXX"), topic = node.category.getTopic().replace("*", "XXX").replace("_", "XXX"));
            if (match != node) {
                System.out.println("" + Graphmaster.inputThatTopic(input, that, topic));
                System.out.println("MATCHED:     " + match.category.inputThatTopic());
                System.out.println("SHOULD MATCH:" + node.category.inputThatTopic());
            }
        } else {
            for (String key : NodemapperOperator.keySet(node)) {
                this.shadowChecker(NodemapperOperator.get(node, key));
            }
        }
    }

    void addAIMLSets() {
        Timer timer = new Timer();
        timer.start();
        try {
            File folder = new File(MagicStrings.sets_path);
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles();
                System.out.println("Loading AIML Sets files from " + MagicStrings.sets_path);
                for (File listOfFile : listOfFiles) {
                    String file;
                    if (!listOfFile.isFile() || !(file = listOfFile.getName()).endsWith(".txt") && !file.endsWith(".TXT")) continue;
                    System.out.println(file);
                    String setName = file.substring(0, file.length() - ".txt".length());
                    System.out.println("Read AIML Set " + setName);
                    AIMLSet aimlSet = new AIMLSet(setName);
                    aimlSet.readAIMLSet(this);
                    this.setMap.put(setName, aimlSet);
                }
            } else {
                System.out.println("addAIMLSets: " + MagicStrings.sets_path + " does not exist.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void addAIMLMaps() {
        Timer timer = new Timer();
        timer.start();
        try {
            File folder = new File(MagicStrings.maps_path);
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles();
                System.out.println("Loading AIML Map files from " + MagicStrings.maps_path);
                for (File listOfFile : listOfFiles) {
                    String file;
                    if (!listOfFile.isFile() || !(file = listOfFile.getName()).endsWith(".txt") && !file.endsWith(".TXT")) continue;
                    System.out.println(file);
                    String mapName = file.substring(0, file.length() - ".txt".length());
                    System.out.println("Read AIML Map " + mapName);
                    AIMLMap aimlMap = new AIMLMap(mapName);
                    aimlMap.readAIMLMap(this);
                    this.mapMap.put(mapName, aimlMap);
                }
            } else {
                System.out.println("addCategories: " + MagicStrings.aiml_path + " does not exist.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

