package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class ContinuationEntitySelector implements EntitySelector {


    private static final Logger LOGGER = LoggerFactory.getLogger(ContinuationEntitySelector.class);

    /**
     * Main Constructor
     * @param walkDirectory The walk directory where existing walks reside.
     * @param actualEntitySelector The actual entity selector that is to be used.
     */
    public ContinuationEntitySelector(File walkDirectory, EntitySelector actualEntitySelector){
        this.actualEntitySelector = actualEntitySelector;
        this.walkDirectory = walkDirectory;
    }

    File walkDirectory;
    EntitySelector actualEntitySelector;

    @Override
    public Set<String> getEntities() {
        Set<String> entities = actualEntitySelector.getEntities();
        if(walkDirectory == null){
            LOGGER.error("The provided walk directory does not contain any walks. Continuation will not be applied " +
                    "(new walks will be generated for all entities).");
            return entities;
        }
        if(!walkDirectory.exists()){
            LOGGER.error("The provided walk directory does not exist. Continuation will not be applied "+
                    "(new walks will be generated for all entities).");
            return entities;
        }
        if(!walkDirectory.isDirectory()){
            LOGGER.error("The provided walk directory is not a directory. Continuation will not be applied "+
                    "(new walks will be generated for all entities).");
            return entities;
        }

        Set<String> existingEntities = new HashSet<>();
        for(File file : walkDirectory.listFiles()){
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
            }
        }

        entities.removeAll(existingEntities);
        return entities;
    }

}
