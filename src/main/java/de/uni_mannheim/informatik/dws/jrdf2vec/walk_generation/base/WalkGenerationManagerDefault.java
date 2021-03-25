package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.entity_selector.EntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.entity_selector.MemoryEntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.runnables.RandomWalkEntityProcessingRunnable;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.NtMemoryWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.IWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.JenaOntModelMemoryWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.WalkGeneratorManager;
import org.apache.jena.ontology.OntModel;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;


/**
 * Default Walk Generator.
 * Intended to work on any data set.
 */
public class WalkGenerationManagerDefault extends WalkGenerationManager {


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
    private boolean parserIsOk = true;

    /**
     * Indicator whether text walks (based on datatype properties) shall be generated.
     */
    private boolean isGenerateTextWalks = false;

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
        this(tripleFile, false);
    }

    /**
     * Constructor for triple file.
     *
     * @param tripleFile File to the NT file or, alternatively, to a directory of NT files.
     * @param isGenerateTextWalks Indicator whether text shall also appear in the embedding space.
     */
    public WalkGenerationManagerDefault(File tripleFile, boolean isGenerateTextWalks){
        this(tripleFile.toURI(), isGenerateTextWalks);
    }

    /**
     * Main Constructor
     * @param knowledgeGraphResource A URI representing the graph for which an embedding shall be trained.
     * @param isGenerateTextWalks True if text shall also appear in the embedding space.
     */
    public WalkGenerationManagerDefault(URI knowledgeGraphResource, boolean isGenerateTextWalks) {
        if(Util.uriIsFile(knowledgeGraphResource)) {
            File knowledgeGraphFile = new File(knowledgeGraphResource);
            if (!knowledgeGraphFile.exists()) {
                LOGGER.error("The knowledge graph resource file you specified does not exist. ABORT.");
                return;
            }
            if (knowledgeGraphFile.isDirectory()) {

                if(Util.isTdbDirectory(knowledgeGraphFile)){
                    // TODO TDB handler
                } else {
                    LOGGER.warn("You specified a directory. Trying to parse files in the directory. The program will fail (later) " +
                            "if you use an entity selector that requires one ontology.");
                    this.walkGenerator = new NtMemoryWalkGenerator(isGenerateTextWalks);
                    ((NtMemoryWalkGenerator) this.walkGenerator).readNtTriplesFromDirectoryMultiThreaded(knowledgeGraphFile, false);
                    this.entitySelector = new MemoryEntitySelector(((NtMemoryWalkGenerator) this.walkGenerator).getData());
                }
            } else {
                // knowledge graph resource is a file
                // decide on parser depending on file ending
                Pair<IWalkGenerator, EntitySelector> parserSelectorPair = WalkGeneratorManager.parseSingleFile(knowledgeGraphFile, isGenerateTextWalks);
                this.walkGenerator = parserSelectorPair.getValue0();
                this.entitySelector = parserSelectorPair.getValue1();
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

    @Override
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

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalks(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateRandomWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
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
        if(!isParserOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateRandomMidWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateWeightedMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    @Override
    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        if(!isParserOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateWeightedMidWalksForEntities(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomMidWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        if(!isParserOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateRandomMidWalksForEntitiesDuplicateFree(entitySelector.getEntities(), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateTextWalks(int numberOfThreads, int walkLength) {
        generateTextWalks(numberOfThreads, walkLength, DEFAULT_WALK_FILE_TO_BE_WRITTEN);
    }

    @Override
    public void generateTextWalks(int numberOfThreads, int walkLength, String filePathOfFileToBeWritten) {
        if(!isParserOk()) return;
        this.filePath = filePathOfFileToBeWritten;
        generateTextWalksForEntities(entitySelector.getEntities(), numberOfThreads, walkLength);
    }

    /**
     * Helper method to check parser and manage log output.
     * @return True if parser is ok, false if parser is not ok.
     */
    private boolean isParserOk(){
        if (this.walkGenerator == null) {
            LOGGER.error("Parser not initialized. Aborting program.");
            return false;
        }
        if (!parserIsOk) {
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
        super.setOutputFileWriter();

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
        super.flushWriter();
    }

    /**
     * Default: Do not change URIs.
     *
     * @param uri The uri to be transformed.
     * @return The URI as it is.
     */
    @Override
    public String shortenUri(String uri) {
        return uri;
    }

    @Override
    public UnaryOperator<String> getUriShortenerFunction() {
        return s -> s;
    }

    public boolean isGenerateTextWalks() {
        return isGenerateTextWalks;
    }

    public void setGenerateTextWalks(boolean generateTextWalks) {
        isGenerateTextWalks = generateTextWalks;
    }
}
