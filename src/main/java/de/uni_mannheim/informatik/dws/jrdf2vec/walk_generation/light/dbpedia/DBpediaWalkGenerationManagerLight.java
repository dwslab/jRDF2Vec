package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.dbpedia;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.WalkGenerationManagerLight;

import java.io.File;
import java.util.HashSet;

public class DBpediaWalkGenerationManagerLight extends WalkGenerationManagerLight {

    public DBpediaWalkGenerationManagerLight(String pathToTripleFile, String pathToEntitiesFile) {
        super(pathToTripleFile, pathToEntitiesFile);
        super.entitySelector = new DBpediaLightEntitySelector(pathToEntitiesFile);
    }

    public DBpediaWalkGenerationManagerLight(File tripleFile, File entitiesFile) {
        super(tripleFile, entitiesFile);
    }

    public DBpediaWalkGenerationManagerLight(File tripleFile, HashSet<String> entitiesToProcess) {
        super(tripleFile, entitiesToProcess);
    }

    public DBpediaWalkGenerationManagerLight(String pathToTripleFile, HashSet<String> entitiesToProcess) {
        super(pathToTripleFile, entitiesToProcess);
    }
}
