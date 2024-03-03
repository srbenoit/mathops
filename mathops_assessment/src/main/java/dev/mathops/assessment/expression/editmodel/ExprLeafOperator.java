package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents an operator.
 */
public final class ExprLeafOperator extends AbstractExprLeaf {

    /** The operator. */
    public final EOperatorSymbol operator;

    /**
     * Constructs a new {@code ExprLeafOperator}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theOperator the operator
     */
    public ExprLeafOperator(final AbstractExprObject theParent, final EOperatorSymbol theOperator) {

        super(theParent);

        this.operator = theOperator;
        innerSetNumCursorPositions(1);
    }
}
