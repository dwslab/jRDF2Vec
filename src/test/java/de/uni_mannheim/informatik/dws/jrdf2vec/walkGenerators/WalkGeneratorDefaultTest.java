package de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators;

import org.junit.jupiter.api.Test;
import de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.base.WalkGeneratorDefault;

import java.io.*;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

class WalkGeneratorDefaultTest {

    @Test
    void generateRandomWalksDuplicateFreeXml() {
        File pizzaOntology = new File(getClass().getResource("/pizza.owl.xml").getFile());

        String generatedFilePath = "./test_walks.gz";
        WalkGeneratorDefault generator = new WalkGeneratorDefault(pizzaOntology);
        generator.generateRandomWalksDuplicateFree(8, 5, 5, generatedFilePath);

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
        File pizzaOntology = new File(getClass().getResource("/pizza.ttl").getFile());

        String generatedFilePath = "./test_walks2.gz";
        WalkGeneratorDefault generator = new WalkGeneratorDefault(pizzaOntology);
        generator.generateRandomWalksDuplicateFree(8, 5, 5, generatedFilePath);

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

}