package walkGenerators.base;

import org.apache.jena.ontology.OntModel;

import java.util.HashSet;

/**
 * Select the entities for which walks shall be generated.
 */
public interface EntitySelector {
    HashSet<String> getEntities(OntModel model);
}
