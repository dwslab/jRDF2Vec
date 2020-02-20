package walkGenerators.base;

/**
 * Data structure used by parsers.
 * Just a data structure, no functionality here.
 */
@Deprecated
class PredicateObject {
    String predicate;
    String object;

    /**
     * Constructor
     * @param predicate Predicate of triple (Subject, Predicate, Object)
     * @param object Object of triple (Subject, Predicate, Object)
     */
    public PredicateObject(String predicate, String object){
        this.predicate = predicate;
        this.object = object;
    }

    @Override
    public String toString(){
        return this.predicate + " " + this.object;
    }
}