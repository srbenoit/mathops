package dev.mathops.assessment.problem;

/**
 * Problem types.
 */
public enum EProblemType {

    /** Numeric response. */
    NUMERIC("Numeric"),

    /** Multiple choice (choose 1 of N). */
    MULTIPLE_CHOICE("MultipleChoice"),

    /** Multiple selection (choose 1 or more of N). */
    MULTIPLE_SELECTION("MultipleSelection"),

    /** Embedded input. */
    EMBEDDED_INPUT("EmbeddedInput"),

    /** Question that is automatically considered correct. */
    AUTO_CORRECT("AutoCorrect"),

    /** A dummy problem that could not be parsed or has no type assigned. */
    DUMMY("Dummy");

    /** The type label. */
    public final String label;

    /**
     * Constructs a new {@code EProblemType}.
     *
     * @param theLabel the problem type label
     */
    EProblemType(final String theLabel) {

        this.label = theLabel;
    }

    /**
     * Gets the string representation of the type.
     *
     * @return the label
     */
    @Override
    public String toString() {

        return this.label;
    }

//    /**
//     * Gets the {@code EProblemType} that has a specified label.
//     *
//     * @param theLabel the label
//     * @return the corresponding {@code EProblemType} ; {@code null} if none
//     */
//     public static EProblemType forLabel(final String theLabel) {
//
//     EProblemType result = null;
//
//     for (EProblemType value : EProblemType.values()) {
//
//     if (value.label.contentEquals(theLabel)) {
//     result = value;
//     break;
//     }
//     }
//
//     return result;
//     }
}
