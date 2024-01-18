package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a single decimal digit, from 0 to 9.
 */
public class ExprGlyphDigit extends AbstractExprGlyph {

    /** The digit, from 0 to 9. */
    public final int digit;

    /**
     * Constructs a new {@code ExprGlyphDigit}.
     *
     * @param theDigit the digit
     */
    public ExprGlyphDigit(final int theDigit) {

        super();

        if (theDigit < 0 || theDigit > 9) {
            throw new IllegalArgumentException("Invalid digit");
        }

        this.digit = theDigit;
    }
}
