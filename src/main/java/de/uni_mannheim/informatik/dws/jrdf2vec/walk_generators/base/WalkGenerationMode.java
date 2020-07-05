package de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.base;

/**
 * The available walk generation modes.
 * <p>
 * Developer note:
 * <ul>
 * <li>
 *     There must be a runnable for each walk generation option.
 * </li>
 * <li>
 *      The must be a resolution in evey implementation of {@link IWalkGenerator#generateWalks(WalkGenerationMode, int, int, int, String)} that shall support this walk mode.
 *      This affects, for example {@link WalkGeneratorDefault#generateWalks(WalkGenerationMode, int, int, int, String)} or {@link de.uni_mannheim.informatik.dws.jrdf2vec.walk_generators.light.WalkGeneratorLight#generateWalks(WalkGenerationMode, int, int, int, String)}.
 * </li>
 * </ul>
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
     * Weighted mid-walk walk generation: Given an entity, it is randomly decided whether to go backwards or forwards randomly
     * where the chances are determined by the number of options to go backwards and forwards:
     * If there are more options to go backwards than forwards, the likelihood of going backwards is larger. The generated
     * walks are free of duplicates. Due to the implementation this can lead to less generated walks than originally specified.
     */
    MID_WALKS_WEIGHTED,

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
     *
     * @param modeString The mode as String.
     * @return If possible, walk generation mode. Else null.
     */
    public static WalkGenerationMode getModeFromString(String modeString) {
        modeString = modeString.toLowerCase().trim();
        switch (modeString) {
            case "mid_walks":
                return MID_WALKS;
            case "mid_walks_duplicate_free":
                return MID_WALKS_DUPLICATE_FREE;
            case "mid_walks_weighted":
                return MID_WALKS_WEIGHTED;
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
     *
     * @return String representation of all modes.
     */
    public static String getOptions() {
        String result = "";
        for (WalkGenerationMode mode : WalkGenerationMode.values()) {
            result += mode.toString() + " | ";
        }
        result = result.substring(0, result.length() - 3);
        return result;
    }

}
