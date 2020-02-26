package walkGenerators.base;

import walkGenerators.dataStructure.TripleDataSetMemory;

import java.util.HashSet;
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
