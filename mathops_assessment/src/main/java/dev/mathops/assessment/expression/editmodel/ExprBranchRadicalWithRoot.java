package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a radical with a subexpression under the radical and a subexpression as the root.
 */
public final class ExprBranchRadicalWithRoot extends AbstractExprBranch {

    /** The root. */
    public final Expr root;

    /** The base. */
    public final Expr base;

    /**
     * Constructs a new {@code ExprBranchRadicalWithRoot}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprBranchRadicalWithRoot(final AbstractExprObject theParent) {

        super(theParent);

        this.root = new Expr(this);
        this.base = new Expr(this);
    }
}

