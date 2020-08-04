import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class RDF2VecTest {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(RDF2Vec.class);

    @Test
    void getWalkFilePath() {
        RDF2Vec rdf2vec = new RDF2Vec(loadFile("emptyFile.txt"));
        // also forward slash on windows
        assertEquals("./walks/walk_file.gz", rdf2vec.getWalkFilePath());
        assertTrue(rdf2vec.getWalkFileDirectoryPath().endsWith(File.separator + "walks" + File.separator) || rdf2vec.getWalkFileDirectoryPath().endsWith(File.separator + "walks"), "Directory path: " + rdf2vec.getWalkFileDirectoryPath());
    }


    @Test
    void train() {
        RDF2Vec classic = new RDF2Vec(loadFile("dummyGraph.nt"));
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.CBOW);
        configuration.setVectorDimension(10);

        classic.train();

        assertTrue(new File("./walks/model").exists(), "Model file not written.");
        assertTrue(new File("./walks/model.kv").exists(), "Vector file not written.");
        assertTrue(new File("./walks/walk_file.gz").exists(), "Walk file not written.");
        assertFalse(classic.getRequiredTimeForLastTrainingString().startsWith("<"), "No training time tracked."); // make sure time was tracked
        assertFalse(classic.getRequiredTimeForLastWalkGenerationString().startsWith("<"), "No walk time tracked."); // make sure time was tracked

        // clean up
        try {
            FileUtils.deleteDirectory(new File("./walks"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }
    }

    @Test
    void trainWithOntModelReference(){
        File ontologyTestFile = null;
        try {
            ontologyTestFile = FileUtils.toFile(RDF2VecTest.class.getClassLoader().getResource("pizza.owl.xml").toURI().toURL());
        } catch (URISyntaxException | MalformedURLException use){
            use.printStackTrace();
            fail("Could not load test resource.");
        }
        assertTrue(ontologyTestFile.exists(), "The required test resource cannot be found.");
        File walkDirectory = new File("./ontModelTest/");
        walkDirectory.mkdir();
        try {
            RDF2Vec rdf2Vec = new RDF2Vec(Util.readOntology(ontologyTestFile, "XML"), walkDirectory);
            String result = rdf2Vec.train();
            assertNotNull(result);
            assertTrue(new File(result).exists());
            assertTrue(Gensim.getInstance().getVocabularySize(result) > 0);
            assertTrue(Gensim.getInstance().isInVocabulary("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot", result));
        } catch (Exception e) {
            fail("Exception occurred with message: " + e.getMessage());
        } finally {
            try {
                FileUtils.deleteDirectory(walkDirectory);
            } catch (IOException ioe){
                LOGGER.error("Unable to delete directory after test execution: " + walkDirectory.getAbsolutePath());
            }
            Gensim.shutDown();
        }
    }

    @Test
    void trainWithMixedInputFiles(){
        File graphFilePath = loadFile("mixedWalkDirectory");
        RDF2Vec classic = new RDF2Vec(graphFilePath);
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);
        configuration.setVectorDimension(10);

        classic.train();

        assertTrue(new File("./walks/model").exists(), "Model file not written.");
        assertTrue(new File("./walks/model.kv").exists(), "Vector file not written.");
        assertTrue(new File("./walks/walk_file.gz").exists(), "Walk file not written.");
        assertFalse(classic.getRequiredTimeForLastTrainingString().startsWith("<"), "No training time tracked."); // make sure time was tracked
        assertFalse(classic.getRequiredTimeForLastWalkGenerationString().startsWith("<"), "No walk time tracked."); // make sure time was tracked

        // clean up
        try {
            FileUtils.deleteDirectory(new File("./walks"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }
    }

    @Test
    void trainWithExternalResourcesDirectory(){
        File graphFilePath = loadFile("dummyGraph.nt");
        File externalResourcesDirectory = new File("./extClassic/");
        externalResourcesDirectory.deleteOnExit();
        externalResourcesDirectory.mkdirs();
        RDF2Vec light = new RDF2Vec(graphFilePath);
        light.setPythonServerResourceDirectory(externalResourcesDirectory);
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.CBOW);
        configuration.setVectorDimension(10);
        light.train();
        File serverFile = new File(externalResourcesDirectory, "python_server.py");
        assertTrue(serverFile.exists());
        try {
            FileUtils.forceDelete(externalResourcesDirectory.getCanonicalFile());
            assertFalse(externalResourcesDirectory.exists());
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }
    }

    /**
     * In rare cases when the test execution is aborted in the middle of processing, some intermediate test files
     * may have been created. When restarting the tests, those may lead to errors. Therefore, the cleanup is also run
     * before all tests.
     */
    @BeforeAll
    static void beforeCleanUp(){
        cleanUp();
    }

    @AfterAll
    static void afterAll(){
        cleanUp();
    }

    /**
     * Deleting test files.
     */
    static void cleanUp(){
        try {
            FileUtils.deleteDirectory(new File("./walks"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./walks/).");
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File("./python-server"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./python-server).");
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File("./extClassic"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./extClassic/).");
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File("./ontModelTest"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./ontModelTest/).");
            e.printStackTrace();
        }
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            File result =  FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }
}