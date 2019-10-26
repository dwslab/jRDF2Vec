package walkGenerators.alod.services.tools.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * A service which allows to persist certain data structures.
 * You can use this very conveniently for smaller data structures.
 * For large structures a DB might be a better choice.
 */
public class PersistenceService {

    private static Logger LOG = LoggerFactory.getLogger(PersistenceService.class);
    private static final String PERSITENCE_DIRECTORY_PATH = "./persistences/";

    /**
     * Persist an object.
     * @param objectToPersist Object to persist.
     * @param key Persistence key.
     */
    public static void persist(Object objectToPersist, String key){
        try {
            LOG.debug("Persisting object with key " + key + " to file...");

            // check for parent directory and create it if it does not exist
            File directory = new File(PERSITENCE_DIRECTORY_PATH);
            if(!directory.exists()){
                // make directory
                directory.mkdir();
            }

            // write the file
            File fileToWrite = new File(PERSITENCE_DIRECTORY_PATH + key + ".ser");
            fileToWrite.createNewFile();
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileToWrite));
            Persistence p = new Persistence();
            p.persistenceObject = objectToPersist;
            out.writeObject(p);
            LOG.debug("Successfully persisted on disk.");
        } catch (FileNotFoundException e) {
            LOG.error("Persistence error.");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Persistence error.");
            e.printStackTrace();
        }
    }


    /**
     * Retrieve an object.
     * @param key Persistence key.
     * @return Object to retrieve.
     */
    public static Object retrieve(String key){
        try {
            LOG.debug("Loading buffer with key " + key + " from disk...");
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(PERSITENCE_DIRECTORY_PATH + key + ".ser")));
            Persistence p = (Persistence) in.readObject();
            LOG.debug("Load complete.");
            return p.persistenceObject;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Checks whether there is a persistance available for the given key.
     * @param key The key.
     * @return TRUE if persistance is available, else false.
     */
    public static boolean persistenceAvailable(String key){
        boolean result = new File(PERSITENCE_DIRECTORY_PATH + key + ".ser").isFile();
        if(result){
            LOG.debug("Persistence with key " + key + " AVAILABLE.");
        } else {
            LOG.debug("Persistence with key " + key + " NOT AVAILABLE.");
        }
        return result;
    }

}
