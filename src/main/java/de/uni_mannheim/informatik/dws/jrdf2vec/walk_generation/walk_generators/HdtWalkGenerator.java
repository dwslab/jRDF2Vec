package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators;

import de.uni_mannheim.informatik.dws.jrdf2vec.util.Util;

import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A parser for HDT files.
 */
public class HdtWalkGenerator implements IWalkGenerator, IMidWalkCapability, IMidWalkDuplicateFreeCapability, IRandomWalkCapability,IRandomWalkDuplicateFreeCapability,
IMidWalkWeightedCapability {

    /**
     * Default logger.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HdtWalkGenerator.class);
    
    /**
     * The data set to be used by the parser.
     */
    HDT hdtDataSet;

    /**
     * Last ID that is shared
     */
    long nShared;

    /**
     * Last ID that is a subject
     */
    long nSubjects;

    /**
     * Last ID that is an object
     */
    long nObjects;

    /**
     * Constructor
     *
     * @param hdtFilePath Path to the HDT file.
     * @exception IOException IOException
     */
    public HdtWalkGenerator(String hdtFilePath) throws IOException {
        try {
            // We load hdt file
            hdtDataSet = HDTManager.mapIndexedHDT(hdtFilePath);
            nShared = hdtDataSet.getDictionary().getNshared();
            nSubjects= hdtDataSet.getDictionary().getNsubjects();
            nObjects = hdtDataSet.getDictionary().getNobjects();
        } catch (IOException e) {
            LOGGER.error("Failed to load HDT file: " + hdtFilePath + "\nProgram will fail.", e);
            throw e;
        }
    }

    public HDT getHdt(){
        return hdtDataSet;
    }
    /**
     * Constructor
     *
     * @param hdtFile HDT file to be used.
     * @exception IOException IOException
     */
    public HdtWalkGenerator(File hdtFile) throws IOException {
        this(hdtFile.getAbsolutePath());
    }

    /**
     * Generates walks that are ready to be processed further (already concatenated, space-separated).
     * @param numberOfWalks The number of walks to be generated.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of each walk.
     * @return List where every item is a walk separated by spaces.
     */
    @Override
    public List<String> generateMidWalksForEntityDuplicateFree(String entity, int numberOfWalks, int depth){
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

    /**
     * Generates a single walk for the given entity with the given depth.
     * @param entity The entity for which a walk shall be generated.
     * @param depth The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @return One walk as list where each element is a walk component.
     */
    public List<String> generateMidWalkForEntity(String entity, int depth) {
        LinkedList<String> result = new LinkedList<>();
        
        long nextElementPredecessor;
        long nextElementSuccessor;
        nextElementPredecessor = hdtDataSet.getDictionary().stringToId(entity, TripleComponentRole.OBJECT);
        nextElementSuccessor = hdtDataSet.getDictionary().stringToId(entity, TripleComponentRole.SUBJECT);
        
        boolean validNextSuccessor = true;
        boolean validNextPredecessor = true;
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
                // Can use as next predecessor
                if(validNextPredecessor){
                    if(isShared(nextElementPredecessor) || isObjectOnly(nextElementPredecessor)){ //if is a valid object you can search
                        try {
                            IteratorTripleID iterator = hdtDataSet.getTriples().search(new TripleID(0,0,nextElementPredecessor));
                            
                            List<TripleID> candidates = new ArrayList<>();
        
                            TripleID ts;
        
                            while (iterator.hasNext()) {
                                ts = iterator.next();
                                TripleID clonedTriple = new TripleID(ts.getSubject(), ts.getPredicate(), ts.getObject());
                                candidates.add(clonedTriple);
                            }
                        
        
                            if (!candidates.isEmpty()) {
                                TripleID drawnTriple = randomDrawFromList(candidates);
        
                                // add walks from the front (walk started before entity)
                                
                                long subjectId = drawnTriple.getSubject();
                                long predicateId = drawnTriple.getPredicate();
                                
                                result.addFirst(hdtDataSet.getDictionary().idToString(predicateId, TripleComponentRole.PREDICATE).toString());
                                result.addFirst(hdtDataSet.getDictionary().idToString(subjectId, TripleComponentRole.SUBJECT).toString());
                                nextElementPredecessor = subjectId;
                                validNextPredecessor = !isSubjectOnly(nextElementPredecessor); // if next predecessor is subjectOnly we couldnt use as object
                            }
                            
                        } catch (Exception e) {
                            LOGGER.error("Search exception while trying to find a predecessor." + nextElementPredecessor);
                        }
                    }else{
                        validNextPredecessor = false; // if not a valid object dont try again
                    }
                }
                
            } else {
                // successor
                // Can use as next successor
                if(validNextSuccessor){
                    if(isShared(nextElementSuccessor) || isSubjectOnly(nextElementSuccessor)){//if is a valid subject you can search
                        try {
                            IteratorTripleID iterator = hdtDataSet.getTriples().search(new TripleID(nextElementSuccessor,0,0));
                            List<TripleID> candidates = new ArrayList<>();
                            
                            while (iterator.hasNext()) {
                                TripleID ts = iterator.next();
        
                                //If we do not create a new instance of TripleID candidates, it will be filled with the last candidate that has been found.
                                TripleID clonedTriple = new TripleID(ts.getSubject(), ts.getPredicate(), ts.getObject());
                                candidates.add(clonedTriple);
                            }
                            
                            if (!candidates.isEmpty()) {

                                TripleID stringToAdd = randomDrawFromList(candidates);

                                long objectId = stringToAdd.getObject();
                                long predicateId = stringToAdd.getPredicate();

                                // add next walk iteration
                                result.addLast(hdtDataSet.getDictionary().idToString(predicateId, TripleComponentRole.PREDICATE).toString());
                                result.addLast(hdtDataSet.getDictionary().idToString(objectId, TripleComponentRole.OBJECT).toString());
                                
                                nextElementSuccessor = objectId;
                                validNextSuccessor = !isObjectOnly(nextElementSuccessor); //if next succesor is objectOnly we cant use as subject
                            }
                        } catch (Exception e) {
                            LOGGER.error("Search exception while trying to find a successor." , e);
                        }
                    }else{
                        validNextSuccessor = false;  // if not a valid subject dont try again
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Writes the given hdt data set as nt file.
     * @param dataSet Set to read.
     * @param fileToWrite File to write
     */
    public static void serializeDataSetAsNtFile(HDT dataSet, File fileToWrite) {
        try {
            IteratorTripleString iterator = dataSet.search("", "", "");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite), StandardCharsets.UTF_8));
            while (iterator.hasNext()) {
                TripleString ts = iterator.next();
                if(ts.getObject().toString().startsWith("\"")){
                writer.write("<" + ts.getSubject().toString() + "> <" + ts.getPredicate().toString() + "> " + ts.getObject().toString() + " .\n");
                } else {
                    writer.write("<" + ts.getSubject().toString() + "> <" + ts.getPredicate().toString() + "> <" + ts.getObject().toString() + "> .\n");
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException | NotFoundException e) {
            LOGGER.error("Could not write file.", e);
        }
    }

    /**
     * Weighted mid walk: If there are more options to go forward, it is more likely to go forward.
     * The walks are duplicate free.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param depth         The depth of the walk. Depth is defined as hop to the next node. A walk of depth 1 will have three walk components.
     * @param numberOfWalks Number of walks to be performed per entity.
     * @return List of walks.
     */
    @Override
    public List<String> generateWeightedMidWalksForEntity(String entity, int numberOfWalks, int depth) {
        return Util.convertToStringWalksDuplicateFree(generateWeightedMidWalkForEntityAsArray(entity, numberOfWalks,
                depth));
    }

    /**
     * Walks of length 1, i.e., walks that contain only one node, are ignored.
     *
     * @param entity        The entity for which walks shall be generated.
     * @param numberOfWalks The number of walks to be performed.
     * @param depth         The depth of each walk (where the depth is the number of hops).
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

        long nextElementPredecessor;
        long nextElementSuccessor;
        nextElementPredecessor = hdtDataSet.getDictionary().stringToId(entity, TripleComponentRole.OBJECT);
        nextElementSuccessor = hdtDataSet.getDictionary().stringToId(entity, TripleComponentRole.SUBJECT);
        
        boolean validNextPredecessor = true;
        boolean validNextSuccessor = true;

        // initialize result
        result.add(entity);

        // variable to store the number of iterations performed so far
        int currentDepth = 0;
      
        while (currentDepth < depth) {
            currentDepth++;

            // randomly decide whether to use predecessors or successors
            double randomPickZeroOne = ThreadLocalRandom.current().nextDouble(0.0, 1.00000001);

            // predecessor candidates
            List<TripleID> candidatesPredecessor = new ArrayList<>();
            if(validNextPredecessor){
                if(isShared(nextElementPredecessor) || isObjectOnly(nextElementPredecessor)){
                    IteratorTripleID  iterator = hdtDataSet.getTriples().search(new TripleID(0,0,nextElementPredecessor));
                    while (iterator.hasNext()){
                        TripleID ts = iterator.next();
                        //If we do not create a new instance of TripleID candidates, it will be filled with the last candidate that has been found.
                        TripleID clonedTriple = new TripleID(ts.getSubject(), ts.getPredicate(), ts.getObject());
                        candidatesPredecessor.add(clonedTriple);
                    }
                }else{
                    validNextPredecessor = false; // if not a valid object dont try again
                    candidatesPredecessor = null;
                }    
            }
            
            // successor candidates
            List<TripleID> candidatesSuccessor = new ArrayList<>();
            if(validNextSuccessor){
                if(isShared(nextElementSuccessor) || isSubjectOnly(nextElementSuccessor)){
                    IteratorTripleID  iterator = hdtDataSet.getTriples().search(new TripleID(nextElementSuccessor,0,0));
                    while (iterator.hasNext()){
                        TripleID ts = iterator.next();
                        TripleID clonedTriple = new TripleID(ts.getSubject(), ts.getPredicate(), ts.getObject());
                        candidatesSuccessor.add(clonedTriple);
                    }

                }else{
                    validNextSuccessor=false;
                    candidatesSuccessor = null;
                }
            }
            
            



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
                    TripleID drawnTriple = randomDrawFromList(candidatesPredecessor);

                    long subjectID = drawnTriple.getSubject();
                    long predicateID = drawnTriple.getPredicate();
                    String predicate = hdtDataSet.getDictionary().idToString(predicateID, TripleComponentRole.PREDICATE).toString();
                    String subject = hdtDataSet.getDictionary().idToString(subjectID, TripleComponentRole.SUBJECT).toString();
                    // add walks from the front (walk started before entity)
                    result.addFirst(predicate);
                    result.addFirst(subject);
                    nextElementPredecessor = subjectID;
                    validNextPredecessor = !isSubjectOnly(nextElementPredecessor);
                }
            } else {
                // successor
                if (candidatesSuccessor != null && candidatesSuccessor.size() > 0) {
                    TripleID drawnTriple = randomDrawFromList(candidatesSuccessor);

                    long objectID = drawnTriple.getObject();
                    long predicateID = drawnTriple.getPredicate();
                    String predicate = hdtDataSet.getDictionary().idToString(predicateID, TripleComponentRole.PREDICATE).toString();
                    String object = hdtDataSet.getDictionary().idToString(objectID, TripleComponentRole.OBJECT).toString();
                    // add next walk iteration
                    result.addLast(predicate);
                    result.addLast(object);
                    validNextSuccessor = isShared(objectID);
                    nextElementSuccessor = objectID;
                    validNextSuccessor = !isObjectOnly(nextElementSuccessor);
                }
            }
        }
        return result;
    }


    public TripleID getRandomTripleForSubjectHDT(long subjectID,boolean isValidNextSubject) {
        if(isValidNextSubject){
            if(isSubjectOnly(subjectID) || isShared(subjectID)){
                // Buscamos triples donde subjectId es el sujeto
                IteratorTripleID iterator = hdtDataSet.getTriples().search(new TripleID(subjectID, 0, 0));
                
                // Si no hay triples, retornamos null
                List<TripleID> triples = new ArrayList<>();
                TripleID aux;
                while (iterator.hasNext()) {
                    aux = iterator.next();
                    TripleID triple = new TripleID(aux.getSubject(), aux.getPredicate(), aux.getObject());
                    triples.add(triple);
                }
                // Seleccionamos un triple aleatorio de la lista de triples
                int randomIndex = ThreadLocalRandom.current().nextInt(triples.size());
                TripleID triple = triples.get(randomIndex);
                
                return triple;
            }else{
                return null;
            }                 
        }else{
            return null;
        }


        
    }

    /** 
     * Devuelve `true` si el ID pertenece tanto a sujetos como a objetos (es compartido).
     */
    private boolean isShared(long id){
        return id >= 1 && id <= nShared;
    }
    /** 
     * Devuelve `true` si el ID es un sujeto exclusivo (nunca aparece como objeto).
     */
    private boolean isSubjectOnly(long id) {
        return id > nShared && id <= nSubjects;
    }

    /** 
     * Devuelve `true` si el ID es un objeto exclusivo (nunca aparece como sujeto).
     */
    private boolean isObjectOnly(long id) {
        return id > nShared && id <= nObjects;
    }
    
    
    @Override
    public List<String> generateRandomWalksForEntity(String entity, int numberOfWalks, int depth){
        List<String> result = new ArrayList<>();

        int currentDepth;
        String currentWalk;
        int currentWalkNumber = 0;
        long subject = hdtDataSet.getDictionary().stringToId(entity, TripleComponentRole.SUBJECT);
        boolean isValidNextSubject = true;

        nextWalk:
        while (currentWalkNumber < numberOfWalks) {
            currentWalkNumber++;
            long lastObject = subject;
            currentWalk = entity;
            currentDepth = 0;
            TripleID po;
            while (currentDepth < depth) {
                currentDepth++;
                po = getRandomTripleForSubjectHDT(lastObject,isValidNextSubject); 
                if(po != null){
                    long objectAux = po.getObject();
                    long predicateAux = po.getPredicate();
                    String object = hdtDataSet.getDictionary().idToString(objectAux, TripleComponentRole.OBJECT).toString();
                    String predicate = hdtDataSet.getDictionary().idToString(predicateAux, TripleComponentRole.PREDICATE).toString();

                    currentWalk += " " + predicate + " " + object;
                    lastObject = objectAux;
                    isValidNextSubject = !isObjectOnly(lastObject);
                } else {
                    // The current walk cannot be continued -> add to list (if there is a walk of depth 1) and create next walk.
                    if(currentWalk.length() != entity.length()) result.add(currentWalk);
                    isValidNextSubject = true;
                    continue nextWalk;
                }
            }
            result.add(currentWalk);
        }
        return result;
    }


    public List<TripleID> getObjectTriplesInvolvingSubjectHDT(long subject,boolean isValidNextSubject) {
        List<TripleID> result = new ArrayList<>();
        if(isValidNextSubject){
            if(isSubjectOnly(subject) || isShared(subject)){  
                // Buscamos todas las tripletas donde subjectID es el sujeto
                IteratorTripleID iterator = hdtDataSet.getTriples().search(new TripleID(subject, 0, 0));
                
                TripleID aux;
                // Iteramos sobre los resultados y los almacenamos en la lista
                while (iterator.hasNext()) {
                    aux = iterator.next();
                    TripleID tripleAux = new TripleID(aux.getSubject(), aux.getPredicate(), aux.getObject());
                    result.add(tripleAux);
                }            
            
                return result; // Retorna la lista con las tripletas encontradas        
            }
            else{
                return null;
            }
        }else{
            return null;
        }
    }
    
    public List<String> generateDuplicateFreeRandomWalksForEntity(String entity, int numberOfWalks, int depth) {
        List<List<TripleID>> walks = new ArrayList<>();
        boolean isFirstIteration = true;

        long subjectIni = hdtDataSet.getDictionary().stringToId(entity, TripleComponentRole.SUBJECT);
        boolean isValidNextSubject = true;
        try {
            for (int currentDepth = 0; currentDepth < depth; currentDepth++) {
                // Inicialización con la entidad de inicio
                if (isFirstIteration) {
                    List<TripleID> neighbours = getObjectTriplesInvolvingSubjectHDT(subjectIni,isValidNextSubject);
                    if (neighbours.isEmpty()) {
                        return new ArrayList<>();
                    }
                    for (TripleID neighbour : neighbours) {
                        ArrayList<TripleID> individualWalk = new ArrayList<>();
                        individualWalk.add(neighbour);
                        walks.add(individualWalk);
                    }
                    isFirstIteration = false;
                } else {
                    // Creamos una copia de la lista de caminos actual
                    List<List<TripleID>> walks_tmp = new ArrayList<>(walks);
    
                    // Recorremos cada camino actual
                    for (List<TripleID> walk : walks_tmp) {
                        // Última entidad del camino
                        TripleID lastTriple = walk.get(walk.size() - 1);
                        long lastObjectId = lastTriple.getObject();
                        isValidNextSubject = !isObjectOnly(lastObjectId);
                        // Obtenemos los triples donde el objeto anterior es sujeto
                        List<TripleID> nextIteration = getObjectTriplesInvolvingSubjectHDT(lastObjectId,isValidNextSubject);
    
                        if (!nextIteration.isEmpty()) {
                            walks.remove(walk); // Eliminamos el camino actual para expandirlo
                            for (TripleID nextStep : nextIteration) {
                                List<TripleID> newWalk = new ArrayList<>(walk);
                                newWalk.add(nextStep);
                                walks.add(newWalk);
                            }
                        }
                    } // Fin del bucle de caminos
                }
    
                // Reducimos la cantidad de caminos si supera el número de walks deseado
                while (walks.size() > numberOfWalks) {
                    int randomNumber = ThreadLocalRandom.current().nextInt(walks.size());
                    walks.remove(randomNumber);
                }
            } // Fin del bucle de profundidad
    
            // Convertimos los caminos en una lista de strings
            return convertToStringWalksHDT(walks, entity);
    
        } catch (Exception e) {
            LOGGER.error("Error generando walks sin duplicados para la entidad: " + entity, e);
            return new ArrayList<>();
        }
    }
    
    private List<String> convertToStringWalksHDT(List<List<TripleID>> walks, String entity) {
        List<String> result = new ArrayList<>();
        
        for (List<TripleID> walk : walks) {
            StringBuilder sb = new StringBuilder(entity);
            for (TripleID triple : walk) {
                String predicate = hdtDataSet.getDictionary().idToString(triple.getPredicate(), TripleComponentRole.PREDICATE).toString();
                String object = hdtDataSet.getDictionary().idToString(triple.getObject(), TripleComponentRole.OBJECT).toString();
                sb.append(" ").append(predicate).append(" ").append(object);
            }
            result.add(sb.toString());
        }
    
        return result;
    }
    /**
     * Draw a random value from a List. This method is thread-safe.
     *
     * @param listToDrawFrom The list from which shall be drawn.
     * @param <T>            Type
     * @return Drawn value of type T.
     */
    public static <T> T randomDrawFromList(List<T> listToDrawFrom) {
        int randomNumber = ThreadLocalRandom.current().nextInt(listToDrawFrom.size());
        return listToDrawFrom.get(randomNumber);
    }
}
