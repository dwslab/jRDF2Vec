package de.uni_mannheim.informatik.dws.jrdf2vec;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecType;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.WalkGenerationManagerLight;

import java.io.File;
import java.time.Instant;


/**
 * This class allows to generate walks and train embeddings for RDF2Vec Light.
 */
public class RDF2VecLight implements IRDF2Vec {


    /**
     * File containing the entities for which an embedding shall be trained.
     */
    private File entitiesFile;

    /**
     * File with KG triples.
     */
    private File knowledgeGraphFile;

    /**
     * Default: Available Processors / 2
     */
    int numberOfThreads = Runtime.getRuntime().availableProcessors() / 2;

    /**
     * Walks to be performed per entity
     */
    int numberOfWalksPerEntity = 100;

    /**
     * Indicator whether text walks (based on datatype properties) shall be generated and used for training.
     */
    boolean isEmbedText;

    /**
     * Number of hops. You can also use uneven numbers.
     * Depth 1: node -&gt; edge -&gt; node.
     */
    int depth = 4;

    /**
     * The training configuration to be used.
     */
    Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);

    /**
     * Resources directory where the python files will be copied to.
     */
    File resourceDirectory = null;

    /**
     * Variable which saves the time it took to generate walks as String (for the last run).
     */
    private String requiredTimeForLastWalkGenerationString = null;

    /**
     * Variable which saves the time ti took to train a model as String (for the last run).
     */
    private String requiredTimeForLastTrainingString = null;

    /**
     * Default walk generation mode.
     */
    public static final WalkGenerationMode DEFAULT_WALK_GENERATION_MODE = WalkGenerationMode.MID_WALKS;

    /**
     * Walk generation mode.
     */
    private WalkGenerationMode walkGenerationMode = DEFAULT_WALK_GENERATION_MODE;

    /**
     * Indicator whether a text file with all the vectors shall be generated.
     * This is, for example, required when using the <a href="https://github.com/mariaangelapellegrino/Evaluation-Framework">evaluation framework for KG embeddings</a>.
     */
    boolean isVectorTextFileGeneration = true;

    private File walkDirectory;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RDF2VecLight.class);

    /**
     * Constructor
     *
     * @param knowledgeGraphFile File containing the knowledge graph.
     * @param entitiesFile       File containing the entities, one per line.
     */
    public RDF2VecLight(File knowledgeGraphFile, File entitiesFile) {
        this(knowledgeGraphFile, entitiesFile, null);
    }

    /**
     * Constructor
     *
     * @param knowledgeGraphFile File to the knowledge graph.
     * @param entitiesFile       File to the entities to be read.
     * @param walkDirectory      Directory to which the walks shall be written to.
     */
    public RDF2VecLight(File knowledgeGraphFile, File entitiesFile, File walkDirectory) {
        this.entitiesFile = entitiesFile;
        this.knowledgeGraphFile = knowledgeGraphFile;
        if (walkDirectory == null || !walkDirectory.isDirectory()) {
            LOGGER.warn("walkDirectory is not a directory. Using default.");
            this.walkDirectory = new File(WalkGenerationManager.DEFAULT_WALK_DIRECTORY);
        } else {
            this.walkDirectory = walkDirectory;
        }
    }

    /**
     * Train an RDF2Vec Light model.
     * The model will appear in the directory where the walks reside.
     */
    public void train() {
        // sanity checks
        if (!entitiesFile.exists()) {
            LOGGER.error("File " + entitiesFile.getAbsolutePath() + " does not exist. ABORT.");
            return;
        }
        if (!knowledgeGraphFile.exists()) {
            LOGGER.error("File " + knowledgeGraphFile.getAbsolutePath() + " does not exist. ABORT.");
            return;
        }

        Instant before = Instant.now();
        WalkGenerationManagerLight generatorLight = new WalkGenerationManagerLight(knowledgeGraphFile, entitiesFile, isEmbedText());
        generatorLight.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalksPerEntity, depth,
                configuration.getWindowSize(), this.walkDirectory);

        Instant after = Instant.now();
        this.requiredTimeForLastWalkGenerationString = Util.getDeltaTimeString(before, after);
        LOGGER.info("Walks successfully generated. Starting training now...");

        before = Instant.now();
        Gensim gensim;
        if(this.resourceDirectory != null) {
            gensim = Gensim.getInstance(this.resourceDirectory);
        } else gensim = Gensim.getInstance();

        String fileToWrite = this.getWalkDirectory().getAbsolutePath() + File.separator + "model.kv";
        gensim.trainWord2VecModel(fileToWrite, getWalkDirectory().getAbsolutePath(), this.configuration);
        if(isVectorTextFileGeneration) {
            gensim.writeModelAsTextFile(fileToWrite, this.getWalkDirectory().getAbsolutePath() +
                    File.separator + "vectors.txt", entitiesFile.getAbsolutePath());
        }
        Gensim.shutDown();
        after = Instant.now();
        this.requiredTimeForLastTrainingString = Util.getDeltaTimeString(before, after);
    }

    public File getEntitiesFile() {
        return entitiesFile;
    }

    public void setEntitiesFile(File entitiesFile) {
        this.entitiesFile = entitiesFile;
    }

    public File getKnowledgeGraphFile() {
        return knowledgeGraphFile;
    }

    public void setKnowledgeGraphFile(File knowledgeGraphFile) {
        this.knowledgeGraphFile = knowledgeGraphFile;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getNumberOfWalksPerEntity() {
        return numberOfWalksPerEntity;
    }

    @Override
    public boolean isEmbedText() {
        return this.isEmbedText;
    }

    @Override
    public void setEmbedText(boolean embedText) {
        this.isEmbedText = embedText;
    }

    public void setNumberOfWalksPerEntity(int numberOfWalksPerEntity) {
        this.numberOfWalksPerEntity = numberOfWalksPerEntity;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Returns the walk directory.
     * @return The walk directory.
     */
    public File getWalkDirectory() {
        return walkDirectory;
    }

    public void setWalkDirectory(File walkDirectory) {
        this.walkDirectory = walkDirectory;
    }

    /**
     * Set the directory to which walk files will be written to.
     *
     * @param directory The directory to which walk files will be written to. The directory should already exists.
     *                  On failure, the default will be used.
     */
    public void setWalkFileDirectoryPath(String directory) {
        File f = new File(directory);
        if (f.isDirectory()) {
            this.walkDirectory = f;
        }
    }

    public Word2VecConfiguration getWord2VecConfiguration() {
        return configuration;
    }

    public void setConfiguration(Word2VecConfiguration configuration) {
        this.configuration = configuration;
    }

    public File getResourceDirectory() {
        return resourceDirectory;
    }

    public void setResourceDirectory(File resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
    }

    /**
     * This method returns the time it took to generate walks for the last run as String.
     * @return The time it took to generate walks for the last run as String. Will never be null.
     */
    public String getRequiredTimeForLastWalkGenerationString() {
        if (this.requiredTimeForLastWalkGenerationString == null) return "<training time not yet set>";
        else return requiredTimeForLastWalkGenerationString;
    }

    /**
     * This method returns he time it took to train the model for the last run as String.
     * @return The time it took to train the model for the last run as String. Will never be null.
     */
    public String getRequiredTimeForLastTrainingString() {
        if(this.requiredTimeForLastTrainingString == null) return "<training time not yet set>";
        else return requiredTimeForLastTrainingString;
    }

    @Override
    public void setWalkGenerationMode(WalkGenerationMode walkGenerationMode) {
        this.walkGenerationMode = walkGenerationMode;
    }

    @Override
    public WalkGenerationMode getWalkGenerationMode() {
        return this.walkGenerationMode;
    }

    @Override
    public boolean isVectorTextFileGeneration() {
        return isVectorTextFileGeneration;
    }

    @Override
    public void setVectorTextFileGeneration(boolean vectorTextFileGeneration) {
        isVectorTextFileGeneration = vectorTextFileGeneration;
    }
}

