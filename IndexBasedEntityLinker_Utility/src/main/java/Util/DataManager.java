/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Tim Pontzen
 */
public class DataManager {
    
    /**
     * Extracts anchors and uris from an Lucene-Index containing a dpedia_uri-field and an
     * anchor-field.
     * @param indexPath the Path to the Index 
     */
    public void extracAnchors(String indexPath) {
        try {
            IndexReader iR = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            IndexSearcher searcher = new IndexSearcher(iR);
            TreeSet<String> set = new TreeSet<>();
            Document doc;
            for (int i = 0; i < iR.maxDoc(); i++) {
                doc = searcher.doc(i);
                set.add("<" + doc.get("dbpedia_uri") + ">" + "|" + doc.get("anchor"));
            }
            PrintWriter pw = new PrintWriter("anchors", "UTF-8");
            for (String s : set) {
                pw.println(s);
            }
            pw.close();
        } catch (IOException ex) {
            System.out.println("this should hopfluy never happen" + ex.getMessage());
        }
    }
}
