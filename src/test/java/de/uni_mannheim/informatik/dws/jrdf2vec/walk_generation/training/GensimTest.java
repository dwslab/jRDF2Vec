package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.training;

import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.deleteFile;
import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.getNumberOfLines;
import static org.junit.jupiter.api.Assertions.*;


class GensimTest {


    private static Gensim gensim;

    @BeforeAll
    public static void setup(){
        gensim = Gensim.getInstance();
    }

    @AfterAll
    public static void tearDown(){
        Gensim.shutDown();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GensimTest.class);

    @Test
    void checkRequirements(){
        assertTrue(Gensim.getInstance().checkRequirements());
    }

    /**
     * Default test with cache.
     */
    @Test
    void isInVocabulary() {
        gensim.setVectorCaching(true);

        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        assertTrue(gensim.isInVocabulary("Europe", pathToModel));
        assertTrue(gensim.isInVocabulary("united", pathToModel));
        assertFalse(gensim.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        File vectorFile = new File(getPathOfResource("test_model_vectors.kv"));
        assertTrue(gensim.isInVocabulary("Europe", vectorFile));
        assertTrue(gensim.isInVocabulary("united", vectorFile));
        assertFalse(gensim.isInVocabulary("China", vectorFile));
    }

    /**
     * Default test without cache.
     */
    @Test
    void isInVocabularyNoCaching() {
        gensim.setVectorCaching(false);

        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        assertNotNull(pathToModel);
        File modelFile = new File(pathToModel);
        assertTrue(modelFile.exists());
        assertTrue(gensim.isInVocabulary("Europe", pathToModel));
        assertTrue(gensim.isInVocabulary("united", pathToModel));
        assertFalse(gensim.isInVocabulary("China", pathToModel));

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        assertTrue(gensim.isInVocabulary("Europe", pathToVectorFile));
        assertTrue(gensim.isInVocabulary("united", pathToVectorFile));
        assertFalse(gensim.isInVocabulary("China", pathToVectorFile));
    }

    @Test
    /**
     * Default test with cache.
     */
    void getSimilarity() {
        gensim.setVectorCaching(true);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }


    @Test
    void getSimilarityNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: model file
        String pathToModel = getPathOfResource("test_model");
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }


    @Test
    void testMultipleShutdownCallsAndRestarts() {
        gensim.setVectorCaching(false);
        // test case 1: model file
        gensim.shutDown();
        gensim = Gensim.getInstance();
        String pathToModel = getPathOfResource("test_model");
        double similarity = gensim.getSimilarity("Europe", "united", pathToModel);
        assertTrue(similarity > 0);

        // test case 2: vector file
        gensim.shutDown();
        gensim = Gensim.getInstance();
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        similarity = gensim.getSimilarity("Europe", "united", pathToVectorFile);
        assertTrue(similarity > 0);
    }

