package walkGenerators.classic.alod.applications.statistics.model;

/**
 * Data structure to store one point of a distribution of relations.
 */
public class DistributionPoint implements Comparable<DistributionPoint>{

    public int relationCount;
    public int frequency;

    /**
     * Constructor
     * @param relationCount Relative count to be set.
     * @param frequency The frequency to be set.
     */
    public DistributionPoint(int relationCount, int frequency){
        this.relationCount = relationCount;
        this.frequency = frequency;
    }


    @Override
    public int compareTo(DistributionPoint that) {
        //returns -1 if "this" object is less than "that" object
        //returns 0 if they are equal
        //returns 1 if "this" object is greater than "that" object
        if(this.relationCount < that.relationCount){
            return -1;
        } else if(this.relationCount == that.relationCount){
            return 0;
        } else {
            // (this.doubleValue > that.doubleValue)
            return 1;
        }
    }
}
