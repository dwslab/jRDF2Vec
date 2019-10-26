package walkGenerators.alod.applications.alodRandomWalks.loadEmbeddings;

import walkGenerators.alod.applications.alodRandomWalks.loadEmbeddings.controller.loadEmbeddingsToMapDB.MapDBembeddingHandler;

/**
 * Load a trained embeddings file into mapDB storage file (will be created on the fly).
 */
public class Embeddings2mapDatabaseApplication {

	public static void main(String[] args) {
		MapDBembeddingHandler handler = new MapDBembeddingHandler("CBOW_200_XL_RED_100_8");
		handler.loadEmbeddings(
				"C:\\Users\\D060249\\OneDrive - SAP SE\\From Linux\\Embeddings\\XL_reduced_10\\XL_reduced_10_cbow_200_100_8_no_reverse_window5_java.gz",
				200);
	}
	
}
