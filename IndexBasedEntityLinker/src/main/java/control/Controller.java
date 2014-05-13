/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import model.EntityExtractor;
import model.GraphHandler;
import view.GUI;

/**
 *
 * @author Tim Pontzen
 */
public class Controller implements ActionListener{
    private GUI gui;
    private GraphHandler graph;
    EntityExtractor ner;

    public Controller(GraphHandler graph,EntityExtractor ner) {
        this.ner=ner;
        this.graph=graph;
        
        //for csv output
        graph.output=true;
    }
    
   public void setGui(GUI gui) {
        this.gui = gui;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Toggle tf_idf use":
                graph.tf_idf_useage=!graph.tf_idf_useage;
                break;
            case "anotate":
                float bTime=System.nanoTime();
                ArrayList<String> words=ner.searchEntities(gui.textInput.getText());
                float time=System.nanoTime()-bTime;
                System.out.println("Anotationtime :"+(time/(Math.pow(10,6))));
                String newText="";
                for(String s:words){
                    newText+=s+"\n";
                }
                
                gui.entityOutput.setText(newText);
                HashMap<String,String> map=graph.findMostPromisingURI(words);
                newText="";
                for(String s:words){
                    newText+=GraphHandler.getEntity(map.get(s))+"\n";
                }
                gui.neighborOutput.setText(newText);
                
//                System.out.println(time/(Math.pow(10,9)));
                //input.setText("Time: "+(time/(Math.pow(10,9))));
                break;
        }
    }
}
