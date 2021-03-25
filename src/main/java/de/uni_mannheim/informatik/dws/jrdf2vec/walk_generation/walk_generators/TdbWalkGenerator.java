package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TdbWalkGenerator implements IWalkGenerator, IMidWalkDuplicateFreeCapability {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TdbWalkGenerator.class);

    private Dataset tdbDataset;
    private Model tdbModel;

    public TdbWalkGenerator(String pathToTdbDataset){
         tdbDataset = TDBFactory.createDataset(pathToTdbDataset);
         tdbDataset.begin(ReadWrite.READ);
         tdbModel = tdbDataset.getDefaultModel();
    }

    @Override
    public List<String> generateMidWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth) {
        return null;
    }

    public String generateMidWalkForEntity(String entity, int depth){
        List<String> result = new LinkedList<>();

        String nextElementPredecessor = entity;
        String nextElementSuccessor = entity;

        // initialize result
        result.add(entity);

        // variable to store the number of iterations performed so far
        int currentDepth = 0;

        // randomly decide whether to use predecessors or successors
        int randomPickZeroOne = ThreadLocalRandom.current().nextInt(2);

        if (randomPickZeroOne == 0) {
            // predecessor

            // now we need to draw



        }

        return null;
    }

    public Set<Triple> getBackwardTriples(String object){
        Set<Triple> result = new HashSet<>();
        for (Statement statement : tdbModel.listStatements(null, null, tdbModel.createResource(object)).toSet()){
            String subjectUri;
            RDFNode subject = statement.getSubject();
            if (subject.isAnon()){
                subjectUri = subject.asResource().getId().getLabelString();
            } else {
                subjectUri = subject.asResource().getURI();
            }
            Triple t = new Triple(subjectUri, statement.getPredicate().getURI(), object);
            result.add(t);
        }
        return result;
    }

    public Set<Triple> getForwardTriples(String subject){
        Set<Triple> result = new HashSet<>();
        for (Statement statement :
                tdbModel.createResource(subject)
                        .listProperties()
                        .filterKeep(x -> x.getObject().isResource())
                        .toSet()){
            String objectUri;
            RDFNode object = statement.getObject();
            if (object.isAnon()){
                objectUri = object.asResource().getId().getLabelString();
            } else {
                objectUri = object.asResource().getURI();
            }
            Triple t = new Triple(subject, statement.getPredicate().getURI(), objectUri);
            result.add(t);
        }
        return result;
    }

    public void close(){
        tdbDataset.close();
    }
}
