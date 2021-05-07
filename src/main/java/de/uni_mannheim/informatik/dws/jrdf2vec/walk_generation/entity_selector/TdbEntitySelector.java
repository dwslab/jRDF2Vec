package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TdbEntitySelector implements EntitySelector {


    private Model tdbModel;

    public TdbEntitySelector(Model tdbModel){
        this.tdbModel = tdbModel;
    }

    @Override
    public Set<String> getEntities() {
        Set<String> result = new HashSet<>();
        result.addAll(
                tdbModel.listSubjects()
                .filterKeep(x -> x.isURIResource())
                .toSet()
                .stream()
                .map(x -> x.getURI())
                .collect(Collectors.toSet())
        );
        result.addAll(
                tdbModel.listObjects()
                        .filterKeep(x -> x.isURIResource())
                        .toSet()
                        .stream()
                        .map(x -> ((Resource) x).getURI())
                        .collect(Collectors.toSet())
        );
        return result;
    }
}
