/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tim Pontzen
 */
public class NER_Handler implements EntityExtractor{
    /**
     * The path to the classifiers for the ner.
     */
    public static final String CLASSIFIER_PATH="src/main/resources/classifiers/"+
            "english.conll.4class.distsim.crf.ser.gz";
    /**
     * the classfier for the ner.
     */
    public static AbstractSequenceClassifier<CoreLabel> classifier;
    /**
     * 
     */
    public NER_Handler(){
        classifier= CRFClassifier.getClassifierNoExceptions(CLASSIFIER_PATH);
       // classifier.
                
    }
    /**
     * uses the stanford ner to tokenize a string
     * @param s the string to be tokenized
     * @return all found entitys
     */
    @Override
    public ArrayList<String> anotate(String s) {
        ArrayList<String> result = new ArrayList<>();
        List<List<CoreLabel>> ner_out = classifier.classify(s);
        String tmp = "";
        for (List<CoreLabel> sentence : ner_out) {
            for (CoreLabel word : sentence) {
                if (!word.get(CoreAnnotations.AnswerAnnotation.class).toString().equals("O")) {
                    tmp +=" "+ word.originalText();
                } else if (!tmp.equals("")) {
                    if(!result.contains(tmp.trim())){
                        result.add(tmp.trim());
                    }                 
                    tmp = "";
                }
            }
        }
        return result;
    }
}
