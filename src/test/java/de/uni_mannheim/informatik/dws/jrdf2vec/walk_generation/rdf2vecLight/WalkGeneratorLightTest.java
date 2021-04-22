package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.rdf2vecLight;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light.WalkGenerationManagerLight;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import static de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode.MID_WALKS;
import static de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE;
import static org.junit.jupiter.api.Assertions.*;

class WalkGeneratorLightTest {


    /**
     * Requires a working internet connection.
     */
    @Test
    void fullRunWithDBpedia() {
        String resourceFile = loadFile("sample_dbpedia_nt_file.nt").getAbsolutePath();
        String entityFile = loadFile("sample_dbpedia_entity_file.txt").getAbsolutePath();
        WalkGenerationManagerLight generatorLight = new WalkGenerationManagerLight(resourceFile, entityFile);
        generatorLight.generateWalks(MID_WALKS, 2, 10, 3, "./walks");
        generatorLight.close();
        File fileToReadFrom = new File("./walks/walk_file_0.txt.gz");
        GZIPInputStream gzip;
        try {
            gzip = new GZIPInputStream(new FileInputStream(fileToReadFrom));

            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
            String readLine;

            HashSet<String> entities = new HashSet<>();
            entities.add("http://dbpedia.org/resource/Illinois_Tool_Works");
            entities.add("http://dbpedia.org/resource/Host_Hotels_&_Resorts");
            entities.add("http://dbpedia.org/resource/State_Bank_of_India");
            entities.add("http://dbpedia.org/resource/Amp");
            entities.add("http://dbpedia.org/resource/SQM");
            while ((readLine = reader.readLine()) != null) {
                String[] tokens = readLine.split(" ");
                assertTrue(tokens.length <= 7);
                for (String token : tokens) {
                    entities.remove(token);
                }
            }
            assertTrue(entities.size() == 1, "Not all 4 entities occurred in the walks (note that Amp cannot be found).");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Problem occurred while reading the walk file.", e);
        }
        // cleaning up
        fileToReadFrom.delete();
    }

    @Test
    void generateRandomWalksDuplicateFreeTtlWithSelector() {
        File pizzaOntology = loadFile("pizza.ttl");

        File walkDirectory = new File("./test_walks_light_pizza");
        walkDirectory.deleteOnExit();

        WalkGenerationManager generator = new WalkGenerationManagerLight(pizzaOntology, loadFile("entityFileForPizzaOntology.txt"));
        generator.generateWalks(WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE, 1, 10, 5,
                walkDirectory);
        generator.close();

        File generatedFile = walkDirectory.listFiles()[0];
        assertTrue(generatedFile.exists(), "Assert that a walk file has been generated.");

        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(new FileInputStream(generatedFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
        String readLine;
        HashSet<String> subjectsOfWalks = new HashSet<>();
        try {
            while ((readLine = reader.readLine()) != null) {
                subjectsOfWalks.add(readLine.split(" ")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(subjectsOfWalks.size() > 0, "Assert that walks have been generated for more than one entity.");
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#Italy"));
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#OliveTopping"));
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourCheesesTopping"));
        assertFalse(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#RealItalianPizza"), "Real Italian Pizza is not in the subset of entities. No walks should be created for this entity.");
        generatedFile.delete();
    }

    @Test
    void generateRandomWalksDuplicateFreeTtl() {
        File pizzaOntology = loadFile("pizza.ttl");

        File generatedFilePath = new File("./test_walks_light_1");

        HashSet<String> entities = new HashSet<>();
        entities.add("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot");

        WalkGenerationManager generator = new WalkGenerationManagerLight(pizzaOntology, entities);
        generator.generateWalks(RANDOM_WALKS_DUPLICATE_FREE, 8, 5, 5, generatedFilePath);
        generator.close();

        File generatedFile = generatedFilePath.listFiles()[0];
        assertTrue(generatedFile.exists(), "Assert that a walk file has been generated.");

        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(new FileInputStream(generatedFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
        String readLine;
        HashSet<String> subjectsOfWalks = new HashSet<>();
        try {
            while ((readLine = reader.readLine()) != null) {
                subjectsOfWalks.add(readLine.split(" ")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(subjectsOfWalks.size() > 0, "Assert that walks have been generated for more than one entity.");
        assertEquals(1, subjectsOfWalks.size(), "There was only one entitiy specified for which walks shall be generated.");
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot"));
        assertFalse(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourCheesesTopping"), "Four Cheese Topping is not in the subset of entities. No walks should be created for this entity.");
        generatedFile.delete();
    }


    @Test
    void generateRandomMidWalksForEntities() {
        // make sure that there are no walk duplicates
        File pizzaOntology = loadFile("pizza.ttl");
        assertTrue(pizzaOntology.exists());

        File generatedFilePath = new File("./test_walks_light_mid_test");

        HashSet<String> entities = new HashSet<>();
        entities.add("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot");

        WalkGenerationManager generator = new WalkGenerationManagerLight(pizzaOntology, entities);
        generator.generateWalks(MID_WALKS, 1, 1000, 1, generatedFilePath);
        generator.close();
        File generatedFile = generatedFilePath.listFiles()[0];
        assertTrue(generatedFile.exists(), "Assert that a walk file has been generated.");

        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(new FileInputStream(generatedFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
        String readLine;
        int numberOfLines = 0;
        try {
            while ((readLine = reader.readLine()) != null) {
                numberOfLines++;
                assertTrue(readLine.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(numberOfLines == 1000, "Expected number of lines: 1000; actual: " + numberOfLines);
        generatedFile.delete();
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

}