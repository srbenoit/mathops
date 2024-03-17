package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A glyph that represents a radical with a subexpression under the radical and a subexpression (consisting of only
 * digits or only a single variable reference) as the root.
 */
public final class ExprBranchRadicalWithRoot extends ExprObjectBranch {

    /** The root. */
    public final Expr root;

    /** The base. */
    public final Expr base;

    /**
     * Constructs a new {@code ExprBranchRadicalWithRoot}.
     */
    public ExprBranchRadicalWithRoot() {

        super();

        this.root = new Expr();
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

        htm.add("ExprBranchRadicalWithRoot{root=");
        htm.add(this.root);
        htm.add(",base=");
        htm.add(this.base);
        htm.add("}");

        return htm.toString();
    }
}

