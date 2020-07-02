package de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.dataStructures.TripleDataSetMemory;

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
