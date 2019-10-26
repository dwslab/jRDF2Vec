package walkGenerators.alod.applications.alodRandomWalks.loadEmbeddings;

import java.util.Arrays;

import walkGenerators.alod.applications.alodRandomWalks.loadEmbeddings.controller.loadEmbeddingsToSQLiteDB.SQLiteEmbeddingHandler;

/**
 * Load a trained embeddings file into a SQLite database (will be created on the fly).
 * Increase memory to > 6Gb.
 */
public class Embeddings2sqliteDatabaseApplication {

    public static void main(String[] args) { // CBOW_500_45_2
        SQLiteEmbeddingHandler s = new SQLiteEmbeddingHandler("CLASSIC_CBOW_200_100_8.db");
        s.connect();
        s.loadFile("C:\\Users\\D060249\\OneDrive - SAP SE\\From Linux\\ClassicWalks_100_8\\CBOW_200_100_8_new_java");
        Arrays.stream(s.getVector("interpersonal dynamics")).forEach(System.out::println);
        s.close();
    }

}
