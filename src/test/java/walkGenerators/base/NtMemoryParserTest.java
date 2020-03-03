package walkGenerators.base;

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

import static org.junit.jupiter.api.Assertions.*;

class NtMemoryParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NtMemoryParserTest.class);

    @org.junit.jupiter.api.Test
    void removeTags() {
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryParser.removeTags("<http://www.w3.org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryParser.removeTags("http://www.w3.org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryParser.removeTags("http://www.w3.org/ns/lemon/ontolex#LexicalEntry"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", NtMemoryParser.removeTags("<http://www.w3.org/ns/lemon/ontolex#LexicalEntry"));
    }

    @org.junit.jupiter.api.Test
    void generateWalkForEntity(){
        testWalkForEntity(getClass().getResource("/dummyGraph.nt").getFile());
        testWalkForEntity(getClass().getResource("/dummyGraph_2.nt").getFile());
    }

    /**
     * For repeated tests.
     * @param graphPath Path to NT file.
     */
    public static void testWalkForEntity(String graphPath){
        NtMemoryParser parser = new NtMemoryParser(graphPath, new DummyWalkGenerator());
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
    void testDepthForRandomWalks(){
        String graphPath = getClass().getResource("/dummyGraph_3.nt").getFile();
        NtMemoryParser parser = new NtMemoryParser(graphPath, new DummyWalkGenerator());
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
            HDT dataSet = HDTManager.loadHDT(getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath());
            HdtParser.serializeDataSetAsNtFile(dataSet, fileToUse);

            NtMemoryParser parser = new NtMemoryParser(fileToUse, new DummyWalkGenerator());
            String concept = "http://data.semanticweb.org/person/amelie-cordier";
            List<String> walks1 = parser.generateMidWalksForEntity(concept, 10, 12);
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


            String hdtPath = getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath();
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
            } catch (IOException e) {
                fail("No exception should occur.", e);
            } catch (NotFoundException e) {
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
            HDT dataSet = HDTManager.loadHDT(getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath());
            HdtParser.serializeDataSetAsNtFile(dataSet, fileToUse);
            NtMemoryParser parser = new NtMemoryParser(fileToUse, new DummyWalkGenerator());

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
                String hdtPath = getClass().getClassLoader().getResource("swdf-2012-11-28.hdt").getPath();
                try {
                    HDT hdtDataSet = HDTManager.loadHDT(hdtPath);
                    for (int i = 2; i < walkArray.length - 1; i += i + 2) {
                        IteratorTripleString iterator = hdtDataSet.search(walkArray[i - 2], walkArray[i - 1], walkArray[i]);
                        assertTrue(iterator.hasNext(), "The following triple appeared in the walk but not in the data set:\n"
                                + walkArray[i - 2] + " " + walkArray[i - 1] + " " + walkArray[i]
                                + "\nSentence:\n" + walk1);
                    }
                } catch (NotFoundException e) {
                    fail("Exception", e);
                } catch (IOException e) {
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



}