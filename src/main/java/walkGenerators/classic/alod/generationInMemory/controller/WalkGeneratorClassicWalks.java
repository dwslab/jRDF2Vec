package walkGenerators.classic.alod.generationInMemory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import walkGenerators.base.IWalkGenerator;
import walkGenerators.base.WalkGenerationMode;
import walkGenerators.classic.alod.generationInMemory.model.StringFloat;
import walkGenerators.classic.alod.generationInMemory.model.WalkGeneratorClassic;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * This class can read the ALOD classic n-quad file and generate paths.
 */
public class WalkGeneratorClassicWalks implements WalkGeneratorClassic, IWalkGenerator {

    private static Logger LOG = LoggerFactory.getLogger(WalkGeneratorClassicWalks.class);
    private HashMap<String, ArrayList<StringFloat>> broaderConcepts = new HashMap<String, ArrayList<StringFloat>>(); // storage structure for concepts
    private OutputStreamWriter walkWriter;
    private String nameOfWalkFile;
    private int fileNumber = 0; // used to decide when to start a new file
    private static URLDecoder urlDecoder = new URLDecoder();

    // classic.statistics
    private long startTime;
    private int processedEntities = 0;
    private int processedWalks = 0;
    private int newFileCounter = 0;

    private static final Logger LOGGER = LoggerFactory.getLogger(WalkGeneratorClassicWalks.class);

