import training.Word2VecConfiguration;

import java.io.File;
import java.time.Instant;

/**
 * Mini command line tool for server application.
 */
public class Main {

    /**
     * word2vec configuration (not just CBOW/SG but contains also all other parameters)
     */
    private static Word2VecConfiguration configuration = Word2VecConfiguration.CBOW;

    /**
     * File for leightweight generation
     */
    private static File lightEntityFile = null;

    /**
     * File to the knowledge graph
     */
    private static File knowledgeGraphFile = null;

    /**
     * The number of threads to be used for the walk generation and for the training.
     */
    private static int threads = -1;

    /**
     * Dimensions for the vectors.
     */
    private static int dimensions = -1;

    /**
     * Depth for the walks to be generated.
     */
    private static int depth = -1;

    /**
     * The number of walks to be generated for each node.
     */
    private static int numberOfWalks = -1;

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

    public static void main(String[] args) {

        if(args.length == 0){
            System.out.println("Not enough arguments.");
        }

        String lightEntityFilePath = getValue("-light", args);
        if(lightEntityFilePath != null){
            lightEntityFile = new File(lightEntityFilePath);
            if(!lightEntityFile.exists()){
                System.out.println("The given file does not exist: " + lightEntityFilePath);
            }
        }

        String knowledgeGraphFilePath = getValue("-graph", args);
        if(knowledgeGraphFilePath == null){
            System.out.println("Required parameter '-graph <kg_file>' not set - program cannot be started. " +
                    "Call '-help' to learn more about the CLI.");
        }
        if(knowledgeGraphFilePath != null){
            knowledgeGraphFile = new File(knowledgeGraphFilePath);
            if(!knowledgeGraphFile.exists()){
                System.out.println("The given file does not exist: " + knowledgeGraphFilePath);
            }
        }

        String walkDirectoryPath = getValue("-walkDir", args);
        walkDirectoryPath = (walkDirectoryPath == null) ? getValue("-walkDirectory", args) : walkDirectoryPath;
        if(walkDirectoryPath != null){
            walkDirectory = new File(walkDirectoryPath);
            if(!walkDirectory.isDirectory()){
                System.out.println("Walk directory is no directory! Using default.");
                walkDirectory = null;
            }
        }

        String threadsText = getValue("-threads", args);
        if(threadsText != null){
            try {
                threads = Integer.parseInt(threadsText);
            } catch (NumberFormatException nfe){
                System.out.println("Could not parse the number of threads. Using default.");
                threads = Runtime.getRuntime().availableProcessors() / 2;
            }
        } else threads = Runtime.getRuntime().availableProcessors() / 2;
        System.out.println("Using " + threads + " threads for walk generation and training.");

        String dimensionText = getValue("-dimension", args);
        dimensionText = (dimensionText == null) ? getValue("-dimensions", args) : dimensionText;
        if(dimensionText != null){
            try {
                dimensions = Integer.parseInt(dimensionText);
            } catch (NumberFormatException nfe){
                System.out.println("Could not parse the number of dimensions. Using default.");
                dimensions = 200;
            }
        } else dimensions = 200;
        System.out.println("Using vector dimension: " + dimensions);

        String depthText = getValue("-depth", args);
        if(depthText != null){
            try {
                depth = Integer.parseInt(depthText);
            } catch (NumberFormatException nfe){
                System.out.println("Could not parse the depth. Using default.");
                depth = 4;
            }
        } else depth = 4;
        System.out.println("Using depth " + depth);

        String numberOfWalksText = getValue("-numberOfWalks", args);
        numberOfWalksText = (numberOfWalksText == null) ? getValue("-numOfWalks", args) : numberOfWalksText;
        if(numberOfWalksText != null){
            try {
                numberOfWalks = Integer.parseInt(numberOfWalksText);
            } catch (NumberFormatException nfe){
                System.out.println("Could not parse the number of walks. Using default.");
            }
        }

        String resourcesDirectroyPath = getValue("-serverResourcesDir", args);
        if(resourcesDirectroyPath != null){
            File f = new File(resourcesDirectroyPath);
            if(f.isDirectory()){
                resourcesDirectory = f;
            } else {
                System.out.println("The specified directory for the python resources is not a directory. Using default.");
            }
        }

        // determining the configuration for the rdf2vec training
        String trainingModeText = getValue("-trainingMode", args);
        trainingModeText = (trainingModeText == null) ? getValue("-trainMode", args) : trainingModeText;
        if(trainingModeText != null){
            if(trainingModeText.equalsIgnoreCase("sg")){
                configuration = Word2VecConfiguration.SG;
            } else configuration = Word2VecConfiguration.CBOW;
        } else configuration = Word2VecConfiguration.CBOW;

        // setting training threads
        if(threads > 0) configuration.setNumberOfThreads(threads);

        // setting dimensions
        if(dimensions > 0) configuration.setVectorDimension(dimensions);


        // ------------------
        //  actual execution
        // ------------------

        Instant before, after;

        if(lightEntityFile == null){
            System.out.println("RDF2Vec Classic");

            RDF2Vec rdf2vec;
            if(walkDirectory == null) rdf2vec = new RDF2Vec(knowledgeGraphFile);
            else rdf2vec = new RDF2Vec(knowledgeGraphFile, walkDirectory);

            // setting threads
            if(threads > 0) rdf2vec.setNumberOfThreads(threads);

            // setting depth
            if(depth > 0) rdf2vec.setDepth(depth);

            // setting the number of walks
            if(numberOfWalks > 0) rdf2vec.setNumberOfWalksPerEntity(numberOfWalks);

            // set resource directory
            if(resourcesDirectory != null) rdf2vec.setResourceDirectory(resourcesDirectory);

            rdf2vec.setConfiguration(configuration);
            before = Instant.now();
            rdf2vec.train();
            after = Instant.now();

            // setting the instance to allow for better testability
            rdf2VecInstance = rdf2vec;
        } else {
            System.out.println("RDF2Vec Light Mode");
            RDF2VecLight rdf2VecLight;
            if(walkDirectory == null) rdf2VecLight = new RDF2VecLight(knowledgeGraphFile, lightEntityFile);
            else rdf2VecLight = new RDF2VecLight(knowledgeGraphFile, lightEntityFile, walkDirectory);

            // setting threads
            if(threads > 0) rdf2VecLight.setNumberOfThreads(threads);

            // setting depth
            if(depth > 0) rdf2VecLight.setDepth(depth);

            // setting the number of walks
            if(numberOfWalks > 0) rdf2VecLight.setNumberOfWalksPerEntity(numberOfWalks);

            // set resource directory
            if(resourcesDirectory != null) rdf2VecLight.setResourceDirectory(resourcesDirectory);

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
     * Get the instance for testing.
     * @return RDF2Vec instance.
     */
    public static IRDF2Vec getRdf2VecInstance() {
        return rdf2VecInstance;
    }

    /**
     * Get the help text on how to use the CLI.
     * Developer note: Also add new commands to the README.
     * @return Help text as String.
     */
    public static String getHelp(){

        return "jRDF2Vec Help\n" +
                "-------------\n\n" +
                "Required Parameters:\n\n"+
                "    -graph <graph_file>\n" +
                "    The file containing the knowledge graph for which you want to generate embeddings.\n\n" +
                "Optional Parameters:\n\n" +
                "    -light <entity_file>\n" +
                "    If you intend to use RDF2Vec Light, you have to use this switch followed by the file path ot the describing the entities for which you require an embedding space. The file should contain one entity (full URI) per line.\n\n" +
                "    -threads <number_of_threads> (default: (# of available processors) / 2)\n" +
                "    This parameter allows you to set the number of threads that shall be used for the walk generation as well as for the training.\n\n" +
                "    -dimension <size_of_vector> (default: 200)\n" +
                "    This parameter allows you to control the size of the resulting vectors (e.g. 100 for 100-dimensional vectors).\n\n" +
                "    -depth <depth> (default: 4)\n" +
                "    This parameter controls the depth of each walk. Depth is defined as the number of hops. Hence, you can also set an odd number. A depth of 1 leads to a sentence in the form <s p o>.\n\n" +
                "    -trainingMode <cbow|sg> (default: cbow)\n" +
                "    This parameter controls the mode to be used for the word2vec training. Allowed values are cbow and sg.\n\n" +
                "    -numberOfWalks <number> (default: 100)\n" +
                "    The number of walks to be performed per entity.\n";
    }

    /**
     * Reset parameters (required for testing)
     */
    public static void reset(){
        configuration = Word2VecConfiguration.CBOW;
        lightEntityFile = null;
        knowledgeGraphFile = null;
        threads = -1;
        dimensions = -1;
        depth = -1;
        numberOfWalks = -1;
        resourcesDirectory = null;
        rdf2VecInstance = null;
    }

}
