import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import training.Word2VecConfiguration;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RDF2VecTest {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(RDF2Vec.class);

    @Test
    void getWalkFilePath() {
        File graphFilePath = new File(this.getClass().getClassLoader().getResource("emptyFile.txt").getPath());
        RDF2Vec rdf2vec = new RDF2Vec(graphFilePath);
        assertEquals("./walks/walk_file.gz", rdf2vec.getWalkFilePath());
        assertTrue(rdf2vec.getWalkFileDirectoryPath().endsWith("/walks/"), "Directory path: " + rdf2vec.getWalkFileDirectoryPath());
    }

    @Test
    void train() {
        File graphFilePath = new File(this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath());
        RDF2Vec classic = new RDF2Vec(graphFilePath);
        Word2VecConfiguration configuration = Word2VecConfiguration.CBOW;
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
        File graphFilePath = new File(this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath());
        File externalResourcesDirectory = new File("./extClassic/");
        externalResourcesDirectory.deleteOnExit();
        externalResourcesDirectory.mkdirs();
        RDF2Vec light = new RDF2Vec(graphFilePath);
        light.setResourceDirectory(externalResourcesDirectory);
        Word2VecConfiguration configuration = Word2VecConfiguration.CBOW;
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
            FileUtils.deleteDirectory(new File("./extClassic"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./extClassic/).");
            e.printStackTrace();
        }
    }

}