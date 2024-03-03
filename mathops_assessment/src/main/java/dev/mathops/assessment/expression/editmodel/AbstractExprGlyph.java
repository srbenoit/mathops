package dev.mathops.assessment.expression.editmodel;

/**
 *  The base class for glyphs that can appear in the linear form of an expression.
 */
public abstract class AbstractExprGlyph {

    /**
     * Constructs a new {@code AbstractExprGlyph}.
     */
    AbstractExprGlyph() {

        // No action
    }

    /**
     * Gets the total number of cursor steps it would take to cross this glyph from left to right.
     *
     * @return the number of cursor steps
     */
    public abstract int getNumCursorSteps();
}
