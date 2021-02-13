package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.TripleDataSetMemory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

/**
 * Memory based parser using the {@link TripleDataSetMemory} data structure.
 * These kind of parsers load the complete model into memory.
 */
public abstract class MemoryParser implements IParser {

    /**
     * The actual data structure, i.e. a set of triples.
     */
    TripleDataSetMemory data;

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryParser.class);

    /**
     * Indicator whether anonymous nodes shall be handled as if they were just one node.
     * E.g. _:genid413438 is handled like -&gt; ANODE
     */
    boolean isUnifyAnonymousNodes = false;

    /**
     * By default false.
     * TODO: Include in parsers which inherit!
     */
    boolean isParseDatatypeProperties = false;

    /**
     * Function to transform URIs while parsing.
     */
    UnaryOperator<String> uriShortenerFunction;

    /**
     * Function to transform data type text.
     */
    UnaryOperator<String> textProcessingFunction = new TextProcessor();

    /**
     * Weighted mid walk: If there are more options to go forward, it is more likely to go forward.
     * @param entity The entity for which walks shall be generated.
     * @param depth The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @param numberOfWalks Number of walks to be performed per entity.
     * @return List of walks.
     */
    public List<String> generateWeightedMidWalksForEntity(String entity, int depth, int numberOfWalks) {
        return convertToStringWalks(generateWeightedMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     * @param entity The entity for which walks shall be generated.
     * @param depth The depth of each walk (where the depth is the number of hops).
     * @param numberOfWalks The number of walks to be performed.
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateWeightedMidWalkForEntityAsArray(String entity, int depth, int numberOfWalks) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < numberOfWalks; i++) {
            List<String> walk = generateWeightedMidWalkForEntity(entity, depth);
            if(walk.size() > 1) {
                result.add(walk);
            }
        }
        return result;
    }

    /**
     * Generates a single walk for the given entity with the given depth.
     *
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @return One walk as list where each element is a walk component.
     */
    public List<String> generateWeightedMidWalkForEntity(String entity, int depth) {
        LinkedList<String> result = new LinkedList<>();

        String nextElementPredecessor = entity;
        String nextElementSuccessor = entity;

        // initialize result
        result.add(entity);

        // variable to store the number of iterations performed so far
        int currentDepth = 0;

        while (currentDepth < depth) {
            currentDepth++;

            // randomly decide whether to use predecessors or successors
            double randomPickZeroOne = ThreadLocalRandom.current().nextDouble(0.0, 1.00000001);

            // predecessor candidates
            List<Triple> candidatesPredecessor = data.getObjectTriplesInvolvingObject(nextElementPredecessor);

            // successor candidates
            List<Triple> candidatesSuccessor = data.getObjectTriplesInvolvingSubject(nextElementSuccessor);

            double numberOfPredecessors = 0.0;
            double numberOfSuccessors = 0.0;

            if(candidatesPredecessor != null) numberOfPredecessors = candidatesPredecessor.size();
            if(candidatesSuccessor != null) numberOfSuccessors = candidatesSuccessor.size();

            // if there are no successors and predecessors: return current walk
            if(numberOfPredecessors == 0 && numberOfSuccessors == 0) return result;

            // determine cut-off point
            double cutOffPoint = numberOfPredecessors / (numberOfPredecessors + numberOfSuccessors);

            if (randomPickZeroOne <= cutOffPoint) {
                // predecessor
                if (candidatesPredecessor != null && candidatesPredecessor.size() > 0) {
                    Triple drawnTriple = randomDrawFromList(candidatesPredecessor);

                    // add walks from the front (walk started before entity)
                    result.addFirst(drawnTriple.predicate);
                    result.addFirst(drawnTriple.subject);
                    nextElementPredecessor = drawnTriple.subject;
                }
            } else {
                // successor
                if (candidatesSuccessor != null && candidatesSuccessor.size() > 0) {
                    Triple tripleToAdd = randomDrawFromList(candidatesSuccessor);

                    // add next walk iteration
                    result.addLast(tripleToAdd.predicate);
                    result.addLast(tripleToAdd.object);
                    nextElementSuccessor = tripleToAdd.object;
                }
            }
        }
        return result;
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     *
     * @param entity        The entity for which a walk shall be generated.
     * @param depth         The depth of each walk.
     * @param numberOfWalks The number of walks to be generated.
     * @return List where every item is a walk separated by spaces.
     */
    public List<String> generateMidWalksForEntity(String entity, int depth, int numberOfWalks) {
        return convertToStringWalks(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    public List<String> convertToStringWalks(List<List<String>> dataStructureToConvert) {
        List<String> result = new ArrayList<>();
        for (List<String> individualWalk : dataStructureToConvert) {
            String walk = "";
            boolean isFirst = true;
            for (String walkComponent : individualWalk) {
                if (isFirst) {
                    isFirst = false;
                    walk = walkComponent;
                } else {
                    walk += " " + walkComponent;
                }
            }
            result.add(walk);
        }
        return result;
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     * @param entity The entity for which walks shall be generated.
     * @param depth The depth of each walk (where the depth is the number of hops).
     * @param numberOfWalks The number of walks to be performed.
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateMidWalkForEntityAsArray(String entity, int depth, int numberOfWalks) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < numberOfWalks; i++) {
            List<String> walk = generateMidWalkForEntity(entity, depth);
            if(walk.size() > 1) {
                result.add(walk);
            }
        }
        return result;
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     * @param numberOfWalks The number of walks to be generated.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    public List<String> generateMidWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth){
        return convertToStringWalksDuplicateFree(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Given a list of walks where a walk is represented as a List of strings, this method will convert that
     * into a list of strings where a walk is one string (and the elements are separated by spaces).
     * The lists are duplicate free.
     * @param dataStructureToConvert The data structure that shall be converted.
     * @return Data structure converted to string list.
     */
    public List<String> convertToStringWalksDuplicateFree(List<List<String>> dataStructureToConvert) {
        HashSet<String> uniqueSet = new HashSet<>();
        for (List<String> individualWalk : dataStructureToConvert){
            String walk = "";
            boolean isFirst = true;
            for(String walkComponent : individualWalk){
                if(isFirst){
                    isFirst = false;
                    walk = walkComponent;
                } else {
                    walk += " " + walkComponent;
                }
            }
            uniqueSet.add(walk);
        }
        return new ArrayList<>(uniqueSet);
    }

    /**
     * Generates a single walk for the given entity with the given depth.
     *
     * @param entity The entity for which a walk shall be generated.
     * @param depth  The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @return One walk as list where each element is a walk component.
     */
    public List<String> generateMidWalkForEntity(String entity, int depth) {
        LinkedList<String> result = new LinkedList<>();

        String nextElementPredecessor = entity;
        String nextElementSuccessor = entity;

        // initialize result
        result.add(entity);

        // variable to store the number of iterations performed so far
        int currentDepth = 0;

        while (currentDepth < depth) {
            currentDepth++;

            // randomly decide whether to use predecessors or successors
            int randomPickZeroOne = ThreadLocalRandom.current().nextInt(2);

            if (randomPickZeroOne == 0) {
                // predecessor
                List<Triple> candidates = data.getObjectTriplesInvolvingObject(nextElementPredecessor);

                if (candidates != null && candidates.size() > 0) {
                    Triple drawnTriple = randomDrawFromList(candidates);

                    // add walks from the front (walk started before entity)
                    result.addFirst(drawnTriple.predicate);
                    result.addFirst(drawnTriple.subject);
                    nextElementPredecessor = drawnTriple.subject;
                }
            } else {
                // successor
                List<Triple> candidates = data.getObjectTriplesInvolvingSubject(nextElementSuccessor);
                if (candidates != null && candidates.size() > 0) {
                    Triple tripleToAdd = randomDrawFromList(candidates);

                    // add next walk iteration
                    result.addLast(tripleToAdd.predicate);
                    result.addLast(tripleToAdd.object);
                    nextElementSuccessor = tripleToAdd.object;
                }

            }
        }
        return result;
    }

    /**
     * Draw a random value from a List. This method is thread-safe.
     *
     * @param listToDrawFrom The list from which shall be drawn.
     * @param <T>            Type
     * @return Drawn value of type T.
     */
    public static <T> T randomDrawFromList(List<T> listToDrawFrom) {
        int randomNumber = ThreadLocalRandom.current().nextInt(listToDrawFrom.size());
        return listToDrawFrom.get(randomNumber);
    }

    /**
     * Obtain a triple for the given subject.
     *
     * @param subject The subject for which a random predicate and object shall be found.
     * @return Triple, randomly obtained for the given subject.
     */
    public Triple getRandomTripleForSubject(String subject) {
        if (subject == null) return null;
        subject = uriShortenerFunction.apply(removeTags(subject));
        List<Triple> queryResult = data.getObjectTriplesInvolvingSubject(subject);
        if (queryResult == null) {
            // no triple found
            return null;
        }
        int randomNumber = ThreadLocalRandom.current().nextInt(queryResult.size());
        LOGGER.info("(" + Thread.currentThread().getName() + ") " + randomNumber);
        return queryResult.get(randomNumber);
    }

    /**
     * Generates duplicate-free walks for the given entity.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param numberOfWalks The number of walks to be generated.
     * @param depth         The number of hops to nodes (!).
     * @return A list of walks where each element in the list represents a walk. The walk elements are separated by
     * spaces.
     */
    public List<String> generateDuplicateFreeRandomWalksForEntity(String entity, int numberOfWalks, int depth) {
        List<String> result = new ArrayList<>();
        List<List<Triple>> walks = new ArrayList();
        boolean isFirstIteration = true;
        for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
            // initialize with first node
            if (isFirstIteration) {
                List<Triple> neighbours = data.getObjectTriplesInvolvingSubject(entity);
                if (neighbours == null || neighbours.size() == 0) {
                    return result;
                }
                for (Triple neighbour : neighbours) {
                    ArrayList<Triple> individualWalk = new ArrayList<>();
                    individualWalk.add(neighbour);
                    walks.add(individualWalk);
                }
                isFirstIteration = false;
            } else {
                // create a copy
                List<List<Triple>> walks_tmp = new ArrayList<>();
                walks_tmp.addAll(walks);

                // loop over current walks
                for (List<Triple> walk : walks_tmp) {
                    // get last entity
                    Triple lastTriple = walk.get(walk.size() - 1);
                    List<Triple> nextIteration = data.getObjectTriplesInvolvingSubject(lastTriple.object);
                    if (nextIteration != null) {
                        walks.remove(walk); // check whether this works
                        for (Triple nextStep : nextIteration) {
                            List<Triple> newWalk = new ArrayList<>(walk);
                            newWalk.add(nextStep);
                            walks.add(newWalk);
                        }
                    }
                } // loop over walks
            }

            // trim the list
            while (walks.size() > numberOfWalks) {
                int randomNumber = ThreadLocalRandom.current().nextInt(walks.size());
                walks.remove(randomNumber);
            }
        } // depth loop

        // now we need to translate our walks into strings
        for (List<Triple> walk : walks) {
            String finalSentence = entity;
            if (this.isUnifyAnonymousNodes()) {
                for (Triple po : walk) {
                    String object = po.object;
                    if (isAnonymousNode(object)) {
                        object = "ANode";
                    }
                    finalSentence += " " + po.predicate + " " + object;
                }
            } else {
                for (Triple po : walk) {
                    finalSentence += " " + po.predicate + " " + po.object;
                }
            }
            result.add(finalSentence);
        }
        return result;
    }

    /**
     * Returns true if the given parameter follows the schema of an anonymous node
     *
     * @param uriString The URI string to be checked.
     * @return True if anonymous node.
     */
    public boolean isAnonymousNode(String uriString) {
        uriString = uriString.trim();
        if (uriString.startsWith("_:genid")) {
            return true;
        } else return false;
    }

    /**
     * Faster version of {@link NtMemoryParser#getRandomTripleForSubject(String)}.
     * Note that there cannot be any leading less-than or trailing greater-than signs around the subject.
     * The subject URI should already be shortened.
     *
     * @param subject The subject for which a random predicate and object shall be found.
     * @return Predicate and object, randomly obtained for the given subject.
     */
    public Triple getRandomTripleForSubjectWithoutTags(String subject) {
        if (subject == null) return null;
        List<Triple> queryResult = data.getObjectTriplesInvolvingSubject(subject);
        if (queryResult == null) {
            // no triple found
            return null;
        }
        int randomNumber = ThreadLocalRandom.current().nextInt(queryResult.size());
        //System.out.println("(" + Thread.currentThread().getName() + ") " + randomNumber + "[" + queryResult.size() + "]");
        return queryResult.get(randomNumber);
    }

    /**
     * This method will remove a leading less-than and a trailing greater-than sign (tags).
     *
     * @param stringToBeEdited The string that is to be edited.
     * @return String without tags.
     */
    public static String removeTags(String stringToBeEdited) {
        if (stringToBeEdited.startsWith("<")) stringToBeEdited = stringToBeEdited.substring(1);
        if (stringToBeEdited.endsWith(">"))
            stringToBeEdited = stringToBeEdited.substring(0, stringToBeEdited.length() - 1);
        return stringToBeEdited;
    }

    // getters and setters below

    public boolean isUnifyAnonymousNodes() {
        return isUnifyAnonymousNodes;
    }

    public void setUnifyAnonymousNodes(boolean unifyAnonymousNodes) {
        isUnifyAnonymousNodes = unifyAnonymousNodes;
    }

    public TripleDataSetMemory getData() {
        return data;
    }

    public boolean isParseDatatypeProperties() {
        return isParseDatatypeProperties;
    }

    public void setParseDatatypeProperties(boolean parseDatatypeProperties) {
        isParseDatatypeProperties = parseDatatypeProperties;
    }

    public long getDataSize(){
        if (data == null) {
            return 0L;
        } else return data.getObjectTripleSize();
    }

    public UnaryOperator<String> getTextProcessingFunction() {
        return textProcessingFunction;
    }

    public void setTextProcessingFunction(UnaryOperator<String> textProcessingFunction) {
        this.textProcessingFunction = textProcessingFunction;
    }
}
