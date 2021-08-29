package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {


    /**
     * Logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(UtilTest.class);

    @Test
    public void getDeltaTimeString(){
        try {

            // simple case

            Instant before = Instant.now();
            TimeUnit.SECONDS.sleep(3);
            Instant after = Instant.now();
            String result = Util.getDeltaTimeString(before, after);

            assertNotNull(result);
            assertTrue(result.contains("Days: 0"));
            assertTrue(result.contains("Hours: 0"));
            assertTrue(result.contains("Minutes: 0"));
            System.out.println(result);

            Pattern secondsPattern = Pattern.compile("(?<=Seconds:\\s)[0-9]*"); //(?<=Seconds: )[0-9]*$
            Matcher secondsMatcher = secondsPattern.matcher(result);
            secondsMatcher.find();
            assertTrue(Integer.parseInt(secondsMatcher.group()) >= 2);

            // complicated case
            after = after.plus(2, ChronoUnit.DAYS);
            after = after.plus(10, ChronoUnit.HOURS);
            after = after.plus(15, ChronoUnit.MINUTES);
            after = after.plus(5, ChronoUnit.SECONDS);

            result = Util.getDeltaTimeString(before, after);
            System.out.println(result);

            secondsPattern = Pattern.compile("(?<=Seconds:\\s)[0-9]*"); //(?<=Seconds: )[0-9]*$
            secondsMatcher = secondsPattern.matcher(result);
            secondsMatcher.find();
            assertTrue(Integer.parseInt(secondsMatcher.group()) >= 7);

            Pattern minutesPattern = Pattern.compile("(?<=Minutes:\\s)[0-9]*");
            Matcher minutesMatcher = minutesPattern.matcher(result);
            minutesMatcher.find();
            int actualMinutes = Integer.parseInt(minutesMatcher.group());
            assertTrue(actualMinutes == 15);

            Pattern hoursPattern = Pattern.compile("(?<=Hours:\\s)[0-9]*");
            Matcher hoursMatcher = hoursPattern.matcher(result);
            hoursMatcher.find();
            int actualHours = Integer.parseInt(hoursMatcher.group());
            assertTrue(actualHours == 10);

            Pattern daysPattern = Pattern.compile("(?<=Days:\\s)[0-9]*");
            Matcher daysMatcher = daysPattern.matcher(result);
            daysMatcher.find();
            int actualDays = Integer.parseInt(daysMatcher.group());
            assertTrue(actualDays == 2);

        } catch (InterruptedException ie){
            LOGGER.error("Could not perform test.", ie);
        }
    }

    @Test
    void getDimensionalityFromVectorTextFileTest() {
        String walkDirectory = loadFile("freude_vectors.txt").getAbsolutePath();
        assertEquals(3, Util.getDimensionalityFromVectorTextFile(walkDirectory));
        assertEquals(3, Util.getDimensionalityFromVectorTextFile(new File(walkDirectory)));
    }

    @Test
    void getDimensionalityFromVectorTextFileFail(){
        assertEquals(-1, Util.getDimensionalityFromVectorTextFile(""));
        assertEquals(-1, Util.getDimensionalityFromVectorTextFile(new File("")));
    }

    @Test
    public void randomDrawFromHashSet() {
        Set<String> hashSet = new HashSet<>(Arrays.asList("A", "B", "C"));
        int aCount = 0;
        int bCount = 0;
        int cCount = 0;

        for (int i = 0; i < 1000; i++) {
            String drawValue = Util.randomDrawFromSet(hashSet);
            switch (drawValue) {
                case "A":
                    aCount++;
                    break;
                case "B":
                    bCount++;
                    break;
                case "C":
                    cCount++;
                    break;
                default:
                    fail("Invalid value: " + drawValue);
            }
        }
        assertTrue(aCount > 0, "A was never drawn.");
        assertTrue(bCount > 0, "B was never drawn.");
        assertTrue(cCount > 0, "C was never drawn.");
        LOGGER.info("A : B : C  :   " + aCount + " : " + bCount + " : " + cCount);
    }

    @Test
    void uriIsFile(){
        try {
            assertTrue(Util.uriIsFile(new File("./myfile.txt").toURI()));
            assertFalse(Util.uriIsFile(new URI("http://www.jan-portisch.eu")));
        } catch (Exception e){
            fail(e);
        }
    }

    @Test
    void getNumberOfLines(){
        assertEquals(2, Util.getNumberOfLines(loadFile("./subset_concepts.txt")));
        assertEquals(-1, Util.getNumberOfLines(null));
    }

    @Test
    void getNumberOfNonBlancLines(){
        assertEquals(2, Util.getNumberOfNonBlancLines(loadFile("./subset_concepts.txt")));
        assertEquals(-1, Util.getNumberOfNonBlancLines(null));
    }

    @Test
    void readEntitiesFromFile() {
        Set<String> result = Util.readEntitiesFromFile(loadFile("entityFileForTest.txt"));
        assertTrue(result.contains("http://dbpedia.org/resource/Amp"));
        assertTrue(result.contains("http://dbpedia.org/resource/SQM"));
        assertTrue(result.contains("http://dbpedia.org/resource/Grupa_Lotos"));
        assertTrue(result.contains("http://dbpedia.org/resource/State_Bank_of_India"));
        assertFalse(result.contains("http://dbpedia.org/resource/War"));
    }

    @Test
    void getPathOfResource(){
        assertNotNull(Util.getPathOfResource("dummyGraph.nt"));
        assertNull(Util.getPathOfResource("_THIS_FILE_DOES_NOT_EXIST_.not_exist"));
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    public static File loadFile(String fileName){
        return Util.loadFile(fileName);
    }

    @Test
    void removeTags() {
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", Util.removeTags("<http://www.w3" +
                ".org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", Util.removeTags("http://www.w3" +
                ".org/ns/lemon/ontolex#LexicalEntry>"));
        assertEquals("http://www.w3.org/ns/lemon/ontolex#LexicalEntry", Util.removeTags("http://www.w3" +
                ".org/ns/lemon/ontolex#LexicalEntry"));
    }
}