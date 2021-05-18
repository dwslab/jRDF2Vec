package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.EntitySelector;

import java.io.*;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.readEntitiesFromFile;

/**
 * Selector which determines for which entities walks shall be generated.
 */
public class LightEntitySelector implements EntitySelector {


    /**
     * The entities for which walks will be generated.
     */
    public Set<String> entitiesToProcess;

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
     * Constructor
     *
     * @param entitiesToProcess The entities for which walks will be performed.
     */
    public LightEntitySelector(Set<String> entitiesToProcess) {
        this.entitiesToProcess = entitiesToProcess;
    }

    @Override
    public Set<String> getEntities() {
        if(this.entitiesToProcess == null) {
            this.entitiesToProcess = readEntitiesFromFile(this.entityFile);
        }
        return this.entitiesToProcess;
    }
}
