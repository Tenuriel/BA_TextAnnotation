/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

/**
 *
 * @author Tim Pontzen
 */
public class GraphHandler {

    public static final float TF_IDF_ALPHA = 0.5f;
    public static String DELIMETER = "lucenedeli";
    public AnchorHandler anchor;
    public StandardAnalyzer analyzer;
    public IndexSearcher entitySearcher;
    public IndexSearcher abstractSearcher;
    public IndexReader entityReader;
    public IndexReader abstractReader;
    public Query parentQuery;

    public GraphHandler() {

        try {
            analyzer = new StandardAnalyzer(Version.LUCENE_46);
            entityReader = DirectoryReader.open(FSDirectory.open(new File("Entity_Index")));
            abstractReader = DirectoryReader.open(FSDirectory.open(new File("Abstract_Index")));

            entitySearcher = new IndexSearcher(entityReader);
            abstractSearcher = new IndexSearcher(abstractReader);
            abstractSearcher.setSimilarity(new CustomSimilarity());
            parentQuery = new TermQuery(new Term("type", "Parent"));
//            BooleanQuery q=new BooleanQuery();
//            BooleanQuery mainq=new BooleanQuery();
//            Filter parentFilter=new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(parentQuery));

            QueryParser parser = new QueryParser(Version.LUCENE_46, "abstract", analyzer);
            Query query = parser.parse("anchor:\"" + delimeterString("America") + "\"");

            BooleanQuery mainq = new BooleanQuery();
            Filter parentFilter = new FixedBitSetCachingWrapperFilter(new QueryWrapperFilter(parentQuery));

            ToParentBlockJoinQuery pq = new ToParentBlockJoinQuery(query, parentFilter, ScoreMode.Avg);

            mainq.add(pq, BooleanClause.Occur.MUST);

//            float bTime = System.nanoTime();
//
            TopDocs res = entitySearcher.search(query, 10);
//            ArrayList<String> idf=tf_idfCandidates("nsdap");
////
            Document doc;
//            for(String s: idf){
//                System.out.println(s);
//            }
            for (int i = 0; i < 140; i++) {
                if (i == res.totalHits) {
                    break;
                }
                doc = entitySearcher.doc(res.scoreDocs[i].doc);
                System.out.println(doc.get("entity"));
                System.out.println(entitySearcher.explain(query, res.scoreDocs[i].doc));
            }
        } catch (IOException ex) {
//            System.out.println("this should hopfluy never happen");
        } catch (ParseException e) {
        }
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
     * extracts the entity form a uri.
     *
     * @param uri
     * @return
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
     * @param entity
     * @return
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

    private String bestMatch(ArrayList<String> posEntitys, String queryString) {
        if (queryString.isEmpty()) {
            return queryString;
        }

        String res = "";
        QueryParser parser = new QueryParser(Version.LUCENE_46, "anchorN", analyzer);
        try {
            StringBuilder efBuilder = new StringBuilder();
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

            mainq.add(pq, BooleanClause.Occur.MUST);
            Query query = parser.parse(efBuilder.toString());
            query.setBoost(TF_IDF_ALPHA);
            mainq.add(query, BooleanClause.Occur.MUST);
//            mainq.add(parser.parse("anchor:\"" + entity + "\""),BooleanClause.Occur.MUST);

            TopDocs docs = entitySearcher.search(mainq, 1);
//            docs = searcher.search(pq, docs.totalHits);
            if (docs.totalHits > 0) {
                res = entitySearcher.doc(docs.scoreDocs[0].doc).get("entity");
            }

        } catch (IOException | ParseException ex) {
            System.out.println("Error while searching neighbors " + ex.getMessage());
        }
        return res;

    }

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
            childq.setBoost(1 - TF_IDF_ALPHA);
            ToParentBlockJoinQuery pq = new ToParentBlockJoinQuery(childq, parentFilter, ScoreMode.Avg);

            mainq.add(pq, BooleanClause.Occur.SHOULD);
            Query query = parser.parse("anchor:\"" + delimeterString(entity) + "\"");
            query.setBoost(TF_IDF_ALPHA);
            mainq.add(query, BooleanClause.Occur.MUST);
//            mainq.add(parser.parse("anchor:\"" + entity + "\""),BooleanClause.Occur.MUST);

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
     * finds the most promising uri from a list of uri by comparing its neighbors
     *
     * @param entitys A list of lists containing possible uris from the anchorindex
     * @return a list of the most promising uri for each entity
     */
    public HashMap<String, String> findMostPromisingURI(ArrayList<String> entitys) {
        float btime;
        float anchorTime = 0;
        float tf_idf_Time = 0;
        int tf_idf_counter = 0;
        float entryTime;

        String queryString;

        ArrayList<String> tf_idf_List;
        String value;

        btime = System.nanoTime();
        HashMap<String, String> result = checkEntrie(entitys);
        entryTime = System.nanoTime() - btime;
        for (Map.Entry<String, String> entry : result.entrySet()) {
            if (entry.getValue() == null) {
                queryString = "( ";
                for (String enti : entitys) {
                    if (!entry.getKey().equals(enti)) {
                        queryString += "anchorN:" + enti + " OR ";
                    }
                }
                queryString = queryString.substring(0, queryString.length() - 3) + ")";

                btime = System.nanoTime();
                value = bestMatch(entry.getKey(), queryString);
                anchorTime += System.nanoTime() - btime;
                if (value.isEmpty()) {
                    tf_idf_counter++;
                    btime = System.nanoTime();
                    tf_idf_List = (tf_idfCandidates(entry.getKey()));
                    value = bestMatch(tf_idf_List, queryString);
                    tf_idf_Time += System.nanoTime() - btime;
                }
                result.put(entry.getKey(), value);
            }
        }

//        for (String entity : entitys) {
//            result.add(bestMatch(entity, queryString));
//        }
        System.out.println("Number of entitys:" + entitys.size() + "\n avg entryCheck:" + entryTime / (entitys.size() * (Math.pow(10, 6)))
                + " entryCheck:" + entryTime / (Math.pow(10, 6))
                + "\n avg anchorTime:" + anchorTime / (entitys.size() * (Math.pow(10, 6)))
                + " anchorTime:" + anchorTime / (Math.pow(10, 6))
                + "\n avg tf_idf_time:" + tf_idf_Time / (tf_idf_counter * (Math.pow(10, 6)))
                + " tf_idf_time:" + tf_idf_Time / (Math.pow(10, 6)));
        return result;
    }

    /**
     * Searches for an excat match for every enitity in the list.
     *
     * @param entitys
     * @return a map with enititys as keys and uri/null as value depending if there is a match
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
