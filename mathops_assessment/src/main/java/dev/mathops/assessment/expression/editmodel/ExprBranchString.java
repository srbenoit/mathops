package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a string, enclosed in matched double-quotes.
 */
public final class ExprBranchString extends AbstractExprBranch {

    /** The characters. */
    public char[] characters;

    /** The length (could be shorter than the allocated character array). */
    public int len;

    /**
     * Constructs a new {@code ExprBranchString}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     */
    public ExprBranchString(final AbstractExprObject theParent) {

        super(theParent);

        this.characters = new char[10];
        this.len = 0;
    }
}
