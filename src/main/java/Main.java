

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import training.Word2VecConfiguration;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

/**
 * Mini command line tool for server application.
 */
public class Main {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

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
     * Where the walks will be persisted (directory).
     */
    private static File walkDirectory = null;

    public static void main(String[] args) {

        if(args.length == 0){
            LOGGER.error("Not enough arguments.");
        }

        String lightEntityFilePath = getValue("-light", args);
        if(lightEntityFilePath != null){
            lightEntityFile = new File(lightEntityFilePath);
            if(!lightEntityFile.exists()){
                LOGGER.error("The given file does not exist: " + lightEntityFilePath);
            }
        }

        String knowledgeGraphFilePath = getValue("-graph", args);
        if(knowledgeGraphFilePath != null){
            knowledgeGraphFile = new File(knowledgeGraphFilePath);
            if(!knowledgeGraphFile.exists()){
                LOGGER.error("The given file does not exist: " + knowledgeGraphFilePath);
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
            }
        }

        String dimensionText = getValue("-dimension", args);
        dimensionText = (dimensionText == null) ? getValue("-dimensions", args) : dimensionText;
        if(dimensionText != null){
            try {
                dimensions = Integer.parseInt(dimensionText);
            } catch (NumberFormatException nfe){
                System.out.println("Could not parse the number of dimensions. Using default.");
            }
        }

        String depthText = getValue("-depth", args);
        if(depthText != null){
            try {
                depth = Integer.parseInt(depthText);
            } catch (NumberFormatException nfe){
                System.out.println("Could not parse the depth. Using default.");
            }
        }

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

        if(lightEntityFile == null){
            // TODO run classic
        } else {
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
            Instant before = Instant.now();
            rdf2VecLight.train();
            Instant after = Instant.now();
            long days = Duration.between(before, after).toDaysPart();
            long hours = Duration.between(before, after).toHoursPart();
            long minutesPart = Duration.between(before, after).toMinutesPart();
            long seconds = Duration.between(before, after).toSecondsPart();
            System.out.println("Training completed.");
            System.out.println("Days: " + days);
            System.out.println("Hours: " + hours);
            System.out.println("Minutes: " + minutesPart);
            System.out.println("Seconds: " + seconds);
        }
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


    public static String getHelp(){
        return "TODO";
    }

}
