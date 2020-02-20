package walkGenerators.classic.DBnary;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.base.NtMemoryParser;
import walkGenerators.base.RandomWalkEntityProcessingRunnable;
import walkGenerators.base.WalkGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * Walk generator for Wiktionary / the DBnary data set.
 */
public class DbnaryWalkGenerator extends WalkGenerator {


    public static void main(String[] args) {
        DbnaryWalkGenerator generator = new DbnaryWalkGenerator("./dbnary_eng.nt");
        generator.generateRandomWalks(50, 500, 8, "./walks/dbnary_500_8_pages_df/dbnary_500_8_pages_df.gz");
    }

    /**
     * Default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(DbnaryWalkGenerator.class);

    /**
     * Central OntModel
     */
    private OntModel model;


    /**
     * Constructor
     * @param pathToTripleFile The path to the NT file.
     */
    public DbnaryWalkGenerator(String pathToTripleFile) {
        File file = new File(pathToTripleFile);
        if(file.isDirectory()){
            LOGGER.error("You specified a directory, but a file needs to be specified as resource file. ABORT.");
            return;
        }
        if(!file.exists()){
            LOGGER.error("The resource file you specified does not exist. ABORT.");
            return;
        }
        try {
            this.model = readOntology(pathToTripleFile, "NT");
            this.parser = new NtMemoryParser(pathToTripleFile, this);
            LOGGER.info("Model read into memory.");
        } catch (MalformedURLException mue) {
            LOGGER.error("Path seems to be invalid. Generator not functional.", mue);
        }
    }


    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalks(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/dbnary_walks.gz");
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateWalksForEntities(getEntities(this.model), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateDuplicateFreeWalksForEntities(getEntities(this.model), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/dbnary_walks.gz");
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
     * Generate walks for the entities.
     *
     * @param entities The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads involved in generating the walks.
     * @param numberOfWalks The number of walks to be generated per entity.
     * @param walkLength The length of each walk.
     */
    public void generateWalksForEntities(HashSet<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

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
            RandomWalkEntityProcessingRunnable th = new RandomWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
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

    /**
     * Obtain the entities in this case: Lexical Entry instances.
     * This method will create a cache.
     *
     * @return Entities as String.
     */
    private HashSet<String> getEntities(OntModel model) {
        File file = new File("./cache/dbnary_entities.txt");
        if (file.exists()) {
            LOGGER.info("Cached file found. Obtaining entities from cache.");
            return readHashSetFromFile(file);
        }
        file.getParentFile().mkdirs();

        HashSet<String> result = new HashSet<>(100000);
        String queryString = "SELECT distinct ?concept WHERE { ?concept <http://kaiko.getalp.org/dbnary#describes> ?something . }";

        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            String conceptUri = queryResult.next().getResource("concept").getURI();
            result.add(shortenUri(conceptUri));
        }
        writeHashSetToFile("./cache/dbnary_entities.txt", result);
        return result;
    }


    @Override
    public String shortenUri(String uri) {
        return uri;
    }
}
