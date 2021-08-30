package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.*;
import static org.junit.jupiter.api.Assertions.*;

class VectorTxtToW2vTest {


    private final static String TXT_VECTOR_FILE_PATH = "./txtVectorFile.w2v";

    @Test
    void vectorTxtFileToW2vFormat() {
        File vectorFile = loadFile("txtVectorFile.txt");
        assertNotNull(vectorFile);
        File fileToWrite = new File(TXT_VECTOR_FILE_PATH);
        fileToWrite.deleteOnExit();
        VectorTxtToW2v.convert(vectorFile, fileToWrite);
        assertTrue(fileToWrite.exists());
        assertEquals(getNumberOfLines(vectorFile) + 1, getNumberOfLines(fileToWrite));
        deleteFile(TXT_VECTOR_FILE_PATH);

        // testing some error cases
        assertFalse(fileToWrite.exists());
        VectorTxtToW2v.convert(null, fileToWrite);
        assertFalse(fileToWrite.exists());
    }

    @AfterAll
    static void cleanUp(){
        deleteFile(TXT_VECTOR_FILE_PATH);
    }
}