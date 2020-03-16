import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import training.Word2VecConfiguration;


import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of the command line functionality.
 */
class MainTest {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

    @Test
    public void trainLight(){
        File lightWalks = new File("./mainLightWalks/");
        lightWalks.mkdir();
        String entityFilePath = this.getClass().getClassLoader().getResource("dummyEntities.txt").getPath();
        String graphFilePath = this.getClass().getClassLoader().getResource("dummyGraph.nt").getPath();
        String[] args = {"-graph", graphFilePath, "-light", entityFilePath, "-walkDir", "./mainLightWalks/"};
        Main.main(args);

        assertTrue(lightWalks.listFiles().length > 0);
        HashSet<String> files = Sets.newHashSet(lightWalks.list());

        // assert that all files are there
        assertTrue(files.contains("model.kv"));
        assertTrue(files.contains("model"));
        assertTrue(files.contains("model.txt"));
        assertTrue(files.contains("walk_file.gz"));

        try {
            FileUtils.deleteDirectory(lightWalks);
        } catch (IOException ioe){
            LOGGER.error("Failed to clean up after test.", ioe);
        }
    }

    @Test
    public void getHelp(){
        String result = Main.getHelp();
        assertNotNull(result);

        // print the help for manual inspection
        System.out.println(result);
    }

    @Test
    public void getDeltaTimeString(){
        try {

            // simple case

            Instant before = Instant.now();
            TimeUnit.SECONDS.sleep(3);
            Instant after = Instant.now();
            String result = Main.getDeltaTimeString(before, after);

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

            result = Main.getDeltaTimeString(before, after);
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

}