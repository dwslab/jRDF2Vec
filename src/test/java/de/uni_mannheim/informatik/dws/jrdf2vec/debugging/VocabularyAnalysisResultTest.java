package de.uni_mannheim.informatik.dws.jrdf2vec.debugging;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class VocabularyAnalysisResultTest {

    @Test
    void getAllNotFound() {
        VocabularyAnalysisResult result = new VocabularyAnalysisResult();
        result.setSubjectsNotFound(new HashSet(Arrays.asList(new String[]{"a"})));
        result.setPredicatesNotFound(new HashSet(Arrays.asList(new String[]{"b", "c"})));
        result.setObjectsNotFound(new HashSet(Arrays.asList(new String[]{"d", "e", "f"})));
        assertTrue(result.getAllAdditional().size() == 0);
        assertTrue(result.getAllNotFound().size() == 6);
    }

    @Test
    void getAllAdditional() {
        VocabularyAnalysisResult result = new VocabularyAnalysisResult();
        result.setAdditionalSubjects(new HashSet(Arrays.asList(new String[]{"a"})));
        result.setAdditionalPredicates(new HashSet(Arrays.asList(new String[]{"b", "c"})));
        result.setAdditionalObjects(new HashSet(Arrays.asList(new String[]{"d", "e", "f"})));
        assertTrue(result.getAllAdditional().size() == 6);
        assertTrue(result.getAllNotFound().size() == 0);
    }
}