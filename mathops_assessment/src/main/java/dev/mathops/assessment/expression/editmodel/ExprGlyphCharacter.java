package dev.mathops.assessment.expression.editmodel;

/**
 * A Unicode UTF-16 character.
 */
public class ExprGlyphCharacter extends AbstractExprGlyph {

    /** The character. */
    public final char character;

    /**
     * Constructs a new {@code ExprGlyphCharacter}.
     *
     * @param theCharacter the digit
     */
    public ExprGlyphCharacter(final char theCharacter) {

        super();

        this.character = theCharacter;
    }
}
