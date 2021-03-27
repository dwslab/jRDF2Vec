package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector.HdtEntitySelector;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HdtEntitySelectorTest {

    @Test
    void getEntities() {
        String hdtPath = loadFile("swdf-2012-11-28.hdt").getAbsolutePath();
        assertNotNull(hdtPath, "Cannot find test resource.");
        try {
            HdtEntitySelector selector = new HdtEntitySelector(hdtPath);
            Set<String> result = selector.getEntities();
            assertNotNull(result, "The result should not be null.");
            assertTrue(result.size() > 10, "The result does not contain enough data.");
            assertTrue(result.contains("http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member"), "Selector did not find entity 'http://data.semanticweb.org/workshop/semwiki/2010/programme-committee-member'.");
        } catch (Exception e) {
            fail("Exception occurred while loading test HDT data set.");
        }
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