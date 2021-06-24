package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JenaOntModelMemoryWalkGeneratorTest {


    @Test
    void readDataFromFile() {
        File ontologyTestFile = loadFile("pizza.owl.xml");
        JenaOntModelMemoryWalkGenerator parser = new JenaOntModelMemoryWalkGenerator();
        parser.readDataFromFile(ontologyTestFile);
        assertTrue(parser.getData().getObjectTripleSize() > 100);

        List<Triple> triplesForArtichokeTopping = parser.getData().getObjectTriplesInvolvingSubject("http://www.co-ode.org/ontologies/pizza/pizza.owl#ArtichokeTopping");
        assertTrue(triplesForArtichokeTopping.size() > 0);
        boolean found = false;
        for (Triple triple : triplesForArtichokeTopping){
            assertTrue(triple.subject.equals("http://www.co-ode.org/ontologies/pizza/pizza.owl#ArtichokeTopping"));
            if(triple.predicate.equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")){
                if(triple.object.equals("http://www.co-ode.org/ontologies/pizza/pizza.owl#VegetableTopping")){
                    found = true;
                }
            }
        }
        assertTrue(found);
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            File result =  FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }

    @Test
    void testDatatypeTripleParsing(){
        JenaOntModelMemoryWalkGenerator parser = new JenaOntModelMemoryWalkGenerator();
        parser.setParseDatatypeProperties(true);
        assertTrue(parser.isParseDatatypeProperties);
        parser.readDataFromFile(loadFile("dummyGraph_with_labels.nt"), "N-TRIPLES");
        TripleDataSetMemory result = parser.getData();
        assertNotNull(result);
        Map<String, Set<String>> datatypeTuplesForW = result.getDatatypeTuplesForSubject("W");
        assertEquals(2, datatypeTuplesForW.size());
        assertFalse(datatypeTuplesForW.containsKey("P7"));
        assertTrue(datatypeTuplesForW.containsKey("rdfs:label"));
        assertTrue(datatypeTuplesForW.containsKey("rdf:Description"));

        // making sure datatype triples do not appear as object triples
        for(Triple triple : result.getObjectTriplesInvolvingSubject("W")){
            assertFalse(triple.predicate.equals("rdf:Description"));
            assertFalse(triple.predicate.equals("rdfs:label"));
        }

        // make sure we only parse if the mode is true
        parser = new JenaOntModelMemoryWalkGenerator();
        assertFalse(parser.isParseDatatypeProperties);
        parser.readDataFromFile(loadFile("dummyGraph_with_labels.nt").toString(), "N-TRIPLES");
        result = parser.getData();
        assertEquals(0, result.getUniqueDatatypeTripleSubjects().size());
        assertTrue(result.getAllObjectTriples().contains(new Triple("W","P7", "V2")));
        assertFalse(result.getUniqueObjectTriplePredicates().contains("rdfs:label"));
    }

    @Test
    void generateTextWalksForEntity(){
        JenaOntModelMemoryWalkGenerator parser = new JenaOntModelMemoryWalkGenerator();
        parser.setParseDatatypeProperties(true);
        assertTrue(parser.isParseDatatypeProperties);
        parser.readDataFromFile(loadFile("dummyGraph_with_labels.nt"), "N-TRIPLES");

        // walk depth 8
        List<String> result = parser.generateTextWalksForEntity("W", 8);
        assertNotNull(result);
        assertTrue(result.contains("W rdfs:label gedichte"));
        assertTrue(result.contains("W rdf:Description wer reitet so spät durch nacht"));
        assertFalse(result.contains("W rdf:Description wer reitet"));

        // walk depth 4
        result = parser.generateTextWalksForEntity("W", 4);
        assertNotNull(result);
        assertTrue(result.contains("W rdfs:label gedichte"));
        assertTrue(result.contains("W rdf:Description wer reitet"));
        assertFalse(result.contains("W rdf:Description wer reitet so spät durch nacht"));
    }

}