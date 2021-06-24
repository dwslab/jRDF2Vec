package de.uni_mannheim.informatik.dws.jrdf2vec.debugging;

import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.EntitySelector;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.HdtWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.IWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.MemoryWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.WalkGeneratorManager;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Check the vocabulary of a gensim or text file model.
 */
public class VocabularyAnalyzer {


    private static Logger LOGGER = LoggerFactory.getLogger(VocabularyAnalyzer.class);

    /**
     * Check for missing entities given an entity file with one entity per line. The file must be UTF-8 encoded.
     * Note that this analysis is less complete than {@link VocabularyAnalyzer#analyze(String, String)}.
     * @param filePathToModel File path to the model (kv file, model file, text file).
     * @param filePathToEntityFile Path to the UTF-8 encoded entity file with one entity per line.
     * @return Result of the analysis.
     */
    public static Set<String> detectMissingEntities(String filePathToModel, String filePathToEntityFile){
        Set<String> conceptsInModel = getModelVocabulary(filePathToModel).getValue0();
        Set<String> conceptsInEntityFile = readSetFromFile(filePathToEntityFile);
        conceptsInEntityFile.removeAll(conceptsInModel);
        return conceptsInEntityFile;
    }

    /**
     * Check for additional entities that appear in the embedding space but not in the specified concept file. The concept file must be UTF-8 encoded.
     * Note that this analysis is less complete than {@link VocabularyAnalyzer#analyze(String, String)}.
     * @param filePathToModel File path to the model (kv file, model file, text file).
     * @param filePathToEntityFile Path to the UTF-8 encoded entity file with one entity per line.
     * @return Result of the analysis.
     */
    public static Set<String> detectAdditionalEntities(String filePathToModel, String filePathToEntityFile){
        Set<String> conceptsInModel = getModelVocabulary(filePathToModel).getValue0();
        Set<String> conceptsInEntityFile = readSetFromFile(filePathToEntityFile);
        conceptsInModel.removeAll(conceptsInEntityFile);
        return conceptsInModel;
    }

    /**
     * Reads a Set from the file as specified by the file.
     * @param pathToFile The file (path) from which shall be read.
     * @return Read concepts in set.
     */
    static Set<String> readSetFromFile(String pathToFile){
        return readSetFromFile(new File(pathToFile));
    }

    /**
     * Reads a Set from the file as specified by the file.
     *
     * @param file The file that is to be read.
     * @return The parsed file as HashSet.
     */
    static Set<String> readSetFromFile(File file) {
        HashSet<String> result = new HashSet<>();
        if (!file.exists()) {
            LOGGER.error("File does not exist.");
            return result;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found.", e);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("IOException occured.", e);
            e.printStackTrace();
        }
        LOGGER.info("Entities read into cache.");
        return result;
    }

    /**
     * Complete analysis via the triple file on which the embedding has been trained on.
     * @param filePathToModel File path to the model (kv file, model file, text file).
     * @param filePathToTripleFile File path to the file with which the model has been trained.
     * @return Result of the analysis.
     */
    public static VocabularyAnalyzerResult analyze(String filePathToModel, String filePathToTripleFile){
        VocabularyAnalyzerResult result = new VocabularyAnalyzerResult();

        Pair<IWalkGenerator, EntitySelector> parserPair = WalkGeneratorManager.parseSingleFile(filePathToTripleFile);

        if(parserPair.getValue0().getClass() == HdtWalkGenerator.class){
            LOGGER.error("Analysis is not implemented for HDT parser!");
            return result;
        }
        Triplet<Set<String>, Integer, Boolean> readInfo = getModelVocabulary(filePathToModel);
        result.setDimension(readInfo.getValue1());
        result.setDimensionConsistent(readInfo.getValue2());

        Set<String> conceptsInModel = readInfo.getValue0();
        MemoryWalkGenerator parser = (MemoryWalkGenerator) parserPair.getValue0();
        TripleDataSetMemory triples = parser.getData();

        Set<String> subjectsNotFound = new HashSet<>(triples.getUniqueObjectTripleSubjects());
        subjectsNotFound.removeAll(conceptsInModel);
        result.setSubjectsNotFound(subjectsNotFound);

        Set<String> predicatesNotFound = new HashSet<>(triples.getUniqueObjectTriplePredicates());
        predicatesNotFound.removeAll(conceptsInModel);
        result.setPredicatesNotFound(predicatesNotFound);

        Set<String> objectsNotFound = new HashSet<>(triples.getUniqueObjectTripleObjects());
        objectsNotFound.removeAll(conceptsInModel);
        result.setObjectsNotFound(objectsNotFound);

        Set<String> additionalConcepts = new HashSet<>(conceptsInModel);
        additionalConcepts.removeAll(triples.getUniqueObjectTripleSubjects());
        additionalConcepts.removeAll(triples.getUniqueObjectTriplePredicates());
        additionalConcepts.removeAll(triples.getUniqueObjectTripleObjects());

        result.setAllAdditional(additionalConcepts);
        return result;
    }


