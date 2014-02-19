/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tim Pontzen
 */
public class DBpediaHandler {
    static ArrayList<String> creatingDocs=new ArrayList<>();
    /**
     * counts the line sin the parsed document.
     */
    public long lineCounter = 0;
    public Scanner scan;
    public IndexWriter writer;
    public Directory dir;
    public IndexReader iR;
    public IndexSearcher searcher;
    public QueryParser parser;
    public boolean running=true;
    /**
     * Class to access anchorIndex.
     */
    public AnchorHandler anchor;

    public DBpediaHandler(AnchorHandler anchor) {
        ArrayList<Thread> t=new ArrayList<>();
        
        this.anchor = anchor;
        try {

            scan = new Scanner(Paths.get("mappingbased_properties_cleaned_en.nt"));
            //clear first line. contains no relevant data
            scan.nextLine();
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            dir = FSDirectory.open(new File("Graph2"));
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
            writer = new IndexWriter(dir, conf);
            parser=new QueryParser(Version.LUCENE_46, "entity", analyzer);
            String[] line=scan.nextLine().split(" ");
            
            
            DatabaseScanner db;
            while (running) {
                while (!(!line[2].toLowerCase().contains("xml")
                        && line[2].contains("<"))) {
                    lineCounter++;
                    line = scan.nextLine().split(" ");
                }
                parseField(line);
                lineCounter++;
                line = scan.nextLine().split(" ");
                parseField(line);
//                db=new DatabaseScanner();
//                db.line=line;
//                new Thread(db).start();
//                if(lineCounter%10==0){
//                writer.prepareCommit();
//                writer.commit();
//                }
            }
            writer.prepareCommit();
            writer.commit();
            writer.close();
            scan.close();
            PrintWriter pw = new PrintWriter("linecount.txt", "UTF-8");
            pw.print(lineCounter);
            pw.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }catch(NoSuchElementException ex){
                System.out.println("found end of file");
                running=false;
        }finally{
            try {
                
                writer.prepareCommit();
                writer.commit();
                writer.close();
                 scan.close();
            PrintWriter pw = new PrintWriter("linecount.txt", "UTF-8");
            pw.print(lineCounter);
            pw.close();
            } catch (IOException ex) {
                System.out.println("This will happen if end of File is reached");
                
            }
           
        }
        System.exit(0);
    }
    public void parseField(String [] line) throws IOException {
            Document doc =new Document();
            doc.add(new Field("entity", line[0], TextField.TYPE_STORED));
            doc.add(new Field("neighbor", line[2], TextField.TYPE_STORED));
            writer.addDocument(doc);
            doc =new Document();
            doc.add(new Field("entity", line[2], TextField.TYPE_STORED));
            doc.add(new Field("neighbor", line[0], TextField.TYPE_STORED));
            writer.addDocument(doc);
            

    }
    /**
     * Threadclass for parsing and sorting
     */
    class DatabaseScanner implements Runnable {
        public Document doc;
        public String[] line;
        @Override
        public void run() {
            try{
                
                parseField(line);
                
            }catch(NoSuchElementException ex){
                System.out.println("found end of file");
                running=false;
            }catch(IndexOutOfBoundsException ex){
                System.out.println("IndexOutofBounds "+ex.getMessage());
            }catch(IOException ex){
                System.out.println("fieldcreation failed");
            }
        }
    }
}
