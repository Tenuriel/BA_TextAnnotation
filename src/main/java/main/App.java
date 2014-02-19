package main;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        new TestingGui(graph);
//        new DBpediaHandler(anchor);
//        try {
//            anchor.iR.close();
//        } catch (IOException ex) {
//            System.out.println("Could not close writer");
//        }
    }
}
