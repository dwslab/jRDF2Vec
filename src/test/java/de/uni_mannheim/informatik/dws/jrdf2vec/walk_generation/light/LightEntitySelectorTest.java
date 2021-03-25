package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.light;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LightEntitySelectorTest {

    @Test
    void readEntitiesFromFile() {
        Set<String> result = LightEntitySelector.readEntitiesFromFile(loadFile("entityFileForTest.txt"));
        assertTrue(result.contains("http://dbpedia.org/resource/Amp"));
        assertTrue(result.contains("http://dbpedia.org/resource/SQM"));
        assertTrue(result.contains("http://dbpedia.org/resource/Grupa_Lotos"));
        assertTrue(result.contains("http://dbpedia.org/resource/State_Bank_of_India"));
        assertFalse(result.contains("http://dbpedia.org/resource/War"));
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            File result =  FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }

}