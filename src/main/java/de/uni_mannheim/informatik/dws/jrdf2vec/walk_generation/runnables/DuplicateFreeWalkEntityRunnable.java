package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.runnables;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationManager;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.IRandomWalkDuplicateFreeCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single task for the thread pool.
 */
public class DuplicateFreeWalkEntityRunnable implements Runnable {


    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateFreeWalkEntityRunnable.class);

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
     * The walk generation manager.
     */
    WalkGenerationManager walkGenerationManager;

    /**
     * Constructor.
     *
     * @param generator Walk generation manager.
     * @param entity The entity this particular thread shall handle.
     * @param numberOfWalks The number of walks to be performed per entity.
     * @param depth Desired length of the walk.
     */
    public DuplicateFreeWalkEntityRunnable(WalkGenerationManager generator, String entity, int numberOfWalks, int depth) {
        this.entity = entity;
        this.numberOfWalks = numberOfWalks;
        this.depth = depth;
        this.walkGenerationManager = generator;
    }

    /**
     * Actual thread execution.
     */
    public void run() {
        if(walkGenerationManager.getWalkGenerator()  instanceof IRandomWalkDuplicateFreeCapability){
            walkGenerationManager
                    .writeToFile(
                            ((IRandomWalkDuplicateFreeCapability) walkGenerationManager.getWalkGenerator())
                                    .generateDuplicateFreeRandomWalksForEntity(walkGenerationManager.shortenUri(entity), numberOfWalks, this.depth));
        } else {
            LOGGER.error("NOT YET IMPLEMENTED FOR THIS WALK GENERATOR (" + walkGenerationManager.getWalkGenerator().getClass() + ")!" +
                    " Make sure" +
                    " it implements IRandomWalkDuplicateFreeCapability.");
        }
    }
}
