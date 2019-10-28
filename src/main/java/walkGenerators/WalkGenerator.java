package walkGenerators;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * Abstract class for all Walk generators.
 */
public abstract class WalkGenerator {

    /**
     * The walk file(s) will be persisted in "./walks".
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     */
    public abstract void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     *
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     * @param filePathOfFileToBeWritten The path to the file that shall be written.
     */
    public abstract void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     *
     * @param numberOfThreads
     * @param numberOfWalksPerEntity
     * @param depth
     * @param filePathOfFileToBeWritten
     */
    public abstract void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     *
     * @param numberOfThreads
     * @param numberOfWalksPerEntity
     * @param depth
     */
    public abstract void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     * Default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WalkGenerator.class);

    /**
     * For the statistical output.
     */
    int processedEntities = 0;

    /**
     * For the statistical output.
     */
    int processedWalks = 0;

    /**
     * For the statistical output.
     */
    int fileProcessedLines = 0;

    /**
     * Parser.
     */
    NtParser parser;

    /**
     * File writer for all the paths.
     */
    Writer writer;

    /**
     * File path to the walk file to be written.
     */
    String filePath;

    /**
     * Given a URI, a short version is created.
     * @param uri The uri to be transformed.
     * @return Shortened version of the URI.
     */
    public abstract String shortenUri(String uri);


    /**
     *
     * @param entities
     * @param numberOfThreads
     * @param numberOfWalks
     * @param walkLength
     */
    public void generateDuplicateFreeWalksForEntities(HashSet<String> entities, int numberOfThreads, int numberOfWalks, int walkLength){
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), "utf-8");
        } catch (Exception e1) {
            LOGGER.error("Could not initialize writer. Aborting process.", e1);
            return;
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            DuplicateFreeWalkEntityProcessingRunnable th = new DuplicateFreeWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        this.close();
    }

    /**
     * Generate walks for the entities.
     *
     * @param entities
     * @param numberOfThreads
     * @param numberOfWalks
     * @param walkLength
     */
    public void generateWalksForEntities(HashSet<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), "utf-8");
        } catch (Exception e1) {
            LOGGER.error("Could not initialize writer. Aborting process.", e1);
            return;
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            RandomWalkEntityProcessingRunnable th = new RandomWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
            pool.execute(th);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted Exception");
            e.printStackTrace();
        }
        this.close();
    }

    /**
     * Adds new walks to the list; If the list is filled it is written to the
     * file.
     *
     * @param tmpList
     */
    public synchronized void writeToFile(List<String> tmpList) {
        processedEntities++;
        processedWalks += tmpList.size();
        fileProcessedLines += tmpList.size();
        for (String str : tmpList)
            try {
                writer.write(str + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (processedEntities % 100 == 0) {
            LOGGER.info("TOTAL PROCESSED ENTITIES: " + processedEntities);
            LOGGER.info("TOTAL NUMBER OF PATHS : " + processedWalks);
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
            String tmpFilename = filePath.replace(".gz", tmpNM + ".gz");
            try {
                writer = new OutputStreamWriter(new GZIPOutputStream(
                        new FileOutputStream(tmpFilename, false)), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Reads a HashSet from the file as specified by the file path.
     *
     * @param filePath
     * @return
     */
    static HashSet<String> readHashSetFromFile(String filePath) {
        return readHashSetFromFile(new File(filePath));
    }

    /**
     * Reads a HashSet from the file as specified by the file.
     *
     * @param file
     * @return
     */
    static HashSet<String> readHashSetFromFile(File file) {
        HashSet<String> result = new HashSet<>();
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return result;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found.", e);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("IOException occured.", e);
            e.printStackTrace();
        }
        LOGGER.info("Entities read into cache.");
        return result;
    }


    /**
     * Writes the given HashSet to a file.
     *
     * @param filePath
     * @param setToWrite
     */
    static void writeHashSetToFile(String filePath, HashSet<String> setToWrite) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)));
            boolean isFirstLine = true;
            for (String entry : setToWrite) {
                if (isFirstLine) isFirstLine = false;
                else writer.write("\n");
                if(entry != null) writer.write(entry);
                else LOGGER.error("Empty entry in set to write.");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("There was a problem writing the file.");
        }
    }


    /**
     * Reads an ontology from a given URL.
     *
     * @param path of ontology to be read.
     * @return Model instance.
     */
    static OntModel readOntology(String path, String language) throws MalformedURLException {
        URL url = new File(path).toURI().toURL();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        model.read(url.toString(), language);
        return model;
    }


    /**
     * Close resources.
     */
    public void close(){
        if(writer == null) return;
        try {
            writer.flush();
            writer.close();
        } catch (IOException ioe){
            LOGGER.error("There was an error when closing the writer.", ioe);
        }
    }

}
