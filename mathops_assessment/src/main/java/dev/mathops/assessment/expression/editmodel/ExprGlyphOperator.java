package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents an operator.
 */
public class ExprGlyphOperator extends AbstractExprGlyph {

    /** The operator. */
    public final EOperatorSymbol operator;

    /**
     * Constructs a new {@code ExprGlyphOperator}.
     *
     * @param theOperator the operator
     */
    public ExprGlyphOperator(final EOperatorSymbol theOperator) {

        super();

        this.operator = theOperator;
    }
}
