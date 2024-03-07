package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.ExprObject;

import java.awt.Color;
import java.awt.Font;

/**
 * An object that stores all formatting and layout data for an expression object.
 */
public class ExprObjectView extends ObjectBox {

    /** The expression object. */
    ExprObject object;

    /** The font. */
    Font font;

    /** The color. */
    Color color;

    /**
     * Constructs a new {@code ExprObjectView}.
     */
    ExprObjectView() {

        super();
    }
}
