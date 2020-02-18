package walkGenerators.light;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class LightEntitySelectorTest {

    @Test
    void readEntitiesFromFile() {
        HashSet<String> result = LightEntitySelector.readEntitiesFromFile(new File(getClass().getResource("/entityFileForTest.txt").getFile()));
        assertTrue(result.contains("http://dbpedia.org/resource/Amp"));
        assertTrue(result.contains("http://dbpedia.org/resource/SQM"));
        assertTrue(result.contains("http://dbpedia.org/resource/Grupa_Lotos"));
        assertTrue(result.contains("http://dbpedia.org/resource/State_Bank_of_India"));
        assertFalse(result.contains("http://dbpedia.org/resource/War"));
    }

}