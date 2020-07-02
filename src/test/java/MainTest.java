import com.google.common.collect.Sets;
import de.uni_mannheim.informatik.dws.jrdf2vec.Main;
import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec;
import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2VecLight;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.base.HdtParser;
import de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.base.WalkGenerationMode;


import java.io.*;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of the command line functionality.
 */
class MainTest {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

    @AfterEach
    public void afterEach() {
        Main.reset();
    }

    @Test
    public void trainClassic() {
        String walkPath = "./mainWalks/";
        File walkDirectory = new File(walkPath);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();
        String graphFilePath = this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath();
        String[] args = {"-graph", graphFilePath, "-walkDir", walkPath};
        Main.main(args);

        assertTrue(Main.getRdf2VecInstance().getClass().equals(RDF2Vec.class), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec.class)");
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file.gz"));

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
            fail();
        }
    }

    @Test
    public void walkGenerationClassic() {
        File pizzaOntology = new File(getClass().getResource("/pizza.ttl").getFile());

        String directoryName = "./classicWalks/";
        File walkDirectory = new File(directoryName);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        String[] mainArgs = {"-graph", pizzaOntology.getAbsolutePath(), "-onlyWalks", "-walkDir", directoryName};
        Main.main(mainArgs);

        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertFalse(files.contains("model.txt"));
        assertTrue(files.contains("walk_file.gz"));

        GZIPInputStream gzip = null;
        try {
            File walkFile = new File(walkDirectory, "walk_file.gz");
            assertTrue(walkFile.exists());
            gzip = new GZIPInputStream(new FileInputStream(walkFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
        String readLine;
        HashSet<String> subjectsOfWalks = new HashSet<>();
        int numberOfLinesDuplicateFree = 0;
        try {
            while ((readLine = reader.readLine()) != null) {
                subjectsOfWalks.add(readLine.split(" ")[0]);
                numberOfLinesDuplicateFree++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(subjectsOfWalks.size() > 0, "Assert that walks have been generated for more than one entity.");
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot"));
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourCheesesTopping"));
        try {
            FileUtils.deleteDirectory(new File("./classicWalks/"));
        } catch (IOException ioe) {
            LOGGER.error("Error while trying to delete ./classicWalks/");
        }


        // now again with another configuration...
        Main.reset();
        walkDirectory = new File(directoryName);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        mainArgs = new String[]{"-graph", pizzaOntology.getAbsolutePath(), "-onlyWalks", "-walkDir", directoryName, "-walkGenerationMode", "RANDOM_WALKS"};
        Main.main(mainArgs);

        files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertFalse(files.contains("model.txt"));
        assertTrue(files.contains("walk_file.gz"));

        gzip = null;
        try {
            File walkFile = new File(walkDirectory, "walk_file.gz");
            assertTrue(walkFile.exists());
            gzip = new GZIPInputStream(new FileInputStream(walkFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        reader = new BufferedReader(new InputStreamReader(gzip));
        subjectsOfWalks = new HashSet<>();
        int numberOfLinesWithDuplicates = 0;
        HashSet<String> walks = new HashSet<>();
        try {
            while ((readLine = reader.readLine()) != null) {
                subjectsOfWalks.add(readLine.split(" ")[0]);
                numberOfLinesWithDuplicates++;
                walks.add(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(subjectsOfWalks.size() > 0, "Assert that walks have been generated for more than one entity.");
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot"));
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourCheesesTopping"));
        assertTrue(walks.size() < numberOfLinesWithDuplicates);
        assertTrue(numberOfLinesDuplicateFree < numberOfLinesWithDuplicates);
        try {
            FileUtils.deleteDirectory(new File("./classicWalks/"));
        } catch (IOException ioe) {
            LOGGER.error("Error while trying to delete ./classicWalks/");
        }
    }


    @Test
    public void trainLight() {
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
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
        }
    }

    @Test
    public void parameterCheck() {
        String entityFilePath = this.getClass().getClassLoader().getResource("dummyEntities.txt").getPath();
        String graphFilePath = this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath();
        Main.main(new String[]{"-graph", graphFilePath, "-light", entityFilePath, "-numberOfWalks", "100", "-minCount", "3"});
        assertEquals(100, ((RDF2VecLight) Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(3, ((RDF2VecLight) Main.getRdf2VecInstance()).getConfiguration().getMinCount());

        // important: reset
        Main.reset();

        // without light option
        Main.main(new String[]{"-graph", graphFilePath, "-numberOfWalks", "100", "-minCount", "2"});
        assertEquals(100, ((RDF2Vec) Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(2, ((RDF2Vec) Main.getRdf2VecInstance()).getConfiguration().getMinCount());
    }

    @Test
    public void parameterCheckNegative() {
        String entityFilePath = this.getClass().getClassLoader().getResource("dummyEntities.txt").getPath();
        String graphFilePath = this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath();
        Main.main(new String[]{"-graph", graphFilePath, "-light", entityFilePath, "-numberOfWalks", "-10", "-minCount", "-3"});
        assertEquals(Main.DEFAULT_NUMBER_OF_WALKS, ((RDF2VecLight) Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(Word2VecConfiguration.MIN_COUNT_DEFAULT, ((RDF2VecLight) Main.getRdf2VecInstance()).getConfiguration().getMinCount());

        // important: reset
        Main.reset();

        // without light option
        Main.main(new String[]{"-graph", graphFilePath, "-numberOfWalks", "abc", "-minCount", "abc"});
        assertEquals(Main.DEFAULT_NUMBER_OF_WALKS, (Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(Word2VecConfiguration.MIN_COUNT_DEFAULT, (Main.getRdf2VecInstance()).getConfiguration().getMinCount());
    }

    @Test
    public void getHelp() {
        String result = Main.getHelp();
        assertNotNull(result);

        // print the help for manual inspection
        System.out.println(result);

        // just making sure there is no exception thrown etc. and the program is not running after calling help.
        Main.main(new String[]{"-help"});
    }

    @Test
    public void containsIgnoreCase() {
        assertTrue(Main.containsIgnoreCase("hello", new String[]{"hello", "world"}));
        assertTrue(Main.containsIgnoreCase("HELLO", new String[]{"hello", "world"}));
        assertFalse(Main.containsIgnoreCase("Europa", new String[]{"hello", "world"}));
        assertFalse(Main.containsIgnoreCase(null, null));
    }

    /**
     * Plain generation of walks.
     */
    @Test
    public void plainWalkGenerationLightHdtFile() {

        // prepare file
        File graphFileToUse = new File(getClass().getClassLoader().getResource("./swdf-2012-11-28.hdt").getPath());

        // prepare directory
        File walkDirectory = new File("./walksOnly/");
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        String lightFilePath = getClass().getClassLoader().getResource("./swdf_light_entities.txt").getPath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "100", "-light", lightFilePath, "-onlyWalks", "-walkDir", "./walksOnly/"});

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file.gz"));

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file.gz");
            assertTrue(walkFile.exists(), "The walk file does not exist.");
            assertFalse(walkFile.isDirectory(), "The walk file is a directory (expected: file).");

            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(walkFile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));

            int heikoCount = 0;
            int heinerCount = 0;
            int pcmCount = 0;

            String readLine;
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("http://data.semanticweb.org/person/heiko-paulheim")) heikoCount++;
                if (readLine.contains("http://data.semanticweb.org/person/heiner-stuckenschmidt")) heinerCount++;
                if (readLine.contains("http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member"))
                    pcmCount++;
            }

            assertTrue(100 <= heikoCount && heikoCount <= 300);
            assertTrue(100 <= heinerCount && heinerCount <= 300);
            assertTrue(100 <= pcmCount && pcmCount <= 300);

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read from walk file.");
        }

        // clean up
        walkDirectory.delete();
    }


    /**
     * Weighted mid walk generation.
     */
    @Test
    public void weightedMidWalkGeneration() {

        // prepare file
        File graphFileToUse = new File("./swdf-2012-11-28.nt");
        HDT dataSet = null;
        try {
            dataSet = HDTManager.loadHDT(getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not load HDT file.");
        }
        HdtParser.serializeDataSetAsNtFile(dataSet, graphFileToUse);

        // prepare directory
        String directoryName = "./walksOnlyMidWeighted/";
        File walkDirectory = new File(directoryName);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        String lightFilePath = getClass().getClassLoader().getResource("./swdf_light_entities.txt").getPath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "10", "-light", lightFilePath, "-onlyWalks", "-walkDir", directoryName, "-walkGenerationMode", "mid_walks_weighted", "-depth", "3"});

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file.gz"));

        assertEquals(WalkGenerationMode.MID_WALKS_WEIGHTED, Main.getWalkGenerationMode());
        assertEquals(3, Main.getDepth());

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file.gz");
            assertTrue(walkFile.exists(), "The walk file does not exist.");
            assertFalse(walkFile.isDirectory(), "The walk file is a directory (expected: file).");

            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(walkFile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));

            int heikoCount = 0;
            int heinerCount = 0;
            int pcmCount = 0;

            String readLine;
            int numberOfLines = 0;
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("http://data.semanticweb.org/person/heiko-paulheim")) heikoCount++;
                if (readLine.contains("http://data.semanticweb.org/person/heiner-stuckenschmidt")) heinerCount++;
                if (readLine.contains("http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member"))
                    pcmCount++;
                numberOfLines++;
            }

            assertTrue(numberOfLines > 10);
            assertTrue(10 <= heikoCount && heikoCount < 30, "heikoCount not within boundaries. Value: " + heikoCount);
            assertTrue(10 <= heinerCount && heinerCount < 30, "heinerCount not within boundaries. Values: " + heinerCount);
            assertTrue(10 <= pcmCount && pcmCount < 30, "pcmCount not within boundaries. Values: " + pcmCount);

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read from walk file.");
        }

        // clean up
        graphFileToUse.delete();
        walkDirectory.delete();
    }

    @Test
    void runMainWithInsufficientArguments() {
        // just making sure that there are no exceptions.
        try {
            Main.main(null);
            Main.main(new String[]{"-helloWorld"});
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown.");
        }
    }

    /**
     * Random Mid walk duplicate free generation.
     */
    @Test
    public void lightWithDuplicateFreeMode() {

        // prepare file
        File graphFileToUse = new File("./swdf-2012-11-28.nt");
        HDT dataSet = null;
        try {
            dataSet = HDTManager.loadHDT(getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not load HDT file.");
        }
        HdtParser.serializeDataSetAsNtFile(dataSet, graphFileToUse);

        // prepare directory
        File walkDirectory = new File("./walksOnly/");
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();


        String lightFilePath = getClass().getClassLoader().getResource("./swdf_light_entities.txt").getPath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "1000", "-light", lightFilePath, "-onlyWalks", "-walkDir", "./walksOnly/", "-walkGenerationMode", "mid_walks_duplicate_free", "-depth", "1"});

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file.gz"));

        assertEquals(WalkGenerationMode.MID_WALKS_DUPLICATE_FREE, Main.getWalkGenerationMode());
        assertEquals(1, Main.getDepth());

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file.gz");
            assertTrue(walkFile.exists(), "The walk file does not exist.");
            assertFalse(walkFile.isDirectory(), "The walk file is a directory (expected: file).");

            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(walkFile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));

            int heikoCount = 0;
            int heinerCount = 0;
            int pcmCount = 0;

            String readLine;
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("http://data.semanticweb.org/person/heiko-paulheim")) heikoCount++;
                if (readLine.contains("http://data.semanticweb.org/person/heiner-stuckenschmidt")) heinerCount++;
                if (readLine.contains("http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member"))
                    pcmCount++;
            }

            assertTrue(1 <= heikoCount && heikoCount < 1000, "heikoCount not within boundaries. Value: " + heikoCount);
            assertTrue(1 <= heinerCount && heinerCount < 1000, "heinerCount not within boundaries. Values: " + heinerCount);
            assertTrue(1 <= pcmCount && pcmCount < 1000, "pcmCount not within boundaries. Values: " + pcmCount);

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read from walk file.");
        }

        // clean up
        graphFileToUse.delete();
        walkDirectory.delete();
    }


    /**
     * Plain generation of walks.
     */
    @Test
    public void plainWalkGenerationLightNtFile() {

        // prepare file
        File graphFileToUse = new File("./swdf-2012-11-28.nt");
        HDT dataSet = null;
        try {
            dataSet = HDTManager.loadHDT(getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not load HDT file.");
        }
        HdtParser.serializeDataSetAsNtFile(dataSet, graphFileToUse);

        // prepare directory
        String walkDirectoryName = "./walksOnly2/";
        File walkDirectory = new File(walkDirectoryName);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();


        String lightFilePath = getClass().getClassLoader().getResource("./swdf_light_entities.txt").getPath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "100", "-light", lightFilePath, "-onlyWalks", "-walkDir", walkDirectoryName});

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file.gz"));

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file.gz");
            assertTrue(walkFile.exists(), "The walk file does not exist.");
            assertFalse(walkFile.isDirectory(), "The walk file is a directory (expected: file).");

            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(walkFile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));

            int heikoCount = 0;
            int heinerCount = 0;
            int pcmCount = 0;

            String readLine;
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("http://data.semanticweb.org/person/heiko-paulheim")) heikoCount++;
                if (readLine.contains("http://data.semanticweb.org/person/heiner-stuckenschmidt")) heinerCount++;
                if (readLine.contains("http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member"))
                    pcmCount++;
            }

            assertTrue(100 <= heikoCount && heikoCount <= 300);
            assertTrue(100 <= heinerCount && heinerCount <= 300);
            assertTrue(100 <= pcmCount && pcmCount <= 300);

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read from walk file.");
        }

        // clean up
        graphFileToUse.delete();
        walkDirectory.delete();
    }

    @Test
    public void getValue() {
        assertNull(Main.getValue(null, null));
        assertNull(Main.getValue(null, new String[]{"european", "union"}));
        assertNull(Main.getValue("hello", null));
        assertNull(Main.getValue("-hello", new String[]{"european", "union"}));
        assertEquals("union", Main.getValue("-european", new String[]{"-european", "union"}));
    }

    @AfterAll
    static void cleanUp() {
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
            FileUtils.deleteDirectory(new File("./extLight"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./extLight/).");
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File("./walksOnly"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./walksOnly/).");
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File("./walksOnly2"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./walksOnly2/).");
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File("./classicWalks/"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./classicWalks/).");
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(new File("./walksOnlyMidWeighted/"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory ./walksOnlyMidWeighted/).");
            e.printStackTrace();
        }
    }
}