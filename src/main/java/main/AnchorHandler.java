/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
/**
 * Es gibt folgende Felder in dem Index:

    anchor 

    dbpedia_uri  ermittelte URI des Anchor

    anchor_uri 

    number Anzahl, wie oft der Anchor im englischen Wikipedia vorkommt.

 * @author Tim Pontzen
 */
public class AnchorHandler {

    public IndexReader iR;
    public StandardAnalyzer analyzer;
    public QueryParser parser;
    public IndexSearcher searcher;
    public AnchorHandler() {
        try {
            analyzer=new StandardAnalyzer(Version.LUCENE_46);
            iR = DirectoryReader.open(FSDirectory.open(new File("AnchorIndex")));
            
            searcher=new IndexSearcher(iR);
//            parser=new QueryParser(Version.LUCENE_46,"anchor" ,analyzer);
            parser=new QueryParser(Version.LUCENE_46,"dbpedia_uri" ,analyzer);
            Query query=parser.parse("<http://dbpedia.org/resource/Aristotle>");
//            TopDocs result=searcher.search(query,10000);
//            for(int i=0;i<result.scoreDocs.length;i++){
//                System.out.println((searcher.doc(result.scoreDocs[i].doc).get("anchor_uri"))
//                        +" " +(searcher.doc(result.scoreDocs[i].doc).get("anchor")));
//            }
            //clear first line. contains no relevant data
//            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
//            Directory dir = FSDirectory.open(new File("Graph"));
//            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
//            conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
//            IndexWriter writer = new IndexWriter(dir, conf);
//            TopDocs res=searcher.search(query, 1);
//            Document doc;
//            Document newDoc;
//            for(int i=0;i<res.totalHits;i++){
//                doc=searcher.doc(i);
//                newDoc=new Document();
//                newDoc.add(new Field("entity",doc.get("dbpedia_uri"), TextField.TYPE_STORED));
//                newDoc.add(new Field("anchor", doc.get("anchor"), TextField.TYPE_STORED));
//                writer.addDocument(newDoc);
//            }
//            writer.prepareCommit();
//            writer.commit();
//            writer.close();
            
            
        } catch (IOException ex) {
            System.out.println("this should hopfluy never happen" + ex.getMessage());
        }
        catch(ParseException e){
            
        }
    }
    public String getUri(String search){
        String result="";
        try {
            Query query=parser.parse(search);
            TopDocs docs=searcher.search(query, 1);
             for(int i=0;i<docs.scoreDocs.length;i++){
                result=(searcher.doc(docs.scoreDocs[i].doc)).get("dbpedia_uri");
                 
            }
        } catch (IOException|ParseException ex) {
            
        }
        
        return result;
    }
}
