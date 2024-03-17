package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.Expr;

import java.awt.Color;
import java.awt.Font;

/**
 * A laid out expression.
 */
public class ExprBox extends ObjectBox {

    /** The source expression object. */
    private final Expr source;

    /** The font. */
    Font font;

    /** The color. */
    Color color;

    /**
     * Constructs a new {@code ExprBox}.
     *
     * @param theSource the source expression
     * @param currentFontSize the current font size
     * @param minFontSize the minimum font size
     */
    ExprBox(final Expr theSource, final double currentFontSize, final double minFontSize) {

        super();

        this.source = theSource;
    }
}
