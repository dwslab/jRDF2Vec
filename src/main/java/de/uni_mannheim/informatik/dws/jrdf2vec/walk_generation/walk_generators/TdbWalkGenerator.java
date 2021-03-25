package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TdbWalkGenerator implements IWalkGenerator, IMidWalkCapability, IMidWalkDuplicateFreeCapability,
        IRandomWalkDuplicateFreeCapability {


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

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     * @param numberOfWalks The number of walks to be generated.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalksDuplicateFree(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     * @param numberOfWalks The number of walks to be generated.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntity(java.lang.String entity, int numberOfWalks, int depth){
        return Util.convertToStringWalks(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     * @param entity The entity for which walks shall be generated.
     * @param depth The depth of each walk (where the depth is the number of hops).
     * @param numberOfWalks The number of walks to be performed.
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateMidWalkForEntityAsArray(String entity, int depth, int numberOfWalks){
        List<List<String>> result = new ArrayList<>();
        for(int i = 0; i < numberOfWalks; i++){
            List<String> walk = generateMidWalkForEntity(entity, depth);
            if(walk.size() > 1) {
                result.add(walk);
            }
        }
        return result;
    }

    public List<String> generateMidWalkForEntity(String entity, int depth){
        LinkedList<String> result = new LinkedList<>();

        String nextElementPredecessor = entity;
        String nextElementSuccessor = entity;

        // initialize result
        result.add(entity);

        // variable to store the number of iterations performed so far
        int currentDepth = 0;

        while(currentDepth < depth) {
            currentDepth++;

            // randomly decide whether to use predecessors or successors
            int randomPickZeroOne = ThreadLocalRandom.current().nextInt(2);

            if (randomPickZeroOne == 0) {
                // predecessor
                Set<Triple> triples = getBackwardTriples(nextElementPredecessor);
                if (triples.size() > 0) {
                    // now we need to draw randomly...
                    Triple triple = Util.randomDrawFromSet(triples);
                    result.addFirst(triple.predicate);
                    result.addFirst(triple.subject);
                    nextElementPredecessor = triple.subject;
                }
            } else {
                // successor
                Set<Triple> triples = getForwardTriples(nextElementSuccessor);
                if (triples.size() > 0) {
                    Triple triple = Util.randomDrawFromSet(triples);

                    result.addLast(triple.predicate);
                    result.addLast(triple.object);
                    nextElementSuccessor = triple.object;
                }
            }
        }
        return result;
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

    @Override
    public List<String> generateDuplicateFreeRandomWalksForEntity(String entity, int numberOfWalks, int depth) {
        List<String> result = new ArrayList<>();
        List<List<Triple>> walks = new ArrayList();
        boolean isFirstIteration = true;
        for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
            // initialize with first node
            if (isFirstIteration) {
                Set<Triple> neighbours = getForwardTriples(entity);
                if (neighbours == null || neighbours.size() == 0) {
                    return result;
                }
                for (Triple neighbour : neighbours) {
                    ArrayList<Triple> individualWalk = new ArrayList<>();
                    individualWalk.add(neighbour);
                    walks.add(individualWalk);
                }
                isFirstIteration = false;
            } else {
                // create a copy
                List<List<Triple>> walks_tmp = new ArrayList<>();
                walks_tmp.addAll(walks);

                // loop over current walks
                for (List<Triple> walk : walks_tmp) {
                    // get last entity
                    Triple lastTriple = walk.get(walk.size() - 1);
                    Set<Triple> nextIteration = getForwardTriples(lastTriple.object);
                    if (nextIteration != null) {
                        walks.remove(walk); // check whether this works
                        for (Triple nextStep : nextIteration) {
                            List<Triple> newWalk = new ArrayList<>(walk);
                            newWalk.add(nextStep);
                            walks.add(newWalk);
                        }
                    }
                } // loop over walks
            }
            // trim the list
            while (walks.size() > numberOfWalks) {
                int randomNumber = ThreadLocalRandom.current().nextInt(walks.size());
                walks.remove(randomNumber);
            }
        } // depth loop

        return Util.convertToStringWalks(walks, entity, false);

    }
}
