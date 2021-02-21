import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2VecLight;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
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

class RDF2VecLightTest {


    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(RDF2VecLightTest.class);

    @Test
    void getWalkFilePath() {
        File entityFilePath =loadFile("emptyFile.nt");
        File graphFilePath = loadFile("emptyFile.txt");
        RDF2VecLight light = new RDF2VecLight(graphFilePath, entityFilePath);
        assertEquals("./walks/walk_file.gz", light.getWalkFilePath());
        assertTrue(light.getWalkFileDirectoryPath().endsWith(File.separator + "walks"), "Directory path: " + light.getWalkFileDirectoryPath());
    }

    @Test
    void train() {
        File entityFilePath = loadFile("dummyEntities.txt");
        File graphFilePath = loadFile("dummyGraph.nt");
        RDF2VecLight light = new RDF2VecLight(graphFilePath, entityFilePath);
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.CBOW);
        configuration.setVectorDimension(10);
        light.train();
        assertTrue(new File("./walks/model").exists(), "Model file not written.");
        assertTrue(new File("./walks/model.kv").exists(), "Vector file not written.");
        assertTrue(new File("./walks/vectors.txt").exists(), "Text file not written.");
        assertTrue(new File("./walks/walk_file.gz").exists(), "Walk file not written.");
        assertFalse(light.getRequiredTimeForLastTrainingString().startsWith("<"), "No training time tracked."); // make sure time was tracked
        assertFalse(light.getRequiredTimeForLastWalkGenerationString().startsWith("<"), "No walk time tracked."); // make sure time was tracked

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
        File entityFilePath = loadFile("dummyEntities.txt");
        File graphFilePath = loadFile("dummyGraph.nt");
        File externalResourcesDirectory = new File("./extLight/");
        externalResourcesDirectory.deleteOnExit();
        externalResourcesDirectory.mkdirs();
        RDF2VecLight light = new RDF2VecLight(graphFilePath, entityFilePath);
        light.setResourceDirectory(externalResourcesDirectory);
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.CBOW);
        configuration.setVectorDimension(10);
        light.train();
        File serverFile = new File(externalResourcesDirectory, "python_server.py");
        assertTrue(serverFile.exists());
        try {
            FileUtils.deleteDirectory(externalResourcesDirectory.getCanonicalFile());
            assertFalse(externalResourcesDirectory.exists());
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
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

    @AfterAll
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
            FileUtils.deleteDirectory(new File("./extLight"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./extLight/).");
            e.printStackTrace();
        }
        Gensim.shutDown();
    }

}