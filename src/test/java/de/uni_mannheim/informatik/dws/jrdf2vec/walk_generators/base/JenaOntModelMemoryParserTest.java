package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.Triple;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class JenaOntModelMemoryParserTest {

    @Test
    void readDataFromFile() {
        File ontologyTestFile = new File(JenaOntModelMemoryParserTest.class.getClassLoader().getResource("pizza.owl.xml").getPath());
        JenaOntModelMemoryParser parser = new JenaOntModelMemoryParser();
        parser.readDataFromFile(ontologyTestFile);
        assertTrue(parser.getData().getSize() > 100);

        ArrayList<Triple> triplesForArtichokeTopping = parser.getData().getTriplesInvolvingSubject("http://www.co-ode.org/ontologies/pizza/pizza.owl#ArtichokeTopping");
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
}