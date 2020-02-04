package walkGenerators.classic.alod.applications.statistics.controller;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import walkGenerators.classic.alod.applications.statistics.model.DistributionPoint;
import walkGenerators.classic.alod.applications.statistics.model.RelationCounter;


/**
 * Distribution Calculator: Given a shortened file, the degree distribution is calculated.
 */
public class RelationsDistributionCalcualtor {

	/**
	 * Calculate a distribution of node degrees given a prepared file and persist the distribution as well as the distribution points.
	 * @param pathToTabSeparatedRelationsFile This file is obtained by running the {@code InstanceFileShortenerApplication}. 
	 * @param pathToOutputFilePlainCounts Path to the plain count file that is to be written. Plain counts (in-, out- degree) are written per concept to a file. 
	 * This aggregation allows to check the degree of one particular concept e.g. by using grep.
	 * @param pathToOutputFileDistribution Path to the distribution file that is to be written. This distribution can, for example, be imported in Excel and be further
	 * processed there.
	 */
    public static void createDistribution(String pathToTabSeparatedRelationsFile, String pathToOutputFilePlainCounts, String pathToOutputFileDistribution) {

        BufferedReader reader = null;
        BufferedWriter writerPlainCounts, writerDistribution = null;
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(pathToTabSeparatedRelationsFile));
            reader = new BufferedReader(new InputStreamReader(gzip));
            writerPlainCounts = new BufferedWriter(new FileWriter(new File(pathToOutputFilePlainCounts)));
            writerDistribution = new BufferedWriter(new FileWriter(new File(pathToOutputFileDistribution)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("ABORT");
            return;
        } catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("ABORT");
            return;
        }

        if(reader == null || writerPlainCounts == null){
            System.out.println("Problem initializing Reader/Writer. ABORT.");
            return;
        }

        // classic.statistics
        long lineCount = 0;

        // init data structures
        HashMap<String, RelationCounter> aggregatedDataSet = new HashMap<>(200000000);
        HashMap<Integer, Integer> distribution = new HashMap<>();

        String readLine;
        String components[];
        System.out.println("Starting loop over optimized n-quads file.");
        try {
            while ((readLine = reader.readLine()) != null) {
                components = readLine.split("\t");
                if(components.length < 2){
                    System.out.println("Error parsing line (" + readLine +"). Continue.");
                    continue;
                }
                if(!aggregatedDataSet.containsKey(components[0])){
                    aggregatedDataSet.put(components[0], new RelationCounter());
                }
                if(!aggregatedDataSet.containsKey(components[1])){
                    aggregatedDataSet.put(components[1], new RelationCounter());
                }
                aggregatedDataSet.get(components[0]).isHyponymCount++;
                aggregatedDataSet.get(components[1]).isHypernymCount++;
                lineCount++;
                if(lineCount % 1000000 == 0){
                    System.out.println(lineCount);
                }
            }
            reader.close();
        } catch (IOException ioe){
            System.out.println("Error while reading optimized nquads file. ABORT");
            ioe.printStackTrace();
            return;
        }
        System.out.println("Loop over n-quads file completed.");


        // write plain counts to file
        System.out.println("Writing plain counts file.");
        try {
            writerPlainCounts.write("Concept\tHyponym Count\tHypernym Count\tSum\n");
            for (Map.Entry<String, RelationCounter> entry : aggregatedDataSet.entrySet()) {
                writerPlainCounts.write(entry.getKey() + " " + entry.getValue().isHyponymCount + " " + entry.getValue().isHypernymCount + "\t" + entry.getValue().getRelationCount() + "\n");
            }
            writerPlainCounts.flush();
            writerPlainCounts.close();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        System.out.println("Writing plain counts file completed.");

        int count = 0;
        int frequency = 0;

        // create distribution
        System.out.println("Create distribution.");
        for(Map.Entry<String, RelationCounter> entry : aggregatedDataSet.entrySet()){
            count = entry.getValue().getRelationCount();
            if(!distribution.containsKey(count)){
                distribution.put(count, 1);
            } else {
                frequency = distribution.get(count) + 1;
                distribution.put(count, frequency);
            }
        }

        ArrayList<DistributionPoint> sortedDistribution = new ArrayList<>();

        for(Map.Entry<Integer, Integer> entry : distribution.entrySet()){
            sortedDistribution.add(new DistributionPoint(entry.getKey(), entry.getValue()));
        }
        System.out.println("Distribution created.");

        System.out.println("Sort distribution.");
        Collections.sort(sortedDistribution);
        long totalFrequency = 0;

        System.out.println("\n\n\n");

        try {
            for (DistributionPoint point : sortedDistribution) {
                System.out.println(point.relationCount + "       " + point.frequency);
                writerDistribution.write(point.relationCount + "\t" + point.frequency + "\n");
                totalFrequency = point.frequency;
            }
            writerDistribution.flush();
            writerDistribution.close();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        System.out.println("\n\n\nTOTAL RELATION FREQUENCY: " + totalFrequency);
    }
}
