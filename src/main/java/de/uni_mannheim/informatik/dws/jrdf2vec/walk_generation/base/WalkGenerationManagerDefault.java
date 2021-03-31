package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.ContinuationEntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.EntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.MemoryEntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.TdbEntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.runnables.*;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.*;
import org.apache.jena.ontology.OntModel;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.zip.GZIPOutputStream;


/**
 * Default Walk Generator.
 * Intended to work on any data set.
 */
public class WalkGenerationManagerDefault implements IWalkGenerationManager{


    /**
     * Default Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(WalkGenerationManagerDefault.class);

    /**
     * Inject default entity selector.
     */
    public EntitySelector entitySelector;

    /**
     * If not specified differently, this directory will be used to persist walks.
     */
    public final static String DEFAULT_WALK_DIRECTORY = "." + File.separator + "walks";

    /**
     * If not specified differently, this file will be used to persists walks.
     */
    public final static String DEFAULT_WALK_FILE_TO_BE_WRITTEN = DEFAULT_WALK_DIRECTORY + File.separator + "walk_file.gz";

    /**
     * Can be set to false if there are problems with the parser to make sure that generation functions do not
     * start.
     */
    private boolean isWalkGeneratorOk = true;

    /**
     * Indicator whether text walks (based on datatype properties) shall be generated.
     */
    private boolean isGenerateTextWalks = false;

    /**
     * For the statistical output.
     */
    int processedEntities = 0;

    /**
     * For the statistical output.
     */
    int processedWalks = 0;

    /**
     * For the statistical output.
     */
    int fileProcessedLines = 0;

    /**
     * Parser.
     */
    public IWalkGenerator walkGenerator;

    /**
     * File writer for all the paths.
     */
    public Writer writer;

    /**
     * File path to the walk file to be written.
     */
    public String filePath;

    File walkDirectory;

    /**
     * Constructor
     * @param ontModel Model for which walks shall be generated.
     */
    public WalkGenerationManagerDefault(OntModel ontModel){
        this(ontModel, false);
    }

    /**
     * Constructor for OntModel.
     *
     * @param ontModel Model for which walks shall be generated.
     * @param isGenerateTextWalks Indicator whether text shall also appear in the embedding space.
     */
    public WalkGenerationManagerDefault(OntModel ontModel, boolean isGenerateTextWalks) {
        this.walkGenerator = new JenaOntModelMemoryWalkGenerator();
        ((JenaOntModelMemoryWalkGenerator) this.walkGenerator).setParseDatatypeProperties(isGenerateTextWalks);
        ((JenaOntModelMemoryWalkGenerator) this.walkGenerator).readDataFromOntModel(ontModel);
        this.entitySelector = new MemoryEntitySelector(((JenaOntModelMemoryWalkGenerator) walkGenerator).getData());
        this.setGenerateTextWalks(isGenerateTextWalks);
    }

    /**
     * Constructor
     * @param tripleFile File to the NT file or, alternatively, to a directory of NT files.
     */
    public WalkGenerationManagerDefault(File tripleFile){
        this(tripleFile, false, true);
    }

    /**
     * Constructor for triple file.
     *
     * @param tripleFile File to the NT file or, alternatively, to a directory of NT files.
     * @param isGenerateTextWalks Indicator whether text shall also appear in the embedding space.
     * @param isSetEntitySelector If true, an entity selector will be chosen automatically.
     */
    public WalkGenerationManagerDefault(File tripleFile, boolean isGenerateTextWalks, boolean isSetEntitySelector){
        this(tripleFile.toURI(), isGenerateTextWalks, isSetEntitySelector, null, null);
    }

