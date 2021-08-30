package de.uni_mannheim.informatik.dws.jrdf2vec.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.deleteFile;
import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.loadFile;
import static org.junit.jupiter.api.Assertions.*;

class VectorTxtToTfProjectorTsvTest {


    private static final String METADTA_TSV_FILE = "./freude_metadata.tsv";
    private static final String VECTOR_TSV_FILE = "./freude_vectors.tsv";

    @BeforeAll
    static void prepare() {
        // just to be sure
        cleanUp();
    }

    @Test
    void convert() {
        File vectorsTxtFile = loadFile("freude_vectors.txt");
        File metadataFile = new File(METADTA_TSV_FILE);
        metadataFile.deleteOnExit();
        File vectorFile = new File(VECTOR_TSV_FILE);
        vectorFile.deleteOnExit();
        VectorTxtToTfProjectorTsv.convert(vectorsTxtFile, vectorFile, metadataFile);
        assertTrue(metadataFile.exists());
        assertTrue(vectorFile.exists());

        // check vector file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vectorFile),
                StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            String[] tokens = line.split("\t");
            assertEquals(3, tokens.length);
            assertEquals("0.00177156", tokens[0]);
            assertEquals("-0.0019879746", tokens[1]);
            assertEquals("-0.001207912", tokens[2]);

        } catch (Exception e) {
            fail(e);
        }

        // check labels file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(metadataFile),
                StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            //assertEquals("Labels", line);
            //line = reader.readLine();
            assertEquals("Freude,", line);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void convertFail() {
        try {
            VectorTxtToTfProjectorTsv.convert(null, null, null);
        } catch (Exception e) {
            fail(e);
        }
        File vectorsTxtFile = new File("my_random_file_that_does_not_exist.txt");
        assertFalse(vectorsTxtFile.exists());
        File metadataFile = new File(METADTA_TSV_FILE);
        File vectorFile = new File(VECTOR_TSV_FILE);
        try {
            VectorTxtToTfProjectorTsv.convert(vectorsTxtFile, vectorFile, metadataFile);
        } catch (Exception e) {
            fail(e);
        }
        assertFalse(metadataFile.exists());
        assertFalse(vectorFile.exists());
    }

    @AfterAll
    static void cleanUp() {
        deleteFile(METADTA_TSV_FILE);
        deleteFile(VECTOR_TSV_FILE);
    }
}