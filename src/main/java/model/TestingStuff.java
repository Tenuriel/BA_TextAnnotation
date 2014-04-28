/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * class for testing various Lucene features. NO JUNIT CLASS.
 * @author Tim Pontzen
 */
public class TestingStuff {
    public TestingStuff(){
        printIndex();
    }
    public void printIndex(){
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            Directory dir = FSDirectory.open(new File("Graph"));
            IndexReader iR=DirectoryReader.open(dir);
            IndexSearcher searcher=new IndexSearcher(iR);
            QueryParser parser=new QueryParser(Version.LUCENE_46,"entity", analyzer);
            TopDocs td=searcher.search(parser.parse("<http://dbpedia.org/resource/Autism>"), 3);
            
            Scanner scan = new Scanner(Paths.get("mappingbased_properties_cleaned_en.nt"));
            System.out.println(scan.nextLine());
            System.out.println(scan.nextLine());
            for(int i=0;i<td.scoreDocs.length;i++){
                Document doc= searcher.doc(td.scoreDocs[i].doc);
                for(IndexableField f:doc.getFields()){
                    System.out.println(doc.get(f.name()));
                }
            }
            
            
        } catch (IOException ex) {
            
        }
        catch(ParseException ex){
            System.out.println("hello");
        }
    }
    public void testIndexStuff(){
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
                Directory dir = FSDirectory.open(new File("Graph"));
                IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
                IndexWriter writer = new IndexWriter(dir, conf);
                QueryParser parser=new QueryParser(Version.LUCENE_46,"entity", analyzer);
                
                
                Document doc=new Document();
                doc.add(new Field("entity", "this is a test", TextField.TYPE_STORED));
                writer.addDocument(doc);
                          
                writer.prepareCommit();
                writer.commit();

                IndexReader iR=DirectoryReader.open(dir);
                
                
                IndexSearcher searcher=new IndexSearcher(iR);
                Query query=parser.parse("this is a test");
                TopDocs result=searcher.search(query,10000);
                 for(int i=0;i<result.scoreDocs.length;i++){
                System.out.println((searcher.doc(result.scoreDocs[i].doc).get("entity")));
                }
                 
                 
                doc.add(new Field("entity", "hello", TextField.TYPE_STORED));
                writer.deleteDocuments(query);
                writer.addDocument(doc);
                writer.prepareCommit();
                writer.commit();
                
                
               
                query=parser.parse("hello");
                iR.close();
                iR=DirectoryReader.open(dir);
                searcher=new IndexSearcher(iR);
                result=searcher.search(query,10000);
            for(int i=0;i<result.scoreDocs.length;i++){
                doc=searcher.doc(result.scoreDocs[i].doc);
                String[] values=doc.getValues("entity");
                for(String a:values){
                    System.out.println(a);
                }
            }
          
        } catch (IOException ex) {
            Logger.getLogger(TestingStuff.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(ParseException e){
            
        }
    }
}
