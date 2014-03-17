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
            scan = new Scanner(Paths.get("cleaned_properties.nt"));
            scan2 = new Scanner(Paths.get("cleaned_properties_neigborToEntity.nt"));
            scan3 = new Scanner(Paths.get("anchors.nt"));
//            clear first line. contains no relevant data
//            System.out.println(scan.nextLine());

            analyzer = new StandardAnalyzer(Version.LUCENE_46);
            dir = FSDirectory.open(new File("Graph2"));
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(dir, conf);
            parser = new QueryParser(Version.LUCENE_46, "entity", analyzer);
//            ArrayList<Scanner> scanners = new ArrayList<>();
//            scanners.add(new Scanner(Paths.get("entity_neighbor_anchorsN")));
//            scanners.add(new Scanner(Paths.get("neighbor_entity_anchorsE")));
            createBlockIndex();
//            setIDs();
//            merge2(scanners);
//            createENAN();
//            String s;
//            Scanner tmp=new Scanner(Paths.get("combined"));
//            while (scan3.hasNext()) {
//                System.out.println(tmp.nextLine());
////                 line=scan.nextLine().split("\\|");
////                 if(line[0].toLowerCase().contains("white_house")){
////                     System.out.println(line[0]+line[1]);
////                 }
////                 
//            }
//            mergeFiles();
//             set=new TreeSet<>();
//             scan.close();
//             while(scan2.hasNext()){
//                 set.add(scan2.nextLine());
//             }
//             scan2.close();
//                pw.print(lineCounter);
//            createGraph();
//            cleanGraph();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (NoSuchElementException ex) {
            System.out.println("found end of file");
            running = false;
        } finally {
            try {
                if (writer != null) {
//                    writer.addDocuments(docs, analyzer);
                    writer.prepareCommit();
                    writer.commit();
                    writer.close();
                }
//                scan.close();
//                scan2.close();
                scan3.close();
//                PrintWriter pw= new PrintWriter("cleaned_properties_neigborToEntity.nt", "UTF-8");
//                int x=0;
//                for(String s:set){
//                    pw.println(s);
//                    if(x>=500_000){
//                        x=0;
//                        pw.flush();
//                    }
//                    
//                }
//                pw.close();
//                PrintWriter pw = new PrintWriter("linecount.txt", "UTF-8");
//                pw.print(lineCounter);
//                pw.println();
//                pw.print(skippedLines);
//                pw.close();
            } catch (IOException ex) {
                System.out.println("This will happen if end of File is reached");

            }

        }
        System.exit(0);
    }

    /**
     * creates 2 files with entits;neighbor;anchor of neighbor. One with entity
     * to neighbor and one with neighbor to entity.
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
            if (anchorMap.containsKey(split[0])&&anchorMap.containsKey(split[1])) {
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
            if (anchorMap.containsKey(split[0])&&anchorMap.containsKey(split[1])) {
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
    public void createBlockIndex() throws IOException {
        Scanner scan = new Scanner(Paths.get("combined"));
        Scanner scan2 = new Scanner(Paths.get("entity_anchors"));
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
//            doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
            doc.add(new Field("anchorN", s, TextField.TYPE_STORED));
            group.add(doc);
        }
        while (scan.hasNext()) {
            line = scan.nextLine().split("\\|");
            if (line.length == 0) {
                System.out.println("found end");
                doc = new Document();
                doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
                doc.add(new Field("anchor", anchorMap.get(line[0]), TextField.TYPE_STORED));
                //this is the marker for the parent field.
                // it needs to be a stringfield so only an exact match will hit
                // and does not need to be in the index since you dont search this field.
                doc.add(new Field("Type", "Parent", StringField.TYPE_NOT_STORED));
                group.add(doc);
                writer.addDocuments(group);
                group.clear();
                break;
            }
            if (!line[0].equals(previous)) {
                doc = new Document();
                doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
                doc.add(new Field("anchor", anchorMap.get(line[0]), TextField.TYPE_STORED));
                //this is the marker for the parent field.
                // it needs to be a stringfield so only an exact match will hit
                // and does not need to be in the index since you dont search this field.
                doc.add(new Field("Type", "Parent", StringField.TYPE_NOT_STORED));
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
//        ToParentBlockJoinQuery q=new ToParentBlockJoinQuery(q, null, ScoreMode.None)
//        Filter f=new CachingWrapperFilter(f)
//        BooleanClause.Occur.
        writer.prepareCommit();
        writer.commit();
        writer.close();
        System.exit(0);
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
//            if (GraphHandler.formatString(currUri[0]).equals("White_House")) {
//                System.out.println("");
//            }
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
//        docs.add(doc);
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("entity", line[2], TextField.TYPE_STORED));
        doc.add(new Field("neighbor", line[0], TextField.TYPE_STORED));
        writer.addDocument(doc);
//        docs.add(doc);
    }
}
