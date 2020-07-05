package de.uni_mannheim.informatik.dws.jrdf2vec;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecType;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGenerationMode;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.light.WalkGeneratorLight;

import java.io.File;
import java.time.Instant;

import static de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGeneratorDefault.DEFAULT_WALK_FILE_TO_BE_WRITTEN;

/**
 * This class allows to generate walks and train embeddings for de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec Light.
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
     * Number of hops. You can also use uneven numbers.
     * Depth 1: node -&gt; edge -&gt; node.
     */
    int depth = 4;

    /**
     * File to which the walk will be written to.
     */
    String walkFilePath = DEFAULT_WALK_FILE_TO_BE_WRITTEN;

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
     * Walk generation mode.
     */
    private WalkGenerationMode walkGenerationMode = WalkGenerationMode.MID_WALKS;

    /**
     * Indicator whether a text file with all the vectors shall be generated.
     * This is, for example, required when using the <a href="https://github.com/mariaangelapellegrino/Evaluation-Framework">evaluation framework for KG embeddings</a>.
     */
    boolean isVectorTextFileGeneration = true;

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
            walkFilePath = DEFAULT_WALK_FILE_TO_BE_WRITTEN;
        } else {
            this.walkFilePath = walkDirectory.getAbsolutePath() + "/walk_file.gz";
        }
    }

    /**
     * Train an de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec Light model.
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
        WalkGeneratorLight generatorLight = new WalkGeneratorLight(knowledgeGraphFile, entitiesFile);
        generatorLight.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalksPerEntity, depth, this.getWalkFilePath());

        Instant after = Instant.now();
        this.requiredTimeForLastWalkGenerationString = Util.getDeltaTimeString(before, after);

        before = Instant.now();
        Gensim gensim;
        if(this.resourceDirectory != null) {
            gensim = Gensim.getInstance(this.resourceDirectory);
        } else gensim = Gensim.getInstance();

        String fileToWrite = this.getWalkFileDirectoryPath() + "model.kv";
        gensim.trainWord2VecModel(fileToWrite, getWalkFileDirectoryPath(), this.configuration);
        if(isVectorTextFileGeneration) {
            gensim.writeModelAsTextFile(fileToWrite, this.getWalkFileDirectoryPath() + "vectors.txt", entitiesFile.getAbsolutePath());
        }
        gensim.shutDown();
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
     * Returns the path to the walk directory.
     * @return Path as String.
     */
    public String getWalkFileDirectoryPath() {
        File f = new File(this.getWalkFilePath());
        if (f != null) {
            try {
                return f.getParentFile().getCanonicalPath() + "/";
            } catch (Exception e) {
                return "./walks/";
            }
        } else return "./walks/";

    }

    /**
     * Set the directory to which walk files will be written to.
     *
     * @param directory The directory to which walk files will be written to. The directory should already exists.
     *                  On failure, the default will be used.
     */
    public void setWalkFileDirectoryPath(String directory) {
        File f = new File(directory);
        if (f != null && f.isDirectory()) {
            this.walkFilePath = f.getAbsolutePath() + "walk_file.gz";
        } else this.walkFilePath = DEFAULT_WALK_FILE_TO_BE_WRITTEN;
    }

    public String getWalkFilePath() {
        return walkFilePath;
    }

    /**
     * Set walk file to be written.
     *
     * @param walkFilePath File to be written.
     */
    public void setWalkFilePath(String walkFilePath) {
        this.walkFilePath = walkFilePath;
    }

    public Word2VecConfiguration getConfiguration() {
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

