package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.runnables;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers.MemoryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runnable for walk entity generation.
 */
public class DatatypeWalkEntityProcessingRunnable implements Runnable {


    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatatypeWalkEntityProcessingRunnable.class);

    /**
     * Entity that is processed by this thread.
     */
    private String entity;

    /**
     * Length of each walk.
     */
    private int depth;

    /**
     * The walk generator for which this parser works.
     */
    private WalkGenerator walkGenerator;

    /**
     * Constructor.
     *
     * @param generator Generator to be used.
     * @param entity    The entity this particular thread shall handle.
     * @param depth     Desired length of the walk. Defines how many entity steps are allowed. Note that
     *                  this leads to more walk components than the specified depth.
     */
    public DatatypeWalkEntityProcessingRunnable(WalkGenerator generator, String entity, int depth) {
        this.entity = entity;
        this.depth = depth;
        this.walkGenerator = generator;
    }

    @Override
    public void run() {
        if (walkGenerator.parser instanceof MemoryParser) {
            // datatype walks are only implemented for memory options
            // yes, the depth and # of walks parameters are this way
            walkGenerator.writeToFile(((MemoryParser) walkGenerator.parser).generateTextWalksForEntity(walkGenerator.shortenUri(entity), this.depth));
        } else LOGGER.error("NOT YET IMPLEMENTED FOR THE CURRENT PARSER!");
    }
}
