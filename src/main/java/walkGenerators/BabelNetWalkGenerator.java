package walkGenerators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripts.IsearchCondition;

import java.io.*;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class BabelNetWalkGenerator extends WalkGenerator {

    public static void main(String[] args) {
        // rename cache file before running
        BabelNetWalkGenerator gen = new BabelNetWalkGenerator("C:\\Users\\D060249\\Downloads\\babelnet-3.6-RDFNT", true);
        gen.writeBabelNetEntitiesToFile("C:\\Users\\D060249\\Downloads\\babelnet-3.6-RDFNT", "./cache/babelnet_entities_en.txt", true);
        //gen.generateWalks(80, 100, 8, "./walks/babelnet_en_100_8/babelnet_en_100_8.gz");
    }

    /**
     * Default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(BabelNetWalkGenerator.class);

    /**
     * Directory where resources reside.
     */
    private String pathToNtDirectory;

    /**
     * Indicator whether only English entities shall be used.
     */
    private boolean isEnglishEntitiesOnly;

    private HashSet<String> babelnetEntities;

    /**
     * Constructor
     * @param pathToNtFiles
     */
    public BabelNetWalkGenerator(String pathToNtFiles, boolean isEnglishEntitiesOnly){
        this.babelnetEntities = getBabelNetEntities(pathToNtFiles, isEnglishEntitiesOnly);
        this.parser = new NtParser(this);
        this.pathToNtDirectory = pathToNtFiles;
        this.isEnglishEntitiesOnly = isEnglishEntitiesOnly;

        // set a search skip condition
        parser.setSkipCondition(new IsearchCondition() {
            Pattern pattern = Pattern.compile("\".*\"");
            Pattern glossPattern = Pattern.compile("_Gloss[0-9]"); // _Gloss[0-9]
            Matcher matcher;
            @Override
            public boolean isHit(String input) {
                if(input.trim().startsWith("#")) return true; // just a comment line
                if(input.trim().equals("")) return true; // empty line
                if(input.contains("http://purl.org/dc/terms/license")) return true;
                if(input.contains("http://purl.org/dc/elements/1.1/source")) return true;
                matcher = pattern.matcher(input);
                if(matcher.find()) return true;
                matcher = glossPattern.matcher(input);
                if(matcher.find()) return true;
                return false;
            }
        });
        parser.readNTriplesFilesFromDirectory(pathToNtFiles);
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalks(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/babelnet_walks.gz");
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateWalksForEntities(this.babelnetEntities, numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateDuplicateFreeWalksForEntities(this.babelnetEntities, numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/babelnet_walks.gz");
    }


    /**
     * Retrieve all babelnet lemon:LexicalEntry instances.
     * @param pathToDirectoryOfGzippedTripleDataSets Path to the BabelNet RDF resources.
     * @return All instances in a Hash Set (you need some RAM to do this).
     */
    public HashSet<String> getBabelNetEntities(String pathToDirectoryOfGzippedTripleDataSets, boolean isWriteOnlyEnglishEntities){
        File file;
        if(isWriteOnlyEnglishEntities) {
            file = new File("./cache/babelnet_entities_en.txt");
        }
        else {
            file = new File("./cache/babelnet_entities.txt");
        }
        if (file.exists()) {
            LOGGER.info("Cached file found. Obtaining entities from cache.");
            return readHashSetFromFile(file);
        } else {
            LOGGER.info("Reading entities into memory.");
            return writeBabelNetEntitiesToFile(pathToDirectoryOfGzippedTripleDataSets, file, isWriteOnlyEnglishEntities, true);
        }
    }


    /**
     * This method will write the BabelNet entities that are of type lemon:LexicalEntry to a file.
     * @param pathToDirectoryOfGzippedTripleDataSets Path to the file where the Babelnet RDF resources reside.
     * @param entityFileToBeWritten Path to the file that will be written.
     * @param isWriteOnlyEnglishEntities If true, only the English concepts will be written.
     */
    public void writeBabelNetEntitiesToFile(String pathToDirectoryOfGzippedTripleDataSets, String entityFileToBeWritten, boolean isWriteOnlyEnglishEntities){
        writeBabelNetEntitiesToFile(pathToDirectoryOfGzippedTripleDataSets, entityFileToBeWritten, isWriteOnlyEnglishEntities, false);
    }


    /**
     * This method will write the BabelNet entities that are of type lemon:LexicalEntry to a file.
     * @param pathToDirectoryOfGzippedTripleDataSets Path to the file where the Babelnet RDF resources reside.
     * @param entityFileToBeWritten Path to the file that will be written.
     * @param keepInMemory True if returning hash set shall contain all entities.
     * @return Empty hash set if {@code keepInMemory} is false. Else the lemmas in the hash set. Note that you need a considerable amount of memory.
     */
    private HashSet<String> writeBabelNetEntitiesToFile(String pathToDirectoryOfGzippedTripleDataSets, String entityFileToBeWritten, boolean isWriteOnlyEnglishEntities, boolean keepInMemory){
        return writeBabelNetEntitiesToFile(pathToDirectoryOfGzippedTripleDataSets, new File(entityFileToBeWritten), isWriteOnlyEnglishEntities, keepInMemory);
    }

    /**
     * This method will write the BabelNet entities that are of type lemon:LexicalEntry to a file.
     * @param pathToDirectoryOfGzippedTripleDataSets Path to the file where the Babelnet RDF resources reside.
     * @param entityFileToBeWritten File that will be written.
     * @param keepInMemory True if returning hash set shall contain all entities.
     * @return Empty hash set if {@code keepInMemory} is false. Else the lemmas in the hash set. Note that you need a considerable amount of memory.
     */
    private HashSet<String> writeBabelNetEntitiesToFile(String pathToDirectoryOfGzippedTripleDataSets, File entityFileToBeWritten, boolean isWriteOnlyEnglishEntities, boolean keepInMemory){
        entityFileToBeWritten.getParentFile().mkdirs();
        HashSet<String> result = new HashSet<>();
        String regexGetSubject = "(?<=<)[^<]*(?=>)"; // (?<=<)[^<]*(?=>)
        Pattern pattern = Pattern.compile(regexGetSubject);
        File directoryOfDataSets = new File(pathToDirectoryOfGzippedTripleDataSets);
        int numberOfSubjects = 0;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(entityFileToBeWritten));
            for (File file : directoryOfDataSets.listFiles()) {
                if(!file.getName().endsWith(".gz")){
                    LOGGER.info("Skipping file " + file.getName());
                    continue;
                }
                LOGGER.info("Processing file " + file.getName());
                try {
                    GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
                    String readLine;
                    Matcher matcher;
                    while ((readLine = reader.readLine()) != null) {
                        if(readLine.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && readLine.contains("http://www.lemon-model.net/lemon#LexicalEntry")){
                            matcher = pattern.matcher(readLine);
                            if(!matcher.find()){
                                System.out.println("There is a problem with parsing the following line: " + readLine);
                            } else {
                                String subject = matcher.group(0);

                                if(isWriteOnlyEnglishEntities) {
                                    if (subject.toLowerCase().endsWith("en")) {
                                        subject = shortenUri(subject);
                                        writer.write(subject + "\n");
                                        numberOfSubjects++;
                                        if(keepInMemory){
                                            result.add(subject);
                                        }
                                    }
                                } else {
                                    subject = shortenUri(subject);
                                    writer.write(subject + "\n");
                                    numberOfSubjects++;
                                    if(keepInMemory){
                                        result.add(subject);
                                    }
                                }
                            }
                        }
                    }
                    reader.close();
                    LOGGER.info("File " + file.getName() + " completed.");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    LOGGER.info("Problem while reading file: " + file.getName());
                }
            }
            writer.flush();
            writer.close();
            LOGGER.info("Retrieving Entities completed.\n" + numberOfSubjects + " read.");
        } catch (IOException ioe){
            ioe.printStackTrace();
            LOGGER.error("Problem with writer.");
        } finally {
            return result;
        }
    }


    /*
    @Override
    public String shortenUri(String uri) {
        try {
            uri = uri
                    .replace("http://www.lemon-model.net/lemon#", "lemon:")
                    .replace("http://babelnet.org/rdf/", "bn:")
                    .replace("http://purl.org/dc/", "dc:")
                    .replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
        } catch (OutOfMemoryError ome){
            LOGGER.error("Out of Memory Error", ome);
            LOGGER.error("URI causing the error: " + uri);
        }
        return uri;
    }
    */

    static Pattern replacePattern = Pattern.compile("http://(?:purl\\.org/(dc)/|(b)abel(n)et\\.org/rdf/|www\\.(?:lemon-model\\.net/(lemon)|w3\\.org/1999/02/22-(rdf)-syntax-ns)#)");
    static Matcher replaceMatcher;

    @Override
    public String shortenUri(String uri) {
        try {
            replaceMatcher = replacePattern.matcher(uri);
            return replaceMatcher.replaceAll("$1$2$3$4$5:");
        } catch (OutOfMemoryError ome){
            LOGGER.error("Out of Memory Error", ome);
            LOGGER.error("URI causing the error: " + uri);
            return uri;
        }
    }

    /**
     * Ignore - just for testing.
     * @param uri
     * @return
     */
    public static String shortenUri_2(String uri) {
        try {
        replaceMatcher = replacePattern.matcher(uri);
        return replaceMatcher.replaceAll("$1$2$3$4$5:");
        } catch (OutOfMemoryError ome){
            LOGGER.error("Out of Memory Error", ome);
            LOGGER.error("URI causing the error: " + uri);
            return uri;
        }
    }

}
