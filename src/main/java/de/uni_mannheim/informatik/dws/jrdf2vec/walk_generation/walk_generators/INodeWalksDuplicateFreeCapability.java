package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import java.util.List;

public interface INodeWalksDuplicateFreeCapability extends IWalkGenerationCapability {


    /**
     * Node walk: A duplicate free forward walk which does not involve edges.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param numberOfWalks Number of walks to be performed per entity.
     * @param depth         The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @return List of walks.
     */
    List<String> generateNodeWalksForEntity(String entity, int numberOfWalks, int depth);
}
