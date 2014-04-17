package main;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AnchorHandler;
import model.DBpediaHandler;
import model.DatabaseHandler;
import model.GraphHandler;
import model.NER_Handler;
import view.TestingGui;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
//        new TestingStuff();
//        AnchorHandler anchor=new AnchorHandler();
        GraphHandler graph=new GraphHandler();
//        NER_Handler ner=new NER_Handler();
//        new TestingGui(graph,ner);
//        new DBpediaHandler();
//        DatabaseHandler db=DatabaseHandler.getInstance();
//        try {
//            db.con.close();
//        } catch (SQLException ex) {
//            System.exit(-1);
//        }
    }
}
