package dev.mathops.assessment.expression.editmodel;

/**
 * A glyph that represents a variable name with no subscript, and with optional accent.
 *
 * <p>
 * When evaluated, this glyph evaluates to the value of its variable, or to an error if the variable does not exist.
 */
public final class ExprGlyphVarName extends AbstractExprGlyph {

    /** The accent to apply to the variable name. */
    private EVariableAccent accent;

    /** True to bold-face the variable name. */
    private boolean bold;

    /** The variable name. */
    public final Expr varname;

    /**
     * Constructs a new {@code ExprGlyphVarName}.
     */
    public ExprGlyphVarName() {

        super();

        this.varname = new Expr();
    }

    /**
     * Gets the total number of cursor steps it would take to cross this glyph from left to right.
     *
     * <p>
     * This construction has one step to move the cursor into the variable name, then the number of steps in the
     * variable name, and finally a step to move out of the variable name.
     *
     * @return the number of cursor steps
     */
    public int getNumCursorSteps() {

        return 2 + this.varname.getNumCursorSteps();
    }
}
