import training.Word2VecConfiguration;
import walkGenerators.base.WalkGenerationMode;
import walkGenerators.base.WalkGeneratorDefault;
import walkGenerators.light.WalkGeneratorLight;

import java.io.File;
import java.time.Instant;

/**
 * Mini command line tool for server application.
 */
public class Main {

    /**
     * word2vec configuration (not just CBOW/SG but contains also all other parameters)
     */
    private static Word2VecConfiguration configuration = Word2VecConfiguration.SG;

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

    private static final int DEFAULT_DEPTH = 4;

    /**
     * Depth for the walks to be generated.
     * Default: 4
     */
    private static int depth = DEFAULT_DEPTH;


    private static final int DEFAULT_NUMBER_OF_WALKS = 100;

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
     * Walk generation mode.
     */
    private static WalkGenerationMode walkGenerationMode = null;

    /**
     * If true, only walks are generated and no embeddings are trained.
     * This can be beneficial when multiple configurations (e.g. SG and CBOW) shall be trained for only one set of walks.
     */
    private static boolean isOnlyWalks = false;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Not enough arguments.");
        }

        if (containsIgnoreCase("-help", args) || containsIgnoreCase("--help", args) || containsIgnoreCase("-h", args)) {
            System.out.println(getHelp());
            return;
        }

        String knowledgeGraphFilePath = getValue("-graph", args);
        if (knowledgeGraphFilePath == null) {
            System.out.println("Required parameter '-graph <kg_file>' not set - program cannot be started. " +
                    "Call '-help' to learn more about the CLI.");
            // stop program execution
            return;
        }
        if (knowledgeGraphFilePath != null) {
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

        isOnlyWalks = containsIgnoreCase("-onlyWalks", args);
        if(!isOnlyWalks) isOnlyWalks = containsIgnoreCase("-walksOnly", args); // allowing a bit more...

        String walkDirectoryPath = getValue("-walkDir", args);
        walkDirectoryPath = (walkDirectoryPath == null) ? getValue("-walkDirectory", args) : walkDirectoryPath;
        if (walkDirectoryPath != null) {
            walkDirectory = new File(walkDirectoryPath);
            if (!walkDirectory.isDirectory()) {
                System.out.println("Walk directory is no directory! Using default.");
                walkDirectory = null;
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
                System.out.println("Could not parse the number of dimensions. Using default.");
                dimensions = 200;
            }
        } else dimensions = 200;
        if(!isOnlyWalks) System.out.println("Using vector dimension: " + dimensions);

        String depthText = getValue("-depth", args);
        if (depthText != null) {
            try {
                depth = Integer.parseInt(depthText);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the depth. Using default.");
                depth = 4;
            }
        } else depth = 4;
        System.out.println("Using depth " + depth);

        String numberOfWalksText = getValue("-numberOfWalks", args);
        numberOfWalksText = (numberOfWalksText == null) ? getValue("-numOfWalks", args) : numberOfWalksText;
        if (numberOfWalksText != null) {
            try {
                numberOfWalks = Integer.parseInt(numberOfWalksText);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse the number of walks. Using default.");
            }
        }

        String resourcesDirectroyPath = getValue("-serverResourcesDir", args);
        if (resourcesDirectroyPath != null) {
            File f = new File(resourcesDirectroyPath);
            if (f.isDirectory()) {
                resourcesDirectory = f;
            } else {
                System.out.println("The specified directory for the python resources is not a directory. Using default.");
            }
        }

        // determining the configuration for the rdf2vec training
        String trainingModeText = getValue("-trainingMode", args);
        trainingModeText = (trainingModeText == null) ? getValue("-trainMode", args) : trainingModeText;
        if (trainingModeText != null) {
            if (trainingModeText.equalsIgnoreCase("sg")) {
                configuration = Word2VecConfiguration.SG;
            } else configuration = Word2VecConfiguration.CBOW;
        } else configuration = Word2VecConfiguration.CBOW;

        // setting training threads
        if (numberOfThreads > 0) configuration.setNumberOfThreads(numberOfThreads);

        // setting dimensions
        if (dimensions > 0) configuration.setVectorDimension(dimensions);


        String walkGenerationModeText = getValue("-walkGenerationMode", args);
        walkGenerationModeText = (walkGenerationModeText == null) ? getValue("-walkMode", args) : walkGenerationModeText;
        if(walkGenerationModeText != null) {
            walkGenerationMode = WalkGenerationMode.getModeFromString(walkGenerationModeText);
        }


        Instant before, after;

        // ------------------
        //     only walks
        // ------------------

        if (isOnlyWalks) {
            System.out.println("Only walks are being generated, no training is performed.");

            String walkFile = "./walks/walk_file.gz";

            // handle the walk directory
            if (walkDirectory == null || !walkDirectory.isDirectory()) {
                System.out.println("walkDirectory is not a directory. Using default: " + walkFile);
            } else {
                walkFile = walkDirectory.getAbsolutePath() + "/walk_file.gz";
            }

            before = Instant.now();

            // now distinguish light/non-light
            if (lightEntityFile != null) {
                // light walk generation:
                WalkGeneratorLight generatorLight = new WalkGeneratorLight(knowledgeGraphFile, lightEntityFile);
                walkGenerationMode = (walkGenerationMode == null) ? WalkGenerationMode.MID_WALKS : walkGenerationMode;
                generatorLight.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalks, depth, walkFile);

            } else {
                // classic walk generation
                WalkGeneratorDefault classicGenerator = new WalkGeneratorDefault(knowledgeGraphFile);
                walkGenerationMode = (walkGenerationMode == null) ? WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE : walkGenerationMode;
                classicGenerator.generateWalks(walkGenerationMode, numberOfThreads, numberOfWalks, depth, walkFile);
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

            // set resource directory for python server files
            if (resourcesDirectory != null) rdf2vec.setPythonServerResourceDirectory(resourcesDirectory);

            rdf2vec.setConfiguration(configuration);
            before = Instant.now();
            rdf2vec.train();
            after = Instant.now();

            // setting the instance to allow for better testability
            rdf2VecInstance = rdf2vec;
        } else {
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

            // setting the walk generation mode
            rdf2VecLight.setWalkGenerationMode(walkGenerationMode);

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


    /**
     * Helper method.
     *
     * @param key       Arg key.
     * @param arguments Arguments as received upon program start.
     * @return Value of argument if existing, else null.
     */
    private static String getValue(String key, String[] arguments) {
        int positionSet = -1;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase(key)) {
                positionSet = i;
                break;
            }
        }
        if (positionSet != -1 && arguments.length >= positionSet + 1) {
            return arguments[positionSet + 1];
        } else return null;
    }

    /**
     * Check whether {@code element} is contained in {@code array}.
     *
     * @param element The element that shall be looked for.
     * @param array   The array in which shall be looked for the element.
     * @return True if {@code element} is contained in {@code array}, else false.
     */
    public static boolean containsIgnoreCase(String element, String[] array) {
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
     * @return Walk Generation Mode.
     */
    public static WalkGenerationMode getWalkGenerationMode() {
        return walkGenerationMode;
    }

    /**
     * Get depth for testing. Not required for operational usage.
     * @return Depth as int.
     */
    public static int getDepth() {
        return depth;
    }

    /**
     * Get the help text on how to use the CLI.
     * Developer note: Also add new commands to the README.
     *
     * @return Help text as String.
     */
    public static String getHelp() {

        return "jRDF2Vec Help\n" +
                "-------------\n\n" +
                "Required Parameters:\n\n" +
                "    -graph <graph_file>\n" +
                "    The file containing the knowledge graph for which you want to generate embeddings.\n\n" +
                "Optional Parameters:\n\n" +
                "    -light <entity_file>\n" +
                "    If you intend to use RDF2Vec Light, you have to use this switch followed by the file path ot the describing the entities for which you require an embedding space. The file should contain one entity (full URI) per line.\n\n" +
                "    -onlyWalks\n" +
                "    If added to the call, this switch will deactivate the training part so that only walks are generated. If training parameters are specified, they are ignored. The walk generation also works with the `-light` parameter.\n\n" +
                "    -threads <number_of_threads> (default: (# of available processors) / 2)\n" +
                "    This parameter allows you to set the number of threads that shall be used for the walk generation as well as for the training.\n\n" +
                "    -dimension <size_of_vector> (default: 200)\n" +
                "    This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).\n\n" +
                "    -depth <depth> (default: 4)\n" +
                "    This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you can also set an odd number. A depth of 1 leads to a sentence in the form <s p o>.\n\n" +
                "    -trainingMode <cbow|sg> (default: sg)\n" +
                "    This parameter controls the mode to be used for the word2vec training. Allowed values are cbow and sg.\n\n" +
                "    -numberOfWalks <number> (default: 100)\n" +
                "    The number of walks to be performed per entity.\n\n" +
                "    -walkGenerationMode <MID_WALKS | MID_WALKS_DUPLICATE_FREE | RANDOM_WALKS | RANDOM_WALKS_DUPLICATE_FREE> (default for light: MID_WALKS, default for classic: RANDOM_WALKS_DUPLICATE_FREE)\n" +
                "    This parameter determines the mode for the walk generation (multiple walk generation algorithms are available). Reasonable defaults are set.\n";
    }

    /**
     * Reset parameters (required for testing)
     */
    public static void reset() {
        configuration = Word2VecConfiguration.CBOW;
        lightEntityFile = null;
        knowledgeGraphFile = null;
        numberOfThreads = -1;
        dimensions = -1;
        depth = DEFAULT_DEPTH;
        numberOfWalks = DEFAULT_NUMBER_OF_WALKS;
        resourcesDirectory = null;
        rdf2VecInstance = null;
        walkGenerationMode = null;
    }

}
