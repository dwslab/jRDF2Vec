package walkGenerators.evaluation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Available Test Cases
 */
public enum TestCase {
    AAUP, CITIES, FORBES, METACRITIC_ALBUMS, METACRITIC_MOVIES;

    private static Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

    /**
     * Returns the relative file path of the data files.
     * @return Relative file path as String.
     */
    public String getFilePath() {
        String basePath = "./data/";
        switch (this) {
            case AAUP:
                return basePath + "AAUP.tsv";
            case CITIES:
                return basePath + "Cities.tsv";
            case FORBES:
                return basePath + "Forbes.tsv";
            case METACRITIC_ALBUMS:
                return basePath + "MetacriticAlbums.tsv";
            case METACRITIC_MOVIES:
                return basePath + "MetacriticMovies.tsv";
            default:
                return "UNDEFINED";
        }
    }

    /**
     * Returns the test file.
     *
     * @return File of test case (taken from <a href="https://github.com/mariaangelapellegrino/Evaluation-Framework">Evaluation Framework</a>.
     */
    public File getFile() {
        return new File(getFilePath());
    }

    public HashSet<String> getDBpediaUris(){
        return getUris("DBpedia_URI15");
    }

    public HashSet<String> getWikidataUris(){
        return getUris("Wikidata_URI15");
    }

    public HashSet<String> getUris(String columnName) {
        HashSet<String> result = new HashSet<>();
        CSVParser parser = null;
        try {
            parser = CSVParser.parse(getFile(), Charset.forName("UTF8"), CSVFormat.TDF.withHeader());
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Could not initialize CSV parser. Returning empty set.");
            return result;
        }

        for (CSVRecord csvRecord : parser) {
            try {
                result.add(csvRecord.get(columnName));
            } catch (IllegalArgumentException iae){
                if(columnName.startsWith("\uFEFF")) {
                    LOGGER.warn("Problem in file: " + getFilePath(), iae);
                } else {
                    LOGGER.warn("Problem in file: " + getFilePath() + "\nTry again with byte order mark.");
                    return getUris("\uFEFFWikidata_URI15");
                }
            }
        }
        return result;
    }


    /**
     * Writes the given set to a file. The file will be UTF-8 encoded.
     * @param set Set to write.
     * @param filePath Path to the file to be written.
     */
    private static void writeSetToFile(Set set, String filePath){
        writeSetToFile(set, new File(filePath));
    }

    /**
     * Writes the given set to a file. The file will be UTF-8 encoded.
     * @param set Set to write.
     * @param fileToWrite File to be written.
     */
    private static void writeSetToFile(Set set, File fileToWrite){
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite), "UTF-8"));
            set.stream().forEach(x -> {
                try {
                    writer.write(x.toString() + "\n");
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            });
            LOGGER.info("Writing file " + fileToWrite);
            writer.flush();
            writer.close();
        } catch (IOException ioe){
            LOGGER.error("Problem writing file " + fileToWrite);
            ioe.printStackTrace();
        }
    }

    /**
     * Writes out all entities of the test cases in the directory {@code ./entities}.
     */
    public static void writeAllEntitiesToFiles(){
        File directory = new File("./entities/");
        if(!directory.exists()) directory.mkdir();
        for(TestCase testCase : TestCase.values()){
            writeSetToFile(testCase.getDBpediaUris(), new File(directory, "" + testCase + "_DBpedia_entities.txt"));
            writeSetToFile(testCase.getWikidataUris(), new File(directory, "" + testCase + "_wikidata_entities.txt"));
        }
    }

    /**
     * Writes the test case entities to a file.
     * @param args No args required.
     */
    public static void main(String[] args) {
        writeAllEntitiesToFiles();
    }

}
