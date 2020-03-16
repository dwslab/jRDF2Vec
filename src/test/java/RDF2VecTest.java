import org.apache.commons.io.FileUtils;
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
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }
    }

}