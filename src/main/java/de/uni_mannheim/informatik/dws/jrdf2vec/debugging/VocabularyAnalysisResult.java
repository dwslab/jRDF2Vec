package de.uni_mannheim.informatik.dws.jrdf2vec.debugging;

import java.util.HashSet;
import java.util.Set;

public class VocabularyAnalysisResult {

    private Set<String> subjectsNotFound = new HashSet<>();
    private Set<String> predicatesNotFound = new HashSet<>();
    private Set<String> objectsNotFound = new HashSet<>();

    private Set<String> additionalSubjects = new HashSet<>();
    private Set<String> additionalPredicates = new HashSet<>();
    private Set<String> additionalObjects = new HashSet<>();

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

    public Set<String> getAdditionalSubjects() {
        return additionalSubjects;
    }

    void setAdditionalSubjects(Set<String> additionalSubjects) {
        this.additionalSubjects = additionalSubjects;
    }

    public Set<String> getAdditionalPredicates() {
        return additionalPredicates;
    }

    void setAdditionalPredicates(Set<String> additionalPredicates) {
        this.additionalPredicates = additionalPredicates;
    }

    public Set<String> getAdditionalObjects() {
        return additionalObjects;
    }

    void setAdditionalObjects(Set<String> additionalObjects) {
        this.additionalObjects = additionalObjects;
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
        Set<String> additional = new HashSet<>();
        additional.addAll(additionalSubjects);
        additional.addAll(additionalPredicates);
        additional.addAll(additionalObjects);
        return additional;
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
}
