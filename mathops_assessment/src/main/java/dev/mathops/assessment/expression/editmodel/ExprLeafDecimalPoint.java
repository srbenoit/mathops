package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a decimal point.
 */
public final class ExprLeafDecimalPoint extends AbstractExprLeaf {

    /**
     * Constructs a new {@code ExprLeafDecimalPoint}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprLeafDecimalPoint(final AbstractExprObject theParent) {

        super(theParent);
    }
}
