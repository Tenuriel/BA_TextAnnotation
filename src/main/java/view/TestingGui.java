/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import model.AnchorHandler;
import model.GraphHandler;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import edu.stanford.nlp.io.IOUtils;
import java.io.File;
import java.sql.ResultSet;
import model.DBpediaHandler;
import model.DatabaseHandler;
import model.NER_Handler;

/**
 *
 * @author Tim Pontzen
 */
public class TestingGui implements ActionListener{
    /**
     * label for the entityOutput field.
     */
    public static JLabel entity_label=new JLabel("entity");
    /**
     * labe for neighborOutpur field.
     */
    public static JLabel neighbors_label=new JLabel("neighbors");
    /**
     * label for textInput field.
     */
    public static JLabel text_label=new JLabel("Text");
    /**
     * inputfield for anchorsearch.
     */
    public JTextField input ;
    /**
     * found entitys in the anchor.
     */
    public JTextArea entityOutput ;
    /**
     * foudn neighbors for the entity.
     */
    public JTextArea neighborOutput ;
    /**
     * area for the text to be tokenized.
     */
    public JTextArea textInput ;
    public NER_Handler ner;
    public JFrame frame;
    public GraphHandler graph;
    public static String defaultFile="./src/main/resources/testText_first_Lady.txt";
//    public AnchorHandler anchor;
    public TestingGui(GraphHandler graph,NER_Handler ner){
//        this.anchor=anchor;
        this.graph=graph;
        this.ner=ner;
        String defaultText="";
        try{
            File file=new File(defaultFile);
            defaultText=IOUtils.slurpFile(file);
        }catch(IOException|NullPointerException ex){
            System.out.println("Error while reading default file: "+ex.getMessage());
        }
        frame=new JFrame("Annotator");
        JPanel panel=new JPanel(new GridBagLayout());
        frame.add(panel);
        GridBagConstraints c=new GridBagConstraints();
        
        JButton go=new JButton("Search for Anchor");
        go.setPreferredSize(new Dimension(150,40));       
        c.gridx=0;
        c.gridy=0;        
        panel.add(go,c);
        go.addActionListener(this);
        
        JButton anotate=new JButton("anotate");
        anotate.setPreferredSize(new Dimension(150,40));       
        c.gridx=2;
        c.gridy=0;        
        panel.add(anotate,c);
        anotate.addActionListener(this);
        
        input=new JTextField();
        input.setPreferredSize(new Dimension(100,40));
        c.gridx=1;
        c.gridy=0;   
        panel.add(input,c);
        
        text_label.setPreferredSize(new Dimension(100, 40));
        c.gridx=0;
        c.gridy=1;   
        panel.add(text_label,c);
        
        entity_label.setPreferredSize(new Dimension(100, 40));
        c.gridx=1;
        c.gridy=1;   
        panel.add(entity_label,c);
        
        neighbors_label.setPreferredSize(new Dimension(100, 40));
        c.gridx=2;
        c.gridy=1;   
        panel.add(neighbors_label,c);
        
        
//        Border border = BorderFactory.createLineBorder(Color.BLACK);
        Border border=BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
        
        textInput=new JTextArea(defaultText);
        textInput.setBorder(border);
        textInput.setLineWrap(true);
        textInput.setPreferredSize(new Dimension(300,200));
        c.gridx=0;
        c.gridy=2;   
        panel.add(textInput,c);
               
       
        entityOutput=new JTextArea();
        border=BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK);
        entityOutput.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        entityOutput.setPreferredSize(new Dimension(300,200));
        c.gridx=1;
        c.gridy=2;   
        panel.add(entityOutput,c);
        
        
        neighborOutput=new JTextArea();
        border=BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
        neighborOutput.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        neighborOutput.setPreferredSize(new Dimension(300,200));
        c.gridx=2;
        c.gridy=2;   
        panel.add(neighborOutput,c);
        
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Search for Anchor":
//                ArrayList<String> uri = graph.getAnchorUri(input.getText());
//                entityOutput.setText(uri.get(0));
//                //        String[] tmp=uri.split("/");
//                //        uri=tmp[tmp.length-1];
//                ArrayList<Document> docs = graph.getDocs(GraphHandler.formatStringForQuery(uri.get(0)));
//                String neighbors = "";
//                for (Document d : docs) {
//                    neighbors = neighbors + d.get("neighbor") + "\n";
//                }
//                neighborOutput.setText(neighbors);
                break;
            case "anotate":
                float bTime=System.nanoTime();
                ArrayList<String> words=ner.anotate(textInput.getText());
                float time=System.nanoTime()-bTime;
                System.out.println("Anotationtime :"+(time/(Math.pow(10,9))));
                String newText="";
                for(String s:words){
                    newText+=s+"\n";
                }
                
                DatabaseHandler db=DatabaseHandler.getInstance();
                db.fillA(words);
                bTime=System.nanoTime();
                
                ResultSet set=db.getUri();
                time=System.nanoTime()-bTime;
                entityOutput.setText(newText);
                newText="";
                for(String s:words){
                    newText+=s+"\n";
                }
                neighborOutput.setText(newText);
                
//                System.out.println(time/(Math.pow(10,9)));
                input.setText("Time: "+(time/(Math.pow(10,9))));
                break;
        }

    }
}
