package walkGenerators.base;

/**
 * Interface for all walk generators
 */
public interface IWalkGenerator {

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
     * @param filePathOfFileToBeWritten The path to the file that shall be written.
     */
    void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     * Generates duplicate free random walks.
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     * @param filePathOfFileToBeWritten The path to the file that shall be written.
     */
    void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     * Generates duplicate free random walks.
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     */
    void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth);
}
