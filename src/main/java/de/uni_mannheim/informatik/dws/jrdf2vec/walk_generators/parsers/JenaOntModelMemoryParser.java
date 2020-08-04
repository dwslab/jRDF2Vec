package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base.WalkGenerator;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.data_structures.TripleDataSetMemory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class transforms a JenaOnt Model (or an RDF file) into the internal data structure of this framework
 * for the walk generation process.
 */
public class JenaOntModelMemoryParser extends MemoryParser {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JenaOntModelMemoryParser.class);

    public JenaOntModelMemoryParser(OntModel ontModel, WalkGenerator walkGenerator){
        readDataFromOntModel(ontModel);
        specificWalkGenerator = walkGenerator;
    }

    /**
     * Simple Constructor
     */
    public JenaOntModelMemoryParser(){
        // do nothing
    }

    /**
     * Read n-triples from the given file into {@link MemoryParser#data}.
     * @param fileToReadFrom File from which will be read (must be any RDF file such as NT, XML etc.).
     */
    public void readDataFromFile(File fileToReadFrom){
        if(!fileToReadFrom.exists()){
            LOGGER.error("The specified file does not exist. Aborting Parsing.");
            return;
        }
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
     * Read n-triples from the given model into {@link MemoryParser#data}.
     * @param model Reference to the {@link OntModel} that shall be parsed into the internal triple data structure.
     */
    public void readDataFromOntModel(OntModel model){
        if(data == null){
            data = new TripleDataSetMemory();
        }
        for(StmtIterator iterator = model.listStatements(); iterator.hasNext();){
            Statement statement = iterator.nextStatement();

            // skip datatype properties
            if(statement.getObject().isLiteral()){
                continue;
            }

            // handling of the subject
            Resource subjectResource = statement.getSubject();
            String subject = null;
            if(subjectResource.isAnon()){
                subject = subjectResource.getId().toString();
            } else {
                subject = statement.getSubject().getURI();
            }

            String predicate = statement.getPredicate().getURI();

            String object = null;
            RDFNode objectResource = statement.getObject();
            if(objectResource.isAnon()){
                object = objectResource.asResource().getId().toString();
            } else {
                object = statement.getObject().asResource().getURI();
            }

            data.add(new Triple(subject, predicate, object));
        }
    }

}
