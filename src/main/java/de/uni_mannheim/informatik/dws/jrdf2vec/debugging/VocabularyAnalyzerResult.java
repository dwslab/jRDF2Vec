package de.uni_mannheim.informatik.dws.jrdf2vec.debugging;

import java.util.HashSet;
import java.util.Set;

/**
 * Result object of {@link VocabularyAnalyzer}.
 */
public class VocabularyAnalyzerResult {

    private Set<String> subjectsNotFound = new HashSet<>();
    private Set<String> predicatesNotFound = new HashSet<>();
    private Set<String> objectsNotFound = new HashSet<>();
    private Set<String> additionalConcepts = new HashSet<>();

    /**
     * The dimension of the vectors in the model.
     */
    private int dimension = -1;

    /**
     * Check whether the dimension in the embedding is consistent (e.g. all vectors are of dimension 200).
     */
    private boolean isDimensionConsistent = true;

    public Set<String> getSubjectsNotFound() {
        return subjectsNotFound;
    }

    void setSubjectsNotFound(Set<String> subjectsNotFound) {
        this.subjectsNotFound = subjectsNotFound;
    }

    public Set<String> getPredicatesNotFound() {
        return predicatesNotFound;
    }

    void setPredicatesNotFound(Set<String> predicatesNotFound) {
        this.predicatesNotFound = predicatesNotFound;
    }

    public Set<String> getObjectsNotFound() {
        return objectsNotFound;
    }

    void setObjectsNotFound(Set<String> objectsNotFound) {
        this.objectsNotFound = objectsNotFound;
    }


    /**
     * Obtain all concepts that have not been found.
     * @return A set of all URIs for which no embedding exists.
     */
    public Set<String> getAllNotFound(){
        Set<String> notFound = new HashSet<>();
        notFound.addAll(getSubjectsNotFound());
        notFound.addAll(getPredicatesNotFound());
        notFound.addAll(getObjectsNotFound());
        return notFound;
    }

    /**
     * Obtain all concepts that are in the embedding space but not in the original source.
     * @return A set of all URIs for which no embeddings exist.
     */
    public Set<String> getAllAdditional(){
        return this.additionalConcepts;
    }

    /**
     * Set the concepts that are in the embedding space but not in the original source.
     * @param additionalConcepts The concepts to be set.
     */
    void setAllAdditional(Set<String> additionalConcepts){
        this.additionalConcepts = additionalConcepts;
    }

    public int getDimension() {
        return dimension;
    }

    void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public boolean isDimensionConsistent() {
        return isDimensionConsistent;
    }

    void setDimensionConsistent(boolean dimensionConsistent) {
        isDimensionConsistent = dimensionConsistent;
    }


    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();

        buffer.append("Dimension: " + this.getDimension() + "\n");
        buffer.append("Consistent file: " + this.isDimensionConsistent());
        buffer.append("\n\n");

        int numberOfSubjectsNotFound = this.getSubjectsNotFound().size();
        buffer.append("Subjects not found [" + numberOfSubjectsNotFound + "]");
        if(numberOfSubjectsNotFound > 0) {
            buffer.append(":");
            for (String s : this.getSubjectsNotFound()) {
                buffer.append("\n" + s);
            }
        }
        buffer.append("\n\n");

        int numberOfPredicatesNotFound = this.getPredicatesNotFound().size();
        buffer.append("Predicates not found [" + numberOfPredicatesNotFound + "]");
        if(numberOfPredicatesNotFound > 0) {
            buffer.append(":");
            for (String s : this.getPredicatesNotFound()) {
                buffer.append("\n" + s);
            }
        }
        buffer.append("\n\n");


        int numberOfObjectsNotFound = this.getObjectsNotFound().size();
        buffer.append("Objects not found [" + numberOfObjectsNotFound + "]");
        if(numberOfObjectsNotFound > 0) {
            buffer.append(":");
            for (String s : this.getObjectsNotFound()) {
                buffer.append("\n" + s);
            }
        }
        buffer.append("\n\n");

        int numberOfAllNotFound = this.getAllNotFound().size();
        buffer.append("Total concepts not found [" + numberOfAllNotFound + "]");
        if(numberOfAllNotFound > 0){
            buffer.append(":");
            for (String s : this.getAllNotFound()) {
                buffer.append("\n" + s);
            }
        }
        buffer.append("\n\n");

        int numberOfAdditionalConcepts = this.getAllAdditional().size();
        buffer.append("Additional concepts [" + numberOfAdditionalConcepts + "]");
        if(numberOfAdditionalConcepts > 0){
            buffer.append(":");
            for (String s : this.getAllAdditional()){
                buffer.append("\n" + s);
            }
        }
        buffer.append("\n\n");

        return buffer.toString();
    }

}
