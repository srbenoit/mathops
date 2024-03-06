package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a fraction with subexpression numerator and subexpression denominator.
 */
public final class ExprBranchFraction extends AbstractExprBranch {

    /** The shape. */
    private EFractionShape shape;

    /** The numerator. */
    public final Expr numerator;

    /** The denominator. */
    public final Expr denominator;

    /**
     * Constructs a new {@code ExprBranchFraction}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theShape the shape
     */
    public ExprBranchFraction(final AbstractExprObject theParent, final EFractionShape theShape) {

        super(theParent);

        this.shape = theShape;
        this.numerator = new Expr(this);
        this.denominator = new Expr(this);
    }

    /**
     * Gets the fraction's shape.
     *
     * @return the shape
     */
    public EFractionShape getShape() {

        return this.shape;
    }

    /**
     * Sets the fraction's shape.
     *
     * @param theShape the shape
     */
    public void setShape(final  EFractionShape theShape) {

        this.shape = theShape;
    }
}

