package walkGenerators.classic.alod.applications.statistics.controller;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This dump shortener accepts the webisalod-instance file (gzipped) and will write a compressed version
 * containing only hypernymy relations without any meta data.
 * 
 * This program can handle the ALOD Classic as well as the ALOD XL endpoint.
 */
public class InstanceFileShortener {
	
	/**
	 * Shortens the given RDF file by only keeping lines that contain hypernymy relations.
	 * @param fileToReadFrom
	 * @param fileToWriteTo
	 */
    public static void shortenInstanceFile(String fileToReadFrom, String fileToWriteTo) {
        File nquadFile = new File(fileToReadFrom);
        try {
            GZIPInputStream gzipInput = new GZIPInputStream(new FileInputStream(nquadFile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInput));

            GZIPOutputStream gzipOutput = new GZIPOutputStream(new FileOutputStream(new File(fileToWriteTo)));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzipOutput));

            String conceptPatternString = "(?<=concept\\/).*?(?=>)"; // (?<=concept\/).*?(?=>)
            Pattern conceptPattern = Pattern.compile(conceptPatternString);

            String readLine, concept1, concept2;
            long lineCounter = 0;
            while((readLine = reader.readLine()) != null){
                if(readLine.contains("skos/core#broader>")){
                    // parsing concepts
                    Matcher conceptMatcher = conceptPattern.matcher(readLine);
                    conceptMatcher.find();
                    concept1 = conceptMatcher.group().replace("\t", "%20");
                    conceptMatcher.find();
                    concept2 = conceptMatcher.group().replace("\t", "%20");
                    concept1 = removeLeadingAndTrailingLowerScores(concept1);
                    concept2 = removeLeadingAndTrailingLowerScores(concept2);
                    // remove leading and trailing lower scores
                    writer.write(concept1 + "\t" + concept2 + "\n");
                }
                lineCounter++;
                if(lineCounter % 1000000 == 0){
                    System.out.println(lineCounter);
                }
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }


    /**
     * Removes leading an trailing '_'.
     * @param input String to be edited.
     * @return Edited String.
     */
    public static String removeLeadingAndTrailingLowerScores(String input){
        if(input.startsWith("_")){
            input = input.substring(1, input.length());
        }
        if(input.endsWith("_")){
            input = input.substring(0, input.length()-1);
        }
        return input;
    }

}
