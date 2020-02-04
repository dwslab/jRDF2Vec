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
     *
     * @param numberOfThreads The number of threads to be run.
     * @param numberOfWalksPerEntity The number of walks that shall be performed per entity.
     * @param depth The depth of each walk.
     * @param filePathOfFileToBeWritten The path to the file that shall be written.
     */
    void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     *
     * @param numberOfThreads
     * @param numberOfWalksPerEntity
     * @param depth
     * @param filePathOfFileToBeWritten
     */
    void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten);

    /**
     *
     * @param numberOfThreads
     * @param numberOfWalksPerEntity
     * @param depth
     */
    void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth);
}
