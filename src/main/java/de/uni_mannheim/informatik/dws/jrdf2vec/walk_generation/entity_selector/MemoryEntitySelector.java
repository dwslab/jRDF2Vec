package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;

import java.util.HashSet;
import java.util.Set;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
/**
 * This entity selector selects all unique nodes.
 */
public class MemoryEntitySelector implements EntitySelector {


    /**
     * Constructor.
     * @param data Triple data set to be used.
     */
    public MemoryEntitySelector(TripleDataSetMemory data){
        this.data = data;
    }

    private TripleDataSetMemory data;

    @Override
    public Set<String> getEntities() {
        Set<String> result = new HashSet<>();
        result.addAll(data.getUniqueSubjects());
        result.addAll(data.getUniqueObjectTripleObjects());

        return result;
    }
}
