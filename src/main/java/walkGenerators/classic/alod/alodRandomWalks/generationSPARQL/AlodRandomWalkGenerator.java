package walkGenerators.classic.alod.alodRandomWalks.generationSPARQL;


import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * This class is able to obtain walks for the ALOD data set.
 * The paths are generated in a random fashion but higher-confidence relations have a higher probability of being drawn.
 *
 * This class serves as reference and is based on Apache Jena and SPARQL. There is a new n-quads file-based in-memory class
 * that can be used which is much faster.
 */
@Deprecated
public class AlodRandomWalkGenerator {

    // constants
    public static final Logger LOG = LoggerFactory.getLogger(AlodRandomWalkGenerator.class);
    public static final String SPARQL_ENDPOINT = "http://webisa.webdatacommons.org/sparql";
    public static final String PREFIX_SKOS = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
    private static final String PREFIX_ISAO = "PREFIX isao: <http://webisa.webdatacommons.org/ontology#>\n";


    boolean useTDB = false;
    private long startTime = 0;
    private int processedEntities = 0;
    private int processedWalks = 0;
    private int fileProcessedLines = 0;
    private String walkQuery = ""; // initialized later
    public String fileName;

    private int depth = 3;
    private int numberOfWalks = 200;


    /**
     * Constructor
     * @param numberOfWalks Number of walks per entity.
     * @param depth Depth of each walk.
     * @param fileName The file to be written.
     */
    public AlodRandomWalkGenerator(int numberOfWalks, int depth, String fileName){
        this.setNumberOfWalks(numberOfWalks);
        this.setDepth(depth);
        this.setFileName(fileName);
    }


    /**
     * File writer which will write all the paths
     */
    public Writer writer;

    /**
     * The RDF model.
     */
    public static Model model;

    /**
     * The jena TDB data set.
     */
    public static Dataset dataset;

    /**
     * Data structure for the entities for which paths will be calculated and persisted.
     */
    HashSet<String> entities;


