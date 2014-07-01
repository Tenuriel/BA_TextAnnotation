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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.EntityExtractor;
import model.GraphHandler;
import model.NER_Handler;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import view.GUI;

/**
 *
 * @author Tim Pontzen
 */
public class Controller implements ActionListener {

    private GUI gui;
    private GraphHandler graph;
    EntityExtractor ner;

    public Controller(GraphHandler graph, EntityExtractor ner) {
        this.ner = ner;
        this.graph = graph;

        //for csv output
//        graph.output=true;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!NER_Handler.error.isEmpty()){
            JOptionPane.showMessageDialog(null, NER_Handler.error);
            return;
        }
        switch (e.getActionCommand()) {
            case "Toggle tf_idf use":
                graph.tf_idf_useage = !graph.tf_idf_useage;
                break;
            case "csv output":
                graph.output = !graph.output;
                break;
            case "anotate":
                if(!graph.indexLoaded){
                    graph.initialize();
                }
                float bTime = System.nanoTime();
                ArrayList<String> words = ner.searchEntities(gui.textInput.getText());
                ArrayList<String> tmp = new ArrayList<>();
                for (String word : words) {
                    if (!word.matches(".*[\"\\']+.*")) {
                        tmp.add(QueryParserUtil.escape(word));
                    }
                }
                words = tmp;
                float time = System.nanoTime() - bTime;
                System.out.println("Anotationtime :" + (time / (Math.pow(10, 6))));
                String newText = "";
                for (String s : words) {
                    newText += s + "\n";
                }

                gui.entityOutput.setText(newText);
                HashMap<String, String> map = graph.findMostPromisingURI(words);
                newText = "";
                for (String s : words) {
                    newText += (map.get(s)) + "\n";
//                    newText+=GraphHandler.getEntity(map.get(s))+"\n";
                }
                gui.neighborOutput.setText(newText);
                break;
        }
    }
}
