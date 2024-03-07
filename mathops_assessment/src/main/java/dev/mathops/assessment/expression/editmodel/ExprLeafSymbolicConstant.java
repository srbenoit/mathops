package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An expression object that represents a symbolic real-valued constant like pi or e or the imaginary unit i.
 */
public final class ExprLeafSymbolicConstant extends ExprObjectLeaf {

    /** The symbolic value. */
    private final ESymbolicConstant value;

    /**
     * Constructs a new {@code ExprLeafSymbolicConstant}.
     *
     * @param theValue the symbolic value
     */
    public ExprLeafSymbolicConstant(final ESymbolicConstant theValue) {

        super();

        if (theValue == null) {
            throw new IllegalArgumentException("Value may not be null");
        }

        this.value = theValue;
    }

    /**
     * Gets the symbolic value.
     *
     * @return the value
     */
    public ESymbolicConstant getValue() {

        return this.value;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprLeafSymbolicConstant{value='");
        htm.add(this.value);
        htm.add("}");

        return htm.toString();
    }
}
