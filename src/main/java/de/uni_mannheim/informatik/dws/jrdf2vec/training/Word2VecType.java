package de.uni_mannheim.informatik.dws.jrdf2vec.training;

import java.util.Locale;

/**
 * Type of word2vec model/approach like CBOW or SG.
 */
public enum Word2VecType {

    /**
     * Continuous bag-of-words model for word2vec.
     */
    CBOW,

    /**
     * Skip-gram model for word2vec.
     */
    SG;

    @Override
    public String toString(){
        return this.name().toLowerCase(Locale.ENGLISH);
    }
}