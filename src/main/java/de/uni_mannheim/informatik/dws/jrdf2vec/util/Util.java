package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Static methods providing basic functionality to be used by multiple classes.
 */
public class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

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
     * Helper method to obtain the number of read lines.
     * @param file File to be read.
     * @return Number of lines in the file.
     */
    public static int getNumberOfLines(File file){
        int linesRead = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while(br.readLine() != null){
                linesRead++;
            }
            br.close();
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        return linesRead;
    }


    /**
     * Given a vector text file, this method determines the dimensionality within the file based on the first valid line.
     * @param vectorTextFilePath Path to the file.
     * @return Dimensionality as int.
     */
    public static int getDimensionalityFromVectorTextFile(String vectorTextFilePath){
        if(vectorTextFilePath == null){
            LOGGER.error("The specified file is null.");
            return -1;
        }
        return getDimensionalityFromVectorTextFile(new File(vectorTextFilePath));
    }

    /**
     * Given a vector text file, this method determines the dimensionality within the file based on the first valid line.
     * @param vectorTextFile Vector text file for which dimensionality of containing vectors shall be determined.
     * @return Dimensionality as int.
     */
    public static int getDimensionalityFromVectorTextFile(File vectorTextFile){
        if(vectorTextFile == null){
            LOGGER.error("The specified file is null.");
            return -1;
        }
        if(!vectorTextFile.exists()){
            LOGGER.error("The given file does not exist.");
            return -1;
        }
        int result = -1;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(vectorTextFile));
            String readLine;
            int validationLimit = 3;
            int currentValidationRun = 0;

            while((readLine = reader.readLine()) != null) {
                if(readLine.trim().equals("") || readLine.trim().equals("\n")){
                    continue;
                }
                if(currentValidationRun == 0){
                    result = readLine.split(" ").length -1;
                }
                int tempResult = readLine.split(" ").length -1;
                if(tempResult != result){
                    LOGGER.error("Inconsistency in Dimensionality!");
                }
                currentValidationRun++;
                if(currentValidationRun == validationLimit){
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

}
