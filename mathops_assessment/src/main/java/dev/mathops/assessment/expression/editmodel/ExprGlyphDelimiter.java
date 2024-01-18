package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a delimiter.
 */
public class ExprGlyphDelimiter extends AbstractExprGlyph {

    /** The delimiter. */
    public final EDelimiter delimiter;

    /**
     * Constructs a new {@code ExprGlyphDelimiter}.
     *
     * @param theDelimiter the delimiter
     */
    public ExprGlyphDelimiter(final EDelimiter theDelimiter) {

        super();

        this.delimiter = theDelimiter;
    }
}
