import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import training.Gensim;
import training.Word2VecConfiguration;
import walkGenerators.light.WalkGeneratorLight;

import java.io.File;

import static walkGenerators.classic.WalkGeneratorDefault.DEFAULT_WALK_FILE_TO_BE_WRITTEN;

/**
 * This class allows to generate walks and train embeddings for RDF2Vec Light.
 */
public class RDF2VecLight {

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
     * Walks to be performed per entitiy
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
    Word2VecConfiguration configuration = training.Word2VecConfiguration.CBOW;

    /**
     * Resources directory where the python files will be copied to.
     */
    File resourceDirectory = null;

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

        WalkGeneratorLight generatorLight = new WalkGeneratorLight(knowledgeGraphFile, entitiesFile);
        generatorLight.generateRandomMidWalks(numberOfThreads, numberOfWalksPerEntity, depth, this.getWalkFilePath());

        Gensim gensim;
        if(this.resourceDirectory != null) {
            gensim = Gensim.getInstance(this.resourceDirectory);
        } else gensim = Gensim.getInstance();

        String fileToWrite = this.getWalkFileDirectoryPath() + "model.kv";
        gensim.trainWord2VecModel(fileToWrite, getWalkFilePath(), this.configuration);
        gensim.writeModelAsTextFile(fileToWrite, fileToWrite.substring(0, fileToWrite.length() - 3) + ".txt", entitiesFile.getAbsolutePath());
        gensim.shutDown();
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

    public static void main(String[] args) {
        File knowledgeGraphFile = new File("/Users/janportisch/Documents/Research/DBpedia/dbpedia_merged.hdt");
        File walkDirectory = new File("/Users/janportisch/PycharmProjects/EmbeddingServer/jRDF2Vec/models/1025/walks/");
        File lightEntityFile = new File("/Users/janportisch/TMP/test_entities.txt");
        RDF2VecLight rdf2VecLight = new RDF2VecLight(knowledgeGraphFile, lightEntityFile, walkDirectory);
        rdf2VecLight.train();
    }

    public File getResourceDirectory() {
        return resourceDirectory;
    }

    public void setResourceDirectory(File resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
    }
}
