import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import training.Word2VecConfiguration;
import walkGenerators.classic.WalkGeneratorDefault;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

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

        if(lightEntityFile == null){
            //System.out.println("RDF2Vec Classic");
            //WalkGeneratorDefault classicGenerator = new WalkGeneratorDefault(knowledgeGraphFile);
            //classicGenerator.generateRandomWalksDuplicateFree(threads, numberOfWalks, depth);
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
            Instant before = Instant.now();
            rdf2VecLight.train();
            Instant after = Instant.now();
            System.out.println("Training completed.");
            System.out.println(getDeltaTimeString(before, after));
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

    /**
     * Helper method. Formats the time delta between {@code before} and {@code after} to a string with human readable
     * time difference in days, hours, minutes, and seconds.
     * @param before Start time instance.
     * @param after End time instance.
     * @return Human-readable string.
     */
    public static String getDeltaTimeString(Instant before, Instant after){

        // unfortunately Java 1.9 which is currently incompatible with coveralls maven plugin...
        //long days = Duration.between(before, after).toDaysPart();
        //long hours = Duration.between(before, after).toHoursPart();
        //long minutes = Duration.between(before, after).toMinutesPart();
        //long seconds = Duration.between(before, after).toSecondsPart();

        Duration delta = Duration.between(before, after);

        long days = delta.toDays();
        long hours = days > 0 ? delta.toHours() % (days * 24) : delta.toHours();


        long minutesModuloPart = days * 24 * 60 + hours * 60;
        long minutes = minutesModuloPart > 0 ? delta.toMinutes() % (minutesModuloPart) : delta.toMinutes();

        long secondsModuloPart = days * 24 * 60 * 60 + hours * 60 * 60 + minutes * 60;
        long seconds = secondsModuloPart > 0 ? TimeUnit.MILLISECONDS.toSeconds(delta.toMillis()) % (secondsModuloPart) : TimeUnit.MILLISECONDS.toSeconds(delta.toMillis());

        String result = "Days: " + days + "\n";
        result += "Hours: " + hours + "\n";
        result += "Minutes: " + minutes + "\n";
        result += "Seconds: " + seconds + "\n";
        return result;
    }


    /**
     * Get the help text on how to use the CLI.
     * @return Help text as String.
     */
    public static String getHelp(){
        return "TODO";
    }

}
