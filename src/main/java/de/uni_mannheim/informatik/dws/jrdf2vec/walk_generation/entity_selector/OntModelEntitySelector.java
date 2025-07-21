package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.entity_selector;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;

import java.util.HashSet;


/**
 * Get all entities using a Jena OntModel.
 */
public class OntModelEntitySelector implements EntitySelector {

    /**
     * Constructor
     * @param model Model to be used
     */
    public OntModelEntitySelector(OntModel model){
        this.model = model;
    }

    /**
     * Jena Model to be used.
     */
    OntModel model;

    @Override
    /**
     * Obtain the entities in this case: Lexical Entry instances.
     * This method will create a cache.
     *
     * @return Set of entities as String.
     */
    public HashSet<String> getEntities() {
        
        HashSet<String> result = new HashSet<>(100000);

        // (1) Subjects
        String queryString = "SELECT distinct ?s WHERE { ?s ?p ?o . }";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            String conceptUri = queryResult.next().getResource("s").getURI();
            if(conceptUri == null) continue;
            result.add(conceptUri);
        }

        // (2) Objects
        queryString = "SELECT distinct ?o WHERE {?s ?p ?o . FILTER(!isLiteral(?o)) .}";
        query = QueryFactory.create(queryString);
        queryExecution = QueryExecutionFactory.create(query, model);
        queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            String conceptUri = queryResult.next().getResource("o").getURI();
            if(conceptUri == null) continue;
            result.add(conceptUri);
        }

        return result;
    }
}
