package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static de.uni_mannheim.informatik.dws.jrdf2vec.util.Util.loadFile;
import static de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode.RANDOM_WALKS_DUPLICATE_FREE;
import static org.junit.jupiter.api.Assertions.*;

class WalkGeneratorDefaultTest {


    @Test
    void generateRandomWalksDuplicateFreeXml() {
        File pizzaOntology = loadFile("pizza.owl.xml");

        File walkDirectory = new File("./test_walks");
        WalkGenerationManager generator = new WalkGenerationManager(pizzaOntology);
        generator.generateWalks(RANDOM_WALKS_DUPLICATE_FREE, 8, 5, 5,
                walkDirectory);
        generator.close();

        File generatedFile = walkDirectory.listFiles()[0];
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

        File generatedFilePath = new File("./test_walks2");
        WalkGenerationManager generator = new WalkGenerationManager(pizzaOntology);
        generator.generateWalks(RANDOM_WALKS_DUPLICATE_FREE, 8, 5, 5, generatedFilePath);
        generator.close();

        File generatedFile = generatedFilePath.listFiles()[0];
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
        File walkDirectory = new File("./test_walks3");
        walkDirectory.deleteOnExit();
        walkDirectory.deleteOnExit();
        WalkGenerationManager generator = new WalkGenerationManager(pizzaOntology, true, true);
        generator.generateTextWalks(8,  5, walkDirectory);
        generator.close();

        File generatedFile = walkDirectory.listFiles()[0];
        generatedFile.deleteOnExit();
        assertTrue(generatedFile.exists(), "Assert that a walk file has been generated.");
        List<String> result = Util.readLinesFromGzippedFile(generatedFile);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AmericanHot http://www.w3.org/2004/02/skos/core#altLabel american hot"));

        // cleaning up
        generatedFile.delete();
    }

    @Test
    void uriAxioms(){
        URI myURI = new File("./myFile.txt").toURI();
        /*
        System.out.println("Path: " + myURI.getPath());
        System.out.println("Raw Path: " + myURI.getRawPath());
        System.out.println("Scheme Specific Part: " + myURI.getSchemeSpecificPart());
        System.out.println("Raw Scheme Specific Part: " + myURI.getRawSchemeSpecificPart());
        System.out.println("User Info: " + myURI.getUserInfo());
        System.out.println("Authority: " + myURI.getAuthority());
        System.out.println("Scheme: " + myURI.getScheme());
        */
        assertEquals("file", myURI.getScheme());
    }
}