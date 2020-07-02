package de.uni_mannheim.informatik.dws.jrdf2vec.walkGenerators.base;

/**
 * A non-functional walk generator.
 */
public class DummyWalkGenerator extends WalkGenerator {

    @Override
    public void generateWalks(WalkGenerationMode generationMode, int numberOfThreads, int numberOfWalks, int depth, String walkFile) {

    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {

    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {

    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {

    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {

    }

    @Override
    public String shortenUri(String uri) {
        return uri;
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {

    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {

    }

    @Override
    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {

    }

    @Override
    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {

    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {

    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {

    }
}
