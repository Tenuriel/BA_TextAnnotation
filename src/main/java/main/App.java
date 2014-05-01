package main;

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
        GraphHandler graph=new GraphHandler();
        NER_Handler ner=new NER_Handler();
        new TestingGui(graph,ner);
    }
}
