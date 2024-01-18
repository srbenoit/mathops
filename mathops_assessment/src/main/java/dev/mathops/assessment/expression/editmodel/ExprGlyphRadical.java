package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a radical with a subexpression under the radical and optional subexpression as the root.
 */
public class ExprGlyphRadical extends AbstractExprGlyph {

    /** The base. */
    public final ExprGlyphSequence base;

    /** The root. */
    public final ExprGlyphSequence root;

    /**
     * Constructs a new {@code ExprGlyphRadical}.
     */
    public ExprGlyphRadical() {

        super();

        this.base = new ExprGlyphSequence();
        this.root = new ExprGlyphSequence();
    }
}
