package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a subexpression (the base) raised to the power of another subexpression (the exponent).
 */
public class ExprGlyphBaseWithExponent extends AbstractExprGlyph {

    /** The base. */
    public final ExprGlyphSequence base;

    /** The exponent. */
    public final ExprGlyphSequence exponent;

    /**
     * Constructs a new {@code ExprGlyphBaseWithExponent}.
     */
    public ExprGlyphBaseWithExponent() {

        super();

        this.base = new ExprGlyphSequence();
        this.exponent = new ExprGlyphSequence();
    }
}
