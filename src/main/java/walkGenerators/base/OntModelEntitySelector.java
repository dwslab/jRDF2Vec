package walkGenerators.base;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;

import java.util.HashSet;

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
     * @return Entities as String.
     */
    public HashSet<String> getEntities() {
        HashSet<String> result = new HashSet<>(100000);

        // NOTE: it is sufficient to do a query only for subjects b/c walks for concepts that appear only as object
        // cannot be calculated.
        String queryString = "SELECT distinct ?s WHERE { ?s ?p ?o . }";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            String conceptUri = queryResult.next().getResource("s").getURI();

            if(conceptUri == null) continue;

            // no concepts will be added
            result.add(conceptUri);
        }
        return result;
    }
}
