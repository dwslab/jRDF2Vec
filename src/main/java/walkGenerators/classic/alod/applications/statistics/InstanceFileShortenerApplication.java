package walkGenerators.classic.alod.applications.statistics;

import walkGenerators.classic.alod.applications.statistics.controller.InstanceFileShortener;

/**
 * This dump shortener accepts the webisalod-instance file (gzipped) and will wirte a compressed version
 * containing only hypernymy relations without any meta data.
 */
public class InstanceFileShortenerApplication {
	
	public static void main(String[] args) {	
		// define the files
		String gzippedInstanceFile = "";
		String outputFileToWrite = "";
		
		// run
		InstanceFileShortener.shortenInstanceFile(gzippedInstanceFile, outputFileToWrite);
	}
}
