package dev.mathops.assessment.expression.editmodel;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * A glyph that represents an operator.
 */
public final class ExprLeafOperator extends ExprObjectLeaf {

    /** The operator. */
    private final EOperatorSymbol operator;

    /**
     * Constructs a new {@code ExprLeafOperator}.
     *
     * @param theOperator the operator
     */
    public ExprLeafOperator(final EOperatorSymbol theOperator) {

        super();

        if (theOperator == null) {
            throw new IllegalArgumentException("Operator may not be null");
        }

        this.operator = theOperator;
    }

    /**
     * Gets the operator.
     *
     * @return the operator
     */
    public EOperatorSymbol getOperator() {

        return this.operator;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprLeafOperator{operator='");
        htm.add(this.operator);
        htm.add("}");

        return htm.toString();
    }
}
