package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a fraction with subexpression numerator and subexpression denominator.
 */
public class ExprGlyphFraction extends AbstractExprGlyph {

    /** The numerator. */
    public final ExprGlyphSequence numerator;

    /** The denominator. */
    public final ExprGlyphSequence denominator;

    /**
     * Constructs a new {@code ExprGlyphFraction}.
     */
    public ExprGlyphFraction() {

        super();

        this.numerator = new ExprGlyphSequence();
        this.denominator = new ExprGlyphSequence();
    }
}
