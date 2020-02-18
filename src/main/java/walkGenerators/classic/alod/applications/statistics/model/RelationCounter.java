package walkGenerators.classic.alod.applications.statistics.model;

/**
 * Data structure to persist relation counts.
 */
public class RelationCounter {
    public int isHyponymCount = 0;
    public int isHypernymCount = 0;

    /**
     * Returns the total number of relations, i.e. the sum of Hyponyms and Hypernyms.
     * @return The relation count as integer.
     */
    public int getRelationCount(){
        return isHypernymCount + isHyponymCount;
    }
}
