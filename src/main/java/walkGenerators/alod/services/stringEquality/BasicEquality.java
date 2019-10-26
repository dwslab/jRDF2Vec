package walkGenerators.alod.services.stringEquality;


import walkGenerators.alod.services.tools.StringOperations;

/**
 * This class considers two Strings to be equal when they contain the same tokens. Lowercasing is applied.
 */
public class BasicEquality implements StringEquality{

    @Override
    public boolean isSameString(String s1, String s2) {
        return StringOperations.isSameString(s1, s2);
    }

    @Override
    public String getName() {
        return "BasicEquality";
    }
}
