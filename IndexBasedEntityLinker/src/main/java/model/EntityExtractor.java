

package model;

import java.util.ArrayList;

/**
 * Interface for Entity Extraction of a text.
 * @author Tim Pontzen
 * 
 */
public interface EntityExtractor {
    public ArrayList<String> anotate(String s);
}
