package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.*;
import static org.junit.jupiter.api.Assertions.*;

class VectorFileReducerTest {


    @Test
    void writeReducedTextVectorFile(){
        String fileToWritePath = "./reduced_vocab.txt";
        String vectorTxtFilePath = getPathOfResource("txtVectorFile.txt");
        String entityFilePath = getPathOfResource("txtVectorFileEntities.txt");

        // try error cases first
        VectorFileReducer.writeReducedTextVectorFile(null, null, null);
        VectorFileReducer.writeReducedTextVectorFile(null, fileToWritePath, entityFilePath);
        VectorFileReducer.writeReducedTextVectorFile(vectorTxtFilePath, null, entityFilePath);
        VectorFileReducer.writeReducedTextVectorFile(vectorTxtFilePath, fileToWritePath, null);

        File fileToWrite = new File(fileToWritePath);
        assertFalse(fileToWrite.exists());

        // now try the real thing:
        VectorFileReducer.writeReducedTextVectorFile(vectorTxtFilePath, fileToWritePath, entityFilePath);
        assertTrue(fileToWrite.exists());
        assertTrue(getNumberOfLines(fileToWrite) <= 3);

        deleteFile(fileToWrite);
    }

}