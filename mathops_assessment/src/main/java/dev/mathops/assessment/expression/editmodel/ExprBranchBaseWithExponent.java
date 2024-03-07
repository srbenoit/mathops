package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An expression object that represents a subexpression (the base) raised to a subexpression (the exponent).
 */
public final class ExprBranchBaseWithExponent extends ExprObjectBranch {

    /** The base. */
    public final Expr base;

    /** The exponent. */
    public final Expr exponent;

    /**
     * Constructs a new {@code ExprBranchBaseWithExponent}.
     */
    public ExprBranchBaseWithExponent() {

        super();

        this.base = new Expr();
        this.exponent = new Expr();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprBranchBaseWithExponent{base=");
        htm.add(this.base);
        htm.add(",exponent=");
        htm.add(this.exponent);
        htm.add("}");

        return htm.toString();
    }
}

