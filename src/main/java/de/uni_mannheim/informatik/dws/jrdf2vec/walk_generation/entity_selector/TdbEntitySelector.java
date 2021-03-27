package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.apache.jena.rdf.model.Model;

import java.util.Set;
import java.util.stream.Collectors;

public class TdbEntitySelector implements EntitySelector {


    private Model tdbModel;

    public TdbEntitySelector(Model tdbModel){
        this.tdbModel = tdbModel;
    }

    @Override
    public Set<String> getEntities() {
        return tdbModel.listSubjects()
                .filterKeep(x -> x.isURIResource())
                .toSet()
                .stream()
                .map(x -> x.getURI())
                .collect(Collectors.toSet());
    }
}
