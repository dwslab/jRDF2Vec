package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TdbEntitySelectorTest {


    @Test
    void getEntities() {
        Dataset tdbDataset = TDBFactory.createDataset(loadFile("pizza_tdb").getAbsolutePath());
        //tdbDataset.begin(ReadWrite.READ);
        Model tdbModel = tdbDataset.getDefaultModel();
        TdbEntitySelector selector = new TdbEntitySelector(tdbModel);
        Set<String> result = selector.getEntities();
        assertTrue(result.size() > 0);
        assertTrue(result.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#Siciliana"));
        assertTrue(result.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#NamedPizza"));
        //tdbDataset.end();
        tdbDataset.close();
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    public static File loadFile(String fileName){
        try {
            File result =  FileUtils.toFile(TdbEntitySelectorTest.class.getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }
}