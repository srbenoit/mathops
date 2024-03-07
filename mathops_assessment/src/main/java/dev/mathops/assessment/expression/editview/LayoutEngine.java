package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.Expr;

/**
 * An engine that generates a sequence of {@code ExprObjectView}s for a given {@code Expr} object.
 */
public enum LayoutEngine {
    ;

    /**
     * Performs layout of an expression.
     * @param fontSize the font size
     * @param minFontSize the minimum font size
     * @param expr the expression to lay out
     * @return the laid out expression
     */
    static ExprObjectView layoutExpr(final float fontSize, final float minFontSize, final Expr expr) {

        // Font sizes decrease by 74% for each exponent level.
        // Bottom of superscript is 60% up ascent
        // Center of subscript is on baseline

        return null;
    }
}
