package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.*;
import static org.junit.jupiter.api.Assertions.*;

class VectorTxtToW2vTest {

    @Test
    void vectorTxtFileToW2vFormat() {
        File vectorFile = loadFile("txtVectorFile.txt");
        File fileToWrite = new File("./txtVectorFile.w2v");
        fileToWrite.deleteOnExit();
        VectorTxtToW2v.vectorTxtFileToW2vFormat(vectorFile, fileToWrite);
        assertTrue(fileToWrite.exists());
        assertEquals(getNumberOfLines(vectorFile) + 1, getNumberOfLines(fileToWrite));
    }

    @AfterAll
    static void cleanUp(){
        deleteFile("./txtVectorFile.w2v");
    }
}