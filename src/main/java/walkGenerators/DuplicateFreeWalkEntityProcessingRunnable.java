package walkGenerators;

import java.util.ArrayList;
import java.util.List;

/**
 * A single task for the thread pool.
 */
public class DuplicateFreeWalkEntityProcessingRunnable implements Runnable {

    /**
     * Entity that is processed by this thread.
     */
    String entity;

    /**
     * Length of each walk.
     */
    int walkLength;

    /**
     * Number of walks to be performed per entity.
     */
    int numberOfWalks;

    /**
     * The walk generator for which this parser works.
     */
    WalkGenerator walkGenerator;

    /**
     * Constructor.
     *
     * @param entity        The entity this particular thread shall handle.
     * @param numberOfWalks The number of walks to be performed per entity.
     */
    public DuplicateFreeWalkEntityProcessingRunnable(WalkGenerator generator, String entity, int numberOfWalks, int walkLength) {
        this.entity = entity;
        this.numberOfWalks = numberOfWalks;
        this.walkLength = walkLength;
        this.walkGenerator = generator;
    }

    /**
     * Actual thread execution.
     */
    public void run() {
        walkGenerator.writeToFile(walkGenerator.parser.generateWalksForEntity(walkGenerator.shortenUri(entity), numberOfWalks, this.walkLength));
    }

}
