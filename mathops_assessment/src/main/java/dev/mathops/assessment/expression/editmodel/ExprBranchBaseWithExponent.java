package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a subexpression (the base) raised to a subexpression (the exponent).
 *
 * <p>
 * When evaluated, if the base and exponent evaluate to numbers, the result is the number found by raising the base
 * to the exponent.  If the base is a square matrix, and the exponent is zero or a (small) positive integer, the result
 * is the matrix raised to the specified power.
 */
public final class ExprBranchBaseWithExponent extends AbstractExprBranch {

    /** The base. */
    public final Expr base;

    /** The exponent. */
    public final Expr exponent;

    /**
     * Constructs a new {@code ExprBranchBaseWithExponent}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprBranchBaseWithExponent(final AbstractExprObject theParent) {

        super(theParent);

        this.base = new Expr(this);
        this.exponent = new Expr(this);
    }
}

