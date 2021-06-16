package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NtMemoryWalkGeneratorTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(NtMemoryWalkGeneratorTest.class);

    @Test
    void removeTags() {
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryWalkGenerator.removeTags("<http://www.w3.org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryWalkGenerator.removeTags("http://www.w3.org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryWalkGenerator.removeTags("http://www.w3.org/ns/lemon/ontolex#LexicalEntry"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryWalkGenerator.removeTags("<http://www.w3.org/ns/lemon/ontolex#LexicalEntry"));
    }

    @Test
    void generateWalkForEntity() {
        testWalkForEntity(loadFile("dummyGraph.nt").getAbsolutePath());
        testWalkForEntity(loadFile("dummyGraph_2.nt").getAbsolutePath());
    }

    @Test
    void generateNodeWalksForEntity() {
        File dummyGraphFile = loadFile("dummyGraph.nt");
        assertNotNull(dummyGraphFile);
        String dummyGraphFilePath = dummyGraphFile.getAbsolutePath();

        NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator(dummyGraphFilePath);
        List<String> result = parser.generateNodeWalksForEntity("A", 100, 8);
        assertTrue(result.size() >= 2);
        for (String sentence : result) {
            assertFalse(sentence.toLowerCase().contains("p"));
        }
    }

    /**
     * For repeated tests.
     * @param graphPath Path to NT file.
     */
    public static void testWalkForEntity(String graphPath){
        NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator(graphPath);
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
    void generateMidTypeWalksForEntityDuplicateFree(){
        NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator(loadFile("pizza.owl.nt").getAbsolutePath());
        List<String> result_1 = parser.generateMidTypeWalksForEntityDuplicateFree("http://www.co-ode" +
                ".org/ontologies/pizza/pizza.owl#DeepPanBase", 10, 5);

        // somewhat cheap test but the given dataset is not sufficient for testing this
        assertNotNull(result_1);
        assertTrue(result_1.size() > 0);

        // for debugging
        //for(String s : result_1) System.out.println(s);

        parser = new NtMemoryWalkGenerator(loadFile("type_file.nt").getAbsolutePath());
        List<String> result_2 = parser.generateMidTypeWalksForEntityDuplicateFree("http://www.jan-portisch.eu/I_Jan", 150, 5);

        // for debugging
        for(String s : result_2) System.out.println(s);

        for(String walk : result_2){
            boolean instanceAppeared = false;
            for(String token : walk.split(" ")){
                if(token.startsWith("http://www.jan-portisch.eu/I_")){
                    assertFalse(instanceAppeared);
                    instanceAppeared = true;
                }
            }
        }
        Set<String> walks = new HashSet<>(result_2);
        assertTrue(walks.contains("http://www.jan-portisch.eu/P_knows http://www.jan-portisch.eu/I_Jan http://www.jan-portisch.eu/P_knows http://www.jan-portisch.eu/C_human"));
    }

    @Test
    void generateMidEdgeWalksForEntityDuplicateFree(){
        NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator(loadFile("dummyGraph.nt").getAbsolutePath());
        List<String> result_1 = parser.generateMidEdgeWalksForEntityDuplicateFree("A", 100, 8);

        // for debugging
        //for(String s : result_1) System.out.println(s);

        // A never (!) appears as object. Hence all walks must start with A. Afterwards all walks must
        // start with P in this example.
        for(String walk : result_1){
            String[] tokens = walk.split(" ");
            for(int i = 0; i< tokens.length; i++){
                if(i == 0){
                    assertEquals("A", tokens[i]);
                } else {
                    assertTrue(tokens[i].startsWith("P"));
                }
            }
        }

        List<String> result_2 = parser.generateMidEdgeWalksForEntityDuplicateFree("V2", 100, 8);

        // for debugging
        // for(String s : result_2) System.out.println(s);

        // V2 is referred to. Make sure there is only one non-property in the walk which is V2.
        for(String walk : result_2){
            boolean containsV2 = false;
            for(String token : walk.split(" ")){
                if(token.equals("V2")){
                    assertFalse(containsV2);
                    containsV2 = true;
                } else {
                    assertTrue(token.startsWith("P"));
                }
            }
            assertTrue(containsV2);
        }

        // check with infinity loops:
        parser = new NtMemoryWalkGenerator(loadFile("dummyGraph_3.nt").getAbsolutePath());
        List<String> result_3 = parser.generateMidEdgeWalksForEntityDuplicateFree("V1", 100, 8);

        // for debugging
        for(String s : result_3) System.out.println(s);


        // V2 is referred to. Make sure there is only one non-property in the walk which is V2.
        for(String walk : result_3){
            boolean containsV1 = false;
            String[] tokens = walk.split(" ");

            // ensure a correct walk length
            // Node of interest only once -> use 9
            // General case -> use 2 * depth + 1
            assertTrue(tokens.length <= 9, "Problematic Walk:\n" + walk);

            for(String token : tokens){
                if(token.equals("V1")){
                    // make sure our node of interest appears only once
                    assertFalse(containsV1);
                    containsV1 = true;
                } else {
                    assertTrue(token.startsWith("P"));
                }
            }
            assertTrue(containsV1);
        }
    }

    @Test
    void testDepthForRandomWalks(){
        String graphPath = loadFile("dummyGraph_3.nt").getAbsolutePath();
        NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator(graphPath);
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

        int maxLengh = 0;
        for(String s : result_3){
            int length = s.split(" ").length;
            if(length > maxLengh) maxLengh = length;
        }

        assertEquals(1 + 2 * 8, maxLengh);

        List<String> result_4 = parser.generateDuplicateFreeRandomWalksForEntity("W", 100, 1);
        System.out.println("\nWalks 4");
        for(String s : result_4) System.out.println(s);

        maxLengh = 0;
        for(String s : result_4){
            int length = s.split(" ").length;
            if(length > maxLengh) maxLengh = length;
        }

        assertEquals(3, maxLengh);
    }

    @Test
    void generateMidWalksForEntity(){
        try {
            // prepare file
            File fileToUse = new File("./swdf-2012-11-28.nt");
            HDT dataSet = HDTManager.loadHDT(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
            HdtWalkGenerator.serializeDataSetAsNtFile(dataSet, fileToUse);

            NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator(fileToUse);
            String concept = "http://data.semanticweb.org/person/amelie-cordier";
            List<String> walks1 = parser.generateMidWalksForEntity(concept, 12, 12);
            assertNotNull(walks1);

            // check number of generated walks
            assertEquals(walks1.size(), 12);

            nextWalk:
            for (String walk : walks1) {

                // check walk size
                assertEquals((walk.split(" ").length % 2), 1.0, "Walks must be uneven. Number of elements in walk: " + walk.split(" ").length + "\nWalk:\n" + walk);

                for (String component : walk.split(" ")) {
                    if (component.equals(concept)) {
                        continue nextWalk;
                    }
                }

                // check whethe the target entity occurs
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
            } catch (Exception e) {
                fail("No exception should occur.", e);
            } finally {
                fileToUse.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void generateMidWalkForEntity() {
        try {
            // prepare file
            File fileToUse = new File("./swdf-2012-11-28.nt");
            HDT dataSet = HDTManager.loadHDT(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
            HdtWalkGenerator.serializeDataSetAsNtFile(dataSet, fileToUse);
            NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator(fileToUse);

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
                } catch (Exception e) {
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
        NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator();
        parser.setParseDatatypeProperties(true);
        assertTrue(parser.isParseDatatypeProperties);
        parser.readNTriples(loadFile("dummyGraph_with_labels.nt").getAbsolutePath());
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
        parser = new NtMemoryWalkGenerator(false);
        assertFalse(parser.isParseDatatypeProperties);
        parser.readNTriples(loadFile("dummyGraph_with_labels.nt").getAbsolutePath());
        result = parser.getData();
        assertEquals(0, result.getUniqueDatatypeTripleSubjects().size());
        assertTrue(result.getAllObjectTriples().contains(new Triple("W","P7", "V2")));
        assertFalse(result.getUniqueObjectTriplePredicates().contains("rdfs:label"));
    }

    @Test
    void generateTextWalksForEntity(){
        NtMemoryWalkGenerator parser = new NtMemoryWalkGenerator();
        parser.setParseDatatypeProperties(true);
        assertTrue(parser.isParseDatatypeProperties);
        parser.readNTriples(loadFile("dummyGraph_with_labels.nt"));

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