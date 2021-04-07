package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.ContinuationEntitySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;

import java.io.File;
import java.net.URI;
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
     * Main Constructor
     * @param knowledgeGraph Knowledge graph URI.
     * @param entitiesFile Entities file.
     * @param isGenerateTextWalks True if text walks shall be generated.
     * @param existingWalks Directory where existing walks reside.
     * @param newWalkDirectory The directory where the new walks shall be written to. Must be different from
     *                         {@code existingWalks}.
     */
    public WalkGenerationManagerLight(URI knowledgeGraph, File entitiesFile, boolean isGenerateTextWalks,
                                      File existingWalks, File newWalkDirectory){
        super(knowledgeGraph, isGenerateTextWalks, false, existingWalks, newWalkDirectory);
        if(!entitiesFile.exists()){
            LOGGER.error("The entities file does not exist: " + entitiesFile.getName() + "\nProgram will fail.");
        }
        if(existingWalks != null && newWalkDirectory != null){
            super.entitySelector = new ContinuationEntitySelector(existingWalks, newWalkDirectory,
                    new LightEntitySelector(entitiesFile));
        } else {
            super.entitySelector = new LightEntitySelector(entitiesFile);
        }
    }

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
        this(tripleFile.toURI(), entitiesFile, isGenerateTextWalks, null, null);
    }

    /**
     * Constructor
     * @param knowledgeGraph Knowledge graph.
     * @param entitiesFile File with entities for which walks shall be generated. One entity per line. No tags around the entities.
     * @param isGenerateTextWalks True if datatype properties shall be parsed and text walks shall be generated.
     */
    public WalkGenerationManagerLight(URI knowledgeGraph, File entitiesFile, boolean isGenerateTextWalks){
        this(knowledgeGraph, entitiesFile, isGenerateTextWalks, null, null);
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
