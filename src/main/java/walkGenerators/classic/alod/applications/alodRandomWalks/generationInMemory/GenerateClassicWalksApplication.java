package walkGenerators.classic.alod.applications.alodRandomWalks.generationInMemory;

import walkGenerators.classic.alod.applications.alodRandomWalks.generationInMemory.controller.WalkGeneratorClassicWalks;

/**
 * Applications in order to generate walks.
 */
public class GenerateClassicWalksApplication {

    public static void main(String[] args) {
		//String pathToInstanceFile = "C:\\Users\\D060249\\Documents\\Alod\\webisalod-instances.nq.gz";
		String pathToOptimizedFile = "./optimized_alod_classic.gz";

		String pathToWalkFile = "./walks/100_4_no_duplicates/alod_classic_100_4.gz"; // name of file for walks that are generated
		int numberOfWalks = 100; // number of walks per entity
		int depth = 4; // depth of a sentence
		int numberOfThreads = 60; // number of threads
    	
        WalkGeneratorClassicWalks generator = new WalkGeneratorClassicWalks();
        //generator.loadFromNquadsFile(pathToInstanceFile, pathToOptimizedFile);
        generator.loadFromOptimizedFile(pathToOptimizedFile);
        generator.generateRandomWalksDuplicateFree(numberOfThreads, numberOfWalks, depth, pathToWalkFile);
    }
}
