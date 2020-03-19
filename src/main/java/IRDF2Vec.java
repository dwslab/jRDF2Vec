/**
 * Interface for RDF2Vec orchestration classes.
 */
public interface IRDF2Vec {

    /**
     * This method returns the time it took to generate walks for the last run as String.
     * @return The time it took to generate walks for the last run as String. Will never be null.
     */
    String getRequiredTimeForLastWalkGenerationString();

    /**
     * This method returns he time it took to train the model for the last run as String.
     * @return The time it took to train the model for the last run as String. Will never be null.
     */
    String getRequiredTimeForLastTrainingString();
}
