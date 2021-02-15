package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGeneratorDefault;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

class WalkGeneratorDefaultTest {


    @Test
    void generateRandomWalksDuplicateFreeXml() {
        File pizzaOntology = loadFile("pizza.owl.xml");

        String generatedFilePath = "./test_walks.gz";
        WalkGeneratorDefault generator = new WalkGeneratorDefault(pizzaOntology);
        generator.generateRandomWalksDuplicateFree(8, 5, 5, generatedFilePath);
        generator.close();

        File generatedFile = new File(generatedFilePath);
        assertTrue(generatedFile.exists(), "Assert that a walk file has been generated.");

        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(new FileInputStream(generatedFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
        String readLine;
        HashSet<String> subjectsOfWalks = new HashSet<>();
        try {
            while ((readLine = reader.readLine()) != null) {
                subjectsOfWalks.add(readLine.split(" ")[0]);
            }
        } catch (IOException e){
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(subjectsOfWalks.size() > 0, "Assert that walks have been generated for more than one entity.");
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot"));
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourCheesesTopping"));
        generatedFile.delete();
    }

    @Test
    void generateRandomWalksDuplicateFreeTtl() {
        File pizzaOntology = loadFile("pizza.ttl");

        String generatedFilePath = "./test_walks2.gz";
        WalkGeneratorDefault generator = new WalkGeneratorDefault(pizzaOntology);
        generator.generateRandomWalksDuplicateFree(8, 5, 5, generatedFilePath);
        generator.close();

        File generatedFile = new File(generatedFilePath);
        assertTrue(generatedFile.exists(), "Assert that a walk file has been generated.");

        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(new FileInputStream(generatedFile));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Input stream to verify file could not be established.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
        String readLine;
        HashSet<String> subjectsOfWalks = new HashSet<>();
        try {
            while ((readLine = reader.readLine()) != null) {
                subjectsOfWalks.add(readLine.split(" ")[0]);
            }
        } catch (IOException e){
            e.printStackTrace();
            fail("Could not read gzipped file.");
        }

        assertTrue(subjectsOfWalks.size() > 0, "Assert that walks have been generated for more than one entity.");
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot"));
        assertTrue(subjectsOfWalks.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#FourCheesesTopping"));
        generatedFile.delete();
    }

    @Test
    void generateTextWalks(){
        File pizzaOntology = loadFile("pizza.ttl");

        String generatedFilePath = "./test_walks3.gz";
        WalkGeneratorDefault generator = new WalkGeneratorDefault(pizzaOntology, true);
        generator.generateTextWalks(8,  5, generatedFilePath);
        generator.close();

        File generatedFile = new File(generatedFilePath);
        generatedFile.deleteOnExit();
        assertTrue(generatedFile.exists(), "Assert that a walk file has been generated.");
        List<String> result = Util.readLinesFromGzippedFile(generatedFile);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot http://www.w3.org/2004/02/skos/core#altLabel american hot"));

        // cleaning up
        generatedFile.delete();
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