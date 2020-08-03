package de.uni_mannheim.informatik.dws.jrdf2vec;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGenerationMode;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGeneratorDefault;

import java.io.File;
import java.time.Instant;

import static de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGeneratorDefault.DEFAULT_WALK_FILE_TO_BE_WRITTEN;

/**
 * This class allows to generate walks and train embeddings for RDF2Vec Classic.
 */
public class RDF2Vec implements IRDF2Vec {

    /**
     * File with KG triples.
     */
    private File knowledgeGraphFile;

    /**
     * Ont model reference in case that is already loaded. (not important for CLI but for API usage)
     */
    private OntModel ontModel;

    /**
     * Default: Available Processors / 2
     */
    int numberOfThreads = Runtime.getRuntime().availableProcessors() / 2;

    /**
     * Walks to be performed per entity.
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
    String walkFilePath;

    /**
     * The training configuration to be used.
     */
    Word2VecConfiguration configuration = new Word2VecConfiguration();

    /**
     * Resources directory where the python files will be copied to.
     */
    File pythonServerResourceDirectory = null;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RDF2Vec.class);

    /**
     * Constructor
     *
     * @param knowledgeGraphFile File containing the knowledge graph.
     */
    public RDF2Vec(File knowledgeGraphFile) {
        this(knowledgeGraphFile, null);
    }

    /**
     * Variable which saves the time it took to generate walks as String (for the last run).
     */
    private String requiredTimeForLastWalkGenerationString = null;

    /**
     * Variable which saves the time ti took to train a model as String (for the last run).
     */
    private String requiredTimeForLastTrainingString = null;

    /**
     * Walk generation mode for the walk generation part.
     */
    private WalkGenerationMode walkGenerationMode = WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE;

    /**
     * Indicator whether a text file with all the vectors shall be generated.
     * This is, for example, required when using the <a href="https://github.com/mariaangelapellegrino/Evaluation-Framework">evaluation framework for KG embeddings</a>.
     */
    boolean isVectorTextFileGeneration = true;

    /**
     * Constructor
     *
     * @param knowledgeGraphFile File to the knowledge graph.
     * @param walkDirectory      Directory to which the walks shall be written to.
     */
    public RDF2Vec(File knowledgeGraphFile, File walkDirectory) {
        this.knowledgeGraphFile = knowledgeGraphFile;
        setWalkDirectory(walkDirectory);
    }

    /**
     * Constructor
     *
     * @param ontModel OntModel reference.
     * @param walkDirectory Directory to which the walks and models shall be written to.
     */
    public RDF2Vec(OntModel ontModel, File walkDirectory) {
       setWalkDirectory(walkDirectory);
       this.ontModel = ontModel;
    }


    /**
     * Train a new model with the existing parameters.
     * @param ontModel Model
     * @param walkDirectory Directory where walks shall be generated.
     * @return Path to model.
     */
    public String trainNew(OntModel ontModel, File walkDirectory){
        setWalkDirectory(walkDirectory);
        this.ontModel = ontModel;
        return train();
    }

    /**
     * Train a new model with the existing parameters.
     * @param knowledgeGraphFile KG file (for which embedding shall be trained).
     * @param walkDirectory Directory where walks shall be generated.
     * @return Path to model.
     */
    public String trainNew(File knowledgeGraphFile, File walkDirectory){
        this.knowledgeGraphFile = knowledgeGraphFile;
        setWalkDirectory(walkDirectory);
        return train();
    }


    /**
     * Train an RDF2Vec model.
     * The model will appear in the directory where the walks reside.
     *
     * @return Returns the path to the trained model.
     */
    public String train() {
        boolean useFile = true;
        if (ontModel == null) {

            // sanity checks
            if (knowledgeGraphFile == null) {
                LOGGER.error("Knowledge Graph File not set. ABORT.");
                return null;
            }
            if (!knowledgeGraphFile.exists()) {
                LOGGER.error("File " + knowledgeGraphFile.getAbsolutePath() + " does not exist. ABORT.");
                return null;
            }
        } else {
            useFile = false;
        }

        Instant before = Instant.now();

        WalkGeneratorDefault classicGenerator;
        if(useFile) {
            classicGenerator = new WalkGeneratorDefault(this.knowledgeGraphFile);
        } else {
            classicGenerator = new WalkGeneratorDefault(this.ontModel);
        }
        classicGenerator.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalksPerEntity, depth, getWalkFilePath());

        Instant after = Instant.now();
        this.requiredTimeForLastWalkGenerationString = Util.getDeltaTimeString(before, after);

        before = Instant.now();
        Gensim gensim;
        if(this.pythonServerResourceDirectory != null) {
            gensim = Gensim.getInstance(this.pythonServerResourceDirectory);
        } else gensim = Gensim.getInstance();

        String fileToWrite = this.getWalkFileDirectoryPath() + File.separator + "model.kv";
        gensim.trainWord2VecModel(fileToWrite, getWalkFileDirectoryPath(), this.configuration);
        if(isVectorTextFileGeneration) {
            gensim.writeModelAsTextFile(fileToWrite, this.getWalkFileDirectoryPath() + File.separator + "vectors.txt");
        }
        gensim.shutDown();
        after = Instant.now();
        this.requiredTimeForLastTrainingString = Util.getDeltaTimeString(before, after);

        return fileToWrite;
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
                return f.getParentFile().getCanonicalPath();
            } catch (Exception e) {
                return "." + File.separator + "walks";
            }
        } else return "." + File.pathSeparator + "walks";

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

    public File getPythonServerResourceDirectory() {
        return pythonServerResourceDirectory;
    }

    public void setPythonServerResourceDirectory(File pythonServerResourceDirectory) {
        this.pythonServerResourceDirectory = pythonServerResourceDirectory;
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

    /**
     * Set the walk file path given the directory.
     * @param walkDirectory Directory where walks shall be generated.
     */
    private void setWalkDirectory(File walkDirectory){
        if (walkDirectory == null || !walkDirectory.isDirectory()) {
            LOGGER.warn("walkDirectory is not a directory. Using default.");
            walkFilePath = WalkGeneratorDefault.DEFAULT_WALK_FILE_TO_BE_WRITTEN;
        } else {
            this.walkFilePath = walkDirectory.getAbsolutePath() + File.separator + "walk_file.gz";
        }
    }

    @Override
    public void setWalkGenerationMode(WalkGenerationMode walkGenerationMode) {
        this.walkGenerationMode = walkGenerationMode;
    }

    @Override
    public WalkGenerationMode getWalkGenerationMode() {
        return this.walkGenerationMode;
    }

    public boolean isVectorTextFileGeneration() {
        return isVectorTextFileGeneration;
    }

    public void setVectorTextFileGeneration(boolean vectorTextFileGeneration) {
        isVectorTextFileGeneration = vectorTextFileGeneration;
    }
}
