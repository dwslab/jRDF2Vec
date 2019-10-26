package walkGenerators.alod.services.tools;

import java.io.Serializable;

/**
 * Tuple structure to persist concepts and their corresponding confidence value.
 */
public class ConceptConfidenceTuple implements Serializable {

    /**
     * Constructor
     * @param concept The ALOD concept.
     * @param confidence The corresponding confidence.
     */
    public ConceptConfidenceTuple(String concept, double confidence){
        this.concept = concept;
        this.confidence = confidence;
    }

    public String concept;
    public double confidence;
}
