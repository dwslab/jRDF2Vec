import walkGenerators.BabelNetWalkGenerator;
import walkGenerators.DbnaryWalkGenerator;
import walkGenerators.WordNetWalkGenerator;
import walkGenerators.alod.applications.alodRandomWalks.generationInMemory.controller.WalkGeneratorClassicWalks;

/**
 * Mini command line tool for server application.
 */
public class Main {

    public static void main(String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help")){
            System.out.println(getHelp());
            return;
        }

        String dataset = getValue("-set", args);
        if(dataset == null){
            System.out.println("-set <set> not found. Aborting.");
            return;
        }

        String threadsWritten = getValue("-threads", args);
        if(threadsWritten == null){
            System.out.println("-threads <number_of_threads> not found. Aborting.");
            return;
        }
        int numberOfThreads = Integer.valueOf(threadsWritten);

        String walksWritten = getValue("-walks", args);
        if(threadsWritten == null){
            System.out.println("-walks <number_of_walks> not found. Aborting.");
            return;
        }
        int numberOfWalks = Integer.valueOf(walksWritten);

        String depthWritten = getValue("-depth", args);
        if(depthWritten == null){
            System.out.println("-depth <sentence_length> not found. Aborting.");
            return;
        }
        int depth = Integer.valueOf(depthWritten);

        String fileWritten = getValue("-file", args);

        String generationModeString = getValue("-duplicateFree", args);
        boolean isDuplicateFree = true;
        if(generationModeString != null && (generationModeString.equalsIgnoreCase("true") || generationModeString.equalsIgnoreCase("false"))){
            isDuplicateFree = Boolean.valueOf(generationModeString);
        }

        String resourcePath = getValue("-res", args);
        if(resourcePath == null){
            System.out.println("ERROR: You have not defined the resource path (parameter '-res <resource path>'). " +
                    "However, this is a required parameter." +
                    "The program execution will stop now.");
        }


        switch (dataset.toLowerCase()) {
            case "babelnet":
                String language = getValue("-en", args);
                    if(language != null){
                        boolean isEnglishOnly = true;
                        if(language != null && (language.equalsIgnoreCase("true") || language.equalsIgnoreCase("false"))){
                            isEnglishOnly = Boolean.valueOf(language);
                        }
                        BabelNetWalkGenerator generator = new BabelNetWalkGenerator(resourcePath, isEnglishOnly);
                        if(fileWritten != null) {
                            if(isDuplicateFree){
                                generator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileWritten);
                            } else {
                                generator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileWritten);
                            }
                                System.out.println("Generating  walks for BabelNet with:\n" +
                                        "with duplicates: " + isDuplicateFree + "\n" +
                                        numberOfWalks + " walks per entity\n" +
                                        "English entities only: " + isEnglishOnly + "\n" +
                                        "depth of " + depth + "\n" +
                                        "Target to be written: " + fileWritten);
                        } else { // the file to be written is null
                            fileWritten = "DEFAULT OPTION (working directory)";
                            if(isDuplicateFree){
                                generator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
                            } else {
                                generator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
                            }
                            System.out.println("Generating  walks for BabelNet with:\n" +
                                    "with duplicates: " + isDuplicateFree + "\n" +
                                    numberOfWalks + " walks per entity\n" +
                                    "English entities only: " + isEnglishOnly + "\n" +
                                    "depth of " + depth + "\n" +
                                    "Target to be written: " + fileWritten);
                        }
                    }
                break;
            case "wordnet":
                    WordNetWalkGenerator wordNetWalkGenerator = new WordNetWalkGenerator(resourcePath);
                    if(isDuplicateFree){
                        if(fileWritten != null) {
                            wordNetWalkGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
                        } else wordNetWalkGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileWritten);
                    } else {
                        if(fileWritten != null) {
                            wordNetWalkGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
                        } else wordNetWalkGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileWritten);
                    }
                    System.out.println("Generating  walks for WordNet with:\n" +
                            "with duplicates: " + isDuplicateFree + "\n" +
                            numberOfWalks + " walks per entity\n" +
                            "depth of " + depth + "\n" +
                            "Target to be written: " + fileWritten);
                break;
            case "wiktionary":
                DbnaryWalkGenerator wiktionaryGenerator = new DbnaryWalkGenerator(resourcePath);
                if(isDuplicateFree){
                    if(fileWritten != null) {
                        wiktionaryGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth);
                    } else wiktionaryGenerator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, fileWritten);
                } else {
                    if(fileWritten != null) {
                        wiktionaryGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth);
                    } else wiktionaryGenerator.generateRandomWalks(numberOfThreads, numberOfWalks, depth, fileWritten);
                }
                System.out.println("Generating  walks for Wiktionary with:\n" +
                        "with duplicates: " + isDuplicateFree + "\n" +
                        numberOfWalks + " walks per entity\n" +
                        "depth of " + depth + "\n" +
                        "Target to be written: " + fileWritten);
                break;
            case "alod":
                // TODO implement
                WalkGeneratorClassicWalks generator = new WalkGeneratorClassicWalks();
                generator.load(resourcePath);
                generator.generateWalksDuplicateFree(fileWritten, numberOfWalks, depth, numberOfThreads);
                break;
        }
        System.out.println("DONE");
    }


    /**
     * Helper method.
     * @param key Arg key.
     * @param arguments Arguments as received upon program start.
     * @return Value of argument if existing, else null.
     */
    private static String getValue(String key, String[] arguments){
        int positionSet = -1;
        for(int i = 0; i < arguments.length; i++){
            if(arguments[i].equalsIgnoreCase(key)) {
                positionSet = i;
                break;
            }
        }
        if(positionSet != -1 && arguments.length >= positionSet + 1){
            return arguments[positionSet +1];
        } else return null;
    }


    /**
     * Returns a help string.
     * @return
     */
    private static String getHelp(){
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
