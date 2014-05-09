/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.FixedBitSetCachingWrapperFilter;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import view.TestingGui;

/**
 *
 * @author Tim Pontzen
 */
public class GraphHandler {

    /**
     * The multiplier for the tf_idf scoring. Formula :score= alpha*tf_idf_score +
     * (1-alpha)*neighbor_score
     */
    public static final float TF_IDF_ALPHA = 0.5f;
    /**
     * The delimeter for encapsulating words. Is needed for excat matching in lucene.
     */
    public static final String DELIMETER = "lucenedeli";
    /**
     * indicates if certain methods should create an output. used for data collection and debugging.
     */
    public boolean output = false;
    /**
     * Analyzer for query creation.
     */
    public StandardAnalyzer analyzer;
    /**
     * The IndexSearcher for the Entity_Index.
     */
    public IndexSearcher entitySearcher;
    /**
     * The IndexSearcher for the Abstract_Index.
     */
    public IndexSearcher abstractSearcher;
    /**
     * The IndexReader for the Entity_Index.
     */
    public IndexReader entityReader;
    /**
     * The IndexReader for the Abstract_Index.
     */
    public IndexReader abstractReader;
    /**
     * The parentquery for identifying parent-documents.
     */
    public Query parentQuery;
    /**
     * Indicates if the algorithmen should search for tfd_idf-candiates.
     */
    public boolean tf_idf_useage = true;

    public GraphHandler() {
        try {
            analyzer = new StandardAnalyzer(Version.LUCENE_46);
            entityReader = DirectoryReader.open(FSDirectory.open(new File("./Entity_Index")));
            abstractReader = DirectoryReader.open(FSDirectory.open(new File("./Abstract_Index")));

            entitySearcher = new IndexSearcher(entityReader);
            abstractSearcher = new IndexSearcher(abstractReader);
            abstractSearcher.setSimilarity(new CustomSimilarity());
            parentQuery = new TermQuery(new Term("type", "Parent"));
        } catch (IOException ex) {
            System.out.println("Error while opening Readers:" + ex.getMessage());
        }
    }

    /**
     * Formates the string to a valid querystring. Mostly used for uri formating.
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
     * Formates an Uri to give back only the last value. E.g : dbpedia.org/.../foo_bar will return
     * foo_bar
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
     * Returns a string enclosed in delimeters.
     *
     * @param s
     * @return
     */
    public static String delimeterString(String s) {
        return DELIMETER + " " + s + " " + DELIMETER;
    }

    /**
     * returns the top 20 entitys for whom their abstract text contains the highest tf_idf-Scores.
     *
     * @param entity the entity to be searched for in the abstracts
     * @return list with most promising matches
     */
    public ArrayList<String> tf_idfCandidates(String entity) {
        ArrayList<String> result = new ArrayList<>();
        QueryParser parser = new QueryParser(Version.LUCENE_46, "abstract", analyzer);
        try {
            Query query = parser.parse(entity);
            TopDocs docs = abstractSearcher.search(query, 20);
            for (int i = 0; i < 20; i++) {
                if (i == docs.totalHits) {
                    break;
                }
//                System.out.println(abstractSearcher.explain(query, docs.scoreDocs[i].doc));
                result.add(abstractSearcher.doc(docs.scoreDocs[i].doc).get("entity"));
            }

        } catch (IOException | ParseException ex) {
            System.out.println("Error while searching for tf_idfCandidates:" + ex.getMessage());
        }
        return result;
    }

