package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class transforms a JenaOnt Model (or any RDF file) into the internal data structure of this framework
 * for the walk generation process.
 */
public class JenaOntModelMemoryParser extends MemoryParser {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JenaOntModelMemoryParser.class);

    /**
     * Read n-triples from the given file.
     * @param fileToReadFrom File from which will be read (must be any RDF file such as NT, XML etc.).
     */
    public void readDataFromFile(File fileToReadFrom){
        try {
            URL url = fileToReadFrom.toURI().toURL();
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            model.read(url.toString());
            readDataFromOntModel(model);
        } catch (MalformedURLException mue){
            LOGGER.error("Could not read from the specified file. A malformed URL exception occurred.", mue);
        }
    }

    /**
     * Read n-triples from the given file.
     * @param model Reference to the {@link OntModel} that shall be parsed into the internal triple data structure.
     */
    public void readDataFromOntModel(OntModel model){
        for(StmtIterator iterator = model.listStatements(); iterator.hasNext();){
            Statement statement = iterator.nextStatement();
            if(statement.getObject().isLiteral()){
                continue;
            }
            String subject = statement.getSubject().getURI();
            String predicate = statement.getPredicate().getURI();
            String object = statement.getObject().asResource().getURI();
            data.add(new Triple(subject, predicate, object));
        }
    }

}
