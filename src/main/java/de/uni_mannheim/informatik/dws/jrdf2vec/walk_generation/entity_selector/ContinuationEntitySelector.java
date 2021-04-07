package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * The ContinuationEntitySelector is required if the previous walk generation has been stopped after some time.
 * In order to not restart the walk generation process, walks will only be created for entities for which walks have
 * not yet been generated. This process does not work for mid walks.
 */
public class ContinuationEntitySelector implements EntitySelector {


    private static final Logger LOGGER = LoggerFactory.getLogger(ContinuationEntitySelector.class);

    /**
     * Main Constructor
     * @param existingWalkDirectory The walk directory where existing walks reside.
     * @param newWalkDirectory The new walk directory.
     * @param actualEntitySelector The actual entity selector that is to be used.
     */
    public ContinuationEntitySelector(File existingWalkDirectory,
                                      File newWalkDirectory,
                                      EntitySelector actualEntitySelector){
        this.actualEntitySelector = actualEntitySelector;
        this.existingWalkDirectory = existingWalkDirectory;
        this.newWalkDirectory = newWalkDirectory;
    }

    File existingWalkDirectory;
    File newWalkDirectory;
    EntitySelector actualEntitySelector;

    @Override
    public Set<String> getEntities() {
        Set<String> entities = actualEntitySelector.getEntities();
        if(existingWalkDirectory == null){
            LOGGER.error("The provided walk directory does not contain any walks. Continuation will not be applied " +
                    "(new walks will be generated for all entities).");
            return entities;
        }
        if(!existingWalkDirectory.exists()){
            LOGGER.error("The provided walk directory does not exist. Continuation will not be applied "+
                    "(new walks will be generated for all entities).");
            return entities;
        }
        if(!existingWalkDirectory.isDirectory()){
            LOGGER.error("The provided walk directory is not a directory. Continuation will not be applied "+
                    "(new walks will be generated for all entities).");
            return entities;
        }
        if(!newWalkDirectory.exists()){
            if(newWalkDirectory.mkdirs()){
                LOGGER.info("Created new walk directory: " + newWalkDirectory.getAbsolutePath());
            } else {
                LOGGER.error("Could not create new walk directory: " + newWalkDirectory.getAbsolutePath());
            }
        }

        Set<String> existingEntities = new HashSet<>();
        for(File file : existingWalkDirectory.listFiles()){
            if(!file.getName().endsWith(".gz")){
                LOGGER.info("Skipping file '" + file.getName() + "' (no .gz file).");
                continue;
            }
            try {
                GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
                BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
                String line;
                while((line = reader.readLine()) != null){
                    String[] tokens = line.split(" ");
                    if(tokens.length > 0){
                        existingEntities.add(tokens[0]);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("An exception occurred while reading walk file '" + file.getName() + "'. Continue with " +
                        "next file.");
                continue;
            }
            // we could successfully read the file, now let's copy it to the new walk directory
            Path originalFile = Paths.get(file.getAbsolutePath());
            Path newFile = Paths.get(new File(newWalkDirectory,
                    changeFilePathForCopy(file.getName())).getAbsolutePath());
            try {
                LOGGER.info("Copy file " + newFile.toString() + " to walk directory.");
                Files.copy(originalFile, newFile);
                LOGGER.info("Copy operation completed.");
            } catch (IOException e) {
                LOGGER.error("Could not copy file '" + file.getAbsolutePath() + "' to directory '" +
                        newWalkDirectory.getAbsolutePath() + "'", e);
            }
        }

        LOGGER.info("Walks already generated for " + existingEntities.size() + " entities.");
        LOGGER.info("Entities before: " + entities.size());
        entities.removeAll(existingEntities);
        LOGGER.info("Entities after removing existing ones: " + entities.size());
        return entities;
    }

    /**
     * Changes the provided file name (suffix {@code copied}) so that copied and newly generated walk files can be
     * distinguished.
     * @param name File name.
     * @return New file name.
     */
    public static String changeFilePathForCopy(String name){
        if(name.endsWith(".txt.gz")) {
            return name.substring(0, name.length() - 7) + "_copied.txt.gz";
        } else {
            return name.substring(0, name.length() - 3) + "_copied.txt.gz";
        }
    }

}
