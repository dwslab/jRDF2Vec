package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.deleteFile;
import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.getPathOfResource;
import static org.junit.jupiter.api.Assertions.*;

class WalkMergerTest {


    private static final String MERGE_PATH = "./mergedWalksTest.txt";

    @BeforeAll
    static void setUp(){
        deleteFile("./reduced_vocab.txt");
    }

    @Test
    void mergeWalks() {
        String walkDirectoryPath = getPathOfResource("walk_merge");
        assertNotNull(walkDirectoryPath);
        String fileToWritePath = MERGE_PATH;
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