package walkGenerators.classic.DBpedia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.base.DuplicateFreeWalkEntityProcessingRunnable;
import walkGenerators.base.NtMemoryParser;
import walkGenerators.base.WalkGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class DBpediaWalkGenerator extends WalkGenerator {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaWalkGenerator.class);


    private String labelsFilePath = "";


    /**
     * Constructor
     *
     * @param pathToDirectory Path to unzipped (!) DBpedia 2016-10 files.
     */
    public DBpediaWalkGenerator(String pathToDirectory){
        File resourcesDirectory = new File(pathToDirectory);
        if(!resourcesDirectory.isDirectory()){
            LOGGER.error("Resources directory is not a directory. ABORTING.");
            return;
        }
        this.parser = new NtMemoryParser(this);

        for(File f : resourcesDirectory.listFiles()){
            // labels file handling
            if(f.getName().contains("labels_en")){
                LOGGER.info("Detected labels file: " + f.getName());
                try {
                    labelsFilePath = f.getCanonicalPath();
                    continue;
                } catch (IOException e) {
                    LOGGER.error("Could not determine canonical file path for labels file.", e);
                    e.printStackTrace();
                    continue;
                }
            }
            // others:
            //try {
            //    this.parser.readNTriples(f.getCanonicalPath());
            //} catch (IOException e) {
            //    LOGGER.error("Could not determine canonical file path for file " + f.getName(), e);
            //    e.printStackTrace();
            //}
        }
        ((NtMemoryParser)this.parser).readNtTriplesFromDirectoryMultiThreaded(pathToDirectory, true);
    }



    /**
     * Obtain the entities in this case: wikipedia instances.
     *
     * @return Entities as String.
     */
    private HashSet<String> getEntities(String dbpediaLabelsFilePath) {

        // option 1: look for cached file
        String cacheFilePath = "./cache/dbpedia_entities.txt";
        File cachedFile = new File(cacheFilePath);
        if (cachedFile.exists()) {
            LOGGER.info("Cached file found. Obtaining entities from cache.");
            return readHashSetFromFile(cachedFile);
        }

        // option 2: go to labels file and parse from there
        File labelsFile = new File(dbpediaLabelsFilePath);
        if (!labelsFile.exists()) {
            LOGGER.error("Labels file not found.");
            return new HashSet<>();
        }
        cachedFile.getParentFile().mkdirs();

        // parsing
        HashSet<String> result = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(labelsFile), StandardCharsets.UTF_8));
            String readLine;
            while((readLine = reader.readLine()) != null){
                if (readLine.startsWith("#")) continue; // just a comment
                String[] tokens = readLine.split(" ");
                result.add(shortenUri(removeTags(tokens[0])));
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Problem with reader.", e);
            e.printStackTrace();
            return result;
        } catch (IOException e) {
            LOGGER.error("Problem with reader.", e);
            e.printStackTrace();
            return result;
        }

        writeHashSetToFile(cacheFilePath, result);
        return result;
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        // not implemented for DBpedia: trigger duplicate free walks
        LOGGER.warn("Random walks with duplicates not implemented for DBpedia. Generating duplicate free walks.");
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        // not implemented for DBpedia: trigger duplicate free walks
        LOGGER.warn("Random walks with duplicates not implemented for DBpedia. Generating duplicate free walks.");
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, filePathOfFileToBeWritten);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        File outputFile = new File(filePath);
        if(outputFile.isDirectory()){
            LOGGER.error("Please specify a file for the walks to be written, not a directory. ABORTING.");
            return;
        }
        outputFile.getParentFile().mkdirs();

        // obtain the entities
        HashSet<String> entities = getEntities(this.labelsFilePath);

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), "utf-8");
        } catch (Exception e1) {
            LOGGER.error("Could not initialize writer. Aborting process.", e1);
            return;
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            DuplicateFreeWalkEntityProcessingRunnable th = new DuplicateFreeWalkEntityProcessingRunnable(this, entity, numberOfWalksPerEntity, depth);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        this.close();
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/dbpedia_walks_df_" + numberOfWalksPerEntity + "_" + depth + "_.gz");
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        LOGGER.error("Not implemented.");
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        LOGGER.error("Not implemented.");
    }

    @Override
    public String shortenUri(String uri) {
        return uri.replace("http://dbpedia.org/resource/", "dbr:");
    }

}
