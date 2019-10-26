package walkGenerators.alod.applications.alodRandomWalks.generationInMemory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import walkGenerators.alod.applications.alodRandomWalks.generationInMemory.model.StringFloat;
import walkGenerators.alod.applications.alodRandomWalks.generationInMemory.model.WalkGeneratorClassic;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * This class can generate reverse walks. It builds upon the optimized file written by the WalkGeneratorClassic.
 */
public class ReverseWalkGenerator implements WalkGeneratorClassic {

    private static Logger LOG  = LoggerFactory.getLogger(ReverseWalkGenerator.class);

    /**
     * true: generate only walks for concepts that do not appear as hyponym in the data set.
     * false: generate walks for all entitites that appear as hypernym.
     */
    boolean generateDeltaWalks = true;

    private HashMap<String, ArrayList<StringFloat>> narrowerConcepts = new HashMap<String, ArrayList<StringFloat>>(); // storage structure for concepts
    private HashSet<String> hyponyms = new HashSet<>(); // contains concepts that appear in the role of a hyponym
    private HashSet<String> hypernyms = new HashSet<>(); // contains concepts that appear in the role of a hypernym
    private OutputStreamWriter walkWriter;
    private String nameOfWalkFile;
    private long startTime; // classic.statistics
    private Random rand = new Random();
    private int processedEntities = 0;
    private long processedWalks = 0;
    private int newFileCounter = 0;
    private int fileNumber = 0;


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
                // add in reverse order
                if(!narrowerConcepts.containsKey(components[1])){
                    narrowerConcepts.put(components[1], new ArrayList());
                }
                narrowerConcepts.get(components[1]).add(new StringFloat(components[0], Float.parseFloat(components[2])));
                hyponyms.add(components[0]);
                hypernyms.add(components[1]);
            }
            LOG.info("Loading of all concepts completed.");
            if(generateDeltaWalks){
                LOG.info("Delta Walks activated. Removing hyponyms.");
                LOG.info("Size before: " + hypernyms.size());
                // remove hypernyms from the hypernym-set that are at the same time also hyponyms
                for(String c : hyponyms){
                    hypernyms.remove(c);
                }
                LOG.info("Size after: " + hypernyms.size());
            }
        } catch (FileNotFoundException fnfe){
            LOG.error("File could not be found. ABORT.");
            fnfe.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        LOG.info("Number of entities loaded: " + narrowerConcepts.size());
    }



    /**
     * This method assumes that the ALOD classic file has been loaded into memory.
     * @param walkOutputFileName Name of the files that contain the walks.
     * @param numberOfWalks The number of walks that shall be generated per entity.
     * @param depth The depth of each walk.
     * @param numberOfThreads The number of threads to be used.
     */
    public void generateWalks(String walkOutputFileName, int numberOfWalks, int depth, int numberOfThreads){
        // validity check
        if(narrowerConcepts == null || narrowerConcepts.size() < 5){
            LOG.error("No concepts seem to be loaded. ABORT.");
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
                        narrowerConcepts.size()));

        startTime = System.currentTimeMillis();
        for (String entity : hypernyms) {
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


    @Override
    public synchronized void writeWalksToFile(List<String> walks) {
        if(walks == null){
            return;
        }
        processedEntities++;
        processedWalks = processedWalks + walks.size();
        newFileCounter = newFileCounter + walks.size();
        for(String walk : walks){
            try {
                // reverse walk
                walkWriter.write(reverseWalk(walk) + "\n");
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

    @Override
    public String drawRandomConcept(String concept) {
        String result = "";
        Float greatestConfidence = 0f;
        float currentConfidence;
        ArrayList<StringFloat> narrower = narrowerConcepts.get(concept);
        if(narrower == null){
            return null;
        }
        for(StringFloat s : narrower){
            currentConfidence = rand.nextFloat() * s.floatValue;
            if(currentConfidence >= greatestConfidence){
                result = s.stringValue;
                greatestConfidence = currentConfidence;
            }
        }
        // System.out.println(concept + "  narrower  " + result);
        return result;
    }

    @Override
    public String drawConcept(String concept) {
        ArrayList<StringFloat> particularNarrowerConcepts = narrowerConcepts.get(concept);
        if(particularNarrowerConcepts == null){
            return null;
        }
        return particularNarrowerConcepts.get(rand.nextInt(particularNarrowerConcepts.size())).stringValue;
    }

    @Override
    public ArrayList<String> generateWalksForEntity(String concept, int numberOfWalks, int depth) {
        return null;
    }


    public static String reverseWalk(String walk){
        String[] walkComponents;
        walkComponents = walk.split(" ");
        String result = "";
        for(int i = walkComponents.length; i > 0 ; i--){
            result = result + " "  + walkComponents[i-1];
        }
        return result.trim();
    }



}