    /**
     * Compares the neighbors of every entity in the list with all other entitys.
     *
     * @param posEntitys possible entitys
     * @param queryString a String to be parsed for the childquery
     * @return the best macht for the query
     */
    private String bestMatch(ArrayList<String> posEntitys, String queryString) {
        if (queryString.isEmpty()) {
            return queryString;
        }

        String res = "";
        QueryParser parser = new QueryParser(Version.LUCENE_46, "anchorN", analyzer);
        try {
            StringBuilder efBuilder = new StringBuilder();
            //create query-string for selected entitys
            for (String s : posEntitys) {
                efBuilder.append("title:\"");
                efBuilder.append(delimeterString(getEntity(s)));
                efBuilder.append("\" ");
            }
            //create the filter for the parent.
            BooleanQuery mainq = new BooleanQuery();
            Filter parentFilter = new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(parentQuery));

            Query childq = parser.parse(queryString);
            childq.setBoost(1 - TF_IDF_ALPHA);
            ToParentBlockJoinQuery pq = new ToParentBlockJoinQuery(childq, parentFilter, ScoreMode.Avg);

            mainq.add(pq, BooleanClause.Occur.SHOULD);
            Query query = parser.parse(efBuilder.toString());
            query.setBoost(TF_IDF_ALPHA);
            mainq.add(query, BooleanClause.Occur.MUST);

            TopDocs docs = entitySearcher.search(mainq, 1);
            if (docs.totalHits > 0) {
                res = entitySearcher.doc(docs.scoreDocs[0].doc).get("entity");
            }

        } catch (IOException | ParseException ex) {
            System.out.println("Error while searching neighbors " + ex.getMessage());
        }
        return res;

    }

    /**
     * Compares the neighbors of an entity with all other entitys.
     *
     * @param entity The selected entity
     * @param queryString a String to be parsed for the childquery
     * @return the best macht for the query
     */
    private String bestMatch(String entity, String queryString) {
        if (queryString.isEmpty()) {
            return queryString;
        }
        String res = "";
        QueryParser parser = new QueryParser(Version.LUCENE_46, "anchorN", analyzer);
        try {
            //create the filter for the parent.
            BooleanQuery mainq = new BooleanQuery();
            Filter parentFilter = new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(parentQuery));

            Query childq = parser.parse(queryString);
            ToParentBlockJoinQuery pq = new ToParentBlockJoinQuery(childq, parentFilter, ScoreMode.Avg);

            mainq.add(pq, BooleanClause.Occur.SHOULD);
            Query query = parser.parse("anchor:\"" + delimeterString(entity) + "\"");
            mainq.add(query, BooleanClause.Occur.MUST);

            TopDocs docs = entitySearcher.search(mainq, 1);
            if (docs.totalHits > 0) {
                res = entitySearcher.doc(docs.scoreDocs[0].doc).get("entity");
            }

        } catch (IOException | ParseException ex) {
            System.out.println("Error while searching neighbors " + ex.getMessage());
        }
        return res;

    }

    /**
     * Finds the most promising uris for a list of entitys.
     *
     * @param entitys A list of entitys determined by the NER
     * @return a list of the most promising uri for each entity
     */
    public HashMap<String, String> findMostPromisingURI(ArrayList<String> entitys) {
        float btime;
        float anchorTime = 0;
        float tf_idf_Time = 0;
        int tf_idf_counter = 0;
        float entryTime;
        int entryCounter=0;
        String queryString;

        ArrayList<String> tf_idf_List;
        String value;

        //check entrys
        btime = System.nanoTime();
        HashMap<String, String> result = checkEntrie(entitys);
        entryTime = System.nanoTime() - btime;

        for (Map.Entry<String, String> entry : result.entrySet()) {
            //create querystring if needed
            if (entry.getValue() == null) {
                entryCounter++;
                queryString = "( ";
                for (String enti : entitys) {
                    if (!entry.getKey().equals(enti)) {
                        queryString += "anchorN:" + enti + " OR ";
                    }
                }
                if (!queryString.isEmpty()) {
                    queryString = queryString.substring(0, queryString.length() - 3) + ")";
                }
                //search for anchors
                btime = System.nanoTime();
                value = bestMatch(entry.getKey(), queryString);
                anchorTime += System.nanoTime() - btime;
                if (value.isEmpty() && tf_idf_useage) {
                    tf_idf_counter++;
                    btime = System.nanoTime();
                    tf_idf_List = (tf_idfCandidates(entry.getKey()));
                    value = bestMatch(tf_idf_List, queryString);
                    tf_idf_Time += System.nanoTime() - btime;
                }
                result.put(entry.getKey(), value);
            }
        }
        if (output) {
            try{
                PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter("./eval.csv", true)));
                DecimalFormat dc=new DecimalFormat("#.##");
                pw.println(TestingGui.input.getText()+";"+entitys.size()+";"+ (result.size()-entryCounter)+";"+ dc.format(entryTime / (entitys.size() * (Math.pow(10, 6))))
                    + ";" + dc.format(entryTime / (Math.pow(10, 6))) +";"+entryCounter +";"
                    + dc.format(anchorTime / (entitys.size() * (Math.pow(10, 6)))) +";"
                    + dc.format(anchorTime / (Math.pow(10, 6)))+";"+tf_idf_counter
                    + ";" + dc.format(tf_idf_Time / (tf_idf_counter * (Math.pow(10, 6))))
                    + ";" + dc.format(tf_idf_Time / (Math.pow(10, 6))));
                pw.close();
            }catch(IOException ex){
                System.out.println(ex.getMessage());
            }
//            System.out.println("Number of entitys:" + entitys.size() + "\n avg entryCheck:" + entryTime / (entitys.size() * (Math.pow(10, 6)))
//                    + " entryCheck:" + entryTime / (Math.pow(10, 6))
//                    + "\n avg anchorTime:" + anchorTime / (entitys.size() * (Math.pow(10, 6)))
//                    + " anchorTime:" + anchorTime / (Math.pow(10, 6))
//                    + "\n avg tf_idf_time:" + tf_idf_Time / (tf_idf_counter * (Math.pow(10, 6)))
//                    + " tf_idf_time:" + tf_idf_Time / (Math.pow(10, 6)));
        }
        return result;
    }

    /**
     * Searches for an excat match for every enitity in the list. A match occurs only if
     * doc.title==entity
     *
     * @param entitys The entitys to search for.
     * @return a map with enititys as keys and uri/null as value depending on whether there is a
     * match or not.
     */
    public HashMap<String, String> checkEntrie(ArrayList<String> entitys) {
        HashMap<String, String> result = new HashMap<>();
        try {
            QueryParser parser = new QueryParser(Version.LUCENE_46, "title", analyzer);
            Query query;
            TopDocs docs;
            for (String entity : entitys) {
                query = parser.parse("\"" + delimeterString(entity) + "\"");
                docs = entitySearcher.search(query, 1);
                if (docs.totalHits == 1) {
                    result.put(entity, entitySearcher.doc(docs.scoreDocs[0].doc).get("entity"));
                } else {
                    result.put(entity, null);
                }
            }
        } catch (IOException | ParseException ex) {
            System.out.println("Error while checking entries for excat match:" + ex.getMessage());
        }
        return result;
    }
}
