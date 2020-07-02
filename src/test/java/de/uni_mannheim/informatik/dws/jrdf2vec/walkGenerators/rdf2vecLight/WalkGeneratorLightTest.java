package de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.rdf2vecLight;

import org.junit.jupiter.api.Test;
import de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.base.WalkGeneratorDefault;
import de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.light.WalkGeneratorLight;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

class WalkGeneratorLightTest {

    /**
     * Requires a working internet connection.
     */
    @Test
    void fullRunWithDBpedia(){
        String resourceFile = getClass().getResource("/sample_dbpedia_nt_file.nt").getFile();
        String entityFile = getClass().getResource("/sample_dbpedia_entity_file.txt").getFile();
        WalkGeneratorLight generatorLight = new WalkGeneratorLight(resourceFile, entityFile);
        generatorLight.generateRandomMidWalks(2, 10, 3);
        File fileToReadFrom = new File("./walks/walk_file.gz");
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
        while((readLine = reader.readLine()) != null){
            String[] tokens = readLine.split(" ");
            assertTrue(tokens.length <= 7);
            for(String token : tokens){
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
        File pizzaOntology = new File(getClass().getResource("/pizza.ttl").getFile());

        String generatedFilePath = "./test_walks_light_pizza.gz";

        WalkGeneratorDefault generator = new WalkGeneratorLight(pizzaOntology, new File(getClass().getResource("/entityFileForPizzaOntology.txt").getFile()));
        generator.generateRandomWalksDuplicateFree(1, 10, 5, generatedFilePath);

        File generatedFile = new File(generatedFilePath);
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
        } catch (IOException e){
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
        File pizzaOntology = new File(getClass().getResource("/pizza.ttl").getFile());

        String generatedFilePath = "./test_walks_light_1.gz";

        HashSet<String> entities = new HashSet<>();
        entities.add("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot");

        WalkGeneratorDefault generator = new WalkGeneratorLight(pizzaOntology, entities);
        generator.generateRandomWalksDuplicateFree(8, 5, 5, generatedFilePath);

        File generatedFile = new File(generatedFilePath);
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
        } catch (IOException e){
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
    void generateRandomMidWalksForEntities(){
        // make sure that there are no walk duplicates
        File pizzaOntology = new File(getClass().getResource("/pizza.ttl").getFile());
        assertTrue(pizzaOntology.exists());

        String generatedFilePath = "./test_walks_light_mid_test.gz";

        HashSet<String> entities = new HashSet<>();
        entities.add("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot");

        WalkGeneratorDefault generator = new WalkGeneratorLight(pizzaOntology, entities);
        generator.generateRandomMidWalks(1, 1000, 1, generatedFilePath);

        File generatedFile = new File(generatedFilePath);
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
        } catch (IOException e){
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(numberOfLines == 1000, "Expected number of lines: 1000; actual: " + numberOfLines);
        generatedFile.delete();
    }


}