package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.NtMemoryWalkGenerator;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContinuationEntitySelectorTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(ContinuationEntitySelectorTest.class);

    @BeforeAll
    static void setup(){
        deleteDirectory("./new_walk_directory_continuation");
    }

    @AfterAll
    static void tearDown(){
        deleteDirectory("./new_walk_directory_continuation");
    }

    private static void deleteDirectory(String directoryPath){
        try {
            FileUtils.deleteDirectory(new File(directoryPath));
        } catch (IOException e) {
            LOGGER.info("Cleanup failed (directory '" + directoryPath + "'.", e);
        }
    }

    @Test
    void getEntities() {
        File existingWalkDirectory = Util.loadFile("existing_walk_directory");
        assertTrue(existingWalkDirectory != null && existingWalkDirectory.exists());

        File newWalkDirectory = new File("./new_walk_directory_continuation");
        newWalkDirectory.deleteOnExit();
        assertTrue(newWalkDirectory.mkdir());

        File ntFile = Util.loadFile("pizza.owl.nt");
        assertTrue(ntFile != null && ntFile.exists());

        NtMemoryWalkGenerator walkGenerator = new NtMemoryWalkGenerator(ntFile);
        ContinuationEntitySelector selector = new ContinuationEntitySelector(existingWalkDirectory, newWalkDirectory,
                new MemoryEntitySelector(walkGenerator.getData()));

        Set<String> entities = selector.getEntities();
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#CheeseyPizza"));
        assertFalse(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#America"));
        assertFalse(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatTopping"));
        assertFalse(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AsparagusTopping"));

        assertEquals(1, newWalkDirectory.listFiles().length);
    }

    @Test
    void getEntitiesFailure() {
        File ntFile = Util.loadFile("pizza.owl.nt");
        assertTrue(ntFile != null && ntFile.exists());

        NtMemoryWalkGenerator walkGenerator = new NtMemoryWalkGenerator(ntFile);
        ContinuationEntitySelector selector = new ContinuationEntitySelector(null, null,
                new MemoryEntitySelector(walkGenerator.getData()));

        Set<String> entities = selector.getEntities();
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#CheeseyPizza"));
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#America"));
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatTopping"));
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AsparagusTopping"));
    }

    @Test
    void changeFilePathForCopy(){
        assertEquals("walk_1_copied.txt.gz", ContinuationEntitySelector.changeFilePathForCopy("walk_1.gz" ));
        assertEquals("walk_1_copied.txt.gz", ContinuationEntitySelector.changeFilePathForCopy("walk_1.txt.gz" ));
    }
}