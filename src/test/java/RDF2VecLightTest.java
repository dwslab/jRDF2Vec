import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import training.Word2VecConfiguration;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RDF2VecLightTest {

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(RDF2VecLightTest.class);

    @Test
    void getWalkFilePath() {
        File entityFilePath = new File(this.getClass().getClassLoader().getResource("emptyFile.nt").getFile());
        File graphFilePath = new File(this.getClass().getClassLoader().getResource("emptyFile.txt").getPath());
        RDF2VecLight light = new RDF2VecLight(graphFilePath, entityFilePath);
        assertEquals("./walks/walk_file.gz", light.getWalkFilePath());
        assertTrue(light.getWalkFileDirectoryPath().endsWith("/walks/"), "Directory path: " + light.getWalkFileDirectoryPath());
    }

    @Test
    void train() {
        File entityFilePath = new File(this.getClass().getClassLoader().getResource("dummyEntities.txt").getFile());
        File graphFilePath = new File(this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath());
        RDF2VecLight light = new RDF2VecLight(graphFilePath, entityFilePath);
        Word2VecConfiguration configuration = Word2VecConfiguration.CBOW;
        configuration.setVectorDimension(10);
        light.train();
    }

    @Test
    void trainWithExternalResourcesDirectory(){
        File entityFilePath = new File(this.getClass().getClassLoader().getResource("dummyEntities.txt").getFile());
        File graphFilePath = new File(this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath());
        File externalResourcesDirectory = new File("./ext/");
        externalResourcesDirectory.mkdirs();
        RDF2VecLight light = new RDF2VecLight(graphFilePath, entityFilePath);
        light.setResourceDirectory(externalResourcesDirectory);
        Word2VecConfiguration configuration = Word2VecConfiguration.CBOW;
        configuration.setVectorDimension(10);
        light.train();
        File serverFile = new File(externalResourcesDirectory, "python_server.py");
        assertTrue(serverFile.exists());
        try {
            FileUtils.deleteDirectory(externalResourcesDirectory);
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }
    }

}