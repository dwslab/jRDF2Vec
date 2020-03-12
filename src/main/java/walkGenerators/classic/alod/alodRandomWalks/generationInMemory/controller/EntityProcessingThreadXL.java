package walkGenerators.classic.alod.alodRandomWalks.generationInMemory.controller;

import java.util.ArrayList;

/**
 * A thread which is able to build walks from the ALOD data set.
 */
class EntityProcessingThreadXL implements Runnable{

    /**
     * Constructor
     * @param generator The generator to be used.
     * @param entity The concept for which walks shall be generated.
     * @param numberOfWalks The desired number of walks.
     * @param depth The desired depth of each walk.
     */
    public EntityProcessingThreadXL(WalkGeneratorXlWalks generator, String entity, int numberOfWalks, int depth){
        resultList = new ArrayList<>();
        this.numberOfWalks = numberOfWalks;
        this.depth = depth;
        this.entity = entity;
        this.generator = generator;
    }

    private ArrayList<String> resultList;
    WalkGeneratorXlWalks generator;
    private String entity;
    private int numberOfWalks;
    private int depth;


    @Override
    public void run() {

        for (int currentNumberOfWalks = 0; currentNumberOfWalks < numberOfWalks; currentNumberOfWalks++) {
            String walk = entity;
            String nextEntity = null;
            depthLoop: for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
                if (nextEntity == null) {
                    nextEntity = generator.drawBroaderConcept(entity);
                } else {
                    nextEntity = generator.drawBroaderConcept(nextEntity);
                }
                if(nextEntity == null){
                    break depthLoop; // there is no hypernym → stop here
                }
                walk = walk + " " + nextEntity;
            } // for (depth)
            resultList.add(walk);
        } // for (number of walks)

        generator.writeWalksToFile(resultList);
    } // (run)

}
