package dev.mathops.assessment.expression.editmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A sequence of expression glyphs.
 */
public class ExprGlyphSequence {

    /** The list of glyphs in the sequence, some of which may be containers for nested glyph sequences. */
    private final List<AbstractExprGlyph> glyphs;

    /**
     * Constructs a new {@code ExprGlyphSequence}.
     */
    public ExprGlyphSequence() {

        this.glyphs = new ArrayList<>(10);
    }
}
