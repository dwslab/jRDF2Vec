package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.deleteFile;
import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.loadFile;
import static org.junit.jupiter.api.Assertions.*;

class KvConverterTest {


    private static final String VECTORS_KV_FILE = "./freude_vectors.kv";
    private static final String VECTORS_W2V_FILE = "./freude_vectors.w2v";

    /**
     * This is also a detailed test of class {@link VectorTxtToW2v}.
     */
    @Test
    void convertTxt(){
        File txtFile = loadFile("freude_vectors.txt");
        assertNotNull(txtFile);
        File fileToWrite = new File(VECTORS_KV_FILE);
        fileToWrite.deleteOnExit();
        KvConverter.convert(txtFile, fileToWrite);
        File w2vFile = new File(VECTORS_W2V_FILE);
        w2vFile.deleteOnExit();
        assertTrue(w2vFile.exists());

        // checking vocabulary
        assertTrue(Gensim.getInstance().isInVocabulary("schöner", fileToWrite.getAbsoluteFile()));

        // checking dimension
        Double[] vector = Gensim.getInstance().getVector("schöner", fileToWrite.getAbsolutePath());

        // checking values
        assertEquals(3, vector.length);
        assertEquals(-0.0016543772, vector[0]);
        assertEquals(-0.0009240248, vector[1]);
        assertEquals(-0.0007398839, vector[2]);

        assertTrue(fileToWrite.exists());
    }

    @Test
    void convertW2v(){
        File txtFile = loadFile("freude_vectors_w2v.w2v");
        assertNotNull(txtFile);
        File fileToWrite = new File(VECTORS_KV_FILE);
        fileToWrite.deleteOnExit();
        KvConverter.convert(txtFile, fileToWrite);
        assertTrue(fileToWrite.exists());

        // checking vocabulary
        assertTrue(Gensim.getInstance().isInVocabulary("schöner", fileToWrite.getAbsoluteFile()));

        // checking dimension
        Double[] vector = Gensim.getInstance().getVector("schöner", fileToWrite.getAbsolutePath());

        // checking values
        assertEquals(3, vector.length);
        assertEquals(-0.0016543772, vector[0]);
        assertEquals(-0.0009240248, vector[1]);
        assertEquals(-0.0007398839, vector[2]);
    }

    @AfterAll
    static void cleanUp(){
        deleteFile(VECTORS_KV_FILE);
        deleteFile(VECTORS_W2V_FILE);
    }

}