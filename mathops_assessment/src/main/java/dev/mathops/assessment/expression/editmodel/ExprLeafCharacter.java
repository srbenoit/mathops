package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a single arbitrary character.
 */
public final class ExprLeafCharacter extends AbstractExprLeaf {

    /** The character. */
    public final char character;

    /**
     * Constructs a new {@code ExprLeafCharacter}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theCharacter the character
     */
    public ExprLeafCharacter(final AbstractExprObject theParent, final char theCharacter) {

        super(theParent);

        this.character = theCharacter;
        innerSetNumCursorPositions(1);
    }
}
