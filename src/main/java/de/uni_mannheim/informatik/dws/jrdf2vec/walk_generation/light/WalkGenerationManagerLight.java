package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;

import java.io.File;
import java.util.HashSet;

/**
 * Default Walk Generator for RDF2Vec Light.
 */
public class WalkGenerationManagerLight extends WalkGenerationManager {


    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WalkGenerationManagerLight.class);

    /**
     * Constructor
     * @param pathToTripleFile Path to the data file.
     * @param pathToEntitiesFile Path to the file with entities for which walks shall be generated. One entity per line. No tags around the entities.
     */
    public WalkGenerationManagerLight(String pathToTripleFile, String pathToEntitiesFile){
        this(pathToTripleFile, pathToEntitiesFile, false);
    }

    /**
     * Constructor
     * @param pathToTripleFile Path to the data file.
     * @param pathToEntitiesFile Path to the file with entities for which walks shall be generated. One entity per line. No tags around the entities.
     * @param isGenerateTextWalks True if datatype properties shall be parsed and text walks shall be generated.
     */
    public WalkGenerationManagerLight(String pathToTripleFile, String pathToEntitiesFile, boolean isGenerateTextWalks){
        this(new File(pathToTripleFile), new File(pathToEntitiesFile), isGenerateTextWalks);
    }

    /**
     * Constructor
     * @param tripleFile Data file.
     * @param entitiesFile File with entities for which walks shall be generated. One entity per line. No tags around the entities.
     */
    public WalkGenerationManagerLight(File tripleFile, File entitiesFile){
        this(tripleFile, entitiesFile, false);
    }

    /**
     * Constructor
     * @param tripleFile Data file.
     * @param entitiesFile File with entities for which walks shall be generated. One entity per line. No tags around the entities.
     * @param isGenerateTextWalks True if datatype properties shall be parsed and text walks shall be generated.
     */
    public WalkGenerationManagerLight(File tripleFile, File entitiesFile, boolean isGenerateTextWalks){
        super(tripleFile, isGenerateTextWalks, false);
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
    public WalkGenerationManagerLight(File tripleFile, HashSet<String> entitiesToProcess) {
        super(tripleFile);
        super.entitySelector = new LightEntitySelector(entitiesToProcess);
    }

    /**
     * Constructor
     * @param pathToTripleFile The file or directory with the triple file. NT format is preferred.
     * @param entitiesToProcess The entities for which walks shall be generated.
     */
    public WalkGenerationManagerLight(String pathToTripleFile, HashSet<String> entitiesToProcess) {
        this(new File(pathToTripleFile), entitiesToProcess);
    }
}
