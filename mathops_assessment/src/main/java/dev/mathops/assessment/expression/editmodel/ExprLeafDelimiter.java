package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a delimiter.
 */
public final class ExprLeafDelimiter extends AbstractExprLeaf {

    /** The delimiter. */
    final EDelimiter delimiter;

    /**
     * Constructs a new {@code ExprLeafDelimiter}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theDelimiter the delimiter
     */
    public ExprLeafDelimiter(final AbstractExprObject theParent, final EDelimiter theDelimiter) {

        super(theParent);

        this.delimiter = theDelimiter;
    }
}
