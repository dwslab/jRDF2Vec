package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures;

import java.util.*;

/**
 * There is a distinction in (1) object triples where the object is a URI and
 * (2) datatpye triples where the object is a string.
 *
 *
 * Developer remarks:
 * For reasons of performance, deletions are not possible currently.
 */
public class TripleDataSetMemory {


    /**
     * Constructor
     */
    public TripleDataSetMemory(){
        subjectToObjectTriples = new HashMap<>();
        predicateToObjectTriples = new HashMap<>();
        objectToObjectTriples = new HashMap<>();
        subjectToDatatypeTuples = new HashMap<>();
        objectTriples = new HashSet<>();
    }

    Map<String, ArrayList<Triple>> subjectToObjectTriples;
    Map<String, ArrayList<Triple>> predicateToObjectTriples;
    Map<String, ArrayList<Triple>> objectToObjectTriples;
    Set<Triple> objectTriples;

    /**
     * Map key: subject URI.
     * Map value: predicate values map with key: property URI, value: Set of values
     */
    Map<String, Map<String, Set<String>>> subjectToDatatypeTuples;

    /**
     * The total number of triples with an object property.
     */
    private long objectTripleSize = 0;

    /**
     * Add the given triple as specified by its components.
     * @param subject Subject
     * @param predicate Predicate
     * @param object Object
     */
    public void addObjectTriple(String subject, String predicate, String object){
        addObjectTriple(new Triple(subject, predicate, object));
    }

    public void addDatatypeTriple(String subject, String predicate, String stringObject){
        addDatatypeTriple(new Triple(subject, predicate, stringObject));
    }

    public synchronized void addDatatypeTriple(Triple tripleToAdd){
        if(this.subjectToDatatypeTuples.containsKey(tripleToAdd.subject)){
            Map<String, Set<String>> propertyMap = this.subjectToDatatypeTuples.get(tripleToAdd.subject);

            if(propertyMap.containsKey(tripleToAdd.predicate)){
                Set<String> propertyValues = propertyMap.get(tripleToAdd.predicate);
                propertyValues.add(tripleToAdd.object);
            } else {
                // there exists nothing for this datatype property (the predicate)
                // let's quickly create it:
                HashSet<String> propertyValues = new HashSet<>();
                propertyValues.add(tripleToAdd.object);
                propertyMap.put(tripleToAdd.predicate, propertyValues);
            }
        } else {
            // string value:
            HashSet<String> propertyValues = new HashSet<>();
            propertyValues.add(tripleToAdd.object);

            // datatype property:
            HashMap<String, Set<String>> propertyMap = new HashMap<>();
            propertyMap.put(tripleToAdd.predicate, propertyValues);

            // add to index:
            this.subjectToDatatypeTuples.put(tripleToAdd.subject, propertyMap);
        }
    }

    /**
     * Add the given triple.
     * @param tripleToAdd Triple to be added.
     */
    public synchronized void addObjectTriple(Triple tripleToAdd){
        if(this.objectTriples.contains(tripleToAdd)){
            return;
        }
        ArrayList<Triple> subjectToTripleList = subjectToObjectTriples.get(tripleToAdd.subject);
        if(subjectToTripleList == null){
            ArrayList<Triple> newList = new ArrayList<>();
            newList.add(tripleToAdd);
            subjectToObjectTriples.put(tripleToAdd.subject, newList);
        } else subjectToTripleList.add(tripleToAdd);

        ArrayList<Triple> predicateToTripleList = predicateToObjectTriples.get(tripleToAdd.predicate);
        if(predicateToTripleList == null){
            ArrayList<Triple> newList = new ArrayList<>();
            newList.add(tripleToAdd);
            predicateToObjectTriples.put(tripleToAdd.predicate, newList);
        } else predicateToTripleList.add(tripleToAdd);

        ArrayList<Triple> objectToTripleList = objectToObjectTriples.get(tripleToAdd.object);
        if(objectToTripleList == null){
            ArrayList<Triple> newList = new ArrayList<>();
            newList.add(tripleToAdd);
            objectToObjectTriples.put(tripleToAdd.object, newList);
        } else objectToTripleList.add(tripleToAdd);
        objectTriples.add(tripleToAdd);
        objectTripleSize++;
    }

    /**
     * Adds all triples of {@code dataToAdd} to this triple set.
     * @param dataToAdd The data that shall be added to this triple set
     */
    public synchronized void addAllObjectTriples(TripleDataSetMemory dataToAdd){
        for(Triple triple : dataToAdd.objectTriples){
            this.addObjectTriple(triple);
        }
    }

    public Map<String, Set<String>> getDatatypeTuplesForSubject(String subject){
        return subjectToDatatypeTuples.get(subject);
    }

    public Set<Triple> getAllObjectTriples(){
        return this.objectTriples;
    }

    public List<Triple> getObjectTriplesInvolvingSubject(String subject){
        return subjectToObjectTriples.get(subject);
    }

    public List<Triple> getObjectTriplesInvolvingPredicate(String predicate){
        return predicateToObjectTriples.get(predicate);
    }

    public List<Triple> getObjectTriplesInvolvingObject(String object){
        return objectToObjectTriples.get(object);
    }

    /**
     * Returns the number of managed triples.
     * @return The number of managed triples.
     */
    public long getObjectTripleSize(){
        return objectTripleSize;
    }

    /**
     * Returns unique object triple and datatype triple subjects! If you only need object triple subjects use {@link TripleDataSetMemory#getUniqueObjectTripleSubjects()}.
     * @return Returns unique object triple and datatype triple subjects!
     */
    public Set<String> getUniqueSubjects(){
        Set<String> result = new HashSet<>();
        result.addAll(getUniqueObjectTripleSubjects());
        result.addAll(getUniqueDatatypeTripleSubjects());
        return result;
    }

    /**
     * Obtain a set of all subjects involved in datatype triples.
     * @return Set of subjects.
     */
    public Set<String> getUniqueDatatypeTripleSubjects(){
        return this.subjectToDatatypeTuples.keySet();
    }

    /**
     * Obtain a set of all subjects involved in object triples.
     * @return Set of subjects.
     */
    public Set<String> getUniqueObjectTripleSubjects(){
        return subjectToObjectTriples.keySet();
    }

    /**
     * Obtain a set of all subjects and objects.
     * @return Set of subjects and objects.
     */
    public Set<String> getUniqueObjectTripleSubjectsAndObjects(){
        HashSet<String> result = new HashSet<>(subjectToObjectTriples.keySet().size() + objectToObjectTriples.keySet().size());
        result.addAll(subjectToObjectTriples.keySet());
        result.addAll(objectToObjectTriples.keySet());
        return result;
    }

    /**
     * Obtain a set of all objects.
     * @return Set of all objets.
     */
    public Set<String> getUniqueObjectTripleObjects(){
        return objectToObjectTriples.keySet();
    }

    /**
     * Obtain a set of all predicates.
     * @return Set of all predicates.
     */
    public Set<String> getUniqueObjectTriplePredicates(){
        return predicateToObjectTriples.keySet();
    }

}
