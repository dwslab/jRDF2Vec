package walkGenerators.classic.alod.services.tools;

import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import walkGenerators.classic.alod.services.tools.persistence.PersistenceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Class for various SPARQL services.
 * Singleton.
 * Buffers results and is able to write buffers to disk.
 */
public class SPARQLservice {

    private static Logger LOG = LoggerFactory.getLogger(SPARQLservice.class);

    private static SPARQLservice instance; // singleton pattern
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String ALOD_CLASSIC_ENDPOINT = "http://webisa.webdatacommons.org/sparql";
    public static final String ALOD_XL_ENDPOINT = "http://webisxl.webdatacommons.org/sparql";
    private static final String CONFIDENCE_TAG_CLASSIC = "<http://webisa.webdatacommons.org/ontology#hasConfidence>";
    private static final String CONFIDENCE_TAG_XL = "<http://webisa.webdatacommons.org/ontology/hasConfidence>";
    public static final int MAX_LIMIT_SPARQL = 10000; // the maximal integer allowed as LIMIT in LIMIT queries

    // buffer infrasructure
    private boolean useBuffer = true;
    private HashMap<String, String> sparqlServiceBufferSingleResult;
    private HashMap<String, HashSet<String>> sparqlServiceBufferResultSet;
    private HashMap<String, ArrayList<ConceptConfidenceTuple>> sparqlServiceConfidenceBufferClassic;
    private HashMap<String, ArrayList<ConceptConfidenceTuple>> sparqlServiceConfidenceBufferXL;
    private HashMap<String, Integer> sparqlServiceNumberBuffer;
    private HashMap<String, Boolean> sparqlServiceAskBuffer;
    private int sparqlServiceNumberBufferInitialCount = 0; // required for efficient persistence
    private int sparqlServiceBufferSingleResultInitialCount = 0; // required for efficient persistence
    private int sparqlServiceBufferResultSetInitialCount = 0; // required for efficient persistence
    private int sparqlServiceConfidenceBufferClassicInitialCount = 0; // required for efficient persistence
    private int sparqlServiceConfidenceBufferXLInitialCount = 0; // required for efficient persistence
    private int sparqlServiceAskBufferInitialCount = 0; // required for efficient persistence

    // buffer infrastructure - autosave
    private boolean autosave = true; // whether autosave shall be use
    private int autosaveAfter = 10000; // autosave the buffers after this amout of queries
    private int queriesSinceLastPersist = 0; // variable for autosave function


    /**
     * Singleton. Getter.
     * @return The SPARQL service instance.
     */
    public static SPARQLservice getSPARQLservice(){
        if (instance == null){
            instance = new SPARQLservice();
            return instance;
        } else {
            return instance;
        }
    }


    /**
     * Constructor. Buffers are initated lazily.
     */
    private SPARQLservice(){
        if(!useBuffer) {
            this.sparqlServiceBufferSingleResult = new HashMap<>();
            this.sparqlServiceBufferResultSet = new HashMap<>();
            this.sparqlServiceConfidenceBufferClassic = new HashMap<>();
            sparqlServiceConfidenceBufferXL = new HashMap<>();
            sparqlServiceNumberBuffer = new HashMap<>();
        }
    }


    /**
     * Returns the label of a resource in a given language.
     * If there are multiple results, only the first one will be returned.
     * @param sparqlEndpoint The endpoint to be queried.
     * @param uri The URI of the resource whose label shall be returned.
     * @param language The language of the label.
     * @return The label without the language annotation, i.e. "label" rather than "label@en".
     */
    public String getLabelInSpecifiedLanguage(String sparqlEndpoint, String uri, String language){
        uri = StringOperations.convertToTag(uri);
        String key = uri + "_getLang_" + language; // key for buffer

        // lazy buffer init if not initialized yet
        lazyInitBufferSingleResult();

        if(sparqlServiceBufferSingleResult.containsKey(key)){
            return sparqlServiceBufferSingleResult.get(key);
        }

        String queryStringWithLanguage =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "SELECT ?label WHERE {\n" +
                        uri + " rdfs:label ?label .\n" +
                        "FILTER(LANG(?label)='" + language + "').\n" +
                        "}";

        Query query = QueryFactory.create(queryStringWithLanguage);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        ResultSet results = qe.execSelect();

        if (results.hasNext() == false) {
            // Query was not successful.
            sparqlServiceBufferSingleResult.put(key, null);
            autosave();
            return null;
        } else {
            // return first entry
            QuerySolution solution = results.next();
            String result = StringOperations.removeLanguageAnnotation(solution.getLiteral("label").getString());
            sparqlServiceBufferSingleResult.put(key, result);
            return result;
        }
    }


    /**
     * Returns the label of a resource. First, the default language will be used (en), then no language tag will
     * be used. If there are multiple results, only the first one will be returned.
     * This method is also suited for the XL ALOD endpoint.
     * @param sparqlEndpoint
     * @param uri
     * @return
     */
    public String getLabel(String sparqlEndpoint, String uri){
        // buffered convenience call
        String defaultLabel = getLabelInSpecifiedLanguage(sparqlEndpoint, uri, DEFAULT_LANGUAGE);
        if(defaultLabel != null){
            return defaultLabel;
        }

        uri = StringOperations.convertToTag(uri);
        String key = sparqlEndpoint + uri; // key for buffer

        // lazy buffer init if not initialized yet
        lazyInitBufferSingleResult();

        // lookup in buffer
        if(sparqlServiceBufferSingleResult.containsKey(key)){
            return sparqlServiceBufferSingleResult.get(key);
        }

        String queryStringWithoutLanguage =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "SELECT ?label WHERE {\n" +
                        uri + " rdfs:label ?label .\n" +
                        "}";

        Query query = QueryFactory.create(queryStringWithoutLanguage);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        ResultSet results = safeExecution(qe);

        if (results.hasNext() == false) {
            // Query was not successful.
            sparqlServiceBufferSingleResult.put(key, null);
            autosave();
            return null;
        } else {
            // return first entry
            QuerySolution solution = results.next();
            return StringOperations.removeLanguageAnnotation(solution.getLiteral("label").getString());
        }
    }


    /**
     * Retrieves a list of broader concepts.
     * This method is specific to the WebIsALOD endpoint
     * (<a href="http://webisa.webdatacommons.org/">http://webisa.webdatacommons.org/</a>).
     * The method recognizes whether the given URI belongs to the Classic ALOD or XL ALOD data set and uses the correct
     * query endpoint.
     * @param uri The URI for which broader concepts shall be retrieved.
     * @param minConfidence Minimal confidence.
     * @param topKbroaderConcepts Number of broader concepts to be retrieved.
     * @return A list of URIs of broader concepts.
     */
    public HashSet<String> getBroaderConcepts(String uri, double minConfidence, int topKbroaderConcepts) {

        uri = StringOperations.convertToTag(uri);
        String key = uri + "_getBroader_" + minConfidence + "_" + topKbroaderConcepts;

        lazyInitBufferResultSet();

        // lookup in buffer
        if(sparqlServiceBufferResultSet.containsKey(key)){
            return sparqlServiceBufferResultSet.get(key);
        }

        HashSet<String> result = new HashSet<>();
        String queryString = formulateQueryForBroaderConcepts(uri, minConfidence, topKbroaderConcepts);
        String sparqlEndpoint = getEndpoint(uri);

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        ResultSet results = safeExecution(qe);

        if (results.hasNext() == false) {
            // Query was not successful.
            sparqlServiceBufferResultSet.put(key, null);
            autosave();
            return null;
        }

        String hyponym = "";
        while (results.hasNext()) {
            hyponym = "";
            QuerySolution solution = results.next();

            // add obj to list if found
            if (solution.getResource("hypernym") != null && !solution.getResource("hypernym").equals("")) {
                hyponym = solution.getResource("hypernym").toString();
                result.add(hyponym);
            }
        }
        //LOG.debug("Broader concepts for "+ uri + " found.");

        // add to buffer and return
        sparqlServiceBufferResultSet.put(key, result);
        autosave();
        return result;
    }


