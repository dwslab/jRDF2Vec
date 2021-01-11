package de.uni_mannheim.informatik.dws.jrdf2vec.debugging;

import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VocabularyAnalyzerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyAnalyzerTest.class);

    @Test
    void detectMissingEntities(){
        Set<String> result = VocabularyAnalyzer.detectMissingEntities(getPathOfResource("freude_vectors.txt"), getPathOfResource("freude_vectors_incomplete_concepts.txt"));
        assertTrue(result.size() == 1);
        assertTrue(result.contains("not_in_the_embedding"));
    }

    @Test
    void detectAdditionalEntities(){
        Set<String> result = VocabularyAnalyzer.detectAdditionalEntities(getPathOfResource("freude_vectors.txt"), getPathOfResource("freude_vectors_incomplete_concepts.txt"));
        assertTrue(result.size() > 1);
        assertTrue(result.contains("Wir"));
        assertTrue(result.contains("dein"));
    }

    @Test
    void readTextVectorFile() {
        Triplet<Set<String>, Integer, Boolean> result = VocabularyAnalyzer.readTextVectorFile(new File(getPathOfResource("freude_vectors.txt")));
        Set<String> concepts = result.getValue0();
        assertTrue(concepts.size() == 12);
        assertTrue(concepts.contains("betreten"));
        assertTrue(result.getValue1() == 3);
        assertTrue(result.getValue2());
    }

    /**
     * Helper method to obtain the canonical path of a (test) resource.
     * @param resourceName File/directory name.
     * @return Canonical path of resource.
     */
    public String getPathOfResource(String resourceName){
        try {
            URL res = getClass().getClassLoader().getResource(resourceName);
            if(res == null) throw new IOException();
            File file = Paths.get(res.toURI()).toFile();
            return file.getCanonicalPath();
        } catch (URISyntaxException | IOException ex) {
            LOGGER.info("Cannot create path of resource", ex);
            return null;
        }
    }
}