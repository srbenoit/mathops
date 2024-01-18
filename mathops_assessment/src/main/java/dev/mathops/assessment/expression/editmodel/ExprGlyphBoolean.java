package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a Boolean constant.
 */
public class ExprGlyphBoolean extends AbstractExprGlyph {

    /** The boolean value. */
    public final boolean value;

    /**
     * Constructs a new {@code ExprGlyphBoolean}.
     *
     * @param theValue the boolean value
     */
    public ExprGlyphBoolean(final boolean theValue) {

        super();

        this.value = theValue;
    }
}
