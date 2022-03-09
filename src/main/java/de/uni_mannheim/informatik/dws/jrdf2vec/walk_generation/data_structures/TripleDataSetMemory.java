package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An in-memory storage option for triples. The storage <em>only</em> allows adding triples!
 * Indices are built and convenience functions are offered to quickly access the triple data.
 * <p>
 * There is a distinction in (1) object triples where the object is a URI and
 * (2) datatpye triples where the object is a string.
 * <p>
 * <p>
 * Developer remarks:
 * - Deletions are not possible currently.
 */
public class TripleDataSetMemory {


    /**
     * Constructor
     */
    public TripleDataSetMemory() {
        subjectToObjectTriples = new HashMap<>();
        predicateToObjectTriples = new HashMap<>();
        objectToObjectTriples = new HashMap<>();
        subjectToDatatypeTuples = new HashMap<>();
        objectTriples = new HashSet<>();
        objectNodes = new HashSet<>();
    }

    /**
     * Form:
     * {@code subject -> (predicate -> triple) }
     */
    Map<String, Map<String, Set<Triple>>> subjectToObjectTriples;

    /**
     * Form:
     * {@code predicate -> List<Triple>}
     */
    Map<String, List<Triple>> predicateToObjectTriples;

    /**
     * Form:
     * {@code object -> (predicate -> triple) }
     */
    Map<String, Map<String, Set<Triple>>> objectToObjectTriples;
    Set<Triple> objectTriples;

    /**
     * Node URIs (no string values).
     */
    Set<String> objectNodes;

    /**
     * Map key: subject URI.
     * Map value: predicate values map with key: property URI, value: Set of values
     */
    Map<String, Map<String, Set<String>>> subjectToDatatypeTuples;

    private static final Logger LOGGER = LoggerFactory.getLogger(TripleDataSetMemory.class);

    /**
     * Add the given triple as specified by its components.
     *
     * @param subject   Subject
     * @param predicate Predicate
     * @param object    Object
     */
    public void addObjectTriple(String subject, String predicate, String object) {
        addObjectTriple(new Triple(subject, predicate, object));
    }

    public void addDatatypeTriple(String subject, String predicate, String stringObject) {
        addDatatypeTriple(new Triple(subject, predicate, stringObject));
    }

