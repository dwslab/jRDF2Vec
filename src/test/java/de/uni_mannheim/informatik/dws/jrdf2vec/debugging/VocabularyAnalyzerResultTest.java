package de.uni_mannheim.informatik.dws.jrdf2vec.debugging;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class VocabularyAnalyzerResultTest {


    @Test
    void getAllNotFound() {
        VocabularyAnalyzerResult result = new VocabularyAnalyzerResult();
        result.setSubjectsNotFound(new HashSet(Arrays.asList(new String[]{"a"})));
        result.setPredicatesNotFound(new HashSet(Arrays.asList(new String[]{"b", "c"})));
        result.setObjectsNotFound(new HashSet(Arrays.asList(new String[]{"d", "e", "f"})));
        assertTrue(result.getAllAdditional().size() == 0);
        assertTrue(result.getAllNotFound().size() == 6);

        String string = result.toString();
        assertNotNull(string);
        //System.out.println(string);
    }

    @Test
    void getAllAdditional() {
        VocabularyAnalyzerResult result = new VocabularyAnalyzerResult();
        result.setAllAdditional(new HashSet(Arrays.asList(new String[]{"a", "b", "c", "d", "e", "f"})));
        assertEquals(6, result.getAllAdditional().size());
        assertTrue(result.getAllNotFound().size() == 0);
    }
}