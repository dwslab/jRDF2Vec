package walkGenerators.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable for mid walk generation.
 */
public class MidWalkEntityProcessingRunnable implements Runnable{

    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MidWalkEntityProcessingRunnable.class);

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
    public MidWalkEntityProcessingRunnable(WalkGenerator generator, String entity, int numberOfWalks, int walkLength) {
        this.entity = entity;
        this.numberOfWalks = numberOfWalks;
        this.walkLength = walkLength;
        this.walkGenerator = generator;
    }

    /**
     * Actual thread execution.
     */
    public void run() {
        if(walkGenerator.parser.getClass() == HdtParser.class) {
            walkGenerator.writeToFile(((HdtParser) walkGenerator.parser).generateMidWalksForEntity(walkGenerator.shortenUri(entity), this.numberOfWalks, this.walkLength));
        } else {
            LOGGER.error("NOT YET IMPLEMENTED FOR OTHER PARSERS THAN HDT_PARSER!");
        }
    }

}
