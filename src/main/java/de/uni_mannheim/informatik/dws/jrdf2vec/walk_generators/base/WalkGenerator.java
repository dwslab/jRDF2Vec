package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.parsers.IParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.runnables.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.zip.GZIPOutputStream;

/**
 * Abstract class for all Walk generators.
 */
public abstract class WalkGenerator implements IWalkGenerator {

    /**
     * The walk file(s) will be persisted in "./walks".
     *
     * @param numberOfThreads        The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth                  The depth of each walk.
     */
    public abstract void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     * @param numberOfThreads           The number of threads to be run.
     * @param numberOfWalksPerEntity    The number of walks that shall be performed per entity.
     * @param depth                     The depth of each walk.
     * @param filePathOfFileToBeWritten The path to the file that shall be written.
     */
    public abstract void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     * @param numberOfThreads           The number of threads to be run.
     * @param numberOfWalksPerEntity    The number of walks that shall be performed per entity.
     * @param depth                     The depth of each walk.
     * @param filePathOfFileToBeWritten The path to the file that shall be written.
     */
    public abstract void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     * @param numberOfThreads        The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth                  The depth of each walk.
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
    public IParser parser;

    /**
     * File writer for all the paths.
     */
    public Writer writer;

    /**
     * File path to the walk file to be written.
     */
    public String filePath;

    /**
     * Given a URI, a short version is created.
     *
     * @param uri The uri to be transformed.
     * @return Shortened version of the URI.
     */
    public abstract String shortenUri(String uri);

    /**
     * Gets the function to be used to shorten URIs.
     * @return Function to shorten URIs (String -&gt; String).
     */
    public abstract UnaryOperator<String> getUriShortenerFunction();

    /**
     * Generate walks for the entities that are duplicate free (i.e., no walk exists twice in the resulting file).
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore). Aka depth.
     */
    public void generateRandomMidWalksForEntitiesDuplicateFree(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), StandardCharsets.UTF_8);
        } catch (Exception e1) {
            LOGGER.error("Could not initialize writer. Aborting process.", e1);
            return;
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            DuplicateFreeMidWalkEntityProcessingRunnable th = new DuplicateFreeMidWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
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
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore). Aka depth.
     */
    public void generateWeightedMidWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), StandardCharsets.UTF_8);
        } catch (Exception e1) {
            LOGGER.error("Could not initialize writer. Aborting process.", e1);
            return;
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            WeightedMidWalkEntityProcessingRunnable th = new WeightedMidWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
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
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore). Aka depth.
     */
    public void generateRandomMidWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), StandardCharsets.UTF_8);
        } catch (Exception e1) {
            LOGGER.error("Could not initialize writer. Aborting process.", e1);
            return;
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads,
                0, TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(entities.size()));

        for (String entity : entities) {
            MidWalkEntityProcessingRunnable th = new MidWalkEntityProcessingRunnable(this, entity, numberOfWalks, walkLength);
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
     * Generate walks for the entities that are free of duplicates (i.e., no walk exists twice in the resulting file).
     *
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore).
     */
    public void generateDuplicateFreeWalksForEntities(Set<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), StandardCharsets.UTF_8);
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
     * @param entities        The entities for which walks shall be generated.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalks   The number of walks to be generated per thread.
     * @param walkLength      The maximal length of each walk (a walk may be shorter if it cannot be continued anymore).
     */
    public void generateWalksForEntities(HashSet<String> entities, int numberOfThreads, int numberOfWalks, int walkLength) {
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();

        // initialize the writer
        try {
            this.writer = new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outputFile, false)), StandardCharsets.UTF_8);
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
     * Adds new walks to the list; If the list is filled, it is written to the
     * file.
     *
     * @param tmpList Entries that shall be written.
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
        if (processedEntities % 1000 == 0) {
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
                        new FileOutputStream(tmpFilename, false)), StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close resources.
     */
    public void close() {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            LOGGER.error("There was an error when closing the writer.", ioe);
        }
    }
}
