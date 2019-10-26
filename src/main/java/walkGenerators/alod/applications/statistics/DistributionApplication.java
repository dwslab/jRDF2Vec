package walkGenerators.alod.applications.statistics;

import walkGenerators.alod.applications.statistics.controller.RelationsDistributionCalcualtor;

/**
 * Create a Node-Degree Distribution for the ALOD data set.
 */
public class DistributionApplication {

	public static void main(String[] args) {
		// this file MUST exist. You obtain it by running the InstanceFileShortenerApplication
		String pathToTabSeparatedRelationsFile = "";
		
		// this file does not yet exist and will be written
		String pathToOutputFilePlainCounts = "";
		
		// this file does not yet exist and will be written
		String pathToOutputFileDistribution = "";
		
		RelationsDistributionCalcualtor.createDistribution(pathToTabSeparatedRelationsFile, pathToOutputFilePlainCounts, pathToOutputFileDistribution);
	}
	
}
