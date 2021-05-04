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
public class WalkGenerationManager {


    /**
     * Default Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(WalkGenerationManager.class);

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
    //public final static String DEFAULT_WALK_FILE_TO_BE_WRITTEN = DEFAULT_WALK_DIRECTORY + File.separator +
    //        "walk_file.gz";

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
    long processedEntities = 0;

    /**
     * For the statistical output.
     */
    long processedWalks = 0;

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
    //public String filePath;

    File walkDirectory;

    /**
     * Constructor
     *
     * @param ontModel Model for which walks shall be generated.
     */
    public WalkGenerationManager(OntModel ontModel) {
        this(ontModel, false);
    }

    /**
     * Constructor for OntModel.
     *
     * @param ontModel            Model for which walks shall be generated.
     * @param isGenerateTextWalks Indicator whether text shall also appear in the embedding space.
     */
    public WalkGenerationManager(OntModel ontModel, boolean isGenerateTextWalks) {
        this.walkGenerator = new JenaOntModelMemoryWalkGenerator();
        ((JenaOntModelMemoryWalkGenerator) this.walkGenerator).setParseDatatypeProperties(isGenerateTextWalks);
        ((JenaOntModelMemoryWalkGenerator) this.walkGenerator).readDataFromOntModel(ontModel);
        this.entitySelector = new MemoryEntitySelector(((JenaOntModelMemoryWalkGenerator) walkGenerator).getData());
        this.setGenerateTextWalks(isGenerateTextWalks);
    }

    /**
     * Constructor
     *
     * @param tripleFile File to the NT file or, alternatively, to a directory of NT files.
     */
    public WalkGenerationManager(File tripleFile) {
        this(tripleFile, false, true);
    }

    /**
     * Constructor for triple file.
     *
     * @param tripleFile          File to the NT file or, alternatively, to a directory of NT files.
     * @param isGenerateTextWalks Indicator whether text shall also appear in the embedding space.
     * @param isSetEntitySelector If true, an entity selector will be chosen automatically.
     */
    public WalkGenerationManager(File tripleFile, boolean isGenerateTextWalks, boolean isSetEntitySelector) {
        this(tripleFile.toURI(), isGenerateTextWalks, isSetEntitySelector, null, null);
    }

