package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A glyph that represents a radical with a subexpression under the radical but no root.
 */
public final class ExprBranchRadical extends ExprObjectBranch {

    /** The base. */
    public final Expr base;

    /**
     * Constructs a new {@code ExprBranchRadical}.
     */
    public ExprBranchRadical() {

        super();

        this.base = new Expr();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprBranchRadical{base=");
        htm.add(this.base);
        htm.add("}");

        return htm.toString();
    }
}

