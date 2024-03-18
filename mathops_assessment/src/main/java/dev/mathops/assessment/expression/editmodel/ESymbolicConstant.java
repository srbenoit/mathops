package dev.mathops.assessment.expression.editmodel;

/**
 * Symbolic constant values.
 */
public enum ESymbolicConstant {

    /** Pi. */
    PI("\u03c0"),

    /** E. */
    E("\u212F"),

    /** I. */
    I("\u2148");

    /** The string that represents the operator. */
    public final String str;

    /**
     * Constructs a new {@code ESymbolicConstant}.
     *
     * @param theStr the character that represents the constant
     */
    ESymbolicConstant(final String theStr) {

        this.str = theStr;
    }
}
