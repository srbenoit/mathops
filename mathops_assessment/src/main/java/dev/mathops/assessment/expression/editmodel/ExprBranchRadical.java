package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a radical with a subexpression under the radical but no root.
 */
public final class ExprBranchRadical extends AbstractExprBranch {

    /** The base. */
    public final Expr base;

    /**
     * Constructs a new {@code ExprBranchRadical}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprBranchRadical(final AbstractExprObject theParent) {

        super(theParent);

        this.base = new Expr(this);
    }
}

