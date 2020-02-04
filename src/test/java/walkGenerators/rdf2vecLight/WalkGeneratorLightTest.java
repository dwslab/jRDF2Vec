package walkGenerators.rdf2vecLight;

import org.junit.jupiter.api.Test;
import walkGenerators.classic.WalkGeneratorDefault;
import walkGenerators.light.WalkGeneratorLight;

import java.io.*;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

class WalkGeneratorLightTest {

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
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot"));
        assertFalse(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourCheesesTopping"), "Four Cheese Topping is not in the subset of entities. No walks should be created for this entity.");
        generatedFile.delete();
    }

}