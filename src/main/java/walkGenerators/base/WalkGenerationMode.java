package walkGenerators.base;

/**
 * Generation Mode
 */
public enum WalkGenerationMode {

    /**
     * Random walks.
     */
    RANDOM_WITH_DUPLICATES,

    /**
     * Random walks without duplicates.
     */
    RANDOM_DUPLICATE_FREE,

    /**
     * Mid walks.
     */
    MID_WITH_DUPLICATES;

    /**
     * Maps a string representation to an enum instance.
     * @param modeIdentifier Label describing the mode.
     * @return Mode, if unknown: null.
     */
    public static WalkGenerationMode getMode(String modeIdentifier){
        modeIdentifier = modeIdentifier.toUpperCase();
        modeIdentifier = modeIdentifier.trim();
        switch (modeIdentifier){
            case "RANDOM_WITH_DUPLICATES":
                return WalkGenerationMode.RANDOM_WITH_DUPLICATES;
            case "RANDOM_DUPLICATE_FREE":
                return WalkGenerationMode.RANDOM_DUPLICATE_FREE;
            case "MID_WITH_DUPLICATES":
                return WalkGenerationMode.MID_WITH_DUPLICATES;
            default:
                return null;
        }
    }

    /**
     * Obtain all available options.
     * @return String of options, pipe separated.
     */
    public static String getOptions(){
        return "RANDOM_WITH_DUPLICATES | RANDOM_DUPLICATE_FREE | MID_WITH_DUPLICATES";
    }

}
