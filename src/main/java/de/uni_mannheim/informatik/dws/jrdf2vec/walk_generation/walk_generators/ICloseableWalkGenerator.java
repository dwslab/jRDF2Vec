package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

public interface ICloseableWalkGenerator extends IWalkGenerator {


    /**
     * Close open resources.
     */
    void close();
}