    /**
     * Generates random walks without duplicates.
     * @param numberOfWalks Number of walks.
     * @param depth Depth of the walks.
     * @param numberOfThreads Number of threads.
     */
    public void generateRandomWalksDuplicateFree(int numberOfWalks, int depth, int numberOfThreads) {
        String walkOutputFileName = "./walks/alod_classic_walks_df_" + numberOfWalks + "_" + depth + "_.gz";
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, walkOutputFileName);
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        LOGGER.error("Not implemented.");
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        LOGGER.error("Not implemented.");
    }

    /**
     * Generates random walks without duplicates.
     *
     * @param numberOfWalks Number of walks.
     * @param depth Depth of the walks.
     * @param numberOfThreads Number of threads.
     * @param walkOutputFileName The name of the walk file that shall be written.
     */
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalks, int depth, String walkOutputFileName){
        // validity check
        if(broaderConcepts == null || broaderConcepts.size() < 5){
            LOG.error("No broader concepts seem to be loaded. ABORT.");
            return;
        }

        // create directory if it does not exist
        File fileToWrite = new File(walkOutputFileName);
        fileToWrite.getParentFile().mkdirs();

        // initialize the writer
        try {
            walkWriter = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(walkOutputFileName, false)), "utf-8");
        } catch (Exception e1) {
            LOG.error("Could not initialize writer.");
            e1.printStackTrace();
        }

        nameOfWalkFile = walkOutputFileName;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<Runnable>(
                        broaderConcepts.size()));

        startTime = System.currentTimeMillis();
        for (String entity : broaderConcepts.keySet()) {
            DuplicateFreeWalkEntityProcessingRunnable th = new DuplicateFreeWalkEntityProcessingRunnable(this, entity, numberOfWalks, depth);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOG.error("Interrupted Exception");
            e.printStackTrace();
        }
        try {
            walkWriter.flush();
            walkWriter.close();
        } catch (IOException e) {
            LOG.error("IO Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void generateWalks(WalkGenerationMode generationMode, int numberOfThreads, int numberOfWalks, int depth, String walkFile) {

    }

    /**
     * Generates random walks.
     * @param numberOfThreads Number of threads.
     * @param numberOfWalks Number of walks.
     * @param depth Depth of each walk.
     */
    public void generateRandomWalks(int numberOfThreads, int numberOfWalks, int depth) {
        String walkOutputFileName = "./walks/alod_classic_walks_with_duplicates_" + numberOfWalks + "_" + depth + "_.gz";
        generateRandomWalks(numberOfThreads, numberOfWalks, depth, walkOutputFileName);
    }

    /**
     * This method assumes that the ALOD classic file has been loaded into memory.
     * @param walkOutputFileName Name of the files that contain the walks.
     * @param numberOfWalks The number of walks that shall be generated per entity.
     * @param depth The depth of each walk.
     * @param numberOfThreads The number of threads to be used.
     */
    public void generateRandomWalks(int numberOfThreads, int numberOfWalks, int depth, String walkOutputFileName){
        // validity check
        if(broaderConcepts == null || broaderConcepts.size() < 5){
            LOG.error("No broader concepts seem to be loaded. ABORT.");
            return;
        }

        // intialize the writer
        try {
            walkWriter = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(walkOutputFileName, false)), "utf-8");
        } catch (Exception e1) {
            LOG.error("Could not initialize writer.");
            e1.printStackTrace();
        }

        nameOfWalkFile = walkOutputFileName;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<Runnable>(
                        broaderConcepts.size()));

        startTime = System.currentTimeMillis();
        for (String entity : broaderConcepts.keySet()) {
            EntityProcessingThreadClassic th = new EntityProcessingThreadClassic(this, entity, numberOfWalks, depth);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOG.error("Interrupted Exception");
            e.printStackTrace();
        }
        try {
            walkWriter.flush();
            walkWriter.close();
        } catch (IOException e) {
            LOG.error("IO Exception");
            e.printStackTrace();
        }
    }


    /**
     * This method will write all concepts to a file. One per line.
     * @param pathToOptimizedFile Path to the optimized file to read from.
     * @param pathToFileToWrite Path to the file to write.
     */
    public void saveEntitiesToFile(String pathToOptimizedFile, String pathToFileToWrite){
        EntityExtractor.writeConceptsToFile(pathToOptimizedFile, pathToFileToWrite);
    }


    /**
     * Will write the hyponyms to a file, one at a line.
     * @param pathToFileToWrite Path to the file that shall be written.
     */
    public void saveHyponymsToFile(String pathToFileToWrite){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathToFileToWrite)));
            Iterator iterator = broaderConcepts.entrySet().iterator();
            while(iterator.hasNext()){
                HashMap.Entry entry = (HashMap.Entry) iterator.next();
                writer.write(entry.getKey().toString()+"\n");
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }


    /**
     * Load from a storage optimized file that was previously written by method {@code loadFromNquadsFile}.
     * @see WalkGeneratorClassicWalks#loadFromNquadsFile(String, String)
     * @param pathToFile Path to the file that shall be read.
     */
    public void loadFromOptimizedFile(String pathToFile){
        LOG.info("Loading optimized file.");
        File f = new File(pathToFile);
        try {
            GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(f));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIn));
            String readLine = "";
            while((readLine = reader.readLine()) != null){
                String[] components = readLine.split("\t");
                // components has 3 elements:
                // 0) Concept 1
                // 1) Hypernym of Concept 1
                // 2) Confidence of Hypernymy relation
                if(!broaderConcepts.containsKey(components[0])){
                    broaderConcepts.put(components[0], new ArrayList());
                }
                broaderConcepts.get(components[0]).add(new StringFloat(components[1], Float.parseFloat(components[2])));
            }
            LOG.info("Load completed.");
        } catch (FileNotFoundException fnfe){
            LOG.error("File could not be found. ABORT.");
            fnfe.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        LOG.info("Number of entities loaded: " + broaderConcepts.size());
    }


    /**
     * Load and determine the kind of load. Note: If you use the optimized file, you have to add optimized to the name.
     * @param pathToFile The path to the file.
     */
    public void load(String pathToFile){
        File file = new File(pathToFile);
        if(!file.exists()){
            LOG.error("File does not exist. ABORT");
            return;
        }
        if(file.getName().contains("optimized")){
            loadFromOptimizedFile(pathToFile);
        } else {
            loadFromNquadsFile(pathToFile, "./alod_optimized.gz");
        }
    }

    /**
     * Load ALOD Classic nquad file. A new, storage-optimized file is written for later use.
     * @param pathToInstanceFile Path to the gzipped n-quads file.
     * @param pathToOutputFile Path to the file to be written.
     */
    public void loadFromNquadsFile(String pathToInstanceFile, String pathToOutputFile) {
        HashMap<Integer, Float> provConf = new HashMap<Integer, Float>();
        GZIPInputStream gzipInput;
        GZIPOutputStream gzipOutput;
        BufferedReader reader;
        BufferedWriter writer;
        try {
            gzipInput = new GZIPInputStream(new FileInputStream(pathToInstanceFile));
            reader = new BufferedReader(new InputStreamReader(gzipInput));
            gzipOutput = new GZIPOutputStream(new FileOutputStream(new File(pathToOutputFile)));
            writer = new BufferedWriter(new OutputStreamWriter(gzipOutput));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("ABORT");
            return;
        } catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("ABORT");
            return;
        }

        //------------------------------------------------------------------------------------------------
        // first iteration: get provenance ID and confidence
        //------------------------------------------------------------------------------------------------

        System.out.println("First Pass Over Data");

        // pattern for prov ID
        String provIDregexPattern = "(?<=\\/prov\\/)[0-9]*(?=>)"; // (?<=\/prov\/)[0-9]*(?=>)
        Pattern provIDpattern = Pattern.compile(provIDregexPattern);

        
        String confRegexPattern = "(?<=#hasConfidence> \\\").*(?=\\\"\\^\\^<)"; // (?<=#hasConfidence> \").*(?=\"\^\^<)
        Pattern confPattern = Pattern.compile(confRegexPattern);

        String readLine;
        Integer provID;
        Float confidence;
        long lineNumber = 0;
        try {
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("ontology#hasConfidence")) {
                    Matcher provIDmatcher = provIDpattern.matcher(readLine);
                    provIDmatcher.find();
                    Matcher confMatcher = confPattern.matcher(readLine);
                    confMatcher.find();
                    provID = Integer.parseInt(provIDmatcher.group());
                    confidence = Float.parseFloat(confMatcher.group());
                    // System.out.println(provID.toString() + "   " + confidence.toString());
                    provConf.put(provID, confidence);
                }
                lineNumber++;
                if(lineNumber % 1000000 == 0){
                    System.out.println(lineNumber);
                }
            }
            System.out.println("Confidence loaded.");

            
         //------------------------------------------------------------------------------------------------
         // second iteration: get broader concepts
         //------------------------------------------------------------------------------------------------

            System.out.println("Second Pass Over Data");
            
            // reinitialize reader
            gzipInput = new GZIPInputStream(new FileInputStream(pathToInstanceFile));
            reader = new BufferedReader(new InputStreamReader(gzipInput));

            String conceptRegexPattern = "(?<=\\/concept\\/).*?(?=> )"; // (?<=\/concept\/).*?(?=> )
            Pattern conceptPattern = Pattern.compile(conceptRegexPattern);
            String concept1;
            String concept2;
            lineNumber = 0;
            while((readLine = reader.readLine()) != null){
                if(readLine.contains("core#broader>")){
                    //System.out.println("\nProcessing Sentence:\n" + readLine);
                    Matcher conceptMatcher = conceptPattern.matcher(readLine);
                    conceptMatcher.find();
                    concept1 = cleanConcept(conceptMatcher.group());
                    conceptMatcher.find();
                    concept2 = cleanConcept(conceptMatcher.group());

                    // get prov ID
                    Matcher provIDmatcher = provIDpattern.matcher(readLine);
                    provIDmatcher.find();

                    provID = Integer.parseInt(provIDmatcher.group());
                    Float confidenceOfHypernym = provConf.get(provID);

                    // save memory:
                    provConf.remove(provID);

                    if(broaderConcepts.get(concept1) == null) {
                        broaderConcepts.put(concept1, new ArrayList<StringFloat>());
                    }
                    broaderConcepts.get(concept1).add(new StringFloat(concept2, confidenceOfHypernym));
                    writer.write(concept1 + "\t" + concept2 + "\t" + confidenceOfHypernym + "\n");
                    //System.out.println(concept1 + "\t" + concept2 + "\t" + confidenceOfHypernym);
                    lineNumber++;
                    if(lineNumber % 1000000 == 0){
                        System.out.println(lineNumber);
                    }
                }
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
            return;
        } finally {
            try {
                reader.close();
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } //(main method)


    /**
     * Return a random broader concept of the given concept.
     * @param concept Concept for which a hypernym shall be found.
     * @return Retrieved hypernym.
     */
    public String drawConcept(String concept){
        ArrayList<StringFloat> particularBroaderConcepts = broaderConcepts.get(concept);
        if(particularBroaderConcepts == null){
            return null;
        }
        int randomNumber = ThreadLocalRandom.current().nextInt(particularBroaderConcepts.size());
        return particularBroaderConcepts.get(randomNumber).stringValue;
    }


    public synchronized void writeWalksToFile(List<String> walks){
        if(walks == null){
            return;
        }
        processedEntities++;
        processedWalks = processedWalks + walks.size();
        newFileCounter = newFileCounter + walks.size();
        for(String walk : walks){
            try {
                walkWriter.write(walk + "\n");
            } catch(IOException ioe){
                ioe.printStackTrace();
            }
        }

        // just output:
        if(processedEntities % 100 == 0){
            System.out.println("TOTAL PROCESSED ENTITIES: " + processedEntities);
            System.out.println("TOTAL NUMBER OF WALKS: " + processedWalks);
            System.out.println("TIME: " + ((System.currentTimeMillis() - startTime) / 1000));
        }

        // file flushing
        if(newFileCounter > 3000000){
            newFileCounter = 0;
            try {
                walkWriter.flush();
                walkWriter.close();
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
            try {
                fileNumber++;
                walkWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(nameOfWalkFile + "_" + fileNumber)));
            } catch (IOException ioe){
                 LOG.error("ERROR while reinstantiating writer.");
                ioe.printStackTrace();
            }
        }
    }


    /**
     *  Returns a random broader concept of the given concept with a higher probability of high-confidence concepts
     *  to be drawn.
     * @param concept The concept for which the hypernym shall be retrieved.
     * @return The random broader concept.
     */
    public String drawRandomConcept(String concept){
        String result = "";
        Float greatestConfidence = 0f;
        float currentConfidence = 0f;
        ArrayList<StringFloat> broader = broaderConcepts.get(concept);
        if(broader == null){
            return null;
        }
        for(StringFloat s : broader){
            currentConfidence = ThreadLocalRandom.current().nextFloat() * s.floatValue;
            if(currentConfidence >= greatestConfidence){
                result = s.stringValue;
                greatestConfidence = currentConfidence;
            }
        }
        return result;
    }


    /**
     * Remove special characters from a concept in order to:
     * - be more space efficient
     * - allow for easier lookup
     * @param conceptToClean That concept that is to be cleaned/edited.
     * @return The edited/cleaned concepts.
     */
    public static String cleanConcept(String conceptToClean){

        // handling URL specific encodings like '%20' for space (' ') or '%26' for '&'.
        try {
            conceptToClean = urlDecoder.decode(conceptToClean, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Decoding URL (" + conceptToClean + ") failed. Program will continue.");
            e.printStackTrace();
        }
        conceptToClean = conceptToClean
                .replace(" ", "_")
                .replace("\t", "_")
                .replace("+", "_");

        if(conceptToClean.startsWith("_")){
            conceptToClean = conceptToClean.substring(1, conceptToClean.length());
        }
        if(conceptToClean.endsWith("_")){
            conceptToClean = conceptToClean.substring(0, conceptToClean.length()-1);
        }
        return conceptToClean.trim();
    }


    /**
     * Generates duplication free walks for the given entitiy.
     * @param entity The entity for which walks shall be generated.
     * @param numberOfWalks The number of walks to be generated.
     * @param depth The number of hops to nodes (!).
     * @return A list of walks.
     */
    public ArrayList<String> generateWalksForEntity(String entity, int numberOfWalks, int depth){
        ArrayList<String> result = new ArrayList<>();
        ArrayList<List<String>> walks = new ArrayList();
        boolean isFirstIteration = true;
        for(int currentDepth = 0; currentDepth < depth; currentDepth++){
            // initialize with first node
            if(isFirstIteration) {
                for(StringFloat entry :  broaderConcepts.get(entity)){
                    ArrayList<String> firstWalk = new ArrayList<>();
                    firstWalk.add(entry.stringValue);
                    walks.add(firstWalk);
                }
                if(walks.size() == 0){
                    return result;
                }
                isFirstIteration = false;
            }

            // create a copy
            List<List<String>> walks_tmp = new ArrayList<>();
            walks_tmp.addAll(walks);

            // loop over current walks
            for(List<String> walk : walks_tmp){
                // get last entity
                String lastEntity = walk.get(walk.size()-1);

                if(broaderConcepts.get(lastEntity) == null) continue;

                List<StringFloat> nextIterationWithFloat = new ArrayList<>(broaderConcepts.get(lastEntity));
                if(nextIterationWithFloat != null && nextIterationWithFloat.size() > 0) {

                    while(nextIterationWithFloat.size() > numberOfWalks){
                        int randomNumber = ThreadLocalRandom.current().nextInt(nextIterationWithFloat.size());
                        nextIterationWithFloat.remove(randomNumber);
                    }

                    List<String> nextIteration = new ArrayList<>();
                    for (StringFloat entry : nextIterationWithFloat) {
                        if (entry != null) {
                            nextIteration.add(entry.stringValue);
                        }
                    }

                    if (nextIteration != null) {
                        walks.remove(walk);
                        for (String nextStep : nextIteration) {
                            List<String> newWalk = new ArrayList<>(walk);
                            newWalk.add(nextStep);
                            walks.add(newWalk);
                        }
                    }
                }
            } // loop over walks

            // trim the list
            while(walks.size() > numberOfWalks){
                int randomNumber = ThreadLocalRandom.current().nextInt(walks.size());
                walks.remove(randomNumber);
            }
        } // depth loop

        // now we need to translate our walks into strings
        for(List<String> walk : walks){
            String finalSentence = entity;
            for(String node : walk){
                finalSentence += " " + node;
            }
            result.add(finalSentence);
        }
        return result;
    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        LOGGER.error("Not implemented.");
    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        LOGGER.error("Not implemented.");
    }



}
