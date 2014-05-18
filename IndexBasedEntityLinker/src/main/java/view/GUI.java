/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import control.Controller;
import edu.stanford.nlp.io.IOUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import model.EntityExtractor;
import model.GraphHandler;

/**
 *
 * @author Tim Pontzen
 */
public class GUI {

    public static final int textAreaHeight = 500;
    /**
     * label for the entityOutput field.
     */
    public static JLabel entity_label = new JLabel("entity");
    /**
     * labe for neighborOutpur field.
     */
    public static JLabel neighbors_label = new JLabel("URIs");
    /**
     * label for textInput field.
     */
    public static JLabel text_label = new JLabel("Text");
    /**
     * inputfield for anchorsearch.
     */
    public static JTextField input;
    /**
     * found entitys in the text.
     */
    public JTextArea entityOutput;
    /**
     * found uris for the entity.
     */
    public JTextArea neighborOutput;
    /**
     * The Controller for the Buttonevents.
     */
    public Controller controller;
    /**
     * area for the text to be tokenized.
     */
    public JTextArea textInput;
    public JFrame frame;
    /**
     * The default Textfile loaded into textInput.
     */
    public static String defaultFile = "./src/main/resources/testText_first_Lady.txt";

    public GUI(Controller controller) {
        this.controller = controller;
        controller.setGui(this);

        String defaultText = "";
        try {
            File file = new File(defaultFile);
            defaultText = IOUtils.slurpFile(file);
        } catch (IOException | NullPointerException ex) {
            System.out.println("Error while reading default file: " + ex.getMessage());
        }
        frame = new JFrame("Annotator");
        JPanel panel = new JPanel(new GridBagLayout());
        frame.add(panel);
        GridBagConstraints c = new GridBagConstraints();

//        JButton go=new JButton("Search for Anchor");
        JPanel optionP=new JPanel(new GridBagLayout());
        JToggleButton go = new JToggleButton("Toggle tf_idf use");
        go.setPreferredSize(new Dimension(150, 40));
        c.gridx = 0;
        c.gridy = 0;
        optionP.add(go,c);
        panel.add(optionP, c);
        go.addActionListener(controller);
        go = new JToggleButton("csv output");
        go.setPreferredSize(new Dimension(150, 40));
        c.gridx = 1;
        go.addActionListener(controller);
        optionP.add(go,c);
        
        
        JButton anotate = new JButton("anotate");
        anotate.setPreferredSize(new Dimension(150, 40));
        c.gridx = 2;
        c.gridy = 0;
        panel.add(anotate, c);
        anotate.addActionListener(controller);

        input = new JTextField();
        input.setPreferredSize(new Dimension(100, 40));
        c.gridx = 1;
        c.gridy = 0;
        panel.add(input, c);

        text_label.setPreferredSize(new Dimension(100, 40));
        c.gridx = 0;
        c.gridy = 1;
        panel.add(text_label, c);

        entity_label.setPreferredSize(new Dimension(100, 40));
        c.gridx = 1;
        c.gridy = 1;
        panel.add(entity_label, c);

        neighbors_label.setPreferredSize(new Dimension(100, 40));
        c.gridx = 2;
        c.gridy = 1;
        panel.add(neighbors_label, c);

//        Border border = BorderFactory.createLineBorder(Color.BLACK);
        Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);

        textInput = new JTextArea(defaultText);
        textInput.setBorder(border);
        textInput.setLineWrap(true);
        textInput.setPreferredSize(new Dimension(300, textAreaHeight));
        c.gridx = 0;
        c.gridy = 2;
        panel.add(textInput, c);

        entityOutput = new JTextArea();
        border = BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK);
        entityOutput.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        c.gridx = 1;
        c.gridy = 2;
        JScrollPane scroll = new JScrollPane();
        scroll.setPreferredSize(new Dimension(300, textAreaHeight));
        scroll.setViewportView(entityOutput);
        panel.add(scroll, c);

        neighborOutput = new JTextArea();
        border = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
        neighborOutput.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
//        neighborOutput.setPreferredSize(new Dimension(300,textAreaHeight));
        c.gridx = 2;
        c.gridy = 2;
        scroll = new JScrollPane();
        scroll.setPreferredSize(new Dimension(300, textAreaHeight));
        scroll.setViewportView(neighborOutput);
        panel.add(scroll, c);
//        panel.add(neighborOutput,c);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();

    }
}