    /**
     * Main Constructor
     * @param knowledgeGraphResource A URI representing the graph for which an embedding shall be trained.
     * @param isGenerateTextWalks True if text shall also appear in the embedding space.
     * @param isSetEntitySelector If true, an entity selector will be chosen automatically.
     * @param existingWalks If existing walks shall be parsed, the existing walk directory can be specified here.
     */
    public WalkGenerationManagerDefault(URI knowledgeGraphResource, boolean isGenerateTextWalks,
                                        boolean isSetEntitySelector, File existingWalks, File newWalkDirectory) {
        if(Util.uriIsFile(knowledgeGraphResource)) {
            File knowledgeGraphFile = new File(knowledgeGraphResource);
            if (!knowledgeGraphFile.exists()) {
                LOGGER.error("The knowledge graph resource file you specified does not exist. ABORT.");
                return;
            }
            if (knowledgeGraphFile.isDirectory()) {
                // DIRECTORY OPTIONS
                // (1) TDB
                // (2) Directory with multiple NT files
                if(Util.isTdbDirectory(knowledgeGraphFile)){
                    // (1) TDB
                    LOGGER.info("TDB directory recognized. Using disk-based TDB walk generator.");
                    this.walkGenerator = new TdbWalkGenerator(knowledgeGraphResource);
                    if(isSetEntitySelector) {
                        LOGGER.info("Setting TDB entity selector...");
                        EntitySelector entitySelector =
                                new TdbEntitySelector(((TdbWalkGenerator) walkGenerator).getTdbModel());
                        if(existingWalks == null){
                            this.entitySelector = entitySelector;
                        } else {
                            this.entitySelector = new ContinuationEntitySelector(existingWalks, newWalkDirectory,
                                    entitySelector);
                        }
                    }
                } else {
                    // (2) NT Directory
                    LOGGER.warn("You specified a directory. Trying to parse files in the directory. The program will fail (later) " +
                            "if you use an entity selector that requires one ontology.");
                    this.walkGenerator = new NtMemoryWalkGenerator(isGenerateTextWalks);
                    ((NtMemoryWalkGenerator) this.walkGenerator).readNtTriplesFromDirectoryMultiThreaded(knowledgeGraphFile, false);
                    if(isSetEntitySelector) {
                        EntitySelector entitySelector =
                                new MemoryEntitySelector(((NtMemoryWalkGenerator) this.walkGenerator).getData());
                        if(existingWalks == null){
                            this.entitySelector = entitySelector;
                        } else {
                            this.entitySelector = new ContinuationEntitySelector(existingWalks, newWalkDirectory,
                                    entitySelector);
                        }
                    }
                }
            } else {
                // knowledge graph resource is a file
                // decide on parser depending on file ending
                Pair<IWalkGenerator, EntitySelector> parserSelectorPair = WalkGeneratorManager.parseSingleFile(knowledgeGraphFile, isGenerateTextWalks);
                this.walkGenerator = parserSelectorPair.getValue0();
                if(isSetEntitySelector) {
                    this.entitySelector = parserSelectorPair.getValue1();
                    if(existingWalks == null){
                        this.entitySelector = entitySelector;
                    } else {
                        this.entitySelector = new ContinuationEntitySelector(existingWalks, newWalkDirectory,
                                entitySelector);
                    }
                }
            }
            this.setGenerateTextWalks(isGenerateTextWalks);
        }
    }

    /**
     * Constructor for path to triple file.
     *
     * @param pathToTripleFile The path to the NT file.
     */
    public WalkGenerationManagerDefault(String pathToTripleFile) {
        this(new File(pathToTripleFile));
    }