    /**
     * Add a triple where the object is a string.
     *
     * @param tripleToAdd Triple where the object is a string.
     */
    public synchronized void addDatatypeTriple(Triple tripleToAdd) {
        this.objectNodes.add(tripleToAdd.subject);
        if (this.subjectToDatatypeTuples.containsKey(tripleToAdd.subject)) {
            Map<String, Set<String>> propertyMap = this.subjectToDatatypeTuples.get(tripleToAdd.subject);

            if (propertyMap.containsKey(tripleToAdd.predicate)) {
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
     * Add the given triple (thread-safe).
     *
     * @param tripleToAdd Triple to be added.
     */
    public synchronized void addObjectTriple(Triple tripleToAdd) {
        if (this.objectTriples.contains(tripleToAdd)) {
            return;
        }
        this.objectNodes.add(tripleToAdd.subject);
        this.objectNodes.add(tripleToAdd.object);
        Map<String, Set<Triple>> subjectPredicateToTripleMap = subjectToObjectTriples.get(tripleToAdd.subject);

        if (subjectPredicateToTripleMap == null) {
            Map<String, Set<Triple>> predicateToObjectMap = new HashMap<>();
            Set<Triple> triples = new HashSet<>();
            triples.add(tripleToAdd);
            predicateToObjectMap.put(tripleToAdd.predicate, triples);
            subjectToObjectTriples.put(tripleToAdd.subject, predicateToObjectMap);
        } else {
            // there is already an entry for the subject
            // check for predicate
            if (subjectPredicateToTripleMap.containsKey(tripleToAdd.predicate)) {
                // predicate contained, add our object
                subjectPredicateToTripleMap.get(tripleToAdd.predicate).add(tripleToAdd);
            } else {
                // predicate not contained, add predicate -> object
                Set<Triple> triples = new HashSet<>();
                triples.add(tripleToAdd);
                subjectPredicateToTripleMap.put(tripleToAdd.predicate, triples);
            }
        }

        List<Triple> predicateToTripleList = predicateToObjectTriples.get(tripleToAdd.predicate);
        if (predicateToTripleList == null) {
            ArrayList<Triple> newList = new ArrayList<>();
            newList.add(tripleToAdd);
            predicateToObjectTriples.put(tripleToAdd.predicate, newList);
        } else predicateToTripleList.add(tripleToAdd);

        Map<String, Set<Triple>> objectPredicateToTripleMap = objectToObjectTriples.get(tripleToAdd.object);
        if (objectPredicateToTripleMap == null) {
            Map<String, Set<Triple>> predicateToObjectMap = new HashMap<>();
            Set<Triple> triples = new HashSet<>();
            triples.add(tripleToAdd);
            predicateToObjectMap.put(tripleToAdd.predicate, triples);
            objectToObjectTriples.put(tripleToAdd.object, predicateToObjectMap);
        } else {
            // there is already an entry for the object
            // check for predicate
            if (objectPredicateToTripleMap.containsKey(tripleToAdd.predicate)) {
                // predicate contained, add our object
                objectPredicateToTripleMap.get(tripleToAdd.predicate).add(tripleToAdd);
            } else {
                // predicate not contained, add predicate -> object
                Set<Triple> triples = new HashSet<>();
                triples.add(tripleToAdd);
                objectPredicateToTripleMap.put(tripleToAdd.predicate, triples);
            }

        }
        objectTriples.add(tripleToAdd);
    }

    /**
     * Adds all triples of {@code dataToAdd} to this triple set.
     *
     * @param dataToAdd The data that shall be added to this triple set
     */
    public synchronized void addAllObjectTriples(TripleDataSetMemory dataToAdd) {
        for (Triple triple : dataToAdd.objectTriples) {
            this.addObjectTriple(triple);
        }
    }

    public Map<String, Set<String>> getDatatypeTuplesForSubject(String subject) {
        return subjectToDatatypeTuples.get(subject);
    }

    public Set<Triple> getAllObjectTriples() {
        return this.objectTriples;
    }

    public List<Triple> getObjectTriplesInvolvingSubject(String subject) {
        Map<String, Set<Triple>> subjectObjects = subjectToObjectTriples.get(subject);
        if (subjectObjects == null) return null;
        List<Triple> result = new ArrayList<>();
        for (Map.Entry<String, Set<Triple>> entry : subjectObjects.entrySet()) {
            result.addAll(entry.getValue());
        }
        return result;
    }

    public List<Triple> getObjectTriplesInvolvingPredicate(String predicate) {
        return predicateToObjectTriples.get(predicate);
    }


    public List<Triple> getObjectTriplesInvolvingObject(String object) {
        Map<String, Set<Triple>> objectPredicates = objectToObjectTriples.get(object);
        if (objectPredicates == null) return null;
        List<Triple> result = new ArrayList<>();
        objectPredicates.forEach((key, value) -> result.addAll(value));
        return result;
    }

    /**
     * This method allows stating (S, P, ?) queries for object property triples.
     * It will not return datatype triples.
     *
     * @param subject   The desired subject.
     * @param predicate Desired predicate.
     * @return Set of triples. Null if nothing was found.
     */
    public Set<Triple> getObjectTriplesWithSubjectPredicate(String subject, String predicate) {
        if (subject == null || predicate == null) return null;
        Map<String, Set<Triple>> s = subjectToObjectTriples.get(subject);
        if (s == null) return null;
        return s.get(predicate);
    }

    /**
     * This method allows stating (?, P, O) queries for object property triples.
     * It will not return datatype triples.
     *
     * @param object    The desired object.
     * @param predicate Desired predicate.
     * @return Set of triples. Null if nothing was found.
     */
    public Set<Triple> getObjectTriplesWithPredicateObject(String predicate, String object) {
        if (object == null || predicate == null) return null;
        Map<String, Set<Triple>> s = objectToObjectTriples.get(object);
        if (s == null) return null;
        return s.get(predicate);
    }


    /**
     * Returns the number of managed object triples.
     *
     * @return The number of managed object triples.
     */
    public long getObjectTripleSize() {
        return objectTriples.size();
    }

    /**
     * Returns unique object triple and datatype triple subjects! If you only need object triple subjects use {@link TripleDataSetMemory#getUniqueObjectTripleSubjects()}.
     *
     * @return Returns unique object triple and datatype triple subjects!
     */
    public Set<String> getUniqueSubjects() {
        Set<String> result = new HashSet<>();
        result.addAll(getUniqueObjectTripleSubjects());
        result.addAll(getUniqueDatatypeTripleSubjects());
        return result;
    }

    /**
     * Obtain a set of all subjects involved in datatype triples.
     *
     * @return Set of subjects.
     */
    public Set<String> getUniqueDatatypeTripleSubjects() {
        return this.subjectToDatatypeTuples.keySet();
    }

    /**
     * Obtain a set of all subjects involved in object triples.
     *
     * @return Set of subjects.
     */
    public Set<String> getUniqueObjectTripleSubjects() {
        return subjectToObjectTriples.keySet();
    }

    /**
     * Obtain a set of all subjects and objects.
     *
     * @return Set of subjects and objects.
     */
    public Set<String> getUniqueObjectTripleSubjectsAndObjects() {
        HashSet<String> result = new HashSet<>(subjectToObjectTriples.keySet().size() + objectToObjectTriples.keySet().size());
        result.addAll(subjectToObjectTriples.keySet());
        result.addAll(objectToObjectTriples.keySet());
        return result;
    }

    /**
     * Remove the provided object triple from all indices.
     * @param tripleToBeRemoved The triple that shall be removed.
     */
    public void removeObjectTriple(Triple tripleToBeRemoved) {
        if (tripleToBeRemoved == null) {
            LOGGER.warn("tripleToBeRemoved is null.");
            return;
        }
        if (!objectTriples.contains(tripleToBeRemoved)) {
            LOGGER.warn("Object triple not found: (" + tripleToBeRemoved.subject + ", "
                    + tripleToBeRemoved.predicate + ", " + tripleToBeRemoved.object + ")");
            return;
        }
        objectTriples.remove(tripleToBeRemoved);

        // remove from sp index
        Map<String, Set<Triple>> sPredicateTripleMap = subjectToObjectTriples.get(tripleToBeRemoved.subject);
        if (sPredicateTripleMap != null) {
            Set<Triple> triples = sPredicateTripleMap.get(tripleToBeRemoved.predicate);
            triples.remove(tripleToBeRemoved);
            if (triples.size() == 0) {
                // remove key
                sPredicateTripleMap.remove(tripleToBeRemoved.predicate);
                if (sPredicateTripleMap.size() == 0) {
                    subjectToObjectTriples.remove(tripleToBeRemoved.subject);
                }
            }
        }

        // predicate index
        List<Triple> predicateTriples = predicateToObjectTriples.get(tripleToBeRemoved.predicate);
        predicateTriples.remove(tripleToBeRemoved);
        if (predicateTriples.size() == 0) {
            predicateToObjectTriples.remove(tripleToBeRemoved.predicate);
        }

        // remove from op index
        Map<String, Set<Triple>> oPredicateTripleMap = objectToObjectTriples.get(tripleToBeRemoved.object);
        if (oPredicateTripleMap != null) {
            Set<Triple> triples = oPredicateTripleMap.get(tripleToBeRemoved.predicate);
            triples.remove(tripleToBeRemoved);
            if (triples.size() == 0) {
                oPredicateTripleMap.remove(tripleToBeRemoved.predicate);
                if (oPredicateTripleMap.size() == 0) {
                    objectToObjectTriples.remove(tripleToBeRemoved.object);
                }
            }
        }

        removeFromObjectTriplesIfNotExists(tripleToBeRemoved.subject);
        removeFromObjectTriplesIfNotExists(tripleToBeRemoved.object);
    }

    /**
     * Checks whether the provided {@code nodeId} is used somewhere. If not, it removes the nodeId from the
     * {@link TripleDataSetMemory#objectTriples}.
     * @param nodeId The nodeId that shall be removed.
     */
    private void removeFromObjectTriplesIfNotExists(String nodeId){
        if (subjectToObjectTriples.get(nodeId) == null
                && objectToObjectTriples.get(nodeId) == null
                && subjectToDatatypeTuples.get(nodeId) == null) {
            objectTriples.remove(nodeId);
        }
    }

    /**
     * Obtain a set of all objects.
     *
     * @return Set of all objets.
     */
    public Set<String> getUniqueObjectTripleObjects() {
        return objectToObjectTriples.keySet();
    }

    /**
     * Obtain a set of all predicates.
     *
     * @return Set of all predicates.
     */
    public Set<String> getUniqueObjectTriplePredicates() {
        return predicateToObjectTriples.keySet();
    }

    public Set<String> getObjectNodes() {
        return objectNodes;
    }

    public int getNumberOfObjectNodes() {
        return objectNodes.size();
    }

    /**
     * Get a set of subjects given a set of triples.
     *
     * @param triples A set of triples.
     * @return The set of all subjects.
     */
    public static Set<String> getSubjectsFromTripleSet(Set<Triple> triples) {
        if (triples == null) {
            return null;
        }
        return triples.stream().map(x -> x.subject).collect(Collectors.toSet());
    }

    /**
     * Get a set of predicates given a set of triples.
     *
     * @param triples A set of triples.
     * @return The set of all predicates.
     */
    public static Set<String> getPredicatesFromTripleSet(Set<Triple> triples) {
        if (triples == null) {
            return null;
        }
        return triples.stream().map(x -> x.predicate).collect(Collectors.toSet());
    }

    /**
     * Get a set of predicates given a set of triples.
     *
     * @param triples A set of triples.
     * @return The set of all predicates.
     */
    public static Set<String> getObjectsFromTripleSet(Set<Triple> triples) {
        if (triples == null) {
            return null;
        }
        return triples.stream().map(x -> x.object).collect(Collectors.toSet());
    }

}
