package walkGenerators.light;

import org.apache.jena.ontology.OntModel;
import walkGenerators.base.EntitySelector;

import java.util.HashSet;

public class LightEntitySelector implements EntitySelector {

    /**
     * The entities for which walks will be generated.
     */
    HashSet<String> entitiesToProcess;

    public LightEntitySelector(HashSet<String> entitiesToProcess){
        this.entitiesToProcess = entitiesToProcess;
    }

    @Override
    public HashSet<String> getEntities(OntModel model) {
        return this.entitiesToProcess;
    }
}
