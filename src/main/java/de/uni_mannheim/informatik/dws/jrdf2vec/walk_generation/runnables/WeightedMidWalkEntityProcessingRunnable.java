package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.runnables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.NtMemoryWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.NxMemoryWalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;

public class WeightedMidWalkEntityProcessingRunnable implements Runnable{


    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WeightedMidWalkEntityProcessingRunnable.class);

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
    WalkGenerationManager walkGenerator;

    /**
     * Constructor.
     *
     * @param generator     Generator to be used.
     * @param entity        The entity this particular thread shall handle.
     * @param numberOfWalks The number of walks to be performed per entity.
     * @param depth    Desired length of the walk. Defines how many entity steps are allowed. Note that
     *                      this leads to more walk components than the specified depth.
     */
    public WeightedMidWalkEntityProcessingRunnable(WalkGenerationManager generator, String entity, int numberOfWalks, int depth) {
        this.entity = entity;
        this.numberOfWalks = numberOfWalks;
        this.depth = depth;
        this.walkGenerator = generator;
    }

    /**
     * Actual thread execution.
     */
    public void run() {
        if (walkGenerator.walkGenerator.getClass() == NtMemoryWalkGenerator.class) {
            // yes, the depth and # of walks parameters are this way
            walkGenerator.writeToFile(((NtMemoryWalkGenerator) walkGenerator.walkGenerator).generateWeightedMidWalksForEntity(walkGenerator.shortenUri(entity),this.depth, this.numberOfWalks));
        } else if (walkGenerator.walkGenerator.getClass() == NxMemoryWalkGenerator.class) {
            walkGenerator.writeToFile(((NxMemoryWalkGenerator) walkGenerator.walkGenerator).generateWeightedMidWalksForEntity(walkGenerator.shortenUri(entity), this.depth, this.numberOfWalks));
        } else LOGGER.error("NOT YET IMPLEMENTED FOR THE CURRENT PARSER!");
    }
}
