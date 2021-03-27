package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;

import java.util.Set;

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
        return data.getUniqueSubjects();
    }
}
