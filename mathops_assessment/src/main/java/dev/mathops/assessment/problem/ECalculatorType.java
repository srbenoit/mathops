package dev.mathops.assessment.problem;

/**
 * Calculator types.
 */
public enum ECalculatorType {

    /** No calculator. */
    NO_CALC("none"),

    /** Basic calculator. */
    BASIC_CALC("basic"),

    /** Scientific calculator. */
    SCIENTIFIC_CALC("scientific"),

    /** Graphing calculator, without solving features. */
    GRAPHING_CALC("graphing"),

    /** Full calculator. */
    FULL_CALC("full");

    /** The value. */
    public final String label;

    /**
     * Constructs a new {@code ECalculatorType}.
     *
     * @param theLabel the label
     */
    ECalculatorType(final String theLabel) {

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

    /**
     * Gets the {@code ECalculatorType} that has a specified label.
     *
     * @param theLabel the label
     * @return the corresponding {@code ECalculatorType} ; {@code null} if none
     */
    public static ECalculatorType forLabel(final CharSequence theLabel) {

        ECalculatorType result = null;

        for (final ECalculatorType value : values()) {
            if (value.label.contentEquals(theLabel)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
