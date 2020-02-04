package walkGenerators.classic.alod.applications.alodRandomWalks.generationInMemory.model;

public class StringFloat implements Comparable<StringFloat>{

    public String stringValue;
    public Float floatValue;

    public StringFloat(String s, Float f){
        stringValue = s;
        floatValue = f;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (!(obj instanceof StringFloat)) return false;

        StringFloat that = (StringFloat)obj;
        return this.floatValue.equals(that.floatValue);
    }

    @Override
    public int hashCode(){
        return stringValue.hashCode();
    }

    @Override
    public int compareTo(StringFloat that){
        //returns -1 if "this" object is less than "that" object
        if(this.floatValue < that.floatValue){
            return -1;
        }

        //returns 0 if they are equal
        if(this.floatValue == that.floatValue){
            return 0;
        }

        //returns 1 if "this" object is greater than "that" object
        return 1;
    }

}