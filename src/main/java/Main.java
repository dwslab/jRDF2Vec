import walkGenerators.BabelNetWalkGenerator;
import walkGenerators.DbnaryWalkGenerator;
import walkGenerators.WordNetWalkGenerator;
import walkGenerators.alod.applications.alodRandomWalks.generationInMemory.controller.WalkGeneratorClassicWalks;

import javax.sound.midi.Soundbank;
import java.util.Scanner;

/**
 * Mini command line tool for server application.
 */
public class Main {

    private static String dataSet;
    private static String resourcePath;
    private static boolean isDuplicateFree;
    private static int numberOfThreads;
    private static int numberOfWalks;
    private static int depth;
    private static boolean isEnglishOnly;
    private static String fileToWrite;

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help")) {
            System.out.println(getHelp());
            return;
        }

        if (args.length == 1 &&
                (args[0].equalsIgnoreCase("-guided") || args[0].equalsIgnoreCase("--guided"))) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to the guided walk generation.");

            // data set
            System.out.println("For which data set do you want to generate walks?");
            dataSet = scanner.nextLine();
            if(dataSet.equalsIgnoreCase("alod") || dataSet.equalsIgnoreCase("babelnet") ||
                    dataSet.equalsIgnoreCase("dbpedia") || dataSet.equalsIgnoreCase("wiktionary") ||
                    dataSet.equalsIgnoreCase("wordnet")){
                // input ok
            } else {
                System.out.println("Invalid input. Has to be one of: alod | babelnet | dbpedia | wiktionary | wordnet");
                System.out.println("Please refer to -help for the documentation.");
                return;
            }

            // resource path
            System.out.println("Where do the resources reside?");
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

            if(dataSet.equalsIgnoreCase("babelnet")){
                System.out.println("Do you only want to generate walks for English babelnet lemmas? [true | false]");
                isEnglishOnly = scanner.nextBoolean();
            }

        } else {

            dataSet = getValue("-set", args);
            if (dataSet == null) {
                System.out.println("-set <set> not found. Aborting.");
                return;
            }

            String threadsWritten = getValue("-threads", args);
            if (threadsWritten == null) {
                System.out.println("-threads <number_of_threads> not found. Aborting.");
                return;
            }
            numberOfThreads = Integer.valueOf(threadsWritten);

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

            String isEnglishOnlyWritten = getValue("-en", args);
            isEnglishOnly = true;
            if (isEnglishOnlyWritten != null) {
                if (isEnglishOnlyWritten != null && (isEnglishOnlyWritten.equalsIgnoreCase("true") || isEnglishOnlyWritten.equalsIgnoreCase("false"))) {
                    isEnglishOnly = Boolean.valueOf(isEnglishOnlyWritten);
                }
            }
        }

        // print configuration for verification
        System.out.println(getConfiguration(dataSet, isDuplicateFree, numberOfThreads, numberOfWalks, depth, fileToWrite, isEnglishOnly));
        System.out.println("\nYour quick configuration for next time:");
        System.out.println(getQuickConfiguration());
        System.out.println();

        switch (dataSet.toLowerCase()) {
            case "babelnet":
                BabelNetWalkGenerator generator = new BabelNetWalkGenerator(resourcePath, isEnglishOnly);
                if (fileToWrite != null) {
                    if (isDuplicateFree) {
                        generator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileToWrite);
                    } else {
                        generator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileToWrite);
                    }
                } else { // the file to be written is null
                    if (isDuplicateFree) {
                        generator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
                    } else {
                        generator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
                    }
                }
                break;
            case "wordnet":
                WordNetWalkGenerator wordNetWalkGenerator = new WordNetWalkGenerator(resourcePath);
                if (isDuplicateFree) {
                    if (fileToWrite != null) {
                        wordNetWalkGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
                    } else
                        wordNetWalkGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileToWrite);
                } else {
                    if (fileToWrite != null) {
                        wordNetWalkGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
                    } else wordNetWalkGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileToWrite);
                }
                break;
            case "wiktionary":
                DbnaryWalkGenerator wiktionaryGenerator = new DbnaryWalkGenerator(resourcePath);
                if (isDuplicateFree) {
                    if (fileToWrite != null) {
                        wiktionaryGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
                    } else
                        wiktionaryGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileToWrite);
                } else {
                    if (fileToWrite != null) {
                        wiktionaryGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
                    } else wiktionaryGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileToWrite);
                }
                break;
            case "alod":
                WalkGeneratorClassicWalks alodGenerator = new WalkGeneratorClassicWalks();
                alodGenerator.load(resourcePath);
                if (isDuplicateFree) {
                    if (fileToWrite != null) {
                        alodGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
                    } else
                        alodGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileToWrite);
                } else {
                    if (fileToWrite != null) {
                        alodGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
                    } else alodGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileToWrite);
                }
                break;
        }
        System.out.println("DONE");
    }


    /**
     * Prints the current configuration.
     *
     * @param dataSet
     * @param isDuplicateFree
     * @param numberOfThreads
     * @param numberOfWalks
     * @param depth
     * @param fileToWrite
     */
    private static String getConfiguration(String dataSet, boolean isDuplicateFree, int numberOfThreads, int numberOfWalks, int depth, String fileToWrite, boolean isEnglishOnly) {
        String result = "Generating walks for " + dataSet + " with the following configuration:\n" +
                "- dulplicate free walk generation: " + isDuplicateFree + "\n" +
                "- walks per entity: " + numberOfWalks + "\n" +
                "- depth of each walk: " + depth + "\n";

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
        if(fileToWrite != null) {
            result += " -file \"" + fileToWrite + "\"";
        }
        if(dataSet.equalsIgnoreCase("babelnet")){
            result += " -en " + isEnglishOnly;
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
     * Returns a help string.
     *
     * @return
     */
    private static String getHelp() {
        String result = "The following settings are required:\n\n" +
                "-set <set>\n" +
                "The kind of data set.\n" +
                "Options for <set>\n" +
                "\talod\n" +
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
                "The following settings are optional:\n\n" +
                "-en <bool>\n" +
                "Required only for babelnet. Indicator whether only English lemmas shall be used for the walk generation.\n" +
                "Values for <bool>\n" +
                "\ttrue\n" +
                "\tfalse\n\n" +
                "-duplicateFree <bool>\n" +
                "Indicator whether the walks shall be duplicate free or not.\n" +
                "Values for <bool>\n" +
                "\ttrue\n" +
                "\tfalse\n\n";
        return result;
    }


}
