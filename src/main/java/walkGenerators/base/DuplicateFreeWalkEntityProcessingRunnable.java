package walkGenerators.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single task for the thread pool.
 */
public class DuplicateFreeWalkEntityProcessingRunnable implements Runnable {

    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateFreeWalkEntityProcessingRunnable.class);

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
     * @param generator Generator to be used.
     * @param entity        The entity this particular thread shall handle.
     * @param numberOfWalks The number of walks to be performed per entity.
     * @param walkLength Desired length of the walk.
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
        if(walkGenerator.parser.getClass() == NtMemoryParser.class) {
            walkGenerator.writeToFile(((NtMemoryParser)walkGenerator.parser).generateDuplicateFreeRandomWalksForEntity(walkGenerator.shortenUri(entity), numberOfWalks, this.walkLength));
        } else {
            LOGGER.error("NOT YET IMPLEMENTED FOR OTHER PARSER THAN NT_PARSER!");
        }
    }

}
