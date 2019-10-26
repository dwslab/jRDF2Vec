package walkGenerators.alod.applications.alodRandomWalks.generationInMemory.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for walk generators.
 */
public interface WalkGeneratorClassic {
    void writeWalksToFile(List<String> walks);
    String drawRandomConcept(String concept);
    String drawConcept(String concept);
    ArrayList<String> generateWalksForEntity(String concept, int numberOfWalks, int depth);
}
