/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tim Pontzen
 */
public class Index_Handler {

    public static String DELIMETER = "lucenedeli";

    /**
     * Returns a string enclosed in delimeters.
     *
     * @param s
     * @return
     */
    public static String delimeterString(String s) {
        return DELIMETER + " " + s + " " + DELIMETER;
    }

    /**
     * creates an index for the abstracts.
     * @return status of operation
     */
    public static String createAbstract_Index() {
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            Directory dir = FSDirectory.open(new File("Abstract_Index"));
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            conf.setSimilarity(new CustomSimilarity());
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(dir, conf);
            Scanner scan = new Scanner(Paths.get("abstract_clean.txt"));
            String[] line;
            Document doc;
            while (scan.hasNextLine()) {
                doc = new Document();
                line = scan.nextLine().split("\\|");
                doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
                doc.add(new Field("abstract", line[1], TextField.TYPE_STORED));
                writer.addDocument(doc);

            }
            writer.prepareCommit();
            writer.commit();
            writer.close();
            scan.close();
        } catch (IOException ex) {
            System.out.println("Failed to creat Index for Abstracts:" + ex.getMessage());
            return "Failed to creat Index for Abstracts:" + ex.getMessage();
        }
        return "done";
    }

    /**
     * Creates a block index . Is necessary for blockjoinquerys.
     * @return status of operation
     */
    public static String createBlockIndex() {
        try {
            Scanner scan = new Scanner(Paths.get("combined.txt.txt"));
            Scanner scan2 = new Scanner(Paths.get("entity_anchors.txt"));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            Directory dir = FSDirectory.open(new File("Entity_Index"));
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            conf.setSimilarity(new CustomSimilarity());
            IndexWriter writer = new IndexWriter(dir, conf);
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
                    doc.add(new Field("titel", delimeterString(getEntity(line[0])), TextField.TYPE_STORED));
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
                    doc.add(new Field("title", delimeterString(getEntity(line[0])), TextField.TYPE_STORED));
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
            System.out.println("Creating Blockindex failed :" + ex.getMessage());
            return "Creating Blockindex failed :" + ex.getMessage();
        }
        return "done";
    }

    /**
     * transforms a String from type a;b;c to delimeter a delimet;delimeter b delimeter;delimenter c
     * deilemter
     *
     * @param anchors
     * @return then anchors with the delimeters
     */
    public static String anchorDeli(String anchors) {
        String[] tmp = anchors.split(";");
        StringBuilder builder = new StringBuilder();
        for (String s : tmp) {
            builder.append(delimeterString(s));
            builder.append(";");
        }
        String res = builder.toString();
        return res.substring(0, res.length() - 1);
    }

    /**
     * extracts the entity form a uri.
     *
     * @param uri
     * @return the entity
     */
    public static String getEntity(String uri) {
        if (uri.isEmpty()) {
            return uri;
        }
        String[] tmp = uri.split("/");
        //in case there is a structre like titel(a/b)
        if (tmp.length >= 2 && tmp[tmp.length - 2].contains("(") && !tmp[tmp.length - 2].contains(")")) {
            tmp[tmp.length - 1] = tmp[tmp.length - 2] + "/" + tmp[tmp.length - 1];
        }
        tmp = tmp[tmp.length - 1].split("_");
        StringBuilder res = new StringBuilder();
        for (String s : tmp) {
            res.append(s);
            res.append(" ");
        }
        String s = res.toString();
        return s.substring(0, s.length() - 2);
    }
    /**
     * extracts anchors from an Lucene Index with a "dpedia_uri" field and an "anchor" field.
     * @return errormessage or "done"
     */
    public static String extracAnchors() {
        try {
            IndexReader iR = DirectoryReader.open(FSDirectory.open(new File("AnchorIndex")));
            IndexSearcher searcher = new IndexSearcher(iR);

            TreeSet<String> set = new TreeSet<>();
            Document doc;
            for (int i = 0; i < iR.maxDoc(); i++) {
                doc = searcher.doc(i);
                set.add("<" + doc.get("dbpedia_uri") + ">" + "|" + doc.get("anchor"));
            }
            PrintWriter pw = new PrintWriter("anchors.txt", "UTF-8");
            for (String s : set) {
                pw.println(s);
            }
            pw.close();
        } catch (IOException ex) {
            System.out.println("Error while extracting Anchors>" + ex.getMessage());
            return "Error while extracting Anchors>" + ex.getMessage();
        }
        return "done";
    }

}
