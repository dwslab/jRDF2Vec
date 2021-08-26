package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Simple class providing the service of removing the tags in a vector txt file.
 * If you want to additionally shorten the file with a positive entity list, use class {@link VectorFileReducer}.
 */
public class TagRemover {


    private static final Logger LOGGER = LoggerFactory.getLogger(TagRemover.class);

    public static void removeTagsWriteNewFile(String vectorFilePath, String fileToWritePath) {
        removeTagsWriteNewFile(new File(vectorFilePath), new File(fileToWritePath));
    }

    public static void removeTagsWriteNewFile(File vectorFile, File fileToWrite) {
        if (vectorFile.getName().endsWith(".txt")) {
            try (
                    BufferedWriter writer =
                            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite),
                                    StandardCharsets.UTF_8));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vectorFile),
                            StandardCharsets.UTF_8))
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(removeTagsFromVectorLine(line));
                }
            } catch (FileNotFoundException fnfe) {
                LOGGER.error("File not found exception. ABORTING program.", fnfe);
            } catch (IOException e) {
                LOGGER.error("An IOException occurred. ABORTING program.", e);
            }
        } else {
            System.out.println("Currently only TXT files are supported for tag removal.");
        }
    }

    /**
     * Removes the tags from the first concept in the given line.
     * @param line The line to be edited.
     * @return Edited line. For example:
     * {@code <concept> 1.0 2.0 3.0} will be transformed to {@code concept 1.0 2.0 3.0}
     */
    public static String removeTagsFromVectorLine(String line){
        String[] tokens = line.split(" ");
        if(tokens.length > 0){
            StringBuilder sb = new StringBuilder();
            String conceptToken = tokens[0];
            String tokenToWrite = conceptToken;
            if (conceptToken.startsWith("<") && conceptToken.endsWith(">")) {
                tokenToWrite = conceptToken.substring(1, conceptToken.length() - 1);
            }
            sb.append(tokenToWrite);
            for (int i = 1; i < tokens.length; i++) {
                sb.append(" ").append(tokens[i]);
            }
            if(!tokens[tokens.length-1].endsWith("\n")) {
                sb.append("\n");
            }
            return sb.toString();
        } else {
            return line;
        }
    }

}
