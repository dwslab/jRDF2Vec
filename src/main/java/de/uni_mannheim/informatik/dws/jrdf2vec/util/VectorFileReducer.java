package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Conceptually similar to {@link TagRemover} but additionally handles positive entity list (a.k.a. light option,
 * reduce option). For reasons of code simplicity, those have been split in two classes.
 */
public class VectorFileReducer {


    private static Logger LOGGER = LoggerFactory.getLogger(VectorFileReducer.class);

    public static void writeReducedTextVectorFile(String textVectorPath, String fileToWritePath, String entityPath) {
        writeReducedTextVectorFile(textVectorPath, fileToWritePath, entityPath, false);
    }

    /**
     * Given a text vector file, only the vectors for the entities in {@code entityFile} are transferred
     *
     * @param textVectorPath  Path to a text vector file.
     * @param fileToWritePath The file that will be written.
     * @param entityPath      The vocabulary that shall appear in the text file.
     *                        The file must contain one word per line. The contents must be a subset of the vocabulary.
     */
    public static void writeReducedTextVectorFile(String textVectorPath, String fileToWritePath, String entityPath,
                                                  boolean isRemoveTags) {
        if (entityPath == null) {
            LOGGER.error("You must provide an entityFile. Aborting program.");
            return;
        }
        if (textVectorPath == null) {
            LOGGER.error("You must provide a textVectorPath. Aborting program.");
            return;
        }
        if (fileToWritePath == null) {
            LOGGER.error("You must provide a fileToWritePath. Aborting program.");
            return;
        }
        File textVectorFile = new File(textVectorPath);

        if (!textVectorFile.exists() || !textVectorFile.isFile()) {
            LOGGER.error("The text vector file does not exist or is no file. Aborting  program.");
            return;
        }

        File entityFile = new File(entityPath);
        if (!entityFile.exists() || !entityFile.isFile()) {
            LOGGER.error("The entity file does not exist or is no file. Aborting  program.");
            return;
        }

        File fileToWrite = new File(fileToWritePath);
        try (
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite),
                        StandardCharsets.UTF_8));
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textVectorFile),
                        StandardCharsets.UTF_8))
        ) {
            Set<String> entities = Util.readEntitiesFromFile(entityFile, isRemoveTags);
            String readLine;
            if (isRemoveTags) {
                while ((readLine = reader.readLine()) != null) {
                    String concept = Util.removeTags(readLine.split(" ")[0]);
                    if (entities.contains(concept)) {
                        writer.write(TagRemover.removeTagsFromVectorLine(readLine));
                    }
                }
            } else {
                while ((readLine = reader.readLine()) != null) {
                    String concept = readLine.split(" ")[0];
                    if (entities.contains(concept)) {
                        writer.write(readLine + "\n");
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while trying to write the file.", e);
        }
    }

}
