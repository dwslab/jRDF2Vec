package de.uni_mannheim.informatik.dws.jrdf2vec;

import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.jrdf2vec.walk_generation.base.WalkGenerationMode;

/**
 * Interface for RDF2Vec orchestration classes.
 */
public interface IRDF2Vec {

    /**
     * This method returns the time it took to generate walks for the last run as String.
     *
     * @return The time it took to generate walks for the last run as String. Will never be null.
     */
    String getRequiredTimeForLastWalkGenerationString();

    /**
     * This method returns he time it took to train the model for the last run as String.
     *
     * @return The time it took to train the model for the last run as String. Will never be null.
     */
    String getRequiredTimeForLastTrainingString();

    /**
     * Set the walk generation mode.
     *
     * @param walkGenerationMode Mode to use.
     */
    void setWalkGenerationMode(WalkGenerationMode walkGenerationMode);

    /**
     * Set the walk generation mode for the generation part of RDF2Vec.
     *
     * @return {@link WalkGenerationMode} to be used.
     */
    WalkGenerationMode getWalkGenerationMode();

    /**
     * Obtain the Word2Vec Configuration
     *
     * @return The configuration object.
     */
    Word2VecConfiguration getWord2VecConfiguration();

    /**
     * Obtain the indicator of whether a vector text file will be generated.
     *
     * @return True if a vector text file will be generated, else false.
     */
    boolean isVectorTextFileGeneration();

    /**
     * Set whether a vector text file will be generated.
     *
     * @param vectorTextFileGeneration True if file shall be generated, else false.
     */
    void setVectorTextFileGeneration(boolean vectorTextFileGeneration);

    /**
     * The number of walks to be generated per entity.
     *
     * @return Number of walks.
     */
    int getNumberOfWalksPerEntity();

    /**
     * Text embedding option getter.
     *
     * @return True if text will be included in the embedding.
     */
    boolean isEmbedText();

    /**
     * Text embedding option setter.
     * @param embedText Boolean
     */
    void setEmbedText(boolean embedText);
}
