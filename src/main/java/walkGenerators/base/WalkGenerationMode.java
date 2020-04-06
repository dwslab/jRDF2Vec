package walkGenerators.base;

/**
 * There must be a runnable for each walk generation option.
 */
public enum WalkGenerationMode {

    /**
     * Mid-walk walk generation: Given an entity, it is randomly decided whether to go backwards or forwards during
     * the walk generation.
     */
    MID_WALKS,

    /**
     * Mid-walk walk generation: Given an entity, it is randomly decided whether to go backwards or forwards during
     * the walk generation. The generated walks are free of duplicates. Due to the implementation this can lead
     * to less generated walks than originally specified.
     */
    MID_WALKS_DUPLICATE_FREE,

    /**
     * Plain random walks generated in a forward-fashion (going backwards is not allowed).
     */
    RANDOM_WALKS,

    /**
     * Plain random walks generated in a forward-fashion (going backwards is not allowed).
     * Duplicates are not allowed.
     */
    RANDOM_WALKS_DUPLICATE_FREE;


    /**
     * String representation of mode.
     * @param modeString The mode as String.
     * @return If possible, walk generation mode. Else null.
     */
    public static WalkGenerationMode getModeFromString(String modeString){
        modeString = modeString.toLowerCase().trim();
        switch (modeString){
            case "mid_walks":
                return MID_WALKS;
            case "mid_walks_duplicate_free":
                return MID_WALKS_DUPLICATE_FREE;
            case "random_walks":
                return RANDOM_WALKS;
            case "random_walks_duplicate_free":
                return RANDOM_WALKS_DUPLICATE_FREE;
            default:
                return null;
        }
    }

    /**
     * Get a string representation of all available modes.
     * @return String representation of all modes.
     */
    public static String getOptions(){
        String result = "";
        for(WalkGenerationMode mode : WalkGenerationMode.values()){
            result += mode.toString() + " | ";
        }
        result = result.substring(0, result.length() - 3);
        return result;
    }

}
