package walkGenerators;

/**
 * Data structure used by parsers.
 * Just a data structure, no functionality here.
 */
class PredicateObject {
    String predicate;
    String object;

    /**
     * Constructor
     * @param predicate
     * @param object
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