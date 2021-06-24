package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.loadFile;
import static org.junit.jupiter.api.Assertions.*;

class HdtWalkGeneratorTest {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HdtWalkGenerator.class);

    /**
     * Just making sure that the method behaves as assumed.
     */
    @Test
    public void randomNumbers() {
        int zeroCount = 0;
        int oneCount = 0;
        for (int i = 0; i < 1000; i++) {
            int randomNumber = ThreadLocalRandom.current().nextInt(2);
            assertTrue(randomNumber < 2);
            assertTrue(randomNumber >= 0);
            if (randomNumber == 0) {
                zeroCount++;
            } else if (randomNumber == 1) {
                oneCount++;
            } else {
                fail("randomNumber out of bounds: " + randomNumber);
            }
        }
        LOGGER.info("Zero count: " + zeroCount);
        LOGGER.info("One count: " + oneCount);
        if (zeroCount != 0) LOGGER.info("Ratio (one / zero): " + (double) oneCount / (double) zeroCount);
    }

    @Test
    public void testHdtAndTestFile() {
        String hdtPath = loadFile("swdf-2012-11-28.hdt").getAbsolutePath();
        assertNotNull(hdtPath, "Cannot find test resource.");
        try {
            HDT hdtDataSet = HDTManager.loadHDT(hdtPath);
            IteratorTripleString it = hdtDataSet.search("", "", "");
            //while(it.hasNext()){
            //    TripleString ts = it.next();
            //    System.out.println(ts);
            //}
            assertTrue(it.hasNext(), "The iterator needs to be filled.");
        } catch (Exception e) {
            fail("Exception occurred while loading test HDT data set.");
        }
    }

    @Test
    void generateWeightedMidWalksForEntity() {
        try {
            HdtWalkGenerator parser = new HdtWalkGenerator(loadFile("swdf-2012-11-28.hdt"));
            String concept = "http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member";

            List<String> walks = parser.generateWeightedMidWalksForEntity(concept, 100, 3);
            assertTrue(walks.size() <= 100);
            for(String walk : walks){
                assertTrue(walk.contains(concept));
                assertTrue(walk.split(" ").length <= 3 * 2 + 1, "Wrong walk length for walk:\n" + walk);
            }
        } catch (IOException ioe) {
            LOGGER.error("HDT Init error.");
            fail("Init should not fail.");
        }
    }

    @Test
    public void generateMidWalkForEntity() {
        try {
            HdtWalkGenerator parser = new HdtWalkGenerator(loadFile("swdf-2012-11-28.hdt"));
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
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("HDT Init error.");
            fail("Init should not fail.");
        }
    }

    @Test
    public void generateMidWalksForEntityDuplicateFree() {
        try {
            HdtWalkGenerator parser = new HdtWalkGenerator(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
            String concept = "http://data.semanticweb.org/person/amelie-cordier";
            int numberOfWalks = 100;
            int depth = 1;
            List<String> walks1 = parser.generateMidWalksForEntityDuplicateFree(concept, numberOfWalks, depth);
            assertNotNull(walks1);

            // check number of generated walks
            assertTrue(walks1.size() < numberOfWalks);

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
        } catch (IOException ioe) {
            LOGGER.error("HDT Init error.");
            fail("Init should not fail.");
        }
    }

    @Test
    public void generateMidWalksForEntity() {
        try {
            HdtWalkGenerator parser = new HdtWalkGenerator(loadFile("swdf-2012-11-28.hdt").getAbsolutePath());
            String concept = "http://data.semanticweb.org/person/amelie-cordier";
            int numberOfWalks = 12;
            int depth = 10;
            List<String> walks1 = parser.generateMidWalksForEntity(concept, numberOfWalks, depth);
            assertNotNull(walks1);

            // check number of generated walks
            assertTrue(walks1.size() == numberOfWalks);

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
        } catch (IOException ioe) {
            LOGGER.error("HDT Init error.");
            fail("Init should not fail.");
        }
    }

    @Test
    void isSameListContent() {
        List<String> list_1 = new ArrayList<>();
        list_1.add("A");
        list_1.add("B");

        List<String> list_2 = new ArrayList<>();
        list_2.add("A");
        list_2.add("B");

        List<String> list_3 = new ArrayList<>();
        list_3.add("A");
        list_3.add("C");

        // default
        assertTrue(list_1.equals(list_2));
        assertFalse(list_1.equals(list_3));
    }
}