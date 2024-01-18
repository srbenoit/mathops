package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a symbolic real-valued constant like pi or e.
 */
public class ExprGlyphSymbolicReal extends AbstractExprGlyph {

    /** The symbolic value. */
    public final ESymbolicReal value;

    /**
     * Constructs a new {@code ExprGlyphSymbolicReal}.
     *
     * @param theValue the symbolic value
     */
    public ExprGlyphSymbolicReal(final ESymbolicReal theValue) {

        super();

        this.value = theValue;
    }
}
