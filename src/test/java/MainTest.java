import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of the command line functionality.
 */
class MainTest {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

    @Test
    public void trainClassic(){
        String walkPath = "./mainWalks/";
        File walkDirectory = new File(walkPath);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();
        String graphFilePath = this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath();
        String[] args = {"-graph", graphFilePath, "-walkDir", walkPath};
        Main.main(args);

        assertTrue(Main.getRdf2VecInstance().getClass().equals(RDF2Vec.class), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: RDF2Vec.class)");
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file.gz"));

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe){
            LOGGER.error("Failed to clean up after test.", ioe);
            fail();
        }
        Main.reset();
    }

    @Test
    public void trainLight(){
        File lightWalks = new File("./mainLightWalks/");
        lightWalks.mkdir();
        lightWalks.deleteOnExit();
        String entityFilePath = this.getClass().getClassLoader().getResource("dummyEntities.txt").getPath();
        String graphFilePath = this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath();
        String[] args = {"-graph", graphFilePath, "-light", entityFilePath, "-walkDir", "./mainLightWalks/"};
        Main.main(args);

        assertTrue(Main.getRdf2VecInstance().getClass().equals(RDF2VecLight.class));
        assertTrue(lightWalks.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(lightWalks.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("model.txt"));
        assertTrue(files.contains("walk_file.gz"));

        try {
            FileUtils.forceDelete(lightWalks);
        } catch (IOException ioe){
            LOGGER.error("Failed to clean up after test.", ioe);
        }
        Main.reset();
    }

    @Test
    public void getHelp(){
        String result = Main.getHelp();
        assertNotNull(result);

        // print the help for manual inspection
        System.out.println(result);
    }
}