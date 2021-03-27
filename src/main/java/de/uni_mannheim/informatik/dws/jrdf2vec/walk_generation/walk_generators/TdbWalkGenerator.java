package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.data_structures.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TdbWalkGenerator implements IWalkGenerator, IMidWalkCapability, IMidWalkDuplicateFreeCapability,
        IRandomWalkDuplicateFreeCapability, IMidWalkWeightedCapability, ICloseableWalkGenerator {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TdbWalkGenerator.class);

    private Dataset tdbDataset;
    private Model tdbModel;

    /**
     * Main Constructor
     *
     * @param pathToTdbDataset File path to the TDB dataset.
     */
    public TdbWalkGenerator(String pathToTdbDataset) {
        tdbDataset = TDBFactory.createDataset(pathToTdbDataset);
        //tdbDataset.begin(ReadWrite.READ);
        tdbModel = tdbDataset.getDefaultModel();
    }

    /**
     * Constructor
     * @param uriToTdbDataset URI to the TDB dataset. Must be a file URI.
     */
    public TdbWalkGenerator(URI uriToTdbDataset) {
        this(new File(uriToTdbDataset).getAbsolutePath());
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     *
     * @param numberOfWalks The number of walks to be generated.
     * @param entity        The entity for which a walk shall be generated.
     * @param depth         The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalksDuplicateFree(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     *
     * @param numberOfWalks The number of walks to be generated.
     * @param entity        The entity for which a walk shall be generated.
     * @param depth         The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntity(java.lang.String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalks(generateMidWalkForEntityAsArray(entity, depth, numberOfWalks));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param depth         The depth of each walk (where the depth is the number of hops).
     * @param numberOfWalks The number of walks to be performed.
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateMidWalkForEntityAsArray(String entity, int depth, int numberOfWalks) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < numberOfWalks; i++) {
            List<String> walk = generateMidWalkForEntity(entity, depth);
            if (walk.size() > 1) {
                result.add(walk);
            }
        }
        return result;
    }

    public List<String> generateMidWalkForEntity(String entity, int depth) {
        LinkedList<String> result = new LinkedList<>();

        String nextElementPredecessor = entity;
        String nextElementSuccessor = entity;

        // initialize result
        result.add(entity);

        // variable to store the number of iterations performed so far
        int currentDepth = 0;

        while (currentDepth < depth) {
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

    /**
     * Answers a (?, ?, o) query.
     * @param object The object in the query.
     * @return Result triples.
     */
    public Set<Triple> getBackwardTriples(String object) {
        Set<Triple> result = new HashSet<>();
        Set<Statement> tdbStatements =  tdbModel.listStatements(null, null, tdbModel.createResource(object)).toSet();

        for (Statement statement : tdbStatements) {
            String subjectUri;
            RDFNode subject = statement.getSubject();
            if (subject.isAnon()) {
                subjectUri = subject.asResource().getId().getLabelString();
            } else {
                subjectUri = subject.asResource().getURI();
            }
            Triple t = new Triple(subjectUri, statement.getPredicate().getURI(), object);
            result.add(t);
        }
        return result;
    }

    /**
     * Answers a (s, ?, ?) query.
     * @param subject The subject in the query.
     * @return Result triples.
     */
    public Set<Triple> getForwardTriples(String subject) {
        Set<Triple> result = new HashSet<>();
        Set<Statement> tdbStatements =  tdbModel.createResource(subject)
                .listProperties()
                .filterKeep(x -> x.getObject().isResource())
                .toSet();

        for (Statement statement : tdbStatements) {
            String objectUri;
            RDFNode object = statement.getObject();
            if (object.isAnon()) {
                objectUri = object.asResource().getId().getLabelString();
            } else {
                objectUri = object.asResource().getURI();
            }
            Triple t = new Triple(subject, statement.getPredicate().getURI(), objectUri);
            result.add(t);
        }
        return result;
    }

    public void close() {
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

    @Override
    public List<String> generateWeightedMidWalksForEntity(String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalksDuplicateFree(generateWeightedMidWalkForEntityAsArray(entity, numberOfWalks, depth));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param depth         The depth of each walk (where the depth is the number of hops).
     * @param numberOfWalks The number of walks to be performed.
     * @return A data structure describing the walks.
     */
    public List<List<String>> generateWeightedMidWalkForEntityAsArray(String entity, int numberOfWalks, int depth) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < numberOfWalks; i++) {
            List<String> walk = generateWeightedMidWalkForEntity(entity, depth);
            if (walk.size() > 1) {
                result.add(walk);
            }
        }
        return result;
    }

    /**
     * Generates a single walk for the given entity with the given depth.
     *
     * @param entity The entity for which a walk shall be generated.
     * @param depth  The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @return One walk as list where each element is a walk component.
     */
    public List<String> generateWeightedMidWalkForEntity(String entity, int depth) {
        LinkedList<String> result = new LinkedList<>();

        String nextElementPredecessor = entity;
        String nextElementSuccessor = entity;

        // initialize result
        result.add(entity);

        // variable to store the number of iterations performed so far
        int currentDepth = 0;

        while (currentDepth < depth) {
            currentDepth++;

            // randomly decide whether to use predecessors or successors
            double randomPickZeroOne = ThreadLocalRandom.current().nextDouble(0.0, 1.00000001);

            // predecessor candidates
            Set<Triple> candidatesPredecessor = getBackwardTriples(nextElementPredecessor);

            // successor candidates
            Set<Triple> candidatesSuccessor = getForwardTriples(nextElementSuccessor);

            double numberOfPredecessors = 0.0;
            double numberOfSuccessors = 0.0;

            if (candidatesPredecessor != null) numberOfPredecessors = candidatesPredecessor.size();
            if (candidatesSuccessor != null) numberOfSuccessors = candidatesSuccessor.size();

            // if there are no successors and predecessors: return current walk
            if (numberOfPredecessors == 0 && numberOfSuccessors == 0) return result;

            // determine cut-off point
            double cutOffPoint = numberOfPredecessors / (numberOfPredecessors + numberOfSuccessors);

            if (randomPickZeroOne <= cutOffPoint) {
                // predecessor
                if (candidatesPredecessor != null && candidatesPredecessor.size() > 0) {
                    Triple drawnTriple = Util.randomDrawFromSet(candidatesPredecessor);

                    // add walks from the front (walk started before entity)
                    result.addFirst(drawnTriple.predicate);
                    result.addFirst(drawnTriple.subject);
                    nextElementPredecessor = drawnTriple.subject;
                }
            } else {
                // successor
                if (candidatesSuccessor != null && candidatesSuccessor.size() > 0) {
                    Triple tripleToAdd = Util.randomDrawFromSet(candidatesSuccessor);

                    // add next walk iteration
                    result.addLast(tripleToAdd.predicate);
                    result.addLast(tripleToAdd.object);
                    nextElementSuccessor = tripleToAdd.object;
                }
            }
        }
        return result;
    }

    public Model getTdbModel() {
        return tdbModel;
    }
}
