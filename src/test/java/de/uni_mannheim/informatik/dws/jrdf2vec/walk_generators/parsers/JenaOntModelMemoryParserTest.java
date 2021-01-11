package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.Triple;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JenaOntModelMemoryParserTest {

    @Test
    void readDataFromFile() {
        File ontologyTestFile = loadFile("pizza.owl.xml");
        JenaOntModelMemoryParser parser = new JenaOntModelMemoryParser();
        parser.readDataFromFile(ontologyTestFile);
        assertTrue(parser.getData().getSize() > 100);

        List<Triple> triplesForArtichokeTopping = parser.getData().getTriplesInvolvingSubject("http://www.co-ode.org/ontologies/pizza/pizza.owl#ArtichokeTopping");
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
}