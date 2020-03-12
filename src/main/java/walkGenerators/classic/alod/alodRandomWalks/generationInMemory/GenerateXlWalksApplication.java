package walkGenerators.classic.alod.alodRandomWalks.generationInMemory;

import walkGenerators.classic.alod.alodRandomWalks.generationInMemory.controller.WalkGeneratorXlWalks;

/**
 * Application to generate regular XL walks.
 */
public class GenerateXlWalksApplication {

	public static void main(String[] args) {

		String pathToInstanceFile = ""; // or 
		String pathToOptimizedFile = ""; 

		String pathToWalkFile = ""; // name of file for walks that are generated
		int numberOfWalks = 100; // number of walks per entity
		int depth = 8; // depth of a sentence
		int numberOfThreads = 80; // number of threads

		WalkGeneratorXlWalks generator = new WalkGeneratorXlWalks();
		generator.loadFromNquadsFile(pathToInstanceFile, pathToOptimizedFile);
		// or
		// generator.loadFromOptimizedFile("");
		generator.generateWalks(pathToWalkFile, numberOfWalks, depth, numberOfThreads);
	}

}
