package walkGenerators.classic.alod.alodRandomWalks.generationInMemory.controller;

import walkGenerators.classic.alod.alodRandomWalks.generationInMemory.model.WalkGeneratorClassic;

import java.util.ArrayList;

/**
 * A single task for the thread pool.
 */
public class DuplicateFreeWalkEntityProcessingRunnable implements Runnable {

    /**
     * Constructor
     * @param generator The generator to be used.
     * @param entity The concept for which walks shall be generated.
     * @param numberOfWalks The desired number of walks.
     * @param depth The desired depth of each walk.
     */
    public DuplicateFreeWalkEntityProcessingRunnable(WalkGeneratorClassic generator, String entity, int numberOfWalks, int depth){
        resultList = new ArrayList<>();
        this.numberOfWalks = numberOfWalks;
        this.depth = depth;
        this.entity = entity;
        this.generator = generator;
    }

    private ArrayList<String> resultList;
    WalkGeneratorClassic generator;
    private String entity;
    private int numberOfWalks;
    private int depth;


    @Override
    public void run() {
        resultList = generator.generateWalksForEntity(this.entity, this.numberOfWalks, this.depth);
        generator.writeWalksToFile(resultList);
    } // (run)

}
