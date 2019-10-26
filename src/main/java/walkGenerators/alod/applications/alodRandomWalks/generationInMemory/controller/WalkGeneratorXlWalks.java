package walkGenerators.alod.applications.alodRandomWalks.generationInMemory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Walk generator for XL data set.
 */
public class WalkGeneratorXlWalks {

	private static Logger LOG = LoggerFactory.getLogger(WalkGeneratorXlWalks.class);
	private HashMap<String, ArrayList<String>> broaderConcepts = new HashMap<String, ArrayList<String>>(); 
	private Random rand = new Random();
	private OutputStreamWriter walkWriter;
	private String nameOfWalkFile;
	private int fileNumber = 0; // used to decide when to start a new file

	// classic.statistics
	private long startTime;
	private int processedEntities = 0;
	private int processedWalks = 0;
	private int newFileCounter = 0;
	private static URLDecoder urlDecoder = new URLDecoder();

	/**
	 * Load from a storage optimized file that was previously written by method
	 * {@code loadFromNquadsFile}.
	 * 
	 * @param pathToFile Path to the file that shall be read.
	 */
	public void loadFromOptimizedFile(String pathToFile) {
		LOG.info("Loading optimized file.");
		File f = new File(pathToFile);
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
			String readLine = "";
			while ((readLine = reader.readLine()) != null) {
				String[] components = readLine.split("\t");
				// components has 3 elements:
				// 0) Concept 1
				// 1) Hypernym of Concept 1
				if (!broaderConcepts.containsKey(components[0])) {
					broaderConcepts.put(components[0], new ArrayList());
				}
				broaderConcepts.get(components[0]).add(components[1]);
			}
			LOG.info("Load completed.");
			reader.close();
		} catch (FileNotFoundException fnfe) {
			LOG.error("File could not be found. ABORT.");
			fnfe.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		LOG.info("Number of entities loaded: " + broaderConcepts.size());
	}

	public void loadFromNquadsFile(String pathToInstanceFile, String pathToOutputFile) {
		try {
			GZIPInputStream gzipInput = new GZIPInputStream(new FileInputStream(pathToInstanceFile));
			BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInput));
			GZIPOutputStream gzipOutput = new GZIPOutputStream(new FileOutputStream(new File(pathToOutputFile)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzipOutput));

			String readLine;
			long lineNumber = 0;

