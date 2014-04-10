package main;

import model.AnchorHandler;
import model.DBpediaHandler;
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
//        GraphHandler graph=new GraphHandler();
//        NER_Handler ner=new NER_Handler();
//        new TestingGui(graph,ner);
        new DBpediaHandler();
//        try {
//            anchor.iR.close();
//        } catch (IOException ex) {
//            System.out.println("Could not close writer");
//        }
    }
}
