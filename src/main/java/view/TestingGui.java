/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import main.AnchorHandler;
import main.GraphHandler;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 *
 * @author Tim Pontzen
 */
public class TestingGui implements ActionListener{
    public JFrame frame;
    JTextField input ;
    JTextArea entityOutput ;
    JTextArea neighborOutput ;
    public GraphHandler graph;
//    public AnchorHandler anchor;
    public TestingGui(GraphHandler graph){
//        this.anchor=anchor;
        this.graph=graph;
        
        frame=new JFrame("Annotator");
        JPanel panel=new JPanel(new GridBagLayout());
        JButton go=new JButton("Go");
        go.setPreferredSize(new Dimension(100,40));
        frame.add(panel);
        panel.add(go);
        input=new JTextField();
        input.setPreferredSize(new Dimension(100,40));
        panel.add(input);
        
        
        entityOutput=new JTextArea("entity");
        entityOutput.setPreferredSize(new Dimension(300,200));
        panel.add(entityOutput);
        
        
        neighborOutput=new JTextArea("neighbors");
        neighborOutput.setPreferredSize(new Dimension(300,200));
        panel.add(neighborOutput);
        
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        go.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String uri=graph.getAnchorUri(input.getText());
        entityOutput.setText(uri);
        String[] tmp=uri.split("/");
        uri=tmp[tmp.length-1];
        ArrayList<Document> docs= graph.getDocs(uri);
        String neighbors="";
        for(Document d:docs){
            List<IndexableField> tmp2=d.getFields();
            neighbors=neighbors+d.get("neighbor")+"\n";
        }
        neighborOutput.setText(neighbors);
        
    }
}
