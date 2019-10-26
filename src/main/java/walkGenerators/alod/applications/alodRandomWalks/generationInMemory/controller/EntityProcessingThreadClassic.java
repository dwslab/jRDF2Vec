package walkGenerators.alod.applications.alodRandomWalks.generationInMemory.controller;

import java.util.ArrayList;

import walkGenerators.alod.applications.alodRandomWalks.generationInMemory.model.WalkGeneratorClassic;

/**
 * A thread which is able to build walks from the ALOD data set.
 */
class EntityProcessingThreadClassic implements Runnable{

    /**
     * Constructor
     * @param generator The generator to be used.
     * @param entity The concept for which walks shall be generated.
     * @param numberOfWalks The desired number of walks.
     * @param depth The desired depth of each walk.
     */
    public EntityProcessingThreadClassic(WalkGeneratorClassic generator, String entity, int numberOfWalks, int depth){
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
    private boolean isBiasedWalks = true;


    @Override
    public void run() {

        if(isBiasedWalks) {
            for (int currentNumberOfWalks = 0; currentNumberOfWalks < numberOfWalks; currentNumberOfWalks++) {
                String walk = entity;
                String nextEntity = null;
                depthLoop: for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
                    if (nextEntity == null) {
                        nextEntity = generator.drawRandomConcept(entity);
                    } else {
                        nextEntity = generator.drawRandomConcept(nextEntity);
                    }
                    if(nextEntity == null){
                        break depthLoop; // there is no hypernym → stop here
                    }
                    walk = walk + " " + nextEntity;
                } // for (depth)
                resultList.add(walk);
            } // for (number of walks)
        } else {
            for (int currentNumberOfWalks = 0; currentNumberOfWalks < numberOfWalks; currentNumberOfWalks++) {
                String walk = entity;
                String nextEntity = null;
                depthLoop: for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
                    if (nextEntity == null) {
                        nextEntity = generator.drawConcept(entity);
                    } else {
                        nextEntity = generator.drawConcept(nextEntity);
                    }
                    if(nextEntity == null){
                        break depthLoop; // there is no hypernym → stop here
                    }
                    walk = walk + " " + nextEntity;
                } // for (depth)
                resultList.add(walk);
            } // for (number of walks)
        }

        generator.writeWalksToFile(resultList);
    } // (run)


    public boolean isBiasedWalks() {
        return isBiasedWalks;
    }

    public void setBiasedWalks(boolean biasedWalks) {
        isBiasedWalks = biasedWalks;
    }
}
