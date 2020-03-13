package walkGenerators.classic;

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.base.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * Default Walk Generator.
 * Intended to work on any data set.
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
    public EntitySelector entitySelector;

    /**
     * If not specified differently, this file will be used to persists walks.
     */
    public final static String DEFAULT_WALK_FILE_TO_BE_WRITTEN = "./walks/walk_file.gz";

    /**
     * Can be set to false if there are problems with the parser to make sure that generation functions do not
     * start.
     */
    private boolean parserIsOk = true;


    /**
     * Constructor
     * @param tripleFile File to the NT file or, alternatively, to a directory of NT files.
     */
    public WalkGeneratorDefault(File tripleFile){
        String pathToTripleFile = tripleFile.getAbsolutePath();
        if(!tripleFile.exists()){
            LOGGER.error("The resource file you specified does not exist. ABORT.");
            return;
        }
        if(tripleFile.isDirectory()){
            LOGGER.warn("You specified a directory. Trying to parse files in the directory. The program will fail (later) " +
                    "if you use an entity selector that requires one ontology.");
            this.parser = new NtMemoryParser(this);
            ((NtMemoryParser) this.parser).readNtTriplesFromDirectoryMultiThreaded(tripleFile, false);
            return;
        } else {
            // decide on parser depending on
            try {
                String fileName = tripleFile.getName();
                if (fileName.toLowerCase().endsWith(".nt")) {
                    try {
                        LOGGER.info("Using NxParser.");
                        this.parser = new NxMemoryParser(pathToTripleFile, this);
                        this.entitySelector = new MemoryEntitySelector(((NxMemoryParser) parser).getData());
                    } catch (Exception e){
                        LOGGER.error("There was a problem using the default NxParser. Retry with slower NtParser.");
                        this.parser = new NtMemoryParser(pathToTripleFile, this);
                        this.entitySelector = new MemoryEntitySelector(((NtMemoryParser) parser).getData());
                    }
                    if(((MemoryParser) parser).getDataSize() == 0L){
                        LOGGER.error("There was a problem using the default NxParser. Retry with slower NtParser.");
                        this.parser = new NtMemoryParser(pathToTripleFile, this);
                        this.entitySelector = new MemoryEntitySelector(((NtMemoryParser) parser).getData());
                    }
                } else if (fileName.toLowerCase().endsWith(".ttl")) {
                    this.model = readOntology(pathToTripleFile, "TTL");
                    this.entitySelector = new OntModelEntitySelector(this.model);
                    File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length() - 3) + "nt");
                    NtMemoryParser.saveAsNt(this.model, newResourceFile);
                    this.parser = new NtMemoryParser(newResourceFile, this);
                } else if (fileName.toLowerCase().endsWith(".xml")) {
                    this.model = readOntology(pathToTripleFile, "RDFXML");
                    this.entitySelector = new OntModelEntitySelector(this.model);
                    File newResourceFile = new File(tripleFile.getParent(), fileName.substring(0, fileName.length() - 3) + "nt");
                    NtMemoryParser.saveAsNt(this.model, newResourceFile);
                    this.parser = new NtMemoryParser(newResourceFile, this);
                } else if (fileName.toLowerCase().endsWith(".hdt") || fileName.toLowerCase().endsWith(".hdt.index.v1-1")){
                    LOGGER.info("HDT file detected. Using HDT parser.");
                        try {
                            this.parser = new HdtParser(pathToTripleFile);
                        } catch (IOException ioe){
                            LOGGER.error("Propagated HDT Initializer Exception", ioe);
                        }
                }
                LOGGER.info("Model read into memory.");
            } catch (MalformedURLException mue) {
                LOGGER.error("Path seems to be invalid. Generator not functional.", mue);
            }
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
        generateRandomWalks(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateDuplicateFreeWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        if(this.parser == null){
            LOGGER.error("Parser not initilized. Aborting program");
            return;
        }
        if(!parserIsOk){
            LOGGER.error("Will not execute walk generation due to parser initialization error.");
            return;
        }
        this.filePath = filePathOfFileToBeWritten;
        generateRandomMidWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }


    /**
     * Generate walks for the entities.
     *
     * @param entities The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads involved in generating the walks.
     * @param numberOfWalks The number of walks to be generated per entity.
     * @param walkLength The length of each walk.
     */
    public void generateWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
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
