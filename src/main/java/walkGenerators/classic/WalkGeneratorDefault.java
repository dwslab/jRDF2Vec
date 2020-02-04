package walkGenerators.classic;

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.base.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * Default Walk Generator.
 * Intended to work on any dataset.
 */
public class WalkGeneratorDefault extends WalkGenerator {

    /**
     * Default Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(WalkGeneratorDefault.class);

    /**
     * Central OntModel
     */
    private OntModel model;


    /**
     * Inject default entity selector.
     */
    public EntitySelector entitySelector = new ClassicEntitySelector();


    /**
     * Constructor
     * @param tripleFile
     */
    public WalkGeneratorDefault(File tripleFile){
        String pathToTripleFile = tripleFile.getAbsolutePath();
        if(tripleFile.isDirectory()){
            LOGGER.error("You specified a directory, but a file needs to be specified as resource file. ABORT.");
            return;
        }
        if(!tripleFile.exists()){
            LOGGER.error("The resource file you specified does not exist. ABORT.");
            return;
        }
        try {
            String fileName = tripleFile.getName();
            if(fileName.toLowerCase().endsWith(".nt")) {
                this.model = readOntology(pathToTripleFile, "NT");
                this.parser = new NtParser(pathToTripleFile, this);
            } else if(fileName.toLowerCase().endsWith(".ttl")) {
                this.model = readOntology(pathToTripleFile, "TTL");
                File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length()-3) + "nt");
                NtParser.saveAsNt(this.model, newResourceFile);
                this.parser = new NtParser(newResourceFile, this);
            } else if (fileName.toLowerCase().endsWith(".xml")) {
                this.model = readOntology(pathToTripleFile, "RDFXML");
                File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length()-3) + "nt");
                NtParser.saveAsNt(this.model, newResourceFile);
                this.parser = new NtParser(newResourceFile, this);
            }
            LOGGER.info("Model read into memory.");
        } catch (MalformedURLException mue) {
            LOGGER.error("Path seems to be invalid. Generator not functional.", mue);
        }
    }

    /**
     * Constructor
     * @param pathToTripleFile The path to the NT file.
     */
    public WalkGeneratorDefault(String pathToTripleFile) {
        this(new File(pathToTripleFile));
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalks(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/walk_file.gz");
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateWalksForEntities(entitySelector.getEntities(this.model), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateDuplicateFreeWalksForEntities(entitySelector.getEntities(this.model), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/walk_file.gz");
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

        // thread pool
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
     * Default: Do not change URIs.
     * @param uri The uri to be transformed.
     * @return The URI as it is.
     */
    @Override
    public String shortenUri(String uri) {
        return uri;
    }
}
