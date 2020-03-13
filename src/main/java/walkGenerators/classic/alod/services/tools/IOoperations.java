package walkGenerators.classic.alod.services.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class IOoperations {


    private static Logger LOG = LoggerFactory.getLogger(IOoperations.class);


    /**
     * Prints overlapping parts of two string arrays.
     * @param <T> Type of iterable.
     * @param s1 Array 1.
     * @param s2 Array 2.
     */
    public static <T> void printOverlapOfSet (Iterable<T> s1, Iterable<T> s2){
        Set<T> set1 = new HashSet<>();
        Set<T> set2 = new HashSet<>();
        Set<T> result = new HashSet<>();

        s1.forEach(set1::add);
        s2.forEach(set2::add);

        set1.forEach(x -> {
            if(set2.contains(x)){
                result.add(x);
            }
        });
        result.stream().forEach(System.out::println);
    }


    /**
     * Prints the content of a HashMap
     * @param hashMapToPrint HashMap which shall be printed.
     * @param <K> Key Type.
     * @param <V> Value Type.
     */
    public static <K,V> void printHashMap(HashMap<K,V> hashMapToPrint){
        hashMapToPrint.forEach( (x,y) -> {
            System.out.println(x.toString() + "   " + y.toString());
        });
    }


    /**
     * Reads a tab separated file.
     * @param file File to read.
     * @return ArrayList with String[].
     */
    public static ArrayList<String[]> readTabSeparatedFile(File file){
        ArrayList<String[]> result = new ArrayList<>();

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String readLine;

            while((readLine = reader.readLine()) != null){
                result.add(readLine.split("\t"));
            }

            reader.close();

        }catch(IOException e){
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Writes the given set to a file in {@code ./output/<filename>}.
     * @param set Set to write.
     * @param fileName Filename (not path).
     */
    public static void writeSetToFileInOutputDirectory(Set set, String fileName){
        File outputDirectory = new File("./output");
        if(!outputDirectory.exists()){
            outputDirectory.mkdir();
            LOG.debug("Directory 'output' created.");
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
            LOG.info("Writing file " + fileName);
            writer.flush();
            writer.close();
        } catch (IOException ioe){
            LOG.error("Problem writing file " + fileName);
            ioe.printStackTrace();
        }
    }

    /**
     * A very simple file writer.
     *
     * @param file    The file in which shall be written.
     * @param content The content that shall be written.
     */
    public static void writeContentToFile(File file, String content) {
        try {
            LOG.info("Writing File: " + file.getCanonicalPath());
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
            writer.close();
            LOG.info("File successfully written.");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * A simple file writer which writes a file with the specified file name and the specified content into the
     * output directory.
     * @param fileNameWithinOutputDirectory Name of the file within the output directory.
     * @param content Output to write.
     */
    public static void writeContentToFile(String fileNameWithinOutputDirectory, String content) {
        File outputDirectory = new File("./output");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
            LOG.debug("Directory 'output' created.");
        }
        if (!fileNameWithinOutputDirectory.contains(".")) {
            // implicit assumption: file has no file type → add .txt
            fileNameWithinOutputDirectory = fileNameWithinOutputDirectory + ".txt";
        }
        File fileToWrite = new File("./output/" + fileNameWithinOutputDirectory);
        if (fileToWrite.exists()) {
            // file exists → change name to <filename>_2.<fileending>
            String newFileNameWithoutType = fileNameWithinOutputDirectory.substring(0, fileNameWithinOutputDirectory.indexOf('.')) + "_2";
            String fileTypeEnding = fileNameWithinOutputDirectory.substring(fileNameWithinOutputDirectory.indexOf('.'), fileNameWithinOutputDirectory.length());
            String newFileName = "./output/" + newFileNameWithoutType + fileTypeEnding;
            LOG.info("File already exists. Saving file under new name: " + newFileName);
            fileToWrite = new File(newFileName);
            writeContentToFile(fileToWrite, content);
        } else {
            writeContentToFile(fileToWrite, content);
        }
    }

}