    /**
     * Main Constructor
     *
     * @param knowledgeGraphResource A URI representing the graph for which an embedding shall be trained.
     * @param isGenerateTextWalks    True if text shall also appear in the embedding space.
     * @param isSetEntitySelector    If true, an entity selector will be chosen automatically.
     * @param existingWalks          If existing walks shall be parsed, the existing walk directory can be specified here.
     * @param newWalkDirectory       The new walk directory that is to be written. If there are existing walks, those will
     *                               be copied if the file is not corrupted.
     */
    public WalkGenerationManager(URI knowledgeGraphResource, boolean isGenerateTextWalks,
                                 boolean isSetEntitySelector, File existingWalks, File newWalkDirectory) {
        if (Util.uriIsFile(knowledgeGraphResource)) {
            File knowledgeGraphFile = new File(knowledgeGraphResource);
            if (!knowledgeGraphFile.exists()) {
                LOGGER.error("The knowledge graph resource file you specified does not exist. ABORT.");
                return;
            }
            if (knowledgeGraphFile.isDirectory()) {
                // DIRECTORY OPTIONS
                // (1) TDB
                // (2) Directory with multiple NT files
                if (Util.isTdbDirectory(knowledgeGraphFile)) {
                    // (1) TDB
                    LOGGER.info("TDB directory recognized. Using disk-based TDB walk generator.");
                    this.walkGenerator = new TdbWalkGenerator(knowledgeGraphResource);
                    if (isSetEntitySelector) {
                        LOGGER.info("Setting TDB entity selector...");
                        EntitySelector entitySelector =
                                new TdbEntitySelector(((TdbWalkGenerator) walkGenerator).getTdbModel());
                        if (existingWalks == null) {
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
                    if (isSetEntitySelector) {
                        EntitySelector entitySelector =
                                new MemoryEntitySelector(((NtMemoryWalkGenerator) this.walkGenerator).getData());
                        if (existingWalks == null) {
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
                if (isSetEntitySelector) {
                    this.entitySelector = parserSelectorPair.getValue1();
                    if (existingWalks == null) {
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
    public WalkGenerationManager(String pathToTripleFile) {
        this(new File(pathToTripleFile));
    }

    public void generateWalks(WalkGenerationMode generationMode, int numberOfThreads, int numberOfWalks, int depth,
                              int textWalkLength, File walkDirectory) {
        if (generationMode == null) {
            System.out.println("walkGeneration mode is null... Using default: RANDOM_WALKS_DUPLICATE_FREE");
            generationMode = WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE;
        }
        generateWalks(generationMode, numberOfThreads, numberOfWalks, depth, walkDirectory);

        // optionally generate text walks on top
        if (isGenerateTextWalks()) {
            this.generateTextWalks(numberOfThreads, textWalkLength);
        }
        this.close();
    }

    public void generateWalks(WalkGenerationMode mode, int numberOfThreads, int numberOfWalksPerEntity, int depth,
                              String walkDirectoryPath) {
        generateWalks(mode, numberOfThreads, numberOfWalksPerEntity, depth, new File(walkDirectoryPath));
    }

    public void generateWalks(WalkGenerationMode mode, int numberOfThreads, int numberOfWalksPerEntity, int depth,
                              File walkDirectory) {
        this.walkDirectory = walkDirectory;
        generateWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth,
                mode);
    }

    /**
     * Generate walks for the entities.
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads involved in generating the walks.
     * @param numberOfWalks   The number of walks to be generated per entity.
     * @param walkLength      The length of each walk.
     * @param mode            The walk generation mode.
     */
    public void generateWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength,
                                         WalkGenerationMode mode) {
        setOutputFileWriter();

        // thread pool
        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            DefaultEntityWalkRunnable th = new DefaultEntityWalkRunnable(this, entity, numberOfWalks, walkLength, mode);
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

    public void generateTextWalks(int numberOfThreads, int walkLength) {
        generateTextWalks(numberOfThreads, walkLength, new File(DEFAULT_WALK_DIRECTORY));
    }

    public void generateTextWalks(int numberOfThreads, int walkLength, File walkDirectory) {
        if (!isWalkGeneratorOk()) return;
        this.walkDirectory = walkDirectory;
        generateTextWalksForEntities(entitySelector.getEntities(), numberOfThreads, walkLength);
    }

    public IWalkGenerator getWalkGenerator() {
        return this.walkGenerator;
    }

    /**
     * Helper method to check parser and manage log output.
     *
     * @return True if parser is ok, false if parser is not ok.
     */
    private boolean isWalkGeneratorOk() {
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
     *
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
     * Generates text walks for the given entities.
     *
     * @param entities        The set of entities for which text walks (datatype property based walks) shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param walkLength      The length of each walks
     */
    public void generateTextWalksForEntities(Set<String> entities, int numberOfThreads, int walkLength) {
        setOutputFileWriter();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));
        for (String entity : entities) {
            DatatypeEntityWalkRunnable runnable = new DatatypeEntityWalkRunnable(this, entity,
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
     * Flushes the walk writer.
     */
    void flushWriter() {
        if (this.writer != null) {
            try {
                this.writer.flush();
            } catch (IOException e) {
                LOGGER.error("Could not flush writer.", e);
            }
        }
    }

    /**
     * Initialize {@link WalkGenerationManager#writer}.
     */
    void setOutputFileWriter() {
        // only act if the writer has not yet been initialized.
        if (this.writer == null) {
            File outputFile = new File(this.walkDirectory, "walk_file_0.txt.gz");
            if (outputFile.getParentFile().mkdirs()) {
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
     * @param walksToWrite Entries that shall be written.
     */
    public synchronized void writeToFile(List<String> walksToWrite) {
        if(walksToWrite == null){
            LOGGER.error("walksToWrite null. No walk will be written.");
            return;
        }
        processedEntities++;
        processedWalks += walksToWrite.size();
        fileProcessedLines += walksToWrite.size();
        for (String str : walksToWrite)
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
            long tmpNM = (processedWalks / 3000000);
            File newFile = new File(this.walkDirectory, "walk_file_" + tmpNM + ".txt.gz");
            try {
                writer = new OutputStreamWriter(new GZIPOutputStream(
                        new FileOutputStream(newFile, false)), StandardCharsets.UTF_8);
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
        if (getWalkGenerator() instanceof ICloseableWalkGenerator) {
            ((ICloseableWalkGenerator) this.walkGenerator).close();
        }
    }
}
