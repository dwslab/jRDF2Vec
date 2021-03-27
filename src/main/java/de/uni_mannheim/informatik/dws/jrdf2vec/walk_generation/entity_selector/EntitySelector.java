package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import java.util.Set;

/**
 * An EntitySelector determines the entities for which walks shall be generated.
 */
public interface EntitySelector {


    /**
     * Obtain all entities for which walks shall be generated.
     * @return The entities to be returned.
     */
    Set<String> getEntities();
}
