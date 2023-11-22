package dev.mathops.assessment.variable;

/**
 * The possible types of variables.
 */
public enum EVariableType {

    /** Boolean. */
    BOOLEAN("Boolean"),

    /** Integer. */
    INTEGER("Integer"),

    /** Real. */
    REAL("Real"),

    /** Span. */
    SPAN("Span"),

    /** Random Boolean. */
    RANDOM_BOOLEAN("Random Boolean"),

    /** Random Integer. */
    RANDOM_INTEGER("Random Integer"),

    /** Random Boolean. */
    RANDOM_REAL("Random Real"),

    /** Random Permutation. */
    RANDOM_PERMUTATION("Random Permutation"),

    /** Random Boolean. */
    RANDOM_CHOICE("Random Choice"),

    /** Random Simple Angle. */
    RANDOM_SIMPLE_ANGLE("Random Simple Angle"),

    /** Input Integer. */
    INPUT_INTEGER("Input Integer"),

    /** Input Real. */
    INPUT_REAL("Input Real"),

    /** Input String. */
    INPUT_STRING("Input String"),

    /** Input Integer Vector. */
    INPUT_INTEGER_VECTOR("Input Integer Vector"),

    /** Derived. */
    DERIVED("Derived"),

    ;

    /** The value. */
    private final String label;

    /**
     * Constructs a new {@code EVariableType}.
     *
     * @param theLabel the label
     */
    EVariableType(final String theLabel) {

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
}
