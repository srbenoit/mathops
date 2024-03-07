package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An expression object that represents a fraction with subexpression numerator and subexpression denominator.
 */
public final class ExprBranchFraction extends ExprObjectBranch {

    /** The shape. */
    private EFractionShape shape;

    /** The numerator. */
    public final Expr numerator;

    /** The denominator. */
    public final Expr denominator;

    /**
     * Constructs a new {@code ExprBranchFraction}.
     *
     * @param theShape the shape
     */
    public ExprBranchFraction(final EFractionShape theShape) {

        super();

        this.shape = theShape;
        this.numerator = new Expr();
        this.denominator = new Expr();
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

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprBranchFraction{numerator=");
        htm.add(this.numerator);
        htm.add(",denominator=");
        htm.add(this.denominator);
        htm.add("}");

        return htm.toString();
    }
}