    /**
     * Generate the walks.
     * @param repoLocation Location of the TDB data set. Data has to be loaded already.
     * @param numberOfThreads Number of threads to be used.
     * @param limit Parameter specifying the number of entities to be processed. Set to a negative number to process
     *              all entities.
     */
    public void generateWalks (String repoLocation, int numberOfThreads, int limit){

        // intialize the writer
        try {
            writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(fileName, false)), "utf-8");
        } catch (Exception e1) {
            LOG.error("Could not initialize writer.");
            e1.printStackTrace();
        }

        walkQuery = generateQuery(depth, numberOfWalks);


        if(useTDB) {
            dataset = TDBFactory.createDataset(repoLocation);
        }

        if(entities == null || entities.size() == 0) {
            entities = getAllEntities(limit);
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<Runnable>(
                        entities.size()));

        startTime = System.currentTimeMillis();
        for (String entity : entities) {
            EntityProcessingThread th = new EntityProcessingThread(entity);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOG.error("Interrupted Exception");
            e.printStackTrace();
        }
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOG.error("IO Exception");
            e.printStackTrace();
        }
    }


    /**
     * Generate the query to obtain a random broader concept.
     * Note: The probability is higher for high-confidence hypernyms.
     * @param concept The concept for which a broader concept shall be retrieved.
     * @return URI of the concept.
     */
    public String getRandomBroaderConcept(String concept){
        String queryString = generateQueryForRandomBroaderConcept(concept);
        Query query = QueryFactory.create(queryString);
        QueryExecution qe;
        if(useTDB) {
            dataset.begin(ReadWrite.READ);
            qe = QueryExecutionFactory.create(query, dataset);
        } else {
            qe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        }
        ResultSet resultsTmp = qe.execSelect();
        String result = "";
        if(resultsTmp.hasNext()){
            QuerySolution solution = resultsTmp.nextSolution();
            result =  solution.get("e").toString();
        }
        qe.close();
        if(useTDB) {
            dataset.end();
        }
        return result;
    }


    /**
     * Generate the query to obtain a random broader concept.
     * Note: The probability is greater for high-confidence hypernyms.
     * @param concept: The concept IRI for which a broader concept shall be retrieved.
     * @return Query in String representation.
     */
    public String generateQueryForRandomBroaderConcept(String concept){
        return PREFIX_SKOS + PREFIX_ISAO +
                "SELECT ?e WHERE\n" +
                "{ GRAPH ?g {<" + concept + "> skos:broader ?e .}\n" +
                "?g isao:hasConfidence ?c .\n" +
                "BIND(RAND()*?c AS ?rank)} ORDER BY ?rank LIMIT 1";
    }


    /**
     * Generates the query with the given depth.
     * @param depth Path depth.
     * @param numberWalks The number of walks.
     * @return Query in String representation.
     */
    public String generateQuery(int depth, int numberWalks) {
        String selectPart = PREFIX_SKOS + PREFIX_ISAO + "SELECT ?o1";
        String mainPart = "{ GRAPH ?g { $ENTITY$ skos:broader ?o1 }\n"
                +"?g isao:hasConfidence ?c . \n";
        String confidenceRandPart = "*?c";
        String query = "";
        int lastO = 1;
        for (int i = 1; i < depth; i++) {
            mainPart += "GRAPH ?g" + i + " { ?o" + i + " skos:broader " + "?o" + (i + 1) + " }\n"
                        + "?g" + i + " isao:hasConfidence " + "?c" + i + " .\n";
            confidenceRandPart += "*?c" + i;
            selectPart += " ?o" + (i + 1);
            lastO = i + 1;
        }
        String lastOS = "?o" + lastO;
        query = selectPart + " WHERE\n" + mainPart + "FILTER(!isLiteral("
                + lastOS
                + ")).\nBIND(RAND()" + confidenceRandPart + " AS ?sortKey) } ORDER BY ?sortKey LIMIT "
                + numberWalks;
        return query;
    }


    /*
    * Executor thread. One thread will handle the task of building the tasks for one entity which is specified
    * in the thread constructor.
     */
    private class EntityProcessingThread implements Runnable{

        private String entity;
        private List<String> finalList;

        /**
         * Constructor.
         * @param entity The entity this particular thread shall handle.
         */
        public EntityProcessingThread(String entity) {
            this.entity = entity;
            finalList = new ArrayList<String>();
        }

        @Override
        public void run() {
            processEntity();
            writeToFile(finalList);
        }

        /**
         * Processing a single entity, i.e. deriving their paths.
         */
        private void processEntity(){
            for(int j = 0; j < numberOfWalks; j++) {
                String path = entity;
                String nextEntity = null;
                for (int i = 0; i < depth; i++) {
                    if (nextEntity == null){
                        nextEntity = getRandomBroaderConcept(entity);
                    } else {
                        nextEntity = getRandomBroaderConcept(nextEntity);
                    }
                    path = path + " " + nextEntity;
                }
                finalList.add(path.replace("http://webisa.webdatacommons.org/concept/","isa:"));
            }
        }

        /**
         * Processing a single entity, i.e. deriving their paths.
         * This method is deprecated as the performance is much worse than the new implementation.
         */
        @Deprecated
        private void processEntityDeprecated() {
            // get all the walks
            String queryStr = walkQuery.replace("$ENTITY$", "<" + entity + ">");
            executeQuery(queryStr, finalList);

            // get all the direct properties
            //queryStr = directPropsQuery.replace("$ENTITY$", "<" + entity + ">");
            //executeQuery(queryStr, finalList);
        }

        /**
         * Execute the Query to get the path.
         * @param queryStr
         * @param walkList
         */
        @Deprecated
        private void executeQuery(String queryStr, List<String> walkList) {
            Query query = QueryFactory.create(queryStr);
            QueryExecution qe;
            if(useTDB) {
                dataset.begin(ReadWrite.READ);
                qe = QueryExecutionFactory.create(query, dataset);
            } else {
                qe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
            }
            ResultSet resultsTmp = qe.execSelect();
            String entityShort = entity.replace("http://webisa.webdatacommons.org/concept/",
                    "isa:");
            ResultSet results = ResultSetFactory.copyResults(resultsTmp);
            qe.close();
            if(useTDB) {
                dataset.end();
            }
            while (results.hasNext()) {
                QuerySolution result = results.next();
                String singleWalk = entityShort + " ";
                // construct the walk from each node or property on the path
                for (String var : results.getResultVars()) { // comment jan: get the variable names for the projectoin
                    try {
                        // clean it if it is a literal
                        if (result.get(var) != null
                                && result.get(var).isLiteral()) {
                            String val = result.getLiteral(var).toString();
                            val = val
                                    .replace("\n", " ")
                                    .replace("\t", " ")
                                    .replace(" ", "_"); // replace " " because used as separator
                            singleWalk += val + " ";
                        } else if (result.get(var) != null) {
                            //result is not null and not a literal
                            singleWalk += result
                                    .get(var)
                                    .toString()
                                    .replace("http://webisa.webdatacommons.org/concept/",
                                            "isa:")
                                    + " ";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                walkList.add(singleWalk.substring(0, singleWalk.length() - 1)); // comment removing the last arrow
            } // (loop over result set)
        } // (executeQuery method)
    } // (thread class)


    /**
     * Adds new walks to the list; If the list is filled it is written to the
     * file.
     *
     * @param tmpList
     */
    private synchronized void writeToFile(List<String> tmpList) {
        processedEntities++;
        processedWalks += tmpList.size();
        fileProcessedLines += tmpList.size();
        for (String str : tmpList)
            try {
                writer.write(str + "\n");
            } catch (IOException e) {
                LOG.error("Error occurred while writing, process will continue.");
                e.printStackTrace();
            }
        if (processedEntities % 100 == 0) {
            System.out
                    .println("TOTAL PROCESSED ENTITIES: " + processedEntities);
            System.out.println("TOTAL NUMBER OF WALKS : " + processedWalks);
            System.out.println("TOTAL TIME:"
                    + ((System.currentTimeMillis() - startTime) / 1000));
        }
        // flush the file
        if (fileProcessedLines > 3000000) {
            fileProcessedLines = 0;
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int tmpNM = (processedWalks / 3000000);
            String tmpFilename = fileName.replace(".txt", tmpNM + ".txt");
            try {
                writer = new OutputStreamWriter(new GZIPOutputStream(
                        new FileOutputStream(tmpFilename, false)), "utf-8");
            } catch (Exception e) {
                LOG.error("An exception occurred while writing the file.");
                e.printStackTrace();
            }
        }
    }


    /**
     * Return the number of entities specified in {@code limit}.
     * @param limit The number of entities that shall be retrieved. Set to a negative number if you want to retrieve
     *              all entities.
     * @return The entities in String representation in a set.
     */
    public HashSet<String> getAllEntities(int limit){
        HashSet<String> resultSet = new HashSet<>();

        String queryString;
        if(limit > 0){
            queryString = PREFIX_SKOS + "SELECT DISTINCT ?entity WHERE {GRAPH ?g {?entity skos:broader ?f } } " +
                    "LIMIT " + limit;
        } else {
            queryString = PREFIX_SKOS + "SELECT DISTINCT ?entity WHERE {GRAPH ?g {?entity skos:broader ?f } }";
        }

        Query query = QueryFactory.create(queryString);

        QueryExecution qe;
        if(useTDB) {
            qe = QueryExecutionFactory.create(query, dataset);
        } else {
            qe = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query);
        }

        ResultSet results = qe.execSelect();
        while(results.hasNext()){
            String result = results.next().get("entity").toString();
            resultSet.add(result);
        }
        qe.close();
        System.out.println("Number of entities: " + resultSet.size());
        return resultSet;
    }



    //-------------------------------------------------------------------------------------
    // Getters and Setters
    //-------------------------------------------------------------------------------------


    public HashSet<String> getEntities() {
        return entities;
    }

    public void setEntities(HashSet<String> entities) {
        this.entities = entities;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = Math.max(depth, 1);
    }

    public int getNumberOfWalks() {
        return numberOfWalks;
    }

    public void setNumberOfWalks(int numberOfWalks) {
        this.numberOfWalks = Math.max(numberOfWalks, 1);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isUseTDB() {
        return useTDB;
    }

    public void setUseTDB(boolean useTDB) {
        this.useTDB = useTDB;
    }

    /**
     * Set the TDB data set by providing a path. Creation will happen in the background.
     * @param pathToDataset Path to the data set.
     */
    public static void setDataset(String pathToDataset) {
        dataset = TDBFactory.createDataset(pathToDataset);
    }

}
