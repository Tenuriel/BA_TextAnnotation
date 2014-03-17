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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.FixedBitSetCachingWrapperFilter;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToChildBlockJoinQuery;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tim Pontzen
 */
public class GraphHandler {

    public AnchorHandler anchor;
    public StandardAnalyzer analyzer;
    public IndexSearcher searcher;
    public IndexReader iR;
    public Query parentQuery;
    public GraphHandler() {


        try {
            analyzer = new StandardAnalyzer(Version.LUCENE_46);
            iR = DirectoryReader.open(FSDirectory.open(new File("Graph")));

            searcher = new IndexSearcher(iR);
            
//            BooleanQuery q=new BooleanQuery();
            parentQuery = new TermQuery(new Term("Type", "Parent"));
            BooleanQuery mainq=new BooleanQuery();
            Filter parentFilter=new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(parentQuery));
            
            
//            QueryParser parser = new QueryParser(Version.LUCENE_46, "entity", analyzer);
//            TopDocs docs=searcher.search(parser.parse("anchor:\""+"white house\""), parentFilter, 20);
//            for(int i=0;i<20;i++){
////                Document doc=searcher.doc(21);
//                System.out.println(searcher.doc(docs.scoreDocs[i].doc).get("entity"));
//            }
//            Document doc=searcher.doc(9352973);
//            String[] tmp=doc.getValues("anchor");
//            
////            ArrayList<Document> docs = getDocs("united_states");
//            for (String s : tmp) {
//                System.out.println(s);
//            }
//            Scanner scan=new Scanner(Paths.get("entitys2.txt"));
//            scan.nextLine();
//            TreeSet<String> set=new TreeSet<>();
//            
//            while(scan.hasNextLine()){
//                    set.add(scan.nextLine());
//            }
//            
//            PrintWriter pw = new PrintWriter("entitys.txt", "UTF-8");
//            int i = 0;
//            for (String s : set) {
//                if (i < 31140) {
//                    i++;
//                }else {
//                    pw.println(s);
//                }
//            }
//            pw.close();
//                pw.print(lineCounter);
//                pw.println();
        } catch (IOException ex) {
            System.out.println("this should hopfluy never happen");
        } 
//        catch (ParseException e) {
//        }
    }

    /**
     * formates the string to a valid querystring.
     *
     * @param s the string to be formated.
     * @return the formated string.
     */
    public static String formatStringForQuery(String s) {
        String[] tmp = s.split("[>]");
        String res = "";
        for (String x : tmp) {
            res = res + x;
        }
        tmp = res.split("/");
        if (tmp.length >= 2 && tmp[tmp.length - 2].contains("(") && !tmp[tmp.length - 2].contains(")")) {
            tmp[tmp.length - 1] = tmp[tmp.length - 2] + "/" + tmp[tmp.length - 1];
        }
        return QueryParserUtil.escape(tmp[tmp.length - 1]);
    }

    /**
     * formates an Uri to give back only the last value.
     *
     * @param s the string to be formated.
     * @return the formated string.
     */
    public static String formatString(String s) {
        String[] tmp = s.split("[>]");
        String res = "";
        for (String x : tmp) {
            res = res + x;
        }
        tmp = res.split("/");
        if (tmp.length >= 2 && tmp[tmp.length - 2].contains("(") && !tmp[tmp.length - 2].contains(")")) {
            tmp[tmp.length - 1] = tmp[tmp.length - 2] + "/" + tmp[tmp.length - 1];
        }
        return (tmp[tmp.length - 1]);
    }

    /**
     * returns all neighbors to the specified depth.
     *
     * @param grade the depth to search ( e.g 2 means neighbors and neighbors of
     * neighbors)
     * @return all found neighbors
     */
    public ArrayList<Document> getNeighbors(int grade) {
        QueryParser parser = new QueryParser(Version.LUCENE_46, "entity", analyzer);
        try {
            long bTime = System.nanoTime();
            Query query = parser.parse("jordan");
            TopDocs res = searcher.search(query, 1);
            res = searcher.search(query, res.totalHits);
            long time = System.nanoTime() - bTime;
            int a = 0;
        } catch (IOException | ParseException ex) {
            System.out.println("Error in neighbor retriving: " + ex.getMessage());
        }
        return null;
    }

    private float score(String entity, String queryString) {
        QueryParser parser = new QueryParser(Version.LUCENE_46, "entity", analyzer);
        float score = 0;
        try {
            Query query = parser.parse(entity);
            TopDocs docs = searcher.search(query, 1);
            float hits = docs.totalHits;
            query = parser.parse("entity:" + entity + " AND " + queryString);
//            query=JoinUtil.
            System.out.println(query.toString());
            docs = searcher.search(query, 1);
            score = 1 / hits * docs.totalHits;
        } catch (IOException | ParseException ex) {
            System.out.println("Error while searching neighbors");
        }
        return score;
    }

    private String bestMatch(String entity, String queryString) {
        String res = "";
        QueryParser parser = new QueryParser(Version.LUCENE_46, "anchorN", analyzer);
        try {
            //create the filter for the parent.
            BooleanQuery mainq=new BooleanQuery();
            BooleanQuery parent=new BooleanQuery();
            parent.add(parentQuery,BooleanClause.Occur.MUST);
            parent.add(parser.parse("anchor:\"" + entity + "\""),BooleanClause.Occur.MUST);
            Filter parentFilter=new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(parent));
            
//            BooleanQuery childq=new BooleanQuery();
//            for(String s:queryString){
//                childq.add(parser.parse(s),BooleanClause.Occur.SHOULD);
//            }
            Query childq=parser.parse(queryString);
//            Query childq=parser.parse("anchor:u*");
            String s=childq.toString();
            ToParentBlockJoinQuery pq=new ToParentBlockJoinQuery(childq, parentFilter, ScoreMode.Avg);
//            ToChildBlockJoinQuery pq=new ToChildBlockJoinQuery(childq, parentFilter, false);
            mainq.add(pq,BooleanClause.Occur.MUST);
//            mainq.add(parser.parse("anchor:\"" + entity + "\""),BooleanClause.Occur.MUST);
            
            TopDocs docs = searcher.search(mainq, 10);
//            docs = searcher.search(pq, docs.totalHits);
            if(docs.totalHits>0){
            res=searcher.doc(docs.scoreDocs[0].doc).get("entity");
            }
//            Query nQuery;
           
        } catch (IOException | ParseException ex) {
            System.out.println("Error while searching neighbors " + ex.getMessage());
        }
        return res;

    }

    /**
     * finds the most promising uri from a list of uri by comparing its
     * neighbors
     *
     * @param anchorURIs A list of lists containing possible uris from the
     * anchorindex
     * @return a list of the most promising uri for each entity
     */
    public ArrayList<String> findMostPromisingURI(ArrayList<String> entitys) {
//        new QueryWrapperFilter()
        ArrayList<String> result = new ArrayList<>();
        String queryString = "( ";
        
        for (String enti : entitys) {
            queryString += "anchorN:" + enti + " OR ";
        }
        queryString = queryString.substring(0, queryString.length() - 3) + ")";
        for (String entity : entitys) {
            result.add(bestMatch(entity, queryString));
        }
        return result;
    }
}
