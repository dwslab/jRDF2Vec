package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.NtMemoryWalkGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContinuationEntitySelectorTest {


    @Test
    void getEntities() {
        File walkDirectory = Util.loadFile("existing_walk_directory");
        assertTrue(walkDirectory != null && walkDirectory.exists());

        File ntFile = Util.loadFile("pizza.owl.nt");
        assertTrue(ntFile != null && ntFile.exists());

        NtMemoryWalkGenerator walkGenerator = new NtMemoryWalkGenerator(ntFile);
        ContinuationEntitySelector selector = new ContinuationEntitySelector(walkDirectory,
                new MemoryEntitySelector(walkGenerator.getData()));

        Set<String> entities = selector.getEntities();
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#CheeseyPizza"));
        assertFalse(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#America"));
        assertFalse(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatTopping"));
        assertFalse(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AsparagusTopping"));
    }

    @Test
    void getEntitiesFailure() {
        File ntFile = Util.loadFile("pizza.owl.nt");
        assertTrue(ntFile != null && ntFile.exists());

        NtMemoryWalkGenerator walkGenerator = new NtMemoryWalkGenerator(ntFile);
        ContinuationEntitySelector selector = new ContinuationEntitySelector(null,
                new MemoryEntitySelector(walkGenerator.getData()));

        Set<String> entities = selector.getEntities();
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#CheeseyPizza"));
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#America"));
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#MeatTopping"));
        assertTrue(entities.contains("http://www.co-ode.org/ontologies/pizza/pizza.owl#AsparagusTopping"));
    }
}