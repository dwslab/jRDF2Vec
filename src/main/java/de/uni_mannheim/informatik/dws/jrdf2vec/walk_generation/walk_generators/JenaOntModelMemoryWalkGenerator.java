package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.TripleDataSetMemory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class transforms a JenaOnt Model (or an RDF file) into the internal data structure of this framework
 * for the walk generation process.
 */
public class JenaOntModelMemoryWalkGenerator extends MemoryWalkGenerator {


    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JenaOntModelMemoryWalkGenerator.class);

    /**
     * Simple Constructor
     */
    public JenaOntModelMemoryWalkGenerator(){
        // do nothing
    }

    /**
     * Constructor
     * @param ontModel The ont model from which data shall be read.
     */
    public JenaOntModelMemoryWalkGenerator(OntModel ontModel){
        this(ontModel, false);
    }

    /**
     * Constructor
     * @param ontModel The ont model from which data shall be read.
     * @param isParseDatatypeProperties True if datatype properties shall also be parsed.
     */
    public JenaOntModelMemoryWalkGenerator(OntModel ontModel, boolean isParseDatatypeProperties){
        this.setParseDatatypeProperties(isParseDatatypeProperties);
        readDataFromOntModel(ontModel);
    }

    /**
     * Read n-triples from the given file into {@link MemoryWalkGenerator#data}.
     * @param fileToReadFrom File from which will be read (RDF/XML).
     */
    public void readDataFromFile(String fileToReadFrom){
        readDataFromFile(new File(fileToReadFrom));
    }

    /**
     * Read n-triples from the given file into {@link MemoryWalkGenerator#data}.
     * @param fileToReadFrom File from which will be read (must be any RDF file such as NT, XML etc.).
     * @param format Predefined values are those in {@link Lang} such as:
     *               <ul>
     *                  <li>"RDF/XML"</li>
     *                  <li>"RDF/XML-ABBREV"</li>
     *                  <li>"N-TRIPLE"</li>
     *                  <li>"TURTLE"</li>
     *               </ul>
     *               The default value, represented by null, is "RDF/XML".
     */
    public void readDataFromFile(String fileToReadFrom, String format){
        readDataFromFile(new File(fileToReadFrom), format);
    }

    /**
     * Read n-triples from the given file into {@link MemoryWalkGenerator#data}.
     * @param fileToReadFrom File from which will be read (RDF/XML).
     */
    public void readDataFromFile(File fileToReadFrom){
        readDataFromFile(fileToReadFrom, null);
    }

    /**
     * Read n-triples from the given file into {@link MemoryWalkGenerator#data}.
     * @param fileToReadFrom File from which will be read (must be any RDF file such as NT, XML etc.).
     * @param format Predefined values are those in {@link Lang} such as:
     *               <ul>
     *                  <li>"RDF/XML"</li>
     *                  <li>"RDF/XML-ABBREV"</li>
     *                  <li>"N-TRIPLE"</li>
     *                  <li>"TURTLE"</li>
     *               </ul>
     *               The default value, represented by null, is "RDF/XML".
     */
    public void readDataFromFile(File fileToReadFrom, String format){
        if(!fileToReadFrom.exists()){
            LOGGER.error("The specified file does not exist. Aborting Parsing.");
            return;
        }
        try {
            URL url = fileToReadFrom.toURI().toURL();
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            model.read(url.toString(), null, format);
            readDataFromOntModel(model);
        } catch (MalformedURLException mue){
            LOGGER.error("Could not read from the specified file. A malformed URL exception occurred.", mue);
        }
    }

    /**
     * Read n-triples from the given model into {@link MemoryWalkGenerator#data}.
     * @param model Reference to the {@link OntModel} that shall be parsed into the internal triple data structure.
     */
    public void readDataFromOntModel(OntModel model){
        if(data == null){
            data = new TripleDataSetMemory();
        }
        for(StmtIterator iterator = model.listStatements(); iterator.hasNext();){
            Statement statement = iterator.nextStatement();

            // parse datatype properties
            if(isParseDatatypeProperties() && statement.getObject().isLiteral()) {
                // handling of the subject
                Resource subjectResource = statement.getSubject();
                String subject = null;
                if(subjectResource.isAnon()){
                    subject = subjectResource.getId().toString();
                } else {
                    subject = statement.getSubject().getURI();
                }

                // handling of the predicate
                String predicate = statement.getPredicate().getURI();

                // handling of the (string) object
                String object = textProcessingFunction.apply(statement.getObject().asLiteral().getLexicalForm());

                data.addDatatypeTriple(subject, predicate, object);
                continue;
            } else if(statement.getObject().isLiteral()) continue;

            // handling of the subject
            Resource subjectResource = statement.getSubject();
            String subject = null;
            if(subjectResource.isAnon()){
                subject = subjectResource.getId().toString();
            } else {
                subject = statement.getSubject().getURI();
            }

            // handling of the predicate
            String predicate = statement.getPredicate().getURI();

            //  handling of the object
            String object;
            RDFNode objectResource = statement.getObject();
            if(objectResource.isAnon()){
                object = objectResource.asResource().getId().toString();
            } else {
                object = statement.getObject().asResource().getURI();
            }

            data.addObjectTriple(new Triple(subject, predicate, object));
        }
    }
}
