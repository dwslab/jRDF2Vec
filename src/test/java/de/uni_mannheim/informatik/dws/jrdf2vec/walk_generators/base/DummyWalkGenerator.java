package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base;

public class DummyWalkGenerator extends WalkGenerator {

    @Override
    public void generateWalks(WalkGenerationMode generationMode, int numberOfThreads, int numberOfWalks, int depth, String walkFile) {
        // do nothing
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        // do nothing
    }

    @Override
    public void generateRandomWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        // do nothing
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        // do nothing
    }

    @Override
    public void generateRandomWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        // do nothing
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        // do nothing
    }

    @Override
    public void generateRandomMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        // do nothing
    }

    @Override
    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth) {

    }

    @Override
    public void generateWeightedMidWalks(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {

    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth) {
        // do nothing
    }

    @Override
    public void generateRandomMidWalksDuplicateFree(int numberOfThreads, int numberOfWalksPerEntity, int depth, String filePathOfFileToBeWritten) {
        // do nothing
    }

    @Override
    public String shortenUri(String uri) {
        return uri;
    }
}