package walkGenerators.classic.babelnet;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class BabelnetEntityRetriever {

    public static void main(String[] args) {
        String pathToDirectoryOfGzippedTripleDataSets = "C:\\Users\\D060249\\Downloads\\babelnet-3.6-RDFNT";
        String entityFileToBeWritten = "./cache/babelnet_entities.txt";
        String regexGetSubject = "(?<=<)[^<]*(?=>)"; // (?<=<)[^<]*(?=>)
        Pattern pattern = Pattern.compile(regexGetSubject);
        File directoryOfDataSets = new File(pathToDirectoryOfGzippedTripleDataSets);
        int numberOfSubjects = 0;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(entityFileToBeWritten)));

            for (File file : directoryOfDataSets.listFiles()) {
                if(!file.getName().endsWith(".gz")){
                    System.out.println("Skipping file " + file.getName());
                    continue;
                }
                System.out.println("Processing file " + file.getName());
                try {
                    GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
                    String readLine;
                    while ((readLine = reader.readLine()) != null) {
                        if(readLine.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") && readLine.contains("http://www.lemon-model.net/lemon#LexicalEntry")){
                            Matcher matcher = pattern.matcher(readLine);
                            if(!matcher.find()){
                                System.out.println("There is a problem with parsing the following line: " + readLine);
                            } else {
                                String subject = matcher.group(0);
                                writer.write(subject + "\n");
                                //System.out.println(readLine + " (" + subject + ")"); // comment out for higher performance
                                numberOfSubjects++;
                            }
                        }
                    }
                    reader.close();
                    System.out.println("File " + file.getName() + " completed.");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    System.out.println("Problem while reading file: " + file.getName());
                }
            }
            writer.flush();
            writer.close();
            System.out.println("Retrieving Entities completed.\n" + numberOfSubjects + " read.");
        } catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("Problem with writer.");
        }
    }




}
