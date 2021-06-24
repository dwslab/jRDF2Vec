package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.loadFile;
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

        assertNotNull(walkGenerator.getBackwardTriples("ERROR_URL"));
    }

    @Test
    void generateDuplicateFreeRandomWalksForEntity(){
        String entity = "http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping";
        List<String> result = walkGenerator.generateDuplicateFreeRandomWalksForEntity(entity, 100, 4);
        assertTrue(result.size() > 0);
        assertTrue(result.size() <= 100);
        for(String walk : result){
            assertTrue(walk.split(" ")[0].equals(entity));
            assertTrue(walk.split(" ").length <= 4 * 2 + 1);
        }
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
    void generateWeightedMidWalksForEntity(){
        String entity = "http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping";
        List<String> walks = walkGenerator.generateWeightedMidWalksForEntity(entity, 100, 3);
        assertTrue(walks.size() <= 100);
        for(String walk : walks){
            assertTrue(walk.contains(entity));
            assertTrue(walk.split(" ").length <= 3 * 2 + 1);
        }
    }

    @Test
    void getForwardTriples() {
        Set<Triple> result = walkGenerator.getForwardTriples("http://www.co-ode.org/ontologies/pizza/pizza" +
                ".owl#Siciliana");

        Triple triple1 = new Triple(
                "http://www.co-ode.org/ontologies/pizza/pizza.owl#Siciliana",
                "http://www.w3.org/2000/01/rdf-schema#subClassOf",
                "http://www.co-ode.org/ontologies/pizza/pizza.owl#NamedPizza");

        // manually tested for bnodes and (not existing!) datatype properties
        assertTrue(result.contains(triple1));

        // test error case
        assertNotNull(walkGenerator.getForwardTriples("ERROR_URL"));
    }
}