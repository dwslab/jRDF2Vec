package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.FileImageInputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Simple class providing the service of removing the tags in a vector txt file.
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
                    String[] tokens = line.split(" ");
                    if (tokens.length > 0) {
                        String conceptToken = tokens[0];
                        String tokenToWrite = conceptToken;
                        if (conceptToken.startsWith("<") && conceptToken.endsWith(">")) {
                            tokenToWrite = conceptToken.substring(1, conceptToken.length() - 1);
                        }
                        writer.write(tokenToWrite);
                        for (int i = 1; i < tokens.length; i++) {
                            writer.write(" " + tokens[i]);
                        }
                        writer.write("\n");
                    }
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

}
