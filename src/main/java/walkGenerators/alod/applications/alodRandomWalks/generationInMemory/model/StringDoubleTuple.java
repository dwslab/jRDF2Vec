package walkGenerators.alod.applications.alodRandomWalks.generationInMemory.model;

/**
 * Sortable Data Structure
 */
public class StringDoubleTuple implements Comparable<StringDoubleTuple>{
    public String stringValue;
    public double doubleValue;
    public StringDoubleTuple(String stringValue, double doubleValue){
        this.stringValue = stringValue;
        this.doubleValue = doubleValue;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof StringDoubleTuple)) return false;
        StringDoubleTuple that = (StringDoubleTuple) obj;
        return (this.stringValue.equals(that.stringValue) && this.doubleValue == that.doubleValue);
    }

    @Override
    public int hashCode(){
        return (stringValue.hashCode());
    }

    @Override
    public int compareTo(StringDoubleTuple that){
        //returns -1 if "this" object is less than "that" object
        //returns 0 if they are equal
        //returns 1 if "this" object is greater than "that" object
        if(this.doubleValue < that.doubleValue){
            return -1;
        } else if(this.doubleValue == that.doubleValue){
            return 0;
        } else {
            // (this.doubleValue > that.doubleValue)
            return 1;
        }
    }
}