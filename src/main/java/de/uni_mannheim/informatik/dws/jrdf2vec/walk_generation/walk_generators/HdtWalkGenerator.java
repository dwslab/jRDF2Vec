package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A parser for HDT files.
 */
public class HdtWalkGenerator implements IWalkGenerator, IMidWalkCapability, IMidWalkDuplicateFreeCapability,
IMidWalkWeightedCapability {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HdtWalkGenerator.class);

    /**
     * The data set to be used by the parser.
     */
    HDT hdtDataSet;

    /**
     * Constructor
     *
     * @param hdtFilePath Path to the HDT file.
     * @exception IOException IOException
     */
    public HdtWalkGenerator(String hdtFilePath) throws IOException {
        try {
            hdtDataSet = HDTManager.loadHDT(hdtFilePath);
        } catch (IOException e) {
            LOGGER.error("Failed to load HDT file: " + hdtFilePath + "\nProgram will fail.", e);
            throw e;
        }
    }

    /**
     * Constructor
     *
     * @param hdtFile HDT file to be used.
     * @exception IOException IOException
     */
    public HdtWalkGenerator(File hdtFile) throws IOException {
        this(hdtFile.getAbsolutePath());
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     * @param numberOfWalks The number of walks to be generated.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth){
        return Util.convertToStringWalksDuplicateFree(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     * @param numberOfWalks The number of walks to be generated.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntity(java.lang.String entity, int numberOfWalks, int depth){
        return Util.convertToStringWalks(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     * @param entity The entity for which walks shall be generated.
     * @param depth The depth of each walk (where the depth is the number of hops).
     * @param numberOfWalks The number of walks to be performed.
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateMidWalkForEntityAsArray(String entity, int depth, int numberOfWalks){
        List<List<String>> result = new ArrayList<>();
        for(int i = 0; i < numberOfWalks; i++){
            List<String> walk = generateMidWalkForEntity(entity, depth);
            if(walk.size() > 1) {
                result.add(walk);
            }
        }
        return result;
    }

    /**
     * Generates a single walk for the given entity with the given depth.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
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
                try {
                    IteratorTripleString iterator = hdtDataSet.search("", "",
                            nextElementPredecessor);
                    HashSet<TripleString> candidates = new HashSet<>();

                    TripleString ts;
                    while (iterator.hasNext()) {
                        ts = iterator.next();
                        candidates.add(ts);
                    }

                    if (candidates.size() > 0) {
                        TripleString drawnTriple = Util.randomDrawFromSet(candidates);

                        // add walks from the front (walk started before entity)
                        result.addFirst(drawnTriple.getPredicate().toString());
                        result.addFirst(drawnTriple.getSubject().toString());
                        nextElementPredecessor = drawnTriple.getSubject().toString();
                    }

                } catch (NotFoundException e) {
                    LOGGER.error("Search exception while trying to find a predecessor.", e);
                }
            } else {
                // successor
                try {
                    IteratorTripleString iterator = hdtDataSet.search(nextElementSuccessor, "", "");
                    HashSet<TripleString> candidates = new HashSet<>();

                    TripleString ts;
                    CharSequence objectCharSequence;
                    while (iterator.hasNext()) {
                        ts = iterator.next();
                        objectCharSequence = ts.getObject();
                        if (objectCharSequence.charAt(0) == '"') {
                            continue;
                        }
                        candidates.add(ts);
                    }
                    if (candidates.size() > 0) {
                        TripleString stringToAdd = Util.randomDrawFromSet(candidates);

                        // add next walk iteration
                        result.addLast(stringToAdd.getPredicate().toString());
                        result.addLast(stringToAdd.getObject().toString());
                        nextElementSuccessor = stringToAdd.getObject().toString();
                    }
                } catch (NotFoundException e) {
                    LOGGER.error("Search exception while trying to find a successor.", e);
                }
            }
        }
        return result;
    }

    /**
     * Writes the given hdt data set as nt file.
     * @param dataSet Set to read.
     * @param fileToWrite File to write
     */
    public static void serializeDataSetAsNtFile(HDT dataSet, File fileToWrite) {
        try {
            IteratorTripleString iterator = dataSet.search("", "", "");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite), StandardCharsets.UTF_8));
            while (iterator.hasNext()) {
                TripleString ts = iterator.next();
                if(ts.getObject().toString().startsWith("\"")){
                writer.write("<" + ts.getSubject().toString() + "> <" + ts.getPredicate().toString() + "> " + ts.getObject().toString() + " .\n");
                } else {
                    writer.write("<" + ts.getSubject().toString() + "> <" + ts.getPredicate().toString() + "> <" + ts.getObject().toString() + "> .\n");
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException | NotFoundException e) {
            LOGGER.error("Could not write file.", e);
        }
    }

    /**
     * Weighted mid walk: If there are more options to go forward, it is more likely to go forward.
     * The walks are duplicate free.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param depth         The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @param numberOfWalks Number of walks to be performed per entity.
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
            Set<TripleString> candidatesPredecessor = new HashSet<>();
            try {
                IteratorTripleString iterator = hdtDataSet.search("", "",
                        nextElementPredecessor);
                while (iterator.hasNext()){
                    TripleString ts = iterator.next();
                    CharSequence objectCharSequence = ts.getObject();
                    if (objectCharSequence.charAt(0) == '"') {
                        continue;
                    }
                    candidatesPredecessor.add(ts);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }


            // successor candidates
            Set<TripleString> candidatesSuccessor = new HashSet<>();
            try {
                IteratorTripleString iterator = hdtDataSet.search(nextElementSuccessor, "", "");
                while (iterator.hasNext()){
                    TripleString ts = iterator.next();
                    CharSequence objectCharSequence = ts.getObject();
                    if (objectCharSequence.charAt(0) == '"') {
                        continue;
                    }
                    candidatesSuccessor.add(ts);
                }
            } catch (NotFoundException e) {
                e.printStackTrace();
            }


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
                    TripleString drawnTriple = Util.randomDrawFromSet(candidatesPredecessor);

                    // add walks from the front (walk started before entity)
                    result.addFirst(drawnTriple.getPredicate().toString());
                    result.addFirst(drawnTriple.getSubject().toString());
                    nextElementPredecessor = drawnTriple.getSubject().toString();
                }
            } else {
                // successor
                if (candidatesSuccessor != null && candidatesSuccessor.size() > 0) {
                    TripleString drawnTriple = Util.randomDrawFromSet(candidatesSuccessor);

                    // add next walk iteration
                    result.addLast(drawnTriple.getPredicate().toString());
                    result.addLast(drawnTriple.getObject().toString());
                    nextElementSuccessor = drawnTriple.getObject().toString();
                }
            }
        }
        return result;
    }
}
