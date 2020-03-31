package walkGenerators.base;

import org.junit.jupiter.api.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HdtEntitySelectorTest {

    @Test
    void getEntities() {
        String hdtPath = getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath();
        assertNotNull(hdtPath, "Cannot find test resource.");
        try {
            HdtEntitySelector selector = new HdtEntitySelector(hdtPath);
            Set<String> result = selector.getEntities();
            assertNotNull(result, "The result should not be null.");
            assertTrue(result.size() > 10, "The result does not contain enough data.");
            assertTrue(result.contains("http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member"), "Selector did not find entity 'http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member'.");
        } catch (Exception e) {
            fail("Exception occurred while loading test HDT data set.");
        }
    }

    public static void main(String[] args) throws Exception{
        HdtEntitySelector selector = new HdtEntitySelector("/Users/janportisch/Documents/Research/DBpedia/dbpedia_merged.hdt");
        System.out.println(selector.getEntities().size());
    }

}