package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base;

import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.walk_generators.IWalkGenerator;

import java.io.File;
import java.util.List;

/**
 * Interface for all walk generation managers.
 */
public interface IWalkGenerationManager {


    /**
     * Generate walks according to the stated method.
     * @param generationMode The algorithm to be used for the walk generation.
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk. Depth 1 leads to three elements in the walk, depth 2 leads to 5 elements in
     *              the walk.
     * @param textWalkLength Length of text walks. Note that unlike depth, here only the number of tokens in the walk
     *                       are counted (not node hops!). Typically the window size would be used as text walk length.
     * @param walkDirectory  The walk directory (directory where the walks shall be generated).
     */
     void generateWalks(WalkGenerationMode generationMode, int numberOfThreads, int numberOfWalksPerEntity, int depth,
             int textWalkLength, File walkDirectory);

    /**
     * The walk file(s) will be persisted in "./walks".
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     */
    void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     * Generates random walks.
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     * @param walkDirectory  The walk directory (directory where the walks shall be generated).
     */
    void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, File walkDirectory);

    /**
     * Generates duplicate free random walks.
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     * @param walkDirectory  The walk directory (directory where the walks shall be generated).
     */
    void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, File walkDirectory);

    /**
     * Generates duplicate free random walks.
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     */
    void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     * Generates mid walks, duplicate walks are possible.
     * A mid walk is a random walk that involves a given entity but may not start or end with the entity in question.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalksPerEntity The maximal number of walks that shall be performed per entity.
     * @param depth The depth of each walk where the depth is the number of node-hops, i.e. depth 1 leads to a sentence with one hop and three elements: S → P → O.
     */
    void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     * Generates mid walks, duplicate walks are possible.
     * A mid walk is a random walk that involves a given entity but may not start or end with the entity in question.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalksPerEntity The maximal number of walks that shall be performed per entity.
     * @param depth The depth of each walk where the depth is the number of node-hops, i.e. depth 1 leads to a sentence with one hop and three elements: S → P → O.
     * @param walkDirectory  The walk directory (directory where the walks shall be generated).
     */
    void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, File walkDirectory);

    /**
     * Generates weighted mid walks, duplicate walks are possible.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalksPerEntity The maximal number of walks that shall be performed per entity.
     * @param depth The depth of each walk where the depth is the number of node-hops, i.e. depth 1 leads to a sentence with one hop and three elements: S → P → O.
     */
    void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     * Generates weighted mid walks, duplicate walks are possible.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalksPerEntity The maximal number of walks that shall be performed per entity.
     * @param depth The depth of each walk where the depth is the number of node-hops, i.e. depth 1 leads to a sentence with one hop and three elements: S → P → O.
     * @param walkDirectory  The walk directory (directory where the walks shall be generated).
     */
    void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, File walkDirectory);

    /**
     * Generates mid walks without duplicates.
     * A mid walk is a random walk that involves a given entity but may not start or end with the entity in question.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalksPerEntity The maximum number of walks that shall be performed per entity. Note that the actual number of generated walks might be lower.
     * @param depth The depth of each walk where the depth is the number of node-hops, i.e. depth 1 leads to a sentence with one hop and three elements: S → P → O.
     */
    void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth);

    /**
     * Generates mid walks without duplicates.
     * A mid walk is a random walk that involves a given entity but may not start or end with the entity in question.
     * @param numberOfThreads The number of threads to be used.
     * @param numberOfWalksPerEntity The maximum number of walks that shall be performed per entity. Note that the actual number of generated walks might be lower.
     * @param depth The depth of each walk where the depth is the number of node-hops, i.e. depth 1 leads to a sentence with one hop and three elements: S → P → O.
     * @param walkDirectory  The walk directory (directory where the walks shall be generated).
     */
    void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, File walkDirectory);

    /**
     * Generate text walks.
     * @param numberOfThreads he number of threads to be used.
     * @param walkLength The length of the walks (!= hops). An example for a walk of length 3 would be "s → p → o"
     */
    void generateTextWalks(int numberOfThreads, int walkLength);

    /**
     * Generate text walks.
     * @param numberOfThreads he number of threads to be used.
     * @param walkLength The length of the walks (!= hops). An example for a walk of length 3 would be "s → p → o"
     * @param walkDirectory  The walk directory (directory where the walks shall be generated).
     */
    void generateTextWalks(int numberOfThreads, int walkLength, File walkDirectory);

    IWalkGenerator getWalkGenerator();

    void writeToFile(List<String> tmpList);

    String shortenUri(String uri);
}
