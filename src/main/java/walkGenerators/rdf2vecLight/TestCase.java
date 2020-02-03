package walkGenerators.rdf2vecLight;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;

/**
 * Available Test Cases
 */
public enum TestCase {
    AAUP, CITIES, FORBES, METACRITIC_ALBUMS, METACRITIC_MOVIES;

    private static Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

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

}
