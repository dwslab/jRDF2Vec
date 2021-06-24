package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Walk merger: By default, walks are written to various gzipped files.
 * For some applications, a raw (potentially huge) text file is required.
 * This class offers the functionality to generate such a file from walks.
 */
public class WalkMerger {


    private static final Logger LOGGER = LoggerFactory.getLogger(WalkMerger.class);

    public static void mergeWalks(String walkDirectoryPath, String fileToWrite) {
        mergeWalks(new File(walkDirectoryPath), new File(fileToWrite));
    }

    public static void mergeWalks(File walkDirectory, File fileToWrite) {
        if (fileToWrite == null) {
            LOGGER.error("The provided file that shall be written is null. ABORTING program...");
            return;
        }
        File[] files = getFiles(walkDirectory);
        if (files == null) return;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite),
                StandardCharsets.UTF_8))
        ) {
            for (File file : files) {
                if (!file.getAbsolutePath().endsWith(".gz")) {
                    LOGGER.info("Skipping file '" + file.getName() + "'");
                    continue;
                }

                // now let's read the gzipped file and write its contents to our fileToWrite
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             new GZIPInputStream(
                                                     new FileInputStream(file))
                                     )
                             )
                ) {
                    String line;
                    while((line = reader.readLine()) != null){
                        writer.write(line + "\n");
                    }
                } catch (IOException ioe){
                    LOGGER.error("Failed to read file '" + file.getName() + "'. Program will continue.");
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("An IOException occurred. File cannot be written.");
        }
    }

    /**
     * This method performs various sanity checks on the provided {@code walkDirectory}.
     *
     * @param walkDirectory The provided walk directory.
     * @return An array of files in case of success, else null
     */
    private static File[] getFiles(File walkDirectory) {
        if (walkDirectory.listFiles() == null) {
            LOGGER.error("The specified directory for walk merging must not be null. ABORTING program...");
            return null;
        }
        if (!walkDirectory.exists()) {
            LOGGER.error("The provided walk directory '" + walkDirectory.getAbsolutePath() + "' does not exist.\n" +
                    "ABORTING program...");
            return null;
        }
        if (!walkDirectory.isDirectory()) {
            LOGGER.error("The provided walk directory '" + walkDirectory.getAbsolutePath() + "' is not a directory.\n" +
                    "ABORTING program...");
            return null;
        }

        File[] files = walkDirectory.listFiles();

        if (files == null || files.length == 0) {
            LOGGER.error("The provided walk directory '" + walkDirectory.getAbsolutePath() + "' does not contain any " +
                    "files.\nABORTING program...");
            return null;
        }

        return files;
    }
}
