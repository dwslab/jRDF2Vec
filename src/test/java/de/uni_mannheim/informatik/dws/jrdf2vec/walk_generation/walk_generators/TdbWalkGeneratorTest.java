package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TdbWalkGeneratorTest {


    static TdbWalkGenerator walkGenerator;

    @BeforeAll
    static void setup() {
        walkGenerator = new TdbWalkGenerator(loadFile("pizza_tdb").getAbsolutePath());
    }

    @AfterAll
    static void teardown() {
        walkGenerator.close();
    }

    @Test
    void getBackwardTriple() {
        Set<Triple> result = walkGenerator.getBackwardTriples("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping");
        assertTrue(result.size() > 0);

        Triple triple1 = new Triple(
                "http://www.co-ode.org/ontologies/pizza/pizza.owl#PetitPoisTopping",
                "http://www.w3.org/2000/01/rdf-schema#subClassOf",
                "http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping");

        assertTrue(result.contains(triple1));
    }

    @Test
    void generateMidWalksForEntity(){
        String entity = "http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping";
        List<String> walks = walkGenerator.generateMidWalksForEntity(entity, 100, 3);
        assertEquals(100, walks.size());
        for(String walk : walks){
            assertTrue(walk.contains(entity));
            assertTrue(walk.split(" ").length <= 3 * 2 + 1);
        }
    }

    @Test
    void generateMidWalksForEntityDuplicateFree(){
        String entity = "http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping";
        List<String> walks = walkGenerator.generateMidWalksForEntityDuplicateFree(entity, 100, 3);
        assertTrue(walks.size() <= 100);
        for(String walk : walks){
            assertTrue(walk.contains(entity));
            assertTrue(walk.split(" ").length <= 3 * 2 + 1);
        }
    }

    @Test
    void getForwardTriple() {
        Set<Triple> result = walkGenerator.getForwardTriples("http://www.co-ode.org/ontologies/pizza/pizza" +
                ".owl#Siciliana");
        assertTrue(result.size() > 0);

        Triple triple1 = new Triple(
                "http://www.co-ode.org/ontologies/pizza/pizza.owl#Siciliana",
                "http://www.w3.org/2000/01/rdf-schema#subClassOf",
                "http://www.co-ode.org/ontologies/pizza/pizza.owl#NamedPizza");

        // manually tested for bnodes and (not existing!) datatype properties
        assertTrue(result.contains(triple1));
    }

    /**
     * Helper function to load files in class path that contain spaces.
     *
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private static File loadFile(String fileName) {
        try {
            File result =
                    FileUtils.toFile(TdbWalkGeneratorTest.class.getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception) {
            fail("Could not load file.", exception);
            return null;
        }
    }
}