    /**
     * Default test with cache.
     */
    @Test
    void getVector() {
        gensim.setVectorCaching(true);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = gensim.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = gensim.getVector("united", pathToVectorFile);

        double similarityJava = (gensim.cosineSimilarity(europeVector, unitedVector));
        double similarityPyhton = (gensim.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPyhton, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = gensim.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }

    /**
     * Test without cache.
     */
    @Test
    void getVectorNoCaching() {
        gensim.setVectorCaching(false);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = gensim.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = gensim.getVector("united", pathToVectorFile);

        double similarityJava = (Gensim.cosineSimilarity(europeVector, unitedVector));
        double similarityPython = (gensim.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPython, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = gensim.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }

    /**
     * Check whether vectors can be read using two different ports.
     * Test without cache.
     */
    @ParameterizedTest
    @ValueSource(ints = {41193, 41194})
    void getVectorNoCachingDifferentPorts(int port) {
        Gensim.shutDown();
        Gensim.setPort(port);
        assertEquals(port, Gensim.getPort());
        gensim = Gensim.getInstance();
        gensim.setVectorCaching(false);
        // test case 1: vector file
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Double[] europeVector = gensim.getVector("Europe", pathToVectorFile);
        assertEquals(100, europeVector.length);

        Double[] unitedVector = gensim.getVector("united", pathToVectorFile);

        double similarityJava = (Gensim.cosineSimilarity(europeVector, unitedVector));
        double similarityPython = (gensim.getSimilarity("Europe", "united", pathToVectorFile));
        assertEquals(similarityJava, similarityPython, 0.0001);

        // test case 2: model file
        String pathToModel = getPathOfResource("test_model");
        europeVector = gensim.getVector("Europe", pathToModel);
        assertEquals(100, europeVector.length);
    }

    @Test
    void writeModelAsTextFile() {
        // "normal" training task
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        String fileToWrite = "./freudeWord2vec.kv";
        assertTrue(gensim.trainWord2VecModel(fileToWrite, testFilePath, new Word2VecConfiguration(Word2VecType.CBOW)));

        File vectorFile = new File(fileToWrite);
        File modelFile = new File(fileToWrite.substring(0, fileToWrite.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        assertTrue(gensim.getSimilarity("Menschen", "Brüder", fileToWrite) > -1.0);

        gensim.writeModelAsTextFile(fileToWrite, "./testTextVectors.txt");
        File writtenFile = new File("./testTextVectors.txt");
        assertTrue(writtenFile.exists());
        assertTrue(getNumberOfLines(writtenFile) > 10);

        String entityFile = getPathOfResource("freudeSubset.txt");
        gensim.writeModelAsTextFile(fileToWrite, "./testTextVectors2.txt", entityFile);
        File writtenFile2 = new File("./testTextVectors2.txt");
        assertTrue(writtenFile2.exists());
        assertTrue(getNumberOfLines(writtenFile2) <= 2);

        // cleaning up
        deleteFile(writtenFile2);
        deleteFile(writtenFile);
        deleteFile(modelFile);
        deleteFile(vectorFile);
    }

    @Test
    void trainWord2VecModelWithWalkDirectory() {
        String testFilePath = getPathOfResource("walk_directory_test");
        String vectorFilePath = "./w2v_directory_test.kv";
        assertTrue(gensim.trainWord2VecModel(vectorFilePath, testFilePath, new Word2VecConfiguration(Word2VecType.SG)));
        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");

        //contains "Hymne" (count = 1) in file "an_die_freude.txt"
        assertTrue(gensim.isInVocabulary("Hymne", vectorFilePath));

        //contains "Freude" (count > 3) in file "an_die_freude.txt"
        assertTrue(gensim.isInVocabulary("Freude", vectorFilePath));

        // contains "Stolz" (count = 1) in file "auf_die_europa.txt"
        assertTrue(gensim.isInVocabulary("Stolz", vectorFilePath));

        // contains "Europen" (count = 1) in file "auf_die_europa.txt"
        assertTrue(gensim.isInVocabulary("Europen", vectorFilePath));

        // cleaning up
        deleteFile(modelFile);
        deleteFile(vectorFile);
    }

    @Test
    void trainWord2VecModelSG() {
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        String vectorFilePath = "./freudeWord2vec_sg.kv";
        assertTrue(gensim.trainWord2VecModel(vectorFilePath, testFilePath, new Word2VecConfiguration(Word2VecType.SG)));

        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        double similarity = gensim.getSimilarity("Menschen", "Brüder", vectorFilePath);
        assertTrue(similarity > -1.0, "Problem with the similiarity. Similarity: " + similarity);

        //contains "Hymne" (count = 1)
        assertTrue(gensim.isInVocabulary("Hymne", vectorFilePath));

        //contains "Freude" (count > 3)
        assertTrue(gensim.isInVocabulary("Freude", vectorFilePath));

        //contains "Bösewicht," (count = 1)
        assertTrue(gensim.isInVocabulary("Bösewicht,", vectorFilePath));

        int vocabularySize = gensim.getVocabularySize(vectorFilePath);
        assertTrue(vocabularySize > 100);

        // cleaning up
        deleteFile(modelFile);
        deleteFile(vectorFile);
    }

    @Test
    void trainWord2VecModelCBOW() {
        String testFilePath = getPathOfResource("testInputForWord2Vec.txt");
        if(testFilePath == null) fail("Test resource not found.");
        String vectorFilePath = "./freudeWord2vec.kv";
        assertTrue(gensim.trainWord2VecModel(vectorFilePath, testFilePath, new Word2VecConfiguration(Word2VecType.CBOW)));

        File vectorFile = new File(vectorFilePath);
        File modelFile = new File(vectorFilePath.substring(0, vectorFilePath.length() - 3));
        assertTrue(vectorFile.exists(), "No vector file was written.");
        assertTrue(modelFile.exists(), "No model file was written.");
        double similarity = gensim.getSimilarity("Menschen", "Brüder", vectorFilePath);
        assertTrue(similarity > -1.0, "Problem with the simliarity. Similarity: " + similarity);

        // cleaning up
        deleteFile(modelFile);
        deleteFile(vectorFile);
    }

    @Test
    void externalResourcesDirectory(){
        // shut down
        Gensim.shutDown();

        // reinitialize
        File externalResourcesDirectory = new File("./ext/");
        gensim = Gensim.getInstance(externalResourcesDirectory);
        File serverFile = new File(externalResourcesDirectory, "python_server.py");
        assertTrue(serverFile.exists());
        try {
            FileUtils.deleteDirectory(externalResourcesDirectory);
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }

        // shut down again to keep using default resources directory
        Gensim.shutDown();

        // we need to restart for subsequent tests
        gensim = Gensim.getInstance();

        try {
            FileUtils.deleteDirectory(externalResourcesDirectory);
        } catch (IOException e) {
            LOGGER.error("Failed to clean up external resources directory.");
        }
    }

    @Test
    void getVocabularyTerms(){
        String pathToVectorFile = getPathOfResource("test_model_vectors.kv");
        Set<String> result = gensim.getVocabularyTerms(pathToVectorFile);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("Europe"));
    }

    @Test
    void writeVocabularyToFile() {
        File vocabFile = new File("./gensim_vocab.txt");
        vocabFile.deleteOnExit();
        gensim.writeVocabularyToFile(getPathOfResource("test_model_vectors.kv"), vocabFile);
        assertTrue(vocabFile.exists());

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocabFile), StandardCharsets.UTF_8));
            Set<String> vocabulary = new HashSet<>();
            String line;
            while((line = reader.readLine()) != null){
                vocabulary.add(line);
            }
            assertTrue(vocabulary.contains("Europe"));
        } catch (IOException  e) {
            fail(e);
        }
    }

    @Test
    void setGetPort(){
        int testPort = 41194;
        Gensim.setPort(testPort);
        assertNotEquals(Gensim.getPort(), testPort);
        Gensim.shutDown();
        Gensim.setPort(testPort);
        gensim = Gensim.getInstance();
        assertEquals(testPort, Gensim.getPort());
        assertTrue(Gensim.getServerUrl().contains("41194"));
    }

    /**
     * Helper method to obtain the canonical path of a (test) resource.
     * @param resourceName File/directory name.
     * @return Canonical path of resource.
     */
    public String getPathOfResource(String resourceName){
        try {
            URL res = getClass().getClassLoader().getResource(resourceName);
            if(res == null) throw new IOException();
            File file = Paths.get(res.toURI()).toFile();
            return file.getCanonicalPath();
        } catch (URISyntaxException | IOException ex) {
            LOGGER.info("Cannot create path of resource", ex);
            return null;
        }
    }
}