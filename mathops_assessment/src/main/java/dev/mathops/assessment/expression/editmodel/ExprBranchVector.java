package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * An expression object that represents a vector with a subexpression for each component.
 */
public final class ExprBranchVector extends ExprObjectBranch {

    /** The maximum number of components supported. */
    private static final int MAX_COMPONENTS = 999;

    /** The type of brackets. */
    private EVectorMatrixBrackets brackets;

    /** The justification. */
    private EEntryJustification justification;

    /** The components. */
    public final List<Expr> components;

    /**
     * Constructs a new {@code ExprBranchVector}.
     *
     * @param numComponents the number of components (can be changed)
     * @param theBrackets the type of brackets
     */
    public ExprBranchVector(final EVectorMatrixBrackets theBrackets, final int numComponents) {

        super();

        if (theBrackets == null) {
            throw new IllegalArgumentException("Bracket type may not be null");
        }

        if (numComponents < 1 || numComponents > MAX_COMPONENTS) {
            throw new IllegalArgumentException("Invalid number of components");
        }

        this.brackets = theBrackets;
        this.components = new ArrayList<>(numComponents);

        for (int i = 0; i < numComponents; ++i) {
            this.components.add(new Expr());
        }
    }

    /**
     * Gets the type of brackets.
     *
     * @return the type of brackets
     */
    public EVectorMatrixBrackets getBrackets() {

        return this.brackets;
    }

    /**
     * Sets the type of brackets.
     *
     * @param theBrackets the new type of brackets
     */
    public void setBrackets(final EVectorMatrixBrackets theBrackets) {

        if (theBrackets == null) {
            throw new IllegalArgumentException("Bracket type may not be null");
        }

        this.brackets = theBrackets;
    }

    /**
     * Gets the number of components in the vector.
     *
     * @return the number of components
     */
    public int size() {

        return this.components.size();
    }

    /**
     * Gets the component expression at a specified index.
     *
     * @param index the index
     * @return the child object
     */
    public Expr getComponent(final int index) {

        return this.components.get(index);
    }

    /**
     * Adds a new component expression to the end of the vector.
     *
     * @return the newly added component expression
     */
    public Expr addComponent() {

        final Expr expr = new Expr();

        this.components.add(expr);

        return expr;
    }

    /**
     * Adds a new component expression at a specified position in the vector.
     *
     * @param index the index at which to add the new component
     * @return the newly added component expression
     */
    public Expr addComponent(final int index) {

        final Expr expr = new Expr();

        this.components.add(index, expr);

        return expr;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final int count = this.components.size();

        final HtmlBuilder htm = new HtmlBuilder(20 + count * 20);

        htm.add("ExprBranchVector{brackets=", this.brackets, ",components=[");

        if (count > 0) {
            final ExprObject child0 = this.components.getFirst();
            htm.add(child0);

            for (int i = 1; i < count; ++i) {
                htm.add(CoreConstants.COMMA);
                final ExprObject child = this.components.get(i);
                htm.add(child);
            }
        }

        htm.add("]}");

        return htm.toString();
    }
}