    /**
     * Retrieves a list of broader concepts with the corresponding confidence.
     * The method recognizes whether the given URI belongs to the Classic ALOD or XL ALOD data set and uses the correct
     * query endpoint.
     * @param uri The URI for which broader concepts with confidence shall be retrieved.
     * @param minConfidence Minimal confidence.
     * @param topKbroaderConcepts Number of broader concepts to be retrieved.
     * @return A list of URIs of broader concepts with their confidence.
     */
    public ArrayList<ConceptConfidenceTuple> getBroaderConceptsWithConfidence(String uri, double minConfidence, int topKbroaderConcepts){
        if(uri == null){
            return null;
        }
        uri = StringOperations.convertToTag(uri);
        boolean isXLdataSet = false; // indicator whether the XL data set is used

        // decide which endpoint is to be used.
        String sparqlEndpoint = "";
        if(uri.endsWith("_>")) {
            sparqlEndpoint = "http://webisa.webdatacommons.org/sparql";
            isXLdataSet = false;
        } else {
            sparqlEndpoint = "http://webisxl.webdatacommons.org/sparql";
            isXLdataSet = true;
        }

        // get correct buffer lazily
        HashMap<String, ArrayList<ConceptConfidenceTuple>> buffer = getConfidenceBuffer(isXLdataSet);

        // lookup in buffer
        String key = uri + "_getBroader_" + minConfidence + "_" + topKbroaderConcepts;
        if(buffer.containsKey(key)){
            return buffer.get(key);
        }

        ArrayList<ConceptConfidenceTuple> result = new ArrayList<>();

        // build query string according to data set (XL or Classic)
        String queryString = "";
        if(isXLdataSet){
            queryString = formulateQueryForBroaderConceptsXL(uri, minConfidence, topKbroaderConcepts);
        } else {
            queryString = formulateQueryForBroaderConceptsClassic(uri, minConfidence, topKbroaderConcepts);
        }

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);

        // secure select
        ResultSet results = safeExecution(qe);

        if (results.hasNext() == false) {
            // Query was not successful.
            buffer.put(key, null);
            autosave();
            return null;
        }

        String hypernym = "";
        while (results.hasNext()) {
            hypernym = "";
            double confidence;
            QuerySolution solution = results.next();

            // add obj to list if found
            if (solution.getResource("hypernym") != null && !solution.getResource("hypernym").equals("")) {
                hypernym = solution.getResource("hypernym").toString();
                confidence = Double.parseDouble(StringOperations.cleanValueFromTypeAnnotation(solution.getLiteral("minConfidence").toString()));
                result.add(new ConceptConfidenceTuple(hypernym, confidence));
                // LOG.debug("Broader concept of "+ obj + " found.");
                // LOG.debug(hypernym + " (" + confidence + ")");
            }
        }

