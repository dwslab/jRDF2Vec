package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;
import org.junit.jupiter.api.Test;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.loadFile;
import static org.junit.jupiter.api.Assertions.*;

class NxMemoryWalkGeneratorTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(NxMemoryWalkGeneratorTest.class);

    /**
     * Remark: dummyGraph.nt not parsable.
     */
    @Test
    void generateWalkForEntity(){
        NxMemoryWalkGenerator parser = new NxMemoryWalkGenerator(loadFile("dummyGraph_2.nt"));
        List<String> result_1 = parser.generateDuplicateFreeRandomWalksForEntity("A", 100, 8);
        System.out.println("Walks 1");
        for(String s : result_1) System.out.println(s);
        assertEquals(3, result_1.size());
        for(String s : result_1){
            assertTrue(s.equals("A P4 E P5 D") || s.equals("A P4 E P6 F") || s.equals("A P1 B P2 C P3 D"));
        }

        List<String> result_2 = parser.generateDuplicateFreeRandomWalksForEntity("Z", 3, 8);
        System.out.println("\nWalks 2");
        for(String s : result_2) System.out.println(s);
        assertEquals(3, result_2.size());

        List<String> result_3 = parser.generateDuplicateFreeRandomWalksForEntity("W", 100, 8);
        System.out.println("\nWalks 3");
        for(String s : result_3) System.out.println(s);
        assertEquals(7, result_3.size());
    }

    @Test
    void generateMidWalksForEntity(){
        try {
            // prepare file
            File fileToUse = new File("./swdf-2012-11-28.nt");
            HDT dataSet = HDTManager.loadHDT(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
            HdtWalkGenerator.serializeDataSetAsNtFile(dataSet, fileToUse);

            NxMemoryWalkGenerator parser = new NxMemoryWalkGenerator(fileToUse);
            String concept = "http://data.semanticweb.org/person/amelie-cordier";
            List<String> walks1 = parser.generateMidWalksForEntity(concept, 12, 10);
            assertNotNull(walks1);

            // check number of generated walks
            assertTrue(walks1.size() == 12);

            nextWalk:
            for (String walk : walks1) {

                // check walk size
                assertTrue((walk.split(" ").length % 2) == 1.0, "Walks must be uneven. Number of elements in walk: " + walk.split(" ").length + "\nWalk:\n" + walk);

                for (String component : walk.split(" ")) {
                    if (component.equals(concept)) {
                        continue nextWalk;
                    }
                }

                // check whether the target entity occurs
                fail("No occurrence of " + concept + " in sentence: " + walk);
            }


            String hdtPath = loadFile("swdf-2012-11-28.hdt").getAbsolutePath();
            try {
                HDT hdtDataSet = HDTManager.loadHDT(hdtPath);
                for (String walk : walks1) {
                    String[] walkArray = walk.split(" ");
                    for (int i = 2; i < walkArray.length - 1; i += i + 2) {
                        IteratorTripleString iterator = hdtDataSet.search(walkArray[i - 2], walkArray[i - 1], walkArray[i]);
                        assertTrue(iterator.hasNext(), "The following triple appeared in the walk but not in the data set:\n"
                                + walkArray[i - 2] + " " + walkArray[i - 1] + " " + walkArray[i]
                                + "\nSentence:\n" + walk);
                    }
                }
            } catch (IOException | NotFoundException e) {
                fail("No exception should occur.", e);
            }

            fileToUse.delete();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void setLinesToCheck(){
        NxMemoryWalkGenerator parser = new NxMemoryWalkGenerator();
        parser.setLinesToCheck(20);
        assertEquals(20, parser.getLinesToCheck());
        parser.setLinesToCheck(-1);
        assertEquals(NxMemoryWalkGenerator.DEFAULT_CHECK_LINES, parser.getLinesToCheck());
    }

    @Test
    public void generateMidWalkForEntity() {
        try {
            // prepare file
            File fileToUse = new File("./swdf-2012-11-28.nt");
            HDT dataSet = HDTManager.loadHDT(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
            HdtWalkGenerator.serializeDataSetAsNtFile(dataSet, fileToUse);
            NxMemoryWalkGenerator parser = new NxMemoryWalkGenerator(fileToUse);

            String concept = "http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member";

            for (int depth = 1; depth < 10; depth++) {
                List<String> walk1 = parser.generateMidWalkForEntity(concept, depth);
                assertNotNull(walk1);
                assertTrue(walk1.size() <= depth * 2 + 1, "The walk is supposed to have at most " + (depth * 2 + 1) + " elements. It has: " + walk1.size()
                        + "\nWalk:\n" + walk1);
                assertTrue(walk1.size() >= 3, "The walk must consist of at least 3 elements. Walk:\n" + walk1);

                String[] walkArray = new String[walk1.size()];
                for (int i = 0; i < walkArray.length; i++) {
                    walkArray[i] = walk1.get(i);
                }
                String hdtPath = loadFile("swdf-2012-11-28.hdt").getAbsolutePath();
                try {
                    HDT hdtDataSet = HDTManager.loadHDT(hdtPath);
                    for (int i = 2; i < walkArray.length - 1; i += i + 2) {
                        IteratorTripleString iterator = hdtDataSet.search(walkArray[i - 2], walkArray[i - 1], walkArray[i]);
                        assertTrue(iterator.hasNext(), "The following triple appeared in the walk but not in the data set:\n"
                                + walkArray[i - 2] + " " + walkArray[i - 1] + " " + walkArray[i]
                                + "\nSentence:\n" + walk1);
                    }
                } catch (NotFoundException | IOException e) {
                    fail("Exception", e);
                } finally {
                    fileToUse.delete();
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("HDT Init error.");
            fail("Init should not fail.");
        }
    }

    @Test
    void testDatatypeTripleParsing(){
        NxMemoryWalkGenerator parser = new NxMemoryWalkGenerator();
        parser.setParseDatatypeProperties(true);
        assertTrue(parser.isParseDatatypeProperties);
        parser.readNtriples(loadFile("dummyGraph_with_labels.nt"));
        TripleDataSetMemory result = parser.getData();
        assertNotNull(result);
        Map<String, Set<String>> datatypeTuplesForW = result.getDatatypeTuplesForSubject("W");
        assertEquals(2, datatypeTuplesForW.size());
        assertFalse(datatypeTuplesForW.containsKey("P7"));
        assertTrue(datatypeTuplesForW.containsKey("rdfs:label"));
        assertTrue(datatypeTuplesForW.containsKey("rdf:Description"));

        for(Triple triple : result.getObjectTriplesInvolvingSubject("W")){
            assertFalse(triple.predicate.equals("rdf:Description"));
            assertFalse(triple.predicate.equals("rdfs:label"));
        }

        // make sure we only parse if the mode is true
        parser = new NxMemoryWalkGenerator();
        parser.setParseDatatypeProperties(false);
        assertFalse(parser.isParseDatatypeProperties());
        parser.readNTriples(loadFile("dummyGraph_with_labels.nt").getAbsolutePath());
        result = parser.getData();
        assertEquals(0, result.getUniqueDatatypeTripleSubjects().size());
        assertTrue(result.getAllObjectTriples().contains(new Triple("W","P7", "V2")));
        assertFalse(result.getUniqueObjectTriplePredicates().contains("rdfs:label"));
    }

    @Test
    void generateTextWalksForEntity(){
        NxMemoryWalkGenerator parser = new NxMemoryWalkGenerator();
        parser.setParseDatatypeProperties(true);
        assertTrue(parser.isParseDatatypeProperties);
        parser.readNtriples(loadFile("dummyGraph_with_labels.nt"));

        // walk depth 8
        List<String> result = parser.generateTextWalksForEntity("W", 8);
        assertNotNull(result);
        assertTrue(result.contains("W rdfs:label gedichte"));
        String expectedSentence = "W rdf:Description wer reitet so spät durch nacht";
        assertTrue(result.contains(expectedSentence), "Could not find String '" + expectedSentence + "'.\n" + transformToString(result) + "\nNumber of walks: " + result.size());
        assertFalse(result.contains("W rdf:Description wer reitet"));

        // walk depth 4
        result = parser.generateTextWalksForEntity("W", 4);
        assertNotNull(result);
        assertTrue(result.contains("W rdfs:label gedichte"));
        assertTrue(result.contains("W rdf:Description wer reitet"));
        assertFalse(result.contains("W rdf:Description wer reitet so spät durch nacht"));
    }

    /**
     * Write the
     * @param toBeTransformed
     * @return
     */
    private String transformToString(List<String> toBeTransformed){
        StringBuffer buffer = new StringBuffer();
        for(String s : toBeTransformed){
            buffer.append(s).append("\n");
        }
        return buffer.toString();
    }

}