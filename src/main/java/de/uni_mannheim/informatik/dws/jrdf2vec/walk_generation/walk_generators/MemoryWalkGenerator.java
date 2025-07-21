package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Memory based walk generator using the {@link TripleDataSetMemory} data structure.
 * These kind of walk generators load the complete model into memory.
 */
public abstract class MemoryWalkGenerator implements IWalkGenerator,
        IMidWalkCapability, IMidWalkDuplicateFreeCapability, IRandomWalkDuplicateFreeCapability,
        IMidWalkWeightedCapability, IMidEdgeWalkDuplicateFreeCapability, IRandomWalkCapability,
        IMidTypeWalkDuplicateFreeCapability, INodeWalksDuplicateFreeCapability {


    /**
     * The actual data structure, i.e. a set of triples.
     */
    TripleDataSetMemory data;

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryWalkGenerator.class);

    /**
     * Indicator whether anonymous nodes shall be handled as if they were just one node.
     * E.g. _:genid413438 is handled like -&gt; ANODE
     */
    boolean isUnifyAnonymousNodes = false;

    /**
     * By default false.
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
     * Only required for {@link IMidTypeWalkDuplicateFreeCapability}.
     */
    private Set<String> typeProperties = new HashSet<>();

    private static final String[] DEFAULT_TYPE_PROPERTIES = {"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"};

    /**
     * Constructor
     */
    public MemoryWalkGenerator(){
        typeProperties.addAll(Arrays.asList(DEFAULT_TYPE_PROPERTIES));
    }

    /**
     * Weighted mid walk: If there are more options to go forward, it is more likely to go forward.
     * The walks are duplicate free.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param numberOfWalks Number of walks to be performed per entity.
     * @param depth         The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @return List of walks.
     */
    @Override
    public List<String> generateWeightedMidWalksForEntity(String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalksDuplicateFree(generateWeightedMidWalkForEntityAsArray(entity, numberOfWalks,
                depth));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param numberOfWalks The number of walks to be performed.
     * @param depth         The depth of each walk (where the depth is the number of hops).
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateWeightedMidWalkForEntityAsArray(String entity, int numberOfWalks, int depth) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < numberOfWalks; i++) {
            List<String> walk = generateWeightedMidWalkForEntity(entity, depth);
            if (walk.size() > 1) {
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

            if (candidatesPredecessor != null) numberOfPredecessors = candidatesPredecessor.size();
            if (candidatesSuccessor != null) numberOfSuccessors = candidatesSuccessor.size();

            // if there are no successors and predecessors: return current walk
            if (numberOfPredecessors == 0 && numberOfSuccessors == 0) return result;

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

    @Override
    public List<String> generateNodeWalksForEntity(String entity, int numberOfWalks, int depth){
        List<String> fullWalks = generateDuplicateFreeRandomWalksForEntity(entity, numberOfWalks, depth);
        Set<String> finalWalks = new HashSet<>();
        for (String walk : fullWalks){
            String[] walkComponents = walk.split(" ");
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            for(int i = 0; i < walkComponents.length; i++){
                if(i % 2 == 0){
                    if(isFirst) {
                        sb.append(walkComponents[i]);
                        isFirst = false;
                    } else {
                        sb.append(" ").append(walkComponents[i]);
                    }
                }
            }
            finalWalks.add(sb.toString());
        }
        return new ArrayList<>(finalWalks);
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     *
     * @param entity        The entity for which a walk shall be generated.
     * @param depth         The depth of each walk.
     * @param numberOfWalks The number of walks to be generated.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntity(String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalks(generateMidWalkForEntityAsArray(entity, numberOfWalks, depth));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param numberOfWalks The number of walks to be performed.
     * @param depth         The depth of each walk (where the depth is the number of hops).
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateMidWalkForEntityAsArray(String entity, int numberOfWalks, int depth) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < numberOfWalks; i++) {
            List<String> walk = generateMidWalkForEntity(entity, depth);
            if (walk.size() > 1) {
                result.add(walk);
            }
        }
        return result;
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     *
     * @param numberOfWalks The number of walks to be generated.
     * @param entity        The entity for which a walk shall be generated.
     * @param depth         The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalksDuplicateFree(generateMidWalkForEntityAsArray(entity, numberOfWalks, depth));
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     *
     * @param entity        The entity for which a walk shall be generated.
     * @param numberOfWalks The number of walks to be generated.
     * @param depth         The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidEdgeWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth) {
        List<List<String>> walksWithNodes = generateMidWalkForEntityAsArray(entity, numberOfWalks, depth);
        List<List<String>> result = new ArrayList<>();
        for (List<String> walkWithNodes : walksWithNodes) {

            // determine how often the entity appears (this is relevant in case there are "loops" in the walk
            // which involve the entity of interest multiple times!
            int appearances = getNumberOfAppearances(entity, walkWithNodes);

            // draw the desired position to keep
            int choice = getRandomNumberBetweenZeroAndX(appearances);

            List<String> walk = new ArrayList<>();
            int currentNodeOfInterestPosition = 0;
            for (int i = 0; i < walkWithNodes.size(); i++) {
                if (i % 2 == 0) {
                    String node = walkWithNodes.get(i);
                    if (node.equals(entity)) {
                        // we found the node of interest
                        if (currentNodeOfInterestPosition == choice) {
                            walk.add(node);
                            currentNodeOfInterestPosition++;
                        } else {
                            // -> we will not add the node of interest this time
                            currentNodeOfInterestPosition++;
                        }
                    }
                } else {
                    String edge = walkWithNodes.get(i);
                    walk.add(edge);
                }
            }
            result.add(walk);
        }
        return Util.convertToStringWalksDuplicateFree(result);
    }

    /**
     * Count how often entity appears in array and return result.
     *
     * @param entity Entity.
     * @param array  String array.
     * @return Number of times the entity appears in the array.
     */
    static int getNumberOfAppearances(String entity, Iterable<String> array) {
        int result = 0;
        for (String s : array) {
            if (s.equals(entity)) result++;
        }
        return result;
    }

    /**
     * Returns a random number between 0 and x (exclusive!).
     *
     * @param x Integer upper bound (exclusive).
     * @return Integer
     */
    static int getRandomNumberBetweenZeroAndX(int x) {
        return ThreadLocalRandom.current().nextInt(x);
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
     * Draw a random value from a set. This method is thread-safe.
     *
     * @param setToDrawFrom The set from which shall be drawn.
     * @param <T> Type
     * @return Drawn value of type T.
     */
    public static<T> T randomDrawFromSet(Set<T> setToDrawFrom) {
        int randomNumber = ThreadLocalRandom.current().nextInt(setToDrawFrom.size());
        int i = 0;
        for(T t : setToDrawFrom){
            if(i == randomNumber){
                return t;
            }
            i++;
        }
        return null;
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
     * Generate text walks. This only works if datatype triples/properties were parsed previously.
     *
     * @param entity The entity for which walks shall be generated.
     * @param depth  Must be &gt; 2.
     * @return List of walks.
     */
    public List<String> generateTextWalksForEntity(String entity, int depth) {
        List<String> result = new ArrayList<>();
        Set<String> datatypeSubjects = this.data.getUniqueDatatypeTripleSubjects();
        if (!datatypeSubjects.contains(entity)) {
            return result;
        }
        Map<String, Set<String>> tuples = this.data.getDatatypeTuplesForSubject(entity);

        for (Map.Entry<String, Set<String>> entry : tuples.entrySet()) {
            String predicate = entry.getKey();
            Set<String> texts = entry.getValue();
            StringBuffer walk = getNewBufferWalk(entity, predicate);
            int currentWalkLength = 2;
            for (String text : texts) {
                for (String token : text.split(" ")) {
                    walk.append(" ").append(this.textProcessingFunction.apply(token));
                    currentWalkLength++;
                    if (currentWalkLength == depth) {
                        result.add(walk.toString());
                        walk = getNewBufferWalk(entity, predicate);
                        currentWalkLength = 2;
                    }
                }
                if (walk.length() > entity.length() + predicate.length() + 1) {
                    result.add(walk.toString());
                    walk = getNewBufferWalk(entity, predicate);
                    currentWalkLength = 2;
                }
            }
        }
        return result;
    }

    private StringBuffer getNewBufferWalk(String subject, String predicate) {
        StringBuffer walk = new StringBuffer();
        walk.append(subject).append(" ").append(predicate);
        return walk;
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
        List<List<Triple>> walks = new ArrayList<>();
        boolean isFirstIteration = true;
        for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
            // initialize with first node
            if (isFirstIteration) {
                List<Triple> neighbours = data.getObjectTriplesInvolvingSubject(entity);
                if (neighbours == null || neighbours.size() == 0) {
                    return new ArrayList<>();
                }
                for (Triple neighbour : neighbours) {
                    ArrayList<Triple> individualWalk = new ArrayList<>();
                    individualWalk.add(neighbour);
                    walks.add(individualWalk);
                }
                isFirstIteration = false;
            } else {
                // create a copy
                List<List<Triple>> walks_tmp = new ArrayList<>(walks);

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
        return Util.convertToStringWalks(walks, entity, isUnifyAnonymousNodes());
    }

    @Override
    public List<String> generateMidTypeWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth){
        List<List<String>> walksWithNodes = generateMidWalkForEntityAsArray(entity, numberOfWalks, depth);
        List<List<String>> result = new ArrayList<>();
        for (List<String> walkWithNodes : walksWithNodes) {

            // determine how often the entity appears
            int appearances = getNumberOfAppearances(entity, walkWithNodes);

            // draw the desired position to keep
            int choice = getRandomNumberBetweenZeroAndX(appearances);

            List<String> walk = new ArrayList<>();
            int currentNodeOfInterestPosition = 0;
            for (int i = 0; i < walkWithNodes.size(); i++) {
                if (i % 2 == 0) {
                    String node = walkWithNodes.get(i);
                    if (node.equals(entity)) {
                        // we found the node of interest
                        if (currentNodeOfInterestPosition == choice) {
                            walk.add(node);
                            currentNodeOfInterestPosition++;
                        } else {
                            // -> we will not add the node of interest this time but instead its supertype
                            String type = getRandomSupertypeOfEntity(node);
                            if(type != null){
                                walk.add(type);
                            }
                            currentNodeOfInterestPosition++;
                        }
                    } else {
                        // we have a normal node that is not a node of interest
                        String type = getRandomSupertypeOfEntity(node);
                        if(type != null){
                            walk.add(type);
                        }
                    }
                } else {
                    String edge = walkWithNodes.get(i);
                    walk.add(edge);
                }
            }
            result.add(walk);
        }
        return Util.convertToStringWalksDuplicateFree(result);
    }

    /**
     * Draw a random supertype. Note that the predicates of {@link MemoryWalkGenerator#typeProperties} are used.
     * @param entity The entity for which the type shall be obtained.
     * @return Type. Null if there is no type.
     */
    public String getRandomSupertypeOfEntity(String entity){
        if(entity == null){
            return null;
        }
        Set<String> candidates = new HashSet<>();
        for(String property : getTypeProperties()) {
            Set<Triple> triples = this.getData().getObjectTriplesWithSubjectPredicate(entity, property);
            if(triples != null && triples.size() > 0){
                for(Triple triple : triples){
                    candidates.add(triple.object);
                }
            }
        }
        if(candidates.size() == 0){
            return null;
        } else {
            return randomDrawFromSet(candidates);
        }
    }

    @Override
    public List<String> generateRandomWalksForEntity(String entity, int numberOfWalks, int depth){
        List<String> result = new ArrayList<>();
        int currentDepth;
        String currentWalk;
        int currentWalkNumber = 0;

        nextWalk:
        while (currentWalkNumber < numberOfWalks) {
            currentWalkNumber++;
            String lastObject = entity;
            currentWalk = entity;
            currentDepth = 0;
            while (currentDepth < depth) {
                currentDepth++;
                Triple po = getRandomTripleForSubjectWithoutTags(lastObject);
                if(po != null){
                    currentWalk += " " + uriShortenerFunction.apply(po.predicate) + " " + uriShortenerFunction.apply(po.object);
                    lastObject = po.object;
                } else {
                    // The current walk cannot be continued -> add to list (if there is a walk of depth 1) and create next walk.
                    if(currentWalk.length() != entity.length()) result.add(currentWalk);
                    continue nextWalk;
                }
            }
            result.add(currentWalk);
        }
        return result;
    }

    /**
     * Faster version of {@link NtMemoryWalkGenerator#getRandomTripleForSubject(String)}.
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

    public long getDataSize() {
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

    @Override
    public Set<String> getTypeProperties() {
        return typeProperties;
    }
}
