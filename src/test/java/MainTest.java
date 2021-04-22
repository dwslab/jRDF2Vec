import com.google.common.collect.Sets;
import de.uni_mannheim.informatik.dws.jrdf2vec.Main;
import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec;
import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2VecLight;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.HdtWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;
import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;


import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.getNumberOfLines;
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

    /**
     * Test classic run with vector text file generation.
     */
    @Test
    public void trainClassic() {
        LOGGER.info("Running test: trainClassic()");
        String walkPath = "./mainWalks/";
        File walkDirectory = new File(walkPath);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();
        String graphFilePath = loadFile("dummyGraph.nt").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-walkDir", walkPath, "-sample", "NOT_A_DOUBLE"};
        Main.main(args);

        assertTrue(Main.getRdf2VecInstance().getClass().equals(RDF2Vec.class), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec.class)");
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertTrue(files.contains("vectors.txt"));

        // assert sample parameter
        assertEquals(Word2VecConfiguration.SAMPLE_DEFAULT, Main.getRdf2VecInstance().getWord2VecConfiguration().getSample());

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
            fail();
        }
    }

    @Test
    void trainWithMixedInputFilesCommandLine() {
        LOGGER.info("Running test: trainWithMixedInputFilesCommandLine()");
        File graphFilePath = new File(loadFile("mixedWalkDirectory").getAbsolutePath());
        Main.main(new String[]{"-graph", graphFilePath.getAbsolutePath(), "-dimension", "100", "-depth", "4", "-trainingMode", "sg", "-numberOfWalks", "100", "-walkGenerationMode", "RANDOM_WALKS_DUPLICATE_FREE"});

        assertTrue(new File("./walks/model").exists(), "Model file not written.");
        assertTrue(new File("./walks/model.kv").exists(), "Vector file not written.");
        assertTrue(new File("./walks/walk_file_0.txt.gz").exists(), "Walk file not written.");
        assertFalse(Main.getRdf2VecInstance().getRequiredTimeForLastTrainingString().startsWith("<"), "No training time tracked."); // make sure time was tracked
        assertFalse(Main.getRdf2VecInstance().getRequiredTimeForLastWalkGenerationString().startsWith("<"), "No walk time tracked."); // make sure time was tracked

        // clean up
        try {
            FileUtils.deleteDirectory(new File("./walks"));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed.");
            e.printStackTrace();
        }
    }

    @Test
    public void onlyTraining() {
        LOGGER.info("Running test: onlyTraining()");
        String walkDirectory = loadFile("walk_directory_test").getAbsolutePath();
        Main.main(new String[]{"-onlyTraining", "-walkDirectory", walkDirectory, "-dimensions", "80"});
        System.out.println(walkDirectory);

        File modelkvFile = new File(walkDirectory + "/model.kv");
        modelkvFile.deleteOnExit();
        assertTrue(modelkvFile.exists());

        File modelFile = new File(walkDirectory + "/model");
        modelFile.deleteOnExit();
        assertTrue(modelFile.exists());

        File vectorsFile = new File(walkDirectory + "/vectors.txt");
        vectorsFile.deleteOnExit();
        assertTrue(vectorsFile.exists());
        assertEquals(80, Util.getDimensionalityFromVectorTextFile(vectorsFile));

        // clean-up
        modelkvFile.delete();
        modelFile.delete();
        vectorsFile.delete();
    }

    /**
     * Just making sure that the program does not fail if used inappropriately.
     */
    @Test
    public void onlyTrainingFail() {
        LOGGER.info("Running test: onlyTrainingFail()");
        Main.main(new String[]{"-onlyTraining"});
        Main.main(new String[]{"-onlyTraining", "-minCount", "5"});
        Main.main(new String[]{"-threads", "3", "-onlyTraining", "-minCount", "5"});
    }

    /**
     * Testing whether a vector text file is not generated for classic if it is explicitly stated so.
     * The test is performed on the default port as well as on a custom port.
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void trainClassicNoTextVectorFile(boolean isRunOnDefaultPort) {
        LOGGER.info("Running test: trainClassicNoTextVectorFile()");
        String walkPath = "./mainWalks/";
        File walkDirectory = new File(walkPath);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();
        String graphFilePath = loadFile("dummyGraph.nt").getAbsolutePath();

        String[] args;
        if (isRunOnDefaultPort) {
            args = new String[]{"-graph", graphFilePath, "-walkDir", walkPath, "-noVectorTextFileGeneration"};
        } else {
            // run on other port
            args = new String[7];
            args[0] = "-graph";
            args[1] = graphFilePath;
            args[2] = "-walkDir";
            args[3] = walkPath;
            args[4] = "-noVectorTextFileGeneration";
            args[5] = "-port";
            args[6] = "1820";
        }

        Main.main(args);

        assertEquals(RDF2Vec.class, Main.getRdf2VecInstance().getClass(), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: RDF2Vec.class)");
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertFalse(files.contains("vectors.txt"));

        // check sample parameter
        assertEquals(Word2VecConfiguration.SAMPLE_DEFAULT, Main.getRdf2VecInstance().getWord2VecConfiguration().getSample());

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
            fail();
        }
        Main.reset();
    }

    /**
     * Testing whether a vector text file is not generated for classic if it is explicitly stated so.
     */
    @Test
    public void trainClassicWithNqFile() {
        String walkPath = "./mainWalksNq/";
        File walkDirectory = new File(walkPath);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();
        String graphFilePath = loadFile("nq_fibo_example.nq").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-walkDir", walkPath, "-noVectorTextFileGeneration", "-sample", "0.01"};
        Main.main(args);

        assertEquals(RDF2Vec.class, Main.getRdf2VecInstance().getClass(), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: RDF2Vec.class)");
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertFalse(files.contains("vectors.txt"));

        // test sample parameter
        assertEquals(0.01, Main.getRdf2VecInstance().getWord2VecConfiguration().getSample());

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
            fail();
        }
    }

    @Test
    public void trainClassicWithOwlFileTextGeneration() {
        String walkPath = "." + File.separator + "mainWalksOwlText" + File.separator;
        File walkDirectory = new File(walkPath);
        walkDirectory.deleteOnExit();
        walkDirectory.mkdir();
        String graphFilePath = loadFile("pizza.owl.xml").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-walkDir", walkPath, "-noVectorTextFileGeneration", "-sample", "0.01", "-embedText", "-window", "5"};
        Main.main(args);

        assertEquals(RDF2Vec.class, Main.getRdf2VecInstance().getClass(), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: RDF2Vec.class)");
        assertEquals(5, Main.getRdf2VecInstance().getWord2VecConfiguration().getWindowSize());
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertFalse(files.contains("vectors.txt"));

        // assert that a text walk has been written
        List<String> lines = Util.readLinesFromGzippedFile(new File(walkDirectory, "walk_file_0.txt.gz"));

        // look for specific walk
        assertTrue(lines.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#Siciliana http://www.w3.org/2004/02/skos/core#altLabel siciliana"));

        // test sample parameter
        assertEquals(0.01, Main.getRdf2VecInstance().getWord2VecConfiguration().getSample());

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
        }
    }

    /**
     * Testing whether a vector text file is not generated for classic if it is explicitly stated so.
     */
    @Test
    public void trainClassicWithNtFileTextGeneration() {
        Main.reset();
        String walkPath = "." + File.separator + "mainWalksNtText" + File.separator;
        File walkDirectory = new File(walkPath);
        walkDirectory.deleteOnExit();
        walkDirectory.mkdir();
        String graphFilePath = loadFile("dummyGraph_with_labels.nt").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-walkDir", walkPath, "-noVectorTextFileGeneration", "-sample", "0.01", "-embedText", "-window", "8"};
        Main.main(args);

        assertEquals(RDF2Vec.class, Main.getRdf2VecInstance().getClass(), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: RDF2Vec.class)");
        assertEquals(8, Main.getRdf2VecInstance().getWord2VecConfiguration().getWindowSize());
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertFalse(files.contains("vectors.txt"));

        // assert that a text walk has been written
        List<String> lines = Util.readLinesFromGzippedFile(new File(walkDirectory, "walk_file_0.txt.gz"));

        // make sure that we do not paste mass text
        for (String line : lines) {
            assertTrue(line.split(" ").length <= 8);
        }

        // look for specific walk
        assertTrue(lines.contains("W rdf:Description freude schöner götterfunken tochter aus elysium"));

        // test sample parameter
        assertEquals(0.01, Main.getRdf2VecInstance().getWord2VecConfiguration().getSample());

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
        }
    }

    /**
     * Testing whether a vector text file is not generated for classic if it is explicitly stated so.
     */
    @Test
    public void trainLightWithNtFileTextGeneration() {
        String walkPath = "." + File.separator + "mainWalksNtText_light" + File.separator;
        File walkDirectory = new File(walkPath);
        walkDirectory.deleteOnExit();
        walkDirectory.mkdir();
        String graphFilePath = loadFile("dummyGraph_with_labels.nt").getAbsolutePath();
        String entityFilePath = loadFile("dummyGraph_with_labels_light_entities.txt").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-walkDir", walkPath, "-light", entityFilePath, "-noVectorTextFileGeneration", "-sample", "0.01", "-embedText", "-window", "8"};
        Main.main(args);

        assertEquals(RDF2VecLight.class, Main.getRdf2VecInstance().getClass(), "Wrong class: " + Main.getRdf2VecInstance().getClass() + " (expected: RDF2VecLight.class)");
        assertEquals(8, Main.getRdf2VecInstance().getWord2VecConfiguration().getWindowSize());
        assertTrue(walkDirectory.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertFalse(files.contains("vectors.txt"));

        // assert that a text walk has been written
        List<String> lines = Util.readLinesFromGzippedFile(new File(walkDirectory, "walk_file_0.txt.gz"));

        // make sure that we do not paste mass text
        for (String line : lines) {
            String[] splitLine = line.split(" ");
            assertTrue(splitLine.length <= 8);
            assertTrue(splitLine[0].equals("W") || splitLine[0].equals("Z"));
        }

        // look for specific walk
        String expectedString = "W rdf:Description freude schöner götterfunken tochter aus elysium";
        assertTrue(lines.contains(expectedString), "Could not find the expected line: '" + expectedString + "'\nIn the walks\n:" + transformToString(lines));

        // test sample parameter
        assertEquals(0.01, Main.getRdf2VecInstance().getWord2VecConfiguration().getSample());

        try {
            FileUtils.forceDelete(walkDirectory);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
        }
    }

    /**
     * Transform the toBeTransformed list to a single string.
     *
     * @param toBeTransformed List that shall be converted into a single String.
     * @return Single String.
     */
    private String transformToString(List<String> toBeTransformed) {
        StringBuilder buffer = new StringBuilder();
        for (String s : toBeTransformed) {
            buffer.append(s).append("\n");
        }
        return buffer.toString();
    }

    @Test
    public void analyzeVocabFail() {
        // just making sure nothing fails
        try {
            Main.main(new String[]{"-analyzeVocab", "b"});
        } catch (Exception e) {
            LOGGER.error("An exception occurred while calling the analysis wrongly. This should fail gracefully.");
            fail(e);
        }
    }

    @Test
    void analyzeVocabWithInputFile() {
        // This just tests whether everything works fine end-to-end.
        // It is hard to check the console output for correctness.
        // The correct results for the test cases stated here are checked in the test of the VocabularyAnalyzer!
        try {
            Main.main(new String[]{"-analyzeVocabulary", getPathOfResource("pizza_full_model.kv"), getPathOfResource("pizza.ttl")});
        } catch (Exception e) {
            LOGGER.error("An exception occurred while calling the analysis. This should not fail.");
            fail(e);
        }
    }

    @Test
    void analyzeVocabWithEntityFile() {
        // This just tests whether everything works fine end-to-end.
        // It is hard to check the console output for correctness.
        // The correct results for the test cases stated here are checked in the test of the VocabularyAnalyzer!
        try {
            Main.main(new String[]{"-analyzeVocab", getPathOfResource("freude_vectors.txt"), getPathOfResource("freude_vectors_incomplete_concepts.txt")});
        } catch (Exception e) {
            LOGGER.error("An exception occurred while calling the analysis. This should not fail.");
            fail(e);
        }
    }

    @Test
    public void testTxtVectorGeneration() {
        String modelFilePath = loadFile("test_model_vectors.kv").getAbsolutePath();
        File modelFile = new File(modelFilePath);
        if (!modelFile.exists()) {
            fail("Could not find required test file.");
        }
        String[] args = {"-generateTxtVectorFile", modelFilePath};
        Main.main(args);
        File vectorFile = new File(modelFile.getParentFile().getAbsolutePath(), "vectors.txt");
        assertTrue(vectorFile.exists());
        assertTrue(getNumberOfLines(vectorFile) > 5);
        vectorFile.delete();
    }

    @Test
    public void testTxtVectorGenerationFail() {
        String modelFilePath = loadFile("test_model_vectors.kv").getAbsolutePath();
        File modelFile = new File(modelFilePath);
        if (!modelFile.exists()) {
            fail("Could not find required test file.");
        }
        String[] args = {"-generateTxtVectorFile"};
        Main.main(args);
        File vectorFile = new File(modelFile.getParentFile().getAbsolutePath(), "vectors.txt");
        assertFalse(vectorFile.exists());
    }

    @Test
    public void walkGenerationClassic() {
        File pizzaOntology = loadFile("pizza.ttl");

        String directoryName = "./classicWalks/";
        File walkDirectory = new File(directoryName);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        String[] mainArgs = new String[]{"-graph", pizzaOntology.getAbsolutePath(), "-onlyWalks", "-walkDir", directoryName};
        Main.main(mainArgs);

        HashSet<String> files = Sets.newHashSet(walkDirectory.list());

        // assert that all files are there
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertFalse(files.contains("model.txt"));
        assertTrue(files.contains("walk_file_0.txt.gz"));

        GZIPInputStream gzip = null;
        try {
            File walkFile = new File(walkDirectory, "walk_file_0.txt.gz");
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
        assertTrue(files.contains("walk_file_0.txt.gz"));

        gzip = null;
        try {
            File walkFile = new File(walkDirectory, "walk_file_0.txt.gz");
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
        String entityFilePath = loadFile("dummyEntities.txt").getAbsolutePath();
        String graphFilePath = loadFile("dummyGraph.nt").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-light", entityFilePath, "-walkDir", lightWalks.getAbsolutePath()};
        Main.main(args);

        assertTrue(Main.getRdf2VecInstance().getClass().equals(RDF2VecLight.class));
        assertTrue(lightWalks.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(lightWalks.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertTrue(files.contains("vectors.txt"));

        try {
            FileUtils.forceDelete(lightWalks);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
        }
    }

    @ParameterizedTest
    @EnumSource(value = WalkGenerationMode.class, names = {"MID_WALKS", "MID_WALKS_DUPLICATE_FREE"})
    public void trainLightTdb(WalkGenerationMode walkGenerationMode) {
        LOGGER.info("Starting TDB test.");
        Main.reset();
        File lightWalks = new File("./mainLightWalksTdb/");
        lightWalks.mkdir();
        lightWalks.deleteOnExit();
        String entityFilePath = loadFile("tdbEntitySubset.txt").getAbsolutePath();
        String graphFilePath = loadFile("pizza_tdb").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-light", entityFilePath, "-walkDir", lightWalks.getAbsolutePath(),
                "-walkGenerationMode", walkGenerationMode.toString()};
        Main.main(args);

        assertTrue(Main.getRdf2VecInstance().getClass().equals(RDF2VecLight.class));

        // the default strategy for light:
        assertEquals(walkGenerationMode, Main.getWalkGenerationMode());
        assertTrue(lightWalks.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(lightWalks.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertTrue(files.contains("vectors.txt"));

        // make sure that there is a vector for http://www.co-ode.org/ontologies/pizza/pizza.owl#Siciliana
        Gensim.getInstance().isInVocabulary("http://www.co-ode.org/ontologies/pizza/pizza.owl#Siciliana",
                new File(lightWalks, "model.kv"));

        try {
            FileUtils.forceDelete(lightWalks);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
        }
        Main.reset();
    }

    /**
     * Test light run with explicit statement that no vector text file shall be written.
     */
    @Test
    public void trainLightNoVectorTextFile() {
        File lightWalks = new File("./mainLightWalks/");
        lightWalks.mkdir();
        lightWalks.deleteOnExit();
        String entityFilePath = loadFile("dummyEntities.txt").getAbsolutePath();
        String graphFilePath = loadFile("dummyGraph.nt").getAbsolutePath();
        String[] args = {"-graph", graphFilePath, "-light", entityFilePath, "-walkDir", "./mainLightWalks/",
                "-noVectorTextFileGeneration", "-port", "11121"};
        Main.main(args);

        assertEquals(RDF2VecLight.class, Main.getRdf2VecInstance().getClass());
        assertTrue(lightWalks.listFiles().length > 0);
        assertEquals(RDF2VecLight.DEFAULT_WALK_GENERATION_MODE, Main.getWalkGenerationMode());
        HashSet<String> files = Sets.newHashSet(lightWalks.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));
        assertFalse(files.contains("vectors.txt"));

        try {
            FileUtils.forceDelete(lightWalks);
        } catch (IOException ioe) {
            LOGGER.error("Failed to clean up after test.", ioe);
        }
    }

    @Test
    public void parameterCheck() {
        String entityFilePath = loadFile("dummyEntities.txt").getAbsolutePath();
        String graphFilePath = loadFile("dummyGraph.nt").getAbsolutePath();
        Main.main(new String[]{"-graph", graphFilePath, "-light", entityFilePath, "-numberOfWalks", "100", "-minCount", "3", "-walkMode", "random_walks_duplicate_free"});
        assertEquals(100, (Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(3, (Main.getRdf2VecInstance()).getWord2VecConfiguration().getMinCount());
        assertEquals(WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE, Main.getRdf2VecInstance().getWalkGenerationMode());

        // important: reset
        Main.reset();

        // without light option
        Main.main(new String[]{"-graph", graphFilePath, "-numberOfWalks", "100", "-minCount", "2"});
        assertEquals(100, ((RDF2Vec) Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(2, ((RDF2Vec) Main.getRdf2VecInstance()).getWord2VecConfiguration().getMinCount());
    }

    @Test
    void checkRequirements() {
        Main.main(new String[]{"-checkInstallation"});
    }

    @Test
    void writeVocabularyToFile() {
        File modelFile = new File(getPathOfResource("test_model_vectors.kv"));
        Main.main(new String[]{"-generateVocabularyFile", modelFile.getAbsolutePath()});
        File vocabFile = new File(modelFile.getParentFile().getAbsolutePath(), "vocabulary.txt");
        vocabFile.deleteOnExit();
        assertTrue(vocabFile.exists());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocabFile), StandardCharsets.UTF_8));
            Set<String> vocabulary = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                vocabulary.add(line);
            }
            assertTrue(vocabulary.contains("Europe"));
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void parameterCheckNegative() {
        String entityFilePath = loadFile("dummyEntities.txt").getAbsolutePath();
        String graphFilePath = loadFile("dummyGraph.nt").getAbsolutePath();
        Main.main(new String[]{"-graph", graphFilePath, "-light", entityFilePath, "-numberOfWalks", "-10", "-minCount", "-3"});
        assertEquals(Main.DEFAULT_NUMBER_OF_WALKS, (Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(Word2VecConfiguration.MIN_COUNT_DEFAULT, (Main.getRdf2VecInstance()).getWord2VecConfiguration().getMinCount());
        assertEquals(0, Main.getIgnoredArguments().size());

        // important: reset
        Main.reset();

        // without light option
        Main.main(new String[]{"-graph", graphFilePath, "-numberOfWalks", "abc", "-minCount", "abc"});
        assertEquals(Main.DEFAULT_NUMBER_OF_WALKS, (Main.getRdf2VecInstance()).getNumberOfWalksPerEntity());
        assertEquals(Word2VecConfiguration.MIN_COUNT_DEFAULT, (Main.getRdf2VecInstance()).getWord2VecConfiguration().getMinCount());
    }

    @Test
    public void getHelp() {
        String result = Main.getHelp();
        assertNotNull(result);

        // print the help for manual inspection
        System.out.println(result);

        // printing some empty lines to better check the output manually
        System.out.println("\n\n\n\n\n");

        // just making sure there is no exception thrown etc. and the program is not running after calling help.
        Main.main(new String[]{"-help"});

        // check ignored arguments
        assertEquals(0, Main.getIgnoredArguments().size());
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
        File graphFileToUse = loadFile("swdf-2012-11-28.hdt");

        // prepare directory
        File walkDirectory = new File("./walksOnly/");
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        String lightFilePath = loadFile("./swdf_light_entities.txt").getAbsolutePath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "100", "-light", lightFilePath, "-onlyWalks", "-walkDir", "./walksOnly/"});

        // check ignored arguments
        assertEquals(0, Main.getIgnoredArguments().size());

        // check default walk generation mode
        assertEquals(RDF2VecLight.DEFAULT_WALK_GENERATION_MODE, Main.getWalkGenerationMode());

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file_0.txt.gz");
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

            assertTrue(100 <= heikoCount);
            assertTrue(100 <= heinerCount);
            assertTrue(100 <= pcmCount);

            reader.close();
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file.");
        }

        // clean up
        walkDirectory.delete();
    }

    @Test
    public void midEdgeWalksDuplicateFree() {
        File graphFileToUse = loadFile("./dummyGraph_3.nt");
        String directoryName = "./midEdgeWalksDuplicateFreeDirectory";
        File directory = new File(directoryName);
        directory.deleteOnExit();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "10", "-walkDir",
                directoryName, "-walkGenerationMode", "MID_EDGE_WALKS_DUPLICATE_FREE", "-depth", "3"});

        // check ignored arguments
        assertEquals(0, Main.getIgnoredArguments().size());

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(directory.list());
        assertTrue(files.contains("model.kv"));

        // now check out the walk file
        try {
            File walkFile = new File(directory, "walk_file_0.txt.gz");
            assertTrue(walkFile.exists(), "The walk file does not exist.");
            assertFalse(walkFile.isDirectory(), "The walk file is a directory (expected: file).");

            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(walkFile));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));

            String readLine;
            int numberOfLines = 0;
            while ((readLine = reader.readLine()) != null) {
                numberOfLines++;
                //System.out.println(readLine);

                String[] tokens = readLine.split(" ");
                boolean nonPropertyAppeared = false;
                for (String token : tokens) {
                    if (!token.startsWith("P")) {
                        assertFalse(nonPropertyAppeared);
                        nonPropertyAppeared = true;
                    }
                }
            }
            assertTrue(numberOfLines > 10);
            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file due to a file not found exception.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read from walk file.");
        }
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
            dataSet = HDTManager.loadHDT(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not load HDT file.");
        }
        HdtWalkGenerator.serializeDataSetAsNtFile(dataSet, graphFileToUse);

        // prepare directory
        String directoryName = "./walksOnlyMidWeighted/";
        File walkDirectory = new File(directoryName);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        String lightFilePath = loadFile("./swdf_light_entities.txt").getAbsolutePath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "10", "-light", lightFilePath, "-onlyWalks", "-walkDir", directoryName, "-walkGenerationMode", "mid_walks_weighted", "-depth", "3"});

        // check ignored arguments
        assertEquals(0, Main.getIgnoredArguments().size());

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));

        assertEquals(WalkGenerationMode.MID_WALKS_WEIGHTED, Main.getWalkGenerationMode());
        assertEquals(3, Main.getDepth());

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file_0.txt.gz");
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
            assertTrue(10 <= heikoCount, "heikoCount not within boundaries. Value: " + heikoCount);
            assertTrue(10 <= heinerCount, "heinerCount not within boundaries. Values: " + heinerCount);
            assertTrue(10 <= pcmCount, "pcmCount not within boundaries. Values: " + pcmCount);

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file due to a file not found exception.");
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

            // check ignored arguments
            assertEquals(1, Main.getIgnoredArguments().size());
            Main.getIgnoredArguments().contains("-helloWorld");
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
            dataSet = HDTManager.loadHDT(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not load HDT file.");
        }
        HdtWalkGenerator.serializeDataSetAsNtFile(dataSet, graphFileToUse);

        // prepare directory
        File walkDirectory = new File("./walksOnly/");
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();


        String lightFilePath = loadFile("./swdf_light_entities.txt").getAbsolutePath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "1000", "-light", lightFilePath, "-onlyWalks", "-walkDir", "./walksOnly/", "-walkGenerationMode", "mid_walks_duplicate_free", "-depth", "1"});

        // check ignored arguments
        assertEquals(0, Main.getIgnoredArguments().size());

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));

        assertEquals(WalkGenerationMode.MID_WALKS_DUPLICATE_FREE, Main.getWalkGenerationMode());
        assertEquals(1, Main.getDepth());

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file_0.txt.gz");
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

            assertTrue(1 <= heikoCount, "heikoCount not within boundaries. Value: " + heikoCount);
            assertTrue(1 <= heinerCount, "heinerCount not within boundaries. Values: " + heinerCount);
            assertTrue(1 <= pcmCount, "pcmCount not within boundaries. Values: " + pcmCount);

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
            dataSet = HDTManager.loadHDT(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not load HDT file.");
        }
        HdtWalkGenerator.serializeDataSetAsNtFile(dataSet, graphFileToUse);

        // prepare directory
        String walkDirectoryName = "./walksOnly2/";
        File walkDirectory = new File(walkDirectoryName);
        walkDirectory.mkdir();
        walkDirectory.deleteOnExit();

        String lightFilePath = loadFile("swdf_light_entities.txt").getAbsolutePath();
        Main.main(new String[]{"-graph", graphFileToUse.getAbsolutePath(), "-numberOfWalks", "100", "-light", lightFilePath, "-onlyWalks", "-walkDir", walkDirectoryName});

        // check ignored arguments
        assertEquals(0, Main.getIgnoredArguments().size());

        // make sure that there is only a walk file
        HashSet<String> files = Sets.newHashSet(walkDirectory.list());
        assertFalse(files.contains("model.kv"));
        assertFalse(files.contains("model"));
        assertTrue(files.contains("walk_file_0.txt.gz"));

        // now check out the walk file
        try {
            File walkFile = new File(walkDirectory, "walk_file_0.txt.gz");
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

            assertTrue(100 <= heikoCount);
            assertTrue(100 <= heinerCount);
            assertTrue(100 <= pcmCount);

            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            fail("Could not read from walk file, file not found exception.", fnfe);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read from walk file.", e);
        }

        // clean up
        graphFileToUse.delete();
        walkDirectory.delete();
    }

    @Test
    void getValue() {
        assertNull(Main.getValue(null, null));
        assertNull(Main.getValue(null, new String[]{"european", "union"}));
        assertNull(Main.getValue("hello", null));
        assertNull(Main.getValue("-hello", new String[]{"european", "union"}));
        assertEquals("union", Main.getValue("-european", new String[]{"-european", "union"}));
    }

    @Test
    void continueOption() {
        Main.reset();
        File continueFile = loadFile("existing_walk_directory");
        File graphFile = loadFile("pizza.owl.nt");
        File walkDirectory = new File("continue_walks");
        walkDirectory.deleteOnExit();
        String[] mainCommand = {"-graph", graphFile.getAbsolutePath(), "-continue", continueFile.getAbsolutePath(),
                "-walkDirectory", walkDirectory.getAbsolutePath()};
        Main.main(mainCommand);

        assertTrue(walkDirectory.exists());
        assertTrue(new File(walkDirectory, "walk_file_copied.txt.gz").exists());
        assertTrue(new File(walkDirectory, "walk_file_0.txt.gz").exists());
        assertTrue(new File(walkDirectory, "model").exists());
        assertTrue(new File(walkDirectory, "model.kv").exists());
        deleteDirectory(walkDirectory);
        Main.reset();
    }

    /**
     * Helper function to load files in class path that contain spaces.
     *
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName) {
        try {
            File result = FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception) {
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }

    @AfterAll
    static void cleanUp() {
        Gensim.shutDown();
        deleteDirectory("./mainWalksNq/");
        deleteDirectory("./mainWalksOwlText/");
        deleteDirectory("./mainWalksNtText_light/");
        deleteDirectory("./mainWalksNtText/");
        deleteDirectory("./walks");
        deleteDirectory("./python-server");
        deleteDirectory("./extClassic");
        deleteDirectory("./extLight");
        deleteDirectory("./walksOnly");
        deleteDirectory("./walksOnly2");
        deleteDirectory("./classicWalks/");
        deleteDirectory("./mainWalks/");
        deleteDirectory("./walksOnlyMidWeighted/");
        deleteDirectory("./continue_walks/");
        deleteDirectory("./midEdgeWalksDuplicateFreeDirectory");
    }

    private static void deleteDirectory(File directory) {
        deleteDirectory(directory.getAbsolutePath());
    }

    private static void deleteDirectory(String directoryPath) {
        try {
            FileUtils.deleteDirectory(new File(directoryPath));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory '" + directoryPath + "'.", e);
        }
    }

    /**
     * Helper method to obtain the canonical path of a (test) resource.
     *
     * @param resourceName File/directory name.
     * @return Canonical path of resource.
     */
    public String getPathOfResource(String resourceName) {
        try {
            URL res = getClass().getClassLoader().getResource(resourceName);
            if (res == null) throw new IOException();
            File file = Paths.get(res.toURI()).toFile();
            return file.getCanonicalPath();
        } catch (URISyntaxException | IOException ex) {
            LOGGER.info("Cannot create path of resource", ex);
            return null;
        }
    }
}