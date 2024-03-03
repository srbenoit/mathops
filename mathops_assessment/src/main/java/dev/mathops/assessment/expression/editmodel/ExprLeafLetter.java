package dev.mathops.assessment.expression.editmodel;

/**
 * An expression object that represents a single letter character.
 */
public final class ExprLeafLetter extends AbstractExprLeaf {

    /** The character. */
    public final char character;

    /**
     * Constructs a new {@code ExprLeafLetter}.
     *
     * @param theParent the parent object ({@code null} only for the root node)
     * @param theCharacter the character
     */
    public ExprLeafLetter(final AbstractExprObject theParent, final char theCharacter) {

        super(theParent);

        if (!Character.isLetter(theCharacter)) {
            throw new IllegalArgumentException("Invalid letter");
        }

        this.character = theCharacter;
        innerSetNumCursorPositions(1);
    }
}
