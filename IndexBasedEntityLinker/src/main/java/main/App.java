package main;

import control.Controller;
import model.GraphHandler;
import model.NER_Handler;
import view.GUI;

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
        Controller c=new Controller(graph, ner); 
        GUI gui=new GUI(c);
    }
}
