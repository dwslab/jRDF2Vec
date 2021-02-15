package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.runnables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers.MemoryParser;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGenerator;

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
    int depth;

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
     * @param entity The entity this particular thread shall handle.
     * @param numberOfWalks The number of walks to be performed per entity.
     * @param depth Desired length of the walk.
     */
    public DuplicateFreeWalkEntityProcessingRunnable(WalkGenerator generator, String entity, int numberOfWalks, int depth) {
        this.entity = entity;
        this.numberOfWalks = numberOfWalks;
        this.depth = depth;
        this.walkGenerator = generator;
    }

    /**
     * Actual thread execution.
     */
    public void run() {
        //if(walkGenerator.parser.getClass() == NtMemoryParser.class) {
        if(MemoryParser.class.isAssignableFrom(walkGenerator.parser.getClass())) {
            walkGenerator.writeToFile(((MemoryParser)walkGenerator.parser).generateDuplicateFreeRandomWalksForEntity(walkGenerator.shortenUri(entity), numberOfWalks, this.depth));
        } else {
            LOGGER.error("NOT YET IMPLEMENTED FOR OTHER PARSER THAN MEMORY PARSER!");
        }
    }

}
