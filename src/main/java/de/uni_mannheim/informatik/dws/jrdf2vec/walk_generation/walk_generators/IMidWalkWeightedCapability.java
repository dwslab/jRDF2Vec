package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import java.util.List;

public interface IMidWalkWeightedCapability extends IWalkGenerationCapability{


    /**
     * Weighted mid walk: If there are more options to go forward, it is more likely to go forward.
     * The walks are duplicate free.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param numberOfWalks Number of walks to be performed per entity.
     * @param depth         The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @return List of walks.
     */
    List<String> generateWeightedMidWalksForEntity(String entity,  int numberOfWalks, int depth);
}
