package walkGenerators.rdf2vecLight;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TestCaseTest {

    @Test
    void getDBpediaUris() {

        for(TestCase testCase : TestCase.values()){
            HashSet<String> uris = testCase.getDBpediaUris();
            assertTrue(uris.size() > 0);
        }

    }

    @Test
    void getWikidataUris() {
        for(TestCase testCase : TestCase.values()){
            HashSet<String> uris = testCase.getWikidataUris();
            assertTrue(uris.size() > 0);
        }
    }
}