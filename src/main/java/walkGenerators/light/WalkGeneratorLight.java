package walkGenerators.light;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.classic.WalkGeneratorDefault;

import java.io.File;
import java.util.HashSet;

/**
 * Default Walk Generator for RDF2Vec Light.
 */
public class WalkGeneratorLight extends WalkGeneratorDefault {

    /**
     * Default Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WalkGeneratorLight.class);

    /**
     * Constructor
     * @param pathToTripleFile Path to the data file.
     * @param pathToEntitiesFile Path to the file with entities for which walks shall be generated. One entity per line. No tags around the entities.
     */
    public WalkGeneratorLight(String pathToTripleFile, String pathToEntitiesFile){
        this(new File(pathToTripleFile), new File(pathToEntitiesFile));
    }

    /**
     * Constructor
     * @param tripleFile Data file.
     * @param entitiesFile File with entities for which walks shall be generated. One entity per line. No tags around the entities.
     */
    public WalkGeneratorLight(File tripleFile, File entitiesFile){
        super(tripleFile);
        if(!tripleFile.exists()){
            LOGGER.error("The data file does not exist: " + tripleFile.getName() + "\nProgram will fail.");
        }
        if(!entitiesFile.exists()){
            LOGGER.error("The entities file does not exist: " + entitiesFile.getName() + "\nProgram will fail.");
        }
        super.entitySelector = new LightEntitySelector(entitiesFile);
    }

    /**
     * Constructor
     * @param tripleFile The file or directory with the triple file. NT format is preferred.
     * @param entitiesToProcess The entities for which walks shall be generated.
     */
    public WalkGeneratorLight(File tripleFile, HashSet<String> entitiesToProcess) {
        super(tripleFile);
        super.entitySelector = new LightEntitySelector(entitiesToProcess);
    }

    /**
     * Constructor
     * @param pathToTripleFile The file or directory with the triple file. NT format is preferred.
     * @param entitiesToProcess The entities for which walks shall be generated.
     */
    public WalkGeneratorLight(String pathToTripleFile, HashSet<String> entitiesToProcess) {
        this(new File(pathToTripleFile), entitiesToProcess);
    }
}
