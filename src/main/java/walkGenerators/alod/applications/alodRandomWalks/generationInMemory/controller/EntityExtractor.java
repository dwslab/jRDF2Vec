package walkGenerators.alod.applications.alodRandomWalks.generationInMemory.controller;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class EntityExtractor {

    public static void main(String[] args) {
        writeConceptsToFile("./output/classic_shortened_with_confidence.txt", "classic_all_concepts.txt");
    }


    public static void writeConceptsToFile(String pathToOptimizedFile, String pathToFileToBeWritten){
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(pathToOptimizedFile));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
            String readLine;
            String[] components;
            HashSet<String> allConcepts = new HashSet<>(1500000);

            while ((readLine = br.readLine()) != null) {
                components = readLine.split("\t");
                if(components.length != 2){
                    System.out.println("Error parsing line (" + readLine +"). Continue.");
                    continue;
                }
                allConcepts.add(components[0]);
                allConcepts.add(components[1]);
            } // end of while loop over file

            System.out.println("Writing to file...");
            writeSetToFileInOutputDirectory(allConcepts, pathToFileToBeWritten);
            System.out.println(allConcepts.size() + " concepts written.");
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * Writes the given set to a file in {@code ./output/<filename>}.
     * @param set Set to write.
     * @param fileName Filename (not path).
     */
    private static void writeSetToFileInOutputDirectory(Set set, String fileName){
        File outputDirectory = new File("./output");
        if(!outputDirectory.exists()){
            outputDirectory.mkdir();
            System.out.println("Directory 'output' created.");
        }
        if(!fileName.endsWith(".txt")){
            fileName = fileName + ".txt";
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("./output/" + fileName)));
            set.stream().forEach(x -> {
                try {
                    writer.write(x.toString() + "\n");
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            });
            System.out.println("Writing file " + fileName);
            writer.flush();
            writer.close();
        } catch (IOException ioe){
            System.out.println("Problem writing file " + fileName);
            ioe.printStackTrace();
        }
    }

}
