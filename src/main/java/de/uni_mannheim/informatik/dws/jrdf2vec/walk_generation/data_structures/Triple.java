package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures;

/**
 * Data structure for a triple whereby the object can be a string or a URI.
 */
public class Triple {


    /**
     * Constructor
     * @param subject Subject
     * @param predicate Predicate
     * @param object Object
     */
    public Triple(String subject, String predicate, String object){
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public String subject;
    public String predicate;
    public String object;

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof Triple)) return false;
        Triple that = (Triple) obj;
        return this.subject.equals(that.subject) && this.predicate.equals(that.predicate) && this.object.equals(that.object);
    }

    @Override
    public int hashCode(){
        return (subject + "_1").hashCode() + (predicate + "_2").hashCode() + (object + "_2").hashCode();
    }
}
