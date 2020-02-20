package walkGenerators.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Memory based parser using the {@link walkGenerators.base.PredicateObject} data structure.
 * These kind of parsers load the complete model into memory.
 */
public abstract class MemoryParser implements IParser {

    /**
     * the actual data structure
     */
    Map<String, ArrayList<PredicateObject>> data;

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
     * Obtain a predicate and object for the given subject.
     *
     * @param subject The subject for which a random predicate and object shall be found.
     * @return Predicate and object, randomly obtained for the given subject.
     */
    public PredicateObject getRandomPredicateObjectForSubject(String subject) {
        if (subject == null) return null;
        subject = specificWalkGenerator.shortenUri(removeTags(subject));
        ArrayList<PredicateObject> queryResult = data.get(subject);
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
     * @return A list of walks.
     */
    public List<String> generateDuplicateFreeRandomWalksForEntity(String entity, int numberOfWalks, int depth) {
        List<String> result = new ArrayList<>();
        List<List<PredicateObject>> walks = new ArrayList();
        boolean isFirstIteration = true;
        for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
            // initialize with first node
            if (isFirstIteration) {
                ArrayList<PredicateObject> neighbours = data.get(entity);
                if (neighbours == null || neighbours.size() == 0) {
                    return result;
                }
                for (PredicateObject neighbour : neighbours) {
                    ArrayList<PredicateObject> individualWalk = new ArrayList<>();
                    individualWalk.add(neighbour);
                    walks.add(individualWalk);
                }
                isFirstIteration = false;
            }

            // create a copy
            List<List<PredicateObject>> walks_tmp = new ArrayList<>();
            walks_tmp.addAll(walks);

            // loop over current walks
            for (List<PredicateObject> walk : walks_tmp) {
                // get last entity
                PredicateObject lastPredicateObject = walk.get(walk.size() - 1);
                ArrayList<PredicateObject> nextIteration = data.get(lastPredicateObject.object);
                if (nextIteration != null) {
                    walks.remove(walk); // check whether this works
                    for (PredicateObject nextStep : nextIteration) {
                        List<PredicateObject> newWalk = new ArrayList<>(walk);
                        newWalk.add(nextStep);
                        walks.add(newWalk);
                    }
                }
            } // loop over walks

            // trim the list
            while (walks.size() > numberOfWalks) {
                int randomNumber = ThreadLocalRandom.current().nextInt(walks.size());
                walks.remove(randomNumber);
            }
        } // depth loop

        // now we need to translate our walks into strings
        for (List<PredicateObject> walk : walks) {
            String finalSentence = entity;
            if (this.isUnifiyAnonymousNodes()) {
                for (PredicateObject po : walk) {
                    String object = po.object;
                    if (isAnonymousNode(object)) {
                        object = "ANode";
                    }
                    finalSentence += " " + po.predicate + " " + object;
                }
            } else {
                for (PredicateObject po : walk) {
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
     * Add data in a thread safe way.
     *
     * @param subject   The subject to be added.
     * @param predicate The predicate to be added.
     * @param object    The object to be added.
     */
    synchronized void addToDataThreadSafe(String subject, String predicate, String object) {
        if (data.get(subject) == null) {
            ArrayList<PredicateObject> list = new ArrayList<>();
            list.add(new PredicateObject(predicate, object));
            data.put(subject, list);
        } else {
            ArrayList<PredicateObject> list = data.get(subject);
            list.add(new PredicateObject(predicate, object));
        }
    }


    /**
     * Faster version of {@link NtMemoryParser#getRandomPredicateObjectForSubject(String)}.
     * Note that there cannot be any leading less-than or trailing greater-than signs around the subject.
     * The subject URI should already be shortened.
     *
     * @param subject The subject for which a random predicate and object shall be found.
     * @return Predicate and object, randomly obtained for the given subject.
     */
    public PredicateObject getRandomPredicateObjectForSubjectWithoutTags(String subject) {
        if (subject == null) return null;
        ArrayList<PredicateObject> queryResult = data.get(subject);
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

}
