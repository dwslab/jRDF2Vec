import walkGenerators.BabelNetWalkGenerator;
import walkGenerators.DbnaryWalkGenerator;
import walkGenerators.WordNetWalkGenerator;
import walkGenerators.alod.applications.alodRandomWalks.generationInMemory.controller.WalkGeneratorClassicWalks;

/**
 * Mini command line tool for server application.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help")) {
            System.out.println(getHelp());
            return;
        }

        String dataSet = getValue("-set", args);
        if (dataSet == null) {
            System.out.println("-set <set> not found. Aborting.");
            return;
        }

        String threadsWritten = getValue("-threads", args);
        if (threadsWritten == null) {
            System.out.println("-threads <number_of_threads> not found. Aborting.");
            return;
        }
        int numberOfThreads = Integer.valueOf(threadsWritten);

        String walksWritten = getValue("-walks", args);
        if (threadsWritten == null) {
            System.out.println("-walks <number_of_walks> not found. Aborting.");
            return;
        }
        int numberOfWalks = Integer.valueOf(walksWritten);

        String depthWritten = getValue("-depth", args);
        if (depthWritten == null) {
            System.out.println("-depth <sentence_length> not found. Aborting.");
            return;
        }
        int depth = Integer.valueOf(depthWritten);

        String fileToWrite = getValue("-file", args);

        String generationModeString = getValue("-duplicateFree", args);
        boolean isDuplicateFree = true;
        if (generationModeString != null && (generationModeString.equalsIgnoreCase("true") || generationModeString.equalsIgnoreCase("false"))) {
            isDuplicateFree = Boolean.valueOf(generationModeString);
        }

        String resourcePath = getValue("-res", args);
        if (resourcePath == null) {
            System.out.println("ERROR: You have not defined the resource path (parameter '-res <resource path>'). " +
                    "However, this is a required parameter." +
                    "The program execution will stop now.");
        }

        String language = getValue("-en", args);
        boolean isEnglishOnly = true;
        if (language != null) {
            if (language != null && (language.equalsIgnoreCase("true") || language.equalsIgnoreCase("false"))) {
                isEnglishOnly = Boolean.valueOf(language);
            }
        }

        // print configuration for verification
        System.out.println(getConfiguration(dataSet, isDuplicateFree, numberOfThreads, numberOfWalks, depth, fileToWrite, isEnglishOnly));

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
        String result = "Generating walks for " + dataSet + " with the follwoing configuration:\n" +
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
