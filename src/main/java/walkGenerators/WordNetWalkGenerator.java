package walkGenerators;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashSet;

/**
 * Generates walks for WordNet RDF graph.
 */
public class WordNetWalkGenerator extends WalkGenerator {


    public static void main(String[] args) throws Exception {
        WordNetWalkGenerator generator = new WordNetWalkGenerator("C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\Experiments\\WordNet_RDF\\wordnet.nt");
        generator.getEntities(generator.model);
        //generator.generateWalks(8, 100, 8);
    }


    /**
     * Constructor
     *
     * @param pathToTripleFile Path to RDF Wordnet file in n-triples format.
     */
    public WordNetWalkGenerator(String pathToTripleFile) {
        try {
            this.model = readOntology(pathToTripleFile, "NT");
            this.parser = new NtParser(pathToTripleFile, this);
            LOGGER.info("Model read into memory.");
        } catch (MalformedURLException mue) {
            LOGGER.error("Path seems to be invalid. Generator not functional.", mue);
        }
    }


    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalks(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/wordnet_walks.gz");
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateWalksForEntities(getEntities(this.model), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        this.filePath = filePathOfFileToBeWritten;
        generateDuplicateFreeWalksForEntities(getEntities(this.model), numberOfThreads, numberOfWalksPerEntity, depth);
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalksPerEntity, depth, "./walks/wordnet_walks.gz");
    }


    /**
     * Central OntModel
     */
    private OntModel model;

    /**
     * Default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WordNetWalkGenerator.class);


    /**
     * Obtain the entities in this case: Lexical Entry instances.
     * This method will create a cache.
     *
     * @return Entities as String.
     */
    private HashSet<String> getEntities(OntModel model) {
        File file = new File("./wordnet_entities.txt");
        if (file.exists()) {
            LOGGER.info("Cached file found. Obtaining entities from cache.");
            return readHashSetFromFile(file);
        }
        file.getParentFile().mkdirs();
        HashSet<String> result = new HashSet<>();
        String queryString = "SELECT distinct ?concept WHERE { ?concept <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/lemon/ontolex#LexicalEntry> . }";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            String conceptUri = queryResult.next().getResource("concept").getURI();
            result.add(shortenUri(conceptUri));
        }
        writeHashSetToFile("./cache/wordnet_entities.txt", result);
        return result;
    }

    @Override
    public String shortenUri(String uri){
        String result = uri
                .replace("http://www.w3.org/ns/lemon/ontolex#", "ontolex:")
                .replace("http://wordnet-rdf.princeton.edu/rdf/lemma/", "wn-lemma:")
                .replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:")
                .replace(" http://wordnet-rdf.princeton.edu/ontology#", "wn-ontology:");
        return result;
    }

    public OntModel getModel() {
        return model;
    }
}
