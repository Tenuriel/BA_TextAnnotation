/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.DefaultSimilarity;

/**
 *
 * @author Tim Pontzen
 */
public class CustomSimilarity extends DefaultSimilarity{
    public CustomSimilarity(){
        super();
    }
    @Override
    public float lengthNorm(FieldInvertState state) {
        return 1;
    }
}