        // add to buffer and return
        buffer.put(key, result);
        autosave();
        return result;
    }


    /**
     * Get narrower concepts together with their confidence.
     * @param uri The URI for which narrower concepts shall be retrieved.
     * @param minConfidence Minimal confidence threshold.
     * @param topKnarrowerConcepts LIMIT annotation in query.
     * @return Narrower concept as List, sorted according to confidene.
     */
    public ArrayList<ConceptConfidenceTuple> getNarrowerConceptsWithConfidence(String uri, double minConfidence, int topKnarrowerConcepts) {
        uri = StringOperations.convertToTag(uri);

        // decide which endpoint is to be used.
        boolean isXLdataSet; // indicator whether the XL data set is used
        String sparqlEndpoint = "";
        if(uri.endsWith("_>")) {
            sparqlEndpoint = ALOD_CLASSIC_ENDPOINT;
            lazyInitConfidenceBufferClassic();
            isXLdataSet = false;
        } else {
            sparqlEndpoint = ALOD_XL_ENDPOINT;
            lazyInitConfidenceBufferXL();
            isXLdataSet = true;
        }

        // lookup in buffer
        String key = uri + "_getNarrower_" + minConfidence + "_" + topKnarrowerConcepts;
        if(isXLdataSet && sparqlServiceConfidenceBufferXL.containsKey(key)){
            return sparqlServiceConfidenceBufferXL.get(key);
        } else if (sparqlServiceConfidenceBufferClassic.containsKey(key)){
            return sparqlServiceConfidenceBufferClassic.get(key);
        }
        ArrayList<ConceptConfidenceTuple> result = new ArrayList<>();

        // build query string according to data set (XL or Classic)
        String queryString = "";
        if(isXLdataSet){
            queryString = formulateQueryForNarrowerConceptsWithConfidenceXL(uri, minConfidence, topKnarrowerConcepts);
        } else {
            queryString = formulateQueryForNarrowerConceptsWithConfidenceClassic(uri, minConfidence, topKnarrowerConcepts);
        }
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        ResultSet results = safeExecution(qe);

        if (results.hasNext() == false) {
            // Query was not successful.
            if(isXLdataSet){
                sparqlServiceConfidenceBufferXL.put(key, null);
            } else {
                sparqlServiceConfidenceBufferClassic.put(key, null);
            }

            // also use result set buffer
            lazyInitBufferResultSet();
            sparqlServiceBufferResultSet.put(key, null);
            autosave();
            return null;
        }

        String hyponym = "";
        Double confidence = 0.0;
        while (results.hasNext()) {
            hyponym = "";
            QuerySolution solution = results.next();

            // add obj to list if found
            if (solution.getResource("hyponym") != null && !solution.getResource("hyponym").equals("")) {
                hyponym = solution.getResource("hyponym").toString();
                confidence = Double.parseDouble(StringOperations.cleanValueFromTypeAnnotation(solution.getLiteral("minConfidence").toString()));
                result.add(new ConceptConfidenceTuple(hyponym, confidence));
                LOG.debug("Narrower concept of "+ uri + " found.");
                LOG.debug(hyponym + " (" + confidence + ")");
            }
        }
        // add to buffer and return
        if(isXLdataSet){
            sparqlServiceConfidenceBufferXL.put(key, result);
        } else {
            sparqlServiceConfidenceBufferClassic.put(key,result);
        }
        autosave();
        return result;
    }


    /**
     * Retrieves a list of narrower concepts (without confidence).
     * This method is specific to the WebIsALOD endpoint
     * (<a href="http://webisa.webdatacommons.org/">http://webisa.webdatacommons.org/</a>).
     * @param uri The URI for which narrower concepts shall be retrieved.
     * @param minConfidence Minimal confidence.
     * @param topKnarrowerConcepts Number of narrower concepts to be retrieved.
     * @return A list of URIs of broader concepts.
     */
    public HashSet<String> getNarrowerConcepts(String uri, double minConfidence, int topKnarrowerConcepts){
        uri = StringOperations.convertToTag(uri);
        String key = uri + "_getNarrower_" + minConfidence + "_" + topKnarrowerConcepts;

        // lookup in buffer
        lazyInitBufferResultSet();
        if(sparqlServiceBufferResultSet.containsKey(key)){
            return sparqlServiceBufferResultSet.get(key);
        }

        HashSet<String> result = new HashSet<>();
        String queryString = formulateQueryForNarrowerConcepts(uri, minConfidence, topKnarrowerConcepts);
        String sparqlEndpoint = getEndpoint(uri);

        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        ResultSet results = safeExecution(qe);

        if (results.hasNext() == false) {
            // Query was not successful.
            sparqlServiceBufferResultSet.put(key, null);
            autosave();
            return null;
        }

        String hyponym = "";
        while (results.hasNext()) {
            hyponym = "";
            QuerySolution solution = results.next();

            // add obj to list if found
            if (solution.getResource("hyponym") != null && !solution.getResource("hyponym").equals("")) {
                hyponym = solution.getResource("hyponym").toString();
                result.add(hyponym);
            }
        }

        //LOG.debug("Narrower concepts of "+ uri + " found.");

        // add to buffer and return
        sparqlServiceBufferResultSet.put(key, result);
        autosave();
        return result;
    }


    /**
     * Returns the appropriate endpoint given an URI.
     * @param uri URI for endpoint.
     * @return Endpoint as String
     */
    private static String getEndpoint(String uri){
        if(isALODxlEndpointConcept(uri)){
            return ALOD_XL_ENDPOINT;
        } else {
            return ALOD_CLASSIC_ENDPOINT;
        }
    }


    /**
     * Determines whether a concept has a hypernym.
     * @param uri the URI for which hypernymy has to be checked.
     * @return True if a hypernym exists, else false.
     */
    public boolean hasBroaderConcepts(String uri){
        String key = "hasBroader_" + uri;
        lazyInitAskBuffer();
        if(sparqlServiceAskBuffer.containsKey(key)){
            return sparqlServiceAskBuffer.get(key);
        }
        uri = StringOperations.convertToTag(uri);
        String sparqlEndpoint = getEndpoint(uri);
        String queryString =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "ASK {\n" +
                        uri + " skos:broader ?hypernym .\n" +
                        "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        boolean result =  safeAsk(qe);
        sparqlServiceAskBuffer.put(key, result);
        return result;
    }


    /**
     * Determines whether a concept is a hypernym of some other concet.
     * @param uri the URI for which hypernymy has to be checked.
     * @return True if a hyponym exists, else false.
     */
    public boolean hasNarrowerConcepts(String uri){
        String key = "hasNarrower_" + uri;
        lazyInitAskBuffer();
        if(sparqlServiceAskBuffer.containsKey(key)){
            return sparqlServiceAskBuffer.get(key);
        }
        uri = StringOperations.convertToTag(uri);
        String sparqlEndpoint = getEndpoint(uri);
        String queryString =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "ASK {\n" +
                        "?hyponym skos:broader "+ uri + ".\n" +
                        "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        boolean result =  safeAsk(qe);
        sparqlServiceAskBuffer.put(key, result);
        return result;
    }


    /**
     * Checks for the existence of braoder concepts given a minimal confidence threshold.
     * @param uri
     * @param minConfidence
     * @return
     */
    public boolean hasBroaderConcepts(String uri, double minConfidence){
        String key = "hasBroader_" + uri + "_mincf_" + minConfidence;
        lazyInitAskBuffer();
        if(sparqlServiceAskBuffer.containsKey(key)){
            return sparqlServiceAskBuffer.get(key);
        }
        uri = StringOperations.convertToTag(uri);
        String sparqlEndpoint = getEndpoint(uri);
        String queryString = formulateQueryForHasBroader(uri, minConfidence);
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        boolean result =  safeAsk(qe);
        sparqlServiceAskBuffer.put(key, result);
        return result;
    }


    /**
     * Checks for the existence of braoder concepts given a minimal confidence threshold.
     * @param uri
     * @param minConfidence
     * @return
     */
    public boolean hasNarrowerConcepts(String uri, double minConfidence){
        String key = "hasNarrower_" + uri + "_mincf_" + minConfidence;
        lazyInitAskBuffer();
        if(sparqlServiceAskBuffer.containsKey(key)){
            return sparqlServiceAskBuffer.get(key);
        }
        uri = StringOperations.convertToTag(uri);
        String sparqlEndpoint = getEndpoint(uri);
        String queryString = formulateQueryForHasNarrower(uri, minConfidence);
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
        boolean result =  safeAsk(qe);
        sparqlServiceAskBuffer.put(key, result);
        return result;
    }


    /**
     * When executing queries it sometimes comes to exceptions (most likely http exceptions).
     * This method executes in a safe environment and will retry after some seconds, when the execution fails.
     * @param queryExecutionInstance
     * @return
     */
    public static boolean safeAsk(QueryExecution queryExecutionInstance){
        boolean result;
        try {
            result = queryExecutionInstance.execAsk();
        } catch (Exception e) {
            LOG.error("An exception occurred while querying. Waiting for 15 seconds...");
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            LOG.error("Retry:");
            result = queryExecutionInstance.execAsk();
        } // end of catch
        return result;
    }


    /**
     * When executing queries it sometimes comes to exceptions (most likely http exceptions).
     * This method executes in a safe environment and will retry after some seconds, when the execution fails.
     * @param queryExecutionInstance Query Execution Object.
     * @return ResultSet Object. Null, if no result after second attempt.
     */
    public static ResultSet safeExecution(QueryExecution queryExecutionInstance){
        ResultSet results;
        try {
            results = queryExecutionInstance.execSelect();
        } catch (Exception e){
            // most likely a http exception
            e.printStackTrace();
            LOG.error("An exception occurred while querying. Waiting for 15 seconds...");
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            LOG.error("Retry");
            results = queryExecutionInstance.execSelect();
        }
        return results;
    }


    /**
     * Count the direct number of broader concepts without querying for those. Use this method if you are not interested
     * in the broader concepts themselves and want a high-performance query.
     * Works for XL and Classic ALOD endpoint.
     * @param uri The URI for which the number of broader concepts shall be calculated.
     * @param minConfidence The minimal confidence.
     * @return The number as integer.
     */
    public int getNumberOfBroaderConcepts(String uri, double minConfidence){
        final String persistenceKey = "numbroader_" + uri + "_" + minConfidence;

        // buffer
        lazyInitNumberBuffer();
        if(sparqlServiceNumberBuffer.containsKey(persistenceKey)){
            return sparqlServiceNumberBuffer.get(persistenceKey);
        }

        String sparqlEndpoint = getEndpoint(uri);

        String queryString = formulateQueryForNumberOfBroaderConcepts(uri, minConfidence);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryString);

        ResultSet result = safeExecution(qe);

        if(result.hasNext()){
            QuerySolution s = result.next();
            int resultInt = s.getLiteral("total").getInt();
            sparqlServiceNumberBuffer.put(persistenceKey, resultInt);
            return resultInt;
        } else {
            sparqlServiceNumberBuffer.put(persistenceKey, 0);
            return 0;
        }
    }


    /**
     * Returns the number of narrower concepts of the specified URI.
     * @param uri The URI for which narrower concepts shall be counted.
     * @param minConfidence Minimum confidence criterion.
     * @return Number of narrower concepts as int.
     */
    public int getNumberOfNarrowerConcepts(String uri, double minConfidence){
        String sparqlEndpoint;
        String queryString;
        final String persistenceKey = "numnarrower_" + uri + "_" + minConfidence;

        // buffer
        lazyInitNumberBuffer();
        if(sparqlServiceNumberBuffer.containsKey(persistenceKey)){
            return sparqlServiceNumberBuffer.get(persistenceKey);
        }

        if(isALODxlEndpointConcept(uri)){
            sparqlEndpoint = ALOD_XL_ENDPOINT;
        } else {
            sparqlEndpoint = ALOD_CLASSIC_ENDPOINT;
        }
        queryString = formulateQueryForNumberOfNarrowerConcepts(uri, minConfidence);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryString);
        ResultSet result = safeExecution(qe);

        if(result.hasNext()){
            QuerySolution s = result.next();
            int resultInt = s.getLiteral("total").getInt();
            sparqlServiceNumberBuffer.put(persistenceKey, resultInt);
            return resultInt;
        } else {
            sparqlServiceNumberBuffer.put(persistenceKey, 0);
            return 0;
        }
    }


    /**
     * Returns the number of common concepts on any desirable level. Can distinguish between ALOD Classic and ALOD XL.
     * @param uri1 URI 1.
     * @param uri2 URI 2.
     * @param level Level.
     * @param minConfidence The minimal confidence criterion.
     * @return The number as int.
     */
    public int getNumberOfCommonBroaderConcepts(String uri1, String uri2, int level, double minConfidence){
        final String persistenceKey = "numcommon_" + uri1 + "_" + uri2 +  "_" + minConfidence + level;

        // buffer
        lazyInitNumberBuffer();
        if(sparqlServiceNumberBuffer.containsKey(persistenceKey)){
            return sparqlServiceNumberBuffer.get(persistenceKey);
        }

        String sparqlEndpoint = getEndpoint(uri1);
        String queryString = formulateQueryForNumberOfCommonBroaderConcepts(uri1, uri2, level, minConfidence);
        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryString);
        ResultSet result = safeExecution(qe);

        if(result.hasNext()){
            QuerySolution s = result.next();
            int resultInt = s.getLiteral("total").getInt();
            sparqlServiceNumberBuffer.put(persistenceKey, resultInt);
            return resultInt;
        } else {
            sparqlServiceNumberBuffer.put(persistenceKey, 0);
            return 0;
        }
    }


    /**
     * Iteratively gets all broader concept of the URI specified.
     * @param uri The URI for which all broader concepts shall be retrieved.
     * @param minimalConfidence The minimal confidence for broader concepts. Set to 0.0 if this restriction shall not be used.
     * @param limit The top k broader concepts which shall be used per concept. Set to MAX_LIMIT_SPARQL if this restriction shall not be used.
     * @return A set of all broader concepts.
     */
    public HashSet<String> getAllBroaderConcepts(String uri, double minimalConfidence, int limit) {
        HashSet<String> result = new HashSet<>();
        HashSet<String> conceptsForNextOperation = new HashSet<>();

        // initial step
        HashSet<String> broaderConcepts = getBroaderConcepts(uri, minimalConfidence, limit);

        // cancel operation if there are no broader concepts
        if (broaderConcepts == null || broaderConcepts.size() == 0) {
            return null;
        }

        result.addAll(broaderConcepts);
        conceptsForNextOperation.addAll(broaderConcepts);

        int conceptsBefore = 0;
        int conceptsAfter = result.size();
        while (conceptsAfter > conceptsBefore) {

            conceptsBefore = result.size();
            HashSet<String> conceptsForNextNextOperation = new HashSet<>();

            for (String concept : conceptsForNextOperation) {
                broaderConcepts = getBroaderConcepts(concept, minimalConfidence, limit);
                if (broaderConcepts != null) {
                    conceptsForNextNextOperation.addAll(broaderConcepts);
                    result.addAll(broaderConcepts);
                }
            } // end of for loop
            conceptsAfter = result.size();
            conceptsForNextOperation = conceptsForNextNextOperation;
        } // end of while loop
        return result;
    }


    /**
     * High-performance check of whether B is a hypernym of A.
     * The method recognizes whether the given URI belongs to the Classic ALOD or XL ALOD data set and uses the correct
     * query endpoint.
     * @param aURI
     * @param bURI
     * @param level
     * @return
     */
    public boolean aHasBroaderConceptB(String aURI, String bURI, int level) {
        String sparqlEndpoint;
        String persistenceKey = "aBroaderB_" + aURI + "_" + bURI + "_" + level;

        // buffer mechanism
        lazyInitAskBuffer();
        if(sparqlServiceAskBuffer.containsKey(persistenceKey)){
            return sparqlServiceAskBuffer.get(persistenceKey);
        }

        if (isALODxlEndpointConcept(aURI)) {
            sparqlEndpoint = ALOD_XL_ENDPOINT;
        } else {
            sparqlEndpoint = ALOD_CLASSIC_ENDPOINT;
        }

        for(int currentLevel = 1; currentLevel <= level; currentLevel++){
            String queryString = formulateQueryHasBroader(aURI, bURI, currentLevel);
            Query query = QueryFactory.create(queryString);
            QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
            boolean result = safeAsk(qe);

            if(result){
                sparqlServiceAskBuffer.put(persistenceKey,true);
                return true;
            }

        } // end of level loop
        sparqlServiceAskBuffer.put(persistenceKey,false);
        return false;
    }


    /**
     * Checks whether the URI in question is a concept of the ALOD XL endpoint.
     * @param uri The URI which shall be checked.
     * @return true if URI is XL endpoint concept.
     */
    public static boolean isALODxlEndpointConcept(String uri){
        try {
            uri = StringOperations.convertToTag(uri);
            if (uri.endsWith("_>")) {
                return false;
            } else {
                return true;
            }
        } catch (NullPointerException npe){
            System.out.println(uri);
        }
        return false;
    }


    /**
     * Checks whether B is a hypernym of A.
     * The method recognizes whether the given URI belongs to the Classic ALOD or XL ALOD data set and uses the correct
     * query endpoint.
     * @param aURI
     * @param bURI
     * @param level
     * @param minimalConfidence
     * @param limit
     * @return
     */
    public boolean aHasBroaderConceptB(String aURI, String bURI, int level, double minimalConfidence, int limit){
        HashSet<String> allBroaderConcepts = new HashSet<>();
        HashSet<String> conceptsForNextOperation = new HashSet<>();

        // initial step
        HashSet<String> broaderConcepts = getBroaderConcepts(aURI, minimalConfidence, limit);

        // cancel operation if there are no broader concepts
        if (broaderConcepts == null || broaderConcepts.size() == 0) {
            return false;
        } else if (broaderConcepts.contains(StringOperations.removeTag(bURI))){
            return true;
        }

        allBroaderConcepts.addAll(broaderConcepts);
        conceptsForNextOperation.addAll(broaderConcepts);

        int conceptsBefore = 0;
        int conceptsAfter = allBroaderConcepts.size();
        int currentLevel = 2;
        while (conceptsAfter > conceptsBefore && currentLevel <= level) {

            conceptsBefore = allBroaderConcepts.size();
            HashSet<String> conceptsForNextNextOperation = new HashSet<>();

            for (String concept : conceptsForNextOperation) {
                broaderConcepts = getBroaderConcepts(concept, minimalConfidence, limit);
                if (broaderConcepts != null) {
                    if (broaderConcepts.contains(StringOperations.removeTag(bURI))){
                        return true;
                    }
                    conceptsForNextNextOperation.addAll(broaderConcepts);
                    allBroaderConcepts.addAll(broaderConcepts);
                }
            } // end of for loop

            conceptsAfter = allBroaderConcepts.size();
            conceptsForNextOperation = conceptsForNextNextOperation;
            currentLevel++;
        } // end of while loop
        return false;
    }



    //--------------------------------------------------------------
    // Query Formulation Below
    //--------------------------------------------------------------

    /**
     * Formulate the query to get broader concepts.
     * @param uri The URI for which broader concepts shall be retrieved.
     *            Example: "http://webisa.webdatacommons.org/concept/_president_"
     * @param minConfidence The minimal confidence level of a broader concept.
     * @param limit The maximal number of broader concepts to be retrieved.
     * @return The query string.
     */
    private String formulateQueryForBroaderConceptsClassic(String uri, double minConfidence, int limit) {
        uri = StringOperations.convertToTag(uri);
        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "PREFIX isa: <http://webisa.webdatacommons.org/concept/>\n" +
                        "PREFIX isaont: <http://webisa.webdatacommons.org/ontology#> \n" +
                        "select distinct ?hypernym ?minConfidence where \n" +
                        "{\n" +
                        "GRAPH ?g {\n" +
                        uri + " skos:broader ?hypernym.\n" +
                        "}\n" +
                        "?g isaont:hasConfidence ?minConfidence.\n" +
                        "FILTER(?minConfidence > "+ minConfidence +")\n" +
                        "} \n" +
                        "ORDER BY DESC(?minConfidence)\n" +
                        "LIMIT " + Math.min(limit, 10000); // 10000 is the maximal number allowed in limit
        return query;
    }


    /**
     * Formulate the query to get broader concepts.
     * @param uri The URI for which broader concepts shall be retrieved.
     *            Example: "http://webisa.webdatacommons.org/concept/_president_"
     * @param minConfidence The minimal confidence level of a broader concept.
     * @param limit The maximal number of broader concepts to be retrieved.
     * @return The query string.
     */
    private String formulateQueryForBroaderConceptsXL(String uri, double minConfidence, int limit) {
        uri = StringOperations.convertToTag(uri);
        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "select distinct ?hypernym ?minConfidence where\n" +
                        "{\n" +
                        "GRAPH ?g {\n" +
                        uri +  " skos:broader ?hypernym .\n" +
                        "}\n" +
                        "?g <http://webisa.webdatacommons.org/ontology/hasConfidence> ?minConfidence .\n" +
                        "FILTER(?minConfidence > " + minConfidence+ ")\n" +
                        "}\n" +
                        "ORDER BY DESC(?minConfidence)\n" +
                        "LIMIT " + Math.min(limit, 10000); // 10000 is the maximal number allowed in limit
        return query;
    }


    /**
     * Formulate the query to get narrower concepts from the ALOD Classic Endpoint.
     * @param uri The URI for which narrower concepts shall be retrieved.
     *            Example: "http://webisa.webdatacommons.org/concept/_president_"
     * @param minConfidence  The minimal confidence level of a narrower concept.
     * @param limit The maximal number of narrower concepts to be retrieved.
     * @return The query string.
     */
    private String formulateQueryForNarrowerConceptsWithConfidenceClassic(String uri, double minConfidence, int limit){
        uri = StringOperations.convertToTag(uri);
        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "PREFIX isa: <http://webisa.webdatacommons.org/concept/>\n" +
                        "PREFIX isaont: <http://webisa.webdatacommons.org/ontology#>  \n" +
                        "select ?hyponym ?minConfidence where  \n" +
                        "{\n" +
                        "    GRAPH ?g {\n" +
                        "         ?hyponym skos:broader " + uri + "\n" +
                        "    }\n" +
                        "    ?g isaont:hasConfidence ?minConfidence .\n" +
                        "    FILTER(?minConfidence >  " + minConfidence + ")\n" +
                        "}  \n" +
                        "ORDER BY DESC(?minConfidence)\n" +
                        "LIMIT " + Math.min(limit, 10000); // 10000 is the maximal number allowed in limit
        return query;
    }


    /**
     * Formulate the query to get narrower concepts from the ALOD XL endpoint.
     * @param uri The URI for which narrower concepts shall be retrieved.
     *            Example: "http://webisa.webdatacommons.org/concept/president"
     * @param minConfidence  The minimal confidence level of a narrower concept.
     * @param limit The maximal number of narrower concepts to be retrieved.
     * @return The query string.
     */
    private String formulateQueryForNarrowerConceptsWithConfidenceXL(String uri, double minConfidence, int limit){
        uri = StringOperations.convertToTag(uri);
        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + // required for broader
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "select ?hyponym ?minConfidence where  \n" +
                        "{\n" +
                        "    GRAPH ?g {\n" +
                        "         ?hyponym skos:broader " + uri + "\n" +
                        "    }\n" +
                        "    ?g <http://webisa.webdatacommons.org/ontology/hasConfidence> ?minConfidence .\n" +
                        "    FILTER(?minConfidence >  " + minConfidence + ")\n" +
                        "}  \n" +
                        "ORDER BY DESC(?minConfidence)\n" +
                        "LIMIT " + Math.min(limit, 10000); // 10000 is the maximal number allowed in limit
        return query;
    }


    /**
     * Get the string representation for a SPARQL query which checks for the existence of broader concepts given a
     * minimal confidence threshold.
     * @param uri The URI to look for.
     * @param minConfidence Minimal confidence threshold.
     * @return Query as String representation.
     */
    private String formulateQueryForHasBroader(String uri, double minConfidence){
        uri = StringOperations.convertToTag(uri);
        final String confidenceTag = getConfidenceTag(uri);
        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "ASK {\n" +
                        "  GRAPH ?g {\n" +
                        "               " + uri + "skos:broader ?hypernym .\n" +
                        "           }\n" +
                        "  ?g " + confidenceTag + " ?minConfidence . \n" +
                        "    FILTER(?minConfidence >  " + minConfidence + ")\n" +
                        "}";
        return query;
    }


    /**
     * Get the string representation for a SPARQL query which checks for the existence of broader concepts given a
     * minimal confidence threshold.
     * @param uri The URI to look for.
     * @param minConfidence Minimal confidence threshold.
     * @return Query as String representation.
     */
    private String formulateQueryForHasNarrower(String uri, double minConfidence){
        uri = StringOperations.convertToTag(uri);
        final String confidenceTag = getConfidenceTag(uri);
        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "ASK {\n" +
                        "  GRAPH ?g {\n" +
                        "                ?hypernym skos:broader " + uri + " .\n" +
                        "           }\n" +
                        "  ?g " + confidenceTag + " ?minConfidence . \n" +
                        "    FILTER(?minConfidence >  " + minConfidence + ")\n" +
                        "}";
        return query;
    }


    /**
     * Formulate a query to retrieve the top {@code limit} narrower concepts.
     * This method can handle the Classic and the XL data set.
     * @param uri URI for which narrower concepts shall be retrieved.
     * @param minConfidence Minimal confidence.
     * @param limit LIMIT clause in query.
     * @return Query as String.
     */
    private String formulateQueryForNarrowerConcepts(String uri, double minConfidence, int limit){
        uri = StringOperations.convertToTag(uri);
        final String confidenceTagClassic = "<http://webisa.webdatacommons.org/ontology#hasConfidence>";
        final String confidenceTagXL = "<http://webisa.webdatacommons.org/ontology/hasConfidence>";
        final String confidenceTag;

        if(isALODxlEndpointConcept(uri)){
            confidenceTag = confidenceTagXL;
        } else {
            confidenceTag = confidenceTagClassic;
        }

        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + // required for broader
                        "select ?hyponym where  \n" +
                        "{\n" +
                        "    GRAPH ?g {\n" +
                        "         ?hyponym skos:broader"  + uri + "\n" +
                        "    }\n" +
                        "    ?g " + confidenceTag + " ?minConfidence .\n" +
                        "    FILTER(?minConfidence >  " + minConfidence + ")\n" +
                        "}  \n" +
                        "ORDER BY DESC(?minConfidence)\n" +
                        "LIMIT " + Math.min(limit, 10000); // 10000 is the maximal number allowed in limit
        return query;
    }


    /**
     * Formulate a query to retrieve the top {@code limit} narrower concepts.
     * This method can handle the Classic and the XL data set.
     * @param uri URI for which narrower concepts shall be retrieved.
     * @param minConfidence Minimal confidence.
     * @param limit LIMIT clause in query.
     * @return Query as String.
     */
    private String formulateQueryForBroaderConcepts(String uri, double minConfidence, int limit){
        uri = StringOperations.convertToTag(uri);
        final String confidenceTagClassic = "<http://webisa.webdatacommons.org/ontology#hasConfidence>";
        final String confidenceTagXL = "<http://webisa.webdatacommons.org/ontology/hasConfidence>";
        final String confidenceTag;
        if(isALODxlEndpointConcept(uri)){
            confidenceTag = confidenceTagXL;
        } else {
            confidenceTag = confidenceTagClassic;
        }

        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" + // required for broader
                        "select ?hypernym where  \n" +
                        "{\n" +
                        "    GRAPH ?g {\n" +
                        "    " + uri + " skos:broader ?hypernym" + "\n" +
                        "    }\n" +
                        "    ?g " + confidenceTag + " ?minConfidence .\n" +
                        "    FILTER(?minConfidence >  " + minConfidence + ")\n" +
                        "}  \n" +
                        "ORDER BY DESC(?minConfidence)\n" +
                        "LIMIT " + Math.min(limit, 10000); // 10000 is the maximal number allowed in limit
        return query;
    }



    /**
     * Formulate a query to evaluate whether aURI has bURI as broader concept upt to a specified level.
     * This query works on ALOD Classic and on ALOD XL.
     * @param aURI Hyponym URI
     * @param bURI Hypernym URI
     * @param level Level up to which checks shall run.
     * @return The query
     */
    private static String formulateQueryHasBroader(String aURI, String bURI, int level){
        aURI = StringOperations.convertToTag(aURI);
        bURI = StringOperations.convertToTag(bURI);

        String result;
        if(level <= 1){
            result =
                    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                            "ASK {\n" +
                            aURI +  "skos:broader " + bURI + " .\n" +
                            "}";
        } else {
            result = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                    "ASK {\n";
            for(int currentLevel = 1; currentLevel <= level; currentLevel++){

                if(currentLevel == 1) {
                    result = result + aURI + " skos:broader ?" + (char) ('a' + currentLevel) + ".\n";
                } else if(currentLevel == level){
                    result = result + "?" + (char) ('a' + currentLevel) + " skos:broader " + bURI + " .\n";
                } else {
                    result = result + "?" + (char) ('a' + currentLevel -1 ) + " skos:broader ?" + (char) ('a' + currentLevel) + ".\n";
                }
            }
            result = result + "}";
        }
        return result;
    }


    /**
     * Formulate the query to retrieve the number of direct broader concepts above a given minimal confidence threshold.
     * This query works for ALOD classic and ALOD XL.
     * @param uri The URI for which the number of broader concepts shall be retrieved.
     * @param minConfidence The minimal confidence threshold.
     * @return The query string.
     */
    private static String formulateQueryForNumberOfBroaderConcepts(String uri, double minConfidence){

        uri = StringOperations.convertToTag(uri);
        final String confidenceTagClassic = "<http://webisa.webdatacommons.org/ontology#hasConfidence>";
        final String confidenceTagXL = "<http://webisa.webdatacommons.org/ontology/hasConfidence>";
        final String confidenceTag;

        if(isALODxlEndpointConcept(uri)){
            confidenceTag = confidenceTagXL;
        } else {
            confidenceTag = confidenceTagClassic;
        }

        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "PREFIX isa: <http://webisa.webdatacommons.org/concept/>\n" +
                        "SELECT (COUNT(*) as ?total) WHERE  \n" +
                        "    {\n" +
                        "        GRAPH ?g {\n" +
                        uri + "  skos:broader ?hypernym .\n" +
                        "        }\n" +
                        "        ?g " + confidenceTag + " ?minConfidence .\n" +
                        "        FILTER(?minConfidence > " + minConfidence + ")\n" +
                        "    }";
        return query;
    }


    /**
     * The resulting query works with the ALOD Classic and the ALOD XL data set.
     * @param uri The URI for which the number of narrower concepts is to be retrieved.
     * @param minConfidence The minimal confidence.
     * @return The number as int.
     */
    private static String formulateQueryForNumberOfNarrowerConcepts(String uri, double minConfidence){

        uri = StringOperations.convertToTag(uri);
        final String confidenceTagClassic = "<http://webisa.webdatacommons.org/ontology#hasConfidence>";
        final String confidenceTagXL = "<http://webisa.webdatacommons.org/ontology/hasConfidence>";
        final String confidenceTag;

        if(isALODxlEndpointConcept(uri)){
            confidenceTag = confidenceTagXL;
        } else {
            confidenceTag = confidenceTagClassic;
        }

        String query =
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                        "PREFIX isa: <http://webisa.webdatacommons.org/concept/>\n" +
                        "SELECT (COUNT(*) as ?total) WHERE  \n" +
                        "    {\n" +
                        "        GRAPH ?g {\n" +
                        "             ?hyponym skos:broader " + uri + " \n" +
                        "        }\n" +
                        "        ?g " + confidenceTag + " ?minConfidence .\n" +
                        "        FILTER(?minConfidence > " + minConfidence + ")\n" +
                        "    }";
        return query;
    }


    /**
     * Get the appropriate confidence tag given the URI.
     * @param uri
     * @return
     */
    private String getConfidenceTag(String uri){
        if(isALODxlEndpointConcept(uri)){
            return CONFIDENCE_TAG_XL;
        } else {
            return CONFIDENCE_TAG_CLASSIC;
        }
    }


    /**
     * A query which will return the number of common broader concepts of uri1 and uri2 on any desirable level in an efficient way.
     * @param uri1 URI 1
     * @param uri2 URI 2
     * @param level The level up to which it shall be looked for.
     * @param minConfidence The minimum confidence threshold.
     * @return The query as String.
     */
    public static String formulateQueryForNumberOfCommonBroaderConcepts(String uri1, String uri2, int level, double minConfidence) {
        String query = formulateQueryForCommonBroaderConcepts(uri1, uri2, level, minConfidence);
        query = query.replace("distinct ?hypernym", "(count(distinct ?hypernym) as ?total)");
        return query;
    }


    /**
     * A query which will return common concepts of uri1 and uri2 on any desirable level in an efficient way.
     * @param uri1 URI 1
     * @param uri2 URI 2
     * @param level The level up to which it shall be looked for.
     * @param minConfidence The minimum confidence threshold.
     * @return The query as String.
     */
    private static String formulateQueryForCommonBroaderConcepts(String uri1, String uri2, int level, double minConfidence){
        // converting to tag
        uri1 = StringOperations.convertToTag(uri1);
        uri2 = StringOperations.convertToTag(uri2);

        final String confidenceTagClassic = "<http://webisa.webdatacommons.org/ontology#hasConfidence>";
        final String confidenceTagXL = "<http://webisa.webdatacommons.org/ontology/hasConfidence>";
        final String confidenceTag;

        // decision on endpoint
        if(isALODxlEndpointConcept(uri1)){
            confidenceTag = confidenceTagXL;
        } else {
            confidenceTag = confidenceTagClassic;
        }

        String result = "select distinct ?hypernym where {\n";
        if(level <= 1){
            result = result +
                    "   GRAPH ?g1 {\n" +
                    uri1 + " <http://www.w3.org/2004/02/skos/core#broader> ?hypernym.\n" +
                    "    }\n" +
                    "   GRAPH ?g2 {\n" +
                    uri2 + " <http://www.w3.org/2004/02/skos/core#broader> ?hypernym.\n" +
                    "   }\n" +
                    "   ?g1 " + confidenceTag + " ?minConfidence1 .\n" +
                    "   ?g2 " + confidenceTag + " ?minConfidence2 .\n" +
                    "   FILTER(?minConfidence1 >   "+ minConfidence +" && ?minConfidence2 >  "+ minConfidence + ")\n" +
                    "}";
        } else {
            // obj 1
            for(int currentLevel = 1; currentLevel <= level; currentLevel++){
                if(currentLevel == 1) {
                    result = result + "      " + uri1 + " <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel) + ".\n";
                } else if(currentLevel == level){
                    result = result + "   GRAPH ?g1 {\n";
                    result = result + "      ?" + (char) ('a' + currentLevel -1) + " <http://www.w3.org/2004/02/skos/core#broader> ?hypernym .\n";
                    result = result + "   }\n";
                } else {
                    result = result + "      ?" + (char) ('a' + currentLevel - 1 ) + " <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel) + ".\n";
                }
            }

            // obj 2
            // the addition of level is required for a correct query
            for(int currentLevel = 1 + level; currentLevel <= level + level; currentLevel++){
                if(currentLevel == 1 + level) {
                    result = result + "      " + uri2 + " <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel) + ".\n";
                } else if(currentLevel == level + level){
                    result = result + "   GRAPH ?g2 {\n";
                    result = result + "      ?" + (char) ('a' + currentLevel -1) + " <http://www.w3.org/2004/02/skos/core#broader> ?hypernym .\n";
                    result = result + "   }\n";
                } else {
                    result = result + "      ?" + (char) ('a' + currentLevel - 1 ) + " <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel) + ".\n";
                }
            }

            result = result +
                    "   ?g1 " + confidenceTag + " ?minConfidence1 .\n" +
                    "   ?g2 " + confidenceTag + " ?minConfidence2 .\n" +
                    "   FILTER(?minConfidence1 >   "+ minConfidence +" && ?minConfidence2 >  "+ minConfidence + ")\n" +
                    "}";
        }
        return result;
    }


    /**
     * A query which will return the number of common narrower concepts of uri1 and uri2 on any desirable level in an efficient way.
     * Leads to a timeout for levels > 1.
     * @param uri1 URI 1
     * @param uri2 URI 2
     * @param level The level up to which it shall be looked for.
     * @param minConfidence The minimum confidence threshold.
     * @return The query as String.
     */
    public static String formulateQueryForNumberOfCommonNarrowerConcepts(String uri1, String uri2, int level, double minConfidence) {
        String query = formulateQueryForCommonNarrowerConcepts(uri1, uri2, level, minConfidence);
        query = query.replace("distinct ?hyponym", "(count(distinct ?hyponym) as ?total)");
        return query;
    }


    /**
     * A query which will return common concepts of uri1 and uri2 on any desirable level in an efficient way.
     * Leads to a timeout for levels > 1.
     * @param uri1 URI 1
     * @param uri2 URI 2
     * @param level The level up to which it shall be looked for.
     * @param minConfidence The minimum confidence threshold.
     * @return The query as String.
     */
    private static String formulateQueryForCommonNarrowerConcepts(String uri1, String uri2, int level, double minConfidence){
        // converting to tag
        uri1 = StringOperations.convertToTag(uri1);
        uri2 = StringOperations.convertToTag(uri2);

        final String confidenceTagClassic = "<http://webisa.webdatacommons.org/ontology#hasConfidence>";
        final String confidenceTagXL = "<http://webisa.webdatacommons.org/ontology/hasConfidence>";
        final String confidenceTag;
        int runningGraphNumber = 0;

        // decision on endpoint
        if(isALODxlEndpointConcept(uri1)){
            confidenceTag = confidenceTagXL;
        } else {
            confidenceTag = confidenceTagClassic;
        }

        String result = "select distinct ?hyponym where {\n";
        if(level <= 1){
            result = result +
                    "   GRAPH ?g1 {\n" +
                    "      ?hyponym <http://www.w3.org/2004/02/skos/core#broader> " + uri1 + ".\n" +
                    "    }\n" +
                    "   GRAPH ?g2 {\n" +
                    "      ?hyponym <http://www.w3.org/2004/02/skos/core#broader> " + uri2 + ".\n" +
                    "   }\n" +
                    "   ?g1 " + confidenceTag + " ?minConfidence1 .\n" +
                    "   ?g2 " + confidenceTag + " ?minConfidence2 .\n" +
                    "   FILTER(?minConfidence1 >   "+ minConfidence +" && ?minConfidence2 >  "+ minConfidence + ")\n" +
                    "}";
        } else {
            // obj 1
            for(int currentLevel = 1; currentLevel <= level; currentLevel++){
                if(currentLevel == 1) {
                    result = result + "   GRAPH ?g" + (++runningGraphNumber) + " {\n";
                    result = result + "   ?" + (char) ('a' + currentLevel) +  " <http://www.w3.org/2004/02/skos/core#broader> ?" + uri1 + ".\n";
                    result = result + "   }\n";
                } else if(currentLevel == level){
                    result = result + "   GRAPH ?g" + (++runningGraphNumber) + " {\n";
                    result = result + "      ?hyponym <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel -1)  + ".\n";
                    result = result + "   }\n";
                } else {
                    result = result + "   GRAPH ?g" + (++runningGraphNumber) + " {\n";
                    result = result + "      ?" + (char) ('a' + currentLevel) + " <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel - 1 )  + ".\n";
                    result = result + "   }\n";
                }
            }

            // obj 2
            // the addition of level is required for a correct query
            for(int currentLevel = 1 + level; currentLevel <= level + level; currentLevel++){
                if(currentLevel == 1 + level) {
                    result = result + "   GRAPH ?g" + (++runningGraphNumber) + " {\n";
                    result = result + "   ?" + (char) ('a' + currentLevel) +  " <http://www.w3.org/2004/02/skos/core#broader> " + uri2 + ".\n";
                    result = result + "   }\n";
                } else if(currentLevel == level + level){
                    result = result + "   GRAPH ?g" + (++runningGraphNumber) + " {\n";
                    result = result + "      ?hyponym <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel -1)  + ".\n";
                    result = result + "   }\n";
                } else {
                    result = result + "   GRAPH ?g" + (++runningGraphNumber) + " {\n";
                    result = result + "      ?" + (char) ('a' + currentLevel) + " <http://www.w3.org/2004/02/skos/core#broader> ?" + (char) ('a' + currentLevel - 1 )  + ".\n";
                    result = result + "   }\n";
                }
            }

            for(int i = 1; i - 1 < runningGraphNumber; i++){
                result = result + "   ?g" + i +" " + confidenceTag + " ?minConfidence" + i + " .\n";
            }

            result = result + "   FILTER(?minConfidence1" + " >   "+ minConfidence;

            for(int i = 2; (i - 1) < runningGraphNumber -1; i++){
                result = result + " && ?minConfidence" + i + " >  "+ minConfidence;
            }

            result = result + " && ?minConfidence" + (runningGraphNumber) + " >  "+ minConfidence + ")\n}";
        }
        return result;
    }


    //--------------------------------------------------------------
    // Buffer Concept Below
    //--------------------------------------------------------------

    public void enableBuffer(){
        useBuffer = true;
    }

    /**
     * Switch buffering mechanism off.
     * This will also lead to current buffers being emptied.
     */
    public void disableBuffer(){
        useBuffer = false;
        autosave = false;
        resetBuffer();
    }

    public void resetBuffer(){
        sparqlServiceBufferSingleResult = null;
        sparqlServiceBufferResultSet = null;
        sparqlServiceConfidenceBufferClassic = null;
        sparqlServiceConfidenceBufferXL = null;
    }

    public boolean isUseBuffer(){
        return useBuffer;
    }


    /**
     * This method will initialize all buffers.
     */
    public void initializeAllBuffers(){
        if(useBuffer) {
            lazyInitBufferSingleResult();
            lazyInitBufferResultSet();
            lazyInitConfidenceBufferClassic();
            lazyInitConfidenceBufferXL();
            lazyInitNumberBuffer();
        } else {
            LOG.debug("Cannot initialize buffers because buffering is switched off. Enable by setting variable useBuffer of the SPARQL service.");
        }
    }

    public void lazyInitBufferSingleResult() {
        if (sparqlServiceBufferSingleResult == null) {
            if (useBuffer) {
                if (PersistenceService.persistenceAvailable("sparqlServiceBufferSingleResult")) {
                    this.sparqlServiceBufferSingleResult = (HashMap<String, String>) PersistenceService.retrieve("sparqlServiceBufferSingleResult");
                    this.sparqlServiceBufferSingleResultInitialCount = sparqlServiceBufferSingleResult.size();
                } else {
                    this.sparqlServiceBufferSingleResult = new HashMap<>();
                }
            } else {
                sparqlServiceBufferSingleResult = new HashMap<>();
            }
        }
    }

    public void lazyInitBufferResultSet() {
        if (sparqlServiceBufferResultSet == null) {
            if (useBuffer) {
                if (PersistenceService.persistenceAvailable("sparqlServiceBufferResultSet")) {
                    this.sparqlServiceBufferResultSet = (HashMap<String, HashSet<String>>) PersistenceService.retrieve("sparqlServiceBufferResultSet");
                    this.sparqlServiceBufferResultSetInitialCount = sparqlServiceBufferResultSet.size();
                } else {
                    this.sparqlServiceBufferResultSet = new HashMap<>();
                }
            } else {
                this.sparqlServiceBufferResultSet = new HashMap<>();
            }
        }
    }

    public void lazyInitConfidenceBufferClassic() {
        if (sparqlServiceConfidenceBufferClassic == null) {
            if (useBuffer) {
                if (PersistenceService.persistenceAvailable("sparqlServiceConfidenceBufferClassic")) {
                    this.sparqlServiceConfidenceBufferClassic = (HashMap<String, ArrayList<ConceptConfidenceTuple>>) PersistenceService.retrieve("sparqlServiceConfidenceBufferClassic");
                    this.sparqlServiceConfidenceBufferClassicInitialCount = sparqlServiceConfidenceBufferClassic.size();
                } else {
                    this.sparqlServiceConfidenceBufferClassic = new HashMap<>();
                }
            } else {
                this.sparqlServiceConfidenceBufferClassic = new HashMap<>();
            }
        }
    }

    public void lazyInitConfidenceBufferXL() {
        if (sparqlServiceConfidenceBufferXL == null) {
            if (useBuffer) {
                if (PersistenceService.persistenceAvailable("sparqlServiceConfidenceBufferXL")) {
                    this.sparqlServiceConfidenceBufferXL = (HashMap<String, ArrayList<ConceptConfidenceTuple>>) PersistenceService.retrieve("sparqlServiceConfidenceBufferXL");
                    this.sparqlServiceConfidenceBufferXLInitialCount = sparqlServiceConfidenceBufferXL.size();
                } else {
                    this.sparqlServiceConfidenceBufferXL = new HashMap<>();
                }
            } else {
                this.sparqlServiceConfidenceBufferXL = new HashMap<>();
            }
        }
    }

    public void lazyInitNumberBuffer() {
        if (sparqlServiceNumberBuffer == null) {
            if (useBuffer) {
                if (PersistenceService.persistenceAvailable("sparqlServiceNumberBuffer")) {
                    this.sparqlServiceNumberBuffer = (HashMap<String, Integer>) PersistenceService.retrieve("sparqlServiceNumberBuffer");
                    this.sparqlServiceNumberBufferInitialCount = sparqlServiceNumberBuffer.size();
                } else {
                    this.sparqlServiceNumberBuffer = new HashMap<>();
                }
            } else {
                this.sparqlServiceNumberBuffer = new HashMap<>();
            }
        }
    }

    public void lazyInitAskBuffer() {
        if (sparqlServiceAskBuffer == null) {
            if (useBuffer) {
                if (PersistenceService.persistenceAvailable("sparqlServiceAskBuffer")) {
                    this.sparqlServiceAskBuffer = (HashMap<String, Boolean>) PersistenceService.retrieve("sparqlServiceAskBuffer");
                    this.sparqlServiceAskBufferInitialCount = sparqlServiceAskBuffer.size();
                } else {
                    this.sparqlServiceAskBuffer = new HashMap<>();
                }
            } else {
                this.sparqlServiceAskBuffer = new HashMap<>();
            }
        }
    }


    /**
     * Write buffers to disk if buffer has changed.
     */
    public void persist() {
        if(useBuffer) {
            if (sparqlServiceBufferSingleResult != null && !sparqlServiceBufferSingleResult.isEmpty() && sparqlServiceBufferSingleResultInitialCount < sparqlServiceBufferSingleResult.size()) {
                PersistenceService.persist(sparqlServiceBufferSingleResult, "sparqlServiceBufferSingleResult");
            }
            if (sparqlServiceBufferResultSet != null && !sparqlServiceBufferResultSet.isEmpty() && sparqlServiceBufferResultSetInitialCount < sparqlServiceBufferResultSet.size()) {
                PersistenceService.persist(sparqlServiceBufferResultSet, "sparqlServiceBufferResultSet");
            }
            if (sparqlServiceConfidenceBufferClassic != null &&!sparqlServiceConfidenceBufferClassic.isEmpty() && sparqlServiceConfidenceBufferClassicInitialCount < sparqlServiceConfidenceBufferClassic.size()) {
                PersistenceService.persist(sparqlServiceConfidenceBufferClassic, "sparqlServiceConfidenceBufferClassic");
            }
            if (sparqlServiceConfidenceBufferXL != null && !sparqlServiceConfidenceBufferXL.isEmpty() && sparqlServiceConfidenceBufferXLInitialCount < sparqlServiceConfidenceBufferXL.size()) {
                PersistenceService.persist(sparqlServiceConfidenceBufferXL, "sparqlServiceConfidenceBufferXL");
            }
            if(sparqlServiceNumberBuffer != null && !sparqlServiceNumberBuffer.isEmpty() && sparqlServiceNumberBufferInitialCount < sparqlServiceNumberBuffer.size()){
                PersistenceService.persist(sparqlServiceNumberBuffer, "sparqlServiceNumberBuffer");
            }
            if(sparqlServiceAskBuffer != null && !sparqlServiceAskBuffer.isEmpty() && sparqlServiceAskBufferInitialCount < sparqlServiceAskBuffer.size()){
                PersistenceService.persist(sparqlServiceAskBuffer, "sparqlServiceAskBuffer");
            }
        }
    }


    /**
     * Returns and initializes the correct buffer.
     * @param isXLdataSet
     * @return
     */
    private HashMap<String, ArrayList<ConceptConfidenceTuple>> getConfidenceBuffer(boolean isXLdataSet){
        if(isXLdataSet){
            lazyInitConfidenceBufferXL();
            return sparqlServiceConfidenceBufferXL;
        } else {
            lazyInitConfidenceBufferClassic();
            return sparqlServiceConfidenceBufferClassic;
        }
    }


    /**
     * Automatically save buffer after {@code autosaveAfter} queries if autosave is enabled.
     */
    private void autosave(){
        if(autosave) {
            if (queriesSinceLastPersist > autosaveAfter) {
                LOG.debug("Autosaving buffers...");
                persist();
                queriesSinceLastPersist = 0;
            } else {
                queriesSinceLastPersist++;
            }
        }
    }

    private boolean isAutosaveEnabled(){
        return autosave;
    }

    private void enableAutosave(){
        autosave = true;
    }

    private void disableAutosave(){
        autosave = false;
    }

    public int getAutosaveAfter(){
        return this.autosaveAfter;
    }

    public void setAutosaveAfter(int autosaveAfter){
        this.autosaveAfter = autosaveAfter;
    }
}
