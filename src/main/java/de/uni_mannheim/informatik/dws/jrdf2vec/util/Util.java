package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Static methods providing basic functionality to be used by multiple classes.
 */
public class Util {


    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    /**
     * Helper method. Formats the time delta between {@code before} and {@code after} to a string with human readable
     * time difference in days, hours, minutes, and seconds.
     *
     * @param before Start time instance.
     * @param after  End time instance.
     * @return Human-readable string.
     */
    public static String getDeltaTimeString(Instant before, Instant after) {

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
     * Helper method to obtain the number of read lines.
     *
     * @param file File to be read.
     * @return Number of lines in the file.
     */
    public static int getNumberOfLines(File file) {
        if (file == null) {
            LOGGER.error("The file is null. Cannot count lines.");
            return -1;
        }
        int linesRead = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            while (br.readLine() != null) {
                linesRead++;
            }
        } catch (IOException fnfe) {
            LOGGER.error("Could not get number of lines for file " + file.getAbsolutePath(), fnfe);
        }
        return linesRead;
    }

    /**
     * Obtains the number of lines in a file that are not blanc.
     * @param file The file to be read.
     * @return The number of lines which contain some text (white space and line breaks do not count).
     */
    public static int getNumberOfNonBlancLines(File file){
        if (file == null) {
            LOGGER.error("The file is null. Cannot count lines.");
            return -1;
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                        StandardCharsets.UTF_8))
        ){
            int lineNumber = 0;
            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(!line.equals("")){
                    lineNumber++;
                }
            }
            return lineNumber;
        } catch (FileNotFoundException e) {
            LOGGER.error("FileNotFoundException. Could not complete program.", e);
            return -1;
        } catch (IOException e) {
            LOGGER.error("IOException occurred. Could not complete program.", e);
            return -1;
        }
    }

    /**
     * Given a vector text file, this method determines the dimensionality within the file based on the first valid line.
     *
     * @param vectorTextFilePath Path to the file.
     * @return Dimensionality as int.
     */
    public static int getDimensionalityFromVectorTextFile(String vectorTextFilePath) {
        if (vectorTextFilePath == null) {
            LOGGER.error("The specified file is null.");
            return -1;
        }
        return getDimensionalityFromVectorTextFile(new File(vectorTextFilePath));
    }

    /**
     * Given a vector text file, this method determines the dimensionality within the file based on the first valid line.
     *
     * @param vectorTextFile Vector text file for which dimensionality of containing vectors shall be determined.
     * @return Dimensionality as int.
     */
    public static int getDimensionalityFromVectorTextFile(File vectorTextFile) {
        if (vectorTextFile == null) {
            LOGGER.error("The specified file is null.");
            return -1;
        }
        if (!vectorTextFile.exists()) {
            LOGGER.error("The given file does not exist.");
            return -1;
        }
        int result = -1;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(vectorTextFile));
            String readLine;
            int validationLimit = 3;
            int currentValidationRun = 0;

            while ((readLine = reader.readLine()) != null) {
                if (readLine.trim().equals("") || readLine.trim().equals("\n")) {
                    continue;
                }
                if (currentValidationRun == 0) {
                    result = readLine.split(" ").length - 1;
                }
                int tempResult = readLine.split(" ").length - 1;
                if (tempResult != result) {
                    LOGGER.error("Inconsistency in Dimensionality!");
                }
                currentValidationRun++;
                if (currentValidationRun == validationLimit) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found (exception).", e);
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }
        return result;
    }

    /**
     * Reads an ontology from a given URL.
     *
     * @param path     of ontology to be read.
     * @param language The syntax format of the ontology file such as {@code "TTL"}, {@code "NT"}, or {@code "RDFXML"}.
     * @return Model instance.
     * @throws MalformedURLException Exception for malformed URLs.
     */
    public static OntModel readOntology(String path, Lang language) throws MalformedURLException {
        return readOntology(new File(path), language);
    }

    /**
     * Reads an ontology from a given URL.
     *
     * @param file     of ontology to be read.
     * @param language The syntax format of the ontology file such as {@code "TTL"}, {@code "NT"}, or {@code "RDFXML"}.
     * @return Model instance.
     * @throws MalformedURLException Exception for malformed URLs.
     */
    public static OntModel readOntology(File file, Lang language) throws MalformedURLException {
        URL url = file.toURI().toURL();
        try {
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            model.read(url.toString(), "", language.getName());
            return model;
        } catch (RiotException re) {
            LOGGER.error("Could not parse: " + file.getAbsolutePath() + "\nin jena.", re);
            return null;
        }
    }

    public static List<String> readLinesFromGzippedFile(String filePath) {
        return readLinesFromGzippedFile(new File(filePath));
    }

    /**
     * Reads each line of the gzipped file into a list. The file must be UTF-8 encoded.
     *
     * @param file File to be read from.
     * @return List. Each entry refers to one line in the file.
     */
    public static List<String> readLinesFromGzippedFile(File file) {
        List<String> result = new ArrayList<>();
        if (file == null) {
            LOGGER.error("The file is null. Cannot read from file.");
            return result;
        }
        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
        String readLine;
        try {
            while ((readLine = reader.readLine()) != null) {
                result.add(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }
        try {
            reader.close();
        } catch (IOException e) {
            LOGGER.error("A problem occurred while trying to close the file reader.", e);
        }
        return result;
    }

    /**
     * Checks whether the provided URI points to a file.
     *
     * @param uriToCheck The URI that shall be checked.
     * @return True if the URI is a file, else false.
     */
    public static boolean uriIsFile(URI uriToCheck) {
        if (uriToCheck == null) {
            return false;
        } else {
            return uriToCheck.getScheme().equals("file");
        }
    }

    /**
     * Returns true if the provided directory is a TDB directory, else false.
     *
     * @param directoryToCheck The directory that shall be checked.
     * @return True if TDB directory, else false.
     */
    public static boolean isTdbDirectory(File directoryToCheck) {
        if (directoryToCheck == null || !directoryToCheck.exists() || !directoryToCheck.isDirectory()) {
            return false;
        }
        boolean isDatFileAvailable = false;
        // note: we already checked that directoryToCheck is a directory
        for (File file : directoryToCheck.listFiles()) {
            // we accept the directory as tdb directory if it contains a dat file.
            if (file.getAbsolutePath().endsWith(".dat")) {
                isDatFileAvailable = true;
                break;
            }
        }
        return isDatFileAvailable;
    }

    /**
     * Given a list of walks where a walk is represented as a List of strings, this method will convert that
     * into a list of strings where a walk is one string (and the elements are separated by spaces).
     * The lists are duplicate free.
     *
     * @param dataStructureToConvert The data structure that shall be converted.
     * @return Data structure converted to string list.
     */
    public static List<String> convertToStringWalksDuplicateFree(List<List<String>> dataStructureToConvert) {
        Set<String> uniqueSet = new HashSet<>();
        for (List<String> individualWalk : dataStructureToConvert) {
            StringBuilder walk = new StringBuilder();
            boolean isFirst = true;
            for (String walkComponent : individualWalk) {
                if (isFirst) {
                    isFirst = false;
                    walk.append(walkComponent);
                } else {
                    walk.append(" ").append(walkComponent);
                }
            }
            uniqueSet.add(walk.toString());
        }
        return new ArrayList<>(uniqueSet);
    }

    public static List<String> convertToStringWalks(List<List<Triple>> walks,
                                                    String entity,
                                                    boolean isUnifyAnonymousNodes) {
        List<String> result = new ArrayList<>();
        for (List<Triple> walk : walks) {
            StringBuilder finalSentence = new StringBuilder(entity);
            if (isUnifyAnonymousNodes) {
                for (Triple po : walk) {
                    String object = po.object;
                    if (isAnonymousNode(object)) {
                        object = "ANode";
                    }
                    finalSentence.append(" ")
                            .append(po.predicate)
                            .append(" ")
                            .append(object);
                }
            } else {
                for (Triple po : walk) {
                    finalSentence.append(" ")
                            .append(po.predicate)
                            .append(" ")
                            .append(po.object);
                }
            }
            result.add(finalSentence.toString());
        }
        return result;
    }

    /**
     * Returns true if the given parameter follows the schema of an anonymous node
     *
     * @param uriString The URI string to be checked.
     * @return True if anonymous node.
     */
    public static boolean isAnonymousNode(String uriString) {
        uriString = uriString.trim();
        if (uriString.startsWith("_:")) {
            return true;
        } else return false;
    }

    /**
     * Given a list of walks where a walk is represented as a List of strings, this method will convert that
     * into a list of strings where a walk is one string (and the elements are separated by spaces).
     *
     * @param dataStructureToConvert The data structure that shall be converted.
     * @return Data structure converted to string list.
     */
    public static List<String> convertToStringWalks(List<List<String>> dataStructureToConvert) {
        List<String> result = new ArrayList<>();
        for (List<String> individualWalk : dataStructureToConvert) {
            StringBuilder walk = new StringBuilder();
            boolean isFirst = true;
            for (String walkComponent : individualWalk) {
                if (isFirst) {
                    isFirst = false;
                    walk.append(walkComponent);
                } else {
                    walk.append(" ").append(walkComponent);
                }
            }
            result.add(walk.toString());
        }
        return result;
    }

    /**
     * Draw a random value from a HashSet. This method is thread-safe.
     *
     * @param setToDrawFrom The set from which shall be drawn.
     * @param <T>           Type
     * @return Drawn value of type T.
     */
    public static <T> T randomDrawFromSet(Set<T> setToDrawFrom) {
        int randomNumber = ThreadLocalRandom.current().nextInt(setToDrawFrom.size());
        Iterator<T> iterator = setToDrawFrom.iterator();
        for (int i = 0; i < randomNumber; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Helper function to load files in class path that contain spaces.
     *
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    public static File loadFile(String fileName) {
        try {
            URL fileUrl = Util.class.getClassLoader().getResource(fileName);
            File result;
            if (fileUrl != null) {
                result = FileUtils.toFile(fileUrl.toURI().toURL());
                assertTrue(result.exists(), "Required resource not available.");
                return result;
            } else {
                fail("FileName URL is null.");
                return null;
            }
        } catch (URISyntaxException | MalformedURLException exception) {
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }

    /**
     * Delete the specified file.
     * @param filePath The path of the file to be deleted.
     */
    public static void deleteFile(String filePath) {
        deleteFile(new File(filePath));

    }

    /**
     * Delete the specified file. Print to console in case of issues.
     * @param file The file to be deleted.
     */
    public static void deleteFile(File file){
        if(file == null){
            LOGGER.error("The file is null. Cannot delete file.");
            return;
        }
        if (file.exists()) {
            boolean isSuccess = file.delete();
            if (!isSuccess) {
                LOGGER.warn("Could not delete file: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Reads the entities in the specified file into a HashSet.
     *
     * @param pathToEntityFile The file to be read from. The file must be UTF-8 encoded.
     * @return A HashSet of entities.
     */
    public static Set<String> readEntitiesFromFile(String pathToEntityFile) {
        return readEntitiesFromFile(new File(pathToEntityFile));
    }

    /**
     * Reads the entities in the specified file into a HashSet.
     *
     * @param entityFile The file to be read from. The file must be UTF-8 encoded.
     * @return A HashSet of entities.
     */
    public static Set<String> readEntitiesFromFile(File entityFile) {
        return readEntitiesFromFile(entityFile, false);
    }

    /**
     * Reads the entities in the specified file into a HashSet.
     *
     * @param entityFile The file to be read from. The file must be UTF-8 encoded.
     * @param isRemoveTags If true, surrounding tags are removed when reading the file.
     * @return A HashSet of entities.
     */
    public static Set<String> readEntitiesFromFile(File entityFile, boolean isRemoveTags) {
        HashSet<String> result = new HashSet<>();
        if (!entityFile.exists()) {
            LOGGER.error("The specified entity file does not exist: " + entityFile.getName() + "\nProgram will fail.");
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entityFile),
                    StandardCharsets.UTF_8));
            String readLine = "";
            if(isRemoveTags){
                while ((readLine = reader.readLine()) != null) {

                    if(readLine.startsWith("<") && readLine.endsWith(">")){
                        readLine = readLine.substring(1, readLine.length()-1);
                        result.add(readLine);
                    } else {
                        result.add(readLine);
                    }
                }
            } else {
                while ((readLine = reader.readLine()) != null) {
                    result.add(readLine);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file.", e);
        }
        LOGGER.info("Number of read entities: " + result.size());
        return result;
    }

    /**
     * Helper method to obtain the canonical path of a (test) resource.
     *
     * @param resourceName File/directory name.
     * @return Canonical path of resource.
     */
    public static String getPathOfResource(String resourceName) {
        try {
            URL res = Util.class.getClassLoader().getResource(resourceName);
            if (res == null) throw new IOException();
            File file = Paths.get(res.toURI()).toFile();
            return file.getCanonicalPath();
        } catch (URISyntaxException | IOException ex) {
            LOGGER.info("Cannot create path of resource.", ex);
            return null;
        }
    }

    /**
     * Deletes the stated directory.
     * @param directory Directory that shall be deleted.
     */
    public static void deleteDirectory(File directory) {
        deleteDirectory(directory.getAbsolutePath());
    }

    /**
     * Deletes the directory stated as string path.
     * @param directoryPath The path to the directory which shall be deleted.
     */
    public static void deleteDirectory(String directoryPath) {
        try {
            FileUtils.deleteDirectory(new File(directoryPath));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory '" + directoryPath + "'.", e);
        }
    }

    /**
     * This method will remove a leading less-than and a trailing greater-than sign (tags).
     * @param stringToBeEdited The string that is to be edited.
     * @return String without tags.
     */
    public static String removeTags(String stringToBeEdited){
        if(stringToBeEdited.startsWith("<")) stringToBeEdited = stringToBeEdited.substring(1);
        if(stringToBeEdited.endsWith(">")) stringToBeEdited = stringToBeEdited.substring(0, stringToBeEdited.length() - 1);
        return stringToBeEdited;
    }
}
