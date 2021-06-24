package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.getPathOfResource;
import static org.junit.jupiter.api.Assertions.*;

class WalkMergerTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(WalkMergerTest.class);

    @Test
    void mergeWalks() {
        String walkDirectoryPath = getPathOfResource("walk_merge");
        assertNotNull(walkDirectoryPath);
        String fileToWritePath = "./mergedWalksTest.txt";
        File fileToWrite = new File(fileToWritePath);
        fileToWrite.deleteOnExit();
        WalkMerger.mergeWalks(walkDirectoryPath, fileToWritePath);

        // re-using the file reader here...
        Set<String> result = Util.readEntitiesFromFile(fileToWrite);
        assertTrue(result.size() > 4);
        assertTrue(result.contains("Ä Ö Ü"));
        assertTrue(result.contains("A B C"));
        assertTrue(result.contains("G H I"));
        assertTrue(result.contains("? = %"));

        assertTrue(fileToWrite.exists());
        assertTrue(fileToWrite.delete(), "Could not delete File.");
    }
}