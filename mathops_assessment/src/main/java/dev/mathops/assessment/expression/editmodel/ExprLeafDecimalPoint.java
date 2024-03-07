package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a decimal point.
 */
public final class ExprLeafDecimalPoint extends ExprObjectLeaf {

    /**
     * Constructs a new {@code ExprLeafDecimalPoint}.
     */
    public ExprLeafDecimalPoint() {

        super();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "ExprLeafDecimalPoint";
    }
}