    /**
     * Read the complete vocabulary from the specified file.
     * @param pathToModel The path to the model. The model can be a text file (ending .txt) or a gensim model file (keyed vector files must have the file ending .kv).
     * @return Triplet with: <br>
     *              [0] A set with the full vocabulary.<br>
     *              [1] Dimension<br>
     *              [2] True if dimension is consistent, else false.<br>
     */
    private static Triplet<Set<String>, Integer, Boolean> getModelVocabulary(String pathToModel){
        return getModelVocabulary(new File(pathToModel));
    }

    /**
     * Read the complete vocabulary from the specified file.
     * @param modelFile The model file.
     * @return Triplet with: <br>
     *              [0] A set with the full vocabulary.<br>
     *              [1] Dimension<br>
     *              [2] True if dimension is consistent, else false.<br>
     */
    private static Triplet<Set<String>, Integer, Boolean> getModelVocabulary(File modelFile){
        Triplet<Set<String>, Integer, Boolean> result = new Triplet<>(null, -1, false);
        if (!modelFile.exists()){
            LOGGER.error("The provided model file (" + modelFile.getAbsolutePath() + ") does not exist. ABORTING operation.");
        }
        if (modelFile.isDirectory()){
            LOGGER.error("The provided model file (" + modelFile.getAbsolutePath() + ") is a directory. ABORTING operation.");
        }

        if(modelFile.getAbsolutePath().endsWith(".txt")){
            return readTextVectorFile(modelFile);
        } else {
            Set<String> vocab = Gensim.getInstance().getVocabularyTerms(modelFile.getAbsolutePath());
            if(vocab != null) {
                result = result.setAt0(vocab);
                System.out.println(result.getValue0());
                if (result.getValue0().size() > 1) {
                    String firstConcept = result.getValue0().stream().findFirst().get();
                    result = result.setAt1(Gensim.getInstance().getVector(firstConcept, modelFile.getAbsolutePath()).length);
                    result = result.setAt2(true);
                } else {
                    result = result.setAt1(-1);
                    result = result.setAt2(false);
                }
            } else {
                // vocab is null
                result = result.setAt1(-1);
                result = result.setAt2(false);
            }
            return result;
        }
    }

    /**
     * Parses the concepts (not the vectors) from a text model file, i.e. a UTF-8 file where the concept is followed by its vector.
     * One concept appears per line. Everything is space separated.
     * @param textModelFile The file the shall be parsed.
     * @return Triplet with: <br>
     *              [0] A set with the full vocabulary.<br>
     *              [1] Dimension<br>
     *              [2] True if dimension is consistent, else false.<br>
     */
    static Triplet<Set<String>, Integer, Boolean> readTextVectorFile(File textModelFile){
        Triplet<Set<String>, Integer, Boolean> result;
        int dimension = -1;
        boolean isDimensionConsistent = true;
        HashSet<String> concepts = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textModelFile), StandardCharsets.UTF_8));
            String line;
            while((line = reader.readLine()) != null){
                String[] tokens = line.split(" ");
                if(tokens.length > 1) {
                    concepts.add(tokens[0]);
                    if(dimension == -1){
                        dimension = tokens.length - 1;
                    } else {
                        if(dimension != tokens.length - 1){
                            isDimensionConsistent = false;
                        }
                    }
                } else {
                    LOGGER.warn("Problem while reading the following line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Error while trying to read text model file: FileNotFoundException", e);
        } catch (IOException e) {
            LOGGER.error("Error while trying to read text model file.", e);
        } finally {
            result = new Triplet<>(concepts, dimension, isDimensionConsistent);
            return result;
        }
    }

}
