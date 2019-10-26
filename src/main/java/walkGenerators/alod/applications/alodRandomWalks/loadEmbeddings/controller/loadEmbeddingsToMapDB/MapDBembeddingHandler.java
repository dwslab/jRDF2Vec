package walkGenerators.alod.applications.alodRandomWalks.loadEmbeddings.controller.loadEmbeddingsToMapDB;

import org.apache.commons.lang3.ArrayUtils;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import walkGenerators.alod.applications.alodRandomWalks.loadEmbeddings.model.EmbeddingHandler;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Class which allows to load and retrieve embeddings to a MapDB data structure.
 */
public class MapDBembeddingHandler implements EmbeddingHandler {

	private final static Logger LOG = LoggerFactory.getLogger(MapDBembeddingHandler.class);
	private String dbName;
	private BTreeMap<String, double[]> embeddings;
	private DB db;
	private boolean isConnected = false;
	Map<String, double[]> buffer = Collections.synchronizedMap(new HashMap<String, double[]>()); // a storage structure to fasten multiple requests for the same concept
	
	/**
	 * Constructor
	 * 
	 * @param dbName Name of the DB to which a connection shall be established.
	 */
	public MapDBembeddingHandler(String dbName) {
		this.dbName = dbName;
		connect();
	}

	/**
	 * Load embeddings from a file to persistence.
	 * 
	 * @param pathToGzippedEmbeddingsFile Path to the gzipped file.
	 * @param dimension                   Dimension of vectors.
	 */
	public void loadEmbeddings(String pathToGzippedEmbeddingsFile, int dimension) {

		try {
			File inputFile = new File(pathToGzippedEmbeddingsFile);
			if (!inputFile.exists()) {
				LOG.error("Input file does not exist. ABORT.");
				db.close(); // required to keep the file uncorrupted.
				return;
			}

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile))));

			long linesRead = 0;
			String readLine;
			String[] components;
			double[] vector;
			while ((readLine = reader.readLine()) != null) {
				vector = new double[dimension];
				components = readLine.split(" ");

				for (int i = 1; i < dimension; i++) {
					vector[i - 1] = Double.parseDouble(components[i]);
				}
				embeddings.put(components[0].replace("_", " "), vector);
				linesRead++;
				if (linesRead % 10000 == 0) {
					System.out.println(linesRead);
				}
			}

			db.close();
			System.out.println("Done loading " + pathToGzippedEmbeddingsFile + ".");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Double[] getVector(String term) {
		return ArrayUtils.toObject(getVectorPrimitive(term));
	}

	public double[] getVectorPrimitive(String term) {
		if (buffer.containsKey(term)) {
			return buffer.get(term);
		}
		double[] result = embeddings.get(term);
		buffer.put(term, result);
		return result;
	}

	@Override
	public boolean hasVector(String term) {
		return embeddings.containsKey(term);
	}

	@Override
	public String getDatabaseFileName() {
		return dbName;
	}

	@Override
	public void connect() {
		if (!isConnected) {
			// init db
			db = DBMaker.fileDB("./output/databases/" + this.dbName).closeOnJvmShutdown().make();
			

			// get data structure
			embeddings = db.treeMap("embeddings").keySerializer(Serializer.STRING)
					.valueSerializer(Serializer.DOUBLE_ARRAY).createOrOpen();

			isConnected = true;
		}
	}

	@Override
	public void close() {
		db.close();
	}

}
