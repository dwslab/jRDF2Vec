package walkGenerators.classic.alod.services.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;


public class IOoperations {

    private static Logger LOGGER = LoggerFactory.getLogger(IOoperations.class);

    /**
     * Writes the given set to a file in {@code ./output/<filename>}.
     * @param set Set to write.
     * @param fileName Filename (not path).
     */
    public static void writeSetToFileInOutputDirectory(Set set, String fileName){
        File outputDirectory = new File("./output");
        if(!outputDirectory.exists()){
            outputDirectory.mkdir();
            LOGGER.debug("Directory 'output' created.");
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
            LOGGER.info("Writing file " + fileName);
            writer.flush();
            writer.close();
        } catch (IOException ioe){
            LOGGER.error("Problem writing file " + fileName);
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
            LOGGER.info("Writing File: " + file.getCanonicalPath());
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
            writer.close();
            LOGGER.info("File successfully written.");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


}
