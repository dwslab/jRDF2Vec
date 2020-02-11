import walkGenerators.classic.DBpedia.DBpediaWalkGenerator;
import walkGenerators.base.IWalkGenerator;
import walkGenerators.classic.DBnary.DbnaryWalkGenerator;
import walkGenerators.classic.WalkGeneratorDefault;
import walkGenerators.classic.alod.applications.alodRandomWalks.generationInMemory.controller.WalkGeneratorClassicWalks;
import walkGenerators.classic.babelnet.BabelNetWalkGenerator;
import walkGenerators.classic.wordnet.WordNetWalkGenerator;
import walkGenerators.light.WalkGeneratorLight;

import java.util.Scanner;

/**
 * Mini command line tool for server application.
 */
public class Main {

    private static String dataSet;

    /**
     * Triple file or directory with triple files.
     */
    private static String resourcePath;
    private static boolean isDuplicateFree;
    private static int numberOfThreads;
    private static int numberOfWalks;
    private static int depth;
    private static boolean isEnglishOnly;
    private static String fileToWrite;
    private static boolean isUnifyAnonymousNodes;

    /**
     * Indicator whether RDF2Vec Light shall be used (only generate walks for certain entities).
     */
    private static boolean isRdf2vecLight;

    /**
     * Path to the file containing the entities for which walks shall be generated. One entity per line. UTF-8 encoding is required for the file.
     */
    private static String rdf2vecLightEntityFile;

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("--h") || args[0].equalsIgnoreCase("-help")) {
            System.out.println(getHelp());
            return;
        }

        if (args.length == 1 &&
                (args[0].equalsIgnoreCase("-guided") || args[0].equalsIgnoreCase("--guided"))) {

            // guided mode

            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to the guided walk generation.");

            // data set
            System.out.println("For which data set do you want to generate walks? [any | alod | babelnet | dbpedia | wiktionary | wordnet] (If in doubt, use 'any'.)");
            dataSet = scanner.nextLine();
            if (dataSet.equalsIgnoreCase("alod") || dataSet.equalsIgnoreCase("any")  || dataSet.equalsIgnoreCase("babelnet") ||
                    dataSet.equalsIgnoreCase("dbpedia") || dataSet.equalsIgnoreCase("wiktionary") ||
                    dataSet.equalsIgnoreCase("wordnet")) {
                // input ok
            } else {
                System.out.println("Invalid input. Has to be one of: any | alod | babelnet | dbpedia | wiktionary | wordnet");
                System.out.println("Please refer to -help for the documentation.");
                return;
            }

            // resource path
            System.out.println("Where does the triple file / the triple directory reside?");
            resourcePath = scanner.nextLine();

            // is duplicate free
            System.out.println("Do you want duplicate free walks? [true|false]");
            isDuplicateFree = scanner.nextBoolean();
            scanner.nextLine();

            // threads
            System.out.println("How many threads shall be used for the walk generation?");
            numberOfThreads = scanner.nextInt();
            scanner.nextLine();

            // walks
            System.out.println("How many walks shall be generated per entity?");
            numberOfWalks = scanner.nextInt();
            scanner.nextLine();

            // depth
            System.out.println("What is the desired maximal depth of each walk?");
            depth = scanner.nextInt();
            scanner.nextLine();

            // file to write
            System.out.println("Into which file shall the walks be written?");
            fileToWrite = scanner.nextLine();

            // anonymous node handling
            if (!dataSet.equalsIgnoreCase("any")) {
                System.out.println("Do you want to unify anonymous nodes? [true | false] (If in doubt, set 'false'.)");
                isUnifyAnonymousNodes = scanner.nextBoolean();
                scanner.nextLine();
            }

            if (dataSet.equalsIgnoreCase("babelnet")) {
                System.out.println("Do you only want to generate walks for English babelnet lemmas? [true | false] (If in doubt, set 'true'.)");
                isEnglishOnly = scanner.nextBoolean();
            }

            if(dataSet.equalsIgnoreCase("any") || dataSet.equalsIgnoreCase("default")){
                System.out.println("Do you want to run RDF2Vec Light? [true|false] (If in doubt, set 'false'.)");
                isRdf2vecLight = scanner.nextBoolean();
                scanner.nextLine();
                if(isRdf2vecLight){
                    System.out.println("You selected RDF2Vec Light. Where is the file containing the entities for which walks shall be generated?");
                    rdf2vecLightEntityFile = scanner.nextLine();
                }
            }

        } else {

            // automatic mode

            dataSet = getValue("-set", args);
            if (dataSet == null) {
                System.out.println("-set <set> not found. Using default.");
            }
            dataSet = "default";

            String threadsWritten = getValue("-threads", args);
            if (threadsWritten == null) {
                System.out.println("-threads <number_of_threads> not found. Using default (12 threads).");
                numberOfThreads = 12;
            } else numberOfThreads = Integer.valueOf(threadsWritten);

            String walksWritten = getValue("-walks", args);
            if (threadsWritten == null) {
                System.out.println("-walks <number_of_walks> not found. Aborting.");
                return;
            }
            numberOfWalks = Integer.valueOf(walksWritten);

            String depthWritten = getValue("-depth", args);
            if (depthWritten == null) {
                System.out.println("-depth <sentence_length> not found. Aborting.");
                return;
            }
            depth = Integer.valueOf(depthWritten);

            fileToWrite = getValue("-file", args);

            String isDuplicateFreeWritten = getValue("-duplicateFree", args);
            isDuplicateFree = true;
            if (isDuplicateFreeWritten != null && (isDuplicateFreeWritten.equalsIgnoreCase("true") || isDuplicateFreeWritten.equalsIgnoreCase("false"))) {
                isDuplicateFree = Boolean.valueOf(isDuplicateFreeWritten);
            }

            resourcePath = getValue("-res", args);
            if (resourcePath == null) {
                System.out.println("ERROR: You have not defined the resource path (parameter '-res <resource path>'). " +
                        "However, this is a required parameter." +
                        "The program execution will stop now.");
            }

            String isUnifyAnonymousNodesWritten = getValue("-unifyAnonymousNodes", args);
            isUnifyAnonymousNodes = false;
            if (isUnifyAnonymousNodesWritten != null && (isUnifyAnonymousNodesWritten.equalsIgnoreCase("true") || isUnifyAnonymousNodesWritten.equalsIgnoreCase("false"))) {
                isUnifyAnonymousNodes = Boolean.valueOf(isUnifyAnonymousNodesWritten);
            }

            String isEnglishOnlyWritten = getValue("-en", args);
            isEnglishOnly = true;
            if (isEnglishOnlyWritten != null) {
                if ((isEnglishOnlyWritten.equalsIgnoreCase("true") || isEnglishOnlyWritten.equalsIgnoreCase("false"))) {
                    isEnglishOnly = Boolean.valueOf(isEnglishOnlyWritten);
                }
            }

            isRdf2vecLight = false;
            String isRdf2vecLightWritten = getValue("-isRdf2vecLight", args);
            if (isRdf2vecLightWritten != null) {
                if ((isRdf2vecLightWritten.equalsIgnoreCase("true") || isRdf2vecLightWritten.equalsIgnoreCase("false"))) {
                    isRdf2vecLight = Boolean.valueOf(isRdf2vecLightWritten);
                }
            }

            rdf2vecLightEntityFile = getValue("-rdf2vecLightEntityFile", args);
            if (rdf2vecLightEntityFile != null) {
                if(rdf2vecLightEntityFile.equalsIgnoreCase("true") || rdf2vecLightEntityFile.equalsIgnoreCase("false")){
                    // given that there is an entity file, assume rdf2vec light = true
                    isRdf2vecLight = true;
                }
            }
        }

        // print configuration for verification
        System.out.println(getConfiguration());
        System.out.println("\nYour quick configuration for next time:");
        System.out.println(getQuickConfiguration());
        System.out.println();

        switch (dataSet.toLowerCase()) {
            case "any":
            case "default":
                if(isRdf2vecLight) {
                    WalkGeneratorLight lightGenerator = new WalkGeneratorLight(resourcePath, rdf2vecLightEntityFile);
                } else {
                    // default rdf2vec configuration
                    WalkGeneratorDefault classicGenerator = new WalkGeneratorDefault(resourcePath);
                    generatorExecution(classicGenerator);
                }
                break;
            case "babelnet":
                BabelNetWalkGenerator babelnetGenerator = new BabelNetWalkGenerator(resourcePath, isEnglishOnly);
                generatorExecution(babelnetGenerator);
                break;
            case "dbpedia":
                DBpediaWalkGenerator dBpediaWalkGenerator = new DBpediaWalkGenerator(resourcePath);
                generatorExecution(dBpediaWalkGenerator);
                break;
            case "wordnet":
                WordNetWalkGenerator wordNetWalkGenerator = new WordNetWalkGenerator(resourcePath, false, isUnifyAnonymousNodes);
                generatorExecution(wordNetWalkGenerator);
                break;
            case "wiktionary":
            case "dbnary":
                DbnaryWalkGenerator wiktionaryGenerator = new DbnaryWalkGenerator(resourcePath);
                generatorExecution(wiktionaryGenerator);
                break;
            case "alod":
                WalkGeneratorClassicWalks alodGenerator = new WalkGeneratorClassicWalks();
                alodGenerator.load(resourcePath);
                generatorExecution(alodGenerator);
        }
        System.out.println("DONE");
    }

    private static void generatorExecution(IWalkGenerator generator) {
        if (isDuplicateFree) {
            if (fileToWrite != null) {
                generator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileToWrite);
            } else
                generator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
        } else {
            if (fileToWrite != null) {
                generator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileToWrite);
            } else generator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
        }
    }

    /**
     * Prints the current configuration.
     */
    private static String getConfiguration() {
        String result = "Generating walks for " + dataSet + " with the following configuration:\n" +
                "- number of threads: " + numberOfThreads + "\n" +
                "- dulplicate free walk generation: " + isDuplicateFree + "\n" +
                "- walks per entity: " + numberOfWalks + "\n" +
                "- depth of each walk: " + depth + "\n" +
                "- rdf2vec LIGHT: " + isRdf2vecLight + "\n";
        if(isRdf2vecLight){
            result += "- rdf2vec LIGHT entity file: " + rdf2vecLightEntityFile + "\n";
        }

        if (dataSet.equalsIgnoreCase("babelnet")) {
            result += "- create walks for only English nodes: " + isEnglishOnly + "\n";
        }

        if (fileToWrite.equals("")) {
            result += "- writing files to default directory (./walks/)";
        } else {
            result += "- writing walks to directory: " + fileToWrite;
        }
        return result;
    }

    /**
     * Prints the current quick start configuration.
     **/
    private static String getQuickConfiguration() {
        String result = "-set " + dataSet + " -res \"" + resourcePath + "\" -threads " + numberOfThreads + " -walks " + numberOfWalks + " -depth " + depth;
        result += " -duplicateFree " + isDuplicateFree;
        result += " -unifyAnonymousNodes " + isUnifyAnonymousNodes;
        if (fileToWrite != null) {
            result += " -file \"" + fileToWrite + "\"";
        }
        if (dataSet.equalsIgnoreCase("babelnet")) {
            result += " -en " + isEnglishOnly;
        }
        result += " -isRdf2vecLight " + isRdf2vecLight;
        if(isRdf2vecLight) {
            result += " -rdf2vecLightEntityFile \"" + rdf2vecLightEntityFile + "\"";
        }
        return result;
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
     * Returns a help String.
     *
     * @return Help text as String.
     */
    private static String getHelp() {
        String result =
                // required values
                "The following settings are required:\n\n" +
                        "-set <set>\n" +
                        "The kind of data set.\n" +
                        "Options for <set>\n" +
                        "\tany\n" +
                        "\talod\n" +
                        "\tany\n" +
                        "\tbabelnet\n" +
                        "\twordnet\n" +
                        "\twiktionary\n\n" +
                        "-res <resource>\n" +
                        "Path to data set file or directory.\n\n" +
                        "-threads <number_of_threads>\n" +
                        "The number of desired threads.\n\n" +
                        "-walks <number_of_walks_per_entity>\n" +
                        "The number of walks per entity.\n\n" +
                        "-depth <desired_sentence_depth>\n" +
                        "The length of each sentence.\n\n" +
                        "-file <file_to_be_written>\n" +
                        "The path to the file that will be written.\n\n\n" +

                        // optional values
                        "The following settings are optional:\n\n" +

                        "-isRdf2vecLight <bool>\n" +
                        "Indicator whether RDF2Vec LIGHT shall be used. If this is the case, parameter -rdf2vecLightEntityFile MUST be set.\n" +
                        "Values for <bool>\n" +
                        "\ttrue\n" +
                        "\tfalse\n\n" +

                        "-rdf2vecLightEntityFile <path_to_file>\n" +
                        "The path to the entity file containing the entities for which walk shall be generated. the file must contain only one entity per line.\n\n" +

                        "-en <bool>\n" +
                        "Required only for BabelNet. Indicator whether only English lemmas shall be used for the walk generation.\n" +
                        "Values for <bool>\n" +
                        "\ttrue\n" +
                        "\tfalse\n\n" +

                        "-duplicateFree <bool>\n" +
                        "Indicator whether the walks shall be duplicate free or not.\n" +
                        "Values for <bool>\n" +
                        "\ttrue\n" +
                        "\tfalse\n\n" +

                        "-unifyAnonymousNodes <bool>\n" +
                        "Indicator whether anonymous node ids shall be unified or not. Default: False.\n" +
                        "Values for <bool>\n" +
                        "\ttrue\n" +
                        "\tfalse\n\n";
        return result;
    }
}
