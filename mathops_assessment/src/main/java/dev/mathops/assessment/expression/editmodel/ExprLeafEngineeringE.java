package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents the "E" that precedes a power of 10 in engineering notation.
 */
public final class ExprLeafEngineeringE extends AbstractExprLeaf {

    /**
     * Constructs a new {@code ExprLeafEngineeringE}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprLeafEngineeringE(final AbstractExprObject theParent) {

        super(theParent);
    }
}
