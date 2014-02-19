/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tim Pontzen
 */
public class GraphHandler {
    public AnchorHandler anchor;
    
    public StandardAnalyzer analyzer;
//    public QueryParser parser;
    public IndexSearcher searcher;
    public IndexReader iR;
    public GraphHandler(){
//        this.anchor=anchor;
        
    
        try {
            analyzer=new StandardAnalyzer(Version.LUCENE_46);
            iR = DirectoryReader.open(FSDirectory.open(new File("Graph")));
            
            searcher=new IndexSearcher(iR);
//            parser=new QueryParser(Version.LUCENE_46,"entity" ,analyzer);
//            Query query=parser.parse("<http://dbpedia.org/resource/Aristotle>");
//            TopDocs result=searcher.search(query,10000);
//            for(int i=0;i<result.scoreDocs.length;i++){
//                System.out.println((searcher.doc(result.scoreDocs[i].doc).get("anchor_uri"))
//                        +" " +(searcher.doc(result.scoreDocs[i].doc).get("anchor")));
//            }
          
        } catch (IOException ex) {
            System.out.println("this should hopfluy never happen");
        }
//        catch(ParseException e){
//            
//        }
    }
    public ArrayList<Document> getDocs(String search){
        ArrayList<Document> result= new ArrayList<>();
        BooleanQuery bQuery=new BooleanQuery();
        QueryParser parser=new QueryParser(Version.LUCENE_46,"entity" ,analyzer);
        try {
            Query query=parser.parse(search);
            bQuery.add(query, BooleanClause.Occur.MUST);
            parser=new QueryParser(Version.LUCENE_46,"neighbor" ,analyzer);
//            query=parser.parse("");
//            bQuery.add(query, BooleanClause.Occur.SHOULD);
            System.out.println(query.toString());
            
            TopDocs docs=searcher.search(query, 10);
//            System.out.println(docs.totalHits);
             for(int i=0;i<docs.scoreDocs.length;i++){
                result.add(searcher.doc(docs.scoreDocs[i].doc));
                 
            }
        } catch (IOException|ParseException ex) {
            System.out.println("Retriving Docs failed: "+ex.getMessage());
        }
        
        return result;
    }
    public String getAnchorUri(String search){
        String result="";
//        BooleanQuery bQuery=new BooleanQuery();
        QueryParser parser=new QueryParser(Version.LUCENE_46,"anchor" ,analyzer);
        try {
            Query query=parser.parse(search);
//            bQuery.add(query, BooleanClause.Occur.SHOULD);
            TopDocs docs=searcher.search(query, 1);
            System.out.println(docs.totalHits);
             for(int i=0;i<docs.scoreDocs.length;i++){
                result=(searcher.doc(docs.scoreDocs[i].doc)).get("entity");
                 
            }
        } catch (IOException|ParseException ex) {
            
        }
        
        return result;
    }
}
