package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.light;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.EntitySelector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

/**
 * Selector which determines for which entities walks shall be generated.
 */
public class LightEntitySelector implements EntitySelector {


    /**
     * The entities for which walks will be generated.
     */
    public HashSet<String> entitiesToProcess;

    /**
     * The file from which the entities will be read.
     */
    public File entityFile;

    /**
     * Constructor
     * @param pathToEntityFile The path to the file which contains the entities for which walks shall be generated. The file must be UTF-8
     *                         encoded.
     */
    public LightEntitySelector(String pathToEntityFile){
        this.entityFile = new File(pathToEntityFile);
    }

    /**
     * Constructor
     *
     * @param entityFile The file which contains the entities for which walks shall be generated. The file must be UTF-8
     *                   encoded.
     */
    public LightEntitySelector(File entityFile) {
        this.entityFile = entityFile;
    }

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LightEntitySelector.class);

    /**
     * Reads the entities in the specified file into a HashSet.
     *
     * @param pathToEntityFile The file to be read from. The file must be UTF-8 encoded.
     * @return A HashSet of entities.
     */
    public static HashSet<String> readEntitiesFromFile(String pathToEntityFile) {
        return readEntitiesFromFile(new File(pathToEntityFile));
    }

    /**
     * Reads the entities in the specified file into a HashSet.
     *
     * @param entityFile The file to be read from. The file must be UTF-8 encoded.
     * @return A HashSet of entities.
     */
    public static HashSet<String> readEntitiesFromFile(File entityFile) {
        HashSet<String> result = new HashSet<>();
        if(!entityFile.exists()){
            LOGGER.error("The specified entity file does not exist: " + entityFile.getName() + "\nProgram will fail.");
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entityFile), StandardCharsets.UTF_8));
            String readLine = "";
            while((readLine = reader.readLine()) != null){
                result.add(readLine);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file.", e);
        }
        LOGGER.info("Number of read entities: " + result.size());
        return result;
    }

    /**
     * Constructor
     *
     * @param entitiesToProcess The entities for which walks will be performed.
     */
    public LightEntitySelector(HashSet<String> entitiesToProcess) {
        this.entitiesToProcess = entitiesToProcess;
    }

    @Override
    public HashSet<String> getEntities() {
        if(this.entitiesToProcess == null) {
            this.entitiesToProcess = readEntitiesFromFile(this.entityFile);
        }
        return this.entitiesToProcess;
    }
}
