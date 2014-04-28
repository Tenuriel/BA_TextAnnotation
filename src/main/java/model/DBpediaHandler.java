/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tim Pontzen
 */
public class DBpediaHandler {

    static ArrayList<String> creatingDocs = new ArrayList<>();
    /**
     * counts the line sin the parsed document.
     */
    public long lineCounter = 0;
    public long skippedLines = 0;
    public Scanner scan;
    public Scanner scan2;
    public Scanner scan3;
    public IndexWriter writer;
    public Directory dir;
    public IndexReader iR;
    public IndexSearcher searcher;
    public QueryParser parser;
    public boolean running = true;
    public Analyzer analyzer;
    TreeSet<String> set;

    /**
     * Class to access anchorIndex.
     */
//    public AnchorHandler anchor;
    public DBpediaHandler() {
        try {
            
        } catch (NoSuchElementException ex) {
            System.out.println("found end of file");
            running = false;
        } finally {
            try {
                if (writer != null) {
                    writer.prepareCommit();
                    writer.commit();
                    writer.close();
                }
            } catch (IOException ex) {
                System.out.println("This will happen if end of File is reached");
            }

        }
        System.exit(0);
    }
    /**
     * creates an index for the abstracts. 
     */
    public void createAbstract_Index(){
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            Directory dir = FSDirectory.open(new File("Abstract_Index"));
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            conf.setSimilarity(new CustomSimilarity());
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(dir, conf);
            Scanner scan = new Scanner(Paths.get("abstract_clean"));
            String[] line;
            Document doc;
            while(scan.hasNextLine()){
                doc=new Document();
                line=scan.nextLine().split("\\|");
                doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
                doc.add(new Field("abstract", line[1], TextField.TYPE_STORED));
                writer.addDocument(doc);
                
            }
            writer.prepareCommit();
            writer.commit();
            writer.close();
            scan.close();
        } catch (IOException ex) {
            System.out.println("Failed to creat Index for Abstracts:"+ex.getMessage());
        }
    }
    /**
     * creates a csv file from the files created ind createTF_IDF().
     * id;entity;word;tf*idf
     */
    public void createTF_CSV() {
        try {
            PrintWriter pw = new PrintWriter("idf.csv", "UTF-8");
            Scanner scan = new Scanner(Paths.get("termCount_allDoc"));
            Scanner scan2 = new Scanner(Paths.get("doc_TermAndCount"));
            HashMap<String, Integer> tfALL = new HashMap<>();
            String[] line;
            String[] words;
            int id=0;
            String tmp;
            int linecount=Integer.valueOf(scan.nextLine());
            while (scan.hasNextLine()) {
                tmp=scan.nextLine();
                line =tmp.split(";");
//                System.out.println(id);
//                id++;
                if(line[1].contains("\\")){
                id++;
            }
                tfALL.put(line[0], Integer.valueOf(line[1]));
            }
            while(scan2.hasNextLine()){
                line=scan2.nextLine().split("\\|");
                words=line[1].split(";");
                for(int i=0;i<words.length;i=i+2){
                   double score=Integer.valueOf(words[i+1])*Math.log(linecount/tfALL.get(words[i]));
                    pw.println(id+";"+line[0]+";"+words[i]+";"+score);
                    id++;
                }
            }
            pw.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void createTF_IDF() {
        try (PrintWriter pw = new PrintWriter("termCount_allDoc", "UTF-8")) {
            PrintWriter pw2 = new PrintWriter("doc_TermAndCount", "UTF-8");
            Scanner scan = new Scanner(Paths.get("abstract_clean"));
            String[] words;
            String[] line;
            TreeMap<String, Integer> tf = new TreeMap<>();
            TreeMap<String, Integer> wordDoc = new TreeMap<>();
            int counter = 0;
            int i = 0;
            while (scan.hasNextLine()) {
                counter++;
                line = scan.nextLine().split("\\|");
                words = line[1].split("[\"\\.@,; ]");
                for (String s : words) {
                    if (s.isEmpty()) {
                        continue;
                    }
                    if (wordDoc.containsKey(s)) {
                        wordDoc.put(s, wordDoc.get(s) + 1);
                    } else {
                        wordDoc.put(s, 1);
                        if (tf.containsKey(s)) {
                            tf.put(s, tf.get(s) + 1);
                        } else {
                            tf.put(s, 1);
                        }
                    }
                }
                if(wordDoc.isEmpty()){
                    continue;
                }
                pw2.print(line[0] + "|");
                for (Map.Entry<String, Integer> e : wordDoc.entrySet()) {
                    pw2.print(e.getKey() + ";" + e.getValue() + ";");
                }
                pw2.println();
                i++;
                if (i > 500_000) {
                    pw2.flush();
                    i = 0;
                }
                wordDoc.clear();
            }
            pw.println(counter);
            for (Map.Entry<String, Integer> e : tf.entrySet()) {
                pw.println(e.getKey() + ";" + e.getValue());
                i++;
                if (i > 500_000) {
                    pw.flush();
                    i = 0;
                }
            }
            pw.close();
            pw2.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void cleanAbstracts() {
        try (PrintWriter pw = new PrintWriter("abstract_clean", "UTF-8")) {
            Scanner scan = new Scanner(Paths.get("entitys.txt"));
            Scanner scan2 = new Scanner(Paths.get("long_abstracts_en.nt"));
            scan2.nextLine();
            String[] tmp;
            HashMap<String, String> map = new HashMap<>();
            while (scan.hasNextLine()) {
                map.put(scan.nextLine(), "");
            }
            while (scan2.hasNextLine()) {
                tmp = scan2.nextLine().split(">");
                if (map.containsKey(tmp[0] + ">")) {
                    map.put(tmp[0] + ">", tmp[2]);
                }

            }
            TreeMap<String, String> set = new TreeMap<>(map);
            int i = 0;
            for (Map.Entry<String, String> e : set.entrySet()) {
                String value = e.getValue().trim();
                if (value.isEmpty()) {
                    continue;
                }
                pw.println(e.getKey() + "|" + value);
                i++;
                if (i > 500000) {
                    i = 0;
                    pw.flush();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * creates a csv with id;entity;anchor;depth
     */
    public void createCSV() {
        try {
            PrintWriter pw = new PrintWriter("data.csv", "UTF-8");
            Scanner scan = new Scanner(Paths.get("combined"));
            Scanner scan2 = new Scanner(Paths.get("entity_anchors"));
            String[] line;//=scan.nextLine().split(" ");
            Map<String, String> anchorMap = new HashMap<>();
            int idCounter = 0;
            int counter = 0;
            String[] anchors;
            //load anchors into ram
            while (scan2.hasNext()) {
                line = scan2.nextLine().split("\\|");
                anchorMap.put(line[0], line[1]);
            }
            line = scan.nextLine().split("\\|");
            String previous = line[0];
            anchors = line[2].split(";");
            for (String s : anchors) {
                pw.println(idCounter + ";" + line[0] + ";" + s + ";1");
                idCounter++;
                counter++;
            }
            while (scan.hasNext()) {
                if (counter > 500_000) {
                    pw.flush();
                    counter = 0;
                }
                line = scan.nextLine().split("\\|");
                if (line.length == 0) {
                    System.out.println("found eof");
                    anchors = anchorMap.get(line[0]).split(";");
                    for (String s : anchors) {
                        pw.println(idCounter + ";" + line[0] + ";" + s + ";0");
                        idCounter++;
                        counter++;
                    }
                    break;
                }
                if (!line[0].equals(previous)) {
                    anchors = anchorMap.get(line[0]).split(";");
                    for (String s : anchors) {
                        pw.println(idCounter + ";" + line[0] + ";" + s + ";0");
                        idCounter++;
                        counter++;
                    }
                    previous = line[0];
                }
                anchors = line[2].split(";");
                for (String s : anchors) {
                    pw.println(idCounter + ";" + line[0] + ";" + s + ";1");
                    counter++;
                    idCounter++;
                }
            }
            pw.close();
        } catch (IOException ex) {
        }
    }

    /**
     * creates 2 files with entits;neighbor;anchor of neighbor. One with entity to neighbor and one
     * with neighbor to entity.
     */
    public void createENAN() throws IOException {
        Scanner anchors = new Scanner(Paths.get("entity_anchors"));
        String[] split;
        Map<String, String> anchorMap = new HashMap<>();
        while (anchors.hasNext()) {
            split = anchors.nextLine().split("\\|");
            anchorMap.put(split[0], split[1]);
        }
        TreeSet<String> set = new TreeSet<>();
        int counter = 0;
        PrintWriter pw = new PrintWriter("entity_neighbor_anchorsN", "UTF-8");
        while (scan.hasNext()) {
            split = scan.nextLine().split("\\|");
            if (anchorMap.containsKey(split[0]) && anchorMap.containsKey(split[1])) {
                set.add(split[1] + "|" + split[0] + "|" + anchorMap.get(split[0]));
            }
        }
        for (String s : set) {
            if (counter > 500_000) {
                counter = 0;
                pw.flush();
            }
            pw.println(s);
            counter++;
        }
        pw.close();
        set.clear();
        pw = new PrintWriter("neighbor_entity_anchorsE", "UTF-8");
        while (scan2.hasNext()) {
            split = scan2.nextLine().split("\\|");
            if (anchorMap.containsKey(split[0]) && anchorMap.containsKey(split[1])) {
                set.add(split[1] + "|" + split[0] + "|" + anchorMap.get(split[0]));
            }
        }
        for (String s : set) {
            if (counter > 500_000) {
                counter = 0;
                pw.flush();
            }
            pw.println(s);
            counter++;
        }
        pw.close();
        set.clear();

        ArrayList<Scanner> scanners = new ArrayList<>();
        scanners.add(new Scanner(Paths.get("entity_neighbor_anchorsN")));
        scanners.add(new Scanner(Paths.get("neighbor_entity_anchorsE")));
        merge2(scanners);

    }

    /**
     * creates a file with ID;entity;anchors fields.
     *
     * @throws IOException
     */
    public void setIDs() throws IOException {
        Scanner entitys = new Scanner(Paths.get("entitys.txt"));
        scan3 = new Scanner(Paths.get("anchors.nt"));
        ArrayList<String> anchors;
        StringBuilder builder;
        String[] anchor;
        String tmp;
        Map<String, ArrayList<String>> anchorMap = new HashMap<>();
        while (scan3.hasNext()) {
            anchor = scan3.nextLine().split("\\|");
            if (anchorMap.containsKey(anchor[0])) {
                anchorMap.get(anchor[0]).add(anchor[1]);
            } else {
                anchors = new ArrayList<>();
                anchors.add(anchor[1]);
                anchorMap.put(anchor[0], anchors);
            }
        }
        PrintWriter pw = new PrintWriter("entity_anchors", "UTF-8");
        int counter = 0;
        int i = 0;
        while (entitys.hasNext()) {
            tmp = entitys.nextLine();
            if (anchorMap.containsKey(tmp)) {
                anchors = anchorMap.get(tmp);
                builder = new StringBuilder(tmp);
                builder.append("|");
                for (String s : anchors) {
                    builder.append(s);
                    builder.append(";");
                }
                pw.println(builder.substring(0, builder.length() - 1));
                if (counter++ > 500_000) {
                    pw.flush();
                    counter = 0;
                }
            }
        }
        pw.close();
    }

    /**
     * merges files with the same seperator for values.
     *
     * @param scanners The scanners with to be merged files as input
     * @throws IOException
     */
    public void merge2(ArrayList<Scanner> scanners) throws IOException {
        if (scanners.size() < 2) {
            System.out.println("no need for merging, to few files");
            return;
        }
        PrintWriter pw = new PrintWriter("combined", "UTF-8");
        int counter = 0;
        boolean finished = false;
        ArrayList<Scanner> toBeRemoved = new ArrayList<>();
        ArrayList<String> input = new ArrayList<>();
        for (Scanner scan : scanners) {
            input.add(scan.nextLine());
        }
        String tmp = input.get(0);
        int indi = 0;
        while (!finished) {
            for (Scanner scan : scanners) {
                if (!scan.hasNext()) {
                    String s = input.remove(scanners.indexOf(scan));
                    input.add(s);
                    if (s.equals(tmp)) {
                        indi = input.size() - 1;
                    }
                    scan.close();
                    toBeRemoved.add(scan);
                }
            }
            if (!toBeRemoved.isEmpty()) {
                scanners.removeAll(toBeRemoved);
                if (scanners.isEmpty()) {
                    break;
                }
                toBeRemoved.clear();
            }
            for (String s : input) {
                if (s.compareTo(tmp) < 0) {
                    tmp = s;
                    indi = input.indexOf(s);
                }
            }
            pw.println(tmp);
            counter++;
            if (counter > 500_000) {
                pw.flush();
                counter = 0;
            }
            if (indi < scanners.size()) {
                input.set(indi, scanners.get(indi).nextLine());
                tmp = input.get(indi);
            } else {
                input.remove(tmp);
                tmp = input.get(input.size() - 1);
                indi = input.size() - 1;
            }
        }
        pw.close();
    }

    /**
     * Creates a block index . Is necessary for blockjoinquerys
     *
     * @throws IOException
     */
    public void createBlockIndex() {
        try {
            Scanner scan = new Scanner(Paths.get("combined"));
            Scanner scan2 = new Scanner(Paths.get("entity_anchors"));
            analyzer = new StandardAnalyzer(Version.LUCENE_46);
            dir = FSDirectory.open(new File("Entity_Index"));
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            conf.setSimilarity(new CustomSimilarity());
            writer = new IndexWriter(dir, conf);
            String[] line;//=scan.nextLine().split(" ");
            ArrayList<Document> group = new ArrayList<>();
            Map<String, String> anchorMap = new HashMap<>();
            Document doc;
            
            String[] anchors;
            //load anchors into ram
            while (scan2.hasNext()) {
                line = scan2.nextLine().split("\\|");
                anchorMap.put(line[0], line[1]);
            }
            line = scan.nextLine().split("\\|");
            String previous = line[0];
            anchors = line[2].split(";");
            for (String s : anchors) {
                doc = new Document();
                doc.add(new Field("anchorN", s, TextField.TYPE_STORED));
                group.add(doc);
            }
            while (scan.hasNext()) {
                line = scan.nextLine().split("\\|");
                if (line.length == 0) {
                    System.out.println("found end");
                    doc = new Document();
                    doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
                    doc.add(new Field("anchor", anchorDeli(anchorMap.get(line[0])), TextField.TYPE_STORED));
                    doc.add(new Field("titel",GraphHandler.delimeterString(GraphHandler.getEntity(line[0])), TextField.TYPE_STORED));
                    //this is the marker for the parent field.
                    // it needs to be a stringfield so only an exact match will hit
                    // and does not need to be in the index since you dont search this field.
                    doc.add(new Field("type", "Parent", StringField.TYPE_NOT_STORED));
                    group.add(doc);
                    writer.addDocuments(group);
                    group.clear();
                    break;
                }
                if (!line[0].equals(previous)) {
                    doc = new Document();
                    doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
                    doc.add(new Field("anchor", anchorDeli(anchorMap.get(line[0])), TextField.TYPE_STORED));
                    doc.add(new Field("title",GraphHandler.delimeterString(GraphHandler.getEntity(line[0])), TextField.TYPE_STORED));
                    //this is the marker for the parent field.
                    // it needs to be a stringfield so only an exact match will hit
                    // and does not need to be in the index since you dont search this field.
                    doc.add(new Field("type", "Parent", StringField.TYPE_NOT_STORED));
                    group.add(doc);
                    writer.addDocuments(group);
                    group.clear();
                    previous = line[0];
                }
                anchors = line[2].split(";");
                for (String s : anchors) {
                    doc = new Document();
                    doc.add(new Field("anchorN", s, TextField.TYPE_STORED));
                    group.add(doc);
                }
            }
            writer.prepareCommit();
            writer.commit();
            writer.close();
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("Creating Blockindex failed :"+ex.getMessage());
        }
    }
    /**
     * transforms a String from type a;b;c
     * to delimeter a delimet;delimeter b delimeter;delimenter c deilemter
     * @param anchors
     * @return then anchors with the delimeters
     */
    public String anchorDeli(String anchors){
        String[] tmp=anchors.split(";");
        StringBuilder builder=new StringBuilder();
        for(String s:tmp){
            builder.append(GraphHandler.delimeterString(s));
            builder.append(";");
        }
        String res=builder.toString();
        return res.substring(0,res.length()-1);
    }
    /**
     * cleans the index from entries with more than 1 occurance.
     */
    public void cleanGraph() throws IOException {
        String prevUri = "beginn";
        String[] currUri;
        Document doc = new Document();
        int counter = 0;
        currUri = scan.nextLine().split("\\|");
        doc.add(new Field("entity", currUri[0], TextField.TYPE_STORED));
        if (currUri[1].startsWith(":")) {
            doc.add(new Field("anchor", currUri[1].substring(1, currUri[1].length()).trim(), TextField.TYPE_STORED));
        } else {
            doc.add(new Field("neighbor", GraphHandler.formatString(currUri[1]), TextField.TYPE_STORED));
        }

        while (scan.hasNext()) {
            counter++;
            currUri = scan.nextLine().split("\\|");
            if (currUri[0].equals(prevUri)) {
                if (currUri[1].startsWith(":")) {
                    doc.add(new Field("anchor", currUri[1].substring(1, currUri[1].length()).trim(), TextField.TYPE_STORED));
                } else {
                    doc.add(new Field("neighbor", GraphHandler.formatString(currUri[1]), TextField.TYPE_STORED));
                }
            } else {
                writer.addDocument(doc);
                doc = new Document();
                doc.add(new Field("entity", currUri[0], TextField.TYPE_STORED));
                if (currUri[1].startsWith(":")) {
                    doc.add(new Field("anchor", currUri[1].substring(1, currUri[1].length()).trim(), TextField.TYPE_STORED));
                } else {
                    doc.add(new Field("neighbor", GraphHandler.formatString(currUri[1]), TextField.TYPE_STORED));
                }
                prevUri = currUri[0];
            }
        }
        writer.addDocument(doc);

    }

    public void parseField(String[] line) throws IOException {
        Document doc = new Document();
        doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
        doc.add(new Field("neighbor", line[2], TextField.TYPE_STORED));
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("entity", line[2], TextField.TYPE_STORED));
        doc.add(new Field("neighbor", line[0], TextField.TYPE_STORED));
        writer.addDocument(doc);
    }
}