    public void generateWalks(WalkGenerationMode generationMode, int numberOfThreads, int numberOfWalks, int depth, int textWalkLength, String walkFile) {
        if (generationMode == null) {
            System.out.println("walkGeneration mode is null... Using default: RANDOM_WALKS_DUPLICATE_FREE");
            this.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, walkFile);
        } else if (generationMode == WalkGenerationMode.MID_WALKS) {
            System.out.println("Generate random mid walks...");
            this.generateRandomMidWalks(numberOfThreads, numberOfWalks, depth, walkFile);
        } else if (generationMode == WalkGenerationMode.MID_WALKS_DUPLICATE_FREE) {
            System.out.println("Generate random mid walks duplicate free...");
            this.generateRandomMidWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, walkFile);
        } else if (generationMode == WalkGenerationMode.RANDOM_WALKS) {
            System.out.println("Generate random walks...");
            this.generateRandomWalks(numberOfThreads, numberOfWalks, depth, walkFile);
        } else if (generationMode == WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE) {
            System.out.println("Generate random walks duplicate free...");
            this.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, walkFile);
        } else if (generationMode == WalkGenerationMode.MID_WALKS_WEIGHTED) {
            System.out.println("Generate weighted mid walks...");
            this.generateWeightedMidWalks(numberOfThreads, numberOfWalks, depth, walkFile);
        } else {
            System.out.println("ERROR. Cannot identify the \"walkGenenerationMode\" chosen. Aborting program.");
        }

        if(isGenerateTextWalks()){
            this.generateTextWalks(numberOfThreads, textWalkLength);
        }
        this.close();
    }

    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalks(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateRandomWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateDuplicateFreeWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        if(!isWalkGeneratorOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateRandomMidWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateWeightedMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        if(!isWalkGeneratorOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateWeightedMidWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomMidWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        if(!isWalkGeneratorOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateRandomMidWalksForEntitiesDuplicateFree(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    public void generateTextWalks(int numberOfThreads, int walkLength) {
        generateTextWalks(numberOfThreads, walkLength, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    public void generateTextWalks(int numberOfThreads, int walkLength, String filePathOfFileToBeWritten) {
        if(!isWalkGeneratorOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateTextWalksForEntities(entitySelector.getEntities(), numberOfThreads, walkLength);
    }

    public IWalkGenerator getWalkGenerator() {
        return this.walkGenerator;
    }

    /**
     * Helper method to check parser and manage log output.
     * @return True if parser is ok, false if parser is not ok.
     */
    private boolean isWalkGeneratorOk(){
        if (this.walkGenerator == null) {
            LOGGER.error("Parser not initialized. Aborting program.");
            return false;
        }
        if (!isWalkGeneratorOk) {
            LOGGER.error("Will not execute walk generation due to parser initialization error.");
            return false;
        }
        return true;
    }

    /**
     * Generate walks for the entities.
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads involved in generating the walks.
     * @param numberOfWalks   The number of walks to be generated per entity.
     * @param walkLength      The length of each walk.
     */
    public void generateRandomWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        setOutputFileWriter();

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
        flushWriter();
    }

    /**
     * Default: Do not change URIs.
     *
     * @param uri The uri to be transformed.
     * @return The URI as it is.
     */
    public String shortenUri(String uri) {
        return uri;
    }

    /**
     * Gets the function to be used to shorten URIs.
     * @return Function to shorten URIs (String -&gt; String).
     */
    public UnaryOperator<String> getUriShortenerFunction() {
        return s -> s;
    }

    public boolean isGenerateTextWalks() {
        return isGenerateTextWalks;
    }

    public void setGenerateTextWalks(boolean generateTextWalks) {
        isGenerateTextWalks = generateTextWalks;
    }

    private int timeout = 10;
    private TimeUnit timeoutUnit = TimeUnit.DAYS;

    /**
     * Generate walks for the entities that are duplicate free (i.e., no walk exists twice in the resulting file).
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore). Aka depth.
     */
    public void generateRandomMidWalksForEntitiesDuplicateFree(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        setOutputFileWriter();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            DuplicateFreeMidWalkEntityProcessingRunnable th = new DuplicateFreeMidWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        flushWriter();
    }

    /**
     * Generate walks for the entities.
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore). Aka depth.
     */
    public void generateWeightedMidWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        setOutputFileWriter();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            WeightedMidWalkEntityProcessingRunnable th = new WeightedMidWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        flushWriter();
    }

    /**
     * Generates text walks for the given entities.
     *
     * @param entities The set of entities for which text walks (datatype property based walks) shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param walkLength The length of each walks
     */
    public void generateTextWalksForEntities(Set<String> entities, int numberOfThreads, int walkLength){
        setOutputFileWriter();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));
        for (String entity : entities) {
            DatatypeWalkEntityProcessingRunnable runnable = new DatatypeWalkEntityProcessingRunnable(this, entity,
                    walkLength);
            pool.execute(runnable);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        flushWriter();
    }

    /**
     * Generate walks for the entities.
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore). Aka depth.
     */
    public void generateRandomMidWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        setOutputFileWriter();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            MidWalkEntityProcessingRunnable th = new MidWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        flushWriter();
    }

    /**
     * Generate walks for the entities that are free of duplicates (i.e., no walk exists twice in the resulting file).
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore).
     */
    public void generateDuplicateFreeWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        setOutputFileWriter();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            DuplicateFreeWalkEntityProcessingRunnable th = new DuplicateFreeWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        flushWriter();
    }

    /**
     * Generate walks for the entities.
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore).
     */
    public void generateWalksForEntities(HashSet<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        setOutputFileWriter();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            RandomWalkEntityProcessingRunnable th = new RandomWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
            pool.execute(th);
        }
        pool.shutdown();

        try {
            pool.awaitTermination(timeout, timeoutUnit);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        flushWriter();
    }

    /**
     * Flushes the walk writer.
     */
    void flushWriter(){
        if(this.writer != null){
            try {
                this.writer.flush();
            } catch (IOException e) {
                LOGGER.error("Could not flush writer.", e);
            }
        }
    }

    /**
     * Initialize {@link WalkGenerationManagerDefault#writer}.
     */
    void setOutputFileWriter(){
        // only act if the writer has not yet been initialized.
        if(this.writer == null) {
            File outputFile = new File(filePath);
            if(outputFile.getParentFile().mkdirs()){
                LOGGER.info("Directory created.");
            }

            // initialize the writer
            try {
                this.writer = new OutputStreamWriter(new GZIPOutputStream(
                        new FileOutputStream(outputFile, false)), StandardCharsets.UTF_8);
            } catch (Exception e1) {
                LOGGER.error("Could not initialize writer. Aborting process.", e1);
            }
        }
    }

    /**
     * Adds new walks to the list; If the list is filled, it is written to the
     * file.
     *
     * @param tmpList Entries that shall be written.
     */
    public synchronized void writeToFile(List<String> tmpList) {
        processedEntities++;
        processedWalks += tmpList.size();
        fileProcessedLines += tmpList.size();
        for (String str : tmpList)
            try {
                writer.write(str + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (processedEntities % 1000 == 0) {
            LOGGER.info("TOTAL PROCESSED ENTITIES: " + processedEntities);
            LOGGER.info("TOTAL NUMBER OF PATHS : " + processedWalks);
        }
        // flush the file
        if (fileProcessedLines > 3000000) {
            fileProcessedLines = 0;
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int tmpNM = (processedWalks / 3000000);
            String tmpFilename = filePath.replace(".gz", tmpNM + ".gz");
            try {
                writer = new OutputStreamWriter(new GZIPOutputStream(
                        new FileOutputStream(tmpFilename, false)), StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close resources.
     */
    public void close() {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            LOGGER.error("There was an error when closing the writer.", ioe);
        }
        if(getWalkGenerator() instanceof ICloseableWalkGenerator){
            ((ICloseableWalkGenerator) this.walkGenerator).close();
        }
    }
}
