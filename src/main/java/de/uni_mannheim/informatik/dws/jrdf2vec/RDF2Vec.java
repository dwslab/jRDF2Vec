package de.uni_mannheim.informatik.dws.jrdf2vec;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManagerDefault;

import java.io.File;
import java.net.URI;
import java.time.Instant;

import static de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManagerDefault.DEFAULT_WALK_FILE_TO_BE_WRITTEN;

/**
 * This class allows to generate walks and train embeddings for RDF2Vec Classic.
 */
public class RDF2Vec implements IRDF2Vec {


    /**
     * File with KG triples.
     */
    private URI knowledgeGraphUri;

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
     * True if datatype properties shall be parsed and included into the embedding space.
     */
    boolean isEmbedText = false;

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

    private static final WalkGenerationMode defaultWalkGenerationMode = WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE;

    /**
     * Walk generation mode for the walk generation part.
     */
    private WalkGenerationMode walkGenerationMode = defaultWalkGenerationMode;

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
        this(knowledgeGraphFile.toURI(), walkDirectory);
    }

    /**
     * Constructor
     *
     * @param ontModel      OntModel reference.
     * @param walkDirectory Directory to which the walks and models shall be written to.
     */
    public RDF2Vec(OntModel ontModel, File walkDirectory) {
        setWalkDirectory(walkDirectory);
        this.ontModel = ontModel;
    }

    /**
     * Main constructor
     * @param knowledgeGraphUri The URI to the knowledge graph.
     * @param walkDirectory The walk directory that shall be generated.
     */
    public RDF2Vec(URI knowledgeGraphUri, File walkDirectory) {
        this.knowledgeGraphUri = knowledgeGraphUri;
        if(!isUriOk(knowledgeGraphUri)){
            LOGGER.error("There is a problem with the provided knowledge graph. RDF2Vec is not functional.");
        }
        setWalkDirectory(walkDirectory);
    }


    /**
     * Train a new model with the existing parameters.
     *
     * @param ontModel      Model
     * @param walkDirectory Directory where walks shall be generated.
     * @return Path to model.
     */
    public String trainNew(OntModel ontModel, File walkDirectory) {
        setWalkDirectory(walkDirectory);
        this.ontModel = ontModel;
        return train();
    }

    /**
     * Train a new model with the existing parameters.
     *
     * @param knowledgeGraphFile KG file (for which embedding shall be trained).
     * @param walkDirectory      Directory where walks shall be generated.
     * @return Path to model.
     */
    public String trainNew(File knowledgeGraphFile, File walkDirectory) {
        this.knowledgeGraphUri = knowledgeGraphFile.toURI();
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
            if (knowledgeGraphUri == null) {
                LOGGER.error("Knowledge Graph File not set. ABORT.");
                return null;
            }
            if (!getFile(knowledgeGraphUri).exists()) {
                LOGGER.error("File " + getFile(knowledgeGraphUri).getAbsolutePath() + " does not exist. ABORT.");
                return null;
            }
        } else {
            useFile = false;
        }

        Instant before = Instant.now();

        WalkGenerationManagerDefault classicGenerator;
        if (useFile) {
            classicGenerator = new WalkGenerationManagerDefault(getFile(this.knowledgeGraphUri), isEmbedText(), true);
        } else {
            classicGenerator = new WalkGenerationManagerDefault(this.ontModel, isEmbedText());
        }

        classicGenerator.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalksPerEntity, depth, configuration.getWindowSize(), getWalkFilePath());

        Instant after = Instant.now();
        this.requiredTimeForLastWalkGenerationString = Util.getDeltaTimeString(before, after);

        before = Instant.now();
        Gensim gensim;
        if (this.pythonServerResourceDirectory != null) {
            gensim = Gensim.getInstance(this.pythonServerResourceDirectory);
        } else gensim = Gensim.getInstance();

        String fileToWrite = this.getWalkFileDirectoryPath() + File.separator + "model.kv";
        gensim.trainWord2VecModel(fileToWrite, getWalkFileDirectoryPath(), this.configuration);
        if (isVectorTextFileGeneration) {
            gensim.writeModelAsTextFile(fileToWrite, this.getWalkFileDirectoryPath() + File.separator + "vectors.txt");
        }
        Gensim.shutDown();
        after = Instant.now();
        this.requiredTimeForLastTrainingString = Util.getDeltaTimeString(before, after);

        return fileToWrite;
    }

    public URI getKnowledgeGraphUri() {
        return knowledgeGraphUri;
    }

    public File getKnowledgeGraphFile() {
        return getFile(this.knowledgeGraphUri);
    }

    public void setKnowledgeGraphUri(File knowledgeGraphFile) {
        this.knowledgeGraphUri = knowledgeGraphFile.toURI();
    }

    public void setKnowledgeGraphUri(URI knowledgeGraphUri) {
        this.knowledgeGraphUri = knowledgeGraphUri;
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
     *
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

    public Word2VecConfiguration getWord2VecConfiguration() {
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
     *
     * @return The time it took to generate walks for the last run as String. Will never be null.
     */
    public String getRequiredTimeForLastWalkGenerationString() {
        if (this.requiredTimeForLastWalkGenerationString == null) return "<training time not yet set>";
        else return requiredTimeForLastWalkGenerationString;
    }

    /**
     * This method returns he time it took to train the model for the last run as String.
     *
     * @return The time it took to train the model for the last run as String. Will never be null.
     */
    public String getRequiredTimeForLastTrainingString() {
        if (this.requiredTimeForLastTrainingString == null) return "<training time not yet set>";
        else return requiredTimeForLastTrainingString;
    }

    /**
     * Set the walk file path given the directory.
     *
     * @param walkDirectory Directory where walks shall be generated.
     */
    private void setWalkDirectory(File walkDirectory) {
        if (walkDirectory == null || !walkDirectory.isDirectory()) {
            LOGGER.warn("walkDirectory is not a directory. Using default.");
            walkFilePath = WalkGenerationManagerDefault.DEFAULT_WALK_FILE_TO_BE_WRITTEN;
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

    @Override
    public boolean isEmbedText() {
        return isEmbedText;
    }

    @Override
    public void setEmbedText(boolean embedText) {
        isEmbedText = embedText;
    }

    static File getFile(URI fileUri) {
        return new File(fileUri);
    }

    /**
     * Check if the URI is ok.
     * @param uriToCheck The URI that shall be checked.
     * @return True if the URI is ok, else false.
     */
    public static boolean isUriOk(URI uriToCheck) {
        if (uriToCheck == null) {
            LOGGER.error("The provided URI/File for the knowledge graph is null.");
            return false;
        }
        try {
            if (getFile(uriToCheck).exists()) {
                return true;
            }
        } catch (IllegalArgumentException iae) {
            // we do nothing but continue with the next check
        }
        try {
            String queryString = "ASK { ?s ?p ?o . }";
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(uriToCheck.toString(), query);
            return queryExecution.execAsk();
        } catch (Exception e) {
            return false;
        }
    }

}
