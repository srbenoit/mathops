package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents an "IF-THEN-ELSE" construction, with one subexpression for IF, one for THEN, and one for
 * ELSE.
 */
public class ExprGlyphIfThenElse extends AbstractExprGlyph {

    /** The "IF" clause. */
    public final ExprGlyphSequence ifClause;

    /** The "THEN" clause. */
    public final ExprGlyphSequence thenClause;

    /** The "ELSE" clause. */
    public final ExprGlyphSequence elseClause;

    /**
     * Constructs a new {@code ExprGlyphIfThenElse}.
     */
    public ExprGlyphIfThenElse() {

        super();

        this.ifClause = new ExprGlyphSequence();
        this.thenClause = new ExprGlyphSequence();
        this.elseClause = new ExprGlyphSequence();
    }
}
