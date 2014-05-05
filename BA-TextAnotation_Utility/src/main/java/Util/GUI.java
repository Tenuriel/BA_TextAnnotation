/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Tim Pontzen
 */
public class GUI implements ActionListener {

    public static Dimension buttDim = new Dimension(170, 100);
    private JFrame frame;
    /**
     * inputfield for the properties-filename.
     */
    private JTextField propInput;
    private JTextField abstractInput;
    private JTextArea console;
    public GUI() {
        frame = new JFrame("Utils");
        JPanel panel = new JPanel(new GridBagLayout());
        frame.add(panel);
        GridBagConstraints c = new GridBagConstraints();
        
        propInput=new JTextField("Filename");
        propInput.setPreferredSize(new Dimension(170, 20));
        c.gridx = 0;
        c.gridy = 0;
        panel.add(propInput,c);
        
        abstractInput=new JTextField("Filename");
        abstractInput.setPreferredSize(new Dimension(170, 20));
        c.gridx = 4;
        c.gridy = 0;
        panel.add(abstractInput,c);
        
        JButton b = new JButton("Clean Properties");
        b.addActionListener(this);
        b.setPreferredSize(buttDim);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(b, c);

        b = new JButton("Extract Anchors");
        b.addActionListener(this);
        b.setPreferredSize(buttDim);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(b, c);

        b = new JButton("Pre BlockIndex");
        b.addActionListener(this);
        b.setPreferredSize(buttDim);
        c.gridx = 2;
        c.gridy = 1;
        panel.add(b, c);

        b = new JButton("Create BlockIndex");
        b.addActionListener(this);
        b.setPreferredSize(buttDim);
        c.gridx = 3;
        c.gridy = 1;
        panel.add(b, c);

        b = new JButton("Clean Abstracts");
        b.addActionListener(this);
        b.setPreferredSize(buttDim);
        c.gridx = 4;
        c.gridy = 1;
        panel.add(b, c);

        b = new JButton("Create Abstract Index");
        b.addActionListener(this);
        b.setPreferredSize(buttDim);
        c.gridx = 5;
        c.gridy = 1;
        panel.add(b, c);
        
        console=new JTextArea();
        console.setEditable(false);
        JScrollPane conPane=new JScrollPane(console);
        conPane.setPreferredSize(new Dimension(buttDim.width*6, 60));
        c.gridx=0;
        c.gridwidth=6;
        c.gridy=2;
        c.fill=GridBagConstraints.HORIZONTAL;
        panel.add(conPane,c);
        
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Clean Properties":
                console.append(DBpediaHandler.cleanMappingProb(propInput.getText())+"\n");
                break;
            case "Extract Anchors":
                console.append(Index_Handler.extracAnchors()+"\n");
                break;
            case "Pre BlockIndex":
                DBpediaHandler.setIDs();
                console.append(DBpediaHandler.createENAN()+"\n");
                break;
            case "Create BlockIndex":
                console.append(Index_Handler.createBlockIndex()+"\n");
                break;
            case "Clean Abstracts":
                console.append(DBpediaHandler.cleanAbstracts("")+"\n");
                break;
            case "Create Abstract Index":
                console.append(Index_Handler.createAbstract_Index()+"\n");
                break;
            default:
        }
    }
}
