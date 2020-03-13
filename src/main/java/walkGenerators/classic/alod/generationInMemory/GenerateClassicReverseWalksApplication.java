package walkGenerators.classic.alod.generationInMemory;

import walkGenerators.classic.alod.generationInMemory.controller.ReverseWalkGenerator;


/**
 * Generates reverse walks for the classic data set.
 */
public class GenerateClassicReverseWalksApplication {

    public static void main(String[] args) {
		String pathToOptimizedFile = ""; // you have to run the regular reverse walk generator before

		String pathToWalkFile = ""; // name of file for walks that are generated
		int numberOfWalks = 100; // number of walks per entity
		int depth = 8; // depth of a sentence
		int numberOfThreads = 80; // number of threads
    	
		ReverseWalkGenerator generator = new ReverseWalkGenerator();
        generator.loadFromOptimizedFile(pathToOptimizedFile);
        generator.generateWalks(pathToWalkFile, numberOfWalks, depth, numberOfThreads);
    }
}
