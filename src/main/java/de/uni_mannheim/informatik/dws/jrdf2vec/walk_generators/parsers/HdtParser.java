package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A parser for HDT files.
 */
public class HdtParser implements IParser {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HdtParser.class);

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
    public HdtParser(String hdtFilePath) throws IOException {
        try {
            hdtDataSet = HDTManager.loadHDT(hdtFilePath);
        } catch (IOException e) {
            LOGGER.error("Failed to load HDT file: " + hdtFilePath + "\nProgramm will fail.", e);
            throw e;
        }
    }

    /**
     * Constructor
     *
     * @param hdtFile HDT file to be used.
     * @exception IOException IOException
     */
    public HdtParser(File hdtFile) throws IOException {
        this(hdtFile.getAbsolutePath());
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
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     * @param numberOfWalks The number of walks to be generated.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    public List<String> generateMidWalksForEntity(String entity, int numberOfWalks, int depth){
        return convertToStringWalks(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Given a list of walks where a walk is represented as a List of strings, this method will convert that
     * into a list of strings where a walk is one string (and the elements are separated by spaces).
     * @param dataStructureToConvert The data structure that shall be converted.
     * @return Data structure converted to string list.
     */
    public List<String> convertToStringWalks(List<List<String>> dataStructureToConvert) {
        List<String> result = new ArrayList<>();
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
    public List<List<String>> generateMidWalkForEntityAsArray(String entity, int depth, int numberOfWalks){
        List<List<String>>  result = new ArrayList<>();
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
                    IteratorTripleString iterator = hdtDataSet.search("", "", nextElementPredecessor);
                    HashSet<TripleString> candidates = new HashSet<>();

                    TripleString ts;
                    while (iterator.hasNext()) {
                        ts = iterator.next();
                        candidates.add(ts);
                    }

                    if (candidates.size() > 0) {
                        TripleString drawnTriple = randomDrawFromHashSet(candidates);

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
                        TripleString stringToAdd = randomDrawFromHashSet(candidates);

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
     * Draw a random value from a HashSet. This method is thread-safe.
     * @param setToDrawFrom The set from which shall be drawn.
     * @param <T> Type
     * @return Drawn value of type T.
     */
    public static <T> T randomDrawFromHashSet(HashSet<T> setToDrawFrom) {
        int randomNumber = ThreadLocalRandom.current().nextInt(setToDrawFrom.size());
        Iterator<T> iterator = setToDrawFrom.iterator();
        for (int i = 0; i < randomNumber; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Writes the given hdt data set as nt file.
     * @param dataSet Set to read.
     * @param fileToWrite File to write
     */
    public static void serializeDataSetAsNtFile(HDT dataSet, File fileToWrite) {
        try {
            IteratorTripleString iterator = dataSet.search("", "", "");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite), "UTF-8"));
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
        } catch (NotFoundException nfe) {
            LOGGER.error("Could not write file.", nfe);
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not write file.", e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not write file.", e);
        } catch (IOException e) {
            LOGGER.error("Could not write file.", e);
        }
    }
}