			String conceptRegexPattern = "(?<=\\/concept\\/).*?(?=> )"; // (?<=\/concept\/).*?(?=> )
			Pattern conceptPattern = Pattern.compile(conceptRegexPattern);
			String concept1;
			String concept2;
			lineNumber = 0;
			while ((readLine = reader.readLine()) != null) {
				if (readLine.contains("core#broader>")) {
					Matcher conceptMatcher = conceptPattern.matcher(readLine);
					conceptMatcher.find();
					concept1 = cleanConcept(conceptMatcher.group());
					conceptMatcher.find();
					concept2 = cleanConcept(conceptMatcher.group());

					if (broaderConcepts.get(concept1) == null) {
						broaderConcepts.put(concept1, new ArrayList<String>());
					}
					broaderConcepts.get(concept1).add(concept2);
					writer.write(concept1 + "\t" + concept2 + "\n");
					lineNumber++;
					if (lineNumber % 1000000 == 0) {
						System.out.println(lineNumber);
					}
				}
			}
			// close resources
			reader.close();
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ABORT");
			return;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("ABORT");
			return;
		} 
	}

	/**
	 * Writes all entities to the given file. One Entity per line. This method
	 * assumes that the ALOD data set has been loaded into memory.
	 * 
	 * @param pathToFileToWrite Path to the file that shall be written.
	 */
	public void saveEntitiesToFile(String pathToFileToWrite) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathToFileToWrite)));
			Iterator iterator = broaderConcepts.entrySet().iterator();
			while (iterator.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) iterator.next();
				writer.write(entry.getKey().toString() + "\n");
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * This method assumes that the ALOD data set has been loaded into memory.
	 * 
	 * @param walkOutputFileName Name of the files that contain the walks.
	 * @param numberOfWalks      The number of walks that shall be generated per
	 *                           entity.
	 * @param depth              The depth of each walk.
	 * @param numberOfThreads    The number of threads to be used.
	 */
	public void generateWalks(String walkOutputFileName, int numberOfWalks, int depth, int numberOfThreads) {
		// validity check
		if (broaderConcepts == null || broaderConcepts.size() < 5) {
			LOG.error("No braoder concepts seem to be loaded. ABORT.");
			return;
		}

		// intialize the writer
		try {
			walkWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(walkOutputFileName, false)),
					"utf-8");
		} catch (Exception e1) {
			LOG.error("Could not initialize writer.");
			e1.printStackTrace();
		}

		nameOfWalkFile = walkOutputFileName;

		ThreadPoolExecutor pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 0, TimeUnit.SECONDS,
				new java.util.concurrent.ArrayBlockingQueue<Runnable>(broaderConcepts.size()));

		startTime = System.currentTimeMillis();
		for (String entity : broaderConcepts.keySet()) {
			EntityProcessingThreadXL th = new EntityProcessingThreadXL(this, entity, numberOfWalks,
					depth);
			pool.execute(th);
		}
		pool.shutdown();
		try {
			pool.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			LOG.error("Interrupted Exception");
			e.printStackTrace();
		}
		try {
			walkWriter.flush();
			walkWriter.close();
		} catch (IOException e) {
			LOG.error("IO Exception");
			e.printStackTrace();
		}
	}

	/**
	 * Return a random broader concept of the given concept.
	 * 
	 * @param concept Concept for which a hypernym shall be found.
	 * @return Retrieved hypernym.
	 */
	public String drawBroaderConcept(String concept) {
		ArrayList<String> concepts = broaderConcepts.get(concept);
		if (concepts == null) {
			return null;
		}
		return concepts.get(rand.nextInt(concepts.size()));
	}

	/**
	 * Method which allows thread to persist their walks.
	 * 
	 * @param walks The walks to be persisted.
	 */
	public synchronized void writeWalksToFile(List<String> walks) {
		if (walks == null) {
			return;
		}
		processedEntities++;
		processedWalks = processedWalks + walks.size();
		newFileCounter = newFileCounter + walks.size();
		for (String walk : walks) {
			try {
				walkWriter.write(walk + "\n");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		// just output:
		if (processedEntities % 1000 == 0) {
			System.out.println("TOTAL PROCESSED ENTITIES: " + processedEntities);
			System.out.println("TOTAL NUMBER OF WALKS: " + processedWalks);
			System.out.println("TIME: " + ((System.currentTimeMillis() - startTime) / 1000));
		}

		// file flushing
		if (newFileCounter > 3000000) {
			newFileCounter = 0;
			try {
				walkWriter.flush();
				walkWriter.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			try {
				fileNumber++;
				walkWriter = new OutputStreamWriter(
						new GZIPOutputStream(new FileOutputStream(nameOfWalkFile + "_" + fileNumber)));
			} catch (IOException ioe) {
				LOG.error("ERROR while reinstantiating writer.");
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Remove special characters from a concept in order to: - be more space
	 * efficient - allow for easier lookup
	 * 
	 * @param conceptToClean
	 * @return
	 */
	public static String cleanConcept(String conceptToClean) {

		// handling URL specific encodings like '%20' for space (' ') or '%26' for '&'.
		try {
			conceptToClean = urlDecoder.decode(conceptToClean, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOG.error("Decoding URL (" + conceptToClean + ") failed. Program will continue.");
			e.printStackTrace();
		}
		conceptToClean = conceptToClean.replace(" ", "_").replace("\t", "_").replace("+", "_");

		if (conceptToClean.startsWith("_")) {
			conceptToClean = conceptToClean.substring(1, conceptToClean.length());
		}
		if (conceptToClean.endsWith("_")) {
			conceptToClean = conceptToClean.substring(0, conceptToClean.length() - 1);
		}
		return conceptToClean.trim();
	}

}
