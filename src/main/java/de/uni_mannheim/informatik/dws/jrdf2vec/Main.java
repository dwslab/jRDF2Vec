package de.uni_mannheim.informatik.dws.jrdf2vec;

import de.uni_mannheim.informatik.dws.jrdf2vec.debugging.VocabularyAnalyzer;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecType;
import de.uni_mannheim.informatik.dws.jrdf2vec.util.*;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.WalkGenerationManagerLight;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Mini command line tool for server application.
 */
public class Main {


    /**
     * word2vec configuration (not just CBOW/SG but contains also all other parameters)
     */
    private static Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);

    /**
     * File for light-weight generation
     */
    private static File lightEntityFile = null;

    /**
     * File to the knowledge graph
     */
    private static File knowledgeGraphFile = null;

    /**
     * The number of threads to be used for the walk generation and for the training.
     */
    private static int numberOfThreads = -1;

    /**
     * Dimensions for the vectors.
     */
    private static int dimensions = -1;

    /**
     * Word2vec minCount parameter.
     */
    private static int minCount = Word2VecConfiguration.MIN_COUNT_DEFAULT;

    /**
     * Default value to be used for the depth.
     */
    public static final int DEFAULT_DEPTH = 4;

    /**
     * Depth for the walks to be generated.
     */
    private static int depth = DEFAULT_DEPTH;

    /**
     * The default number of walks to be generated per node in the graph.
     */
    public static final int DEFAULT_NUMBER_OF_WALKS = 100;

    /**
     * The number of walks to be generated for each node.
     * Default: 100
     */
    private static int numberOfWalks = DEFAULT_NUMBER_OF_WALKS;

    /**
     * The file to which the python resources shall be copied.
     */
    private static File resourcesDirectory;

    /**
     * Orchestration instance
     */
    private static IRDF2Vec rdf2VecInstance;

    /**
     * Where the walks will be persisted (directory).
     */
    private static File walkDirectory = null;

    /**
     * In some cases, some walks may have already been generated. In such cases, the {@code oldWalkDirectory} can be provided.
     * Note that {@code oldWalkDirectory} must be different from {@link Main#walkDirectory}.
     */
    private static File existingWalkDirectory = null;

    /**
     * Walk generation mode.
     */
    private static WalkGenerationMode walkGenerationMode = null;

    /**
     * Sample parameter for down-sampling.
     */
    private static double sample = Word2VecConfiguration.SAMPLE_DEFAULT;

    /**
     * Epochs parameter.
     */
    private static int epochs = Word2VecConfiguration.EPOCHS_DEFAULT;

    /**
     * Window parameter.
     */
    private static int window = Word2VecConfiguration.WINDOW_SIZE_DEFAULT;

    /**
     * If true, only walks are generated and no embeddings are trained.
     * This can be beneficial when multiple configurations (e.g. SG and CBOW) shall be trained for only one set of walks.
     */
    private static boolean isOnlyWalks = false;

    /**
     * If true, only the training step is executed.
     */
    private static boolean isOnlyTraining = false;

    /**
     * By default a vector text file is generated.
     */
    private static boolean isVectorTextFileGeneration = true;

    /**
     * Args that were not parsed. Intended to show the user which parts were ignored.
     */
    private static HashSet<String> ignoredArguments;

    /**
     * If true, text will be included in the embeddings.
     * This is an extension to RDF2vec classic, therefore it is false by default.
     */
    private static boolean isEmbedText = false;

    /**
     * The port that is to be used
     */
    private static int port = Gensim.DEFAULT_PORT;

    /**
     * The default merge file.
     */
    public static final String DEFAULT_MERGE_FILE = "./mergedWalks.txt";

    /**
     * This variable is static merely for testing
     */
    private static boolean isServerOk;

    /**
     * The main method that is executed when running the JAR.
     *
     * @param args All the options for walk generation and training. Run with -help in order to get an overview.
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            ignoredArguments = new HashSet<>();
        } else ignoredArguments = new HashSet<>(Arrays.asList(args));

        if (args == null || args.length == 0) {
            System.out.println("Not enough arguments. Call '-help' to learn more about the CLI.");
            return;
        }

        // check for help
        if (containsIgnoreCase("-help", args) || containsIgnoreCase("--help", args) ||
                containsIgnoreCase("-h", args)) {
            System.out.println(getHelp());
            return;
        }

        if (containsIgnoreCase("-merge", args) || containsIgnoreCase("--merge", args) ||
                containsIgnoreCase("-mergeWalks", args) || containsIgnoreCase("--mergeWalks", args)
        ) {
            String walkDirectory = null;
            if (containsIgnoreCase("-walkDirectory", args) || containsIgnoreCase("-walkDir", args)) {
                walkDirectory = getValue("-walkDirectory", args);
                if (walkDirectory == null) {
                    walkDirectory = (walkDirectory == null) ? getValue("-walkDir", args) : null;
                }
            }
            if (walkDirectory == null) {
                System.out.println("Please provide a walkDirectory if you use -mergeWalks");
                return;
            }
            String fileToWrite = getValue("-o", args);
            if (fileToWrite == null) {
                System.out.println("Writing file: " + DEFAULT_MERGE_FILE);
                fileToWrite = DEFAULT_MERGE_FILE;
            }
            WalkMerger.mergeWalks(walkDirectory, fileToWrite);
            return;
        }

        String portString = getValue("-port", args);
        if (portString != null) {
            try {
                int intPort = Integer.parseInt(portString);
                Gensim.setPort(intPort);
                port = intPort;
            } catch (NumberFormatException nfe) {
                System.out.println("A problem occurred while trying to parse the following port number: " + portString
                        + "\nUsing default port: " + Gensim.DEFAULT_PORT);
            }
        }
        System.out.println("Using server port: " + port);

        // check install
        if (containsIgnoreCase("-checkInstall", args) ||
                containsIgnoreCase("-check", args) ||
                containsIgnoreCase("-checkRequirements", args) ||
                containsIgnoreCase("-checkInstallation", args)) {

            isServerOk = Gensim.getInstance().checkRequirements();
            if (isServerOk) {
                System.out.println("Installation is ok! [✔︎]");
            } else {
                System.out.println("Installation is not ok! [❌]\nIs Python 3 installed? Please check the log for " +
                        "missing dependencies.");
            }
            return;
        }

        // conversion to kv
        if (containsIgnoreCase("-convertToW2V", args)) {
            convertToW2v(args);
            return;
        }

        if (containsIgnoreCase("-convertToKv", args)) {
            convertToKv(args);
            return;
        }

        if (containsIgnoreCase("-convertToTfProjector", args)){
            convertToTfTsv(args);
            return;
        }

        // check for analysis request
        if (args[0].equalsIgnoreCase("-analyzevocab") || args[0].equalsIgnoreCase("-analyzevocabulary") ||
                args[0].equalsIgnoreCase("--analyzevocabulary") || args[0].equalsIgnoreCase("--analyzevocab")) {
            analyzeVocabulary(args);
            return;
        }

        // check for text file generation feature
        if (containsIgnoreCase("-generateTxtVectorFile", args) || containsIgnoreCase("-generateTextVectorFile", args)) {
            cliTextFileGeneration(args);
            return;
        }

        if (args.length == 2) {
            String modelFilePath = getValueMultiOption(args, "-generateVocabFile", "-generateVocabularyFile");
            if (modelFilePath != null) {
                printIfIgnoredOptionsExist();
                generateVocabFile(modelFilePath);
                return;
            }
        }

        if (containsIgnoreCase("-embedText", args) ||
                containsIgnoreCase("-text", args) ||
                containsIgnoreCase("--text", args) ||
                containsIgnoreCase("--embedText", args) ||
                containsIgnoreCase("-textEmbeddings", args) ||
                containsIgnoreCase("--textEmbeddings", args)) {
            isEmbedText = true;
        }



        if (containsIgnoreCase("-onlyTraining", args)) {
            isOnlyTraining = true;
            String walksPath = getValue("-walkDirectory", args);
            if (walksPath == null) {
                // try again with a different writing
                walksPath = getValue("-walkDir", args);
                if (walksPath == null) {
                    System.out.println("Required parameter -walkDirectory <path to walk directory or file> missing.\n" +
                            "Aborting program. Call '-help' to learn more about the CLI.");
                    return;
                }
            }
        }

        String knowledgeGraphFilePath = getValue("-graph", args);
        if (knowledgeGraphFilePath == null) knowledgeGraphFilePath = getValue("-g", args);

        isOnlyWalks = containsIgnoreCase("-onlyWalks", args);
        // allowing a bit more...
        if (!isOnlyWalks) isOnlyWalks = containsIgnoreCase("-walksOnly", args);

        if (!isOnlyTraining) {
            // the KG file path is only relevant if we want to do walk generation...
            if (knowledgeGraphFilePath == null) {
                System.out.println("Required parameter '-graph <kg_file>' not set - program cannot be started. " +
                        "Call '-help' to learn more about the CLI.");
                // stop program execution
                return;
            }
            knowledgeGraphFile = new File(knowledgeGraphFilePath);
            if (!knowledgeGraphFile.exists()) {
                System.out.println("The given file does not exist: " + knowledgeGraphFilePath);
                // stop program execution
                return;
            }
        }

        String lightEntityFilePath = getValue("-light", args);
        if (lightEntityFilePath != null) {
            lightEntityFile = new File(lightEntityFilePath);
            if (!lightEntityFile.exists()) {
                System.out.println("The given file does not exist: " + lightEntityFilePath);
            }
        }

        String walkDirectoryPath = getValue("-walkDir", args);
        walkDirectoryPath = (walkDirectoryPath == null) ? getValue("-walkDirectory", args) : walkDirectoryPath;
        if (walkDirectoryPath != null) {
            walkDirectory = new File(walkDirectoryPath);

            // Check whether the specified directory exists. If it does not exist, try to make the directory.
            if (!walkDirectory.exists()) {
                System.out.println("The specified walk directory does not exist. Trying to make the directory.");
                if (!walkDirectory.mkdirs()) {
                    System.out.println("Failed to make new walk directory. Using default.");
                    walkDirectory = null;
                }
            }

            // Check whether the specified directory is a directory.
            if (!walkDirectory.isDirectory()) {
                System.out.println("Walk directory is no directory! Using default.");
                walkDirectory = null;
            }
        }

        String existingWalkDirectoryPath = getValue("-continue", args);
        existingWalkDirectoryPath = (existingWalkDirectoryPath == null) ? getValue("--continue", args) :
                existingWalkDirectoryPath;
        if (existingWalkDirectoryPath != null) {
            existingWalkDirectory = new File(existingWalkDirectoryPath);

            // Check whether the specified directory exists.
            if (!existingWalkDirectory.exists() || !existingWalkDirectory.isDirectory()) {
                System.out.println("The specified continuation walk directory does not exist. No existing/old walks" +
                        "will be used.");
                existingWalkDirectory = null;
            } else {
                System.out.println("Re-using existing walks in '" + existingWalkDirectoryPath + "'.");
            }
        }

        String threadsText = getValue("-threads", args);
        if (threadsText != null) {
            try {
                numberOfThreads = Integer.parseInt(threadsText);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the number of threads. Using default.");
                numberOfThreads = Runtime.getRuntime().availableProcessors() / 2;
            }
        } else numberOfThreads = Runtime.getRuntime().availableProcessors() / 2;
        System.out.println("Using " + numberOfThreads + " threads for walk generation and training.");

        String dimensionText = getValue("-dimension", args);
        dimensionText = (dimensionText == null) ? getValue("-dimensions", args) : dimensionText;
        if (dimensionText != null) {
            try {
                dimensions = Integer.parseInt(dimensionText);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the number of dimensions. Using default (" +
                        Word2VecConfiguration.VECTOR_DIMENSION_DEFAULT + ").");
                dimensions = Word2VecConfiguration.VECTOR_DIMENSION_DEFAULT;
            }
        } else dimensions = Word2VecConfiguration.VECTOR_DIMENSION_DEFAULT;
        if (!isOnlyWalks) System.out.println("Using vector dimension: " + dimensions);

        String depthText = getValue("-depth", args);
        if (depthText != null) {
            try {
                depth = Integer.parseInt(depthText);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the depth. Using default (" + DEFAULT_DEPTH + ").");
                depth = DEFAULT_DEPTH;
            }
        } else depth = DEFAULT_DEPTH;
        System.out.println("Using depth " + depth);

        String numberOfWalksText = getValueMultiOption(args, "-numberOfWalks", "-numOfWalks", "-numOfWalks");
        if (numberOfWalksText != null) {
            try {
                numberOfWalks = Integer.parseInt(numberOfWalksText);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the number of walks. Using default.");
            }
        }
        System.out.println("Generating " + numberOfWalks + " walks per entity.");

        String resourcesDirectoryPath = getValue("-serverResourcesDir", args);
        if (resourcesDirectoryPath != null) {
            File f = new File(resourcesDirectoryPath);
            if (f.isDirectory()) {
                resourcesDirectory = f;
            } else {
                System.out.println("The specified directory for the python resources is not a directory. Using default.");
            }
        }

        String minCountString = getValue("-minCount", args);
        if (minCountString != null) {
            try {
                minCount = Integer.parseInt(minCountString);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the minCount. Using default (" + Word2VecConfiguration.MIN_COUNT_DEFAULT + ").");
                minCount = Word2VecConfiguration.MIN_COUNT_DEFAULT;
            }
        } else minCount = Word2VecConfiguration.MIN_COUNT_DEFAULT;

        String samplingString = getValue("-sample", args);
        if (samplingString != null) {
            try {
                sample = Double.parseDouble(samplingString);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the sample parameter. Using default (" + Word2VecConfiguration.SAMPLE_DEFAULT + ").");
                sample = Word2VecConfiguration.SAMPLE_DEFAULT;
            }
        } else sample = Word2VecConfiguration.SAMPLE_DEFAULT;

        String epochsString = getValue("-epochs", args);
        if (epochsString != null) {
            try {
                epochs = Integer.parseInt(epochsString);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the epochs parameter. Using default (" + Word2VecConfiguration.EPOCHS_DEFAULT + ").");
                epochs = Word2VecConfiguration.EPOCHS_DEFAULT;
            }
        }

        String windowString = getValue("-window", args);
        if (windowString != null) {
            try {
                window = Integer.parseInt(windowString);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the window parameter. Using default (" + Word2VecConfiguration.WINDOW_SIZE_DEFAULT + ").");
                window = Word2VecConfiguration.WINDOW_SIZE_DEFAULT;
            }
        }

        if (containsIgnoreCase("-noVectorTextFileGeneration", args)) {
            isVectorTextFileGeneration = false;
        } else if (containsIgnoreCase("-vectorTextFileGeneration", args)) {
            isVectorTextFileGeneration = true;
        }

        // determining the configuration for the training
        String trainingModeText = getValueMultiOption(args, "-trainingMode", "-trainMode");
        if (trainingModeText != null) {
            if (trainingModeText.equalsIgnoreCase("sg")) {
                configuration = new Word2VecConfiguration(Word2VecType.SG);
            } else configuration = new Word2VecConfiguration(Word2VecType.CBOW);
        } else configuration = new Word2VecConfiguration(Word2VecType.SG); // default: SG

        // setting training threads
        if (numberOfThreads > 0) configuration.setNumberOfThreads(numberOfThreads);

        // setting dimensions
        if (dimensions > 0) configuration.setVectorDimension(dimensions);

        // setting minCount
        if (minCount > 0) configuration.setMinCount(minCount);

        // setting epochs
        if (epochs > 0) configuration.setEpochs(epochs);

        // setting the window
        if (window > 0) configuration.setWindowSize(window);

        // set sample
        configuration.setSample(sample);

        String walkGenerationModeText = getValueMultiOption(args, "-walkGenerationMode", "-walkMode");
        if (walkGenerationModeText != null) {
            walkGenerationMode = WalkGenerationMode.getModeFromString(walkGenerationModeText);
        }

        // setting the default walk generation mode
        if (lightEntityFile != null) {
            walkGenerationMode = (walkGenerationMode == null) ? WalkGenerationMode.MID_WALKS : walkGenerationMode;
        } else {
            walkGenerationMode = (walkGenerationMode == null) ? WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE : walkGenerationMode;
        }

        Instant before, after;

        // -------------------
        //    only training
        // -------------------
        if (isOnlyTraining) {
            printIfIgnoredOptionsExist();
            System.out.println("Only training is performed, no walks are going to be generated.");
            before = Instant.now();
            String modelFilePathToWrite = walkDirectory.getAbsolutePath() + "/model.kv";
            Gensim.getInstance().trainWord2VecModel(modelFilePathToWrite, walkDirectory.getAbsolutePath(), configuration);
            Gensim.getInstance().writeModelAsTextFile(modelFilePathToWrite, walkDirectory.getAbsolutePath() + "/vectors.txt");
            after = Instant.now();
            System.out.println("\nTotal Time:");
            System.out.println(Util.getDeltaTimeString(before, after));
            return;
        }


        // ------------------
        //     only walks
        // ------------------
        if (isOnlyWalks) {
            printIfIgnoredOptionsExist();
            System.out.println("Only walks are being generated, training is performed.");

            // handle the walk directory
            if (walkDirectory == null || !walkDirectory.isDirectory()) {
                walkDirectory = new File(WalkGenerationManager.DEFAULT_WALK_DIRECTORY);
            }

            before = Instant.now();

            // now distinguish light/non-light
            if (lightEntityFile != null) {
                // light walk generation:
                WalkGenerationManagerLight generatorLight = new WalkGenerationManagerLight(
                        knowledgeGraphFile.toURI(),
                        lightEntityFile,
                        isEmbedText,
                        existingWalkDirectory,
                        walkDirectory);
                generatorLight.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalks, depth, window, walkDirectory);
            } else {
                // classic walk generation
                WalkGenerationManager classicGenerator = new WalkGenerationManager(knowledgeGraphFile.toURI(),
                        isEmbedText, true, existingWalkDirectory, walkDirectory);
                classicGenerator.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalks, depth, window, walkDirectory);
            }

            after = Instant.now();
            System.out.println("\nTotal Time:");
            System.out.println(Util.getDeltaTimeString(before, after));
            return; // important: stop here to avoid any training.
        }


        // ------------------------------------
        //     full run (walks + training)
        // ------------------------------------

        if (lightEntityFile == null) {
            printIfIgnoredOptionsExist();
            System.out.println("RDF2Vec Classic");

            RDF2Vec rdf2vec;
            if (walkDirectory == null) rdf2vec = new RDF2Vec(knowledgeGraphFile);
            else rdf2vec = new RDF2Vec(knowledgeGraphFile, walkDirectory);

            // setting threads
            if (numberOfThreads > 0) rdf2vec.setNumberOfThreads(numberOfThreads);

            // setting depth
            if (depth > 0) rdf2vec.setDepth(depth);

            // setting the number of walks
            if (numberOfWalks > 0) rdf2vec.setNumberOfWalksPerEntity(numberOfWalks);

            // setting the walk generation mode
            rdf2vec.setWalkGenerationMode(walkGenerationMode);

            // setting the text embedding option
            rdf2vec.setEmbedText(isEmbedText);

            // setting the continuation walk directory
            if (existingWalkDirectory != null) rdf2vec.setExistingWalkDirectory(existingWalkDirectory);

            // set resource directory for python server files
            if (resourcesDirectory != null) rdf2vec.setPythonServerResourceDirectory(resourcesDirectory);

            // set vector text file
            rdf2vec.setVectorTextFileGeneration(isVectorTextFileGeneration);

            rdf2vec.setConfiguration(configuration);
            before = Instant.now();
            rdf2vec.train();
            after = Instant.now();

            // setting the instance to allow for better testability
            rdf2VecInstance = rdf2vec;
        } else {
            printIfIgnoredOptionsExist();
            System.out.println("RDF2Vec Light Mode");
            RDF2VecLight rdf2VecLight;
            if (walkDirectory == null) rdf2VecLight = new RDF2VecLight(knowledgeGraphFile, lightEntityFile);
            else rdf2VecLight = new RDF2VecLight(knowledgeGraphFile, lightEntityFile, walkDirectory);

            // setting threads
            if (numberOfThreads > 0) rdf2VecLight.setNumberOfThreads(numberOfThreads);

            // setting depth
            if (depth > 0) rdf2VecLight.setDepth(depth);

            // setting the number of walks
            if (numberOfWalks > 0) rdf2VecLight.setNumberOfWalksPerEntity(numberOfWalks);

            // set resource directory
            if (resourcesDirectory != null) rdf2VecLight.setResourceDirectory(resourcesDirectory);

            // set vector text file
            rdf2VecLight.setVectorTextFileGeneration(isVectorTextFileGeneration);

            // setting the walk generation mode
            rdf2VecLight.setWalkGenerationMode(walkGenerationMode);

            // setting the text embedding option
            rdf2VecLight.setEmbedText(isEmbedText);

            // set vector text file
            rdf2VecLight.setVectorTextFileGeneration(isVectorTextFileGeneration);

            rdf2VecLight.setConfiguration(configuration);
            before = Instant.now();
            rdf2VecLight.train();
            after = Instant.now();

            // setting the instance to allow for better testability
            rdf2VecInstance = rdf2VecLight;
        }

        System.out.println("\nTotal Time:");
        System.out.println(Util.getDeltaTimeString(before, after));

        System.out.println("\nWalk Generation Time:");
        System.out.println(rdf2VecInstance.getRequiredTimeForLastWalkGenerationString());

        System.out.println("\nTraining Time:");
        System.out.println(rdf2VecInstance.getRequiredTimeForLastTrainingString());
    }

    private static void convertToTfTsv(String[] args) {
        String[] parameters = getValues("-convertToTfProjector", 3, args);
        if (parameters == null) {
            String txtFile = getValue("-convertToTfProjector", args);
            if (txtFile == null) {
                System.out.println("Your input is not correct.\n" +
                        "The syntax is: -convertToTfProjector <txt_file.txt> [<vectors.tsv> <metadata.tsv>]");
                return;
            }
            VectorTxtToTfProjectorTsv.convert(new File(txtFile));
        } else {
            VectorTxtToTfProjectorTsv.convert(
                    new File(parameters[0]),
                    new File(parameters[1]),
                    new File(parameters[2])
            );
        }
    }

    private static void convertToW2v(String[] args) {
        String[] parameters = getValues("-convertToW2V", 2, args);
        if (parameters == null) {
            System.out.println("Your input is not correct.\n" +
                    "The syntax is: -convertToW2V <txt_file_path> <new_file.w2v>");
            return;
        }
        VectorTxtToW2v.convert(new File(parameters[0]), new File(parameters[1]));
    }

    private static void convertToKv(String[] args) {
        String[] parameters = getValues("-convertToKv", 2, args);
        if (parameters == null) {
            System.out.println("Your input is not correct.\n" +
                    "The syntax is: -convertToKv <txt_file_path> <new_file.kv>");
            return;
        }
        KvConverter.convert(new File(parameters[0]), new File(parameters[1]));
    }

    /**
     * Write a UTF-8 encoded file containing the specified model's vocabulary.
     *
     * @param modelFilePath The model of which the vocabulary shall be written.
     */
    private static void generateVocabFile(String modelFilePath) {
        File modelFile = new File(modelFilePath);
        if (!modelFile.exists()) {
            System.out.println("The given file does not exist. Cannot generate vocabulary file.");
            return;
        }
        if (modelFile.isDirectory()) {
            System.out.println("The specified file is a directory. Cannot generate vocabulary file.");
            return;
        }
        File fileToGenerate = new File(modelFile.getParentFile().getAbsolutePath(), "vocabulary.txt");
        Gensim.getInstance().writeVocabularyToFile(modelFilePath, fileToGenerate.getAbsolutePath());
    }

    /**
     * Text vector file generation was triggered and will be further executed in this method.
     *
     * @param args The args.
     */
    private static void cliTextFileGeneration(String[] args) {
        String transformationSource = getValueMultiOption(args, "-generateTxtVectorFile", "-generateTextVectorFile");
        if (transformationSource != null) {
            String entityFile = getValue("-light", args);
            String fileToWritePath = getValueMultiOption(args, "-file", "-newFile");
            boolean isNoTags = containsIgnoreCase("-noTags", args);
            printIfIgnoredOptionsExist();
            generateTextVectorFile(transformationSource, entityFile, fileToWritePath, isNoTags);
        } else {
            System.out.println("Please specify which vector file shall be used.");
        }
    }

    /**
     * Given a model or vector file, a text file is generated containing all the vectors.
     *
     * @param transformationSource File path to the model or vector file.
     * @param entityFilePath       The entity file path pointing to a file containing the entities that shall be added
     *                             to the text vector file. The file must contain one entity per line. The file
     *                             must be UTF-8 encoded.
     * @param filePathToBeWritten  File path to be written.
     * @param isNoTags             Indicates whether surrounding concept tags shall be removed.
     *                             For example {@code <http://www.example.com/myConcept>} will be changed to
     *                             {@code http://www.example.com/myConcept}. Concepts without surrounding tags are not affected.
     */
    private static void generateTextVectorFile(String transformationSource, String entityFilePath,
                                               String filePathToBeWritten, boolean isNoTags) {
        File sourceFile = new File(transformationSource);
        if (!sourceFile.exists()) {
            System.out.println("The given file does not exist. Cannot generate text vector file.");
            return;
        }
        if (sourceFile.isDirectory()) {
            System.out.println("The specified file is a directory. Cannot generate text vector file.");
            return;
        }

        File fileToGenerate;

        // check text vector reduction
        if (transformationSource.endsWith(".txt")) {
            // sanity check
            if (entityFilePath == null && !isNoTags) {
                System.out.println("You already have a vector txt file. You must specify an entity file (-light) to " +
                        "reduce it or declare that tags shall be removed (-noTags). Doing nothing.");
                return;
            }

            if (entityFilePath != null) {
                // light option / reduce file option

                if (filePathToBeWritten == null) {
                    // auto-assign name:
                    System.out.println("A file with the name: reduced_vectors.txt will be written (in the directory of " +
                            "the txt vector source file.)");
                    fileToGenerate = new File(sourceFile.getParentFile().getAbsolutePath(), "reduced_vectors.txt");

                } else {
                    fileToGenerate = new File(filePathToBeWritten);
                }
                VectorFileReducer.writeReducedTextVectorFile(transformationSource, fileToGenerate.getAbsolutePath(),
                        entityFilePath, isNoTags);
                return;
            } else {
                // simply remove tags

                if (filePathToBeWritten == null) {
                    System.out.println("A file with the name: vectors_no_tags.txt will be written (in the directory of " +
                            "the txt vector source file.)");
                    fileToGenerate = new File(sourceFile.getParentFile().getAbsolutePath(), "vectors_no_tags.txt");
                } else {
                    fileToGenerate = new File(filePathToBeWritten);
                }
                TagRemover.removeTagsWriteNewFile(transformationSource, fileToGenerate.getAbsolutePath());
            }
        }

        if (filePathToBeWritten == null) {
            fileToGenerate = new File(sourceFile.getParentFile().getAbsolutePath(), "vectors.txt");
        } else {
            fileToGenerate = new File(filePathToBeWritten);
        }
        if (entityFilePath != null) {
            File entityFile = new File(entityFilePath);
            if (entityFile.exists()) {
                if (!entityFile.isDirectory()) {
                    Gensim.getInstance().writeModelAsTextFile(transformationSource, fileToGenerate.getAbsolutePath(),
                            entityFile.getAbsolutePath());
                    // we need to stop here:
                    return;
                } else {
                    System.out.println("ERROR: The given entity file is a directory. Writing vector file for all entities.");
                }
            } else {
                System.out.println("ERROR: The given entity file does not exist. Writing vector file for all entities.");
            }
        }
        Gensim.getInstance().writeModelAsTextFile(transformationSource, fileToGenerate.getAbsolutePath());
    }

    /**
     * If there are arguments that are not processed, they will be printed to the console for the user.
     */
    private static void printIfIgnoredOptionsExist() {
        if (ignoredArguments != null && ignoredArguments.size() > 0) {
            System.out.println("\nThe following arguments were ignored:");
            for (String s : ignoredArguments) {
                System.out.println("\t- " + s);
            }
            System.out.println();
        }
    }

    /**
     * Helper method.
     *
     * @param key       Arg key.
     * @param arguments Arguments as received upon program start.
     * @return Value of argument if existing, else null.
     */
    public static String getValue(String key, String[] arguments) {
        if (arguments == null) return null;
        int positionSet = -1;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase(key)) {
                positionSet = i;
                break;
            }
        }
        if (positionSet != -1 && arguments.length > positionSet + 1) {
            if (ignoredArguments != null) {
                ignoredArguments.remove(key);
                ignoredArguments.remove(arguments[positionSet + 1]);
            }
            return arguments[positionSet + 1];
        } else return null;
    }

    /**
     * Obtain more than one value given a key and an args array.
     *
     * @param key          The key preceding the values.
     * @param valuesNumber The number of values to obtain.
     * @param arguments    The arguments which shall be parsed.
     * @return The values in a String array. Null if there were any issues.
     */
    public static String[] getValues(String key, int valuesNumber, String[] arguments) {
        if (arguments == null) return null;
        if (valuesNumber <= 0) return null;
        if (key == null) return null;
        String[] result = new String[valuesNumber];
        int positionSet = -1;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase(key)) {
                positionSet = i;
                break;
            }
        }
        if (positionSet == -1) {
            return null;
        }
        for (int i = 0; i < valuesNumber; i++) {
            if (arguments.length > positionSet + i + 1) {
                ignoredArguments = getIgnoredArguments();
                if (i == 0) {
                    ignoredArguments.remove(key);
                }
                ignoredArguments.remove(arguments[positionSet + 1 + i]);
                result[i] = arguments[positionSet + 1 + i];
            } else return null;
        }
        return result;
    }

    /**
     * Helper method. Obtains the value following the first key found in {@code keys}.
     *
     * @param args Args array.
     * @param keys Keys for which the array shall be checked.
     * @return First value that is found.
     */
    public static String getValueMultiOption(String[] args, String... keys) {
        if (args == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            String result = getValue(key, args);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Check whether {@code element} is contained in {@code array}.
     *
     * @param element The element that shall be looked for.
     * @param array   The array in which shall be looked for the element.
     * @return True if {@code element} is contained in {@code array}, else false.
     */
    public static boolean containsIgnoreCase(String element, String[] array) {
        if (element == null || array == null) return false;

        // remove from set of ignored options.
        String removeFromIgnoredOptions = "";
        for (String s : ignoredArguments) {
            if (element.equalsIgnoreCase(s)) {
                removeFromIgnoredOptions = s;
                break;
            }
        }
        ignoredArguments.remove(removeFromIgnoredOptions);

        // perform the actual check
        for (String s : array) {
            if (element.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    /**
     * Get the instance for testing. Not required for operational usage.
     *
     * @return RDF2Vec instance.
     */
    public static IRDF2Vec getRdf2VecInstance() {
        return rdf2VecInstance;
    }

    /**
     * Get the walk generation mode for testing. Not required for operational usage.
     *
     * @return Walk Generation Mode.
     */
    public static WalkGenerationMode getWalkGenerationMode() {
        return walkGenerationMode;
    }

    /**
     * Get depth for testing. Not required for operational usage.
     *
     * @return Depth as int.
     */
    public static int getDepth() {
        return depth;
    }

    public static boolean isIsServerOk() {
        return isServerOk;
    }

    /**
     * Obtain the arguments that were not (yet) parsed.
     *
     * @return Set of arguments that are not (yet) parsed.
     */
    public static HashSet<String> getIgnoredArguments() {
        if (ignoredArguments == null) {
            ignoredArguments = new HashSet<>();
        }
        return ignoredArguments;
    }

    /**
     * Perform the analysis of the vocabulary.
     *
     * @param args The CLI args.
     */
    public static void analyzeVocabulary(String[] args) {
        // check the amount of parameters
        if (args.length != 3) {
            System.out.println("ERROR: Two parameters are required for -analyzeVocab! Please use the command as stated below:\n" +
                    "-analyzeVocab <model_file> <training_file | entity_file>\n" +
                    "Please refer to the help for more information (-help).");
            return;
        }

        System.out.println("Report\n------");
        System.out.println("Model file: " + args[1]);
        System.out.println("Entity file: " + args[2] + "\n\n");

        if (args[2].endsWith(".txt")) {
            System.out.println("Missing Concepts:");
            for (String s : VocabularyAnalyzer.detectMissingEntities(args[1], args[2])) {
                System.out.println(s);
            }
            System.out.println("\n\n");
            System.out.println("Additional Concepts:");
            for (String s : VocabularyAnalyzer.detectAdditionalEntities(args[1], args[2])) {
                System.out.println(s);
            }
        } else {
            System.out.println(VocabularyAnalyzer.analyze(args[1], args[2]));
        }
    }

    /**
     * Get the help text on how to use the CLI.
     * Developer note: Also add new commands to the README.
     *
     * @return Help text as String.
     */
    public static String getHelp() {
        return "*****************\n" +
                "* jRDF2Vec Help *\n" +
                "*****************\n\n" +

                "Walk Generation and RDF2Vec Training\n" +
                "------------------------------------\n\n" +

                "Required Parameters:\n\n" +
                "    -graph <graph_file>\n" +
                "    The file containing the knowledge graph for which you want to generate embeddings.\n\n" +

                "Optional Parameters:\n\n" +
                "    -onlyWalks\n" +
                "    If added to the call, this switch will deactivate the training part so that only walks are generated. \n" +
                "    If training parameters are specified, they are ignored. The walk generation also works with the\n" +
                "    `-light` parameter.\n\n" +

                "    -light <entity_file>\n" +
                "    If you intend to use RDF2Vec Light, you have to use this switch followed by the file path ot the\n" +
                "    describing the entities for which you require an embedding space. The file should contain one\n" +
                "    entity (full URI) per line.\n\n" +

                "    -numberOfWalks <number> (default: 100)\n" +
                "    The number of walks to be performed per entity.\n\n" +

                "    -depth <depth> (default: 4)\n" +
                "    This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you\n" +
                "    can also set an odd number. A depth of 1 leads to a sentence in the form <s p o>.\n\n" +

                "    -walkGenerationMode <MID_WALKS | MID_WALKS_DUPLICATE_FREE | RANDOM_WALKS | RANDOM_WALKS_DUPLICATE_FREE>\n" +
                "    (default for light: MID_WALKS, default for classic: RANDOM_WALKS_DUPLICATE_FREE)\n" +
                "    This parameter determines the mode for the walk generation (multiple walk generation algorithms\n" +
                "    are available). Reasonable defaults are set.\n\n" +

                "    -threads <number_of_threads> (default: (# of available processors) / 2)\n" +
                "    This parameter allows you to set the number of threads that shall be used for the walk generation\n" +
                "    as well as for the training.\n\n" +

                "    -walkDirectory <directory where walk files shall be generated/reside>\n" +
                "    The directory where the walks shall be generated into. In case of -onlyTraining, the directory\n" +
                "    where the walks reside.\n\n" +

                "    -embedText\n" +
                "    If added to the call, this switch will also generate walks that contain textual fragments of datatype properties.\n\n" +

                "    -onlyTraining\n" +
                "    If added to the call, this switch will deactivate the walk generation part so that only the training\n" +
                "    is performed. The parameter -walkDirectory must be set. If walk generation parameters are specified,\n" +
                "    they are ignored.\n\n" +

                "    -trainingMode <cbow|sg> (default: sg)\n" +
                "    This parameter controls the mode to be used for the word2vec training. Allowed values are cbow and sg.\n\n" +

                "    -dimension <size_of_vector> (default: 200)\n" +
                "    This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).\n\n" +

                "    -minCount <number> (default: 1)\n" +
                "    The minimum word count for the training. Unlike in the gensim defaults, this parameter is set to 1\n" +
                "    because for KG embeddings, a vector for each node/arc is desired.\n\n" +

                "    -noVectorTextFileGeneration | -vectorTextFileGeneration\n" +
                "    A switch that indicates whether a text file with the vectors shall be persisted on the disk. This\n" +
                "    is enabled by default. Use -noVectorTextFileGeneration to disable the file generation.\n\n" +

                "    -sample <number> (default: 0)\n" +
                "    The threshold for configuring which higher-frequency words are randomly down-sampled, a useful \n" +
                "    range is (0, 0.00001).\n\n" +

                "    -window <number> (default: 5)\n" +
                "    The window size to be used for the word2vec algorithm component.\n\n" +

                "    -epochs <number> (default: 5)\n" +
                "    The epochs for the training.\n\n" +

                "    -port <port_number> (default: 1808)\n" +
                "    The port that shall be used for the server.\n\n" +

                "    -continue <existing_walk_directory>\n" +
                "    In some cases, old walks shall be re-used (e.g. if the program was interrupted after 48h).\n" +
                "    With the -continue option, the old walks will be re-used and only missing walks are\n" +
                "    generated. This does not work for MID_WALKS. If you do not need to generate additional walks\n" +
                "    use -onlyTraining instead.\n\n" +

                "\n" +

                "Additional Services\n" +
                "-------------------\n\n" +

                "A) Generation of Vector Text File\n" +
                "   jRDF2vec is compatible with the evaluation framework for KG embeddings (GEval). This framework\n" +
                "   requires the vectors to be present in a text file. If you have a gensim model or vector file,\n" +
                "   you can use the following parameter to generate this file:\n\n" +
                "       -generateTextVectorFile <model_or_vector_file>\n" +
                "       The file path to the model or vector file that shall be used to write the vectors in a text\n" +
                "       file needs to be specified.\n\n" +
                "   If you want to write a `vectors.txt` file that contains only a subset of the vocabulary, you\n" +
                "   can additionally specify the entities of interest using the `-light <entity_file>` option\n" +
                "   (The `<entity_file>` should contain one entity (full URI) per line.).\n" +
                "   You can find the file (named `vectors.txt`) in the directory where the model/vector file is\n" +
                "   located.\n" +
                "   If you want to specify the file name/path yourself, you can use option `-newFile <file_path>`.\n" +
                "   If the vector concepts contain surrounding tags that you want to remove in the process, use\n" +
                "   option `-noTags`.\n" +
                "   This command also works if <model_or_vector_file> is an existing vector text\n" +
                "   file that shall be reduced." +
                "   \n\n" +
                "B) Generation of Vocabulary Text File\n" +
                "   jRDF2vec provides functionality to print all concepts for which a vector has been trained:\n\n" +
                "       -generateVocabularyFile <model_or_vector_file>\n" +
                "       One word of the vocabulary will be printed per line to a file named vocabulary.txt.\n" +
                "       The model or vector file needs to be specified.\n\n" +
                "C) Analysis of the Vocabulary\n" +
                "   For RDF2vec, it is not always guaranteed that all concepts in the graph appear in the embedding\n" +
                "   space.\n" +
                "   For example, some concepts may only appear in the object position of statements and may never be\n" +
                "   reached by random walks. In addition, the word2vec configuration parameters may filter out infrequent\n" +
                "   words depending on the configuration (see -minCount above, for example). To analyze such rather \n" +
                "   seldom cases, you can use the `-analyzeVocab` function specified as follows:\n\n" +
                "       -analyzeVocab <model> <training_file|entity_file>\n" +
                "       where <model>\n" +
                "          refers to any model representation such as gensim model file, .kv file, or .txt file\n" +
                "          Just make sure you use the correct file endings.\n" +
                "       where <training_file|entity_file>\n" +
                "          refers either to the NT/TTL etc. file that has been used to train the model or to a text file.\n" +
                "          containing the concepts you  want to check (one concept per line in the text file, make sure\n" +
                "          the file ending is .txt).\n\n" +
                "D) Merge of All Walk Files Into One\n" +
                "   By default, jRDF2vec serializes walks in different gzipped files. If you require one\n" +
                "   uncompressed, file, you can use the `-mergeWalks` keyword. You need to provide a\n" +
                "   `-walkDirectory <dir>` and you can optionally specify the output file using `-o <file_path>`\n\n" +
                "E) Generation of Tensorflow Projector Files\n" +
                "   If you want to visualize your embedding space by using the Tensorflow Projector, you can do so\n" +
                "   by converting your vectors.txt file to the two files required by the tool.\n" +
                "   Use the following command:\n\n" +
                "       -convertToTfProjector <txt_file_path> [<vectors.tsv> <metadata.tsv>]\n" +
                "       where <txt_file_path>\n" +
                "           refers to the vector text file as generated by jRDF2vec.\n" +
                "       where <vectors.tsv>\n" +
                "           refers to the vector tsv file path that will be written. If you specify a vectors.tsv\n" +
                "           path, you must also specify a metadata.tsv path (see below).\n" +
                "       where <metadata.tsv>\n" +
                "           refers to the metadata tsv file path that will be written.\n\n" +
                "F) Converting a txt Vector File to w2v Format\n" +
                "   To create a word2vec formatted file from the text file, you can use the following command:\n\n" +
                "       -convertToW2V <txt_file_path> <new_file.w2v>\n\n" +
                "G) Converting a txt/w2v Vector File to Gensim kv Format\n" +
                "   You can convert any txt or w2v vector file, generated by jRDF2vec or any other tool, to a\n" +
                "   gensim keyed vectors file (.kv). Use the following command:\n\n" +
                "       -convertToKv <txt_file_path> <new_file.kv>\n" +
                "       where <txt_file_path>\n" +
                "           is the file you want to convert. Make sure you use the correct file endings to indicate\n" +
                "           the format (.txt/.w2v).\n" +
                "       where <new_file.kv>\n" +
                "           is the new file that is to be written. It is recommend to use file suffix '.kv'.\n";
    }

    /**
     * Reset parameters (required for testing).
     */
    public static void reset() {
        configuration = new Word2VecConfiguration(Word2VecType.SG);
        lightEntityFile = null;
        knowledgeGraphFile = null;
        numberOfThreads = -1;
        dimensions = -1;
        depth = DEFAULT_DEPTH;
        numberOfWalks = DEFAULT_NUMBER_OF_WALKS;
        resourcesDirectory = null;
        rdf2VecInstance = null;
        walkGenerationMode = null;
        isVectorTextFileGeneration = true;
        isOnlyTraining = false;
        isEmbedText = false;
        Gensim.shutDown();
    }
}
