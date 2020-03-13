package walkGenerators.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.dataStructure.Triple;
import walkGenerators.dataStructure.TripleDataSetMemory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Memory based parser using the {@link TripleDataSetMemory} data structure.
 * These kind of parsers load the complete model into memory.
 */
public abstract class MemoryParser implements IParser {

    /**
     * the actual data structure
     */
    TripleDataSetMemory data;

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryParser.class);

    /**
     * Walk generator that uses the parser.
     */
    WalkGenerator specificWalkGenerator;

    /**
     * Include datatype properties into walk generation.
     * Default false.
     */
    boolean isIncludeDatatypeProperties = false;

    /**
     * Indicator whether anonymous nodes shall be handled as if they were just one node.
     * E.g. _:genid413438 is handled like -> ANODE
     */
    boolean isUnifiyAnonymousNodes = false;


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
                ArrayList<Triple> candidates = data.getTriplesInvolvingObject(nextElementPredecessor);

                if (candidates != null && candidates.size() > 0) {
                    Triple drawnTriple = randomDrawFromList(candidates);

                    // add walks from the front (walk started before entity)
                    result.addFirst(drawnTriple.predicate);
                    result.addFirst(drawnTriple.subject);
                    nextElementPredecessor = drawnTriple.subject;
                }

            } else {
                // successor
                ArrayList<Triple> candidates = data.getTriplesInvolvingSubject(nextElementSuccessor);
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
        subject = specificWalkGenerator.shortenUri(removeTags(subject));
        ArrayList<Triple> queryResult = data.getTriplesInvolvingSubject(subject);
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
                ArrayList<Triple> neighbours = data.getTriplesInvolvingSubject(entity);
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
                    ArrayList<Triple> nextIteration = data.getTriplesInvolvingSubject(lastTriple.object);
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
            if (this.isUnifiyAnonymousNodes()) {
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
        ArrayList<Triple> queryResult = data.getTriplesInvolvingSubject(subject);
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

    public boolean isIncludeDatatypeProperties() {
        return isIncludeDatatypeProperties;
    }

    public boolean isUnifiyAnonymousNodes() {
        return isUnifiyAnonymousNodes;
    }

    public void setUnifiyAnonymousNodes(boolean unifiyAnonymousNodes) {
        isUnifiyAnonymousNodes = unifiyAnonymousNodes;
    }

    public WalkGenerator getSpecificWalkGenerator() {
        return specificWalkGenerator;
    }

    public void setSpecificWalkGenerator(WalkGenerator specificWalkGenerator) {
        this.specificWalkGenerator = specificWalkGenerator;
    }

    public TripleDataSetMemory getData() {
        return data;
    }

    public long getDataSize(){
        return data.getSize();
    }
}
