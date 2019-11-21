package walkGenerators;

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.IsearchCondition;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A parser for NT files. Mainly implemented to support {@link NtParser#getRandomPredicateObjectForSubject(String)} in
 * an efficient way.
 */
public class NtParser {

    /**
     * the actual data structure
     */
    private Map<String, ArrayList<PredicateObject>> data;

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NtParser.class);

    /**
     * Walk generator that uses the parser.
     */
    private WalkGenerator specificWalkGenerator;

    /**
     * returns true if a triple shall be excluded.
     */
    private IsearchCondition skipCondition;

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
     * Indicator whether an optimized file shall be written for quick parsing later on (will be written in ./optmized/)
     */
    boolean isWriteOptimizedFile = false;

    /**
     * Default Constructor
     */
    public NtParser(WalkGenerator walkGenerator) {
        data = new ConcurrentHashMap<>(1000000000); // one billion is reasonable for babelnet

        specificWalkGenerator = walkGenerator;
        skipCondition = new IsearchCondition() {
            Pattern pattern = Pattern.compile("\".*\"");

            @Override
            public boolean isHit(String input) {
                if (input.trim().startsWith("#")) return true; // just a comment line
                if (input.trim().equals("")) return true; // empty line
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) return true;
                return false;
            }
        };
    }


    /**
     * Constructor
     *
     * @param pathToTripleFile The nt file to be read (not zipped).
     */
    public NtParser(String pathToTripleFile, WalkGenerator walkGenerator) {
        this(walkGenerator);
        readNTriples(pathToTripleFile);
    }


    /**
     * Save an ontModel as TTL file.
     *
     * @param ontModel              Model to Write.
     * @param filePathToFileToWrite File that shall be written.
     */
    public static void saveAsNt(OntModel ontModel, String filePathToFileToWrite) {
        try {
            ontModel.write(new FileWriter(new File(filePathToFileToWrite)), "N-Triples");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Read the given nt file into memory.
     *
     * @param pathToFile Path to the file.
     */
    public void readNTriples(String pathToFile) {
        readNTriples(pathToFile, false);
    }


    /**
     * Will load all .nt and .gz files from the given directory.
     *
     * @param pathToDirectory The directory where the n-triple files reside.
     */
    public void readNTriplesFilesFromDirectory(String pathToDirectory) {
        File directoryOfDataSets = new File(pathToDirectory);
        if (!directoryOfDataSets.isDirectory()) {
            LOGGER.error("The given pathToDirectory is no directory, aborting. (given: " + pathToDirectory + ")");
            return;
        }
        for (File file : directoryOfDataSets.listFiles()) {
            LOGGER.info("Processing file " + file.getName());
            if (file.getName().endsWith(".gz")) {
                readNTriples(file, true);
            } else if (file.getName().endsWith(".nt") || file.getName().endsWith(".ttl")) {
                readNTriples(file, false);
            } else {
                LOGGER.info("Skipping file: " + file.getName());
                continue;
            }
        }
    }


    /**
     * A new thread will be opened for each file.
     *
     * @param pathToDirectory
     */
    public void readNtTriplesFromDirectoryMultiThreaded(String pathToDirectory, boolean isWriteOptimizedFile) {
        this.isWriteOptimizedFile = isWriteOptimizedFile;
        File directoryOfDataSets = new File(pathToDirectory);
        if (!directoryOfDataSets.isDirectory()) {
            LOGGER.error("The given pathToDirectory is no directory, aborting. (given: " + pathToDirectory + ")");
            return;
        }

        HashMap<String, File> optimizedFiles = new HashMap<>();
        // check for optimized files
        File optimizedDirectory = new File("./optimized");
        if (optimizedDirectory.exists() && optimizedDirectory.isDirectory()) {
            LOGGER.info("Found optimized directory. Will use it for reading.");
            for (File optimizedFile : optimizedDirectory.listFiles()) {
                optimizedFiles.put(optimizedFile.getName(), optimizedFile);
            }
        }

        ArrayList<Thread> allThreads = new ArrayList<>();
        for (File fileOriginal : directoryOfDataSets.listFiles()) {
            if(optimizedFiles.containsKey(fileOriginal.getName())){
                LOGGER.info("Found optimized file for " + fileOriginal.getName() + ", will use that one.");
                FileReaderThread zThread = new FileReaderThread(this, optimizedFiles.get(fileOriginal.getName()), true, true);
                zThread.start();
                allThreads.add(zThread);
            } else if (fileOriginal.getName().endsWith(".gz")) {
                FileReaderThread zThread = new FileReaderThread(this, fileOriginal, true, false);
                zThread.start();
                allThreads.add(zThread);
            } else if (fileOriginal.getName().endsWith(".nt") || fileOriginal.getName().endsWith(".ttl")) {
                FileReaderThread zThread = new FileReaderThread(this, fileOriginal, false, false);
                zThread.start();
                allThreads.add(zThread);
            } else {
                LOGGER.info("Skipping file: " + fileOriginal.getName());
                continue;
            }
        }

        // wait for thread completion
        try {
            for (Thread thread : allThreads) {
                thread.join();
            }
        } catch (InterruptedException ie) {
            LOGGER.error("Problem waiting for thread...", ie);
        }
        LOGGER.info("Data read.");
    }


    /**
     * Simple Thread
     */
    class FileReaderThread extends Thread {

        public FileReaderThread(NtParser parser, File fileToRead, boolean gzipped, boolean optimized) {
            this.fileToRead = fileToRead;
            this.parser = parser;
            this.isGzipped = gzipped;
            this.isOptimizedFile = optimized;
        }

        private boolean isOptimizedFile;
        private NtParser parser;
        private File fileToRead;
        private boolean isGzipped;

        @Override
        public void run() {
            if(!isOptimizedFile) {
                LOGGER.info("STARTED thread for file " + fileToRead.getName());
                parser.readNTriples(fileToRead, isGzipped);
            } else {
                LOGGER.info("STARTED (optimized) thread for file " + fileToRead.getName());
                parser.readNTriplesOptimized(fileToRead);
            }
            LOGGER.info("Thread for file " + fileToRead.getName() + " completed.");
        }
    }


    /**
     * Read the given nt file into memory. This method will add the data in the file to the existing {@link NtParser#data} store.
     *
     * @param pathToFile    Path to the file.
     * @param isGzippedFile Indicator whether the given file is gzipped.
     */
    public void readNTriples(String pathToFile, boolean isGzippedFile) {
        File fileToReadFrom = new File(pathToFile);
        readNTriples(fileToReadFrom, isGzippedFile);
    }


    public void readNTriplesOptimized(File fileToReadFrom) {
        if (!fileToReadFrom.exists()) {
            LOGGER.error("File does not exist. Cannot parse.");
            return;
        }
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileToReadFrom));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));

            String readLine;
            int lineNumber = 0;
            while((readLine = reader.readLine()) != null){
                lineNumber += 1;
                String[] parsed = readLine.split(" ");
                if(parsed.length != 3){
                    LOGGER.error("Problem with line: \n" + readLine);
                } else {
                    String subject = parsed[0];
                    String predicate = parsed[1];
                    String object = parsed[2];
                    addToDataThreadSafe(subject, predicate, object);
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Could not initialize optimized reader for file " + fileToReadFrom.getName());
        }
    }


    /**
     * Add data in a thread safe way.
     * @param subject The subject to be added.
     * @param predicate The predicate to be added.
     * @param object The object to be added.
     */
    private synchronized void addToDataThreadSafe(String subject, String predicate, String object){
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
     * Read the given nt file into memory. This method will add the data in the file to the existing {@link NtParser#data} store.
     *
     * @param fileToReadFrom the file.
     * @param isGzippedFile  Indicator whether the given file is gzipped.
     */
    public void readNTriples(File fileToReadFrom, boolean isGzippedFile) {
        if (!fileToReadFrom.exists()) {
            LOGGER.error("File does not exist. Cannot parse.");
            return;
        }

        BufferedWriter writer = null; // the writer used to write the optimized file
        if (isWriteOptimizedFile) {
            try {
                File fileToWrite = new File("./optimized/" + fileToReadFrom.getName());
                fileToWrite.getParentFile().mkdirs();
                GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(fileToWrite));
                writer = new BufferedWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8));
                LOGGER.info("Writer initialized.");
            } catch (FileNotFoundException fnfe) {
                LOGGER.error("Could not initialize gzip output stream.", fnfe);
            } catch (IOException e) {
                LOGGER.error("Problem initializing gzip output stream.", e);
            }
        }

        Pattern datatypePattern = Pattern.compile("\".*");
        try {
            BufferedReader reader;
            if (isGzippedFile) {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fileToReadFrom));
                reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToReadFrom), StandardCharsets.UTF_8));
            }
            String readLine;
            long lineNumber = 0;
            Matcher datatypeMatcher; // only required if datatype properties shall be included
            nextLine:
            while ((readLine = reader.readLine()) != null) {
                try {
                    lineNumber++;
                    if (skipCondition.isHit(readLine)) {
                        continue nextLine;
                    }

                    // remove the dot at the end of a statement
                    readLine = readLine.replaceAll("(?<=>)*[ ]*.[ ]*$", "");


                    if (isIncludeDatatypeProperties) {
                        datatypeMatcher = datatypePattern.matcher(readLine);
                        if (datatypeMatcher.find()) {
                            String datatypeValue = datatypeMatcher.group(0);
                            String newDatatypeValue = datatypeValue.replaceAll(" ", "_");
                            readLine = readLine.replace(datatypeValue, newDatatypeValue);
                        }
                    }

                    String[] spo = readLine.split(" ");
                    if (spo.length != 3) {
                        LOGGER.error("Error in file " + fileToReadFrom.getName() + " in line " + lineNumber + " while parsing the following line:\n" + readLine + "\n Required tokens: 3\nActual tokens: " + spo.length);
                        int i = 1;
                        for (String token : spo) {
                            LOGGER.error("Token " + i++ + ": " + token);
                        }
                        continue nextLine;
                    }
                    String subject = specificWalkGenerator.shortenUri(removeTags(spo[0])).intern();
                    String predicate = specificWalkGenerator.shortenUri(removeTags(spo[1]).intern());
                    String object = specificWalkGenerator.shortenUri(removeTags(spo[2])).intern();

                    addToDataThreadSafe(subject, predicate, object);

                    if (isWriteOptimizedFile) {
                        writer.write(subject + " " + predicate + " " + object + "\n");
                    }

                } catch (Exception e) {
                    // it is important that the parsing continues no matter what happens
                    LOGGER.error("A problem occurred while parsing line number " + lineNumber + " of file " + fileToReadFrom.getName(), e);
                    LOGGER.error("The problem occured in the following line:\n" + readLine);
                }
            } // end of while loop
            LOGGER.info("File " + fileToReadFrom.getName() + " successfully read. " + data.size() + " subjects loaded.");
            if(isWriteOptimizedFile) {
                writer.flush();
                writer.close();
            }
            reader.close();
        } catch (Exception e) {
            LOGGER.error("Error while parsing file.", e);
        }
    }


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
    public List<String> generateWalksForEntity(String entity, int numberOfWalks, int depth) {
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
            if(this.isUnifiyAnonymousNodes()){
                for (PredicateObject po : walk) {
                    String object = po.object;
                    if(isAnonymousNode(object)){
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
     * Faster version of {@link NtParser#getRandomPredicateObjectForSubject(String)}.
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

    public WalkGenerator getSpecificWalkGenerator() {
        return specificWalkGenerator;
    }

    public void setSpecificWalkGenerator(WalkGenerator specificWalkGenerator) {
        this.specificWalkGenerator = specificWalkGenerator;
    }

    public IsearchCondition getSkipCondition() {
        return skipCondition;
    }

    public void setSkipCondition(IsearchCondition skipCondition) {
        this.skipCondition = skipCondition;
    }

    /**
     * This method will remove a leading less-than and a trailing greater-than sign (tags).
     *
     * @param stringToBeEdited The string that is to be edited.
     * @return String without tags.
     */
    static String removeTags(String stringToBeEdited) {
        if (stringToBeEdited.startsWith("<")) stringToBeEdited = stringToBeEdited.substring(1);
        if (stringToBeEdited.endsWith(">"))
            stringToBeEdited = stringToBeEdited.substring(0, stringToBeEdited.length() - 1);
        return stringToBeEdited;
    }

    public boolean isIncludeDatatypeProperties() {
        return isIncludeDatatypeProperties;
    }

    /**
     * Returns true if the given parameter follows the schema of an anonymous node
     * @param uriString The URI string to be checked.
     * @return True if anonymous node.
     */
    public boolean isAnonymousNode (String uriString){
        uriString = uriString.trim();
        if(uriString.startsWith("_:genid")){
            return true;
        } else return false;
    }

    public boolean isUnifiyAnonymousNodes() {
        return isUnifiyAnonymousNodes;
    }

    public void setUnifiyAnonymousNodes(boolean unifiyAnonymousNodes) {
        isUnifiyAnonymousNodes = unifiyAnonymousNodes;
    }

    /**
     * Note that this function will overwrite the skip condition.
     *
     * @param includeDatatypeProperties
     */
    public void setIncludeDatatypeProperties(boolean includeDatatypeProperties) {
        LOGGER.warn("Overwriting skip condition.");
        skipCondition = new IsearchCondition() {
            @Override
            public boolean isHit(String input) {
                if (input.trim().startsWith("#")) return true; // just a comment line
                if (input.trim().equals("")) return true; // empty line
                return false;
            }
        };
        isIncludeDatatypeProperties = includeDatatypeProperties;
    }
}
