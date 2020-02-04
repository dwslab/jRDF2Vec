package walkGenerators.classic.alod.applications.alodRandomWalks.loadEmbeddings.model;

/**
 * Interface for classes capable of writing and retrieving embeddings to/from disk.
 * This interface allows to implement (and eventually run and compare) different persistence solutions.
 */
public interface EmbeddingHandler {
	
    /**
     * Obtain the embedding vector.
     * @param term The term for which the vector is to be retrieved.
     * @return The vector as array of Double objects.
     */
	Double[] getVector(String term);
	
	
	/**
	 * Obtain the embedding vector.
	 * @param term The term for which the vector is to be retrieved.
	 * @return The vector as array of double primitives.
	 */
	double[] getVectorPrimitive(String term);
	
	/**
     * Check whether there is an embedding for the specified concept.
     * @param term The concept for which existence is to be checked.
     * @return True if there is a concept, else false,
     */
	boolean hasVector(String term);
	
	
	/**
	 * Returns the name of the persistence object.
	 * @return Name of the data base file.
	 */
	String getDatabaseFileName();
	
	
	/**
	 * Connects to the persistence object.
	 */
	public void connect();
	
	
	/**
	 * Closes any open connection.
	 */
	void close();
	
}
